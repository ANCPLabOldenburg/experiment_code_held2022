package actr.tasks.driving;

/**
 * A class that defines the particular scenario represented by the driving
 * environment.
 * 
 * @author Dario Salvucci
 */
public class Scenario {

	boolean curvedRoad = false;
	int drivingMinutes = 15;
	int timeBetweenTrials = 240;
	boolean baselineOnly = false;
	double[] lanewidth = {2.5, 3.5}; // {highway, construction} (3.5m Bundesstraße, 3.66m (12ft) US highways)
	int blockLength = 40000;
	int blocks = 10;
	boolean construction = false;

	Scenario() {
	}

	String writeString() {
		String s = new String("");
		s += ((curvedRoad) ? 1 : 0) + "\t";
		s += drivingMinutes + "\t";
		s += timeBetweenTrials;
		return s;
	}

	// static Scenario readString (MyStringTokenizer st)
	// {
	// Scenario s = new Scenario();
	// s.curvedRoad = (st.nextInt() == 1);
	// s.simCarConstantSpeed = (st.nextInt() == 1);
	// s.simCarMPH = st.nextInt();
	// s.leadCarConstantSpeed = (st.nextInt() == 1);
	// s.leadCarMPH = st.nextInt();
	// s.leadCarBrakes = (st.nextInt() == 1);
	// s.drivingMinutes = st.nextInt();
	// s.timeBetweenTrials = st.nextInt();
	// return s;
	// }
}
