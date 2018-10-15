package community.layer7.customassertion.xmljsonTransform.transforms;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guy Deffaux
 * October 2018
 */
public class XMLToJSONConverter {
	
	private String inputXML;
	private XMLNodeSpec rootContainerXMLNodeSpec;
	private JSONObject output;
	//private static final String[] EMPTY_ARRAY = {};
	
	public XMLToJSONConverter(String inputXML, XMLNodeSpec rootContainerXMLNodeSpec) throws MapException {
		if(inputXML == null)
			throw new MapException("Input XML is null");
		if(rootContainerXMLNodeSpec == null)
			throw new MapException("rootXMLNodeSpec is null");
		this.inputXML = inputXML;
		this.rootContainerXMLNodeSpec = rootContainerXMLNodeSpec;
	}
	
	public void process() throws MapException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = null;
		try {
			reader = factory.createXMLStreamReader(new StringReader(inputXML));
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error");
		}
		//reader carries root element
		output = parseJSONObject(reader, rootContainerXMLNodeSpec, "");
	}
	
	public JSONObject getOutput() {
		return output;
	}

	static private JSONObject parseJSONObject(XMLStreamReader reader, XMLNodeSpec xmlNodeSpec, String xpath) throws MapException {
		String characters = ""; //Purposely not use StringBuilder as concatenation for this variable happens rarely
		JSONObject o = new JSONObject();
		XMLNodeSpec startedXMLNodeSpec = null;
		String startedXMLNodeXPath = null;

		System.out.println("parseJSONObject xpath:" + xpath);
		//add non wrapped arrays. When a wrapped array comes in, then the json array is added
		for(String jsonArrayName:xmlNodeSpec.getNonNestedJSONArrayNames()) {
			o.put(jsonArrayName, new JSONArray());
			System.out.println("--- proactive add array: " + jsonArrayName + " for xpath: " + xpath);
		}
		
		try {
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(characters.length() == 0 && reader.isWhiteSpace()) {
						System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					if(startedXMLNodeSpec == null)
						throw new MapException("Characters at this location are not allowed", xpath);
					characters = characters + reader.getText();
					System.out.println(xpath + " CHARACTERS: " + characters);
					continue;
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					//check if it is the end of a property element (innerXMLNodeSpec != null) or end of the current object
					if(startedXMLNodeSpec == null)
						//no object waiting for characters
						break;
					//it is the end of a simple PROPERTY element, add key and value (characters) to JSON object
					String key = startedXMLNodeSpec.getOriginalJSONName();
					if(startedXMLNodeSpec.isArrayItem()) {
						putSimpleKeyToJSONArray(o.getJSONArray(key),
								key,
								characters,
								startedXMLNodeSpec.getNodeType(), //actual items type is one level down
								startedXMLNodeXPath
								);
					}
					else {
						//simple property type (integer, string, boolean, number)
						putSimpleKeyToJSONObject(o,
								key,
								characters,
								startedXMLNodeSpec.getNodeType(),
								startedXMLNodeXPath,
								startedXMLNodeSpec.isArrayItem());						
					}
					//reset character and innerXMLNodeSpec. JSON object property handling is done
					characters = "";
					startedXMLNodeSpec = null;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					String fqElementName = reader.getLocalName(); //fully qualified (prefix:name, or name if no prefix)
					//add prefix is defined
					String prefix = reader.getPrefix();
					if(prefix != null && prefix.length() > 0)
						fqElementName = prefix + ":" + fqElementName;
					System.out.println(xpath + " START_ELEMENT: " + fqElementName);
					startedXMLNodeSpec = xmlNodeSpec.getChildElementXMLNodeSpecByName(fqElementName);
					if(startedXMLNodeSpec == null)
						throw new MapException("Element not found in JSON schema", xpath + "/" + fqElementName);
					System.out.println("START ELEMENT Found getOriginalJSONName:" + startedXMLNodeSpec.getOriginalJSONName() + ", getTargetXMLName:" + startedXMLNodeSpec.getTargetXMLName());
					//define currentPath
					startedXMLNodeXPath = xpath + "/" + fqElementName;
					if(startedXMLNodeSpec.isArrayItem()) {
						Object value = o.opt(startedXMLNodeSpec.getOriginalJSONName());
						if(value == null)
							startedXMLNodeXPath += "[1]";
						else 
							startedXMLNodeXPath += "[" + (((JSONArray)value).length() + 1) + "]";
					}
					System.out.println("startedXMLNodeXPath:" + startedXMLNodeXPath);
					if(startedXMLNodeSpec.getNodeType() == XMLNodeSpec.TYPE_OBJECT) {
						//parse attributes of the XML element (if any) first
						int attributeCount = reader.getAttributeCount();
						JSONObject attributeProperties = new JSONObject();
						for(int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
							String prefixName = reader.getAttributePrefix(attributeIndex);
							String attributeName = reader.getAttributeLocalName(attributeIndex);
							if(prefixName != null && prefixName.length() > 0)
								attributeName = prefixName + ":" + attributeName;
							//now attributeName is prefix:localName
							XMLNodeSpec attributeXMLNodeSpec = startedXMLNodeSpec.getAttributeXMLNodeSpecByName(attributeName);
							if(attributeXMLNodeSpec == null)
								throw new MapException("Attribute not found in JSON schema", startedXMLNodeXPath + "/@" + attributeName);
							putSimpleKeyToJSONObject(attributeProperties,
									attributeXMLNodeSpec.getOriginalJSONName(),
									reader.getAttributeValue(attributeIndex),
									attributeXMLNodeSpec.getNodeType(),
									startedXMLNodeXPath + "/@" + attributeName,
									false
									);
						}
						//check no duplicate
						if(!startedXMLNodeSpec.isArrayItem() && o.opt(startedXMLNodeSpec.getOriginalJSONName()) != null)
							throw new MapException("Duplicated element", startedXMLNodeXPath);						
						//then parse inner JSON properties (inner XML elements)
						System.out.println("startedXMLNodeXPath:" + startedXMLNodeXPath + " isArrayItem=" + startedXMLNodeSpec.isArrayItem());
						JSONObject innerObject = parseJSONObject(reader, startedXMLNodeSpec, startedXMLNodeXPath);
						if(startedXMLNodeSpec.isArrayItem())
							o.append(startedXMLNodeSpec.getOriginalJSONName(), innerObject);
						else
							o.put(startedXMLNodeSpec.getOriginalJSONName(), innerObject);
						System.out.println(" (2) o.put KEY=" + startedXMLNodeSpec.getOriginalJSONName());
						//if attributeProperties has properties, move them to innerObject
						for(String attributePropertyKey:attributeProperties.keySet())
							innerObject.put(attributePropertyKey, attributeProperties.get(attributePropertyKey));
						startedXMLNodeSpec = null;
					}
					else if(startedXMLNodeSpec.getNodeType() == XMLNodeSpec.TYPE_ARRAY_WRAPPER) {
						//then parse inner JSON array
						System.out.println("startedXMLNodeSpec.getTargetXMLName()=" + startedXMLNodeSpec.getTargetXMLName());
						JSONObject innerXMLObject = parseJSONObject(reader, startedXMLNodeSpec, startedXMLNodeXPath);
						Object innerXMLNodeArray = innerXMLObject.opt(startedXMLNodeSpec.getOriginalJSONName());
						if(startedXMLNodeSpec.isArrayItem()) {
							//case of nested array 2nd level
							//
							//  myArrayOfArrays (wrapper) -> myArray (wrapper, arrayItem) -> myContent (arrayItem)
							//  startedXMLNodeSpec = myArray

							if(innerXMLNodeArray == null)
								//there is no myContent, so make myArray an empty array
								o.put(startedXMLNodeSpec.getOriginalJSONName(), new JSONArray());
							else if(innerXMLNodeArray instanceof JSONArray)
								//set the array content of myArray to be the content of myContent array
								o.append(startedXMLNodeSpec.getOriginalJSONName(), innerXMLNodeArray);
							else
								throw new MapException("Bug in the code, class: " + innerXMLNodeArray.getClass());
						}
						else {
							//simple case where an object o carries a property which is an array
							//
							//  myArrayOfArrays (wrapper) -> myContent (arrayItem) (1)
							//  myArrayOfArrays (wrapper) -> myArray (wrapper, arrayItem) -> myContent (arrayItem) (2)
							//  startedXMLNodeSpec = myArrayOfArrays
							
							//check this array does not exist already
							if(o.opt(startedXMLNodeSpec.getOriginalJSONName()) != null)
								throw new MapException("Duplicated element", startedXMLNodeXPath);
							if(innerXMLNodeArray == null)
								//there is no myContent (1) or no myArray (2), so make myArrayOfArrays an empty array
								o.put(startedXMLNodeSpec.getOriginalJSONName(), new JSONArray());
							else if(innerXMLNodeArray instanceof JSONArray)
								//put content of myContent (1) or no myArray (2) to myArrayOfArrays
								o.put(startedXMLNodeSpec.getOriginalJSONName(), innerXMLNodeArray);
							else
								throw new MapException("Bug in the code, class: " + innerXMLNodeArray.getClass());
						}
						
						System.out.println(" (2) o.put KEY=" + startedXMLNodeSpec.getOriginalJSONName());
						startedXMLNodeSpec = null;
						continue;
					}
					//if not TYPE_OBJECT it is assumed to be a normal property, it will be handled at END_ELEMENT event
					//since innerXMLNodeSpec != null
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					break;
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", xpath);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MapException("Problem to create the target JSON object", xpath);
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error at " + e.getLocation().toString(), xpath);
		}
		return o;
	}
	
	static private void putSimpleKeyToJSONObject(JSONObject o, String key, String characters, int nodeType, String path, boolean isArrayItem) throws MapException {
		System.out.println(" (1) o.put KEY=" + key);		
		if(isArrayItem) {
			o.append(key, createObjectFromSimpleProperty(characters, nodeType, path));
			return;
		}
		//check no duplicate key first
		if(o.opt(key) != null)
			throw new MapException("Duplicated element", path);
		//then add
		o.put(key, createObjectFromSimpleProperty(characters, nodeType, path));
	}
	
	static private void putSimpleKeyToJSONArray(JSONArray a, String key, String characters, int nodeType, String path) throws MapException {
		System.out.println(" (1) o.put array KEY=" + key);	
		a.put(createObjectFromSimpleProperty(characters, nodeType, path));
	}
	
	static private Object createObjectFromSimpleProperty(String characters, int nodeType, String path) throws MapException {
		String charactersTrimmed = characters.trim();
		if(nodeType == XMLNodeSpec.TYPE_STRING) {
			return charactersTrimmed;
		}
		else if(charactersTrimmed.equalsIgnoreCase("null"))
			return JSONObject.NULL;
		else if(nodeType == XMLNodeSpec.TYPE_BOOLEAN) {
			if(charactersTrimmed.equalsIgnoreCase("true"))
				 return Boolean.TRUE;
			else if(charactersTrimmed.equalsIgnoreCase("false"))
				return Boolean.FALSE;
			else
				throw new MapException("Invalid boolean", path);
		}
		else if(nodeType == XMLNodeSpec.TYPE_INTEGER) {
			try {
				if(charactersTrimmed.length() > 9)
					//we use this trick to guess it could be a very large integer (int max value is 10 chars)
					//we keep Integer case for performance optimization
					return new BigInteger(charactersTrimmed);
				else
					return Integer.parseInt(charactersTrimmed);
			} catch (NumberFormatException e) {
				throw new MapException("Failure to convert value to an integer", path);
			}
		}
		else if(nodeType == XMLNodeSpec.TYPE_NUMBER) {
			try {
				return new BigDecimal(charactersTrimmed);
			} catch (NumberFormatException e) {
				throw new MapException("Failure to convert value to a number", path);
			}
		}
		else
			throw new MapException("Unknown XMLNodeSpec type " + nodeType, path);
	}

}
