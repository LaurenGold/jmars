package edu.asu.jmars.layer.stamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.asu.jmars.layer.stamp.networking.StampLayerNetworking;
import edu.asu.jmars.swing.ColorInterp;
import edu.asu.jmars.swing.ColorMapper;
import edu.asu.jmars.util.Util;
import edu.asu.msff.DataField;
import edu.asu.msff.util.QueryTemplate;
import edu.asu.msff.util.QueryTemplate.Filter;

public class QueryTemplateUI extends JDialog {
	
	private JComboBox<String> categoryBx;
	private JComboBox<String> templateBx;
	private JTextArea descripTA;
	private JTextArea detailsTA;
	private ColorMapper templateColorMapper;
	private JTextField colorExpTF;
	private JTextField orderColTF;
	private JTextField colorMinTF;
	private JTextField colorMaxTF;
	private JComboBox<String> orderDirBx;
	private JButton cancelBtn;
	private JButton applyBtn;
	
	private ArrayList<QueryTemplate> queryTemplates;
	private HashMap<String,ArrayList<String>> categoryToTemplates;
	private HashMap<DataField, JComponent[]> dataMap;
	
	private final String SELECT_CATEGORY_TEXT = "Select Category";
	private final String SELECT_TEMPLATE_TEXT = "Select Template";
	
	private AddLayerDialog parentDialog;
	

