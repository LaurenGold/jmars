package edu.asu.jmars.swing.quick.add.layer;

import edu.asu.jmars.Main;
import edu.asu.jmars.layer.LManager;
import edu.asu.jmars.layer.LViewFactory;
import edu.asu.jmars.layer.LayerParameters;
import edu.asu.jmars.layer.map2.MapLViewFactory;
import edu.asu.jmars.layer.map2.custom.CM_Manager;
import edu.asu.jmars.layer.shape2.ShapeFactory;
import edu.asu.jmars.layer.shape2.ShapeLView;

public class CommandReceiver {

	public CommandReceiver() {
	}

	public void loadCustomShapesLayer() {
		LayerParameters l = LayerParameters.lParameters.stream().filter(
				lparam -> "shape".equalsIgnoreCase(lparam.type) 
				&& "Custom Shape Layer".equalsIgnoreCase(lparam.name))
				.findAny().orElse(null);
		if (l != null) {
			ShapeLView shpLView = (ShapeLView) new ShapeFactory().newInstance(false, l);
			LManager.receiveNewLView(shpLView);
			LManager.getLManager().repaint();
		}
	}

	public void loadCustomMaps() {
		CM_Manager mgr = CM_Manager.getInstance();
		mgr.setLocationRelativeTo(Main.mainFrame);
		mgr.setVisible(true);
		mgr.setSelectedTab(CM_Manager.TAB_UPLOAD);
	}

	public void load3DLayer() {
		LayerParameters l = LayerParameters.lParameters.stream().filter(lparam -> "3d".equalsIgnoreCase(lparam.type))
				.findAny().orElse(null);
		if (l != null) {
			LViewFactory factory = LViewFactory.findFactoryType(l.type);
			if (factory != null) {
				factory.createLView(false, l);
				LManager.getLManager().repaint();
			}
		}
	}	

	public void loadAdvancedMap() {
		new MapLViewFactory().createLView(true, Main.mainFrame);
		LManager.getLManager().repaint();		
	}
}
