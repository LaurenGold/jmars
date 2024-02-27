package edu.asu.jmars.swing.quick.add.layer;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import edu.asu.jmars.swing.quick.menu.command.EnumStubInterface;

//following Joshua Bloch's "Effective Java" enum impl
public enum MenuCommand implements EnumStubInterface {

	CUSTOM_MAPS("Custom Maps"), 
	CUSTOM_SHAPES("Custom Shape Layer"), 
	THREE_D("3D Layer"), 
	ADVANCED_MAP("Advanced Map Layer");
	

	private String menuCommand;
	private static Map<String, MenuCommand> commands = new ConcurrentHashMap<>();

	public String getMenuCommand() {
		return menuCommand;
	}

	MenuCommand(String command) {
		this.menuCommand = command;
	}

	static {
		Map<String,MenuCommand> map = new ConcurrentHashMap<>();
		for (MenuCommand instance : MenuCommand.values()) {
			map.put(instance.getMenuCommand(), instance);
		}
		commands = Collections.unmodifiableMap(map);
	}

	public static MenuCommand get(String name) {
		return commands.get(name);
	}
}
