package community.layer7.customassertion.xmljsonTransform.transforms2.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.json.JSONObject;
import org.junit.Test;

import community.layer7.customassertion.xmljsonTransform.transforms2.JSONSchemaForXML;
import community.layer7.customassertion.xmljsonTransform.transforms2.JSONSchemaLoadException;
import community.layer7.customassertion.xmljsonTransform.transforms2.MapException;

/**
 * This test of scripts test the scenarios without any XML schema attribute
 * Assumption : JSON schema is correct
 * @author Guy
 * TODO: cleanup of test cases description
 * TODO: rename json file + method name based on type of test case 
 */
public class JSONSchemaForXMLTest {
	
	private static final String JSON_TEST_FILES_FOLDER = "community/layer7/customassertion/xmljsonTransform/transforms2/test/";
	
	private String convertInputStreamToString(InputStream inputStream, Charset charset) throws IOException {
		 
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		return stringBuilder.toString();
	}

	@Test
	public void testJSONSchema_InvalidSchemas() {
		try {
			new JSONSchemaForXML(null);
			fail("null should throw exception");
		}
		catch(JSONSchemaLoadException e) {
			assertTrue(e.getMessage().indexOf("Failed to parse JSON") != -1);
		}
		try {
			new JSONSchemaForXML("asldkjlaksd");
			fail("garbage JSON should throw exception");
		}
		catch(JSONSchemaLoadException e) {
			assertTrue(e.getMessage().indexOf("Failed to parse JSON") != -1);
		}
	}
	
