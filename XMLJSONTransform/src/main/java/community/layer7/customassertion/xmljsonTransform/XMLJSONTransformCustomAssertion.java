package community.layer7.customassertion.xmljsonTransform;

import com.l7tech.policy.assertion.SetsVariables;
import com.l7tech.policy.assertion.UsesVariables;
import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.variable.VariableMetadata;

/**
 * @author Guy D.
 * October 2018
 */
public class XMLJSONTransformCustomAssertion implements CustomAssertion, UsesVariables, SetsVariables {

	private static final long serialVersionUID = -428665346501717715L;
	public static final String ERROR_MESSAGE_CONTEXT_VARIABLE = "transformError";
	private String inputVariable;
	private String outputVariable;
	private int transformationTypeID;
	private boolean isOutputFormatted;
	private String jsonSchema;
	
    public String getJsonSchema() {
		return jsonSchema;
	}

	public void setJsonSchema(String jsonSchema) {
		this.jsonSchema = jsonSchema;
	}

	public boolean isOutputFormatted() {
		return isOutputFormatted;
	}

	public void setOutputFormatted(boolean isOutputFormatted) {
		this.isOutputFormatted = isOutputFormatted;
	}

	public String getName() {
        return "XML / JSON Transform";
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

	public int getTransformationTypeID() {
		return transformationTypeID;
	}

	public void setTransformationTypeID(int transformationTypeID) {
		if(transformationTypeID < 0 || transformationTypeID > 1)
			throw new IllegalArgumentException("transformationTypeID shall be 0 or 1 only");
		this.transformationTypeID = transformationTypeID;
	}

    public String[] getVariablesUsed() {
        if (inputVariable == null)
        		return new String[]{};
        return new String[] {"" + inputVariable};
    }

    public VariableMetadata[] getVariablesSet() {
        // this custom assertion does not happen to be setting context variables
    	if(outputVariable == null)
    		return new VariableMetadata[] {
    				new VariableMetadata(ERROR_MESSAGE_CONTEXT_VARIABLE),
    		};
        return new VariableMetadata[] {
	                new VariableMetadata("" + outputVariable),
	                new VariableMetadata(ERROR_MESSAGE_CONTEXT_VARIABLE),
            };
    }

}