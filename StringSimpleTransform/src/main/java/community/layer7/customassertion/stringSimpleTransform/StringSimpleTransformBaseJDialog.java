package community.layer7.customassertion.stringSimpleTransform;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import community.layer7.customassertion.stringSimpleTransform.tranforms.StringTransformer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Guy Deffaux
 * June 2018
 */
public class StringSimpleTransformBaseJDialog extends JDialog {

	private static final long serialVersionUID = -3490995866655648890L;
	private final JPanel contentPanel = new JPanel();
	private JTextField inputVariableTextField;
	private JTextField outputVariableTextField;
	private JComboBox transformationTypeComboBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			StringSimpleTransformBaseJDialog dialog = new StringSimpleTransformBaseJDialog();
			//additional lines added for test
			dialog.getTransformationTypeComboBox().setModel(new DefaultComboBoxModel(StringTransformer.getSupportedTransformsWithLabelArray()));
			dialog.getTransformationTypeComboBox().setToolTipText(StringTransformer.getSupportedTransformsComboBoxTooltip());
			//prepare then open dialog
			dialog.registerESCKey();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public StringSimpleTransformBaseJDialog() {
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setTitle("String Simple Transform Properties - v180614");
		setBounds(100, 100, 400, 213);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0, 208, 0};
		gbl_contentPanel.rowHeights = new int[] {40, 40, 40, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblNewLabel = new JLabel("Input Variable:");
			lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			contentPanel.add(lblNewLabel, gbc_lblNewLabel);
		}
		{
			inputVariableTextField = new JTextField();
			GridBagConstraints gbc_inputVariableTextField = new GridBagConstraints();
			gbc_inputVariableTextField.weightx = 1.0;
			gbc_inputVariableTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_inputVariableTextField.insets = new Insets(0, 0, 5, 0);
			gbc_inputVariableTextField.gridx = 1;
			gbc_inputVariableTextField.gridy = 0;
			contentPanel.add(inputVariableTextField, gbc_inputVariableTextField);
			inputVariableTextField.setColumns(10);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("Output Variable:");
			lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			contentPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		}
		{
			outputVariableTextField = new JTextField();
			GridBagConstraints gbc_outputVariableTextField = new GridBagConstraints();
			gbc_outputVariableTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_outputVariableTextField.insets = new Insets(0, 0, 5, 0);
			gbc_outputVariableTextField.gridx = 1;
			gbc_outputVariableTextField.gridy = 1;
			contentPanel.add(outputVariableTextField, gbc_outputVariableTextField);
			outputVariableTextField.setColumns(10);
		}
		{
			JLabel lblNewLabel_2 = new JLabel("Transformation Type:");
			lblNewLabel_2.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 2;
			contentPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		}
		{
			transformationTypeComboBox = new JComboBox();
			transformationTypeComboBox.setModel(new DefaultComboBoxModel(new String[] {"val1", "val2"}));
			GridBagConstraints gbc_transformationTypeComboBox = new GridBagConstraints();
			gbc_transformationTypeComboBox.anchor = GridBagConstraints.WEST;
			gbc_transformationTypeComboBox.gridx = 1;
			gbc_transformationTypeComboBox.gridy = 2;
			contentPanel.add(transformationTypeComboBox, gbc_transformationTypeComboBox);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
						StringSimpleTransformBaseJDialog.this.onOK();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent arg0) {
						StringSimpleTransformBaseJDialog.this.onCancel();
						StringSimpleTransformBaseJDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	protected JTextField getInputVariableTextField() {
		return inputVariableTextField;
	}
	protected JTextField getOutputVariableTextField() {
		return outputVariableTextField;
	}
	protected JComboBox getTransformationTypeComboBox() {
		return transformationTypeComboBox;
	}
	
	protected void onCancel() {
		dispose();		
	}
	
	protected void onOK() {
		dispose();
	}
	
	protected void registerESCKey() {
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        actionMap.put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 6433143544659374034L;
			public void actionPerformed(ActionEvent e) {
            	onCancel();
            }
        });
    }
}
