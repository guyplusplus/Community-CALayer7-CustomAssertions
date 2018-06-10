package community.layer7.customassertion.stringSimpleTransform.tranforms;

import java.util.Hashtable;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.text.StringEscapeUtils;

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
	//these variables are available within package for the test class
	static final String TRIM = "trim";
	static final String TO_LOWER_CASE = "toLowerCase";
	static final String TO_UPPER_CASE = "toUpperCase";
	static final String TO_UTF8_HEX = "toUTF8Hex";
	static final String FROM_UTF8_HEX = "fromUTF8Hex";
	static final String TO_UTF16_HEX = "toUTF16Hex";
	static final String FROM_UTF16_HEX = "fromUTF16Hex";
	static final String TO_UTF16LE_HEX = "toUTF16LEHex";
	static final String FROM_UTF16LE_HEX = "fromUTF16LEHex";
	static final String TO_UTF16BE_HEX = "toUTF16BEHex";
	static final String FROM_UTF16BE_HEX = "fromUTF16BEHex";
	static final String ENCODE_AS_JSON_STRING = "encodeAsJSONString";
	static final String DECODE_JSON_STRING = "decodeJSONString";
	static final String ENCODE_AS_XML10_STRING = "encodeAsXML10String";
	static final String ENCODE_AS_XML11_STRING = "encodeAsXML11String";
	static final String DECODE_XML_STRING = "decodeXMLString";
	
	static final String TOOLTIP_TEST_STRING = " aB\t&\\\u65e5 \u0e01 1";

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
		new StringTransformTypeWithLabel(ENCODE_AS_XML10_STRING, "Encode as XML 1.0 string"),
		new StringTransformTypeWithLabel(ENCODE_AS_XML11_STRING, "Encode as XML 1.1 string"),
		new StringTransformTypeWithLabel(DECODE_XML_STRING, "Decode XML string"),
	};
	
	private static final String SUPPORTED_TRANSFORMS_IN_COMBOX_TOOLTIP[] = new String[] {
		TRIM, TO_LOWER_CASE, TO_UPPER_CASE,
		TO_UTF8_HEX, TO_UTF16_HEX,
		TO_UTF16LE_HEX, TO_UTF16BE_HEX,
		ENCODE_AS_JSON_STRING, ENCODE_AS_XML10_STRING,
	};
		
	private static Hashtable<String, StringTransformTypeWithLabel> SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY_HASHTABLE =
			new Hashtable<String, StringTransformTypeWithLabel>();
	
	private static String SUPPORTED_TRANSFORMS_COMBOX_TOOLTIP = "";
			
	static {
		try {
			for(StringTransformTypeWithLabel stringTransformTypeWithLabel:SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY)
				SUPPORTED_TRANSFORMS_WITH_LABEL_ARRAY_HASHTABLE.put(stringTransformTypeWithLabel.getTransformType(), stringTransformTypeWithLabel);				
			//each entry requires ENCODE_AS_XML_STRING since it is a HTML based tooltip
			StringBuilder sb = new StringBuilder("<html>The string '").append(TOOLTIP_TEST_STRING).append("' (space, tab, nichi in JP, kokai in TH) gets transformed as:<ul>");
			for(String supportedTransform:SUPPORTED_TRANSFORMS_IN_COMBOX_TOOLTIP) {
				sb.append("<li>").append(getSupportedTransformsWithLabel(supportedTransform).getLabel()).append(": '");
				sb.append(transformString(ENCODE_AS_XML10_STRING, transformString(supportedTransform, TOOLTIP_TEST_STRING)));
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
			//return StringEscapeUtils.escapeJson(inputString);
			return JSONUtil.encodeJSON(inputString);
		case DECODE_JSON_STRING:
			return JSONUtil.JSONStringToString(inputString);
			//waiting for commons text 1.4. V1.3 does not unescape ", / and \
			//return StringEscapeUtils.unescapeJson(inputString);
		case ENCODE_AS_XML10_STRING:
			return StringEscapeUtils.escapeXml10(inputString);
		case ENCODE_AS_XML11_STRING:
			return StringEscapeUtils.escapeXml11(inputString);
		case DECODE_XML_STRING:
			return StringEscapeUtils.unescapeXml(inputString);
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
}
