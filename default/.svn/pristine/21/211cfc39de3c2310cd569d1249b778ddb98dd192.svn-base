package edu.asu.jmars.layer.stamp.spectra;

public class SpectraObject {

	private String name;
	private String desc;
	private double[] xValues;
	private double[] yValues;
	/** This variable is used to keep the datasets together,
	 * eg. I_over_F, radiance, etc.
	 */
	private String type;
	
	private boolean isMarkedStamp = false;
	private boolean isComputedSpectra = false;
	
	
	public SpectraObject(String desc, double[] x, double[] y, String type, boolean marked, boolean computed){
		this.name = desc;
		this.desc = desc;
		this.type = type;
		xValues = x;
		yValues = y;
		isMarkedStamp = marked;
		isComputedSpectra = computed;
	}
	
	
	public String getName(){
		return name;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public double[] getXValues(){
		return xValues;
	}
	
	public double[] getYValues(){
		return yValues;
	}
	
	public String getType(){
		return type;
	}
	
	public boolean isMarked(){
		return isMarkedStamp;
	}
	
	public boolean isComputed(){
		return isComputedSpectra;
	}
	
	public void setName(String newName){
		name = newName;
	}
}
