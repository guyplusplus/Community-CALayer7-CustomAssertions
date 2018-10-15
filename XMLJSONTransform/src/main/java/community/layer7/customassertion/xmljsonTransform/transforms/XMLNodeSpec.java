package community.layer7.customassertion.xmljsonTransform.transforms;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

/**
 * @author Guy Deffaux
 * October 2018
 */
public class XMLNodeSpec {
	
	public static final int TYPE_UNDEFINED = 0;
	public static final int TYPE_OBJECT = 1;
	public static final int TYPE_ARRAY_WRAPPER = 2;
	public static final int TYPE_STRING = 3;
	public static final int TYPE_NUMBER = 4;
	public static final int TYPE_INTEGER = 5;
	public static final int TYPE_BOOLEAN = 6;
	
	private int nodeType = TYPE_UNDEFINED;
	private String originalJSONName = null;
	private String targetXMLName = null;
	private boolean isXMLAttribute = false; //for simple types only
	private boolean isArrayItem = false; //for array. Is this entry a wrapper or a real array item
	private HashMap<String, XMLNodeSpec> xmlElements = new HashMap<String, XMLNodeSpec>();
	private HashMap<String, XMLNodeSpec> xmlAttributes = new HashMap<String, XMLNodeSpec>();
	private ArrayList<String> nonWrappedJSONArrayNames = new ArrayList<String>();
	
	public XMLNodeSpec(XMLNodeSpec root) {
		nodeType = TYPE_OBJECT;
		xmlElements.put(root.getTargetXMLName(), root);
		System.out.println("added object ELEMENT " + root.getTargetXMLName() + " to root container");
	}
	
	public XMLNodeSpec() {
		
	}
	
	public XMLNodeSpec getChildElementXMLNodeSpecByName(String fullname) {
		return xmlElements.get(fullname);
	}
	
	public XMLNodeSpec getAttributeXMLNodeSpecByName(String fullname) {
		return xmlAttributes.get(fullname);
	}
	
	public int getNodeType() {
		return nodeType;
	}
	
	public String getTargetXMLName() {
		return targetXMLName;
	}
	
	public String getOriginalJSONName() {
		return originalJSONName;
	}
	
	public boolean isXMLAttribute() {
		return isXMLAttribute;
	}
	
	public ArrayList<String> getNonNestedJSONArrayNames() {
		return nonWrappedJSONArrayNames;
	}
	
	public boolean isArrayItem() {
		return isArrayItem;
	}
	
