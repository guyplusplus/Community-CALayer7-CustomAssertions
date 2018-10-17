package community.layer7.customassertion.xmljsonTransform.transforms;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guy Deffaux
 * October 2018
 */
public class JSONSchemaForXML {
	
	private static final String DEFAULT_XML_ROOT_ELEMENT_NAME = "root";
	
	private long createdTimeInMs;
	private XMLNodeSpec rootXMLNodeSpec;
	private XMLNodeSpec rootContainerXMLNodeSpec;
	private HashMap<String, XMLNodeSpec> definitionXMLNodeSpecHashMap = new HashMap<String, XMLNodeSpec>();
	
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
		//ensure type is object
		String type = schema.optString("type", null);
		if(!"object".equals(type)) {
			throw new JSONSchemaLoadException("JSON schema top element must be of type 'object'");
		}
		rootXMLNodeSpec = loadJSONObject(schema);
		rootContainerXMLNodeSpec = new XMLNodeSpec(rootXMLNodeSpec);
		//load definitions
		JSONObject definitions = schema.optJSONObject("definitions");
		if(definitions != null) {
			for(String definitionName : definitions.keySet()) {
				JSONObject definition = definitions.getJSONObject(definitionName);
				definitionXMLNodeSpecHashMap.put("#/definitions/" + definitionName, loadJSONObject(definition));
			}
		}
	}
	
	private XMLNodeSpec loadJSONObject(JSONObject schema) throws JSONSchemaLoadException {
		XMLNodeSpec aNodeSpec = new XMLNodeSpec();
		try {
			aNodeSpec.loadJSONObject(schema, DEFAULT_XML_ROOT_ELEMENT_NAME);
		}
		catch(JSONException e) {
			throw new JSONSchemaLoadException("Failed to parse JSON schema: " + e);
		}
		return aNodeSpec;
	}
	
	public JSONObject mapXMLToJSON(String inputXML) throws MapException {
		System.out.println("***************");
		XMLToJSONConverter converter = new XMLToJSONConverter(inputXML, rootContainerXMLNodeSpec);
		converter.process();
		JSONObject rootIncludedObject = converter.getOutput();
		//get the first key
		return rootIncludedObject.getJSONObject(rootIncludedObject.keys().next());
	}
	
	public long getCreatedTimeInMs() {
		return createdTimeInMs;
	}

}
