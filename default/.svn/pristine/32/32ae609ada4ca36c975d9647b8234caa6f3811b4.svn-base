package edu.asu.jmars.ui.looknfeel;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import edu.asu.jmars.util.Config;

public class GUIState {
	

	private static final GUIState manager = new GUIState();

	private GUITheme theme;	
	
	public static GUIState getInstance() {
		return manager;
	}

	public GUITheme getGUITheme() {
		return theme;
	}

	public void applyTheme() {
		String uitheme = Config.get("uitheme","dark");
		this.theme = GUITheme.valueOf(uitheme.toUpperCase());
		this.theme.apply();	
	}

	public void configureUI() {
		try {			
			JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(false);
			UIManager.setLookAndFeel("mdlaf.MaterialLookAndFeel");
			applyTheme();			
		} catch (UnsupportedLookAndFeelException e) {
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {}	
	}

	private GUIState() {
	}
}