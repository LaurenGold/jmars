package edu.asu.jmars.swing.quick.edit.row;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import edu.asu.jmars.layer.LViewFactory;
import edu.asu.jmars.layer.Layer.LView;
import edu.asu.jmars.swing.quick.menu.command.CommandExecutor;
import edu.asu.jmars.swing.quick.menu.command.QuickMenuCommand;
import static edu.asu.jmars.swing.quick.edit.row.MenuCommand.OPEN;
import static edu.asu.jmars.swing.quick.edit.row.MenuCommand.OPEN_DOCKED;
import static edu.asu.jmars.swing.quick.edit.row.MenuCommand.DELETE;
import static edu.asu.jmars.swing.quick.edit.row.MenuCommand.RENAME;
import static edu.asu.jmars.swing.quick.edit.row.MenuCommand.TOOLTIP;

public class RowQuickAccessPopup extends JPanel {

	private JPopupMenu rowpopup;
	private LView view;
	private JCheckBoxMenuItem tooltip;
	CommandExecutor knowsWhatTodo = new CommandExecutor();

	public RowQuickAccessPopup(LView lview) {

		rowpopup = new JPopupMenu();
		view = lview;	

		ActionListener menuListener = (ActionEvent event) -> {
			String cmd = event.getActionCommand();
			knowsWhatTodo.processRequest(MenuCommand.get(cmd));
		};

		JMenuItem item;
       
		rowpopup.add(item = new JMenuItem(OPEN.getMenuCommand()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		rowpopup.add(item = new JMenuItem(OPEN_DOCKED.getMenuCommand()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);

		rowpopup.add(item = new JMenuItem(DELETE.getMenuCommand()));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);
		if (LViewFactory.cartographyList.contains(view.originatingFactory)) {
			item.setEnabled(false);
		}		

		rowpopup.add(item = new JMenuItem(RENAME.getMenuCommand()));
		item.setHorizontalTextPosition(JMenuItem.RIGHT);
		item.addActionListener(menuListener);		
				
		rowpopup.add(tooltip = new JCheckBoxMenuItem(TOOLTIP.getMenuCommand(), true));
		tooltip.setHorizontalTextPosition(JMenuItem.RIGHT);
		tooltip.addActionListener(menuListener);
		
		buildMenuCommands();
	};
    

	private void buildMenuCommands() {

		CommandReceiver knowsHowTodo = new CommandReceiver();

		QuickMenuCommand openCmd = new OpenCommand(knowsHowTodo);
		QuickMenuCommand opendockedCmd = new OpenDockedCommand(knowsHowTodo);
		QuickMenuCommand renameCmd = new RenameCommand(knowsHowTodo);
		QuickMenuCommand deleteCmd = new DeleteCommand(knowsHowTodo);
		QuickMenuCommand tooltipCmd = new TooltipCommand(knowsHowTodo, tooltip);
		
		knowsWhatTodo.addRequest(OPEN, openCmd);
		knowsWhatTodo.addRequest(OPEN_DOCKED, opendockedCmd);
		knowsWhatTodo.addRequest(RENAME, renameCmd);
		knowsWhatTodo.addRequest(DELETE, deleteCmd);
		knowsWhatTodo.addRequest(TOOLTIP, tooltipCmd);
	}

	public JPopupMenu getPopupmenu() {
		return rowpopup;
	}

}
