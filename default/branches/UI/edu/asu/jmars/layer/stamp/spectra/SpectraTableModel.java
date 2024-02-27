package edu.asu.jmars.layer.stamp.spectra;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class SpectraTableModel extends AbstractTableModel {

	private static final String ID_COL = "ID";
	private static final String NAME_COL = "Name";
	private ArrayList<String> cNames;
	private ArrayList<SpectraObject> spectra;
	
	public SpectraTableModel(ArrayList<SpectraObject> contents){
		spectra = contents;
		
		cNames = new ArrayList<String>();
//		cNames.add(ID_COL);
		cNames.add(NAME_COL);
	}
	
	@Override
	public int getRowCount() {
		return spectra.size();
	}

	@Override
	public int getColumnCount() {
		return cNames.size();
	}

	@Override
	public String getColumnName(int col){
		return cNames.get(col);
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		SpectraObject s = spectra.get(rowIndex);
		
		switch(getColumnName(columnIndex)){
//		case ID_COL:
//			return s.getId();
		case NAME_COL:
			return s.getName();
		}
		return null;
	}
	
	
	public SpectraObject getSpectra(int row){
		return spectra.get(row);
	}

	
	public ArrayList<SpectraObject> getSpectra(int[] rows){
		ArrayList<SpectraObject> result = new ArrayList<SpectraObject>();
		for(int row : rows){
			result.add(spectra.get(row));
		}
		return result;
	}
}