	public void loadJSONObject(JSONObject schema, String originalJSONName) throws JSONSchemaLoadException {
		this.originalJSONName = originalJSONName;
		nodeType = TYPE_OBJECT;
		JSONObject xml = schema.optJSONObject("xml");
		targetXMLName = buildTargetXMLFullname(originalJSONName, xml);
		//load all properties
		JSONObject properties = schema.optJSONObject("properties");
		if(properties == null)
			throw new JSONSchemaLoadException("Properties can not be found for object '" + originalJSONName + "'");
		for(String propertyName:properties.keySet()) {
			if(propertyName.trim().length() == 0)
				throw new JSONSchemaLoadException("One property name (after trim) is empty for object '" + originalJSONName + "'");				
			XMLNodeSpec xmlNodeSpec = new XMLNodeSpec();
			JSONObject propertyObject = properties.getJSONObject(propertyName);
			String propertyType = propertyObject.optString("type");
			if(propertyType == null)
				throw new JSONSchemaLoadException("Type is not defined for property '" + propertyName + "' for object '" + originalJSONName + "'");
			if(propertyType.equals("object")) {
				xmlNodeSpec.loadJSONObject(propertyObject, propertyName);
				checkKeyDoesNotExistInHasMap(xmlElements, xmlNodeSpec.getTargetXMLName(), originalJSONName);
				xmlElements.put(xmlNodeSpec.getTargetXMLName(), xmlNodeSpec);
				System.out.println("added object ELEMENT " + xmlNodeSpec.getTargetXMLName() + " to " + getTargetXMLName());
			}
			else if(propertyType.equals("array")) {
				xmlNodeSpec.loadJSONArray(propertyObject, propertyName);
				checkKeyDoesNotExistInHasMap(xmlElements, xmlNodeSpec.getTargetXMLName(), originalJSONName);
				xmlElements.put(xmlNodeSpec.getTargetXMLName(), xmlNodeSpec);
				//ensure if it is not a wrapper element that we create up-front the JSON array
				if(xmlNodeSpec.isArrayItem())
					//ensure that if no item do exist, at least an empty array is created
					nonWrappedJSONArrayNames.add(xmlNodeSpec.getOriginalJSONName());
				System.out.println("added array ELEMENT " + xmlNodeSpec.getTargetXMLName() + " to " + getTargetXMLName());
			}
			else {
				xmlNodeSpec.loadJSONProperty(propertyObject, propertyName, propertyType);
				if(xmlNodeSpec.isXMLAttribute) {
					checkKeyDoesNotExistInHasMap(xmlAttributes, xmlNodeSpec.getTargetXMLName(), originalJSONName);
					xmlAttributes.put(xmlNodeSpec.getTargetXMLName(), xmlNodeSpec);
					System.out.println("added property ATTRIBUTE " + xmlNodeSpec.getTargetXMLName() + " to " + getTargetXMLName());
				}
				else {
					checkKeyDoesNotExistInHasMap(xmlElements, xmlNodeSpec.getTargetXMLName(), originalJSONName);
					xmlElements.put(xmlNodeSpec.getTargetXMLName(), xmlNodeSpec);
					System.out.println("added property ELEMENT " + xmlNodeSpec.getTargetXMLName() + " to " + getTargetXMLName());
				}
			}
		}
	}
	
	private void checkKeyDoesNotExistInHasMap(HashMap<String, XMLNodeSpec> hashMap, String key, String jsonPropertyName) throws JSONSchemaLoadException {
		if(hashMap == null || key == null)
			return;
		if(hashMap.containsKey(key))
			throw new JSONSchemaLoadException("Element or property '" + key + "' is duplicated for JSON object property '" + jsonPropertyName + "'");
	}
	
