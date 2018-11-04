package community.layer7.customassertion.xmljsonTransform.transforms2;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Document;

/**
 * @author Guy D.
 * October 2018
 */
public class JSONSchemaForXML {
	
	static final String DEFAULT_XML_ROOT_ELEMENT_NAME = "root";
	private static final String XML_INDENT_SPACES = "4";
	private static final int JSON_INDENT_SPACES = 2;
	
	private static final Logger logger = Logger.getLogger(JSONSchemaForXML.class.getName());

	private long createdTimeInMs;
	private PropertyXMLNodeSpec rootPropertyXMLNodeSpecObject;
	
	private HashMap<String, PropertyXMLNodeSpec> definitionXMLNodeSpecHashMap = new HashMap<String, PropertyXMLNodeSpec>();
	
	public JSONSchemaForXML(String source) throws JSONSchemaLoadException {
		createdTimeInMs = System.currentTimeMillis();
		JSONObject schema = null;
		if(source == null)
			throw new JSONSchemaLoadException("Failed to parse JSON: source is null");
		try {
			schema = new JSONObject(source);
		}
		catch(JSONException e) {
			throw new JSONSchemaLoadException("Failed to parse JSON: " + e);
		}
		//load main object
		rootPropertyXMLNodeSpecObject = loadJSONElement(schema);
		//load definitions
		JSONObject definitions = schema.optJSONObject("definitions");
		if(definitions != null) {
			for(String definitionName : definitions.keySet()) {
				JSONObject definition = definitions.getJSONObject(definitionName);
				PropertyXMLNodeSpec propertyXMLNodeSpec = loadJSONElement(definition);
				if(propertyXMLNodeSpec.getRef() != null)
					throw new JSONSchemaLoadException("Definition '" + definitionName + "' defined as a reference is not supported for this release");
				definitionXMLNodeSpecHashMap.put("#/definitions/" + definitionName, propertyXMLNodeSpec);
			}
			//1. resolve ref in definitions
			for(String definitionName:definitionXMLNodeSpecHashMap.keySet()) {
				PropertyXMLNodeSpec propertyXMLNodeSpec = definitionXMLNodeSpecHashMap.get(definitionName); //can't be null
				resolveRef(propertyXMLNodeSpec);
			}
		}
		//2. resolve main object
		resolveRef(rootPropertyXMLNodeSpecObject);
		//3. check root element if of type object
		if(rootPropertyXMLNodeSpecObject.getXmlNodeSpec().getNodeType() != XMLNodeSpec.TYPE_OBJECT)
			throw new JSONSchemaLoadException("Root object must be of type 'object'");
	}
	
	private PropertyXMLNodeSpec getDefinitionXMLNodeSpecHashMap(String ref) throws JSONSchemaLoadException {
		PropertyXMLNodeSpec innerPropertyXMLNodeSpec = definitionXMLNodeSpecHashMap.get(ref);
		if(innerPropertyXMLNodeSpec != null)
			return innerPropertyXMLNodeSpec;
		throw new JSONSchemaLoadException("Definition '" + ref + "' is not defined");
	}
	
	private void resolveRef(XMLNodeSpec xmlNodeSpec) throws JSONSchemaLoadException {
		if(xmlNodeSpec.getNodeType() == XMLNodeSpec.TYPE_OBJECT) {
			XMLNodeSpecObject xmlNodeSpecObject = (XMLNodeSpecObject)xmlNodeSpec;
			Iterator<String> propertyRefIterator = xmlNodeSpecObject.getRefs().keySet().iterator();
			while(propertyRefIterator.hasNext()) {
				String propertyRef = propertyRefIterator.next();
				PropertyXMLNodeSpec keyPropertyXMLNodeSpec = xmlNodeSpecObject.getRefs().get(propertyRef);
				propertyRefIterator.remove();
				resolveRef(keyPropertyXMLNodeSpec);
				xmlNodeSpecObject.addPropertyXMLNodeSpec(keyPropertyXMLNodeSpec);
			}
			for(PropertyXMLNodeSpec propertyXMLNodeSpec:xmlNodeSpecObject.getXMLAttributes().values())
				resolveRef(propertyXMLNodeSpec);
			for(PropertyXMLNodeSpec propertyXMLNodeSpec:xmlNodeSpecObject.getXMLElements().values())
				resolveRef(propertyXMLNodeSpec);			
		}
		else if(xmlNodeSpec.getNodeType() == XMLNodeSpec.TYPE_ARRAY) {
			XMLNodeSpecArray xmlNodeSpecArray = (XMLNodeSpecArray)xmlNodeSpec;
			String itemsRef = xmlNodeSpecArray.getItemsRef();
			if(itemsRef != null) {
				if(xmlNodeSpecArray.getItemsXMLNodeSpec() == null) {
					PropertyXMLNodeSpec itemsPropertyXMLNodeSpec = getDefinitionXMLNodeSpecHashMap(itemsRef);
					xmlNodeSpecArray.setItemsXMLNodeSpec(itemsPropertyXMLNodeSpec.getXmlNodeSpec());
					resolveRef(itemsPropertyXMLNodeSpec);
				}
				return;	
			}
			resolveRef(xmlNodeSpecArray.getItemsXMLNodeSpec());
		}
	}
	
