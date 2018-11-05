package community.layer7.customassertion.xmljsonTransform.transforms2;

import java.io.StringReader;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class JSONToXMLConverter {

	private static final String XMLNS_COLUMN = "xmlns:";
	private static final String XMLNS_ATTRIBUTE_NS_URI = "http://www.w3.org/2000/xmlns/";

	private static final Logger logger = Logger.getLogger(JSONToXMLConverter.class.getName());

	private String inputJSON;
	private XMLNodeSpecObject rootContainerXMLNodeSpec;
	private Document output;

	public JSONToXMLConverter(String inputJSON, XMLNodeSpecObject rootContainerXMLNodeSpec) throws MapException {
		if(inputJSON == null)
			throw new MapException("Input JSON is null");
		if(rootContainerXMLNodeSpec == null)
			throw new MapException("rootXMLNodeSpec is null");
		this.inputJSON = inputJSON;
		this.rootContainerXMLNodeSpec = rootContainerXMLNodeSpec;
	}
	
	public void process() throws MapException {
		//System.out.println("***********");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			//should never happen
			throw new MapException("Internal problem while instanciating DocumentBuilder");
		}
		output = docBuilder.newDocument();
		output.setXmlStandalone(true);
		SimplePath jpath = new SimplePath();
		Element rootElement = createObjectElement(output, rootContainerXMLNodeSpec, JSONSchemaForXML.DEFAULT_XML_ROOT_ELEMENT_NAME, jpath);
		output.appendChild(rootElement);
		JsonParser parser = Json.createParser(new StringReader(inputJSON));
		Event e = parser.next();
		if(e != Event.START_OBJECT)
			throw new MapException("Input JSON must of an object, starting with {", jpath.getFullJSONPath());
		parseObject(parser, rootElement, rootContainerXMLNodeSpec, "", jpath);
	}
	
	public Document getOutput() {
		return output;
	}
	
	private static void parseArray(JsonParser parser, Element objectElement, XMLNodeSpecArray xmlNodeSpecArray, String currentPropertyName, SimplePath jpath)
		throws MapException {
		Element targetElement = objectElement; //current objectElement is array is not wrapped or new element to be created bellow
		String arrayFQElementName = currentPropertyName;
		if(xmlNodeSpecArray.isXMLWrapped()) {
			Element newArrayElement = createObjectElement(objectElement, xmlNodeSpecArray, currentPropertyName, jpath);
			arrayFQElementName = newArrayElement.getNodeName();
			objectElement.appendChild(newArrayElement);
			targetElement = newArrayElement;
		}
		int arrayIndex = 0;
		while (parser.hasNext()) {
			Event e = parser.next();
			if(e == Event.START_ARRAY) {
				jpath.pushIndex(arrayIndex++);
				if(!(xmlNodeSpecArray.getItemsXMLNodeSpec() instanceof XMLNodeSpecArray))
					throw new MapException("Value should not be a JSON array", jpath.getFullJSONPath());
				parseArray(parser, targetElement, (XMLNodeSpecArray)xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, jpath);
				jpath.pop();
			}
			else if(e == Event.END_ARRAY)
				return;
			else if(e == Event.START_OBJECT) {
				jpath.pushIndex(arrayIndex++);
				if(!(xmlNodeSpecArray.getItemsXMLNodeSpec() instanceof XMLNodeSpecObject))
					throw new MapException("Value should not be a JSON object", jpath.getFullJSONPath());
				Element childObjectElement = createObjectElement(targetElement, xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, jpath); 
				targetElement.appendChild(childObjectElement);
				parseObject(parser, childObjectElement, (XMLNodeSpecObject)xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, jpath);
				jpath.pop();
			}
			else if(e == Event.VALUE_STRING || e == Event.VALUE_NUMBER ||
					e == Event.VALUE_NULL || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE) {
				jpath.pushIndex(arrayIndex++);
				if(!xmlNodeSpecArray.getItemsXMLNodeSpec().isPrimitiveType())
					throw new MapException("Value is expected to be a container (array or object)", jpath.getFullJSONPath());
				String memberValue = null;
				//TODO validate childXmlNodeSpec is right type
				if(e == Event.VALUE_STRING) {
					if(xmlNodeSpecArray.getItemsXMLNodeSpec().getNodeType() != XMLNodeSpec.TYPE_STRING)
						throw new MapException("Value should not be a string", jpath.getFullJSONPath());
					memberValue = parser.getString();
				}
				else if(e == Event.VALUE_NUMBER) {
					if(xmlNodeSpecArray.getItemsXMLNodeSpec().getNodeType() != XMLNodeSpec.TYPE_NUMBER &&
							xmlNodeSpecArray.getItemsXMLNodeSpec().getNodeType() != XMLNodeSpec.TYPE_INTEGER)
						throw new MapException("Value should not be a number or an integer", jpath.getFullJSONPath());
					memberValue = parser.getString();
				}
				else if(e == Event.VALUE_NULL) {
//					if(xmlNodeSpecArray.getItemsXMLNodeSpec().getNodeType() != XMLNodeSpec.TYPE_NULL)
//						throw new MapException("Value should not be a null", jpath.getFullJSONPath());
					memberValue = "null";
				}
				else if(e == Event.VALUE_TRUE) {
					if(xmlNodeSpecArray.getItemsXMLNodeSpec().getNodeType() != XMLNodeSpec.TYPE_BOOLEAN)
						throw new MapException("Value should not be a boolean", jpath.getFullJSONPath());
					memberValue = "true";
				}
				else if(e == Event.VALUE_FALSE) {
					if(xmlNodeSpecArray.getItemsXMLNodeSpec().getNodeType() != XMLNodeSpec.TYPE_BOOLEAN)
						throw new MapException("Value should not be a boolean", jpath.getFullJSONPath());
					memberValue = "false";
				}
				addKeyValueLeafElement(targetElement, (XMLNodeSpecPrimitiveType)xmlNodeSpecArray.getItemsXMLNodeSpec(), arrayFQElementName, memberValue, jpath);
				jpath.pop();
			}
		}
	}
	
	private static void parseObject(JsonParser parser, Element objectElement, XMLNodeSpecObject xmlNodeSpecObject, String currentPropertyName, SimplePath jpath)
			throws MapException {
		String memberKey = null;
		XMLNodeSpec childXmlNodeSpec = null;
		while (parser.hasNext()) {
			Event e = parser.next();
			if(e == Event.START_ARRAY) {
				if(!(childXmlNodeSpec instanceof XMLNodeSpecArray))
					throw new MapException("Value should not be a JSON array", jpath.getFullJSONPath());
				parseArray(parser, objectElement, (XMLNodeSpecArray)childXmlNodeSpec, memberKey, jpath);
//				Element newArrayElement = createIfWrappedArrayElementAndAddToArrayElement(entryModifiers, objectElement, currentPath + "." + memberKey + "[*]", memberKey);
//				parseArray(parser, newArrayElement, currentPath + "." + memberKey + "[*]", memberKey, entryModifiers);
				jpath.pop();
			}
			else if(e == Event.START_OBJECT) {
				if(!(childXmlNodeSpec instanceof XMLNodeSpecObject))
					throw new MapException("Value should not be a JSON object", jpath.getFullJSONPath());
				Element childObjectElement = createObjectElement(objectElement, childXmlNodeSpec, memberKey, jpath); 
				objectElement.appendChild(childObjectElement);
				parseObject(parser, childObjectElement, (XMLNodeSpecObject)childXmlNodeSpec, memberKey, jpath);
				jpath.pop();
			}
			else if(e == Event.END_OBJECT) {
				return;
			}
			else if(e == Event.KEY_NAME) {
				memberKey = parser.getString();
				jpath.pushElement(memberKey);
				childXmlNodeSpec = xmlNodeSpecObject.getJSONPropertyByName(memberKey);
				if(childXmlNodeSpec == null)
					throw new MapException("Property is not defined in schema", jpath.getFullJSONPath());
			}
			else if(e == Event.VALUE_STRING || e == Event.VALUE_TRUE || e == Event.VALUE_FALSE || e == Event.VALUE_NUMBER || e == Event.VALUE_NULL) {
				if(!childXmlNodeSpec.isPrimitiveType())
					throw new MapException("Value is expected to be a container (array or object)", jpath.getFullJSONPath());
				String memberValue = null;
				//TODO validate childXmlNodeSpec is right type
				if(e == Event.VALUE_STRING || e == Event.VALUE_NUMBER)
					memberValue = parser.getString();
				else if(e == Event.VALUE_NULL)
					memberValue = "";
				else if(e == Event.VALUE_TRUE)
					memberValue = "true";
				else if(e == Event.VALUE_FALSE)
					memberValue = "false";
				addKeyValueLeafElement(objectElement, (XMLNodeSpecPrimitiveType)childXmlNodeSpec, memberKey, memberValue, jpath);
				jpath.pop();
			}
		}		
	}

	private static void addKeyValueLeafElement(Element objectElement, XMLNodeSpecPrimitiveType propertyXMLNodeSpec, String objectKey, String objectValue, SimplePath jpath)
			throws MapException {
		//System.out.println("addKeyValueLeafElement objectKey=" + objectKey + ", objectElement.getNodeName()=" + objectElement.getNodeName());
		String targetFQELementName = propertyXMLNodeSpec.calculateTargetFullXMLName(objectKey);
		if(propertyXMLNodeSpec.isXMLAttribute()) {
			//System.out.println("is attribute targetFQELementName:" + targetFQELementName);
			objectElement.setAttribute(targetFQELementName, objectValue);
			if(propertyXMLNodeSpec.getXmlPrefix() != null && propertyXMLNodeSpec.getXmlNamespace() != null) {
				objectElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_COLUMN + propertyXMLNodeSpec.getXmlPrefix(), propertyXMLNodeSpec.getXmlNamespace());
				//System.out.println("adding " + XMLNS_COLUMN + propertyXMLNodeSpec.getXmlPrefix() + "=" + propertyXMLNodeSpec.getXmlNamespace());
			}
			return;
		}
		Element childElement = createObjectElement(objectElement, propertyXMLNodeSpec, objectKey, jpath);
		if(propertyXMLNodeSpec.getXmlPrefix() != null && propertyXMLNodeSpec.getXmlNamespace() != null)
			childElement.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_COLUMN + propertyXMLNodeSpec.getXmlPrefix(), propertyXMLNodeSpec.getXmlNamespace());
		objectElement.appendChild(childElement);
		childElement.appendChild(objectElement.getOwnerDocument().createTextNode(objectValue));
	}
	

	private static Element createObjectElement(Node node, XMLNodeSpec xmlNodeSpec, String currentPropertyName, SimplePath jpath) {
		String targetFQELementName = xmlNodeSpec.calculateTargetFullXMLName(currentPropertyName);
		Element element = null;
		if(node instanceof Document)
			element = ((Document)node).createElement(targetFQELementName);
		else
			element = node.getOwnerDocument().createElement(targetFQELementName);
		if(xmlNodeSpec.getXmlPrefix() != null && xmlNodeSpec.getXmlNamespace() != null)
			element.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_COLUMN + xmlNodeSpec.getXmlPrefix(), xmlNodeSpec.getXmlNamespace());
		return element;
	}
}
