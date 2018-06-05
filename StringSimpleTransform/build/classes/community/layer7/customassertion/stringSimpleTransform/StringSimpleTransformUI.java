package community.layer7.customassertion.stringSimpleTransform;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.l7tech.policy.assertion.ext.AssertionEditor;
import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.assertion.ext.CustomAssertionUI;

/**
 * @author Guy Deffaux
 * June 2018
 */
public class StringSimpleTransformUI implements CustomAssertionUI, Serializable {

	private static final long serialVersionUID = -1503148014955165491L;
    private static final Logger logger = Logger.getLogger(StringSimpleTransformUI.class.getName());

	@Override
	public AssertionEditor getEditor(CustomAssertion customAssertion) {
        if (!(customAssertion instanceof StringSimpleTransformCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			StringSimpleTransformCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
            throw new IllegalArgumentException(StringSimpleTransformCustomAssertion.class +" type is required");
        }
        return new StringSimpleTransformJDialog((StringSimpleTransformCustomAssertion) customAssertion);
	}

	@Override
	public ImageIcon getLargeIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource("icon.stringsimpletransform.32.png"));
	}

	@Override
	public ImageIcon getSmallIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource("icon.stringsimpletransform.16.png"));
	}
}
