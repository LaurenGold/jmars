package edu.asu.jmars.util.stable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import edu.asu.jmars.Main;
import edu.asu.jmars.layer.stamp.networking.StampLayerNetworking;
import edu.asu.jmars.swing.STable;
import edu.asu.jmars.swing.TableColumnAdjuster;


/**
 * Allows selecting which columns are displayed in the table.
 */
public class ColumnDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private FilteringColumnModel columnModel;
	private JPanel columnPanel;
	final STable columnTable = new STable();
	
	ArrayList<String> columnNames = new ArrayList<String>();
	ArrayList<Boolean> checkBoxes = new ArrayList<Boolean>();

	public ColumnDialog(JDialog parent, final FilteringColumnModel columnModel, final String saveKey) {
	    super(parent, "Columns", false);
	    initializeDialog(columnModel, saveKey);
	}
	
	public ColumnDialog(Frame parent, final FilteringColumnModel columnModel, final String saveKey) {
		super((Frame) parent, "Columns", false);
		initializeDialog(columnModel, saveKey);
	}
	private void initializeDialog(final FilteringColumnModel columnModel, final String saveKey) {
		this.columnModel = columnModel;
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());	
		
		columnTable.setColumnModel(new DefaultTableColumnModel());

		buildCheckboxes();
		
		columnTable.setUnsortedTableModel(model);
		
		columnTable.setColumnSelectionAllowed(false);
		columnTable.setPreferredScrollableViewportSize(new Dimension(getPreferredSize().width,400));

		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mousePressed(MouseEvent e){
				int row = columnTable.rowAtPoint(e.getPoint());
				
				int tableRow = columnTable.getSorter().unsortRow(row);
				
	    		String columnNameToMatch = columnNames.get(tableRow);
	    		
	    		for (final TableColumn column: columnModel.getAllColumns()) {
	    			String name = column.getHeaderValue().toString();
	    			if (name.equalsIgnoreCase(columnNameToMatch)) {
	    				boolean visible = (null != columnModel.getVisColumn(column.getIdentifier()));
	    				columnModel.setVisible(column, !visible);
	    				validate();
	    				paint (getGraphics());
	    				return;
	    			}
	    		}
			}
		};
		
		columnTable.addMouseListener(mouseAdapter);

		
		TableColumnAdjuster tca = new TableColumnAdjuster(columnTable);
		tca.adjustColumns();
		
		JScrollPane scrollPane = new JScrollPane(columnTable);		
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(250,400));	

		// Set up the button panel at the bottom of the panel.
		JPanel buttonPanel = new JPanel();
		JButton allButton = new JButton(new AbstractAction("Show All".toUpperCase()) {
			public void actionPerformed(ActionEvent e) {
				columnModel.setAllVisible(true);
				validate();
				//paint (getGraphics());
			}
		});

		JButton nothingButton = new JButton(new AbstractAction("Hide All".toUpperCase()) {
			public void actionPerformed(ActionEvent e) {
				columnModel.setAllVisible(false);
				validate();
				//paint (getGraphics());
			}
		});

		final JCheckBox persistChoices = new JCheckBox("Set selection as defaults", false);
			
		JButton defaultsButton = new JButton(new AbstractAction("Reset Defaults".toUpperCase()) {
			public void actionPerformed(ActionEvent e) {
				String defaultCols[]=columnModel.getDefaultColumns();
				for (final TableColumn column: columnModel.getAllColumns()) {
					boolean showColumn = false;
					for (String columnName : defaultCols) {
						if (columnName.equalsIgnoreCase(column.getHeaderValue().toString())) {
							showColumn = true;
							break;
						}
					}
					columnModel.setVisible(column, showColumn);
				}
				validate();
				//paint (getGraphics());
			}			
		});

		JButton okButton = new JButton(new AbstractAction("OK".toUpperCase()) {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
				if (persistChoices.isSelected()) {
					Enumeration<TableColumn> colEnum=columnModel.getColumns();
					
					String cols[] = new String[columnModel.getColumnCount()];
					
					for (int i=0; i<cols.length; i++) {
						cols[i]=colEnum.nextElement().getHeaderValue().toString();
					}
					
					StampLayerNetworking.setDefaultColumns(saveKey, cols);
				}
			}
		});

		buttonPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		buttonPanel.add(allButton, gbc);
		gbc.gridx = 1;
		buttonPanel.add(nothingButton, gbc);
		
		if (columnModel.getDefaultColumns()!=null) {
			gbc.gridx = 2;
			buttonPanel.add(defaultsButton, gbc);
		}
		
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.insets.top = 0;
		buttonPanel.add(okButton, gbc);
		
		if (saveKey!=null && saveKey.length()>0) {
			gbc.gridy = 2;
			gbc.gridx = 0;
			gbc.gridwidth = 2;
		
			buttonPanel.add(persistChoices, gbc);
			
			if (Main.USER!=null&&Main.USER.length()>0) {
				persistChoices.setEnabled(true);
			} else {
				persistChoices.setEnabled(false);
				persistChoices.setToolTipText("You must be logged in to use this feature");
			}

		}
		
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	public void buildCheckboxes() {
		columnNames.clear();
		checkBoxes.clear();
		
		for (final TableColumn column: columnModel.getAllColumns()) {
			String name = column.getHeaderValue().toString();
			boolean visible = (null != columnModel.getVisColumn(column.getIdentifier()));
			columnNames.add(name);
			checkBoxes.add(new Boolean(visible));			
		}
		validate();
		paint (getGraphics());
	}	
	
	TableModel model = new AbstractTableModel() {
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex==1) {
				return columnNames.get(rowIndex);
			} else if (columnIndex==0) {
	    		String columnNameToMatch = columnNames.get(rowIndex);
	    		
	    		for (final TableColumn column: columnModel.getAllColumns()) {
	    			String name = column.getHeaderValue().toString();
	    			if (name.equalsIgnoreCase(columnNameToMatch)) {
	    				boolean visible = (null != columnModel.getVisColumn(column.getIdentifier()));
	    				return visible;
	}
}
	    		return false;
			} else if (columnIndex==2) {
				if (columnModel.getDefaultColumns()==null) return false;
				for (String name : columnModel.getDefaultColumns()) {
					if (columnNames.get(rowIndex).equalsIgnoreCase(name)) {
						return true;
					}
				}
				
				return false;
			} else {
				return "";
			}
			
		}
		
		@Override
		public int getRowCount() {
			return columnNames.size();
		}
		
		@Override
		public int getColumnCount() {
			return 2;  // Changing this to 3 will show default columns.  Current consensus is this is not desired.
		}		
		public String getColumnName(int column) {
			switch (column) {
				case 0: return "Visible";
				case 1: return "Column Name";
				case 2: return "Default?";
				default:
					return "";  // shouldn't happen
			}
		}
		
	    public Class<?> getColumnClass(int columnIndex) {
	    	switch (columnIndex) {
		    	case 0: return Boolean.class;
		    	case 1: return String.class;
		    	case 2: return Boolean.class;
	    	}
	    	
	    	return String.class;
		}
	};
}
