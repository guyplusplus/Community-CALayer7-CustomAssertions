package community.layer7.customassertion.xmljsonTransform;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.w3c.dom.Document;

import com.l7tech.policy.assertion.ext.CustomAssertionStatus;
import com.l7tech.policy.assertion.ext.ServiceInvocation;
import com.l7tech.policy.assertion.ext.message.CustomPolicyContext;

import community.layer7.customassertion.xmljsonTransform.transforms2.JSONSchemaForXML;
import community.layer7.customassertion.xmljsonTransform.cache.SchemaCache;

public class XMLJSONTransformServiceInvocation extends ServiceInvocation {
    
	private static final String JSONXML_SCHEMACACHE_MAXAGE_VARIABLE_NAME = "jsonxml.schemaCache.maxAge";
	private static final String JSONXML_SCHEMACACHE_MAXDOWNLOADSIZE_VARIABLE_NAME = "jsonxml.schemaCache.maxDownloadSize";
	private static final String JSONXML_SCHEMACACHE_MAXENTRIES_VARIABLE_NAME = "jsonxml.schemaCache.maxEntries";
	
	private static final long JSONXML_SCHEMA_CACHE_UPDATE_FREQUENCY = 5 * 60 * 1000;

	private static final Logger logger = Logger.getLogger(XMLJSONTransformServiceInvocation.class.getName());
	
	private static long lastSchemaCacheUpdateTimeInMs;
    
    private XMLJSONTransformCustomAssertion xmljsonTransformCustomAssertion;
    private String input;

    @Override
    public CustomAssertionStatus checkRequest(CustomPolicyContext customPolicyContext) {
    	if(!(customAssertion instanceof XMLJSONTransformCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			XMLJSONTransformCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
        	return CustomAssertionStatus.FAILED;        		    		
    	}
    	updateJSONXMLSchemaCacheUpdateFrequency(customPolicyContext);
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
        try {
        	String jsonSchemaString = customPolicyContext.expandVariable(xmljsonTransformCustomAssertion.getJsonSchema());
        	JSONSchemaForXML jsonSchemaForXML = SchemaCache.getSingleton().getJSONSchemaForXML(jsonSchemaString);
        	String output = null;
        	if(xmljsonTransformCustomAssertion.getTransformationTypeID() == TransformationHelper.XML_TO_JSON_TRANSFORMATION_ID) {
	        	JSONObject jsonObject = jsonSchemaForXML.mapXMLToJSON(input);
	            output = JSONSchemaForXML.jsonToString(jsonObject, xmljsonTransformCustomAssertion.isOutputFormatted());
        	}
        	else if(xmljsonTransformCustomAssertion.getTransformationTypeID() == TransformationHelper.JSON_TO_XML_TRANSFORMATION_ID) {
        		Document document = jsonSchemaForXML.mapJSONToXML(input);
        		output = JSONSchemaForXML.xmlToString(document, xmljsonTransformCustomAssertion.isOutputFormatted());
        	}
        	else
        		throw new Exception("Internal error: invalid transformationTypeID=" + xmljsonTransformCustomAssertion.getTransformationTypeID());
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

    private static synchronized void updateJSONXMLSchemaCacheUpdateFrequency(CustomPolicyContext customPolicyContext) {
    	long now = System.currentTimeMillis();
    	long pointInTime = now - JSONXML_SCHEMA_CACHE_UPDATE_FREQUENCY;
    	if(lastSchemaCacheUpdateTimeInMs > pointInTime)
    		return;
    	try {
    		long l = Long.parseLong(customPolicyContext.expandVariable("${gateway." + JSONXML_SCHEMACACHE_MAXAGE_VARIABLE_NAME + "}"));
    		logger.log(Level.INFO, JSONXML_SCHEMACACHE_MAXAGE_VARIABLE_NAME  + " set to " + l); 
    		SchemaCache.setJsonxmlSchemaCacheMaxAge(l);
    	}
    	catch(Exception e) {
    		logger.log(Level.WARNING, "Problem to parse " + JSONXML_SCHEMACACHE_MAXAGE_VARIABLE_NAME + ". e=" + e);
    	}
    	try {
    		int i = Integer.parseInt(customPolicyContext.expandVariable("${gateway." + JSONXML_SCHEMACACHE_MAXDOWNLOADSIZE_VARIABLE_NAME + "}"));    		
    		logger.log(Level.INFO, JSONXML_SCHEMACACHE_MAXDOWNLOADSIZE_VARIABLE_NAME  + " set to " + i); 
    		SchemaCache.setJsonxmlSchemaCacheMaxDownloadSize(i);
    	}
    	catch(Exception e) {
    		logger.log(Level.WARNING, "Problem to parse " + JSONXML_SCHEMACACHE_MAXDOWNLOADSIZE_VARIABLE_NAME + ". e=" + e);
    	}
    	try {
    		int i = Integer.parseInt(customPolicyContext.expandVariable("${gateway." + JSONXML_SCHEMACACHE_MAXENTRIES_VARIABLE_NAME + "}"));    		
    		logger.log(Level.INFO, JSONXML_SCHEMACACHE_MAXENTRIES_VARIABLE_NAME  + " set to " + i); 
    		SchemaCache.setJsonxmlSchemaCacheMaxEntries(Integer.parseInt(customPolicyContext.expandVariable("${gateway." + JSONXML_SCHEMACACHE_MAXENTRIES_VARIABLE_NAME + "}")));
    	}
    	catch(Exception e) {
    		logger.log(Level.WARNING, "Problem to parse " + JSONXML_SCHEMACACHE_MAXENTRIES_VARIABLE_NAME + ". e=" + e);
    	}
    	lastSchemaCacheUpdateTimeInMs = now;
    }
}