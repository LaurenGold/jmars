package edu.asu.jmars.samples.layer.blank;

import static edu.asu.jmars.ui.image.factory.ImageCatalogItem.DOT;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.LViewFactory;
import edu.asu.jmars.layer.SerializedParameters;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.ui.image.factory.ImageFactory;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeImages;

public class BlankLViewFactory extends LViewFactory {

	public LView createLView() {
		return null;
	}

	public void createLView(boolean async) {
		// Create LView with defaults
		BlankLView lview = new BlankLView(new BlankLayer());
		lview.originatingFactory = this;
		LManager.receiveNewLView(lview);
	}

	public LView recreateLView(SerializedParameters parmBlock) {
		BlankLView lview = new BlankLView(new BlankLayer());
		lview.originatingFactory = this;
		return lview;
	}

	public String getDesc() {
		return "Blank LView Description";
	}

	public String getName() {
		return "Blank LView";
	}

	@Override
	public Icon getLayerIcon() {
		Color imgLayerColor = ((ThemeImages) GUITheme.get("images")).getLayerfill();
		return new ImageIcon(ImageFactory.createImage(DOT.withDisplayColor(imgLayerColor)));				
	}

}
