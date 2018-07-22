package community.layer7.customassertion.noDuplicateJSONName;

import com.l7tech.policy.assertion.ext.ServiceInvocation;
import com.l7tech.policy.assertion.ext.CustomAssertionStatus;
import com.l7tech.policy.assertion.ext.message.CustomPolicyContext;

import community.layer7.customassertion.noDuplicateJSONName.logic.DuplicatedKeyName;
import community.layer7.customassertion.noDuplicateJSONName.logic.JSONDuplicatedNameChecker;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Guy Deffaux
 * July 2018
 */
public class NoDuplicateJSONNameServiceInvocation extends ServiceInvocation {
	
    private static final Logger logger = Logger.getLogger(NoDuplicateJSONNameServiceInvocation.class.getName());
    //private NoDuplicateRequestParameterCustomAssertion noDuplicateRequestParameterCustomAssertion;

    @Override
    public CustomAssertionStatus checkRequest(CustomPolicyContext customPolicyContext) {
    	if(!(customAssertion instanceof NoDuplicateJSONNameCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			NoDuplicateJSONNameCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
        	return CustomAssertionStatus.FAILED;        		    		
    	}
    	
    	Object mainpartObject = customPolicyContext.getVariable("request.mainpart");
    	if(mainpartObject == null) {
			logger.log(Level.WARNING, "request.mainpart is null");
			return CustomAssertionStatus.FAILED;
    	}
    	if(!(mainpartObject instanceof String)) {
			logger.log(Level.WARNING, "request.mainpart is not a String but of class: " + mainpartObject.getClass().getName());
			return CustomAssertionStatus.FAILED;
    	}

    	try {
			JSONDuplicatedNameChecker.checkDuplicatedName((String)mainpartObject);
		} catch (DuplicatedKeyName e) {
			customPolicyContext.setVariable(NoDuplicateJSONNameCustomAssertion.DUPLICATED_NAME_CONTEXT_VARIABLE, e.getKeyName());
			return CustomAssertionStatus.FAILED;    		
		}
		//all good, no duplicated names
    	customPolicyContext.setVariable(NoDuplicateJSONNameCustomAssertion.DUPLICATED_NAME_CONTEXT_VARIABLE, null);
		return CustomAssertionStatus.NONE;
    }
    

}
