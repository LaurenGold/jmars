package edu.asu.jmars.layer.krc;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import edu.asu.jmars.Main;
import edu.asu.jmars.layer.FocusPanel;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.swing.ColorCombo;
import edu.asu.jmars.swing.ExportTableAction;
import edu.asu.jmars.swing.TableColumnAdjuster;
import edu.asu.jmars.ui.looknfeel.ThemeFont;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeChart;
import edu.asu.jmars.util.Config;
import edu.asu.jmars.util.HttpRequestType;
import edu.asu.jmars.util.JmarsHttpRequest;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.ThreeDManager;

@SuppressWarnings("serial")
public class KRCFocusPanel extends FocusPanel{

	private JTable inputDataTbl;
	private JPanel inputTablePnl;
	private JTable resultsDataTbl;
	private JPanel resultsTablePnl;
	private JTable dayROTbl;
	private JTable yearROTbl;
	private JScrollPane dayROSP;
	private JScrollPane yearROSP;
	private JPanel dayROPnl;
	private JPanel yearROPnl;
	private JButton addPointBtn;
	private JLabel noDataLbl1;
	private JLabel noDataLbl2;
	private ChartPanel dayCPnl;
	private ChartPanel yearCPnl;
	private JFreeChart dayChart;
	private JFreeChart yearChart;
	private Crosshair dayCH;
	private Crosshair yearCH;
	private JScrollPane inputTableSP;
	private JScrollPane resultsTableSP;
	private JButton setInputBtn;
	private JButton createFromInputBtn;
	private JButton runKrcBtn;
	private JTextField lsTf;
	private JTextField hourTf;
	private JTextField elevTf;
	private JTextField albedoTf;
	private JTextField tiTf;
	private JTextField opacityTf;
	private JTextField slopeTf;
	private JTextField azimuthTf;
	private JLabel lsLbl;
	private JLabel hourLbl;
	private JLabel elevLbl;
	private JLabel albedoLbl;
	private JLabel tiLbl;
	private JLabel opacityLbl;
	private JLabel slopeLbl;
	private JLabel azimuthLbl;
	private JTabbedPane outputTp;
	private JCheckBox pointChk;
	private JCheckBox labelChk;
	private ColorCombo outlineCc;
	private ColorCombo fillCc;
	private ColorCombo labelCc;
	private JComboBox<Integer> sizeBx;
	private JLabel outlineLbl;
	private JLabel fillLbl;
	private JLabel sizeLbl;
	private JLabel fontLbl;
	
	private ArrayList<String> statFailures = new ArrayList<String>();
	
	private final String YEAR_MODEL = "season";
	private final String DAY_MODEL = "hour";
	
	//UI constants
	private int row = 0;
	private int col = 0;
	private int pad = 2;
	private Insets in = new Insets(pad,pad,pad,pad);	
	
	private int textFieldSize = 8;
	private Dimension inputTableDim = new Dimension(100,200);
	private Dimension resultsTableDim = new Dimension(100,220);
	private DecimalFormat xFormat = new DecimalFormat("0");
	private Range dayRange = new Range(0,24);
	private Range yearRange = new Range(0,360);
	private NumberTickUnit dayStep = new NumberTickUnit(2);
	private NumberTickUnit yearStep = new NumberTickUnit(45);
	
	/**
	 * The selected KRC datapoint from the input data table. Is null if no
	 * selection is made
	 */
	private KRCDataPoint selectedDataPoint;
	
	private KRCLView myLView;
	private KRCLayer myLayer;
	
	/**
	 * Create the Focus panel for the KRC Layer.  Currently
	 * has a "Data" tab to display all the information for 
	 * the layer
	 * @param parent
	 */
	public KRCFocusPanel(LView parent) {
		super(parent, false);
		myLView = (KRCLView) parent;
		myLayer = (KRCLayer) myLView.getLayer();
		
		add("Data", createDataPanel());
		add("Results", createResultsPanel());
	}
	
