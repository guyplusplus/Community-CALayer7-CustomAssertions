package community.layer7.customassertion.xmljsonTransform;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;

import com.l7tech.policy.assertion.ext.AssertionEditor;
import com.l7tech.policy.assertion.ext.AssertionEditorSupport;
import com.l7tech.policy.assertion.ext.EditListener;

import community.layer7.customassertion.xmljsonTransform.transforms.TransformationHelper;

/**
 * @author Guy Deffaux
 * October 2018
 */
public class XMLJSONTransformJDialog extends XMLJSONTransformBaseJDialog implements AssertionEditor {

	private static final long serialVersionUID = -1124987349256892481L;
	private static final Logger logger = Logger.getLogger(XMLJSONTransformJDialog.class.getName());
	private XMLJSONTransformCustomAssertion xmljsonTransformCustomAssertion;
	private AssertionEditorSupport editorSupport;
	
	public XMLJSONTransformJDialog(XMLJSONTransformCustomAssertion customAssertion) {
		xmljsonTransformCustomAssertion = customAssertion;
		editorSupport = new AssertionEditorSupport(this);
		//set icon of the JDialog
		Image dialogIconImage = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.xmljsontransform.32.png"));
		if(dialogIconImage != null)
			super.setIconImage(dialogIconImage);
		//register ESC key
		registerESCKey();
		//set input textfield
		super.getInputVariableNameJTextField().setText(customAssertion.getInputVariable());
		//set output textfield
		super.getOutputVariableNameJTextField().setText(customAssertion.getOutputVariable());
		//set combox
		super.getTransformationJComboBox().setModel(new DefaultComboBoxModel(TransformationHelper.getSupportedTransformations()));
		super.getTransformationJComboBox().setSelectedIndex(customAssertion.getTransformationTypeID() - 1);
		//set formatted output checkbox
		super.getFormatOutputJCheckBox().setSelected(customAssertion.isOutputFormatted());
	}

	@Override
	public void addEditListener(EditListener listener) {
		editorSupport.addListener(listener);
	}

	@Override
	public void edit() {
		setVisible(true);
	}

	@Override
	public void removeEditListener(EditListener listener) {
		editorSupport.removeListener(listener);
	}
	
	@Override
	protected void onCancel() {
		editorSupport.fireCancelled(xmljsonTransformCustomAssertion);
		dispose();
	}
	
	@Override
	protected void onOK() {
		xmljsonTransformCustomAssertion.setInputVariable(super.getInputVariableNameJTextField().getText());
		xmljsonTransformCustomAssertion.setOutputVariable(super.getOutputVariableNameJTextField().getText());
		xmljsonTransformCustomAssertion.setTransformationTypeID(super.getTransformationJComboBox().getSelectedIndex() + 1);
		xmljsonTransformCustomAssertion.setOutputFormatted(super.getFormatOutputJCheckBox().isSelected());
		editorSupport.fireEditAccepted(xmljsonTransformCustomAssertion);
		dispose();
	}
}