	public QueryTemplateUI(AddLayerDialog parentDialog, AddLayerWrapper wrapper) {
		super(parentDialog, true);
		this.parentDialog = parentDialog;
		
		String instrument = wrapper.getInstrument();
		dataMap = wrapper.dataMap;
		
		queryTemplates = StampLayerNetworking.retrieveQueryTemplates(instrument);
		
		Vector<String> categories = new Vector<String>();
		categories.add(SELECT_CATEGORY_TEXT);
		
		categoryToTemplates = new HashMap<String,ArrayList<String>>();
		
		for (QueryTemplate qt : queryTemplates) {
			if (!qt.instrument.equalsIgnoreCase(instrument)) {
				continue;
			}
			
			if (!categories.contains(qt.category)) {
				categories.add(qt.category);
			}
			
			ArrayList<String> templateNames = null;
			
			if (categoryToTemplates.containsKey(qt.category)) {
				templateNames = categoryToTemplates.get(qt.category);
			} else {
				templateNames = new ArrayList<String>();
				templateNames.add(SELECT_TEMPLATE_TEXT);
				categoryToTemplates.put(qt.category, templateNames);
			}
			
			templateNames.add(qt.name);
			
		}
				
		categoryBx = new JComboBox<String>(categories); 
		categoryBx.addActionListener(catListener);
		templateBx = new JComboBox<String>();
		templateBx.addActionListener(templateListener);

		descripTA = new JTextArea();
		descripTA.setBorder(new EmptyBorder(0,5,0,5));
		descripTA.setBackground(Util.panelGrey);
		descripTA.setWrapStyleWord(true);
		descripTA.setLineWrap(true);
		descripTA.setEditable(false);
		JPanel descripPnl = new JPanel();
		descripPnl.setLayout(new GridLayout(1,1));
		descripPnl.setBorder(new TitledBorder("Description"));
		JScrollPane descripSP = new JScrollPane(descripTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		descripSP.setBorder(new EmptyBorder(0,0,0,0));
		descripPnl.add(descripSP);
		
		detailsTA = new JTextArea();
		detailsTA.setBackground(Util.panelGrey);
		detailsTA.setWrapStyleWord(true);
		detailsTA.setLineWrap(true);
		detailsTA.setEditable(false);
		JPanel detailsPnl = new JPanel(new GridLayout(1,1));
		detailsPnl.setBorder(new TitledBorder("Query Settings"));
		JScrollPane detailsSP = new JScrollPane(detailsTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		detailsSP.setBorder(new EmptyBorder(0,0,0,0));
		detailsPnl.add(detailsSP);
		
		templateColorMapper = new ColorMapper();
		templateColorMapper.setEnabled(false);
		
		colorExpTF = new JTextField();
		colorExpTF.setEditable(false);
		orderColTF = new JTextField();
		orderColTF.setEditable(false);
		colorMinTF = new JTextField();
		colorMinTF.setEditable(false);
		colorMaxTF = new JTextField();
		colorMaxTF.setEditable(false);
		
		orderDirBx = new JComboBox<String>(new String[]{"Ascending", "Descending"});
		UIManager.put("ComboBox.disabledForeground", Color.GRAY);
		orderDirBx.setEnabled(false);
		
		cancelBtn = new JButton(cancelAct);
		applyBtn = new JButton(applyAct);
		applyBtn.setEnabled(false);
		
	    int pad = 2;
	    Insets in = new Insets(pad,pad,pad,pad);
		
		JPanel queryPnl = new JPanel(new GridBagLayout());
		queryPnl.setBorder(new TitledBorder("Query Settings"));
		int row = 0;
		queryPnl.add(categoryBx, new GridBagConstraints(0, row, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
	    row++;	
		queryPnl.add(templateBx, new GridBagConstraints(0, row, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
	    row++;	
		queryPnl.add(descripPnl, new GridBagConstraints(0, row, 1, 2, .6, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, in, pad, pad));
		queryPnl.add(detailsPnl, new GridBagConstraints(1, row, 1, 2, .4, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, in, pad, pad));

		JPanel colorPnl = new JPanel(new GridBagLayout());
		colorPnl.setBorder(new TitledBorder("Colorization Settings"));
		row=0;
		colorPnl.add(templateColorMapper, new GridBagConstraints(0, row, 3, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, in, pad, pad));
		row++;
		colorPnl.add(new JLabel("Colorize by:"), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, in, pad, pad));
		colorPnl.add(colorExpTF, new GridBagConstraints(1, row, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
		row++;
		colorPnl.add(new JLabel("Range:"), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, in, pad, pad));
		colorPnl.add(colorMinTF, new GridBagConstraints(1, row, 1, 1, 0.5, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
		colorPnl.add(colorMaxTF, new GridBagConstraints(2, row, 1, 1, 0.5, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
		row++;
		colorPnl.add(new JLabel("Order column:"),new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, in, pad, pad));
		colorPnl.add(orderColTF, new GridBagConstraints(1, row, 1, 1, 0.5, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
		colorPnl.add(orderDirBx, new GridBagConstraints(2, row, 1, 1, 0.5, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, in, pad, pad));
		
		JPanel templatePanel = new JPanel(new GridBagLayout());
		templatePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		row=0;
		templatePanel.add(queryPnl, new GridBagConstraints(0, row, 1, 1, 1, 0.8, GridBagConstraints.CENTER, GridBagConstraints.BOTH, in, pad, pad));
		row++;
		templatePanel.add(colorPnl, new GridBagConstraints(0, row, 1, 1, 1, 0.2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, in, pad, pad));

		JPanel buttonPanel = new JPanel();
		FlowLayout buttonFlow = new FlowLayout(FlowLayout.TRAILING);
		buttonPanel.setBackground(Util.lightBlue);
		buttonPanel.setLayout(buttonFlow);
		buttonPanel.add(applyBtn);
		buttonPanel.add(cancelBtn);

		JPanel mainPnl = new JPanel(new BorderLayout());
		mainPnl.setBackground(Util.lightBlue);
		mainPnl.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPnl.add(templatePanel, BorderLayout.CENTER);
		mainPnl.add(buttonPanel, BorderLayout.SOUTH);
		mainPnl.setPreferredSize(new Dimension(450, 510));
		
		setTitle(wrapper.getInstrument() + " Query Templates");
		setLayout(new GridLayout(1,1));
		add(mainPnl);
		
		pack();
		
		setLocationRelativeTo(parentDialog);		
	}
	
	private void clearFields(){
		descripTA.setText("");
		detailsTA.setText("");
		//reset color mapper to default colors
		templateColorMapper.setColors(new int[] {0, 255}, new Color[] {Color.black, Color.white}); 
		colorExpTF.setText("");
		orderColTF.setText("");
		colorMinTF.setText("");
		colorMaxTF.setText("");
	}
	
	
	private ActionListener catListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// If the user clicks on the select option, ignore it
			if (!categoryBx.getSelectedItem().equals(SELECT_CATEGORY_TEXT)){
				String selCat = categoryBx.getSelectedItem().toString();
				ArrayList<String> names = categoryToTemplates.get(selCat);
				DefaultComboBoxModel model = new DefaultComboBoxModel(names.toArray(new String[0]));
				templateBx.setModel(model);
			}
			//clear fields then return
			clearFields();
			return;
		}
	};
	
	private ActionListener templateListener = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// If the user clicks on the select option, ignore it
			if (templateBx.getSelectedItem().equals(SELECT_TEMPLATE_TEXT)){
				//clear fields then return
				clearFields();
				return;
			}
			
			String selTemp = templateBx.getSelectedItem().toString();
			
			for (QueryTemplate qt : queryTemplates) {
				if (qt.category.equalsIgnoreCase(categoryBx.getSelectedItem().toString()) &&
						qt.name.equalsIgnoreCase(selTemp)) {
					
					//clear the fields to start
					clearFields();

					if (qt.description!=null && qt.description.length()>0) {
						descripTA.setText(qt.description);
						descripTA.setCaretPosition(0);
					}
					
					detailsTA.setText("");
					
					for (Filter filter : qt.filters) {
						String val1 = filter.minVal;
						String val2 = filter.maxVal;
					
						detailsTA.append(filter.fieldName + " " + val1 + " " + val2 + "\n");
					}
					
					detailsTA.setCaretPosition(0);
					
					// Stolen from TES Layer code:
					String valuesStr = qt.colorValues;
					String colorsStr = qt.colorColors;
					String interpKw = qt.colorInterp;

					ColorMapper.State colorMapperState = null;
					ColorInterp colorInterp = ColorInterp.forKeyword(interpKw);
					if (valuesStr != null && colorsStr != null && colorInterp != null){
						int[] values = commaDelimStrToIntArray(valuesStr);
						Color[] colors = commaDelimStrToColorArray(colorsStr);
						colorMapperState = new ColorMapper.State(values, colors, colorInterp);
					}											
					
					//
					
					templateColorMapper.setState(colorMapperState);
					
					if (qt.colorColumn!=null && qt.colorColumn.length()>0) {
						colorExpTF.setText(qt.colorColumn);
					} else {
						colorExpTF.setText("");
					}
					
					colorExpTF.setCaretPosition(0);
					colorExpTF.setToolTipText(colorExpTF.getText());

					if (qt.colorMin!=null && qt.colorMin.length()>0) {
						colorMinTF.setText(qt.colorMin);
					} else {
						colorMinTF.setText("");
					}


					if (qt.colorMax!=null && qt.colorMax.length()>0) {
						colorMaxTF.setText(qt.colorMax);
					} else {
						colorMaxTF.setText("");
					}


					if (qt.orderColumn!=null && qt.orderColumn.length()>0) {
						orderColTF.setText(qt.orderColumn);
						orderColTF.setCaretPosition(0);
					} else {
						orderColTF.setText("");
					}

					if (qt.orderDirection!=null) {
						if (qt.orderDirection) {
							orderDirBx.setSelectedIndex(0);
						} else {
							orderDirBx.setSelectedIndex(1);
						}
					} else {
						orderDirBx.setSelectedIndex(0);
					}

				} else {
					continue;
				}
			}
			applyBtn.setEnabled(true);
		}
	};
	
	
	private AbstractAction cancelAct = new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e) {
			isCancelled=true;
			setVisible(false);
		}
	};
	
	private AbstractAction applyAct = new AbstractAction("Apply") {
		public void actionPerformed(ActionEvent e) {
			String selTemp = templateBx.getSelectedItem().toString();
			
			for (QueryTemplate qt : queryTemplates) {
				if (qt.category.equalsIgnoreCase(categoryBx.getSelectedItem().toString()) &&
						qt.name.equalsIgnoreCase(selTemp)) {
					
					for (Filter filter : qt.filters) {							
						for (DataField df : dataMap.keySet()) {
							if (df.getDisplayName().equalsIgnoreCase(filter.fieldName) 
									|| (df.getFieldName().equalsIgnoreCase("longitude") && filter.fieldName.equalsIgnoreCase("longitude"))) {
								JComponent fields[] = dataMap.get(df);
								
								if (fields[0] != null) {
									if (fields[0] instanceof JTextField) {
										((JTextField)fields[0]).setText(filter.minVal);
									} else if (fields[0] instanceof JComboBox) {
										((JComboBox)fields[0]).setSelectedItem(filter.minVal);
									}
								}

								if (fields.length ==2 && fields[1] != null) {
									if (fields[1] instanceof JTextField) {
										((JTextField)fields[1]).setText(filter.maxVal);
									} else if (fields[1] instanceof JComboBox) {
										((JComboBox)fields[1]).setSelectedItem(filter.maxVal);
									}
								}

							}
						}
					}
					
					if (qt.colorColumn!=null && qt.colorColumn.length()>0) {
						colorExpTF.setText(qt.colorColumn);
					}

					if (qt.colorMin!=null && qt.colorMin.length()>0) {
						colorMinTF.setText(qt.colorMin);
					}

					if (qt.colorMax!=null && qt.colorMax.length()>0) {
						colorMaxTF.setText(qt.colorMax);
					}

					if (qt.orderColumn!=null && qt.orderColumn.length()>0) {
						orderColTF.setText(qt.orderColumn);
					}

					if (qt.orderDirection!=null) {
						if (qt.orderDirection) {
							orderDirBx.setSelectedIndex(0);
						} else {
							orderDirBx.setSelectedIndex(1);
						}
					}
				} else {
					continue;
				}
			}
			
			parentDialog.colorExpression=colorExpTF.getText();
			parentDialog.colorState=templateColorMapper.getState();
			parentDialog.orderColumn=orderColTF.getText();
			parentDialog.orderDirection=orderDirBx.getSelectedIndex()==0;
	
			double colorMinVal = Double.NaN;
			double colorMaxVal = Double.NaN;
			
			try {
				String colorMinStr = colorMinTF.getText();
				if (colorMinStr!=null && colorMinStr.length()>0) {
					colorMinVal = Double.parseDouble(colorMinStr);
				}
	
				String colorMaxStr = colorMaxTF.getText();
				if (colorMaxStr!=null && colorMaxStr.length()>0) {
					colorMaxVal = Double.parseDouble(colorMaxStr);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			parentDialog.colorMin=colorMinVal;
			parentDialog.colorMax=colorMaxVal;
			
			isCancelled=false;
			setVisible(false);
		}
	};
	
	
	boolean isCancelled = true;
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	
	
	private int[] commaDelimStrToIntArray(String str){
    	String[] pcs = str.split(",");
    	int[] out = new int[pcs.length];
    	for(int i=0; i<pcs.length; i++)
    		out[i] = Integer.parseInt(pcs[i]);
    	return out;
    }
	
    private Color[] commaDelimStrToColorArray(String str){
    	String[] pcs = str.split(",");
    	Color[] out = new Color[pcs.length];
    	for(int i=0; i<pcs.length; i++)
    		out[i] = new Color(Integer.parseInt(pcs[i]));
    	return out;
    }

    // REMOVE
    // Override of Dialog setVisible so we can also reset the cancelled flag
	public void setVisible(boolean b) {
		super.setVisible(b);
	}

}
