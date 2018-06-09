package community.layer7.customassertion.stringSimpleTransform.tranforms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Guy Deffaux
 * June 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class StringTransformerTest {

	private static final String TEST_STRING = " aB\t&\\\u65e51\u0e012";

    @Test
	public void TestEncodings() {
		String input = null;
		try {
			assertEquals(StringTransformer.transformString(StringTransformer.TRIM, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "45"), "E");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "4a"), "J");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "4A"), "J");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16_HEX, "00450e010046"), "E\u0e01F");

			input = StringTransformer.TOOLTIP_TEST_STRING;
			assertEquals(StringTransformer.transformString(StringTransformer.TRIM, input), "aB\t&\\\u65e51\u0e012");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_LOWER_CASE, input), " ab\t&\\\u65e51\u0e012");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UPPER_CASE, input), " AB\t&\\\u65e51\u0e012");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, input), "20614209265CE697A531E0B88132");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16_HEX, input), "FEFF00200061004200090026005C65E500310E010032");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16LE_HEX, input), "200061004200090026005C00E5653100010E3200");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16BE_HEX, input), "00200061004200090026005C65E500310E010032");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, input), " aB\\t&\\\\\\u65E51\\u0E012");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, input), " aB	&amp;\\\u65e51\u0e012");
			
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_XML11_STRING, input)), input);

			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16_HEX, StringTransformer.transformString(StringTransformer.TO_UTF16_HEX, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16BE_HEX, StringTransformer.transformString(StringTransformer.TO_UTF16BE_HEX, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16LE_HEX, StringTransformer.transformString(StringTransformer.TO_UTF16LE_HEX, input)), input);

			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, "A\u001e&\u001f'\u0020B\ufffeC\u0000D\u000CE"), "A&amp;&apos; BCDE");

			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "A\\u0032B\\u0e01C\\tDE\\\\F\\\"G"), "A2B\u0e01C\tDE\\F\"G");

			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, "A\\B'C\"D/E	F\u0003G\u0019H\u0000I\1111J"), "A\\\\B'C\\\"D\\/E\\tF\\u0003G\\u0019H\\u0000II1J");;
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, "A&#x0e01;&amp;&apos; B&#33;C"), "A\u0e01&' B!C");;

			input = StringTransformer.TOOLTIP_TEST_STRING + " & &amp; \\ \\\\ \t ' \\' \" \\\" &#33; &amp;#33; &#x0e01; &amp;#x0e01; \n \\n / \\/ "; // \f and \r \b are problems
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, input)), input);
			input += " \f \\f \r \\r \b \\b ";
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_XML11_STRING, input)), input);

		} catch (Exception e) {
			System.out.println("Exception: " + e);
			assertTrue(false);
		}

		//section of tests that trigger exception
		try {
			StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "GG");
			assertTrue(false);
		} catch (Exception e) {
		}
		try {
			StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, null);
			assertTrue(false);
		} catch (Exception e) {
		}
		try {
			StringTransformer.transformString(null, "123");
			assertTrue(false);
		} catch (Exception e) {
		}
		try {
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "A\\u0032B\\u0e"), "A");;
			assertTrue(false);
		} catch (Exception e) {
		}
		try {
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "A\\u00egB"), "A");;
			assertTrue(false);
		} catch (Exception e) {
		}
		try {
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "A\\@B"), "A");;
			assertTrue(false);
		} catch (Exception e) {
		}

		System.out.println("Finished test");
	}
}
