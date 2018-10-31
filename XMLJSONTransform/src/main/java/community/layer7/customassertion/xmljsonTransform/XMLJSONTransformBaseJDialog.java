package community.layer7.customassertion.xmljsonTransform;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.json.JSONObject;

import community.layer7.customassertion.xmljsonTransform.transforms2.JSONSchemaForXML;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

/**
 * @author Guy D.
 * October 2018
 */
public class XMLJSONTransformBaseJDialog extends JDialog {

	private static final long serialVersionUID = -4991258331186771701L;
	private JTextField outputVariableNameJTextField;
	private JTextField inputVariableNameJTextField;
	private JComboBox transformationJComboBox;
	private JCheckBox formatOutputJCheckBox;
	private RSyntaxTextArea schemaTextArea;
	private RSyntaxTextArea testInputJTextArea;
	private RSyntaxTextArea testOutputJTextArea;
	private JLabel testErrorJLabel;

	public static void main(String[] args) {
		try {
			XMLJSONTransformBaseJDialog dialog = new XMLJSONTransformBaseJDialog();
			//more lines for init
			dialog.setTestJTextAreaStyles(TransformationHelper.XML_TO_JSON_TRANSFORMATION_ID);
			//additional lines added for test
			dialog.getTransformationJComboBox().setModel(new DefaultComboBoxModel(TransformationHelper.getSupportedTransformations()));			
			//prepare then open dialog
			dialog.registerESCKey();
			dialog.setVisible(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void setTestJTextAreaStyles(int direction) {
		if(direction == TransformationHelper.JSON_TO_XML_TRANSFORMATION_ID) {
			getTestInputJTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
			getTestOutputJTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		}
		else {
			getTestInputJTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
			getTestOutputJTextArea().setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
			
		}
	}
	
	public XMLJSONTransformBaseJDialog() {
		setTitle("XML / JSON Transform Properties - v0.8.0");
		setModal(true);
		setBounds(100, 100, 872, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(0, 4, 4, 2));
		
		JLabel lblNewLabel = new JLabel("Transformation :");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(lblNewLabel);
		
		transformationJComboBox = new JComboBox();
		transformationJComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				setTestJTextAreaStyles(getTransformationJComboBox().getSelectedIndex());
			}
		});
		transformationJComboBox.setModel(new DefaultComboBoxModel(new String[] {"XML to JSON"}));
		panel_1.add(transformationJComboBox);
		
		JLabel lblInputVariable = new JLabel("Input Variable Name :");
		lblInputVariable.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(lblInputVariable);
		
		inputVariableNameJTextField = new JTextField();
		panel_1.add(inputVariableNameJTextField);
		inputVariableNameJTextField.setColumns(15);
		
		JLabel lblNewLabel_1 = new JLabel("Format Output :");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(lblNewLabel_1);
		
		formatOutputJCheckBox = new JCheckBox("");
		panel_1.add(formatOutputJCheckBox);
		
		JLabel lblOutputVariableName = new JLabel("Output Variable Name :");
		lblOutputVariableName.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_1.add(lblOutputVariableName);
		
		outputVariableNameJTextField = new JTextField();
		panel_1.add(outputVariableNameJTextField);
		outputVariableNameJTextField.setColumns(10);
		
		JPanel okCancelJPanel = new JPanel();
		FlowLayout fl_okCancelJPanel = (FlowLayout) okCancelJPanel.getLayout();
		fl_okCancelJPanel.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(okCancelJPanel, BorderLayout.SOUTH);
		
		JButton cancelJButton = new JButton("Cancel");
		cancelJButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				XMLJSONTransformBaseJDialog.this.onCancel();
			}
		});
		
		JButton okJButton = new JButton("OK");
		okJButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				XMLJSONTransformBaseJDialog.this.onOK();
			}
		});
		okCancelJPanel.add(okJButton);
		okCancelJPanel.add(cancelJButton);
		
		JPanel panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.CENTER);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] {1};
		gbl_panel_3.rowHeights = new int[] {1, 1, 1};
		gbl_panel_3.columnWeights = new double[]{1.0};
		gbl_panel_3.rowWeights = new double[]{1.0, 2.0, 0.0};
		panel_3.setLayout(gbl_panel_3);
		
		JPanel testJPanel = new JPanel();
		GridBagConstraints gbc_testJPanel = new GridBagConstraints();
		gbc_testJPanel.weighty = 1.0;
		gbc_testJPanel.weightx = 1.0;
		gbc_testJPanel.insets = new Insets(0, 5, 0, 5);
		gbc_testJPanel.fill = GridBagConstraints.BOTH;
		gbc_testJPanel.gridx = 0;
		gbc_testJPanel.gridy = 1;
		panel_3.add(testJPanel, gbc_testJPanel);
		testJPanel.setLayout(new GridLayout(1, 2, 0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Input :", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		testJPanel.add(scrollPane);
		
		testInputJTextArea = new RSyntaxTextArea();
		testInputJTextArea.setHighlightCurrentLine(false);
		testInputJTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		scrollPane.setViewportView(testInputJTextArea);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Test Output:", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		testJPanel.add(scrollPane_2);
		
		testOutputJTextArea = new RSyntaxTextArea();
		testOutputJTextArea.setHighlightCurrentLine(false);
		testOutputJTextArea.setEditable(false);
		testOutputJTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		scrollPane_2.setViewportView(testOutputJTextArea);
		
		JScrollPane schemaJPanel = new JScrollPane();
		schemaJPanel.setViewportBorder(new TitledBorder(null, "JSON Schema : Configure in advance", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_schemaJPanel = new GridBagConstraints();
		gbc_schemaJPanel.weighty = 3.0;
		gbc_schemaJPanel.insets = new Insets(0, 5, 5, 5);
		gbc_schemaJPanel.weightx = 1.0;
		gbc_schemaJPanel.fill = GridBagConstraints.BOTH;
		gbc_schemaJPanel.gridx = 0;
		gbc_schemaJPanel.gridy = 0;
		panel_3.add(schemaJPanel, gbc_schemaJPanel);
		
		schemaTextArea = new RSyntaxTextArea();
		schemaTextArea.setHighlightCurrentLine(false);
		schemaTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		schemaTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		schemaJPanel.setViewportView(schemaTextArea);
		
		JPanel testButtonJPlanel = new JPanel();
		GridBagConstraints gbc_testButtonJPlanel = new GridBagConstraints();
		gbc_testButtonJPlanel.insets = new Insets(0, 5, 0, 0);
		gbc_testButtonJPlanel.anchor = GridBagConstraints.WEST;
		gbc_testButtonJPlanel.weightx = 1.0;
		gbc_testButtonJPlanel.gridx = 0;
		gbc_testButtonJPlanel.gridy = 2;
		panel_3.add(testButtonJPlanel, gbc_testButtonJPlanel);
		
		JButton testJButton = new JButton("Test");
		testJButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				XMLJSONTransformBaseJDialog.this.onTest();
			}
		});
		testButtonJPlanel.add(testJButton);
		
		testErrorJLabel = new JLabel("");
		testErrorJLabel.setForeground(Color.RED);
		testButtonJPlanel.add(testErrorJLabel);		
	}
	
	protected JTextField getInputVariableNameJTextField() {
		return inputVariableNameJTextField;
	}
	
	protected JTextField getOutputVariableNameJTextField() {
		return outputVariableNameJTextField;
	}
	
	protected JComboBox getTransformationJComboBox() {
		return transformationJComboBox;
	}

	protected JCheckBox getFormatOutputJCheckBox() {
		return formatOutputJCheckBox;
	}

	protected RSyntaxTextArea getSchemaTextArea() {
		return schemaTextArea;
	}
	
	protected RSyntaxTextArea getTestInputJTextArea() {
		return testInputJTextArea;
	}
	
	protected RSyntaxTextArea getTestOutputJTextArea() {
		return testOutputJTextArea;
	}

	protected JLabel getTestErrorJLabel() {
		return testErrorJLabel;
	}

	protected void onCancel() {
		dispose();		
	}
	
	protected void onOK() {
		dispose();
	}
	
	protected void onTest() {
		//clear error and output areas first
		getTestErrorJLabel().setText("");
		getTestOutputJTextArea().setText("");
		try {
			//parse JSON schema
			String jsonSchemaString = getSchemaTextArea().getText();
			JSONSchemaForXML jsonSchemaForXML = new JSONSchemaForXML(jsonSchemaString);
			//apply the right transformation
			String intputXML = getTestInputJTextArea().getText();
			JSONObject o = jsonSchemaForXML.mapXMLToJSON(intputXML);
			//set output area
			getTestOutputJTextArea().setText(o.toString(getFormatOutputJCheckBox().isSelected() ? 2 : 0));
		}
		catch(Exception e) {
			String msg = e.toString();
			msg = msg.replace('\t', '-').replace('\n', '-').replace('\r','-');
			getTestErrorJLabel().setText(msg);
		}
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
