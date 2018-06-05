package community.layer7.customassertion.stringSimpleTransform;

import java.awt.Image;
import java.awt.Toolkit;

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
	private StringSimpleTransformCustomAssertion stringSimpleTransformCustomAssertion;
	private AssertionEditorSupport editorSupport;
	
	public StringSimpleTransformJDialog(StringSimpleTransformCustomAssertion customAssertion) {
		stringSimpleTransformCustomAssertion = customAssertion;
		editorSupport = new AssertionEditorSupport(this);
		//set icon of the JDialog
		Image dialogIconImage = Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.stringsimpletransform.32.png"));
		if(dialogIconImage != null)
			super.setIconImage(dialogIconImage);
		super.getInputVariableTextField().setText(customAssertion.getInputVariable());
		super.getOutputVariableTextField().setText(customAssertion.getOutputVariable());
		super.getTransformationTypeComboBox().setModel(new DefaultComboBoxModel(StringTransformer.getSupportedTransformsWithLabelArray()));
		super.getTransformationTypeComboBox().setToolTipText(StringTransformer.getSupportedTransformsComboBoxTooltip());
		//by default select 1 item in the list
		if(customAssertion.getTransformationType() == null || customAssertion.getTransformationType().length() == 0)
			super.getTransformationTypeComboBox().setSelectedItem(0);
		else
			super.getTransformationTypeComboBox().setSelectedItem(
					StringTransformer.getSupportedTransformsWithLabel(customAssertion.getTransformationType()));
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
	protected void okClicked() {
		stringSimpleTransformCustomAssertion.setInputVariable(super.getInputVariableTextField().getText());
		stringSimpleTransformCustomAssertion.setOutputVariable(super.getOutputVariableTextField().getText());
		stringSimpleTransformCustomAssertion.setTransformationType(
				((StringTransformTypeWithLabel)super.getTransformationTypeComboBox().getSelectedItem()).getTransformType());
		editorSupport.fireEditAccepted(stringSimpleTransformCustomAssertion);
	}
}
