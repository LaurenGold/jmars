package edu.asu.jmars.layer.grid;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import edu.asu.jmars.ProjObj;
import edu.asu.jmars.layer.Layer;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.renderer.textures.Decal;
import edu.asu.jmars.viz3d.renderer.textures.DecalSet;

public class GridLView3D extends LView3D{
	//TODO: this variable can be deleted when updating
	// shape models for decals is supported
	private String shapeModel = "";
	private GridLView myLView;
	private int currentStateId = -1;
	
	public GridLView3D(Layer layer) {
		super(layer);
		
		exists = true;
		usesDecals = true;
	}
	
	/**
	 * Set the LView on this LView3d
	 * @param view
	 */
	public void setLView(GridLView view){
		myLView = view;
	}
	

	/**
	 * This layer will only be enabled when the shape model
	 * that was active when it was added, is selected. This is
	 * because changing shape models with decals is not yet 
	 * supported.
	 * 
	 * When updating shape models is supported for decals the 
	 * comment should read:
	 * Is always enabled since it is not shape model dependant
	 * @see edu.asu.jmars.layer.Layer.LView3D#isEnabled()
	 */
	public boolean isEnabled(){
		if(mgr.getShapeModel() == null){
			return true;
		}else{
			//first time through, set the shape model
			if(shapeModel.equals("")){
				shapeModel = mgr.getShapeModelName();
			}
		}
		
		return mgr.getShapeModelName().equals(shapeModel);
		
		//TODO: when updating shape models is supported the
		// method should return true always.
//		return true;
	}
	
	
	public DecalSet getDecals(){	
		final int layerState = myLayer.getStateId(0);
		
		//if the current layer state for this lview3d is not equal
		// to the state on the layer, something has changed, so 
		// redraw the decals
		if(currentStateId != layerState){
			//set the states the same, it can be checked throughout 
			// the process and aborted if it becomes outdated
			currentStateId = layerState;

			ArrayList<DecalSet> decalSets = mgr.getLayerDecalSet(this);
			final DecalSet dSet = decalSets.get(0);
			dSet.setRenderable(false);
			//do the decal drawing on a separate thread, so it doesn't
			// hold up the rest of the application
			final ArrayList<Decal> decals = dSet.getDecals();
			if(!decals.isEmpty()){
				final Thread manager = new Thread(new Runnable() {
					public void run() {
						for(Decal d : decals){
							if(layerState != myLayer.getStateId(0)){
								//break out of creating new threads if at any
								// time the map layer has changed since this work
								// has started
								break;
							}
							BufferedImage bi = drawLines(d);
							d.setImage(bi);
						}
						
						dSet.setRenderable(true);
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								mgr.refresh();
							}
						});
					}
				});
				
				manager.start();
			}
		}
		// Return value doesn't matter
		return null;
	}
	
	
	private BufferedImage drawLines(Decal decal){
		//TODO: maybe this initial logic should be put into the 
		// decal object, since it's done in almost every LView3D class?
		//create a buffered image from the information in the decal
		int ppd = decal.getPPD();
		Point2D minExtent = decal.getMinExtent();
		Point2D maxExtent = decal.getMaxExtent();
		double width = maxExtent.getX()-minExtent.getX();
		double height = maxExtent.getY()-minExtent.getY();
		height = Math.abs(height);
		width = Math.abs(width);
		ProjObj po = decal.getProjection();
		Rectangle2D worldRect = new Rectangle2D.Double(minExtent.getX(), maxExtent.getY(), width, height);
		BufferedImage bufferedImage = createDataImage(worldRect, po, ppd, -1);
		
		return bufferedImage;
	}
	
	
	
	private Graphics2D createTransformedG2(BufferedImage bi, double x, double y, double width, double height, int ppd){
		Graphics2D g2 = bi.createGraphics();
		
		//This graphics correction all came from the StampUtil drawShapes method
		Dimension window = new Dimension((int)(width*ppd), (int)(height*ppd));
		double wHeight = window.getHeight();
		double wWidth = window.getWidth();
		AffineTransform world2image = new AffineTransform();
		// Correct for upside-down-ness
		world2image.scale(1, -1);
		world2image.translate(0, -wHeight);
		world2image.scale(wWidth/width, wHeight/height);
		world2image.translate(-x, -y);
		g2.transform(world2image);	
		
		return g2;
	}
	
	public BufferedImage createDataImage(Rectangle2D worldRect, ProjObj po, int ppd, int labelScaleFactor){
		BufferedImage bufferedImage = Util.newBufferedImage((int)(worldRect.getWidth()*ppd), (int)(worldRect.getHeight()*ppd));

		double x = worldRect.getX();
		double y = worldRect.getY();
		
		double width = worldRect.getWidth();
		double height = worldRect.getHeight();
		
		//create the array object used for drawing the lines
		Graphics2D[] g2s;
		//create the first graphics object
		Graphics2D g2 = createTransformedG2(bufferedImage, x, y, width, height, ppd);
		//if necessary shift the starting x so that the x+width is within 0-360 
		// and create a second graphics object
		//Populate the array either way
		if(x+width>360){
			x = x-360;
			
			Graphics2D g2b = createTransformedG2(bufferedImage, x, y, width, height, ppd);
			g2s = new Graphics2D[]{g2, g2b};
			
		}else if(x<0){
			x = x+360;
			
			Graphics2D g2b = createTransformedG2(bufferedImage, x, y, width, height, ppd);
			g2s = new Graphics2D[]{g2, g2b};
			
		}else{
			g2s = new Graphics2D[]{g2};
		}
		
		//draw the minor and major lines
		if(myLView.minor.isVisible(GridLView.THREE_D)){
			myLView.drawLatLon(-1, g2s, myLView.minor, po, true);
		}
		if(myLView.major.isVisible(GridLView.THREE_D)){
			myLView.drawLatLon(-1, g2s, myLView.major, po, true);
		}
		
		
		return bufferedImage;
	}

}
