package edu.asu.jmars.layer.mcd;

import java.awt.Color;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DatasetUtilities;

public class ReadoutTableModel extends AbstractTableModel {
	protected static final String HEIGHT = "HT";
	protected static final String LT = "LT";
	protected static final String LS = "LS";
	private static final String NAME_COL = "Name";
	private static final String COLOR_COL = "Color";
	private static final String TEMP_COL = "Temperature (K)";
	private static final String HOUR_COL = "Hour";
	private static final String LS_COL = "Ls";
	private static final String HEIGHT_COL = "Height";
	private static final String SCENARIO_COL = "Scenario";
	
	private ArrayList<String> cNames;
	private String myType;
	private ArrayList<MCDDataPoint> mcdPoints;
	private JFreeChart myChart;
	private double x_value = 0;
	private MCDFocusPanel myFocus;
	
	private DecimalFormat df = new DecimalFormat("###,###.###");
	
	public ReadoutTableModel(MCDFocusPanel fp, String type, ArrayList<MCDDataPoint> data, JFreeChart chart){
		myType = type;
		mcdPoints = data;
		myChart = chart;
		myFocus = fp;
		
		cNames = new ArrayList<String>();
		cNames.add(NAME_COL);
		cNames.add(COLOR_COL);
		cNames.add(TEMP_COL);
		cNames.add(HEIGHT_COL);
		cNames.add(HOUR_COL);
		cNames.add(LS_COL);
		cNames.add(SCENARIO_COL);
	}
	
	@Override
	public int getRowCount() {
		return mcdPoints.size();
	}

	@Override
	public int getColumnCount() {
		return cNames.size();
	}
	
	@Override
	public String getColumnName(int col){
		return cNames.get(col);
	}
	
	/**
	 * Add this override so that the Table that uses this 
	 * table model can set the column editors and renderers
	 * appropriately
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex){
		switch(getColumnName(columnIndex)){
			case NAME_COL:
				return String.class;
			case COLOR_COL: 
				return Color.class;
			case TEMP_COL:
			case HOUR_COL:
			case LS_COL:
			case HEIGHT_COL:
				return String.class;
			case SCENARIO_COL:
				return Integer.class;
		}
		
		return Object.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		MCDDataPoint dataPt = mcdPoints.get(rowIndex);
		//create objects which will be returned for each column
		String name = dataPt.getName();
		Color c = null;
		Paint p =  myChart.getXYPlot().getRenderer().getSeriesPaint(rowIndex);
		if(p instanceof Color){
			c = (Color)p;
		}
		String temp = df.format(DatasetUtilities.findYValue(myChart.getXYPlot().getDataset(), rowIndex, x_value));;
		String height = "";
		String hour = "";
		String ls = "";
		String scen = dataPt.getScenario()+"";
		
		//populate objects with correct values
		//data is in different places depending on which type we're looking at
		if(myType == HEIGHT){
			height = df.format(x_value);
			hour = df.format(dataPt.getHour());
			ls = df.format(dataPt.getLSubS());
		}
		else if(myType == LT){
			height = df.format(dataPt.getHeight());
			hour = df.format(x_value);
			ls = df.format(dataPt.getLSubS());
		}
		else if(myType == LS){
			height = df.format(dataPt.getHeight());
			hour = df.format(dataPt.getHour());
			ls = df.format(x_value);
		}
		
		switch(getColumnName(columnIndex)){
		case NAME_COL:
			return name;
		case COLOR_COL:
			return c;
		case TEMP_COL:
			return temp;
		case HOUR_COL:
			return hour;
		case LS_COL:
			return ls;
		case HEIGHT_COL:
			return height;
		case SCENARIO_COL:
			return scen;
		}
	
		return null;
		
	}
	
	/**
	 * Takes x-value for the chart crosshair and displays it
	 * as the x value in the table, and uses it to find the y 
	 * value and displays that as well.
	 * @param x  The x-value that the chart crosshair is at
	 */
	public void setXValue(double x){
		x_value = x;
		fireTableDataChanged();
	}
	
	public boolean isCellEditable(int rowIndex, int colIndex){
		return (getColumnName(colIndex) == COLOR_COL);
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex){
		if (!getColumnName(columnIndex).equals(COLOR_COL)){
			throw new IllegalArgumentException("Columns other than the color column are uneditable.");
		}
		
		XYPlot plot = myChart.getXYPlot();
		if (rowIndex < plot.getDataset().getSeriesCount()){
			myChart.getXYPlot().getRenderer().setSeriesPaint(rowIndex, (Color)value);
		}
	}
}
