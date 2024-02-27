package edu.asu.jmars.layer.mosaics;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.LViewFactory;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.layer.SerializedParameters;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.ui.image.factory.ImageCatalogItem;
import edu.asu.jmars.ui.image.factory.ImageFactory;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeImages;

public class MosaicsLViewFactory extends LViewFactory {
	
	static Color imgLayerColor = ((ThemeImages) GUITheme.get("images")).getLayerfill();
	static final Icon layerTypeIcon = new ImageIcon(ImageFactory.createImage(ImageCatalogItem.MOSAICS_LAYER_IMG
       .withDisplayColor(imgLayerColor)));	
	
	
	public MosaicsLViewFactory(){
		super("Mosaics", "Displays blended mosaics of many images over areas of interest");
		type = "mosaic";
	}
	
	public LView createLView() {
		return null;
	}

	public void createLView(boolean async, LayerParameters l) {
		LView lview = buildLView();
		lview.setLayerParameters(l);
		LManager.receiveNewLView(lview);
	}

	public LView recreateLView(SerializedParameters parmBlock) {
		return buildLView();
	}
	
	private LView buildLView(){
		MosaicsLayer layer = new MosaicsLayer();
		MosaicsLView3D lview3d = new MosaicsLView3D(layer);
		MosaicsLView lview = new MosaicsLView(layer, lview3d);
		lview.originatingFactory = this;
		return lview;
	}
	
	  @Override 
	  public Icon getLayerIcon() {
		  return layerTypeIcon;	
	   }	

}
