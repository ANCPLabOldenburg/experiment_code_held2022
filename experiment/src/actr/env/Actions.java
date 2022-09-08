package actr.env;

import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;

import resources.Resources;

class Actions {
	private Core core;
	private Frame frame;

	Action newAction, openAction, closeAction, saveAction, saveAsAction, printAction, aboutAction, quitAction;
	Action undoAction, redoAction, cutAction, copyAction, pasteAction;
	Action findAction, findNextAction, findPreviousAction, findHideAction, prefsAction;
	Action runAction, runAnalysisAction, stopAction, resumeAction;
	Action outputBuffersAction, outputWhyNotAction, outputDMAction, outputPRAction, outputVisiconAction,
			outputTasksAction;

	Actions(final Core core, final Frame frame) {
		this.core = core;
		this.frame = frame;

		runAction = new AbstractAction("Run", Resources.getIcon("jlfPlay16.gif")) {
			public void actionPerformed(ActionEvent e) {
				core.openFrame();
				frame.runAnalysis();
			}
		};
	
	}

	Action createOpenRecentAction(final File file) {
		return new AbstractAction(file.getName()) {
			public void actionPerformed(ActionEvent e) {
				core.openFrame(file);
			}
		};
	}

	void update() {
		if (frame == null)
			return;

		boolean runEnabled = !core.isAnyModelRunning();
		runAction.setEnabled(runEnabled); // && frame.isModelFile());
		
		boolean changed = frame.getDocument().isChanged();
		frame.getRootPane().putClientProperty("Window.documentModified", changed);
		String title = "Driving experiment";
		if (changed)
			title = "*" + title;
		frame.setTitle(title);
	}

	Action createAppletFileAction(final String name) {
		return new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
				try {
					frame.open(new URL(ApplicationMain.getApplet().getCodeBase(), "models/" + name));
				} catch (Exception ex) {
				}
			}
		};
	}
}
