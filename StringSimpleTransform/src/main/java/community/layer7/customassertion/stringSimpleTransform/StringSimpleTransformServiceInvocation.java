package community.layer7.customassertion.stringSimpleTransform;

import com.l7tech.policy.assertion.ext.ServiceInvocation;
import com.l7tech.policy.assertion.ext.CustomAssertionStatus;
import com.l7tech.policy.assertion.ext.message.CustomPolicyContext;

import community.layer7.customassertion.stringSimpleTransform.tranforms.StringTransformer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Guy Deffaux
 * June 2018
 */
public class StringSimpleTransformServiceInvocation extends ServiceInvocation {
    private static final Logger logger = Logger.getLogger(StringSimpleTransformServiceInvocation.class.getName());
    private StringSimpleTransformCustomAssertion stringSimpleTransformCustomAssertion;
    private String input;
    private String transformationType;

    @Override
    public CustomAssertionStatus checkRequest(CustomPolicyContext customPolicyContext) {
    	if(!(customAssertion instanceof StringSimpleTransformCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			StringSimpleTransformCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
        	return CustomAssertionStatus.FAILED;        		    		
    	}
        stringSimpleTransformCustomAssertion = (StringSimpleTransformCustomAssertion)customAssertion;
        if (stringSimpleTransformCustomAssertion.getInputVariable() != null) {
        	Object inputAsObject = customPolicyContext.getVariable(stringSimpleTransformCustomAssertion.getInputVariable());
        	if(inputAsObject == null) { //such as using undefined variable or InputVariable is empty
        		//we pick the gentle approach : use empty s
        		inputAsObject = "";
        		//other approach would be to raise a failure, as bellow:
            	//logger.log(Level.WARNING, "Input value is null");
            	//return CustomAssertionStatus.FAILED;        		
        	}
        	if(!(inputAsObject instanceof String)) {
            	logger.log(Level.WARNING, "Input value is not a String object, but a " + inputAsObject.getClass().getName());
            	return CustomAssertionStatus.FAILED;        		
        	}
        	input = (String)inputAsObject;
        } else {
        	logger.log(Level.WARNING, "InputVariable is not defined");
        	return CustomAssertionStatus.FAILED;
        }
        if (stringSimpleTransformCustomAssertion.getTransformationType() != null)
        	transformationType = stringSimpleTransformCustomAssertion.getTransformationType();
        else {
        	logger.log(Level.WARNING, "transformationType is not defined");
        	return CustomAssertionStatus.FAILED;
        }
        try {
            String output = StringTransformer.transformString(transformationType, input);
            customPolicyContext.setVariable(stringSimpleTransformCustomAssertion.getOutputVariable(), output);
        }
        catch(Exception e) {
        	logger.log(Level.WARNING, "Failed to transform, error: " + e); //no need stack trace
        	return CustomAssertionStatus.FAILED;
        }
        return CustomAssertionStatus.NONE;
    }


}
