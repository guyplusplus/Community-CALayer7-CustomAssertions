package community.layer7.customassertion.xmljsonTransform.transforms2;

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

public class XMLToJSONConverter {

	private String inputXML;
	private XMLNodeSpecObject rootContainerXMLNodeSpec;
	private JSONObject output;

	public XMLToJSONConverter(String inputXML, XMLNodeSpecObject rootContainerXMLNodeSpec) throws MapException {
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
		moveReaderToFirstStartElement(reader);
		//ensure root name is correct
		String fqElementName = calculateFQElementName(reader);
		String expectedFQELementName = rootContainerXMLNodeSpec.calculateTargetFullXMLName(JSONSchemaForXML.DEFAULT_XML_ROOT_ELEMENT_NAME);
//		if(expectedFQELementName == null)
//			expectedFQELementName = JSONSchemaForXML.DEFAULT_XML_ROOT_ELEMENT_NAME;
		if(!fqElementName.equals(expectedFQELementName))
			throw new MapException("XML root node name is invalid", "/" + fqElementName);
		output = parseJSONObject(reader, rootContainerXMLNodeSpec, "/" + fqElementName);		
	}
	
	public JSONObject getOutput() {
		return output;
	}
	
	static private void moveReaderToFirstStartElement(XMLStreamReader reader) throws MapException {
		String xpath = "";
		try {
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(reader.isWhiteSpace()) {
						System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					throw new MapException("Characters at this location are not allowed", xpath);
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					throw new MapException("End element not allowed", xpath);
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					System.out.println(xpath + " START_ELEMENT: " + reader.getLocalName());
					break;
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
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error at " + e.getLocation().toString(), xpath);
		}		
	}
	
	static private Object parseJSONValue(XMLStreamReader reader, XMLNodeSpec xmlNodeSpec, String xpath) throws MapException {
		if(xmlNodeSpec.getNodeType() == XMLNodeSpec.TYPE_OBJECT)
			return parseJSONObject(reader, (XMLNodeSpecObject)xmlNodeSpec, xpath);
		if(xmlNodeSpec.getNodeType() == XMLNodeSpec.TYPE_ARRAY)
			return parseJSONArray(reader, (XMLNodeSpecArray)xmlNodeSpec, xpath);
		return parseJSONSimpleValue(reader, (XMLNodeSpecSimpleValue)xmlNodeSpec, xpath);
	}
	
	static private JSONArray parseJSONArray(XMLStreamReader reader, XMLNodeSpecArray xmlNodeSpecArray, String xpath) throws MapException {
		return null;
	}
	
	static private Object parseJSONSimpleValue(XMLStreamReader reader, XMLNodeSpecSimpleValue xmlNodeSpecSimpleValue, String xpath) throws MapException {
		String characters = ""; //Purposely not use StringBuilder as concatenation for this variable happens rarely
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
					characters += reader.getText();
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					break;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					System.out.println(xpath + " START_ELEMENT: " + reader.getLocalName());
					throw new MapException("New XML element not allowed for a simple property (string, boolean, integer, number)", xpath + "/" + reader.getLocalName());
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					throw new MapException("Too early end of XML document", xpath);
				}
				else if(eventType == XMLStreamConstants.COMMENT) {
					continue;
				}
				else {
					throw new MapException("Unknown StAX event type '" + eventType + "' while parsing XML", xpath);
				}
			}
			return createObjectFromSimpleProperty(characters, xmlNodeSpecSimpleValue.getNodeType(), xpath);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new MapException("Problem to create the target JSON object", xpath);
		} catch (XMLStreamException e) {
			throw new MapException("XML parsing error at " + e.getLocation().toString(), xpath);
		}
	}

	static private JSONObject parseJSONObject(XMLStreamReader reader, XMLNodeSpecObject xmlNodeSpecObject, String xpath) throws MapException {
		System.out.println("parseJSONObject xpath:" + xpath);
		JSONObject o = new JSONObject();
		//add non wrapped arrays. When a wrapped array comes in, then the json array is added
		for(String jsonArrayName:xmlNodeSpecObject.getNonWrappedJSONArrayNames()) {
			o.put(jsonArrayName, new JSONArray());
			System.out.println("--- proactive add array: " + jsonArrayName + " for xpath: " + xpath);
		}
		try {
			if(!reader.hasNext())
				return o;
			//handle attributes
			int attributeCount = reader.getAttributeCount();
			for(int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
				String prefixName = reader.getAttributePrefix(attributeIndex);
				String fqAttributeName = reader.getAttributeLocalName(attributeIndex);
				if(prefixName != null && prefixName.length() > 0)
					fqAttributeName = prefixName + ":" + fqAttributeName;
				//now attributeName is prefix:localName
				PropertyXMLNodeSpec attributePropertyXMLNodeSpec = xmlNodeSpecObject.getAttributePropertyXMLNodeSpecByName(fqAttributeName);
				if(attributePropertyXMLNodeSpec == null)
					throw new MapException("Attribute not found in JSON schema", xpath + "/@" + fqAttributeName);
				if(!(attributePropertyXMLNodeSpec.getXmlNodeSpec() instanceof XMLNodeSpecSimpleValue))
					throw new MapException("Attribute should be a simple type (boolean, integer, number, string)", xpath + "/@" + fqAttributeName);
				if(prefixName != null && attributePropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace() != null)
					if(!attributePropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace().equals(reader.getAttributeNamespace(attributeIndex)))
						throw new MapException("Invalid namespace", xpath + "/@" + fqAttributeName);
				
				Object attributeValue = createObjectFromSimpleProperty(reader.getAttributeValue(attributeIndex), attributePropertyXMLNodeSpec.getXmlNodeSpec().getNodeType(), xpath + "/@" + fqAttributeName); 
				addKeyValueToJSONObject(attributePropertyXMLNodeSpec.getPropertyName(), attributeValue, o, xpath + "/@" + fqAttributeName, false);
			}
			while(true) {
				if(!reader.hasNext())
					break;
				int eventType = reader.next();
				if(eventType == XMLStreamConstants.CHARACTERS) {
					if(reader.isWhiteSpace()) {
						System.out.println(xpath + " CHARACTERS white spaces, ignore");
						continue;
					}
					throw new MapException("Characters at this location are not allowed", xpath);
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT) {
					System.out.println(xpath + " END_ELEMENT: " + reader.getLocalName());
					return o;
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT) {
					String fqElementName = calculateFQElementName(reader);
					System.out.println(xpath + " START_ELEMENT: " + fqElementName);
					PropertyXMLNodeSpec childElementPropertyXMLNodeSpec = xmlNodeSpecObject.getChildElementPropertyXMLNodeSpecByName(fqElementName);
					if(childElementPropertyXMLNodeSpec == null)
						throw new MapException("Element not found in JSON schema", xpath + "/" + fqElementName);
					//check namespace is correct
					if(childElementPropertyXMLNodeSpec.getXmlNodeSpec().getXmlPrefix() != null && childElementPropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace() != null)
						if(!childElementPropertyXMLNodeSpec.getXmlNodeSpec().getXmlNamespace().equals(reader.getNamespaceURI()))
							throw new MapException("Invalid namespace", xpath + "/" + fqElementName);
					Object childElement = parseJSONValue(reader, childElementPropertyXMLNodeSpec.getXmlNodeSpec(), xpath + "/" + fqElementName);
					addKeyValueToJSONObject(childElementPropertyXMLNodeSpec.getPropertyName(), childElement, o, xpath + "/" + fqElementName, false);
				}
				else if(eventType == XMLStreamConstants.END_DOCUMENT) {
					throw new MapException("Too early end of XML document", xpath);
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
	
	static private String calculateFQElementName(XMLStreamReader reader) {
		String fqElementName = reader.getLocalName(); //fully qualified (prefix:name, or name if no prefix)
		//add prefix is defined
		String prefix = reader.getPrefix();
		if(prefix != null && prefix.length() > 0)
			fqElementName = prefix + ":" + fqElementName;
		return fqElementName;
	}

	static private void addKeyValueToJSONObject(String key, Object value, JSONObject o, String path, boolean isKeyArrayItem) throws MapException {
		System.out.println(" (1) o.put KEY=" + key);		
		if(isKeyArrayItem) {
			o.append(key, value);
			return;
		}
		//check no duplicate key first
		if(o.opt(key) != null)
			throw new MapException("Duplicated element", path);
		//then add
		o.put(key, value);
	}

	static private Object createObjectFromSimpleProperty(String characters, int nodeType, String xpath) throws MapException {
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
				throw new MapException("Failure to convert value to a boolean", xpath);
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
				throw new MapException("Failure to convert value to an integer", xpath);
			}
		}
		else if(nodeType == XMLNodeSpec.TYPE_NUMBER) {
			try {
				return new BigDecimal(charactersTrimmed);
			} catch (NumberFormatException e) {
				throw new MapException("Failure to convert value to a number", xpath);
			}
		}
		else
			throw new MapException("Unknown XMLNodeSpec type " + nodeType, xpath);
	}
}
