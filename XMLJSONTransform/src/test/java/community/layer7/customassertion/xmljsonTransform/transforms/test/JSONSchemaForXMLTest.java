package community.layer7.customassertion.xmljsonTransform.transforms.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.json.JSONObject;
import org.junit.Test;

import community.layer7.customassertion.xmljsonTransform.transforms.JSONSchemaForXML;
import community.layer7.customassertion.xmljsonTransform.transforms.JSONSchemaLoadException;
import community.layer7.customassertion.xmljsonTransform.transforms.MapException;

/**
 * This test of scripts test the scenarios without any XML schema attribute
 * Assumption : JSON schema is correct
 * @author Guy
 * TODO: cleanup of test cases description
 * TODO: rename json file + method name based on type of test case 
 */
public class JSONSchemaForXMLTest {
	
	private static final String JSON_TEST_FILES_FOLDER = "community/layer7/customassertion/xmljsonTransform/transforms/test/";
	
	public String convert(InputStream inputStream, Charset charset) throws IOException {
		 
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
	public void testJSONSchemaForXML0() {
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
	public void testJSONSchemaForXML1() {
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

		String resourceName = JSON_TEST_FILES_FOLDER + "simple1.json";
		try {
			schema = convert(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
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
			jsonSchemaForXML.mapXMLToJSON("    ");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Line number = 1") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("asdasd");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Line number = 1") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<root>\n</cd>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Line number = 2") != -1);
			assertTrue(e.toString().indexOf("path: /root") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<?xml version=\"1.0\" encoding=\"utf-8\"?> <!DOCTYPE sample [ <!NOTATION vrml PUBLIC \"VRML 1.0\"> <!ENTITY dotto \"Dottoro\"> ]> <entityTest>hello</entityTest>");
			fail("XML Reference should not be accepted");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Unknown StAX event type") != -1);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<AA></AA>");
			fail("Wrong root node should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /AA") != -1);
		}
		try {
			//empty root
			o = jsonSchemaForXML.mapXMLToJSON("<root></root>");
			assertTrue(o.toString(0).equals("{}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
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
			//empty root
			o = jsonSchemaForXML.mapXMLToJSON("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><root></root>");
			assertTrue(o.toString(0).equals("{}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"aaa\"}"));
			//simple string property with unicode (2 styles)
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>a\u0e01a&#x0e02;a</aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"a\u0e01a\u0e02a\"}"));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n  <aString>aaa</aString>\n</root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"aaa\"}"));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n\t<aString>null</aString>\n</root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"null\"}"));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n\t<aString>\n\t\taaa\n\t</aString>\n</root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString></aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root>\n\t<aString>\n\t</aString>\n</root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString/></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		try {
			//Test XML comment
			o = jsonSchemaForXML.mapXMLToJSON("<root><!-- This is a comment --><aString>aaa</aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"aaa\"}"));
			//Test CDATA
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString><![CDATA[aaa]]></aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"aaa\"}"));
			//Test multi lines
			o = jsonSchemaForXML.mapXMLToJSON("<root>   \n   \t  \n    <aString>    \n    aaa      \n  \t  \n  </aString>  \n   \n   </root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"aaa\"}"));
			//Test escapes
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>1\n2\t3&amp;4&quot;5/6\\7</aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"1\\n2\\t3&4\\\"5/6\\\\7\"}"));
			//Test escapes with spaces
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>1\n2\t3&amp;4 \n    \n &quot;5/6\\7</aString></root>");
			assertTrue(o.toString(0).equals("{\"aString\":\"1\\n2\\t3&4 \\n    \\n \\\"5/6\\\\7\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//simple aNumber property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber>123.456</aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":123.456}"));
			//simple aNumber property with spaces and new line
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber>   \n  \t  123.456 \t  \n \t  </aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":123.456}"));
			//simple aNumber property null
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber>   \n  \t  null \t  \n \t  </aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":null}"));
			//simple string + number property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><aNumber>123.456</aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":123.456,\"aString\":\"aaa\"}"));
			//simple string + number property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString>  \t  \n  \t   <aNumber>-123.456000</aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":-123.456,\"aString\":\"aaa\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		try {
			jsonSchemaForXML.mapXMLToJSON("<root><aString>ccc<b></b></aString></root>");
			fail("garbagge XML should throw exception");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("Element not found in JSON schema") != -1);
			assertTrue(e.toString().indexOf("path: /root/b") != -1);
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
			//empty aNumber property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber></aNumber></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /root/aNumber") != -1);
		}
		try {
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>123</anInteger></root>");
			assertTrue(o.toString(0).equals("{\"anInteger\":123}"));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>0</anInteger></root>");
			assertTrue(o.toString(0).equals("{\"anInteger\":0}"));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>-66</anInteger></root>");
			assertTrue(o.toString(0).equals("{\"anInteger\":-66}"));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>123456789012345678901234567890</anInteger></root>");
			assertTrue(o.toString(0).equals("{\"anInteger\":123456789012345678901234567890}"));
			//simple anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>-123456789012345678901234567890</anInteger></root>");
			assertTrue(o.toString(0).equals("{\"anInteger\":-123456789012345678901234567890}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		try {
			//empty anInteger property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger></anInteger></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /root/anInteger") != -1);
		}
		try {
			//anInteger is a decimal property
			o = jsonSchemaForXML.mapXMLToJSON("<root><anInteger>123.456</anInteger></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /root/anInteger") != -1);
		}
		try {
			//simple aBoolean property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean>TRue</aBoolean></root>");
			assertTrue(o.toString(0).equals("{\"aBoolean\":true}"));
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean> \t \n FAlse     \t \n   \t   </aBoolean></root>");
			assertTrue(o.toString(0).equals("{\"aBoolean\":false}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		try {
			//empty aBoolean property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean></aBoolean></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /root/aBoolean") != -1);
		}
		try {
			//invalid aBoolean property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aBoolean>truefalse</aBoolean></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /root/aBoolean") != -1);
		}
		try {
			//unknown property aNumber2
			o = jsonSchemaForXML.mapXMLToJSON("<root><aNumber2>123</aNumber2></root>");
			fail("Should fail");
		}
		catch(MapException e) {
			//ok
			assertTrue(e.toString().indexOf("path: /root/aNumber2") != -1);
		}
		try {
			//simple aNumber property
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"}}"));
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject2><str1>one</str1><str2>two</str2></anObject2><anObject><str1>one</str1><str2>two</str2></anObject><aNumber>123.456</aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"anObject2\":{\"str1\":\"one\",\"str2\":\"two\"}}"));
			o = jsonSchemaForXML.mapXMLToJSON("<root><anObject2><str1>one</str1><str2>two</str2></anObject2><anObject><str1>one</str1><str2>two</str2></anObject></root>");
			assertTrue(o.toString(0).equals("{\"anObject\":{\"str1\":\"one\",\"str2\":\"two\"},\"anObject2\":{\"str1\":\"one\",\"str2\":\"two\"}}"));
			o = jsonSchemaForXML.mapXMLToJSON("<root><aString>aaa</aString><anObject></anObject><aNumber>123.456</aNumber></root>");
			assertTrue(o.toString(0).equals("{\"aNumber\":123.456,\"aString\":\"aaa\",\"anObject\":{}}"));
		}
		catch(MapException e) {
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
	public void testJSONSchemaForXML2() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "simple2.json";
		try {
			schema = convert(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><str1>aaa</str1></root>");
			assertTrue(o.toString(0).equals("{\"str1\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><str1></str1></root>");
			assertTrue(o.toString(0).equals("{\"str1\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><xmlstr2>aaa</xmlstr2></root>");
			assertTrue(o.toString(0).equals("{\"str2\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><xmlstr2></xmlstr2></root>");
			assertTrue(o.toString(0).equals("{\"str2\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref3=\"http://example.com/schema\"><pref3:str3>aaa</pref3:str3></root>");
			assertTrue(o.toString(0).equals("{\"str3\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref3=\"http://example.com/schema\"><pref3:str3></pref3:str3></root>");
			assertTrue(o.toString(0).equals("{\"str3\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\">aaa</pref4:str4></root>");
			assertTrue(o.toString(0).equals("{\"str4\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref4:str4 xmlns:pref4=\"http://example.com/schema\"></pref4:str4></root>");
			assertTrue(o.toString(0).equals("{\"str4\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\">aaa</pref5:xmlstr5></root>");
			assertTrue(o.toString(0).equals("{\"str5\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><pref5:xmlstr5 xmlns:pref5=\"http://example.com/schema\"></pref5:xmlstr5></root>");
			assertTrue(o.toString(0).equals("{\"str5\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
	}
	
	@Test
	public void testJSONSchemaForXML3() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "simple3.json";
		try {
			schema = convert(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1>aaa</str1></soap:xmlroot>");
			assertTrue(o.toString(0).equals("{\"str1\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><str1></str1></soap:xmlroot>");
			assertTrue(o.toString(0).equals("{\"str1\":\"\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2>aaa</xmlstr2></soap:xmlroot>");
			assertTrue(o.toString(0).equals("{\"str2\":\"aaa\"}"));
			//empty string property
			o = jsonSchemaForXML.mapXMLToJSON("<soap:xmlroot xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><xmlstr2></xmlstr2></soap:xmlroot>");
			assertTrue(o.toString(0).equals("{\"str2\":\"\"}"));
		}
		catch(MapException e) {
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
	public void testJSONSchemaForXML4() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "simple4.json";
		try {
			schema = convert(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			fail("Failed to parse schema, e=" + e);
			return;
		}
			
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"str0\":\"aaa\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root str1=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"str1\":\"bbb\",\"str0\":\"aaa\"}"));
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root str1=\"\tbbb\t\"><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"str1\":\"bbb\",\"str0\":\"aaa\"}"));
		}
		catch(MapException e) {
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
			assertTrue(o.toString(0).equals("{\"int1\":123,\"str2\":\"bbb\",\"str0\":\"aaa\"}"));
			//empty string properties
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"\" xmlint1=\"00012345678901234567890\"><str0></str0></root>");
			assertTrue(o.toString(0).equals("{\"int1\":12345678901234567890,\"str2\":\"\",\"str0\":\"\"}"));
			//string property with spaces, int property with spaces
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"     \" xmlint1=\"     12345678901234567890     \"><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"int1\":12345678901234567890,\"str2\":\"\",\"str0\":\"aaa\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//invalid integer property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"bbb\" xmlint1=\"abc\"><str0>aaa</str0></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/@xmlint1") != -1);			
		}
		
		try {
			//empty integer property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlstr2=\"bbb\" xmlint1=\"\"><str0>aaa</str0></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/@xmlint1") != -1);			
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref3=\"http://example.com/schema\" pref3:str3=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"str3\":\"bbb\",\"str0\":\"aaa\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref4=\"http://example.com/schema\" pref4:str4=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"str4\":\"bbb\",\"str0\":\"aaa\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//simple string property
			o = jsonSchemaForXML.mapXMLToJSON("<root xmlns:pref5=\"http://example.com/schema\" pref5:xmlstr5=\"bbb\"><str0>aaa</str0></root>");
			assertTrue(o.toString(0).equals("{\"str5\":\"bbb\",\"str0\":\"aaa\"}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
	}

	@Test
	public void testJSONSchemaForXML_Arrays() {
		String schema = null;
		JSONObject o = null;
		String resourceName = JSON_TEST_FILES_FOLDER + "arrays2.json";
		try {
			schema = convert(this.getClass().getClassLoader().getResourceAsStream(resourceName), Charset.defaultCharset());
			assertTrue(schema.indexOf("$schema") != -1); //load string ok
		} catch (IOException e) {
			fail("Failed to load " + resourceName + ", e=" + e);
			return;
		}
		JSONSchemaForXML jsonSchemaForXML = null;
		try {
			jsonSchemaForXML = new JSONSchemaForXML(schema);
		} catch(JSONSchemaLoadException e) {
			fail("Failed to parse schema, e=" + e);
			return;
		}

		try {
			//empty structure
			o = jsonSchemaForXML.mapXMLToJSON("<root></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array of objects
			o = jsonSchemaForXML.mapXMLToJSON("<root><relationships><name>name 1</name></relationships><relationships><name>name 2</name><age>17</age></relationships></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[{\"name\":\"name 1\"},{\"name\":\"name 2\",\"age\":17}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//array of objects, wrong 2nd object
			o = jsonSchemaForXML.mapXMLToJSON("<root><relationships><name>name 1</name></relationships><relationships><name>name 2</name><age>aaa</age></relationships></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/relationships[2]/age") != -1);			
		}
		
		try {
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><phones1></phones1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"\"]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><phones1>p1</phones1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"p1\"]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><phones1>p1</phones1><phones1>p2</phones1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[\"p1\",\"p2\"]}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}
		
		try {
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2></PHONE_2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[\"\"],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[\"p1\"],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><PHONE_2>p2</PHONE_2><aString>abc</aString></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"aString\":\"abc\",\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONE_2>p1</PHONE_2><aString>abc</aString><PHONE_2>p2</PHONE_2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"aString\":\"abc\",\"phones2\":[\"p1\",\"p2\"],\"arrayOfArrays2\":[],\"phones1\":[]}"));
		}
		catch(MapException e) {
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
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3></PHONE_3></PHONES_3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"\"]}"));
			//array item is a simple string
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>123</PHONE_3></PHONES_3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"123\"]}"));
			o = jsonSchemaForXML.mapXMLToJSON("<root><PHONES_3><PHONE_3>123</PHONE_3><PHONE_3>456</PHONE_3></PHONES_3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"phones3\":[\"123\",\"456\"]}"));
		}
		catch(MapException e) {
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
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"ints1\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><INTS_1><INT_1>123</INT_1></INTS_1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"ints1\":[123],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><INTS_1><INT_1>123</INT_1><INT_1>456</INT_1></INTS_1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"ints1\":[123,456],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1></OBJECTS_1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"objects1\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1></OBJECT_1></OBJECTS_1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"objects1\":[{}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"18\"></OBJECT_1></OBJECTS_1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"objects1\":[{\"name\":\"Joe\"},{\"age\":18}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1></OBJECT_1><OBJECT_1></OBJECT_1></OBJECTS_1></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"objects1\":[{},{}],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
		}
		catch(MapException e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><OBJECTS_1><OBJECT_1><name>Joe</name></OBJECT_1><OBJECT_1 age=\"aaa\"></age></OBJECT_1></OBJECTS_1></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/OBJECTS_1/OBJECT_1[2]/@age") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS></ARRAYS></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY></ARRAY></ARRAYS></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[]]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY></ARRAYS></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3]]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY><INT>4</INT></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3],[4],[5,6,7,8]]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY></ARRAY><ARRAY><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY></ARRAYS></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays\":[[1,2,3],[],[5,6,7,8]]}"));
		}
		catch(Exception e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>a</INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[1]/INT[3]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT></INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[1]/INT[3]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS><ARRAY><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY><ARRAY><INT>1</INT><INT></INT><INT>3</INT></ARRAY></ARRAYS></root>");
			fail("invalid integer property should throw exception");
		}
		catch(MapException e) {
			assertTrue(e.toString().indexOf("Failure to convert value to an integer") != -1);
			assertTrue(e.toString().indexOf("path: /root/ARRAYS/ARRAY[2]/INT[2]") != -1);			
		}	

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2></ARRAY2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[]],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3]],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2><ARRAY2><INT>4</INT></ARRAY2><ARRAY2><INT>5</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3],[4],[5,6,7,8]],\"phones1\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAY2><INT>1</INT><INT>2</INT><INT>3</INT></ARRAY2><ARRAY2></ARRAY2><ARRAY2><INT>null</INT><INT>6</INT><INT>7</INT><INT>8</INT></ARRAY2></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[[1,2,3],[],[null,6,7,8]],\"phones1\":[]}"));
		}
		catch(Exception e) {
			fail("Should not throw exception: " + e);
		}

		try {
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3></ARRAYS3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3><ARRAY3></ARRAY3></ARRAYS3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[]]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3><ARRAY3><STRING></STRING></ARRAY3></ARRAYS3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[\"\"]]}"));
			//array item is a simple integers
			o = jsonSchemaForXML.mapXMLToJSON("<root><ARRAYS3><ARRAY3><STRING>1</STRING><STRING></STRING><STRING>3</STRING></ARRAY3></ARRAYS3></root>");
			assertTrue(o.toString(0).equals("{\"relationships\":[],\"phones2\":[],\"arrayOfArrays2\":[],\"phones1\":[],\"arrayOfArrays3\":[[\"1\",\"\",\"3\"]]}"));
		}
		catch(Exception e) {
			fail("Should not throw exception: " + e);
		}

	}
	
//	@Test
//	public void testJSONSchemaForXML_Definitions() {
//		String schema = null;
//		JSONObject o = null;
//		try {
//			schema = convert(this.getClass().getClassLoader().getResourceAsStream(JSON_TEST_FILES_FOLDER + "definitions.json"), Charset.defaultCharset());
//			assertTrue(schema.indexOf("$schema") != -1); //load string ok
//		} catch (IOException e) {
//			fail("Failed to load pkg2/definitions.json, e=" + e);
//			return;
//		}
//		JSONSchemaForXML jsonSchemaForXML = null;
//		try {
//			jsonSchemaForXML = new JSONSchemaForXML(schema);
//		} catch(JSONSchemaLoadException e) {
//			e.printStackTrace();
//			fail("Failed to parse schema, e=" + e);
//			return;
//		}
//
//		try {
//			//empty structure
//			o = jsonSchemaForXML.mapXMLToJSON("<root><shipping_address><city>Washington</city></shipping_address></root>");
//			assertTrue(o.toString(0).equals("{\"shipping_address\":{\"city\":\"Washington\"}}"));
//		}
//		catch(MapException e) {
//			fail("Should not throw exception: " + e);
//		}
//	}
}
