package community.layer7.customassertion.stringSimpleTransform.tranforms;

import java.util.Hashtable;

import javax.xml.bind.DatatypeConverter;

/**
 * @author Guy Deffaux
 * June 2018
 * 
 * To add a new transformation type:
 *   - define a new type and create a unique String
 *   - create a new StringTransformTypeWithLabel with its human readable description, and add it to the array SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY
 *   - if this transformation shall appear in the combobox tooltip, add the transformType to the array SUPPORTED_TRANSFORMS_IN_COMBOX_TOOLTIP
 */
public class StringTransformer {
	
	//DO NOT CHANGE the string values or it will break currently deployed assertions
	private static final String TRIM = "trim";
	private static final String TO_LOWER_CASE = "toLowerCase";
	private static final String TO_UPPER_CASE = "toUpperCase";
	private static final String TO_UTF8_HEX = "toUTF8Hex";
	private static final String FROM_UTF8_HEX = "fromUTF8Hex";
	private static final String TO_UTF16_HEX = "toUTF16Hex";
	private static final String FROM_UTF16_HEX = "fromUTF16Hex";
	private static final String TO_UTF16LE_HEX = "toUTF16LEHex";
	private static final String FROM_UTF16LE_HEX = "fromUTF16LEHex";
	private static final String TO_UTF16BE_HEX = "toUTF16BEHex";
	private static final String FROM_UTF16BE_HEX = "fromUTF16BEHex";
	private static final String ENCODE_AS_JSON_STRING = "encodeAsJSONString";
	private static final String DECODE_JSON_STRING = "decodeJSONString";
	private static final String ENCODE_AS_XML_STRING = "encodeAsXMLString";
	
	private static final StringTransformTypeWithLabel[] SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY = new StringTransformTypeWithLabel[] {
		new StringTransformTypeWithLabel(TRIM, "Trim spaces"),
		new StringTransformTypeWithLabel(TO_LOWER_CASE, "To lower case"),
		new StringTransformTypeWithLabel(TO_UPPER_CASE, "To upper case"),
		new StringTransformTypeWithLabel(TO_UTF8_HEX, "To UTF-8 Hex"),
		new StringTransformTypeWithLabel(FROM_UTF8_HEX, "From UTF-8 Hex"),
		new StringTransformTypeWithLabel(TO_UTF16_HEX, "To UTF-16 Hex"),
		new StringTransformTypeWithLabel(FROM_UTF16_HEX, "From UTF-16 Hex"),
		new StringTransformTypeWithLabel(TO_UTF16LE_HEX, "To UTF-16LE Hex"),
		new StringTransformTypeWithLabel(FROM_UTF16LE_HEX, "From UTF-16LE Hex"),
		new StringTransformTypeWithLabel(TO_UTF16BE_HEX, "To UTF-16BE Hex"),
		new StringTransformTypeWithLabel(FROM_UTF16BE_HEX, "From UTF-16BE Hex"),
		new StringTransformTypeWithLabel(ENCODE_AS_JSON_STRING, "Encode as JSON string"),
		new StringTransformTypeWithLabel(DECODE_JSON_STRING, "Decode JSON string"),
		new StringTransformTypeWithLabel(ENCODE_AS_XML_STRING, "Encode as XML string"),
	};
	
	private static final String SUPPORTED_TRANSFORMS_IN_COMBOX_TOOLTIP[] = new String[] {
		TRIM, TO_LOWER_CASE, TO_UPPER_CASE,
		TO_UTF8_HEX, TO_UTF16_HEX,
		TO_UTF16LE_HEX, TO_UTF16BE_HEX,
		ENCODE_AS_JSON_STRING, ENCODE_AS_XML_STRING,
	};
	
	private static final String TEST_STRING = " aB\t&\\\u65e51\u0e012";
	
	private static Hashtable<String, StringTransformTypeWithLabel> SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY_HASHTABLE =
			new Hashtable<String, StringTransformTypeWithLabel>();
	
