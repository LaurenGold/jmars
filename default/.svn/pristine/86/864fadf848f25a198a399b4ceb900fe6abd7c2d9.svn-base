package edu.asu.jmars.layer.scale;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import edu.asu.jmars.Main;
import edu.asu.jmars.ProjObj;
import edu.asu.jmars.graphics.FontRenderer;
import edu.asu.jmars.graphics.GraphicsWrapped;
import edu.asu.jmars.layer.Layer.LView3D;
import edu.asu.jmars.layer.MultiProjection;
import edu.asu.jmars.util.Util;

public class ScaleLView3D extends LView3D {
	private ScaleLayer myLayer;

	public ScaleLView3D(ScaleLayer layer) {
		super(layer);
		myLayer = layer;
	}
	
	public BufferedImage createDataImage(Rectangle2D worldRect, ProjObj po, int ppd, int labelScaleFactor){
		BufferedImage bufferedImage = Util.newBufferedImage((int)(worldRect.getWidth()*ppd), (int)(worldRect.getHeight()*ppd));
		//create the graphics object to draw on
		Graphics2D g2 = bufferedImage.createGraphics();
		//use a wrapped world graphics object so that if the 
		// footprints go passed 360 world coords, they will 
		// still be drawn
		g2 = new GraphicsWrapped(g2, 360, ppd, worldRect, "wrapWorldGraphics");
		
		//This graphics correction all came from the StampUtil drawShapes method
		Dimension window = new Dimension((int)(worldRect.getWidth()*ppd), (int)(worldRect.getHeight()*ppd));
		double wHeight = window.getHeight();
		double wWidth = window.getWidth();
		AffineTransform world2image = new AffineTransform();
		// Correct for upside-down-ness
		world2image.scale(1, -1);
		world2image.translate(0, -wHeight);
		world2image.scale(wWidth/worldRect.getWidth(), wHeight/worldRect.getHeight());
		world2image.translate(-worldRect.getX(), -worldRect.getY());
		g2.transform(world2image);
		
		
		ScaleParameters parms = myLayer.getParameters();
		Rectangle2D box = myLayer.getWorldRulerBox();

		//draw ruler
		g2.setColor(parms.barColor);
		g2.setStroke((new BasicStroke((float)1.0/ppd)));
		g2.fill(box);
		
		//draw tick boxes
		g2.setColor(parms.tickColor);
		for(Rectangle2D tick : myLayer.getWorldTickBoxes()){
			g2.fill(tick);
		}
		
		//draw the text
		Font font = new Font(parms.labelFont.getFamily(), parms.labelFont.getStyle(), parms.labelFont.getSize()*labelScaleFactor);
		FontRenderer fr = new FontRenderer(font, parms.fontOutlineColor, parms.fontFillColor);
		fr.setLabel(myLayer.getFontString());
		fr.setBorder(null);
		fr.setAntiAlias(true);
		
		//font dimensions in pixels
		double fontWidth = fr.getPreferredSize().getWidth();
		double fontHeight = fr.getPreferredSize().getHeight();
		
		//calculate the font dimensions in world coords
		MultiProjection proj = Main.testDriver.mainWindow.getProj();
		Point2D start = proj.screen.toWorld(0, 0);
		Point2D end = proj.screen.toWorld(fontWidth, fontHeight);
		double fontWidthWorld = end.getX() - start.getX();
		double fontHeightWorld = start.getY() - end.getY();
		//scale for ppd difference
		double worldScale = ppd/proj.getPPD();
		fontWidthWorld = fontWidthWorld/worldScale;
		fontHeightWorld = fontHeightWorld/worldScale;
		
		double heightBuffer = 1.8;
		if(myLayer.getWorldTickBoxes().size() == 0){
			heightBuffer = 1.2;
		}
		//determine the location of the label
		double fontX, fontY;
		switch (parms.h_alignment) {
		case Center:
			fontX = box.getX()+box.getWidth()/2 - (fontWidthWorld/2);
			break;
		case Right:
			fontX = box.getX()+box.getWidth() - fontWidthWorld;
			break;
		case Left:
		default:
			fontX = box.getX();
			break;
		}
		switch (parms.v_alignment) {
		case Below:
			fontY = box.getY() - fontHeightWorld;
			break;
		case Above:
		default:
			fontY = box.getY()+box.getHeight()*heightBuffer;
			break;
		}
		
		g2.translate(fontX, fontY);
		fr.paintLabel(g2, myLayer.getFontString(), 0, 0);
		
		return bufferedImage;
	}
	
	@Override
	public boolean isEnabled(){
		return false;
	}
	
	@Override
	public boolean exists(){
		return false;
	}

}
