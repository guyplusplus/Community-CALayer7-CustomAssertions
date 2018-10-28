package community.layer7.customassertion.xmljsonTransform.transforms2;

public class XMLNodeSpecSimpleValue extends XMLNodeSpec {
	
	public XMLNodeSpecSimpleValue(int nodeType) {
		super(nodeType);
	}

	public boolean isXMLAttribute() {
		return isXMLAttribute;
	}
	@Override
	public boolean isSimpleValue() {
		return true;
	}
	
}