	private static String SUPPORTED_TRANSFORMS_COMBOX_TOOLTIP = "";
			
	static {
		try {
			for(StringTransformTypeWithLabel stringTransformTypeWithLabel:SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY)
				SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY_HASHTABLE.put(stringTransformTypeWithLabel.getTransformType(), stringTransformTypeWithLabel);				
			//each entry requires ENCODE_AS_XML_STRING since it is a HTML based tooltip
			StringBuilder sb = new StringBuilder("<html>The string '").append(TEST_STRING).append("' (space, tab, nichi in JP, kokai in TH) gets transformed as:<ul>");
			for(String supportedTransform:SUPPORTED_TRANSFORMS_IN_COMBOX_TOOLTIP) {
				sb.append("<li>").append(getSupportedTransformsWithLabel(supportedTransform).getLabel()).append(": '");
				sb.append(transformString(ENCODE_AS_XML_STRING, transformString(supportedTransform, TEST_STRING)));
				sb.append("'</li>");
			}
			sb.append("</ul></html>");
			SUPPORTED_TRANSFORMS_COMBOX_TOOLTIP = sb.toString();
		}
		catch(Exception e) {
			//impossible
			SUPPORTED_TRANSFORMS_COMBOX_TOOLTIP = "Failed to initialize the tooltip: " + e;
		}
	}
	
	public static String transformString(String transformationType, String inputString) throws Exception {
		//check input and throw nice exception content if required
		if(transformationType == null)
			throw new NullPointerException("transformationType is null");
		if(inputString == null)
			throw new NullPointerException("inputString is null");
		switch (transformationType) {
		case TRIM:
			return inputString.trim();
		case TO_LOWER_CASE:
			return inputString.toLowerCase();
		case TO_UPPER_CASE:
			return inputString.toUpperCase();
		case TO_UTF8_HEX:
			return DatatypeConverter.printHexBinary(inputString.getBytes("UTF-8"));
		case FROM_UTF8_HEX:
			return new String(DatatypeConverter.parseHexBinary(inputString), "UTF-8");
		case TO_UTF16_HEX:
			return DatatypeConverter.printHexBinary(inputString.getBytes("UTF-16"));
		case FROM_UTF16_HEX:
			return new String(DatatypeConverter.parseHexBinary(inputString), "UTF-16");
		case TO_UTF16LE_HEX:
			return DatatypeConverter.printHexBinary(inputString.getBytes("UTF-16LE"));
		case FROM_UTF16LE_HEX:
			return new String(DatatypeConverter.parseHexBinary(inputString), "UTF-16LE");
		case TO_UTF16BE_HEX:
			return DatatypeConverter.printHexBinary(inputString.getBytes("UTF-16BE"));
		case FROM_UTF16BE_HEX:
			return new String(DatatypeConverter.parseHexBinary(inputString), "UTF-16BE");
		case ENCODE_AS_JSON_STRING:
			return JSONUtil.quote(inputString);
		case DECODE_JSON_STRING:
			return JSONUtil.JSONStringToString(inputString);
		case ENCODE_AS_XML_STRING:
			return XMLUtil.escapeXML(inputString);
		}
		throw new Exception("Unknow transformationType: " + transformationType);
	}
	
	public static StringTransformTypeWithLabel[] getSupportedTransformsWithLabelArray() {
		return SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY;
	}
	
	public static StringTransformTypeWithLabel getSupportedTransformsWithLabel(String transformationType) {
		return SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY_HASHTABLE.get(transformationType);
	}
	
	public static String getSupportedTransformsComboBoxTooltip() {
		return SUPPORTED_TRANSFORMS_COMBOX_TOOLTIP;
	}
	