	private JPanel createDataPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Util.lightBlue);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//data table panel
		inputTablePnl = new JPanel(new BorderLayout());
		inputTablePnl.setBorder(new TitledBorder("Data Points"));
		inputDataTbl = createDataTable(true);
		inputTableSP = new JScrollPane(inputDataTbl);
		addPointBtn = new JButton(addPointAct);
		JPanel addPnl = new JPanel();
		addPnl.add(addPointBtn);
		inputTablePnl.add(inputTableSP, BorderLayout.CENTER);
		inputTablePnl.add(addPnl, BorderLayout.SOUTH);
		inputTablePnl.setPreferredSize(inputTableDim);
		inputTablePnl.setMinimumSize(inputTableDim);
		
		//display panel
		JPanel dispPnl = new JPanel(new GridBagLayout());
		dispPnl.setBorder(new TitledBorder("Display Options"));
		pointChk = new JCheckBox(checkboxAct);
		pointChk.setText("Point");
		outlineLbl = new JLabel("Outline:");
		Dimension boxDim = new Dimension(50,20);
		outlineCc = new ColorCombo(Color.BLACK);
		outlineCc.setPreferredSize(boxDim);
		outlineCc.addActionListener(boxListener);
		fillLbl = new JLabel("Fill:");
		fillCc = new ColorCombo(Color.WHITE);
		fillCc.setPreferredSize(boxDim);
		fillCc.addActionListener(boxListener);
		labelChk = new JCheckBox(checkboxAct);
		labelChk.setText("Label");
		sizeLbl = new JLabel("Size:");
		Vector<Integer> sizes = new Vector<Integer>();
		sizes.add(10);
		sizes.add(11);
		sizes.add(12);
		sizes.add(13);
		sizes.add(14);
		sizes.add(15);
		sizes.add(16);
		sizes.add(17);
		sizes.add(18);
		sizeBx = new JComboBox<Integer>(sizes);
		sizeBx.setPreferredSize(boxDim);
		sizeBx.addActionListener(boxListener);
		fontLbl = new JLabel("Fill:");
		labelCc = new ColorCombo(Color.WHITE);
		labelCc.setPreferredSize(boxDim);
		labelCc.addActionListener(boxListener);

		row = 0; col = -1;
		dispPnl.add(pointChk, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(outlineLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(outlineCc, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(fillLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(fillCc, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(Box.createHorizontalStrut(5), new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, in, pad, pad));
		dispPnl.add(labelChk, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(sizeLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(sizeBx, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(fontLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		dispPnl.add(labelCc, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, in, pad, pad));
		
		
		//input panel
		//TODO: make input text fields have a document filter 
		// allowing valid numbers to be entered
		JPanel inputPnl = new JPanel(new GridBagLayout());
		inputPnl.setBorder(new TitledBorder("Inputs"));
		lsLbl = new JLabel("Ls:");
		lsTf = new JTextField(textFieldSize);
		String lsHint = "Range: 0-360";
		lsLbl.setToolTipText(lsHint);
		lsTf.setToolTipText(lsHint);
		hourLbl = new JLabel("Hour:");
		hourTf = new JTextField(textFieldSize);
		String hourHint = "Range: 0-24";
		hourLbl.setToolTipText(hourHint);
		hourTf.setToolTipText(hourHint);
		elevLbl = new JLabel("Elevation (km):");
		elevTf = new JTextField(textFieldSize);
		albedoLbl = new JLabel("Albedo:");
		albedoTf = new JTextField(textFieldSize);
		tiLbl = new JLabel("Thermal Inertia:");
		tiTf = new JTextField(textFieldSize);
		opacityLbl = new JLabel("Opacity:");
		opacityTf = new JTextField(textFieldSize);
		slopeLbl = new JLabel("Slope:");
		slopeTf = new JTextField(textFieldSize);
		azimuthLbl = new JLabel("Azimuth:");
		azimuthTf = new JTextField(textFieldSize);

		JPanel buttonPnl = new JPanel();
		setInputBtn = new JButton(setInputsAct);
		createFromInputBtn = new JButton(createFromInputsAct);
		buttonPnl.add(setInputBtn);
		buttonPnl.add(Box.createHorizontalStrut(5));
		buttonPnl.add(createFromInputBtn);
		
		//disable all input fields because a row has not been selected
		refreshInputs();
		
		row = 0; col = -1;
		inputPnl.add(lsLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(lsTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		inputPnl.add(hourLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(hourTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		inputPnl.add(elevLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(elevTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		row++; col = -1;
		inputPnl.add(albedoLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(albedoTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		inputPnl.add(slopeLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(slopeTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		inputPnl.add(opacityLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(opacityTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		row++; col = -1;
		inputPnl.add(tiLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(tiTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		inputPnl.add(azimuthLbl, new GridBagConstraints(++col, row, 1, 1, 0, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, in, pad, pad));
		inputPnl.add(azimuthTf, new GridBagConstraints(++col, row, 1, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, in, pad, pad));
		row++; col = -1;
		inputPnl.add(buttonPnl, new GridBagConstraints(++col, row, 8, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, in, pad, pad));
		
		//disable all display options until a row is selected
		refreshDisplayOptions();
		
		//add everything to display
		JPanel cenPnl = new JPanel(new GridBagLayout());
		cenPnl.setBackground(Util.lightBlue);
		row = 0;
		pad = 2;
		cenPnl.add(inputPnl, new GridBagConstraints(0, row, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, in, pad, pad));
		cenPnl.add(Box.createVerticalStrut(5),new GridBagConstraints(0, ++row, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, in, pad, pad));
		cenPnl.add(dispPnl, new GridBagConstraints(0, ++row, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, in, pad, pad));

		panel.add(inputTablePnl, BorderLayout.NORTH);
		panel.add(cenPnl, BorderLayout.CENTER);

		//TODO: limit minimum size of the panel...
//		panel.setMinimumSize(new Dimension(200,0));
		
		return panel;
	}
	
	private JPanel createResultsPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Util.lightBlue);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setPreferredSize(new Dimension(0, 700));
		
		//data table panel
		resultsTablePnl = new JPanel(new BorderLayout());
		resultsTablePnl.setBorder(new TitledBorder("Data Points"));
		resultsDataTbl = createDataTable(false);
		resultsTableSP = new JScrollPane(resultsDataTbl);
		runKrcBtn = new JButton(runAct);
		runKrcBtn.setEnabled(false);
		JPanel runPnl = new JPanel();
		runPnl.add(runKrcBtn);
		
		resultsTablePnl.add(resultsTableSP, BorderLayout.CENTER);
		resultsTablePnl.add(runPnl, BorderLayout.SOUTH);
		resultsTablePnl.setPreferredSize(resultsTableDim);
		resultsTablePnl.setMinimumSize(resultsTableDim);
		
		//output charts
		dayChart = null;
		yearChart = null;
		dayCPnl = new ChartPanel(dayChart, true);
		yearCPnl = new ChartPanel(yearChart, true);
		//add chart listener (to update the readout tables)
		dayCPnl.addChartMouseListener(dayChartListener);
		yearCPnl.addChartMouseListener(yearChartListener);
		//add crosshair overlay, so the chart doesn't need to be redrawn
		// when the crosshair moves
	    CrosshairOverlay dayOverlay = new CrosshairOverlay();
	    dayCH = new Crosshair(0, ThemeChart.getIndicatorColor(), new BasicStroke(1.0f));
	    dayOverlay.addDomainCrosshair(dayCH);
	    dayCPnl.addOverlay(dayOverlay);
	    CrosshairOverlay yearOverlay = new CrosshairOverlay();
	    yearCH = new Crosshair(0, ThemeChart.getIndicatorColor(), new BasicStroke(1.0f));
	    yearOverlay.addDomainCrosshair(yearCH);
	    yearCPnl.addOverlay(yearOverlay);
	    
		//add csv export option to chart panels
		dayCPnl.getPopupMenu().add(createCSVMenuItem(true));
		yearCPnl.getPopupMenu().add(createCSVMenuItem(false));
		String noDataStr = "No Data Available.  Please run KRC.";
		noDataLbl1 = new JLabel(noDataStr);
		noDataLbl2 = new JLabel(noDataStr);
		
		//add the readout tables to the day and year panels
		//day panel
		JPanel dayPnl = new JPanel(new BorderLayout());
		dayROTbl = createReadoutTable(true);
		dayROSP = new JScrollPane(dayROTbl);

		//stick the scroll pane in a panel with a fixed size
		dayROPnl = new JPanel(new GridLayout(1,1));
		Dimension roSize = new Dimension(0,150);
		dayROPnl.setMinimumSize(roSize);
		dayROPnl.setPreferredSize(roSize);
		dayROPnl.add(dayROSP);

		//populate the day panel
		dayPnl.add(dayCPnl, BorderLayout.CENTER);
		dayPnl.add(dayROPnl, BorderLayout.SOUTH);

		//year panel
		JPanel yearPnl = new JPanel(new BorderLayout());
		yearROTbl = createReadoutTable(false);
		yearROSP = new JScrollPane(yearROTbl);

		//stick the scroll pane in a panel with a fixed size
		yearROPnl = new JPanel(new GridLayout(1,1));
		yearROPnl.setMinimumSize(roSize);
		yearROPnl.setPreferredSize(roSize);
		yearROPnl.add(yearROSP);

		//populate the year panel
		yearPnl.add(yearCPnl, BorderLayout.CENTER);
		yearPnl.add(yearROPnl, BorderLayout.SOUTH);

		//output tabbed pane
		outputTp = new JTabbedPane(JTabbedPane.TOP);
		outputTp.setPreferredSize(new Dimension(100,250));
		outputTp.add("Day".toUpperCase(), dayPnl);
		outputTp.add("Year".toUpperCase(), yearPnl);

		//refresh charts
		refreshCharts();
		
		//add everything for display
		panel.add(resultsTablePnl, BorderLayout.NORTH);
		panel.add(outputTp, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JMenuItem createCSVMenuItem(final boolean day){
		JMenuItem item = new JMenuItem("Save as CSV");
		
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileFilter() {
					public String getDescription() {
						return "Text Files";
					}
					
					@Override
					public boolean accept(File f) {
						String name = f.getName();
						if(name.contains(".csv") || name.contains(".txt")){
							return true;
						}
						return false;
					}
				});
				
				int result = fileChooser.showSaveDialog(KRCFocusPanel.this);
				if(result == JFileChooser.APPROVE_OPTION){
					boolean succeed = true;
					String error = "";
					File file = fileChooser.getSelectedFile();
					//if the name doesn't have the extension, add it
					if(!file.getName().contains(".csv")){
						String path = file.getPath();
						file = new File(path+".csv");
					}
					//if it exists, confirm the user wants to overwrite before saving
					if (!file.exists() ||
							JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
								fileChooser, "File exists, overwrite?", "File already exists",
								JOptionPane.YES_NO_OPTION)) {
						try {
							saveAsText(file, day);
						} catch(Exception ex) {
							succeed = false;
							ex.printStackTrace();
							error = ex.getMessage();
						}
					}
					//if the save worked, notify the user, if it didn't notify the user
					if(succeed){
						JOptionPane.showMessageDialog(fileChooser, "CSV file exported successfully!", "Save Success", JOptionPane.PLAIN_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(fileChooser, "Unable to save file: "+error, "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		return item;
	}
	
	private void saveAsText(File outputFile, boolean day) throws FileNotFoundException{
		JFreeChart chart;
		String xAxis;
		if(day){
			chart = dayChart;
			xAxis = "Hour";
		}else{
			chart = yearChart;
			xAxis = "Ls";
		}
		XYDataset dataset = chart.getXYPlot().getDataset(0);
		
		String delim = ",";
		PrintStream ps = new PrintStream(outputFile);
		//Header
		ps.print(xAxis);
		for(int i=0; i<dataset.getSeriesCount(); i++){
			ps.print(delim+dataset.getSeriesKey(i));
		}
		ps.println();
		
		//Data
		//cycle through all values of x
		for(int j=0; j<dataset.getItemCount(0); j++){
			//print the x value
			ps.print(dataset.getX(0, j));
			//print the y value of each plot at that x
			for(int i=0; i<dataset.getSeriesCount(); i++){
				ps.print(delim + dataset.getYValue(i, j));
			}
			ps.println();
		}
		
		ps.close();
	}
	
	
	private AbstractAction addPointAct = new AbstractAction("Create New Data Point...".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			new AddDataPointDialog(KRCFocusPanel.this.getFrame(), myLView, "", null, null, null, false);
		}
	};
	
	private AbstractAction checkboxAct = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			int imageState = myLayer.getStateId(KRCLayer.IMAGES_BUFFER);
			int labelState = myLayer.getStateId(KRCLayer.LABELS_BUFFER);

			if(e.getSource() == pointChk){
				//only set boolean and trigger state change if different
				boolean sel = pointChk.isSelected();
				if(sel != selectedDataPoint.showPoint()){
					selectedDataPoint.setShowPoint(sel);
					//update proper state id buffer
					myLayer.increaseStateId(KRCLayer.IMAGES_BUFFER);
				}
			}
			if(e.getSource() == labelChk){
				//only set boolean and trigger state change if different
				boolean sel = labelChk.isSelected();
				if(sel != selectedDataPoint.showLabel()){
					selectedDataPoint.setShowLabel(sel);
					//update proper state id buffer
					myLayer.increaseStateId(KRCLayer.LABELS_BUFFER);
				}
			}
			//update the display options
			refreshDisplayOptions();
			//refresh lview
			parent.repaint();
			
			//update 3d if something changed
			if(myLayer.getStateId(KRCLayer.IMAGES_BUFFER)!=imageState || myLayer.getStateId(KRCLayer.LABELS_BUFFER)!=labelState){
				if(ThreeDManager.getInstance().isReady()){
					ThreeDManager.getInstance().updateDecalsForLView(myLView, true);
				}	
			}
		}
	};
	
	private ActionListener boxListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			int imageState = myLayer.getStateId(KRCLayer.IMAGES_BUFFER);
			int labelState = myLayer.getStateId(KRCLayer.LABELS_BUFFER);
			//outline color
			if(e.getSource() == outlineCc){
				//only set the color and trigger state change if it's different
				Color c = outlineCc.getColor();
				if(c != selectedDataPoint.getOutlineColor()){
					selectedDataPoint.setOutlineColor(c);
					//update proper state id buffer
					myLayer.increaseStateId(KRCLayer.IMAGES_BUFFER);
				}
			}
			//fill color
			if(e.getSource() == fillCc){
				//only set the color and trigger state change if it's different
				Color c = fillCc.getColor();
				if(c != selectedDataPoint.getFillColor()){
					selectedDataPoint.setFillColor(c);
					//update proper state id buffer
					myLayer.increaseStateId(KRCLayer.IMAGES_BUFFER);
				}
			}
			//label size
			if(e.getSource() == sizeBx){
				//only set the size and trigger state change if it's different
				int size = (int)sizeBx.getSelectedItem();
				if(size != selectedDataPoint.getFontSize()){
					selectedDataPoint.setFontSize(size);
					//update proper state id buffer
					myLayer.increaseStateId(KRCLayer.LABELS_BUFFER);
				}
			}
			//label color
			if(e.getSource() == labelCc){
				//only set color and trigger state change if it's different
				Color c = labelCc.getColor();
				if(c != selectedDataPoint.getLabelColor()){
					selectedDataPoint.setLabelColor(c);
					//update proper state id buffer
					myLayer.increaseStateId(KRCLayer.LABELS_BUFFER);
				}
			}
			parent.repaint();
			
			//update 3d if something changed
			if(myLayer.getStateId(KRCLayer.IMAGES_BUFFER)!=imageState || myLayer.getStateId(KRCLayer.LABELS_BUFFER)!=labelState){
				if(ThreeDManager.getInstance().isReady()){
					ThreeDManager.getInstance().updateDecalsForLView(myLView, true);
				}
			}
		}
	};
	
	private AbstractAction setInputsAct = new AbstractAction("Set Inputs for Selected Data Point".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			String check = checkInputs();
			if(!check.equals("")){
				String errorText = "One or more inputs is invalid. See the following failures:";
				String[] errors = check.split(",");
				for(String error : errors){
					errorText += "\n"+error;
				}
				JOptionPane.showMessageDialog(KRCFocusPanel.this, errorText, "Invalid Input(s)", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			setInputsOnDataPoint(selectedDataPoint);
			refreshCharts();
		}
	};
	
	private AbstractAction createFromInputsAct = new AbstractAction("Create New Data Point from Inputs".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			//create a new name based on the old one
			int counter = 0;
			String name = selectedDataPoint.getName();
			//if it already has a counter prefix, remove that 
			// before continuing
			if(name.contains("(")){
				name = name.substring(0, name.length()-4);
			}
			for(KRCDataPoint pt : myLayer.getKRCDataPoints()){
				if(pt.getName().contains(name)){
					counter++;
				}
			}
			name += " ("+counter+")";
			
			KRCDataPoint newPt = new KRCDataPoint(myLayer, name, selectedDataPoint.getLat(), selectedDataPoint.getLon(), false);
			setInputsOnDataPoint(newPt);
			
			//add to layer
			myLayer.addDataPoint(newPt);
			//update focus panel
			refreshDataTables();
			//refresh lview
			myLView.repaint();
		}
	};

	private AbstractAction runAct = new AbstractAction("Run KRC for Necessary Data Point(s)".toUpperCase()) {
		public void actionPerformed(ActionEvent e) {
			
			//TODO: update the layer indicator while it's running
			
			//get selected datapoints
			int[] rows = resultsDataTbl.getSelectedRows();
			for(int row : rows){
				KRCDataPoint dp = ((DataTableModel)resultsDataTbl.getModel()).getDataPoint(row);
				//only run krc if it needs to be run
				if(dp.getDayData() == null || dp.getYearData() == null){
					runDavinciKRC(dp);
				}
			}
			
			runKrcBtn.setEnabled(false);

			//once the output is generated, refresh the table and charts display
			refreshResultTable();
		    refreshCharts();
		    
		    //reset the selected rows
		    for(int row : rows){
		    	resultsDataTbl.getSelectionModel().addSelectionInterval(row, row);
		    }
			    
		}
	};
	
	/**
	 * @return Returns true if all inputs are not empty and can be 
	 * parsed into Doubles.
	 */
	private String checkInputs(){		
		//comma separated list of failures
		String failures = "";
		//check to make sure the inputs aren't empty
		String lsTxt = lsTf.getText();
		if(lsTxt.length() == 0){
			failures += lsLbl.getText()+" was null."+",";
		}
		String hourTxt = hourTf.getText();
		if(hourTxt.length() == 0){
			failures += hourLbl.getText()+" was null."+",";
		}
		String elevTxt = elevTf.getText();
		if(elevTxt.length() == 0){
			failures += elevLbl.getText()+" was null."+",";
		}
		String albedoTxt = albedoTf.getText();
		if(albedoTxt.length() == 0){
			failures += albedoLbl.getText()+" was null."+",";
		}
		String slopeTxt = slopeTf.getText();
		if(slopeTxt.length() == 0){
			failures += slopeLbl.getText()+" was null."+",";
		}
		String opacityTxt = opacityTf.getText();
		if(opacityTxt.length() == 0){
			failures += opacityLbl.getText()+" was null."+",";
		}
		String tiTxt = tiTf.getText();
		if(tiTxt.length() == 0){
			failures += tiLbl.getText()+" was null."+",";
		}
		String azimuthTxt = azimuthTf.getText();
		if(azimuthTxt.length() == 0){
			failures += azimuthLbl.getText()+" was null."+",";
		}
		
		//all inputs should be valid doubles
		double ls = -1;
		double hour = -1;
		try{
			ls = Double.parseDouble(lsTxt);
			hour = Double.parseDouble(hourTxt);
			Double.parseDouble(elevTxt);
			Double.parseDouble(albedoTxt);
			Double.parseDouble(slopeTxt);
			Double.parseDouble(opacityTxt);
			Double.parseDouble(tiTxt);
			Double.parseDouble(azimuthTxt);
		}catch(NumberFormatException e){
			failures += "One or more inputs was not a valid number."+",";
			return failures;
		}
		
		//l sub s should go from 0 to 360
		if(ls<0 || ls>360){
			failures += lsLbl.getText()+" must go from 0 to 360."+",";
		}
		//hour must go from 0 to 24
		if(hour<0 || hour>24){
			failures += hourLbl.getText()+" must go from 0 to 24."+",";
		}
		
		return failures;
	}
	
	private void runDavinciKRC(KRCDataPoint dp) {
		String serverURL = Config.get("krc.url");
		
		String bodyStr = "";
		//TODO this has to be tailored for each body...
		if(Main.getBody().toLowerCase().equals("mars")){
			bodyStr = "Mars";
		}
		if(Main.getBody().toLowerCase().equals("luna")){
			bodyStr = "Earth,Moon";
		}
		if(Main.getBody().toLowerCase().equals("phobos")){
			bodyStr = "Mars,Phobos";
		}
		if(Main.getBody().toLowerCase().equals("bennu")){
			bodyStr = "minor,Bennu";
		}
		
		//all data point related info
		String urlStr = "KRCRunner?elevation="+dp.getElevation()
				+"&albedo="+dp.getAlbedo()+"&inertia="+dp.getThermalInertia()
				+"&opacity="+dp.getOpacity()+"&slope="+dp.getSlope()
				+"&latitude="+dp.getLat()+"&ls="+dp.getLSubS()
				+"&hour="+dp.getHour()+"&aspect="+dp.getAzimuth();
		if(bodyStr.length()>0){
			urlStr += "&body="+bodyStr;
		}
		
		//general script info
		urlStr += "&process="+KRCLayer.POINT_MODE_PROCESS+"&version="+KRCLayer.SCRIPT_VERSION;
		//add user name
		urlStr += "&user="+Main.USER+"&product="+Main.PRODUCT+"&domain="+Main.AUTH_DOMAIN;
		
		int idx = urlStr.indexOf("?");
		
		String connStr = serverURL + urlStr.substring(0, idx);
		String dataStr = urlStr.substring(idx+1);
		
		//TODO: give the user some kind of feedback that krc is running
		String scriptOutput = "Script Output:";
		try{
			JmarsHttpRequest req = new JmarsHttpRequest(connStr, HttpRequestType.POST);
			req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
			req.addOutputData(dataStr);
			req.setConnectionTimeout(60*1000);
			req.setReadTimeout(60*1000);
			req.send();
			ObjectInputStream ois = new ObjectInputStream(req.getResponseAsStream());
			ArrayList<ArrayList<Double>> values = (ArrayList<ArrayList<Double>>) ois.readObject();
			scriptOutput += (String) ois.readObject();
			ois.close();
			req.close();
			
			ArrayList<Double> dayX = values.get(0);
			ArrayList<Double> dayY = values.get(1);
			ArrayList<Double> yearX = values.get(2);
			ArrayList<Double> yearY = values.get(3);
			
			XYSeries daySeries = new XYSeries(dp.getName());
	        for (int i=0; i<dayX.size(); i++) {
	            daySeries.add(dayX.get(i), dayY.get(i));
	        }
	        
	        XYSeries yearSeries = new XYSeries(dp.getName());
	        for(int i=0; i<yearX.size(); i++){
	        	yearSeries.add(yearX.get(i), yearY.get(i));
	        }
	        
	        dp.setDayData(daySeries);
	        dp.setYearData(yearSeries);
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println(scriptOutput);
		}
	}
	
	
	private JTable createDataTable(boolean forInput){
		DataTableModel tm = new DataTableModel(myLayer.getKRCDataPoints(), forInput);
		JTable table = new DataTable(tm);
		ListSelectionModel lsm = table.getSelectionModel();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		if(forInput){
			lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lsm.addListSelectionListener(inputTableListener);
			table.addMouseListener(inputTableMouseListener);
		}else{
			//add a different listener for the resultTable
			lsm.addListSelectionListener(resultsTableListener);			
			table.addMouseListener(resultsTableMouseListener);
		}
		
		TableColumnAdjuster tca = new TableColumnAdjuster(table);
		tca.adjustColumns();
		
		return table;
	}
	
	private JTable createReadoutTable(boolean day){

		ArrayList<KRCDataPoint> data = new ArrayList<KRCDataPoint>();
		String tableModelType = "";
		JFreeChart chart = null;
		
		//day plot
		if(day){
			tableModelType = ReadoutTableModel.DAY;
			chart = dayChart;
			// if the chart has data, get the data points off the chart
			if(dayChart != null){
				ArrayList<String> krcNames = new ArrayList<String>();
				XYDataset dataset = dayChart.getXYPlot().getDataset();
				for(int i=0; i<dataset.getSeriesCount(); i++){
					krcNames.add((String)dayChart.getXYPlot().getDataset().getSeriesKey(i));
				}
				//find the matching krc points and add them to the data list
				for(KRCDataPoint dp : myLayer.getKRCDataPoints()){
					if(krcNames.contains(dp.getName())){
						data.add(dp);
					}
				}
			}
		}
		//year plot
		else{
			tableModelType = ReadoutTableModel.YEAR;
			chart = yearChart;
			// if the chart has data, get the data points off the chart
			if(yearChart != null){
				ArrayList<String> krcNames = new ArrayList<String>();
				XYDataset dataset = yearChart.getXYPlot().getDataset();
				for(int i=0; i<dataset.getSeriesCount(); i++){
					krcNames.add((String)yearChart.getXYPlot().getDataset().getSeriesKey(i));
				}
				//find the matching krc points and add them to the data list
				for(KRCDataPoint dp : myLayer.getKRCDataPoints()){
					if(krcNames.contains(dp.getName())){
						data.add(dp);
					}
				}
			}
		}

		ReadoutTableModel tm = new ReadoutTableModel(tableModelType, data, chart);
		JTable table = new ReadoutTable(tm);

		return table;
	}
	
	private ListSelectionListener inputTableListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			int row = inputDataTbl.getSelectedRow();
			if(row == -1){
				selectedDataPoint = null;
			}else{
				selectedDataPoint = ((DataTableModel)inputDataTbl.getModel()).getDataPoint(row);
			}
			
			refreshInputs();
			refreshDisplayOptions();
			
			myLView.repaint();
			
			//update 3d too
			myLayer.increaseStateId(KRCLayer.IMAGES_BUFFER);
			myLayer.increaseStateId(KRCLayer.LABELS_BUFFER);
			if(ThreeDManager.getInstance().isReady()){
				ThreeDManager.getInstance().updateDecalsForLView(myLView, true);
			}
		}
	};
	
	private ListSelectionListener resultsTableListener = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			//check to see if the run button needs to be enabled
			runKrcBtn.setEnabled(false);
			int[] rows = resultsDataTbl.getSelectedRows();
			for(int row : rows){
				//if any of the datapoints has null chart data, enable the button
				KRCDataPoint dp = ((DataTableModel)resultsDataTbl.getModel()).getDataPoint(row);
				if(dp.getDayData() == null || dp.getYearData() == null){
					runKrcBtn.setEnabled(true);
					break;
				}
			}
			
			refreshCharts();
		}
	};
	
	
	private MouseAdapter inputTableMouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent e){
			//show the menu on right-click
			if(SwingUtilities.isRightMouseButton(e)){
				//the menu
				JPopupMenu menu = new JPopupMenu();
				//the menu items
				JMenuItem centerOnSel = new JMenuItem(centerOnDataPointAct);
				JMenuItem deleteSel = new JMenuItem(deleteSelectedAct);
				JMenuItem editSel = new JMenuItem(editSelectedAct);
				//add items to menu
				menu.add(centerOnSel);
				menu.add(deleteSel);
				menu.add(editSel);
				//disable menu options if no datapoint is selected
				if(selectedDataPoint == null){
					centerOnSel.setEnabled(false);
					deleteSel.setEnabled(false);
					editSel.setEnabled(false);
				}
				//show menu item
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
			//if double click, center on point
			if(e.getClickCount() == 2){
				centerOnDataPoint();
			}
		}
	};
	
	
	private MouseAdapter resultsTableMouseListener = new MouseAdapter() {
		public void mouseClicked(MouseEvent e){
			//show the menu on right-click
			if(SwingUtilities.isRightMouseButton(e)){
				// the menu
				JPopupMenu menu = new JPopupMenu();
				
				//menu item
				JMenuItem exportItm = new JMenuItem(new ExportTableAction(menu, "Export KRC Data Table", ",", resultsDataTbl));
				
				//populate menu
				menu.add(exportItm);
				
				//show menu item
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	};
	
	private AbstractAction centerOnDataPointAct = new AbstractAction("Center on selected KRC point") {
		public void actionPerformed(ActionEvent e) {
			if(selectedDataPoint!=null){
				centerOnDataPoint();
			}
		}
	};
	private void centerOnDataPoint(){
		if(selectedDataPoint!=null){
			//Get the spatial center from the selected site.
			//Convert to world point to pass to the location manager
			Point2D worldCenter = Main.PO.convSpatialToWorld(selectedDataPoint.getPoint());
			//recenter the mainview
			Main.testDriver.mainWindow.getLocationManager().setLocation(worldCenter, true);
		}
	}
	
	private AbstractAction deleteSelectedAct = new AbstractAction("Delete selected KRC point") {
		public void actionPerformed(ActionEvent e) {
			if(selectedDataPoint != null){
				//delete data point from layer
				myLayer.removeDataPoint(selectedDataPoint);
				//set selected to null
				selectedDataPoint = null;
				//update focus panel
				refreshDataTables();
				refreshDisplayOptions();
				refreshInputs();
				//refresh lview
				myLView.repaint();
				//update 3d too
				myLayer.increaseStateId(KRCLayer.IMAGES_BUFFER);
				myLayer.increaseStateId(KRCLayer.LABELS_BUFFER);
				if(ThreeDManager.getInstance().isReady()){
					ThreeDManager.getInstance().updateDecalsForLView(myLView, true);
				}
			}
		}
	};
	
	private AbstractAction editSelectedAct = new AbstractAction("Edit KRC point...") {
		public void actionPerformed(ActionEvent e) {
			if(selectedDataPoint!=null){
				//show the edit dialog
				new AddDataPointDialog(KRCFocusPanel.this.getFrame(), myLView, "", null, null, selectedDataPoint, true);
			}
		}
	};
	
	/**
	 * Refresh the data table which shows all the krc data points
	 */
	public void refreshDataTables(){
		//refresh input
		refreshInputTable();
		
		//refresh results
		refreshResultTable();
	}
	
	private void refreshInputTable(){
		inputTablePnl.remove(inputTableSP);
		inputDataTbl = createDataTable(true);
		inputTableSP = new JScrollPane(inputDataTbl);
		inputTablePnl.add(inputTableSP);
		inputTablePnl.validate();
		
		int row = inputDataTbl.getSelectedRow();
		if(row == -1){
			selectedDataPoint = null;
		}else{
			selectedDataPoint = ((DataTableModel)inputDataTbl.getModel()).getDataPoint(row);
		}
		refreshDisplayOptions();
		refreshInputs();
	}
	
	private void refreshResultTable(){
		resultsTablePnl.remove(resultsTableSP);
		resultsDataTbl = createDataTable(false);
		resultsTableSP = new JScrollPane(resultsDataTbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultsTablePnl.add(resultsTableSP);
		resultsTablePnl.validate();
	}
	
	
	private void refreshDisplayOptions(){
		//disable all display options if no data point is selected
		if(selectedDataPoint == null){
			pointChk.setEnabled(false);
			outlineLbl.setEnabled(false);
			outlineCc.setEnabled(false);
			fillLbl.setEnabled(false);
			fillCc.setEnabled(false);
			labelChk.setEnabled(false);
			sizeLbl.setEnabled(false);
			sizeBx.setEnabled(false);
			fontLbl.setEnabled(false);
			labelCc.setEnabled(false);
		}else{
			//enable the checkboxes
			pointChk.setEnabled(true);
			labelChk.setEnabled(true);
			//set the field values based off the selected data point
			pointChk.setSelected(selectedDataPoint.showPoint());
			outlineCc.setColor(selectedDataPoint.getOutlineColor());
			fillCc.setColor(selectedDataPoint.getFillColor());
			labelChk.setSelected(selectedDataPoint.showLabel());
			sizeBx.setSelectedItem(selectedDataPoint.getFontSize());
			labelCc.setColor(selectedDataPoint.getLabelColor());
			//set whether they're enabled based off the check boxes
			//point
			outlineLbl.setEnabled(pointChk.isSelected());
			outlineCc.setEnabled(pointChk.isSelected());
			fillLbl.setEnabled(pointChk.isSelected());
			fillCc.setEnabled(pointChk.isSelected());
			//label
			sizeLbl.setEnabled(labelChk.isSelected());
			sizeBx.setEnabled(labelChk.isSelected());
			fontLbl.setEnabled(labelChk.isSelected());
			labelCc.setEnabled(labelChk.isSelected());
		}
	}
	
	private void refreshCharts(){
		//clear both charts to start with
		dayCPnl.removeAll();
		yearCPnl.removeAll();
		dayChart = null;
		yearChart = null;
		
		XYSeriesCollection dayData = new XYSeriesCollection();
		XYSeriesCollection yearData = new XYSeriesCollection();
		for(int row : resultsDataTbl.getSelectedRows()){
			KRCDataPoint dp = ((DataTableModel)resultsDataTbl.getModel()).getDataPoint(row);
			if(dp.getDayData()!=null){
				dayData.addSeries(dp.getDayData());
			}
			if(dp.getYearData()!=null){
				yearData.addSeries(dp.getYearData());
			}
		}
		
		if(dayData.getSeriesCount()>0 && yearData.getSeriesCount()>0){
			
			dayChart = createChart(dayData, true);			
			ThemeChart.configureUI(dayChart);			  
		    XYItemRenderer rr = ((XYPlot) dayChart.getPlot()).getRenderer();
		    rr.setSeriesPaint(0, ThemeChart.getPlotColor());
			
			yearChart = createChart(yearData, false);
			ThemeChart.configureUI(yearChart);			  
		    rr = ((XYPlot) yearChart.getPlot()).getRenderer();
		    rr.setSeriesPaint(0, ThemeChart.getPlotColor());			
		}else{
			dayCPnl.add(noDataLbl1);
			yearCPnl.add(noDataLbl2);		
			refreshReadoutTables();
			}
		
		
		dayCPnl.setChart(dayChart);
		yearCPnl.setChart(yearChart);
	}

	private void refreshReadoutTables() {
		// day readout
		dayROPnl.removeAll();
		dayROTbl = createReadoutTable(true);
		dayROSP = new JScrollPane(dayROTbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		dayROPnl.add(dayROSP);
		dayROPnl.revalidate();
		// year readout
		yearROPnl.removeAll();
		yearROTbl = createReadoutTable(false);
		yearROSP = new JScrollPane(yearROTbl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,	ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		yearROPnl.add(yearROSP);
		yearROPnl.revalidate();
	}

	private JFreeChart createChart(XYSeriesCollection dataset, boolean day) {

		Range range;
		String title;
		String xAxis;
		NumberTickUnit step;
		
		//use day values
		if(day){
			title = "Surface Temp over a Day";
			range = dayRange;
			step = dayStep;
			xAxis = "Hour of Day";
		}
		//otherwise use year values
		else{
			title = "Surface Temp over a Year";
			range = yearRange;
			step = yearStep;
			xAxis = "Ls";
		}
		
		
		JFreeChart chart = ChartFactory.createXYLineChart(
			title,		//title
			xAxis,		 		//x axis label
			"Temperature (K)",		 	//y axis label
			dataset,			 		//data
			PlotOrientation.VERTICAL,	//orientation
			false,		 				//legend
			false, 						//tooltips
			false);						//urls 
     
	      //format x-axis
	      XYPlot plot = (XYPlot) chart.getPlot();
	      
	      NumberAxis xaxis = (NumberAxis) plot.getDomainAxis();
	      xaxis.setNumberFormatOverride(xFormat);
	      xaxis.setRange(range);
	      xaxis.setTickUnit(step);
	    
	      //format y-axis
	      NumberAxis yaxis = (NumberAxis) plot.getRangeAxis();
	      yaxis.setAutoRangeIncludesZero(false);
	      
	      //format title size
	      chart.getTitle().setFont(ThemeFont.getBold().deriveFont(18f));
	      chart.getTitle().setPadding(16, 0, 16, 0);    
	   
	      
	      //add a progress listener, so whenever it's redrawn, it will trigger
	      // a refresh of the readout table
	      chart.addProgressListener(new ChartProgressListener() {
	    	  public void chartProgress(ChartProgressEvent e) {
	    		  if(e.getType() == ChartProgressEvent.DRAWING_FINISHED){
	    			  refreshReadoutTables();
	    		  }
	    	  }
	      });
	      
	      return chart;
	}
	
	private ChartMouseListener dayChartListener = new ChartMouseListener() {
		public void chartMouseMoved(ChartMouseEvent e) {
			chartMouseMovedEventOccurred(dayCH, dayCPnl, e.getChart(), e);
		}

		public void chartMouseClicked(ChartMouseEvent arg0) {
		}
	};
	
	private ChartMouseListener yearChartListener = new ChartMouseListener() {
		public void chartMouseMoved(ChartMouseEvent e) {
			chartMouseMovedEventOccurred(yearCH, yearCPnl, e.getChart(), e);
		}

		public void chartMouseClicked(ChartMouseEvent arg0) {
		}
	};
	

	private void chartMouseMovedEventOccurred(Crosshair ch, ChartPanel panel, JFreeChart chart, ChartMouseEvent e){
		XYPlot plot = chart.getXYPlot();
		ValueAxis xAxis = plot.getDomainAxis();
		
		Rectangle2D dataArea = panel.getScreenDataArea();
		double x = xAxis.java2DToValue(e.getTrigger().getX(), dataArea, plot.getDomainAxisEdge());
		Range range = xAxis.getRange();
		
		//only update plot and table if the x is within the domain range
		if(range.contains(x)){
			double xItemValue = -1;
			double xDiff = Double.MAX_VALUE;
			//cycle through x values and find the one nearest to the mouse
			for(int i=0; i<plot.getDataset().getItemCount(0); i++){
				double xValue = plot.getDataset().getXValue(0, i);
				double diff = Math.abs(x-xValue);
				//approaching the nearest point
				if(diff < xDiff){
					xDiff = diff;
					xItemValue = xValue;
				}
				//leaving the nearest point, so stop searching
				else{
					break;
				}
			}
			
			ch.setValue(xItemValue);
			getReadoutTableModelForChart(chart).setXValue(xItemValue);
		}
		
	}
	
	private ReadoutTableModel getReadoutTableModelForChart(JFreeChart chart){
		if(chart == dayChart){
			return (ReadoutTableModel)dayROTbl.getModel();
		}
		else if(chart == yearChart){
			return (ReadoutTableModel)yearROTbl.getModel();
		}
		return null;
	}
	
	
	private void refreshInputs(){
		//clear input text
		lsTf.setText("");
		hourTf.setText("");
		elevTf.setText("");
		albedoTf.setText("");
		slopeTf.setText("");
		opacityTf.setText("");
		tiTf.setText("");
		azimuthTf.setText("");
		//if no data point is selected, clear all inputs and disable
		if(selectedDataPoint == null){
			//disable labels
			lsLbl.setEnabled(false);
			hourLbl.setEnabled(false);
			elevLbl.setEnabled(false);
			albedoLbl.setEnabled(false);
			slopeLbl.setEnabled(false);
			opacityLbl.setEnabled(false);
			tiLbl.setEnabled(false);
			azimuthLbl.setEnabled(false);
			//disable input fields
			lsTf.setEnabled(false);
			hourTf.setEnabled(false);
			elevTf.setEnabled(false);
			albedoTf.setEnabled(false);
			slopeTf.setEnabled(false);
			opacityTf.setEnabled(false);
			tiTf.setEnabled(false);
			azimuthTf.setEnabled(false);
			//disable input buttons
			setInputBtn.setEnabled(false);
			createFromInputBtn.setEnabled(false);
		}
		//enable and populate the inputs based off the selected data point
		else{
			//enable labels
			lsLbl.setEnabled(true);
			hourLbl.setEnabled(true);
			elevLbl.setEnabled(true);
			albedoLbl.setEnabled(true);
			slopeLbl.setEnabled(true);
			opacityLbl.setEnabled(true);
			tiLbl.setEnabled(true);
			azimuthLbl.setEnabled(true);
			//enable input fields
			lsTf.setEnabled(true);
			hourTf.setEnabled(true);
			elevTf.setEnabled(true);
			albedoTf.setEnabled(true);
			slopeTf.setEnabled(true);
			opacityTf.setEnabled(true);
			tiTf.setEnabled(true);
			azimuthTf.setEnabled(true);
			//populate input fields
			//keep track of any failures if map sampling doesn't have data
			statFailures.clear();
			lsTf.setText(getTextForInput(selectedDataPoint.getLSubS(), "L sub S"));
			hourTf.setText(getTextForInput(selectedDataPoint.getHour(), "Hour"));
			elevTf.setText(getTextForInput(selectedDataPoint.getElevation(), "Elevation"));
			albedoTf.setText(getTextForInput(selectedDataPoint.getAlbedo(), "Albedo"));
			slopeTf.setText(getTextForInput(selectedDataPoint.getSlope(), "Slope"));
			opacityTf.setText(getTextForInput(selectedDataPoint.getOpacity(), "Opacity"));
			tiTf.setText(getTextForInput(selectedDataPoint.getThermalInertia(), "Thermal Inertia"));
			azimuthTf.setText(getTextForInput(selectedDataPoint.getAzimuth(), "Azimuth"));
			//if there were any failures tell the user
			if(statFailures.size()>0){
				String errorText = "The following stat(s) failed to find defaults:\n\n";
				for(String stat : statFailures){
					errorText+=stat+"\n";
				}
				errorText+="\nPlease enter the necessary input value(s).";
				
				JOptionPane.showMessageDialog(KRCFocusPanel.this, errorText, "Default(s) not found", JOptionPane.ERROR_MESSAGE);
			}
			
			//enable input buttons
			setInputBtn.setEnabled(true);
			createFromInputBtn.setEnabled(true);
		}
	}
	
	/**
	 * Converts the double value of an input to a String.  Also, checks
	 * for a Double.MAX_VALUE which can be returned for inputs that are
	 * map sampled, and returns an empty string.  Also adds the stat to the
	 * failure list to be displayed to the user.
	 * @param inputValue The value to convert to string
	 * @param stat The name of the stat that value is for
	 * @return A String object representing the double value, or an empty
	 * string if the value is Double.MIN_VALUE (Because that means the 
	 * map sampling failed).
	 */
	private String getTextForInput(double inputValue, String stat){
		String result = "";
		
		//if the map sampling failed, then the value will be Double.MIN_VALUE
		if(inputValue == Double.MAX_VALUE){
			result = "";
			statFailures.add(stat);
			
		}else{
			result = inputValue+"";
		}
		
		return result;
	}
	
	
	private void setInputsOnDataPoint(KRCDataPoint dp){
		dp.setLSubS(Double.parseDouble(lsTf.getText()));
		dp.setHour(Double.parseDouble(hourTf.getText()));
		dp.setElevation(Double.parseDouble(elevTf.getText()));
		dp.setAlbedo(Double.parseDouble(albedoTf.getText()));
		dp.setSlope(Double.parseDouble(slopeTf.getText()));
		dp.setOpacity(Double.parseDouble(opacityTf.getText()));
		dp.setThermalInertia(Double.parseDouble(tiTf.getText()));
		dp.setAzimuth(Double.parseDouble(azimuthTf.getText()));
	}
	
	/**
	 * @return The selected KRC data point shown in the
	 * data table
	 */
	public KRCDataPoint getSelectedDataPoint(){
		return selectedDataPoint;
	}
}
