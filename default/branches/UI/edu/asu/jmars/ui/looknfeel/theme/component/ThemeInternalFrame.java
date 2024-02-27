package edu.asu.jmars.ui.looknfeel.theme.component;

import javax.swing.UIManager;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.ThemeFont;
import edu.asu.jmars.ui.looknfeel.theme.ThemeComponent;
import mdlaf.utils.MaterialImageFactory;


public class ThemeInternalFrame implements ThemeComponent {

	private static String catalogKey = "internalframe";

	static {
		GUITheme.getCatalog().put(catalogKey, new ThemeInternalFrame());
	}

	public ThemeInternalFrame() {
	}

	public static String getCatalogKey() {
		return catalogKey;
	}	

	@Override
	public void configureUI() {			   
		UIManager.put("InternalFrame.titleFont", ThemeFont.getBold());
		UIManager.put("InternalFrame.closeIcon", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.CLOSE_WINDOWS_BLACK));		
	}
}


