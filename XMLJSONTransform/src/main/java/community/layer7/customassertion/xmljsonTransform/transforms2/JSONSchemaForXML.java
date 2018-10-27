package community.layer7.customassertion.xmljsonTransform.transforms2;

import java.util.HashMap;

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
				definitionXMLNodeSpecHashMap.put("#/definitions/" + definitionName, loadJSONElement(definition));
			}
		}
	}
	
	private PropertyXMLNodeSpec loadJSONElement(JSONObject schema) throws JSONSchemaLoadException {
		try {
			//check if ref
			String ref = schema.optString("$ref", null);
			if(ref != null)
				return new PropertyXMLNodeSpec(DEFAULT_XML_ROOT_ELEMENT_NAME, ref, null);
			//object is specified, check type is object
			String type = schema.optString("type", null);
			if(!"object".equals(type)) {
				throw new JSONSchemaLoadException("JSON schema top element must be of type 'object'");
			}
			XMLNodeSpecObject rootXMLNodeSpecObject = new XMLNodeSpecObject();
			rootXMLNodeSpecObject.loadJSONValue(schema, "$");
			return new PropertyXMLNodeSpec(DEFAULT_XML_ROOT_ELEMENT_NAME, null, rootXMLNodeSpecObject);
		}
		catch(JSONException e) {
			throw new JSONSchemaLoadException("Failed to parse JSON schema: " + e);
		}
	}
	
	public JSONObject mapXMLToJSON(String inputXML) throws MapException {
		System.out.println("***************");
		XMLToJSONConverter converter = new XMLToJSONConverter(inputXML, (XMLNodeSpecObject)rootPropertyXMLNodeSpecObject.getXmlNodeSpec());
		converter.process();
		return converter.getOutput();
	}
	
	public long getCreatedTimeInMs() {
		return createdTimeInMs;
	}

}
