package actr.env;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

class Menus extends JMenuBar {
	private Actions actions;
	private Preferences prefs;
	private JMenu fileMenu, editMenu, runMenu, outputMenu;
	private JMenu openRecentMenu;
	private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private String appletFiles[] = { "U1Addition.actr", "U1Count.actr", "U1Semantic.actr", "U1Tutor.actr",
			"U2Demo.actr", "U3Sperling.actr", "U4Paired.actr", "U5Fan.actr", "U5Grouped.actr", "U5Siegler.actr",
			"U6BST.actr", "U7Paired.actr" };

	Menus(Actions actions, Preferences prefs) {
		this(actions, prefs, false);
	}

	Menus(Actions actions, Preferences prefs, boolean fileOnly) {
	}

	void addToMenu(JMenu menu, Action action, int vk, int modifiers) {
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(vk, modifiers));
		JMenuItem item = new JMenuItem(action);
		item.setIcon(null);
		menu.add(item);
	}

	void addToMenu(JMenu menu, Action action, int vk) {
		addToMenu(menu, action, vk, accelerator);
	}

	void addToMenu(JMenu menu, Action action) {
		JMenuItem item = new JMenuItem(action);
		item.setIcon(null);
		menu.add(item);
	}
}