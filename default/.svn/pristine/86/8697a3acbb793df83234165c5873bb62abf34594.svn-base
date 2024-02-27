package edu.asu.jmars.ui.looknfeel.theme.component;

import java.awt.Color;
import javax.swing.UIManager;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.ThemeProvider;
import edu.asu.jmars.ui.looknfeel.theme.ThemeComponent;
import mdlaf.utils.MaterialImageFactory;


public class ThemeFileChooser implements ThemeComponent {

    private static String catalogKey = "filechooser";

    static {
        GUITheme.getCatalog().put(catalogKey, new ThemeFileChooser());
    }

    public ThemeFileChooser() {
    }

    public static String getCatalogKey() {
        return catalogKey;
    }

    public Color getListimgcolor() {
        return ThemeProvider.getInstance().getImage().getFill();
    }

    public Color getDetailsimgcolor() {
        return ThemeProvider.getInstance().getText().getMain();
    }

    public int getImgscaledwidth() {
        return ThemeProvider.getInstance().getSettings().getFilechooserImgScaledWidth();
    }

    public int getImgscaledheight() {
        return ThemeProvider.getInstance().getSettings().getFilechooserImgScaledHeight();
    }

    @Override
    public void configureUI() {        
        UIManager.put("FileChooser[icons].computer", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.COMPUTER_WHITE));
        UIManager.put("FileChooser[icons].file", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.FILE_WHITE));
        UIManager.put("FileChooser[icons].home", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.HOME_WHITE));
        UIManager.put("FileChooser[icons].directory", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.FOLDER_WHITE));
        UIManager.put("FileChooser[icons].floppyDrive", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.FLOPPY_DRIVE_WHITE));
        UIManager.put("FileChooser[icons].hardDrive", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.HARD_DRIVE_WHITE));        
        UIManager.put("FileChooser[icons].list", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.LIST_WHITE));
        UIManager.put("FileChooser[icons].details", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.DETAILS_WHITE));
        UIManager.put("FileChooser[icons].newFolder", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.NEW_FOLDER_WHITE));
        UIManager.put("FileChooser[icons].upFolder", MaterialImageFactory.getInstance().getImage(MaterialImageFactory.BACK_ARROW_WHITE));     
     }
}