	@Test
	public void testJSONSchema_Simple() {
		String schema = null;
		JSONObject o = null;
		JSONSchemaForXML jsonSchemaForXML = null;		
		
		try {
			//check that json schema parse fails and shows exact location
			jsonSchemaForXML = new JSONSchemaForXML("{\n   \"a\":\"1\",\n   \"b\": 2,\n   \"c\";\"12\"\n }");
			fail("garbagge JSON should throw exception");
		} catch(JSONSchemaLoadException e) {
			assertTrue(e.toString().indexOf("Failed to parse JSON") != -1);
			assertTrue(e.toString().indexOf("character 7 line 4") != -1);
		}

		String resourceName = JSON_TEST_FILES_FOLDER + "schema_simple.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
		
		try {
			jsonSchemaForXML.mapXMLToJSON(null);
			fail("null should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Input XML is null") != -1);
		}
		try {
			//XML with just spaces
			jsonSchemaForXML.mapXMLToJSON("    ");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Line number = 1") != -1);
		}
		try {
			//XML with no elements
			jsonSchemaForXML.mapXMLToJSON("asdasd");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Line number = 1") != -1);
		}
		try {
			//XML is start root but no end root
			jsonSchemaForXML.mapXMLToJSON("<root>\n</cd>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error at Line number = 2") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			//XML is start root but no end root
			jsonSchemaForXML.mapXMLToJSON("<root>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error at Line number = 1") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			//use of XML reference
			jsonSchemaForXML.mapXMLToJSON("<?xml version=\"1.0\" encoding=\"utf-8\"?> <!DOCTYPE sample [ <!NOTATION vrml PUBLIC \"VRML 1.0\"> <!ENTITY dotto \"Dottoro\"> ]> <entityTest>hello</entityTest>");
			fail("XML Reference should not be accepted");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Unknown StAX event type") != -1);
		}
		try {
			//wrong XML root node
			jsonSchemaForXML.mapXMLToJSON("<AA></AA>");
			fail("Wrong root node should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML root node name is invalid") != -1);
			assertTrue(e.toString().indexOf("path: /AA") != -1);
		}
		try {
			//wired element
			o = jsonSchemaForXML.mapXMLToJSON("<root>abc</root>");
			fail("Show throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Characters at this location") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			//wired characters at the wrong location
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString>abc</root>");
			fail("Show throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Characters at this location") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<root><aString>ccc</aString2></root>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<root><aString>ccc<b></b></aString></root>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("New XML element not allowed for a simple property") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString/b") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString>asdad<aNumber>123.456</aNumber></root>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}

		try {
			//empty content
			o = jsonSchemaForXML.mapXMLToJSON("<root></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{}")));
			//empty root with <?xml...
			o = jsonSchemaForXML.mapXMLToJSON("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><root></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{}")));

			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString></aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n\t<aString>\n\t</aString>\n</root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString/></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			//simple string property with unicode (2 styles)
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>a\u0e01a&#x0e02;a</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"a\u0e01a\u0e02a\"}")));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n  <aString>aaa</aString>\n</root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			//simple string property "null"
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n\t<aString>null</aString>\n</root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"null\"}")));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n\t<aString>\n\t\taaa\n\t</aString>\n</root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));

			//Test XML comment (before element)
			o = jsonSchemaForXML.mapXMLToJSON("<root><!-- This is a comment --><aString>aaa</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			//Test XML comment (start of element)
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString><!-- This is a comment -->aaa</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			//Test XML comment (middle of element)
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>a<!-- This is a comment -->aa</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));

			//Test CDATA
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString><![CDATA[aaa]]></aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			//Test multi lines
			o = jsonSchemaForXML.mapXMLToJSON("<root>   \n   \t  \n    <aString>    \n    aaa      \n  \t  \n  </aString>  \n   \n   </root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"aaa\"}")));
			//Test escapes
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>1\n2\t3&amp;4&quot;5/6\\7</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"1\\n2\\t3&4\\\"5/6\\\\7\"}")));
			//Test escapes with spaces
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>1\n2\t3&amp;4 \n    \n &quot;5/6\\7</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aString\":\"1\\n2\\t3&4 \\n    \\n \\\"5/6\\\\7\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//simple aNumber property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber>123.456</aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456}")));
			//simple aNumber property with spaces and new line
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber>   \n  \t  123.456 \t  \n \t  </aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456}")));
			//simple aNumber property null
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber>   \n  \t  null \t  \n \t  </aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":null}")));
			//simple string + number property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><aNumber>123.456</aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\"}")));
			//simple string + number property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString>  \t  \n  \t   <aNumber>-123.456000</aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":-123.456,\"aString\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//empty aNumber property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber></aNumber></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to a number") != -1);
			assertTrue(e.toString().indexOf("path: /root/aNumber") != -1);
		}

		try {
			//duplicated aString property name
			jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><aString>bbb</aString></root>");
			fail("duplicate element should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString") != -1);
		}
		try {
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>123</anInteger></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":123}")));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>0</anInteger></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":0}")));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>-66</anInteger></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":-66}")));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>123456789012345678901234567890</anInteger></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":123456789012345678901234567890}")));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>-123456789012345678901234567890</anInteger></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anInteger\":-123456789012345678901234567890}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		try {
			//empty anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger></anInteger></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/anInteger") != -1);
		}
		try {
			//anInteger is a decimal property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>123.456</anInteger></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/anInteger") != -1);
		}
		try {
			//simple aBoolean property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean>TRue</aBoolean></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aBoolean\":true}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean> \t \n FAlse     \t \n   \t   </aBoolean></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aBoolean\":false}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		try {
			//empty aBoolean property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean></aBoolean></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to a boolean") != -1);
			assertTrue(e.toString().indexOf("path: /root/aBoolean") != -1);
		}
		try {
			//invalid aBoolean property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean>truefalse</aBoolean></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to a boolean") != -1);
			assertTrue(e.toString().indexOf("path: /root/aBoolean") != -1);
		}
		try {
			//invalid aNull property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNull>sdf</aNull></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Failure to convert text to a null") != -1);
			assertTrue(e.toString().indexOf("path: /root/aNull") != -1);
		}
		try {
			//unknown property aNumber2
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber2>123</aNumber2></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Element not found in JSON schema") != -1);
			assertTrue(e.toString().indexOf("path: /root/aNumber2") != -1);
		}
		try {
			//simple aNumber property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"}}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject2><str1>one</str1><str2>two</str2></anObject2><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"anObject2\":{\"str1\":\"one\",\"str2\":\"two\"}}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><anObject2><str1>one</str1><str2>two</str2></anObject2><anObject><str1>one</str1><str2>two</str2></anObject></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"anObject2\":{\"str1\":\"one\",\"str2\":\"two\"}}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject></anObject><aNumber>123.456</aNumber></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{}}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		try {
			//duplicated anObject object name
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber><anObject><str1>one</str1><str2>two</str2></anObject></root>");
			fail("duplicate element should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/anObject") != -1);
		}
	}

	@Test
	public void testJSONSchema_PropertyXML() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "schema_propertyXML.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><str1>aaa</str1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><str1></str1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><xmlstr2>aaa</xmlstr2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><xmlstr2></xmlstr2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref3=\"http://example.com/schema\"><pref3:str3>aaa</pref3:str3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str3\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref3=\"http://example.com/schema\"><pref3:str3></pref3:str3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str3\":\"\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\">aaa</pref4:str4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\"></pref4:str4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"\"}")));
			//namespace is declared above
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref4=\"http://example.com/schema\"><pref4:str4>aaa</pref4:str4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"aaa\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\">aaa</pref5:xmlstr5></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str5\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\"></pref5:xmlstr5></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str5\":\"\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//wrong namespace
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref4=\"http://example.com/schema2\"><pref4:str4>aaa</pref4:str4></root>");
			fail("wrong name should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Invalid namespace") != -1);
			assertTrue(e.toString().indexOf("path: /root/pref4:str4") != -1);
		}
	}
	
	@Test
	public void testJSONSchema_RootXML() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "schema_rootxml.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1>aaa</str1></soap:xmlroot>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1></str1></soap:xmlroot>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2>aaa</xmlstr2></soap:xmlroot>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"aaa\"}")));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2></xmlstr2></soap:xmlroot>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str2\":\"\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\" soap:encodingStyle=\"http://www.w3.org/2003/05/soap-encoding\"><xmlstr2>aaa</xmlstr2></soap:xmlroot>");
			fail("soap:encodingStyle is unknown, should raise exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Attribute not found") != -1);
		}
	}
	
	@Test
	public void testJSONSchema_Attributes() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "schema_attribute.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str0\":\"aaa\"}")));

			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root str1=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"bbb\",\"str0\":\"aaa\"}")));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root str1=\"\tbbb\t\"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str1\":\"bbb\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//duplicated property
			o = jsonSchemaForXML.mapXMLToJSON("<root str1=\"bbb\" str1=\"ccc\"><str0>aaa</str0></root>");
			fail("duplicated property should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("XML parsing") != -1);
		}
		
		try {
			//unknown string property
			o = jsonSchemaForXML.mapXMLToJSON("<root str999=\"bbb\"><str0>aaa</str0></root>");
			fail("unknown string property should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Attribute not found") != -1);
			assertTrue(e.toString().indexOf("path: /root/@str999") != -1);			
		}
		
		try {
			//property is not mistaken with element
			o = jsonSchemaForXML.mapXMLToJSON("<root><str0>aaa</str0><str1>bbb</str1></root>");
			fail("property is mistaken with element");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Element not found") != -1);
			assertTrue(e.toString().indexOf("path: /root/str1") != -1);			
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"bbb\" xmlint1=\"123\"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"int1\":123,\"str2\":\"bbb\",\"str0\":\"aaa\"}")));
			//empty string properties
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"\" xmlint1=\"00012345678901234567890\"><str0></str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"int1\":12345678901234567890,\"str2\":\"\",\"str0\":\"\"}")));
			//string property with spaces, int property with spaces
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"     \" xmlint1=\"     12345678901234567890     \"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"int1\":12345678901234567890,\"str2\":\"\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//invalid integer property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"bbb\" xmlint1=\"abc\"><str0>aaa</str0></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/@xmlint1") != -1);			
		}
		
		try {
			//empty integer property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"bbb\" xmlint1=\"\"><str0>aaa</str0></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/@xmlint1") != -1);			
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref3=\"http://example.com/schema\" pref3:str3=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str3\":\"bbb\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref4=\"http://example.com/schema\" pref4:str4=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str4\":\"bbb\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//missing namespace
			o = jsonSchemaForXML.mapXMLToJSON("<root pref4:str4=\"bbb\"><str0>aaa</str0></root>");
			fail("missing namespace should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("XML parsing error") != -1);
		}
		
		try {
			//wrong namespace
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref4=\"http://example.com/schema2\" pref4:str4=\"bbb\"><str0>aaa</str0></root>");
			fail("wrong namespace should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Invalid namespace") != -1);
			assertTrue(e.toString().indexOf("path: /root/@pref4") != -1);			
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref5=\"http://example.com/schema\" pref5:xmlstr5=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"str5\":\"bbb\",\"str0\":\"aaa\"}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
	}

	@Test
	public void testJSONSchema_Arrays() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "schema_arrays.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}
		
		//relationship is non wrapped array, no name change
		//phones1 is non wrapped array, name change none
		//phones2 is non wrapped array, name change items
		//phones3 is wrapped array,     name change wrapper and items
		//phones4 is wrapped array,     name change wrapper
		//
		//array_n are 3 layers of array
		//arrayOfArrays :	wrapped(renamed)		wrapped(renamed)		int(renamed)  
		//arrayOfArrays2 :	not wrapped				wrapped(renamed)		int(renamed)  

		//arrayOfArrays3 :	wrapped(renamed)		wrapped(renamed)		string(renamed)   
		//arrayOfArrays4 :	wrapped(renamed)		wrapped(renamed)		int(not renamed)  
		//arrayOfArrays5 :	wrapped(renamed)		wrapped(not renamed)	int(renamed)  
		//arrayOfArrays6 :	wrapped(renamed)		wrapped(not renamed)	int(not renamed)  
		//arrayOfArrays7 :	wrapped(not renamed)	wrapped(renamed)		int(renamed)  
		//arrayOfArrays8 :	wrapped(not renamed)	wrapped(renamed)		int(not renamed)  
		//arrayOfArrays9 :	wrapped(not renamed)	wrapped(not renamed)	int(renamed)  
		//arrayOfArrays10 :	wrapped(not renamed)	wrapped(not renamed)	int(not renamed)  

		try {
			//empty structure
			o = jsonSchemaForXML.mapXMLToJSON("<root></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array of objects
			o = jsonSchemaForXML.mapXMLToJSON("<root><relationships><name>name 1</name></relationships><relationships><name>name 2</name><age>17</age></relationships></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[{\"name\":\"name 1\"},{\"name\":\"name 2\",\"age\":17}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
		
		try {
			//array of objects, wrong 2nd object
			o = jsonSchemaForXML.mapXMLToJSON("<root><relationships><name>name 1</name></relationships><relationships><name>name 2</name><age>aaa</age></relationships></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/relationships[2]/age") != -1);			
		}
		
		try {
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><phones1></phones1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"\"]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><phones1>p1</phones1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"p1\"]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><phones1>p1</phones1><phones1>p2</phones1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"p1\",\"p2\"]}")));

			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2></PHONE_2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[\"\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[\"p1\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2><aString>abc</aString></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"aString\":\"abc\",\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><aString>abc</aString><PHONE_2>p2</PHONE_2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"aString\":\"abc\",\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple string with duplicated property
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><aString>abc</aString><PHONE_2>p2</PHONE_2><aString>abc</aString></root>");
			fail("duplicated property - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/aString") != -1);			
		}
		
		try {
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3></PHONES_3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3></PHONE_3></PHONES_3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"\"]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>123</PHONE_3></PHONES_3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"123\"]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>123</PHONE_3><PHONE_3>456</PHONE_3></PHONES_3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"123\",\"456\"]}")));
			
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_4></PHONES_4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones4\":[]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_4><PHONES_4></PHONES_4></PHONES_4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones4\":[\"\"]}")));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_4><PHONES_4>123</PHONES_4></PHONES_4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones4\":[\"123\"]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//wrapped array, duplicated wrapped entry
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>1</PHONE_3><PHONE_3>2</PHONE_3></PHONES_3><PHONES_3><PHONE_3>3</PHONE_3><PHONE_3>4</PHONE_3></PHONES_3></root>");
			fail("wrapped array, duplicated wrapped entry - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_3") != -1);			
		}
		
		try {
			//wrapped array, duplicated wrapped entry
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_4><PHONES_4>1</PHONES_4><PHONES_4>2</PHONES_4></PHONES_4><PHONES_4><PHONES_4>4</PHONES_4><PHONES_4>4</PHONES_4></PHONES_4></root>");
			fail("wrapped array, duplicated wrapped entry - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Duplicated element") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_4") != -1);			
		}
		
		try {
			//wrapped array, wrapped entry is wrong name
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>1</PHONE_3><PHONE_3>2</PHONE_3><PHONE_33>3</PHONE_33></PHONES_3></root>");
			fail("wrapped entry is wrong name - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Wrapped element name is not matching specifications") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_3/PHONE_33") != -1);			
		}
		
		try {
			//wrapped array, wrapped entry is wrong name
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_4><PHONES_4>1</PHONES_4><PHONES_4>2</PHONES_4><PHONES_44>4</PHONES_44></PHONES_4></root>");
			fail("wrapped entry is wrong name - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Wrapped element name is not matching specifications") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_4/PHONES_44") != -1);			
		}
		
		try {
			//wrapped array, string at wrong place
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>1</PHONE_3>3<PHONE_3>2</PHONE_3></PHONES_3></root>");
			fail("wrapped array, string at wrong place - should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Characters at this location are not allowed") != -1);
			assertTrue(e.toString().indexOf("path: /root/PHONES_3") != -1);			
		}
		
		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><INTS_1></INTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"ints1\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><INTS_1><INT_1>123</INT_1></INTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"ints1\":[123],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><INTS_1><INT_1>123</INT_1><INT_1>456</INT_1></INTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"ints1\":[123,456],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));

			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1></OBJECTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1></OBJECT_1></OBJECTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[{}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"18\"></OBJECT_1></OBJECTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[{\"name\":\"Joe\"},{\"age\":18}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1></OBJECT_1><OBJECT_1></OBJECT_1></OBJECTS_1></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"objects1\":[{},{}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
		}
		catch(Exception e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"aaa\"></age></OBJECT_1></OBJECTS_1></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/OBJECTS_1/OBJECT_1[2]/@age") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS></ARRAYS></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY></ARRAY></ARRAYS></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[]]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY></ARRAYS></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3]]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY><INT>4</INT></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3],[4],[5,6,7,8]]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3],[],[5,6,7,8]]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>a</INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[1]/INT[3]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT></INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[1]/INT[3]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY><INT>1</INT><INT></INT><INT>3</INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert text to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[2]/INT[2]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2></ARRAY2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[]],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3]],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2><ARRAY2><INT>4</INT></ARRAY2><ARRAY2><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3],[4],[5,6,7,8]],\"phones1\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2><ARRAY2></ARRAY2><ARRAY2><INT>null</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY2></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3],[],[null,6,7,8]],\"phones1\":[]}")));

			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3></ARRAYS3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3><ARRAY3></ARRAY3></ARRAYS3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[]]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3><ARRAY3><STRING></STRING></ARRAY3></ARRAYS3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[\"\"]]}")));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3><ARRAY3><STRING>1</STRING><STRING></STRING><STRING>3</STRING></ARRAY3></ARRAYS3></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[\"1\",\"\",\"3\"]]}")));
			
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS4><ARRAY><ARRAY>1</ARRAY><ARRAY>2</ARRAY><ARRAY>3</ARRAY></ARRAY><ARRAY></ARRAY><ARRAY><ARRAY>5</ARRAY><ARRAY>6</ARRAY><ARRAY>7</ARRAY><ARRAY>8</ARRAY></ARRAY></ARRAYS4></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays4\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS5><ARRAYS5><INT>1</INT><INT>2</INT><INT>3</INT></ARRAYS5><ARRAYS5></ARRAYS5><ARRAYS5><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAYS5></ARRAYS5></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays5\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS6><ARRAYS6><ARRAYS6>1</ARRAYS6><ARRAYS6>2</ARRAYS6><ARRAYS6>3</ARRAYS6></ARRAYS6><ARRAYS6></ARRAYS6><ARRAYS6><ARRAYS6>5</ARRAYS6><ARRAYS6>6</ARRAYS6><ARRAYS6>7</ARRAYS6><ARRAYS6>8</ARRAYS6></ARRAYS6></ARRAYS6></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays6\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays7><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></arrayOfArrays7></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays7\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays8><ARRAY><ARRAY>1</ARRAY><ARRAY>2</ARRAY><ARRAY>3</ARRAY></ARRAY><ARRAY></ARRAY><ARRAY><ARRAY>5</ARRAY><ARRAY>6</ARRAY><ARRAY>7</ARRAY><ARRAY>8</ARRAY></ARRAY></arrayOfArrays8></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays8\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays9><arrayOfArrays9><INT>1</INT><INT>2</INT><INT>3</INT></arrayOfArrays9><arrayOfArrays9></arrayOfArrays9><arrayOfArrays9><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></arrayOfArrays9></arrayOfArrays9></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays9\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays10><arrayOfArrays10><arrayOfArrays10>1</arrayOfArrays10><arrayOfArrays10>2</arrayOfArrays10><arrayOfArrays10>3</arrayOfArrays10></arrayOfArrays10><arrayOfArrays10></arrayOfArrays10><arrayOfArrays10><arrayOfArrays10>5</arrayOfArrays10><arrayOfArrays10>6</arrayOfArrays10><arrayOfArrays10>7</arrayOfArrays10><arrayOfArrays10>8</arrayOfArrays10></arrayOfArrays10></arrayOfArrays10></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays10\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>1</ns1:arrayOfArrays11><ns1:arrayOfArrays11>2</ns1:arrayOfArrays11><ns1:arrayOfArrays11>3</ns1:arrayOfArrays11></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>5</ns1:arrayOfArrays11><ns1:arrayOfArrays11>6</ns1:arrayOfArrays11><ns1:arrayOfArrays11>7</ns1:arrayOfArrays11><ns1:arrayOfArrays11>8</ns1:arrayOfArrays11></ns1:arrayOfArrays11></arrayOfArrays11></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays11\":[[1,2,3],[],[5,6,7,8]]}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays12 xmlns:ns2=\"http://test.com\"><arrayOfArrays12><ns2:arrayOfArrays12>1</ns2:arrayOfArrays12><ns2:arrayOfArrays12>2</ns2:arrayOfArrays12><ns2:arrayOfArrays12>3</ns2:arrayOfArrays12></arrayOfArrays12><arrayOfArrays12></arrayOfArrays12><arrayOfArrays12><ns2:arrayOfArrays12>5</ns2:arrayOfArrays12><ns2:arrayOfArrays12>6</ns2:arrayOfArrays12><ns2:arrayOfArrays12>7</ns2:arrayOfArrays12><ns2:arrayOfArrays12>8</ns2:arrayOfArrays12></arrayOfArrays12></arrayOfArrays12></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays12\":[[1,2,3],[],[5,6,7,8]]}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}

		try {
			//invalid namespace
			o = jsonSchemaForXML.mapXMLToJSON("<root><arrayOfArrays11 xmlns:ns1=\"http://test2.com\"><ns1:arrayOfArrays11><ns1:arrayOfArrays11>1</ns1:arrayOfArrays11><ns1:arrayOfArrays11>2</ns1:arrayOfArrays11><ns1:arrayOfArrays11>3</ns1:arrayOfArrays11></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"></ns1:arrayOfArrays11><ns1:arrayOfArrays11 xmlns:ns1=\"http://test.com\"><ns1:arrayOfArrays11>5</ns1:arrayOfArrays11><ns1:arrayOfArrays11>6</ns1:arrayOfArrays11><ns1:arrayOfArrays11>7</ns1:arrayOfArrays11><ns1:arrayOfArrays11>8</ns1:arrayOfArrays11></ns1:arrayOfArrays11></arrayOfArrays11></root>");
			fail("invalid namespace should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Invalid namespace") != -1);
			assertTrue(e.toString().indexOf("path: /root/arrayOfArrays11/ns1:arrayOfArrays11") != -1);			
		}	

	}
	
	@Test
	public void testJSONSchemaForXML_Definitions() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "schema_definitions.json";
		try {
			schema = convertInputStreamToString(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}

		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			e.printStackTrace();
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			o = jsonSchemaForXML.mapXMLToJSON("<root><shipping_address id=\"1\"><city>Washington</city></shipping_address><billing_address id=\"2\"></billing_address></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"shipping_address\":{\"id\":1,\"city\":\"Washington\"},\"billing_address\":{\"id\":2}}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><billing_address id=\"2\"><PHONES><PHONE>1</PHONE><PHONE>2</PHONE></PHONES></billing_address></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"billing_address\":{\"id\":2,\"phones\":[1,2]}}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><shipping_address id=\"1\"><city>Washington</city><ZIPCODE>1234</ZIPCODE></shipping_address><billing_address id=\"2\"></billing_address></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"shipping_address\":{\"id\":1,\"city\":\"Washington\",\"zipcode\":1234},\"billing_address\":{\"id\":2}}")));
			o = jsonSchemaForXML.mapXMLToJSON("<root><shipping_address id=\"1\"><destination><name>John Dust</name><children><name>John Jr 1</name></children><children><name>John Jr 2</name></children></destination><city>Washington</city><ZIPCODE>1234</ZIPCODE></shipping_address><billing_address id=\"2\"></billing_address></root>");
			assertTrue(JSONComparator.areObjectsEqual(o, new JSONObject("{\"shipping_address\":{\"id\":1,\"destination\":{\"name\":\"John Dust\", \"children\":[ {\"name\":\"John Jr 1\",\"children\":[]},{\"name\":\"John Jr 2\",\"children\":[]}]},\"city\":\"Washington\",\"zipcode\":1234},\"billing_address\":{\"id\":2}}")));
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("Should not throw exception: " + e);
		}
	}
}
