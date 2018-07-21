package community.layer7.customassertion.noDuplicateJSONName;

import com.l7tech.policy.assertion.SetsVariables;
import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.variable.VariableMetadata;

/**
 * @author Guy Deffaux
 * July 2018
 */
public class NoDuplicateJSONNameCustomAssertion implements CustomAssertion, SetsVariables {

	private static final long serialVersionUID = 7730311911192766605L;
	public static final String DUPLICATED_NAME_CONTEXT_VARIABLE = "duplicatedName";
	
	public String getName() {
        return "No Duplicate JSON Name";
    }

	@Override
	public VariableMetadata[] getVariablesSet() {
        return new VariableMetadata[] { new VariableMetadata(DUPLICATED_NAME_CONTEXT_VARIABLE) };
	}
}
