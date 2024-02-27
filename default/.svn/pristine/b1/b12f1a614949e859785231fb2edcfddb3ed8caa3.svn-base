package edu.asu.jmars.swing.quick.add.layer;

import static edu.asu.jmars.ui.image.factory.ImageCatalogItem.MAP_LAYER_IMG;
import static edu.asu.jmars.ui.image.factory.ImageCatalogItem.SHAPE_LAYER_IMG;
import static edu.asu.jmars.ui.image.factory.ImageCatalogItem.THREE_D_LAYER_IMG;
import static edu.asu.jmars.swing.quick.add.layer.MenuCommand.ADVANCED_MAP;
import static edu.asu.jmars.swing.quick.add.layer.MenuCommand.CUSTOM_MAPS;
import static edu.asu.jmars.swing.quick.add.layer.MenuCommand.CUSTOM_SHAPES;
import static edu.asu.jmars.swing.quick.add.layer.MenuCommand.THREE_D;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import edu.asu.jmars.Main;
import edu.asu.jmars.swing.quick.menu.command.CommandExecutor;
import edu.asu.jmars.swing.quick.menu.command.QuickMenuCommand;
import edu.asu.jmars.ui.image.factory.ImageFactory;
import edu.asu.jmars.ui.looknfeel.GUITheme;
import edu.asu.jmars.ui.looknfeel.theme.component.ThemeImages;



public class LayerQuickAccessPopup extends JPanel {

	private JPopupMenu lqapopup;
	private Color imgLayerColor = ((ThemeImages) GUITheme.get("images")).getLayerfill();
	CommandExecutor knowsWhatTodo = new CommandExecutor();
	
	public LayerQuickAccessPopup() {

		lqapopup = new JPopupMenu();
		
		buildMenuCommands();

		ActionListener menuListener = (ActionEvent event) ->  {
			String cmd = event.getActionCommand();
			knowsWhatTodo.processRequest(MenuCommand.get(cmd));			
		};		
		
		JMenuItem item;
		
		ImageIcon menuicon = new ImageIcon(ImageFactory.createImage(MAP_LAYER_IMG
			    			 .withDisplayColor(imgLayerColor)));                
		lqapopup.add(item = new JMenuItem(CUSTOM_MAPS.getMenuCommand(), menuicon));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);		
		item.addActionListener(menuListener);
		//disable item if the user is not logged in
		if(Main.USER == null || "".equals(Main.USER.trim())) {
		    item.setEnabled(false);
		}

		menuicon = new ImageIcon(ImageFactory.createImage(SHAPE_LAYER_IMG
   			 .withDisplayColor(imgLayerColor))); 
		lqapopup.add(item = new JMenuItem(CUSTOM_SHAPES.getMenuCommand(),menuicon));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		menuicon = new ImageIcon(ImageFactory.createImage(THREE_D_LAYER_IMG
	   			 .withDisplayColor(imgLayerColor))); 
		lqapopup.add(item = new JMenuItem(THREE_D.getMenuCommand(), menuicon));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);		

		menuicon = new ImageIcon(ImageFactory.createImage(MAP_LAYER_IMG
   			 .withDisplayColor(imgLayerColor)));                
		lqapopup.add(item = new JMenuItem(ADVANCED_MAP.getMenuCommand(), menuicon));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		lqapopup.setLabel("Layer Quick Access");
		lqapopup.setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	private void buildMenuCommands() {
		
		CommandReceiver knowsHowTodo = new CommandReceiver(); 
		
		QuickMenuCommand customShapesCmd = new CustomShapesCommand(knowsHowTodo);
		QuickMenuCommand customMapsCmd = new CustomMapsCommand(knowsHowTodo);
		QuickMenuCommand threeDCmd = new ThreeDCommand(knowsHowTodo);
		QuickMenuCommand advancedMapCmd = new AdvancedMapCommand(knowsHowTodo); 
		
		knowsWhatTodo.addRequest(CUSTOM_SHAPES, customShapesCmd);
		knowsWhatTodo.addRequest(CUSTOM_MAPS, customMapsCmd);
		knowsWhatTodo.addRequest(THREE_D, threeDCmd);
		knowsWhatTodo.addRequest(ADVANCED_MAP, advancedMapCmd);
	}

	public JPopupMenu getPopupmenu() {
		return lqapopup;
	}


	public static void main(String s[]) {
		JFrame frame = new JFrame("Popup Menu Example");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new LayerQuickAccessPopup());
		frame.setSize(300, 300);
		frame.setVisible(true);
	}
}