	private void resolveRef(PropertyXMLNodeSpec propertyXMLNodeSpec) throws JSONSchemaLoadException {
		String ref = propertyXMLNodeSpec.getRef();
		if(ref != null) { 
			if(propertyXMLNodeSpec.getXmlNodeSpec() == null) {
				PropertyXMLNodeSpec innerPropertyXMLNodeSpec = getDefinitionXMLNodeSpecHashMap(ref);
				propertyXMLNodeSpec.setPropertyXMLNodeSpec(innerPropertyXMLNodeSpec.getXmlNodeSpec());
				resolveRef(innerPropertyXMLNodeSpec);
			}
			return;
		}
		resolveRef(propertyXMLNodeSpec.getXmlNodeSpec());
	}
	
	private PropertyXMLNodeSpec loadJSONElement(JSONObject schema) throws JSONSchemaLoadException {
		try {
			//check if ref
			String ref = schema.optString("$ref", null);
			if(ref != null)
				return new PropertyXMLNodeSpec(DEFAULT_XML_ROOT_ELEMENT_NAME, ref, null);
			String type = schema.optString("type", null);
			int rootNodeType = XMLNodeSpec.typeStringToTYPE(type);
			XMLNodeSpec rootXMLNodeSpec = null;
			if(rootNodeType == XMLNodeSpec.TYPE_OBJECT) {
				XMLNodeSpecObject rootXMLNodeSpecObject = new XMLNodeSpecObject();
				rootXMLNodeSpecObject.loadJSONValue(schema, "$");
				rootXMLNodeSpec = rootXMLNodeSpecObject;
			}
			else if(rootNodeType == XMLNodeSpec.TYPE_ARRAY) {
				XMLNodeSpecArray rootXMLNodeSpecArray = new XMLNodeSpecArray();
				rootXMLNodeSpecArray.loadJSONValue(schema, "$");
				rootXMLNodeSpec = rootXMLNodeSpecArray;
			}
			else {
				XMLNodeSpecPrimitiveType rootXMLNodeSpecPrimitiveType = new XMLNodeSpecPrimitiveType(rootNodeType);
				rootXMLNodeSpecPrimitiveType.loadJSONValue(schema, "$");
				rootXMLNodeSpec = rootXMLNodeSpecPrimitiveType;
			}
			return new PropertyXMLNodeSpec(DEFAULT_XML_ROOT_ELEMENT_NAME, null, rootXMLNodeSpec);
		}
		catch(JSONException e) {
			throw new JSONSchemaLoadException("Failed to parse JSON schema: " + e);
		}
	}
	
	public JSONObject mapXMLToJSON(String inputXML) throws MapException {
		XMLToJSONConverter converter = new XMLToJSONConverter(inputXML, (XMLNodeSpecObject)rootPropertyXMLNodeSpecObject.getXmlNodeSpec());
		converter.process();
		return converter.getOutput();
	}
	
	public Document mapJSONToXML(String inputJSON) throws MapException {
		JSONToXMLConverter converter = new JSONToXMLConverter(inputJSON, (XMLNodeSpecObject)rootPropertyXMLNodeSpecObject.getXmlNodeSpec());
		converter.process();
		return converter.getOutput();
	}
	
	public static String jsonToString(JSONObject o, boolean formatOutput) {
		return o.toString(formatOutput ? JSON_INDENT_SPACES : 0);
	}
	
	public static String xmlToString(Document inputDocument, boolean formatOutput) throws MapException {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, formatOutput ? "yes" : "no");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", XML_INDENT_SPACES);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, formatOutput ? "no" : "yes");
			DOMSource domSource = new DOMSource(inputDocument);
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			return sw.toString();
		} catch (Exception e) {
			//should never happen
			logger.log(Level.WARNING, "Failed to convert XML object to a string", e);
			throw new MapException("Failed to convert XML object to a string");
		}
	}
	
	public long getCreatedTimeInMs() {
		return createdTimeInMs;
	}

}
