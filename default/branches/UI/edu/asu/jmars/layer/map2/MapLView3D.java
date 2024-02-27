package edu.asu.jmars.layer.map2;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import edu.asu.jmars.ProjObj;
import edu.asu.jmars.graphics.GraphicsWrapped;
import edu.asu.jmars.layer.Layer;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.util.Util;
import edu.asu.jmars.viz3d.renderer.textures.Decal;
import edu.asu.jmars.viz3d.renderer.textures.DecalSet;

public class MapLView3D extends LView3D{
	
	private Pipeline[] pipeline;
	private DecalSet dSet = null;
	private int currentStateId;
	private MapLView myLView;
	
	//TODO: this variable can be deleted when updating
	// shape models for decals is supported
	private String shapeModel = "";

	public MapLView3D(Layer layer) {
		super(layer);
		
		exists = true;
		usesDecals = true;
		//there is only one buffer (decal set) for maps
		currentStateId = myLayer.getStateId(0);
	}
	
	
	/**
	 * Set the LView on this LView3d
	 * @param view
	 */
	public void setLView(MapLView view){
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
	
	
	public synchronized DecalSet getDecals(){
		
		int layerState = myLayer.getStateId(0);
		
		if(dSet == null || currentStateId != layerState){
			currentStateId = layerState;
			ArrayList<DecalSet> decalSets = mgr.getLayerDecalSet(this);
			dSet = decalSets.get(0);
			
			
			if (dSet != null) {
								
				final ArrayList<Decal> copyDecals = dSet.getDecals();
				
				if (!copyDecals.isEmpty()) {
					if(currentStateId == myLayer.getStateId(0)){
						if (myLView==null || myLView.getGraphicRequest()==null) return dSet;
						
						Pipeline[] pipeline = myLView.getGraphicRequest().getPipelines();

						// We maintain this mapping so we know which request relates to our decal, and also to cancel in bulk if needed
						final HashMap<Decal, MapChannelTiled> allRequests = new HashMap<Decal, MapChannelTiled>();
						final HashMap<Decal, BufferedImage> allImages = new HashMap<Decal, BufferedImage>();
												
						for (final Decal d : copyDecals){
							Point2D minExtent = d.getMinExtent();
							Point2D maxExtent = d.getMaxExtent();
									
							double width = maxExtent.getX()-minExtent.getX();
							double height = maxExtent.getY()-minExtent.getY();
									
							height = Math.abs(height);
							width = Math.abs(width);
									
							final Rectangle2D worldRect = new Rectangle2D.Double(minExtent.getX(), maxExtent.getY(), width, height);
							final Dimension window = new Dimension((int)width*d.getPPD(), (int)height*d.getPPD());
														
							MapChannelReceiver statusReceiver = new MapChannelReceiver() {
								public void mapChanged(MapData mapData) {
									if (currentStateId != myLayer.getStateId(0)) {
										HashMap<Decal, MapChannelTiled> clonedRequests = (HashMap<Decal, MapChannelTiled>)allRequests.clone();
										
										for (MapChannelTiled request : clonedRequests.values()) {
											request.cancel();
										}
										allRequests.clear();

										return;
									}
									
									BufferedImage decalImage = allImages.get(d);
																		
									Graphics2D g2 = decalImage.createGraphics();
									
									AffineTransform world2image = new AffineTransform();
						
									// Correct for upside-down-ness
									world2image.scale(1, -1);
									world2image.translate(0, -window.getHeight());
						
									world2image.scale(window.getWidth()  / worldRect.getWidth(),
													  window.getHeight() / worldRect.getHeight());
									world2image.translate(-worldRect.getX(), -worldRect.getY());
									
									g2.setTransform(world2image);
									
									g2 = new GraphicsWrapped(g2, 360, d.getPPD(), worldRect, "wrapWorldGraphics");
									
									BufferedImage img = mapData.getImage();
									
									// paint a good MapData object by passing through ignore filter if defined, and clipping to extent
									BufferedImageOp op = mapData.getOperator();
									if (op != null) {
										img = op.filter(img, Util.newBufferedImage(img.getWidth(), img.getHeight()));
									}
									Rectangle2D dataBounds = mapData.getRequest().getExtent();
									Rectangle2D worldClip = new Rectangle2D.Double();
									worldClip.setFrame(-180,-90,1080,180);
									Rectangle bounds = MapData.getRasterBoundsForWorld(img.getRaster(), dataBounds, worldClip);
									if (!bounds.isEmpty()) {
										g2.setComposite(AlphaComposite.Src);
										Rectangle2D.intersect(worldClip, dataBounds, worldClip);
										img = img.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
										g2.drawImage(img, Util.image2world(img.getWidth(), img.getHeight(), worldClip), null);
									} 
									
									MapChannelTiled request = allRequests.get(d);
									
									// Only update 3D with tiles when each request finishes
									if (request!=null && !request.isFinished()) {
										return;
									} else {
										allRequests.remove(d);;
									}
									
									if (currentStateId == myLayer.getStateId(0)) {
										d.setImage(allImages.get(d));
										
										if(currentStateId == myLayer.getStateId(0)){
											dSet.setRenderable(true);
										}
			
										SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												mgr.refresh();
											}
										});
										
									}
								}
							};
														
							MapChannelTiled decalRequest = new MapChannelTiled(statusReceiver);
							allRequests.put(d, decalRequest);  							
							allImages.put(d, Util.newBufferedImage((int)(width*d.getPPD()), (int)(height*d.getPPD())));

							decalRequest.setRequest(d.getProjection(), worldRect, d.getPPD(), pipeline);
						}
					}
				}
			}			
		}
		
		return dSet;
	}
	
	/**
	 * This class and the code below is used as part of the High Res Export process, but is not used in the decal rendering portion of MapLView3D
	 *
	 */
	private class MapSampler implements Runnable, MapChannelReceiver{
		private CountDownLatch latch;
		private Rectangle2D worldRect;
		private ProjObj po;
		private int ppd;
		private BufferedImage finalImage;

		private MapChannelTiled request;
		
		private MapSampler(CountDownLatch latch, Rectangle2D worldRect, ProjObj po, int ppd){
			this.latch = latch;
			this.worldRect = worldRect;
			this.po = po;
			this.ppd = ppd;

			finalImage = Util.newBufferedImage((int)(worldRect.getWidth()*ppd), (int)(worldRect.getHeight()*ppd));

			request = new MapChannelTiled(this);

		}
		
		@Override
		public void mapChanged(MapData mapData) {
			// This code is very similar to the logic in getDecals(), but combining the two smartly wasn't obvious
			Graphics2D g2 = finalImage.createGraphics();
			
			// world2Screen
			double width = worldRect.getWidth();
			double height = worldRect.getHeight();
					
			Dimension window = new Dimension((int)width*ppd, (int)height*ppd);
			AffineTransform world2image = new AffineTransform();

			// Correct for upside-down-ness
			world2image.scale(1, -1);
			world2image.translate(0, -window.getHeight());

			world2image.scale(window.getWidth()  / worldRect.getWidth(),
							  window.getHeight() / worldRect.getHeight());
			world2image.translate(-worldRect.getX(), -worldRect.getY());
			
			g2.setTransform(world2image);
			
			g2 = new GraphicsWrapped(g2, 360, ppd, worldRect, "wrapWorldGraphics");
			BufferedImage img = mapData.getImage();
			
			// paint a good MapData object by passing through ignore filter if defined, and clipping to extent
			BufferedImageOp op = mapData.getOperator();
			if (op != null) {
				img = op.filter(img, Util.newBufferedImage(img.getWidth(), img.getHeight()));
			}
			Rectangle2D dataBounds = mapData.getRequest().getExtent();
			Rectangle2D worldClip = new Rectangle2D.Double();
			worldClip.setFrame(-180,-90,1080,180);
			Rectangle bounds = MapData.getRasterBoundsForWorld(img.getRaster(), dataBounds, worldClip);
			if (!bounds.isEmpty()) {
				g2.setComposite(AlphaComposite.Src);
				Rectangle2D.intersect(worldClip, dataBounds, worldClip);
				img = img.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
				g2.drawImage(img, Util.image2world(img.getWidth(), img.getHeight(), worldClip), null);
			}
			
			if (request.isFinished()) {
				latch.countDown();
			}
		}

		@Override
		public void run() {
			if(currentStateId == myLayer.getStateId(0)){
				if(myLView.getGraphicRequest() == null){
					//if there's nothing to draw, don't make any requests
					latch.countDown();
					return;
				}else{
					pipeline = myLView.getGraphicRequest().getPipelines();
					request.setRequest(po, worldRect, ppd, pipeline);
				}				
			}
		}
		
		public BufferedImage getImage(){
			return finalImage;
		}
		
	}
	
	public BufferedImage createDataImage(Rectangle2D worldRect, ProjObj po, int ppd, int labelScaleFactor){
		final BufferedImage bi = Util.newBufferedImage((int)(worldRect.getWidth()*ppd), (int)(worldRect.getHeight()*ppd));
		final Rectangle2D wRect = worldRect;
		final ProjObj projObj = po;
		final int Ppd = ppd;
		
		currentStateId = myLayer.getStateId(0);
		//draws one image at a time
		CountDownLatch latch = new CountDownLatch(1);
		
		MapSampler ms = new MapSampler(latch, wRect, projObj, Ppd);
		
		Thread t = new Thread(ms);
		t.start();
		
		try {
			Long appropriateWait = (long)100000;
			latch.await(appropriateWait, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(latch.getCount()>0){
			System.err.println("Request timed out while trying to pull map tiles for High Res export.");
		}else{
			 bi.getGraphics().drawImage(ms.getImage(), 0, 0, null);
		}
		
		return bi;
	}

}
