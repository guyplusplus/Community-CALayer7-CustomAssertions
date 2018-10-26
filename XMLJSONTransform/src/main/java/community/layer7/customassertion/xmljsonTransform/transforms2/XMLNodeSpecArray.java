package community.layer7.customassertion.xmljsonTransform.transforms2;

import org.json.JSONObject;

public class XMLNodeSpecArray extends XMLNodeSpec {
	
	private String itemsRef;
	private XMLNodeSpec itemsXMLNodeSpec;
	
	public XMLNodeSpecArray() {
		super(TYPE_ARRAY);
	}
	
	public String getItemsRef() {
		return itemsRef;
	}

	public XMLNodeSpec getItemsXMLNodeSpec() {
		return itemsXMLNodeSpec;
	}

	public boolean isXMLWrapped() {
		return isXMLWrapped;
	}

	@Override
	public String calculateTargetFullXMLName(String objectName) {
		if(isXMLWrapped) {
			//in case of wrapped array, return value if either xmlName or xmlPrefix is defined
			if(xmlName != null || xmlPrefix != null) {
				String prefix = (xmlPrefix == null ? "" : xmlPrefix + ":");
				return prefix + (xmlName == null ? objectName : xmlName);
			}
		}
		//then need to look at inner object
		return itemsXMLNodeSpec.calculateTargetFullXMLName(objectName);
	}
	
	@Override
	public void loadJSONValue(JSONObject schema, String valueDesriptionForException) throws JSONSchemaLoadException {
		super.loadJSONValue(schema, valueDesriptionForException);
		JSONObject items = schema.optJSONObject("items");
		if(items == null)
			throw new JSONSchemaLoadException("Items can not be found for object '" + valueDesriptionForException + "'");
		//check for $ref
		String ref = items.optString("$ref", null);
		if(ref != null) {
			itemsRef = ref;
			return;
		}
		String itemsType = items.optString("type", null);
		if(itemsType == null)
			throw new JSONSchemaLoadException("Type is not defined for items of object '" + valueDesriptionForException + "'");
		if(itemsType.equals("object")) {
			itemsXMLNodeSpec = new XMLNodeSpecObject();
			itemsXMLNodeSpec.loadJSONValue(items, valueDesriptionForException);
			System.out.println("added items object");
			return;
		}
		if(itemsType.equals("array")) {
			XMLNodeSpecArray itemsXMLNodeSpecArray = new XMLNodeSpecArray();
			itemsXMLNodeSpec = itemsXMLNodeSpecArray;
			itemsXMLNodeSpecArray.loadJSONValue(items, valueDesriptionForException);
			//check that items array is wrapped
			if(!itemsXMLNodeSpecArray.isXMLWrapped())
				throw new JSONSchemaLoadException("Nested array should always be wrapped for array '" + valueDesriptionForException + "'");
			System.out.println("added items array");
			return;
		}
		int type = typeStringToTYPE(itemsType);
		if(type == TYPE_UNDEFINED)
			throw new JSONSchemaLoadException("Type is undefined for for object '" + valueDesriptionForException + "'");
		itemsXMLNodeSpec = new XMLNodeSpecSimpleValue(type);
		itemsXMLNodeSpec.loadJSONValue(items, valueDesriptionForException);
		System.out.println("added items simple value");
	}
}
