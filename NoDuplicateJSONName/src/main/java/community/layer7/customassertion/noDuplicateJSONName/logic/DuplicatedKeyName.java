package community.layer7.customassertion.noDuplicateJSONName.logic;

public class DuplicatedKeyName extends Exception {

	private static final long serialVersionUID = -1403244813862870245L;

	private String keyName;
	
	public DuplicatedKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	public String getKeyName() {
		return keyName;
	}
	
	public String toString() {
		return "DuplicatedKeyName keyName=" + keyName;
	}

}