	public static void main(String[] args) {
		//test code
		String input = null;
		try {
			input = "";
			System.out.println(input + "." + TO_UTF8_HEX + "=" + transformString(TO_UTF8_HEX, input));
			System.out.println(input + "." + FROM_UTF8_HEX + "=" + transformString(FROM_UTF8_HEX, input));
			input = "45";
			System.out.println(input + "." + FROM_UTF8_HEX + "=" + transformString(FROM_UTF8_HEX, input));			
			input = "4a";
			System.out.println(input + "." + FROM_UTF8_HEX + "=" + transformString(FROM_UTF8_HEX, input));			
			input = "4A";
			System.out.println(input + "." + FROM_UTF8_HEX + "=" + transformString(FROM_UTF8_HEX, input));			
			input = "00450e010046";
			System.out.println(input + "." + FROM_UTF16_HEX + "=" + transformString(FROM_UTF16_HEX, input));			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = "GG";
			System.out.println(input + "." + TO_UTF8_HEX + "=" + transformString(TO_UTF8_HEX, input));
			System.out.println(input + "." + FROM_UTF8_HEX + "=" + transformString(FROM_UTF8_HEX, input));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = null;
			System.out.println(input + "." + TO_UTF8_HEX + "=" + transformString(TO_UTF8_HEX, input));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = "00";
			System.out.println(input + ".null=" + transformString(null, input));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = TEST_STRING;
			System.out.println(input + "." + TRIM + "=" + transformString(TRIM, input));
			System.out.println(input + "." + TO_LOWER_CASE + "=" + transformString(TO_LOWER_CASE, input));
			System.out.println(input + "." + TO_UPPER_CASE + "=" + transformString(TO_UPPER_CASE, input));
			System.out.println(input + "." + TO_UTF8_HEX + "=" + transformString(TO_UTF8_HEX, input));
			System.out.println(input + "." + TO_UTF16_HEX + "=" + transformString(TO_UTF16_HEX, input));
			System.out.println(input + "." + TO_UTF16LE_HEX + "=" + transformString(TO_UTF16LE_HEX, input));
			System.out.println(input + "." + TO_UTF16BE_HEX + "=" + transformString(TO_UTF16BE_HEX, input));
			System.out.println(input + "." + ENCODE_AS_JSON_STRING + "=" + transformString(ENCODE_AS_JSON_STRING, input));
			System.out.println(input + "." + ENCODE_AS_XML_STRING + "=" + transformString(ENCODE_AS_XML_STRING, input));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = "A\u001e&\u001f'\u0020B\ufffeC\u0000D";
			System.out.println(input + "." + ENCODE_AS_XML_STRING + "=" + transformString(ENCODE_AS_XML_STRING, input));
			System.out.println("Via COMMON LANG: " + org.apache.commons.text.StringEscapeUtils.escapeXml11(input));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = TEST_STRING;
			String jsonString = transformString(ENCODE_AS_JSON_STRING, input);
			System.out.println(input + "." + ENCODE_AS_JSON_STRING + "=" + jsonString);
			String decoded = transformString(DECODE_JSON_STRING, jsonString);
			System.out.println(jsonString + "." + DECODE_JSON_STRING + "=" + decoded);
			System.out.println("input == decoded ? " + (input.equals(decoded)));
			System.out.println(input + "." + TO_UTF16_HEX + "=" + transformString(TO_UTF16_HEX, input));
			System.out.println(decoded + "." + TO_UTF16_HEX + "=" + transformString(TO_UTF16_HEX, decoded));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = "A\\u0032B\\u0e01C\\tD\\'E\\\"F";
			String decoded = transformString(DECODE_JSON_STRING, input);
			System.out.println(input + "." + DECODE_JSON_STRING + "=" + decoded);
			System.out.println(decoded + "." + TO_UTF16_HEX + "=" + transformString(TO_UTF16_HEX, decoded));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		try {
			input = "A\\u0032B\\u0e";
			System.out.println(input + "." + DECODE_JSON_STRING + "=" + transformString(DECODE_JSON_STRING, input));
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}
}
