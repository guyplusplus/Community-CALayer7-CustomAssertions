package community.layer7.customassertion.xmljsonTransform.transforms2;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guy D.
 * October 2018
 */
public class JSONSchemaForXML {
	
	public static final String DEFAULT_XML_ROOT_ELEMENT_NAME = "root";
	
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
				XMLNodeSpecSimpleValue rootXMLNodeSpecSimpleValue = new XMLNodeSpecSimpleValue(rootNodeType);
				rootXMLNodeSpecSimpleValue.loadJSONValue(schema, "$");
				rootXMLNodeSpec = rootXMLNodeSpecSimpleValue;
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
	
	public long getCreatedTimeInMs() {
		return createdTimeInMs;
	}

}
