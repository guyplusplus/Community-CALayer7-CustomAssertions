package community.layer7.customassertion.xmljsonTransform.transforms2;

/**
 * @author Guy Deffaux
 * October 2018
 */
public class JSONSchemaLoadException extends Exception {


	private static final long serialVersionUID = -6419307147871763588L;

	public JSONSchemaLoadException(String message) {
		super(message);
	}
	
	public String toString() {
		return getMessage();
	}
}
