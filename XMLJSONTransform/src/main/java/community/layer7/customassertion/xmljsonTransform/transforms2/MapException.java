package community.layer7.customassertion.xmljsonTransform.transforms2;

/**
 * @author Guy D.
 * October 2018
 */
public class MapException extends Exception {
	
	private static final long serialVersionUID = -6453639024565418113L;
	private String path;
	
	public MapException(String message, String path) {
		super(message);
		this.path = path;
	}
	
	public MapException(String message) {
		super(message);
		path = null;
	}
	
	public String toString() {
		if(path == null)
			return super.getMessage();
		return super.getMessage() + " (path: " + path + ")";
	}

}
