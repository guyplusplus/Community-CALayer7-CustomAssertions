package community.layer7.customassertion.stringSimpleTransform;

import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.assertion.UsesVariables;
import com.l7tech.policy.assertion.SetsVariables;
import com.l7tech.policy.variable.VariableMetadata;

/**
 * @author Guy Deffaux
 * June 2018
 */
public class StringSimpleTransformCustomAssertion implements CustomAssertion, UsesVariables, SetsVariables {

	private static final long serialVersionUID = -2648981123267655293L;
	private String inputVariable;
	private String outputVariable;
	private String transformationType;
	
    public String getName() {
        return "String Simple Transform";
    }

    public String getInputVariable() {
        return inputVariable;
    }

    public void setInputVariable(String inputVariable) {
        this.inputVariable = inputVariable;
    }

	public String getOutputVariable() {
		return outputVariable;
	}

	public void setOutputVariable(String outputVariable) {
		this.outputVariable = outputVariable;
	}

	public String getTransformationType() {
		return transformationType;
	}

	public void setTransformationType(String transformationType) {
		this.transformationType = transformationType;
	}

    public String[] getVariablesUsed() {
        if (inputVariable == null)
        		return new String[]{};
        return new String[] {"" + inputVariable};
    }

    public VariableMetadata[] getVariablesSet() {
        // this custom assertion does not happen to be setting context variables
    	if(outputVariable == null)
    		return new VariableMetadata[] {};
        return new VariableMetadata[] {
                new VariableMetadata("" + outputVariable),
        };
    }

}
