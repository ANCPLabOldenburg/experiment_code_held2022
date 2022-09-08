package actr.env;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

import resources.Resources;

class Toolbar extends JToolBar {
	private String speedup, iterations;

	interface Setter {
		void set(String s);
	}

	Toolbar(Frame frame, Actions actions) {
		super();

		setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		setFloatable(false);
		setFocusable(false);
		setRollover(true);
		
		addButton(actions.runAction, 20);

		// addSeparator();

		add(Box.createHorizontalGlue());
	}

	JButton addButton(Action action, int width) {
		final JButton button = new JButton(action);
		if (button.getIcon() != null)
			button.setText(null);
		button.setToolTipText((String) action.getValue(Action.NAME));
		button.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				if (button.isEnabled())
					button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			}

			public void mouseExited(MouseEvent e) {
				button.setBorder(null);
			}
		});
		button.setFocusable(false);
		button.setBorder(null);
		button.setPreferredSize(new Dimension(width, 26));
		add(button);
		return button;
	}

	JButton addButton(Action action) {
		return addButton(action, 26);
	}

	JButton addDropButton(String title, final JButton primary, String[][] options, int defaultOption,
			final Setter setter) {
		final JButton button = new JButton(Resources.getIcon("DropArrow16.gif"));
		final JPopupMenu menu = new JPopupMenu();
		JMenuItem titleItem = new JMenuItem(title);
		titleItem.setEnabled(false);
		menu.add(titleItem);
		menu.addSeparator();
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < options.length; i++) {
			final String pair[] = options[i];
			JMenuItem item = new JRadioButtonMenuItem(pair[0]);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setter.set(pair[1]);
				}
			});
			group.add(item);
			menu.add(item);
			if (i == defaultOption) {
				item.setSelected(true);
				setter.set(pair[1]);
			}
		}
		button.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				menu.show(button, 0, button.getHeight());
			}

			public void mouseReleased(MouseEvent e) {
				mousePressed(e);
			}

			public void mouseEntered(MouseEvent e) {
				button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				primary.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			}

			public void mouseExited(MouseEvent e) {
				button.setBorder(null);
				primary.setBorder(null);
			}
		});
		button.setFocusable(false);
		button.setBorder(null);
		button.setPreferredSize(new Dimension(10, 25));
		add(button);
		return button;
	}

	String getSpeedup() {
		return speedup;
	}

	String getIterations() {
		return iterations;
	}
}
