package community.layer7.customassertion.xmljsonTransform.transforms2;

public class XMLNodeSpecSimpleValue extends XMLNodeSpec {
	
	public XMLNodeSpecSimpleValue(int nodeType) {
		super(nodeType);
	}

	@Override
	public boolean isXMLAttribute() {
		return isXMLAttribute;
	}
}
