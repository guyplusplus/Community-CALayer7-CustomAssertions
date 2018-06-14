package community.layer7.customassertion.stringSimpleTransform;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;

import com.l7tech.policy.assertion.ext.AssertionEditor;
import com.l7tech.policy.assertion.ext.AssertionEditorSupport;
import com.l7tech.policy.assertion.ext.EditListener;

import community.layer7.customassertion.stringSimpleTransform.tranforms.StringTransformTypeWithLabel;
import community.layer7.customassertion.stringSimpleTransform.tranforms.StringTransformer;

/**
 * @author Guy Deffaux
 * June 2018
 */
public class StringSimpleTransformJDialog extends StringSimpleTransformBaseJDialog implements AssertionEditor {

	private static final long serialVersionUID = -1660969099465344485L;
    private static final Logger logger = Logger.getLogger(StringSimpleTransformJDialog.class.getName());
	private StringSimpleTransformCustomAssertion stringSimpleTransformCustomAssertion;
	private AssertionEditorSupport editorSupport;
	
	public StringSimpleTransformJDialog(StringSimpleTransformCustomAssertion customAssertion) {
		stringSimpleTransformCustomAssertion = customAssertion;
		editorSupport = new AssertionEditorSupport(this);
		//set icon of the JDialog
		Image dialogIconImage = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.stringsimpletransform.32.png"));
		if(dialogIconImage != null)
			super.setIconImage(dialogIconImage);
		//register ESC key
		registerESCKey();
		//set intut textfield
		super.getInputVariableTextField().setText(customAssertion.getInputVariable());
		//set output textfield
		super.getOutputVariableTextField().setText(customAssertion.getOutputVariable());
		//set combox
		super.getTransformationTypeComboBox().setModel(new DefaultComboBoxModel(StringTransformer.getSupportedTransformsWithLabelArray()));
		//set tooltip
		super.getTransformationTypeComboBox().setToolTipText(StringTransformer.getSupportedTransformsComboBoxTooltip());
		//by default select 1 item in the list
		String transformationType = customAssertion.getTransformationType();
		if(transformationType == null || transformationType.length() == 0)
			super.getTransformationTypeComboBox().setSelectedItem(0);
		else {
			StringTransformTypeWithLabel typeWithLabel = StringTransformer.getSupportedTransformsWithLabel(customAssertion.getTransformationType());
			if(typeWithLabel == null) {
				//play safe by showing the first transformationType. Then user can change it to a known type
				logger.warning("Unknown transformationType: " + transformationType);
				super.getTransformationTypeComboBox().setSelectedItem(0);
			}
			else
				super.getTransformationTypeComboBox().setSelectedItem(typeWithLabel);
		}
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
		editorSupport.fireCancelled(stringSimpleTransformCustomAssertion);
		dispose();
	}
	
	@Override
	protected void onOK() {
		stringSimpleTransformCustomAssertion.setInputVariable(super.getInputVariableTextField().getText());
		stringSimpleTransformCustomAssertion.setOutputVariable(super.getOutputVariableTextField().getText());
		stringSimpleTransformCustomAssertion.setTransformationType(
				((StringTransformTypeWithLabel)super.getTransformationTypeComboBox().getSelectedItem()).getTransformType());
		editorSupport.fireEditAccepted(stringSimpleTransformCustomAssertion);
		dispose();
	}
}
