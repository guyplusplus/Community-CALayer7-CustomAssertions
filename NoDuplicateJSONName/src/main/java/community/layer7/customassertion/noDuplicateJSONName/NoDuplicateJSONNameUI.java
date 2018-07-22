package community.layer7.customassertion.noDuplicateJSONName;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.l7tech.policy.assertion.ext.AssertionEditor;
import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.assertion.ext.CustomAssertionUI;

/**
 * @author Guy Deffaux
 * July 2018
 */
public class NoDuplicateJSONNameUI implements CustomAssertionUI, Serializable {
	
	private static final long serialVersionUID = 3521962669443276478L;
	private static final Logger logger = Logger.getLogger(NoDuplicateJSONNameUI.class.getName());

	@Override
	public AssertionEditor getEditor(CustomAssertion customAssertion) {
        if (!(customAssertion instanceof NoDuplicateJSONNameCustomAssertion)) {
        	logger.log(Level.SEVERE, String.format("customAssertion must be of type [{%s}], but it is of type [{%s}] instead",
        			NoDuplicateJSONNameCustomAssertion.class.getName(),
                    (customAssertion == null ? "null" : customAssertion.getClass().getName())));
            throw new IllegalArgumentException(NoDuplicateJSONNameCustomAssertion.class +" type is required");
        }
        //return new NoDuplicateJSONNameJDialog((NoDuplicateJSONNameCustomAssertion) customAssertion);
        return null;
	}

	@Override
	public ImageIcon getLargeIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource("icon.noduplicatejsonname.32.png"));
	}

	@Override
	public ImageIcon getSmallIcon() {
		return new ImageIcon(getClass().getClassLoader().getResource("icon.noduplicatejsonname.16.png"));
	}
}
