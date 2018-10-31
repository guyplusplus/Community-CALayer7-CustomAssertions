package community.layer7.customassertion.xmljsonTransform;

/**
 * @author Guy D.
 * October 2018
 */
public class TransformationHelper {
	
	private static final String XML_TO_JSON_TRANSFORMATION = "XML to JSON";
	private static final String JSON_TO_XML_TRANSFORMATION = "JSON to XML";
	private static final String[] SUPPORTED_TRANSFORMATIONS = new String[] {XML_TO_JSON_TRANSFORMATION, JSON_TO_XML_TRANSFORMATION};
	
	public static final int XML_TO_JSON_TRANSFORMATION_ID = 0;
	public static final int JSON_TO_XML_TRANSFORMATION_ID = 1;
	
	
	public static String[] getSupportedTransformations() {
		return SUPPORTED_TRANSFORMATIONS;
	}

}