	private void loadJSONArray(JSONObject arraySchema, String arrayOriginalJSONName) throws JSONSchemaLoadException {
		this.originalJSONName = arrayOriginalJSONName;
		JSONObject xmlFromArray = arraySchema.optJSONObject("xml");
		JSONObject items = arraySchema.optJSONObject("items");
		if(items == null)
			throw new JSONSchemaLoadException("Items is not defined for array '" + arrayOriginalJSONName + "'");
		JSONObject xmlFromItems = items.optJSONObject("xml");
		String itemsTargetXMLName = buildTargetXMLFullname(arrayOriginalJSONName, xmlFromItems);
		//if wrapped and have name in array.xml, we use this name
		boolean isXMLWrappedArray = false;
		if(xmlFromArray != null) {
			isXMLWrappedArray = xmlFromArray.optBoolean("wrapped", false);				
		}
		XMLNodeSpec itemsXMLNodeSpec = null;
		if(isXMLWrappedArray) {
			nodeType = TYPE_ARRAY_WRAPPER;
			targetXMLName = buildTargetXMLFullname(arrayOriginalJSONName, xmlFromArray);			
			itemsXMLNodeSpec = new XMLNodeSpec();
			itemsXMLNodeSpec.originalJSONName = arrayOriginalJSONName;
			nonWrappedJSONArrayNames.add(arrayOriginalJSONName);
			checkKeyDoesNotExistInHasMap(xmlElements, itemsTargetXMLName, arrayOriginalJSONName);
			this.xmlElements.put(itemsTargetXMLName, itemsXMLNodeSpec);
			System.out.println("added wrapped ELEMENT " + itemsTargetXMLName + " to " + getTargetXMLName());
		}
		else {
			itemsXMLNodeSpec = this;
		}
		itemsXMLNodeSpec.targetXMLName = itemsTargetXMLName;
		itemsXMLNodeSpec.isArrayItem = true;
		
		String itemsType = items.optString("type", null);
		if(itemsType == null)
			throw new JSONSchemaLoadException("Type is not defined for items of array '" + arrayOriginalJSONName + "'");
		if(itemsType.equals("string"))
			itemsXMLNodeSpec.nodeType = TYPE_STRING;
		else if(itemsType.equals("number"))
			itemsXMLNodeSpec.nodeType = TYPE_NUMBER;
		else if(itemsType.equals("integer"))
			itemsXMLNodeSpec.nodeType = TYPE_INTEGER;
		else if(itemsType.equals("boolean"))
			itemsXMLNodeSpec.nodeType = TYPE_BOOLEAN;
		else if(itemsType.equals("object")) {
			itemsXMLNodeSpec.nodeType = TYPE_OBJECT;
			//load all properties of object
			itemsXMLNodeSpec.loadJSONObject(items, arrayOriginalJSONName);
			System.out.println("loaded object ELEMENT " + itemsXMLNodeSpec.getTargetXMLName() + " to " + getTargetXMLName());
		}
		else if(itemsType.equals("array")) {
			XMLNodeSpec nestedArrayItemsXMLNodeSpec = new XMLNodeSpec();
			nestedArrayItemsXMLNodeSpec.loadJSONArray(items, arrayOriginalJSONName);
			if(nestedArrayItemsXMLNodeSpec.getNodeType() != TYPE_ARRAY_WRAPPER)
				throw new JSONSchemaLoadException("Nested array should always be wrapped for array " + arrayOriginalJSONName + "'");
			itemsXMLNodeSpec.nodeType = TYPE_ARRAY_WRAPPER;
			itemsXMLNodeSpec.xmlElements = nestedArrayItemsXMLNodeSpec.xmlElements;
			itemsXMLNodeSpec.nonWrappedJSONArrayNames = nestedArrayItemsXMLNodeSpec.nonWrappedJSONArrayNames;
			System.out.println("added nested array ELEMENT to " + itemsXMLNodeSpec.getTargetXMLName());
		}
		else
			throw new JSONSchemaLoadException("Unknown type '" + itemsType+ "' for nested array '" + arrayOriginalJSONName + "'");
	}

	private void loadJSONProperty(JSONObject propertySchema, String propertyOriginalJSONName, String propertyType) throws JSONSchemaLoadException {
		this.originalJSONName = propertyOriginalJSONName;
		if(propertyType.equals("string"))
			nodeType = TYPE_STRING;
		else if(propertyType.equals("number"))
			nodeType = TYPE_NUMBER;
		else if(propertyType.equals("integer"))
			nodeType = TYPE_INTEGER;
		else if(propertyType.equals("boolean"))
			nodeType = TYPE_BOOLEAN;
		else
			throw new JSONSchemaLoadException("Unknown type '" + propertyType + "' for property '" + propertyOriginalJSONName + "'");
		JSONObject xml = propertySchema.optJSONObject("xml");
		targetXMLName = buildTargetXMLFullname(propertyOriginalJSONName, xml);
		if(xml != null) {
			if(xml.optBoolean("attribute", false))
				isXMLAttribute = true;
		}
	}
		
	private String buildTargetXMLFullname(String originalJSONName, JSONObject xml) {
		if(xml == null)
			return originalJSONName;
		String xmlName = xml.optString("name", null);
		String xmlPrefix = xml.optString("prefix", null);
		if(xmlPrefix == null)
			return (xmlName == null ? originalJSONName : xmlName);
		return xmlPrefix + ":" + (xmlName == null ? originalJSONName : xmlName);
	}
	
}
