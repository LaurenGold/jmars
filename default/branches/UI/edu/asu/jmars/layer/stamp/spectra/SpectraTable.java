package edu.asu.jmars.layer.stamp.spectra;

import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class SpectraTable extends JTable{
	private SpectraTableModel myModel;
	
	public SpectraTable(SpectraTableModel model){
		super(model);
		myModel = model;
	}
	
	public String getToolTipText(MouseEvent event) {
		String tip = "";
		int row = rowAtPoint(event.getPoint());
		if(row>-1 && row<myModel.getRowCount()){
			SpectraObject so = myModel.getSpectra(row);
			tip = "Desc: "+so.getDesc();
		}
		
		return tip;
	}
}
