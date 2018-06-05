package community.layer7.customassertion.stringSimpleTransform.tranforms;

/**
 * @author Guy Deffaux
 * June 2018
 */
public class StringTransformTypeWithLabel {
	
	private String transformType;
	private String label;
	
	public StringTransformTypeWithLabel(String transformType, String label) {
		this.transformType = transformType;
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getTransformType() {
		return transformType;
	}
	
	public boolean equals(Object o) {
		if(o == null || !(o instanceof StringTransformTypeWithLabel))
			return false;
		return transformType.equals(((StringTransformTypeWithLabel)o).transformType);
	}
}
