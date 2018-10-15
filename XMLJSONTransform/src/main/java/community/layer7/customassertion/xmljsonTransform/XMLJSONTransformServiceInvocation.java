package community.layer7.customassertion.xmljsonTransform;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.l7tech.policy.assertion.ext.CustomAssertionStatus;
import com.l7tech.policy.assertion.ext.ServiceInvocation;
import com.l7tech.policy.assertion.ext.message.CustomPolicyContext;

import community.layer7.customassertion.xmljsonTransform.transforms.JSONSchemaForXML;

public class XMLJSONTransformServiceInvocation extends ServiceInvocation {
    private static final Logger logger = Logger.getLogger(XMLJSONTransformServiceInvocation.class.getName());
    private XMLJSONTransformCustomAssertion xmljsonTransformCustomAssertion;
    private String input;
    private int transformationTypeID;

    @Override
    public CustomAssertionStatus checkRequest(CustomPolicyContext customPolicyContext) {
    	if(!(customAssertion instanceof XMLJSONTransformCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			XMLJSONTransformCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
        	return CustomAssertionStatus.FAILED;        		    		
    	}
        xmljsonTransformCustomAssertion = (XMLJSONTransformCustomAssertion)customAssertion;
        if (xmljsonTransformCustomAssertion.getInputVariable() != null) {
        	Object inputAsObject = customPolicyContext.getVariable(xmljsonTransformCustomAssertion.getInputVariable());
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
        if (xmljsonTransformCustomAssertion.getTransformationTypeID() != 0)
        	transformationTypeID = xmljsonTransformCustomAssertion.getTransformationTypeID();
        else {
        	logger.log(Level.WARNING, "transformationTypeID is defaulted to 0");
        	return CustomAssertionStatus.FAILED;
        }
        try {
        	String jsonSchemaString = xmljsonTransformCustomAssertion.getJsonSchema();
        	JSONSchemaForXML jsonSchemaForXML = new JSONSchemaForXML(jsonSchemaString);
        	JSONObject o = jsonSchemaForXML.mapXMLToJSON(input);
            String output = o.toString(xmljsonTransformCustomAssertion.isOutputFormatted() ? 2 : 0);
            customPolicyContext.setVariable(xmljsonTransformCustomAssertion.getOutputVariable(), output);
            customPolicyContext.setVariable(XMLJSONTransformCustomAssertion.ERROR_MESSAGE_CONTEXT_VARIABLE, "");
        }
        catch(Exception e) {
        	logger.log(Level.WARNING, "Failed to transform, error: " + e); //no need stack trace
            customPolicyContext.setVariable(xmljsonTransformCustomAssertion.getOutputVariable(), "");
        	customPolicyContext.setVariable(XMLJSONTransformCustomAssertion.ERROR_MESSAGE_CONTEXT_VARIABLE, e.toString());
        	return CustomAssertionStatus.FAILED;
        }
        return CustomAssertionStatus.NONE;
    }


}