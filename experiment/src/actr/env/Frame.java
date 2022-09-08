package actr.env;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;

import actr.model.Model;
import actr.model.Utilities;
import actr.task.*;
import actr.tasks.driving.*;
import networking.ServerMain;

import actr.env.NDialog;


/**
 * The class that defines a graphical frame (window) for editing and running an
 * ACT-R model. The frame contains a text editor for the model text, a task
 * panel for displaying the task interface, and an output panel for displaying
 * the text output of the simulation.
 * 
 * @author Dario Salvucci
 */

public class Frame extends JFrame {
	private Frame frame;

	private Core core;
	private Actions actions;
	private Editor editor;
	private JTextArea outputArea;
	private Brain brainPanel;
	private Navigator navigator;
	private FindPanel editorFind, outputFind;
	private JSplitPane splitPane, taskSplitPane;
	private Menus menus;
	private Toolbar toolbar;
	private Trials trials;
	public static Dimension screenSize;
	private File file;
	private Model model;
	private boolean stop;

	Frame(Core core) {
		this(core, null);
		// GL: make sure the EDF file is saved when closing prematurely
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				//ServerMain.participant.endExperiment();
				System.exit(0);
			}
		});
	}

	Frame(final Core core, File file) {

		super("ACT-R");
		frame = this;
		screenSize = new Dimension(1920, 1080);
		this.core = core;

		this.trials = new Trials();
		System.out.println("Trials:");
		for(int i = 0; i < trials.getList().size(); i++)
		{
			System.out.println("N-back level: " + trials.getList().get(i).nBack + "      Construction site: " + trials.getList().get(i).construction);
		}

		actions = new Actions(core, this);

		editor = new Editor(this, core.getPreferences());
		editor.grabFocus();

		JScrollPane editorScroll = new JScrollPane(editor);
		editorScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		editorScroll.setVisible(false);

		navigator = new Navigator(this);
		navigator.setVisible(false);

		editorFind = new FindPanel(this, true);
		editorFind.setVisible(false);

		JPanel modelSubPanel = new JPanel();
		modelSubPanel.setLayout(new BorderLayout(6, 6));
		modelSubPanel.add(editorScroll, BorderLayout.CENTER);
		modelSubPanel.add(editorFind, BorderLayout.SOUTH);
		modelSubPanel.setVisible(false);

		JPanel modelPanel = new JPanel();
		modelPanel.setLayout(new BorderLayout(6, 6));
		modelPanel.add(modelSubPanel, BorderLayout.CENTER);
		modelPanel.add(navigator, BorderLayout.SOUTH);
		modelPanel.setVisible(false);

		outputArea = new JTextArea();
		outputArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // "Monaco"
		outputArea.setLineWrap(false);
		outputArea.setWrapStyleWord(false);
		outputArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				update();
			}

			public void focusLost(FocusEvent e) {
				update();
			}
		});
		outputArea.setVisible(false);

		JScrollPane outputScroll = new JScrollPane(outputArea);
		outputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputScroll.setVisible(false);

		outputFind = new FindPanel(this, false);
		outputFind.setVisible(false);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout(6, 6));
		outputPanel.add(outputScroll, BorderLayout.CENTER);
		outputPanel.add(outputFind, BorderLayout.SOUTH);
		outputPanel.setVisible(false);
		
		taskSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new Task(), outputPanel);
		taskSplitPane.setOneTouchExpandable(false);
		taskSplitPane.setDividerSize(0);
		//taskSplitPane.setOpaque (true);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, modelPanel, taskSplitPane);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerSize(0);

		//splitPane.setOpaque(true); 175 180 	45 40

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);

		brainPanel = new Brain(this);
		brainPanel.setVisible(false);
		getContentPane().add(brainPanel, BorderLayout.EAST);

		toolbar = new Toolbar(this, actions);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		getRootPane().registerKeyboardAction(actions.findHideAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		if (ApplicationMain.inApplication())
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				core.closeFrame(frame);
			}

			public void windowActivated(WindowEvent e) {
				update();
			}

			public void windowDeactivated(WindowEvent e) {
				update();
			}
		});

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				core.getPreferences().frameWidth = frame.getWidth();
				core.getPreferences().frameHeight = frame.getHeight();
			}
		});

		if (file != null)
			open(file);

		pack();
		setSize(core.getPreferences().frameWidth, core.getPreferences().frameHeight);
		setExtendedState(JFrame.MAXIMIZED_BOTH); 

		setVisible(true);

		splitPane.setDividerLocation(core.getPreferences().editorPaneSplit);
		taskSplitPane.setDividerLocation(core.getPreferences().taskPaneSplit);
		repaint();

		setVisible(true);

		update();
	}

	Actions getActions() {
		return actions;
	}

	Menus getMenus() {
		return menus;
	}

	Toolbar getToolbar() {
		return toolbar;
	}

	JTextArea getOutputArea() {
		return outputArea;
	}



	/**
	 * Gets the file associated with the frame.
	 * 
	 * @return the file, or <tt>null</tt> if there is none (e.g., the editor has not
	 *         been saved)
	 */
	public File getFile() {
		return file;
	}

	boolean isModelFile() {
		return file != null && file.getName().endsWith(".actr");
	}

	boolean isBatchFile() {
		return file != null && file.getName().endsWith(".batch");
	}

	Editor getEditor() {
		return editor;
	}

	Document getDocument() {
		return (editor != null) ? editor.getModelDocument() : null;
	}

	Model getModel() {
		return model;
	}

	Task getTask() {
		return (model != null) ? model.getTask() : null;
	}

	Navigator getNavigator() {
		return navigator;
	}

	String getShortName() {
		String name = getFileName();
		if (name.indexOf(".actr") >= 0)
			name = name.substring(0, name.indexOf(".actr"));
		return name;
	}

	String getFileName() {
		if (file == null)
			return "Untitled";
		else
			return file.getName();
	}

	String getFilePath() {
		if (file == null)
			return "Untitled";
		return file.getPath();
	}

	void showTask(Task task) {
		int divider = taskSplitPane.getDividerLocation();
		taskSplitPane.setTopComponent(task);
		taskSplitPane.setDividerLocation(divider);
		repaint();

	}

	void hideTask() {
		// int divider = taskSplitPane.getDividerLocation();
		// taskSplitPane.setTopComponent (new JPanel());
		// taskSplitPane.setDividerLocation (divider);
		// repaint();
	}

	void setDefaultButton(JButton button) {
		getRootPane().setDefaultButton(button);
	}

	/**
	 * Opens the given file in the frame.
	 * 
	 * @param file            the file
	 * @param suppressStyling true to disable coloring and indenting (for batch
	 *                        processing)
	 */
	public void open(File file, boolean suppressStyling) {
		this.file = file;
		if (editor.open(file, suppressStyling))
			getRootPane().putClientProperty("Window.documentFile", file);
		else
			this.file = null;
		editor.grabFocus();
		model = null;
		update();
	}

	/**
	 * Opens the given file in the frame.
	 * 
	 * @param file the file
	 */
	public void open(File file) {
		open(file, false);
	}

	/**
	 * Opens the given URL in the frame.
	 * 
	 * @param url the URL
	 */
	public void open(URL url) {
		this.file = new File(url.getPath());
		;
		editor.open(url);
		editor.grabFocus();
		model = null;
		update();
	}

	/**
	 * Runs the model currently loaded into the editor.
	 */
	public void run() {
		run(true);
	}

	/**
	 * Runs the model currently loaded into the editor, possibly resetting it before
	 * running.
	 * 
	 * @param reset flag indicating whether or not to recompile the model before
	 *              running
	 */
	public void run(final boolean reset) {
		if (isBatchFile()) {
			runBatch(true);
			return;
		}

		final String modelText = editor.getText();
		final String speedup = "1";
		(new SwingWorker<Object, Object>() {
			public Object doInBackground() {
				if (!core.acquireLock(frame))
					return null;
				update();
				if (reset) {
					clearOutput();
					output("> (run)\n");
					editor.clearMarkers();
					model = Model.compile(modelText, frame);
					editor.addMarkers(model.getErrors(), true);
					if (model.hasFatalErrors())
						model = null;
				} else
					output("\n> (resume)\n");
				if (model != null) {
					brainPanel.setVisible(model.getBold().isImaging());
					showTask(model.getTask());
					if (!speedup.equals(""))
						model.setParameter(":real-time", speedup);
					model.run(reset, 0, false, 0);
					hideTask();
				}
				core.releaseLock(frame);
				update();
				return null;
			}
		}).execute();
	}

	/**
	 * Runs an analysis of the current model. The analysis runs the model the number
	 * of times defined by the task's <tt>analysisIterations()</tt> method and then
	 * runs the task's <tt>analyze()</tt> method on all the iterations of the task,
	 * printing the analysis output to the frame's output panel.
	 */

	/**
	 * @MdM edited: the runAnalysis method is now used to run multiple simulations in a row.
	 */
	public void runAnalysis() {
		final String modelText = editor.getText();
		model = Model.compile(modelText, frame);
		if (model == null)
			return;
		(new SwingWorker<Object, Object>() {
			public Object doInBackground() {
				if (!core.acquireLock(frame))
					return null;
				stop = false;
				ServerMain.participant.prepareExperiment();
				String welcomeMessage = "<html>Welcome!<br><br>You will be driving on a three lane highway. Speed signs will show up at different points.<br>Your task is to drive according to the speed limit from n speed signs ago, where n is either 0, 1, 2, 3, or 4.<br>You will have to memorize the order of the speed limits as they appear.<br><br>0-back trial: simply drive the speed limit presented on the speed sign you see.<br>1-back trial: drive according to the speed limit presented 1 speed sign ago.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This means you need to memorize the previous speed sign each time you see one.<br>2-back trial: drive according to the speed limit from 2 speed signs ago.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Again this means you need to memorize speed limits in the order they are presented,<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;which you will have to do for the 3-back trial and the 4-back trial too.<br>3-back trial: drive according to the speed limit from 3 speed signs ago.<br>4-back trial: drive according to the speed limit from 4 speed signs ago.<br><br>Before each trial you will be told which n-back trial will be tested.<br>The right pedal is the accelerator and the left pedal is the break.<br><br>First you will drive a practice round with a 2-back trial.<br>Press X to start.</html>";
				new NDialog(frame, welcomeMessage, "Instructions", new Dimension(360, 200));
				update();
				clearOutput();
				if (model != null && model.getTask() != null) {

					// Perform 1 practice trial, 2-back task, only 5 speed-signs.
					
					String title_message = "Practice trial";
					String content_message = "In this practice trial, you will perform a 2-back task.";
					
					new NDialog(frame, content_message, title_message, new Dimension(200, 100));
/*
					model = Model.compile(modelText, frame);
					showTask(model.getTask());
					model.setParameter(":real-time", "1");
					model.run(false, 2, true, -1);
					model.getTask().finish(); */
					
					// This loop runs 10 recorded iterations of the driving-simulation.
					for (int i = 0; !stop && i < trials.getList().size(); i++) {
						if(i == 10)
						{
							title_message = "Break";
							content_message = "This is the halfway point of the experiment, you can now take a break.";
							new NDialog(frame, content_message, title_message, new Dimension(200, 100));
						}
						boolean construction = trials.getList().get(i).construction;
						int nBack = trials.getList().get(i).nBack;
						ServerMain.participant.prepareTrial(i, construction, nBack);
						title_message = "Trial " + (i+1) + " - Progress: " + Math.round((i) * 100.0 / trials.getList().size()) + "% / 100%";
						content_message = "In the next trial, you will perform a " + nBack + "-back task.";
						new NDialog(frame, content_message, title_message, new Dimension(200, 100));
						model = Model.compile(modelText, frame);
						showTask(model.getTask());
						model.setParameter(":real-time", "1");
						ServerMain.participant.startRecording();
						model.run(construction, nBack, false, i);
						ServerMain.participant.endTrial();
						model.getTask().finish();

						title_message = "Drift";
						content_message = "Please press the button once to start drift correction.";
						new NDialog(frame, content_message, title_message, new Dimension(200, 100));
						ServerMain.participant.doDriftCorrection();
					}
					// model = null;
					ServerMain.participant.endExperiment();
					hideTask();
					title_message = "End";
					content_message = "This is the end of the experiment, thanks for participating!";
					new NDialog(frame, content_message, title_message, new Dimension(200, 100));
				}
				core.releaseLock(frame);
				update();
				return null;
			};
		}).execute();
	}


	void runBatch(final boolean output) {
		(new SwingWorker<Object, Object>() {
			public Object doInBackground() {
				if (file == null)
					return null;
				if (!closing())
					return null;
				if (!core.acquireLock(frame))
					return null;
				stop = false;
				File savedFile = file;
				update();
				clearOutput();
				output("> (run-batch)\n");
				output(Result.headerString());
				String basePath = file.getPath();
				basePath = basePath.substring(0, basePath.lastIndexOf(File.separator)) + File.separator;
				String[] filenames = editor.getText().split("\\s");
				if (filenames != null) {
					for (int k = 0; !stop && k < filenames.length; k++) {
						String line = filenames[k];
						String modelName = line;
						String taskOverrides[] = { null };
						if (line.contains(":")) {
							modelName = line.substring(0, line.indexOf(":"));
							taskOverrides = line.substring(line.indexOf(":") + 1).split(",");
						}
						open(new File(basePath + modelName), true);
						final String modelText = editor.getText();

						for (int ti = 0; ti < taskOverrides.length; ti++) {
							String taskOverride = taskOverrides[ti];
							model = Model.compile(modelText, frame, taskOverride);
							if (model == null) {
								output("Error: Model " + basePath + modelName + " does not exist");
								break;
							}
							int n = model.getTask().analysisIterations();
							Task[] tasks = new Task[n];
							for (int i = 0; !stop && i < n; i++) {
								model = Model.compile(modelText, frame, taskOverride);
								if (model == null)
									System.exit(1);
								brainPanel.setVisible(false);
								showTask(model.getTask());
								model.setParameter(":real-time", "nil");
								model.setParameter(":v", "nil");
								model.run(true, 0, false, 0);
								model.getTask().finish();
								tasks[i] = model.getTask();
							}
							if (!stop && model != null) {
								Task task = model.getTask();
								Result result = task.analyze(tasks, output);
								output(result.toString());
							}
						}
					}
				}
				model = null;
				hideTask();
				open(savedFile);
				core.releaseLock(frame);
				update();
				return null;
			}
		}).execute();
	}


	/**
	 * Stops the model simulation at the earliest time possible.
	 */
	public void stop() {
		if (core.hasLock(frame)) {
			if (model != null)
				model.stop();
			stop = true;
		}
	}

	public void resume() {
		if (model != null)
			run(false);
	}
	/**
	 * Resumes the previous model simulation if possible (equivalent to
	 * <tt>run(false)</tt>).
	 */

	void save(boolean forceSaveAs) {
		if (ApplicationMain.inApplet())
			return;

		try {
			if (forceSaveAs || file == null) {
				File newFile = null;
				while (newFile == null) {
					FileDialog fileDialog = new FileDialog(frame, "Save As...", FileDialog.SAVE);
					if (file != null)
						fileDialog.setDirectory(file.getPath());
					else
						fileDialog.setDirectory(core.getPreferences().getMostRecentPath());
					fileDialog.setVisible(true);
					if (fileDialog.getFile() == null)
						return;
					String filename = fileDialog.getDirectory() + fileDialog.getFile();
					if (filename.indexOf('.') != -1)
						filename = filename.substring(0, filename.indexOf('.'));
					filename += ".actr";
					newFile = new File(filename);
					if (newFile.exists()) {
						String options[] = { "Yes", "No", "Cancel" };
						int choice = JOptionPane.showOptionDialog(frame,
								"Overwrite existing \"" + newFile.getName() + "\"?", "File Exists",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
						if (choice == 1)
							newFile = null;
						else if (choice == 2)
							return;
					}
				}
				file = newFile;
			}
			if (file == null)
				return;
			StringReader sr = new StringReader(editor.getText());
			FileWriter outputStream = null;
			outputStream = new FileWriter(file);
			int c;
			while ((c = sr.read()) != -1)
				outputStream.write(c);
			outputStream.close();
			getDocument().setChanged(false);
			getDocument().noteSave();
			update();
		} catch (IOException exc) {
		}
	}

	void find() {
		if (editor.hasFocus() || editorFind.hasFocus()) {
			editorFind.setVisible(true);
			editorFind.grabFocus();
			navigator.setVisible(false);
			outputFind.setVisible(false);
		} else {
			outputFind.setVisible(true);
			outputFind.grabFocus();
			editorFind.setVisible(false);
			navigator.setVisible(true);
		}
		update();
	}

	void findNext() {
		if (editorFind.isVisible() || editor.hasFocus())
			editorFind.findNext();
		else
			outputFind.findNext();
	}

	void findPrevious() {
		if (editorFind.isVisible() || editor.hasFocus())
			editorFind.findPrevious();
		else
			outputFind.findPrevious();
	}

	void findHide() {
		if (editorFind.isVisible())
			editor.grabFocus();
		if (outputFind.isVisible())
			outputArea.grabFocus();
		editorFind.setVisible(false);
		navigator.setVisible(true);
		outputFind.setVisible(false);
	}

	boolean isFindNextPossible() {
		if (editorFind.isVisible() || editor.hasFocus())
			return editorFind.isFindNextPossible();
		else if (outputFind.isVisible() || outputArea.hasFocus())
			return outputFind.isFindNextPossible();
		else
			return false;
	}

	void print() {
		try {
			editor.getModelDocument().changeFontSize(-3);
			editor.print();
			editor.getModelDocument().changeFontSize(0);
			// Editor printEditor = (Editor) editor.createCopy();
			// printEditor.getModelDocument().changeFontSize (3);
			// printEditor.print();
		} catch (Exception e) {
			output("Print error");
		}
	}

	void refresh() {
		getDocument().resetStyles();
		getDocument().restyle();
		repaint();
	}

	boolean closing() {
		if (model != null && core.hasLock(frame))
			stop();
		if (ApplicationMain.inApplication() && getDocument().isChanged()) {
			SaveDialog saveDialog = new SaveDialog(frame, getFileName());
			if (saveDialog.cancel)
				return false;
			else if (saveDialog.save)
				save(false);
		}
		return true;
	}

	boolean close() {
		if (!closing())
			return false;
		core.getPreferences().frameWidth = getWidth();
		core.getPreferences().frameHeight = getHeight();
		core.getPreferences().editorPaneSplit = splitPane.getDividerLocation();
		core.getPreferences().taskPaneSplit = taskSplitPane.getDividerLocation();
		core.getPreferences().save();
		return true;
	}

	void update() {
		actions.update();
		repaint();
	}

	/**
	 * Prints the given string to the frame's output panel.
	 * 
	 * @param s the string
	 */
	public void output(String s) {
		outputArea.append(s + "\n");
		outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}

	/**
	 * Clears the frame's output panel.
	 */
	public void clearOutput() {
		outputArea.setText("");
	}

	/**
	 * Prints the current contents of the model buffers.
	 */
	public void outputBuffers() {
		output("\n> (buffers)\n");
		if (model != null)
			model.outputBuffers();
	}

	/**
	 * Prints the model's current "why not" trace.
	 */
	public void outputWhyNot() {
		output("\n> (why-not)\n");
		if (model != null)
			model.outputWhyNot();
	}

	/**
	 * Prints the model's current declarative memory.
	 */
	public void outputDeclarative() {
		output("\n> (dm)\n");
		if (model != null)
			model.outputDeclarative();
	}

	/**
	 * Prints the model's current production rules.
	 */
	public void outputProcedural() {
		output("\n> (p)\n");
		if (model != null)
			model.outputProcedural();
	}

	/**
	 * Prints the model's current visual objects.
	 */
	public void outputVisualObjects() {
		output("\n> (visual-objects)\n");
		if (model != null)
			model.outputVisualObjects();
	}

	/**
	 * Prints all implemented task classes.
	 */
	public void outputTasks() {
		output("\n> (all-tasks)\n");
		String[] tasks = Task.allTaskClasses();
		for (int i = 0; i < tasks.length; i++)
			output(tasks[i]);
	}

	/**
	 * Repaints the simulated brain.
	 */
	public void updateVisuals() {
		brainPanel.repaint();
	}

	public Trials getTrials()
	{
		return trials;
	}

}