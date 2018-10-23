package community.layer7.customassertion.xmljsonTransform.transforms;

/**
 * @author Guy D.
 * October 2018
 */
public class JSONSchemaLoadException extends Exception {

	private static final long serialVersionUID = 2305445806587352545L;

	public JSONSchemaLoadException(String message) {
		super(message);
	}
	
	public String toString() {
		return getMessage();
	}
}
