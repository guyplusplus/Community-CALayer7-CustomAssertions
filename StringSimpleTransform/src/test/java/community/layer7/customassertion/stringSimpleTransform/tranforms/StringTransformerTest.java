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

    @Test
	public void TestEncodings() {
		String input = null;
		try {
			//test empty string
			assertEquals(StringTransformer.transformString(StringTransformer.TRIM, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_LOWER_CASE, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UPPER_CASE, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16BE_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16BE_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16LE_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16LE_HEX, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_XML11_STRING, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, ""), "");
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, ""), "");
			
			//few simple cases
			assertEquals(StringTransformer.transformString(StringTransformer.TRIM, " "), "");
			assertEquals(StringTransformer.transformString(StringTransformer.TRIM, "\t"), "");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "45"), "E");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "4a"), "J");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, "4A"), "J");
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16_HEX, "00450e010046"), "E\u0e01F");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, "E"), "45");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, "A\u001e&\u001f'\u0020B\ufffeC\u0000D\u000CE"), "A&amp;&apos; BCDE");
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, "A&#x0e01;&amp;&apos; B&#33;C"), "A\u0e01&' B!C");;
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, "A\\B'C\"D/E	F\u0003G\u0019H\u0000I\1111J"), "A\\\\B'C\\\"D\\/E\\tF\\u0003G\\u0019H\\u0000II1J");;
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "A\\u0032B\\u0e01C\\tDE\\\\F\\\"G"), "A2B\u0e01C\tDE\\F\"G");

			//test all TO
			input = " aB\t&\\\u65e5 \u0e01 1"; //not use StringTransformer.TOOLTIP_TEST_STRING since this string can change anytime
			assertEquals(StringTransformer.transformString(StringTransformer.TRIM, input), "aB\t&\\\u65e5 \u0e01 1");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_LOWER_CASE, input), " ab\t&\\\u65e5 \u0e01 1");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UPPER_CASE, input), " AB\t&\\\u65e5 \u0e01 1");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, input), "20614209265CE697A520E0B8812031");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16_HEX, input), "FEFF00200061004200090026005C65E500200E0100200031");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16LE_HEX, input),   "200061004200090026005C00E5652000010E20003100");
			assertEquals(StringTransformer.transformString(StringTransformer.TO_UTF16BE_HEX, input),   "00200061004200090026005C65E500200E0100200031");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, input), " aB\\t&\\\\\u65E5 \u0E01 1");
			assertEquals(StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, input), " aB	&amp;\\\u65e5 \u0e01 1");
			
			//ensure all round trip decode(encode) work with a long complex case
			input = StringTransformer.TOOLTIP_TEST_STRING + " & &amp; \\ \\\\ \t ' \\' \" \\\" &#33; &amp;#33; &#x0e01; &amp;#x0e01; < > \n \\n / \\/ \\u0020 \\u00gg \\u12 ";
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_XML10_STRING, input)), input);
			input += " \f \\f \r \\r \b \\b ";  // \f and \r \b are problems on XML 1.0
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_JSON_STRING, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_XML_STRING, StringTransformer.transformString(StringTransformer.ENCODE_AS_XML11_STRING, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF8_HEX, StringTransformer.transformString(StringTransformer.TO_UTF8_HEX, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16_HEX, StringTransformer.transformString(StringTransformer.TO_UTF16_HEX, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16BE_HEX, StringTransformer.transformString(StringTransformer.TO_UTF16BE_HEX, input)), input);
			assertEquals(StringTransformer.transformString(StringTransformer.FROM_UTF16LE_HEX, StringTransformer.transformString(StringTransformer.TO_UTF16LE_HEX, input)), input);

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
		try {
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "AB\\"), "A");;
			assertTrue(false);
		} catch (Exception e) {
		}
		try {
			assertEquals(StringTransformer.transformString(StringTransformer.DECODE_JSON_STRING, "AB\\u"), "A");;
			assertTrue(false);
		} catch (Exception e) {
		}

		System.out.println("Finished test");
	}
}
