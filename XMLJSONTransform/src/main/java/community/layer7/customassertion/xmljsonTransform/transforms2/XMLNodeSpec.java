package community.layer7.customassertion.xmljsonTransform.transforms2;

import org.json.JSONObject;

public abstract class XMLNodeSpec {
	
	public static final int TYPE_UNDEFINED = 0;
	public static final int TYPE_OBJECT = 1;
	public static final int TYPE_ARRAY = 2;
	public static final int TYPE_STRING = 3;
	public static final int TYPE_NUMBER = 4;
	public static final int TYPE_INTEGER = 5;
	public static final int TYPE_BOOLEAN = 6;
	
	protected int nodeType = TYPE_UNDEFINED;
	protected String xmlName = null;
	protected String xmlPrefix = null;
	protected String xmlNamespace = null;
	protected boolean isXMLAttribute = false;
	protected boolean isXMLWrapped = false;
	
	protected static int typeStringToTYPE(String type) {
		if(type == null)
			return TYPE_UNDEFINED;
		if(type.equals("object"))
			return TYPE_OBJECT;
		if(type.equals("array"))
			return TYPE_ARRAY;
		if(type.equals("string"))
			return TYPE_STRING;
		if(type.equals("number"))
			return TYPE_NUMBER;
		if(type.equals("integer"))
			return TYPE_INTEGER;
		if(type.equals("boolean"))
			return TYPE_BOOLEAN;
		return TYPE_UNDEFINED;
	}
	
	public XMLNodeSpec(int nodeType) {
		this.nodeType = nodeType;
	}
	
	public int getNodeType() {
		return nodeType;
	}
	
	public String calculateTargetFullXMLName(String objectName) {
		String name = (xmlName == null ? objectName : xmlName);
		String fullXMLName = (xmlPrefix == null ? name : xmlPrefix + ":" + name);
		return fullXMLName;
	}
	
	public String getXmlName() {
		return xmlName;
	}
	
	public String getXmlPrefix() {
		return xmlPrefix;
	}
	
	public String getXmlNamespace() {
		return xmlNamespace;
	}

//	public boolean isXMLAttribute() throws JSONSchemaLoadException {
//		throw new JSONSchemaLoadException("isAttribute() is not allowed for this node type: " + nodeType);
//	}
//
//	public boolean isXMLWrapped() throws JSONSchemaLoadException {
//		throw new JSONSchemaLoadException("isWrapped() is not allowed for this node type: " + nodeType);
//	}
	
	private void loadXML(JSONObject xml) {
		if(xml == null)
			return;
		xmlName = xml.optString("name", null);
		xmlPrefix = xml.optString("prefix", null);
		xmlNamespace = xml.optString("namespace", null);
		isXMLAttribute = xml.optBoolean("attribute", false);
		isXMLWrapped = xml.optBoolean("wrapped", false);
		//constructFullXMLName();
	}
	
	public void loadJSONValue(JSONObject schema, String valueDesriptionForException) throws JSONSchemaLoadException {
		loadXML(schema.optJSONObject("xml"));
	}

}
