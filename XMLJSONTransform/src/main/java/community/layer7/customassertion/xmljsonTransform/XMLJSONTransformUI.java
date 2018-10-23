package community.layer7.customassertion.xmljsonTransform;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.l7tech.policy.assertion.ext.AssertionEditor;
import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.assertion.ext.CustomAssertionUI;

/**
 * @author Guy D.
 * October 2018
 */
public class XMLJSONTransformUI  implements CustomAssertionUI, Serializable {

	private static final long serialVersionUID = 6353938677654531903L;
	private static final Logger logger = Logger.getLogger(XMLJSONTransformUI.class.getName());

	@Override
	public AssertionEditor getEditor(CustomAssertion customAssertion) {
        if (!(customAssertion instanceof XMLJSONTransformCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			XMLJSONTransformCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
            throw new IllegalArgumentException(XMLJSONTransformCustomAssertion.class +" type is required");
        }
        return new XMLJSONTransformJDialog((XMLJSONTransformCustomAssertion) customAssertion);
	}

	@Override
	public ImageIcon getLargeIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource("icon.xmljsontransform.32.png"));
	}

	@Override
	public ImageIcon getSmallIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource("icon.xmljsontransform.16.png"));
	}
}
