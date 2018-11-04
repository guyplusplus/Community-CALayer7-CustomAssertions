package community.layer7.customassertion.xmljsonTransform.transforms2;

public class XMLNodeSpecPrimitiveType extends XMLNodeSpec {
	
	public XMLNodeSpecPrimitiveType(int nodeType) {
		super(nodeType);
	}

	public boolean isXMLAttribute() {
		return isXMLAttribute;
	}
	@Override
	public boolean isPrimitiveType() {
		return true;
	}
	
}
