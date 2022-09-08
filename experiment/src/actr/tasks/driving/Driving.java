package actr.tasks.driving;
import actr.env.Condition;
import actr.env.Frame;

import java.awt.BorderLayout;
// import java.io.BufferedWriter;
// import java.io.FileWriter;
// import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;

import actr.model.Model;
import actr.task.Result;
import actr.task.Task;
import actr.env.Frame;

/**
 * The main Driving task class that sets up the simulation and starts periodic
 * updates.
 * 
 * @author Dario Salvucci
 */
public class Driving extends actr.task.Task {
	static Simulator simulator = null;
	Frame frame;
	

	Simulation simulation;
	JLabel nearLabel, farLabel, signLabel, carLabel, speedoLabel, leftmirrorLabel, rightmirrorLabel, leftLaneLabel,
			rightLaneLabel, construction;

	final double scale = 0.6; // .85 //.6
	final double steerFactor_dfa = (16 * scale);
	final double steerFactor_dna = (4 * scale);
	final double steerFactor_na = (3 * scale);
	final double steerFactor_fa = (0 * scale);
	final double accelFactor_thw = (1 * .40);
	final double accelFactor_dthw = (3 * .40);
	final double steerNaMax = .07;
	final double thwFollow = 1.0;
	final double thwMax = 5.0;

	double startTime = 0, endTime = 170;
	double accelBrake = 0, speed = 0;

	static int minX = 174, maxX = (238 + 24), minY = 94, maxY = (262 + 32);
	static int centerX = (minX + maxX) / 2, centerY = (minY + maxY) / 2;

	String previousLimit = "0";
	int nback_count = 0;
	double signOnset = 0;
	double instructionsOnset = 0;
	double warningOnset = 0;
	static boolean instructionsSeen = false;
	static boolean warningSeen = false;
	static int speedI = 0;
	static String currentNBack = "";
	String[] nBack_list = { "2back", "3back", "0back", "1back", "4back", "0back", "3back", "4back", "1back", "2back" };
	double sign_count = 0;
	int rehearsal_count = 0;
	static String imaginedSpeedlimit = "";
	List<String> output = new ArrayList<String>();
	int block;
	boolean con;
	int nBack;
	boolean practice;
	int trialNum;

	public Driving() {
		super();
		nearLabel = new JLabel(".");
		farLabel = new JLabel("X");
		carLabel = new JLabel("Car");
		signLabel = new JLabel("Sign");
		speedoLabel = new JLabel("Speed");
		leftmirrorLabel = new JLabel("Lmirror");
		rightmirrorLabel = new JLabel("Rmirror");
		leftLaneLabel = new JLabel("lLane");
		rightLaneLabel = new JLabel("rLane");
		construction = new JLabel("Cons");
	}

	public void start(boolean con, int n, boolean practice, int trialNum) {
		this.con = con;
		this.nBack = n;
		this.practice = practice;
		this.trialNum = trialNum;
		endTime += practice ? -60 : 20*n;

		System.out.println("Endtime is: " + endTime);
		simulation = new Simulation(getModel(), con);
		setLayout(new BorderLayout());
		simulator = new Simulator();
		add(simulator, BorderLayout.CENTER);
		simulator.useSimulation(simulation);

		// they don't reset otherwise if you run the model multiple times in a row
		accelBrake = 0;
		previousLimit = "60";
		addPeriodicUpdate(Env.sampleTime);
	}

	Simulation getSim(){
		return this.simulation;
	}

	public void update(double time) {
		if (time <= endTime) {
			simulation.env.time = time - startTime;
			simulation.update();
		} else {
			getModel().stop();
			output("myData", simulation.samples);
		}
	}

	// save to file
	void output(String filename, Vector<Sample> samples) {
		for (int i = 1; i < samples.size(); i++) {
			Sample s = samples.elementAt(i);
			if (i == 1) {
				output.add(s.listVarsSep() + System.lineSeparator());
				output.add(s.toStringSep() + System.lineSeparator());
			} else
				output.add(s.toStringSep() + System.lineSeparator());
		}
		Model.print(output, con, nBack, practice, trialNum);
	}


}
