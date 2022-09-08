package actr.tasks.driving;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Observer;

//import javax.lang.model.util.ElementScanner14;

import java.io.File;
import java.awt.Toolkit;


/**
 * The driver's own vehicle and its controls.
 * 
 * @author Dario Salvucci
 */
public class Simcar extends Vehicle {
	Driver driver;

	// mlh indicator
	private double startR = -999;
	private double startL = -999;

	double steerAngle;
	double accelerator;
	double brake;
	long roadIndex;
	Position nearPoint;
	Position farPoint;
	Position carPoint;
	int lane;
	double dist_to_nearest_lane;
	double diffDist;
	String indicator;

	public Simcar(Driver driver, Env env) {
		super();

		this.driver = driver;

		steerAngle = 0;
		accelerator = 0;
		brake = 0;
		speed = 0;
		lane = 2;
	}

	int order = 6;
	int max_order = 10;
	double gravity = 9.8;
	double air_drag_coeff = .25;
	double engine_max_watts = 106000;
	double brake_max_force = 8000;
	double f_surface_friction = .2;
	double lzz = 2618;
	double ms = 1175;
	double a = .946;
	double b = 1.719;
	double caf = 48000;
	double car = 42000;
	double[] y = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double[] dydx = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double[] yout = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	double heading = -999;
	double heading1 = -999;
	double heading2 = -999;
	double car_heading;
	double car_accel_pedal;
	double car_brake_pedal;
	double car_deltaf;
	double car_steer;
	double car_speed;
	double car_ke;

	void derivs(double y[], double dydx[]) {
		double phi = y[1];
		double r = y[2];
		double beta = y[3];
		double ke = y[4];
		double u = (ke > 0) ? Math.sqrt(ke * 2 / ms) : 0;
		double deltar = 0;
		double deltaf = car_deltaf;
		dydx[1] = r;
		if (u > 5) {
			dydx[2] = (2.0 * a * caf * deltaf - 2.0 * b * car * deltar - 2.0 * (a * caf - b * car) * beta
					- (2.0 * (a * a * caf + b * b * car) * r / u)) / lzz;
			dydx[3] = (2.0 * caf * deltaf + 2.0 * car * deltar - 2.0 * (caf + car) * beta
					- (ms * u + (2.0 * (a * caf - b * car) / u)) * r) / (ms * u);
		} else {
			dydx[1] = 0.0;
			dydx[2] = 0.0;
			dydx[3] = 0.0;
		}
		double pengine = car_accel_pedal * engine_max_watts;
		double fbrake = car_brake_pedal * brake_max_force;
		double fdrag = (f_surface_friction * ms * gravity) + (air_drag_coeff * u * u);
		dydx[4] = pengine - fdrag * u - fbrake * u;
		dydx[5] = u * Math.cos(phi);
		dydx[6] = u * Math.sin(phi);
	}

	void rk4(int n, double x, double h) {
		double dym[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double dyt[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double yt[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double hh = h * 5;
		double h6 = h / 6;
		int i;

		for (i = 1; i <= n; i++)
			yt[i] = y[i] + hh * dydx[i];
		derivs(yt, dyt);
		for (i = 1; i <= n; i++)
			yt[i] = y[i] + hh * dyt[i];
		derivs(yt, dym);
		for (i = 1; i <= n; i++) {
			yt[i] = y[i] + h * dym[i];
			dym[i] += dyt[i];
		}
		derivs(yt, dyt);
		for (i = 1; i <= n; i++)
			yout[i] = y[i] + h6 * (dydx[i] + dyt[i] + 2.0 * dym[i]);
	}

	void updateDynamics(Env env) {
		Road road = env.road;
		double time = env.time;
		double sampleTime = Env.sampleTime;

		if (heading2 == -999.0) {

			heading = heading1 = heading2 = Math.atan2(h.z, h.x);
			yout[1] = y[1] = car_heading = heading;
			yout[2] = y[2] = 0.0;
			yout[3] = y[3] = 0.0;
			yout[4] = y[4] = car_ke = 50000; // 0.0; // kinetic energy > 0, otherwise unstable at start
			yout[5] = y[5] = p.x;
			yout[6] = y[6] = p.z;
			if (car_ke > 0.0)
				car_speed = Math.sqrt(2.0 * car_ke / ms);
			else
				car_speed = 0;
		}

		car_steer = env.controls.getSteering();
		car_accel_pedal = env.controls.getAccelerator() > 0 ? env.controls.getAccelerator() : 0;
		car_brake_pedal = env.controls.getAccelerator() < 0 ? -env.controls.getAccelerator() : 0;

		indicator = env.controls.getIndicator();

		if (indicator.equals("right")) {
			startR = env.time;
		} else if (indicator.equals("left")) {
			startL = env.time;
		}

		steerAngle = car_steer;
		accelerator = car_accel_pedal;
		brake = car_brake_pedal;

		// original had lines below; changing to linear steering function
		// if (car_steer < 0.0) car_deltaf = -0.0423 * Math.pow(-1.0*car_steer, 1.3);
		// else car_deltaf = 0.0423 * Math.pow(car_steer,1.3);
		car_deltaf = 0.0423 * car_steer;

		// drift -mh
		double forcing = 0.125 * (0.01 * Math.sin(2.0 * 3.14 * 0.13 * time + 1.137)
				+ 0.005 * Math.sin(2.0 * 3.14 * 0.47 * time + 0.875));
		forcing /= 1; // scale the drift
		car_deltaf += forcing;

		derivs(y, dydx);
		rk4(order, time, sampleTime);

		y[1] = car_heading = yout[1];
		y[2] = yout[2];
		y[3] = yout[3];
		y[4] = car_ke = yout[4];
		y[5] = p.x = yout[5];
		y[6] = p.z = yout[6];

		if (car_ke > 0.0)
			car_speed = Math.sqrt(2.0 * car_ke / ms);
		else
			car_speed = 0.0;

		h.x = Math.cos(car_heading);
		h.z = Math.sin(car_heading);

		heading2 = heading1;
		heading1 = heading;
		heading = car_heading;

		speed = car_speed;
		// speed = Utilities.mph2mps(Utilities.kph2mph(speed));

		long i = Math.max(1, roadIndex);
		long newi = i;
		Position nearloc = (road.middle(i)).subtract(p);
		double norm = (nearloc.x * nearloc.x) + (nearloc.z * nearloc.z); // error in lisp!
		double mindist = norm;
		boolean done = false;
		while (!done) {
			i += 1;
			nearloc = (road.middle(i)).subtract(p);
			norm = (nearloc.x * nearloc.x) + (nearloc.z * nearloc.z); // error in lisp!
			if (norm < mindist) {
				mindist = norm;
				newi = i;
			} else
				done = true;
		}
		Position vec1 = (road.middle(newi)).subtract(p);
		Position vec2 = (road.middle(newi)).subtract(road.middle(newi - 1));
		double dotprod = -((vec1.x * vec2.x) + (vec1.z * vec2.z));
		double fracdelta;
		if (dotprod < 0) {
			newi--;
			fracdelta = 1.0 + dotprod;
		} else
			fracdelta = dotprod;

		fracIndex = newi + fracdelta;
		roadIndex = newi;
		// Updating lane variable.
		this.lane = env.road.vehicleLane(this);

		double distLeft = env.simcar.p.z - env.road.left(env.simcar.fracIndex, lane).z;
		double distRight = env.simcar.p.z - env.road.right(env.simcar.fracIndex, lane).z;
		dist_to_nearest_lane = Utilities.absoluteMin(distLeft, distRight);
		diffDist = Math.abs(distLeft) - Math.abs(distRight); // positive -> should drive to the right
	}

	void update(Env env) {
		updateDynamics(env);

		nearPoint = env.road.nearPoint(this, lane);
		farPoint = env.road.farPoint(this, lane);
		carPoint = env.autocar.p;
	}

	void draw(Graphics g, Env env) {
		int dashHeight = (int) Math.rint(Env.envHeight * 0.32);

		int speedometer_x = (int) Math.rint(Env.envWidth * 0.40);
		int speedometer_y = (int) Math.rint(Env.envHeight * 0.83);

		int top_mirror_x_1 = (int) Math.rint(Env.envWidth * 0.351);
		int top_mirror_x_2 = (int) Math.rint(Env.envWidth * 0.375);
		int top_mirror_x_3 = (int) Math.rint(Env.envWidth * 0.3535);

		// int left_mirror_x_1 = (int) Math.rint(Env.envWidth * 0.137);
		int left_mirror_x_1 = (int) Math.rint(Env.envWidth * 0.05);
		int left_mirror_y_1 = (int) Math.rint(Env.envHeight * 0.625);
		// int left_mirror_x_2 = (int) Math.rint(Env.envWidth * 0.1395);
		int left_mirror_x_2 = (int) left_mirror_x_1 + 5;
		int left_mirror_y_2 = (int) Math.rint(Env.envHeight * 0.630);

		// int right_mirror_x_1 = (int) Math.rint(Env.envWidth * 0.637);
		int right_mirror_x_1 = (int) Math.rint(Env.envWidth * 0.8);
		int right_mirror_y_1 = (int) Math.rint(Env.envHeight * 0.625);
		// int right_mirror_x_2 = (int) Math.rint(Env.envWidth * 0.6395);
		int right_mirror_x_2 = right_mirror_x_1 + 5;
		int right_mirror_y_2 = (int) Math.rint(Env.envHeight * 0.630);

		int rect_x_1 = 0;
		int rect_y_1 = Env.envHeight - dashHeight;
		int rect_width_1 = (int) Math.rint(Env.envWidth * 0.137);
		int rect_height_1 = dashHeight;

		int rect_x_2 = (int) Math.rint(Env.envWidth * 0.684);
		int rect_y_2 = Env.envHeight - dashHeight;
		int rect_width_2 = Env.envWidth;
		int rect_height_2 = dashHeight;

		g.setColor(Color.black);
		g.fillRect(0, Env.envHeight - dashHeight, Env.envWidth, dashHeight);

		// mh - speedometer

		double speedNum = speed;
		String speed = Integer.toString((int) Utilities.mph2kph(Utilities.mps2mph(speedNum)));
		Font myFont = new Font("Helvetica", Font.BOLD, 24);
		g.setFont(myFont);
		g.setColor(Color.WHITE);
		g.drawString(speed, speedometer_x, speedometer_y);

		// top - mirror

		g.setColor(Color.black);
		g.fillRoundRect(top_mirror_x_1, 15, 100, 60, 30, 20);
		g.fillRect(top_mirror_x_2, 0, 10, 20);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(top_mirror_x_3, 20, 90, 50, 30, 20);

		double distance = this.fracIndex - env.autocar.fracIndex;

		// If autocar is behind simcar in same lane, display car in top mirror
		if ((env.autocar.lane == this.lane) && (distance > 0) && (distance <= 80)) {
			// Normalize distance, if distance == 80 (autocar is 80 metres behind simcar),
			// scale is 0.
			double scale = (distance - 80) / (0 - 80);
			int width = (int) Math.rint(scale * 74.0);
			int height = (int) Math.rint(scale * 34.0);
			int pos_x = (int) Math.rint(37 - (scale * 37.0));

			g.setColor(Color.blue);
			g.fillRect(top_mirror_x_3 + pos_x + 8, 20 + 8, width, height);
		}

		// g.fillRoundRect(5, Env.envHeight - dashHeight, 45, 25, 30, 20); side-view

		// left-side mirror
		g.setColor(Color.black);
		g.fillRoundRect(left_mirror_x_1, left_mirror_y_1, 90, 60, 40, 20);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(left_mirror_x_2, left_mirror_y_2, 80, 50, 40, 20);
		// g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);

		// If autocar is behind simcar in left lane, display car in left mirror
		if ((env.autocar.lane < this.lane) && (distance > 0) && (distance <= 80)) {
			double scale = (distance - 80) / (0 - 80);
			int width = (int) Math.rint(scale * 64.0);
			int height = (int) Math.rint(scale * 34.0);
			int pos_x = (int) Math.rint(64 - (scale * 64.0));

			g.setColor(Color.blue);
			g.fillRect(left_mirror_x_2 + pos_x + 8, Env.envHeight - dashHeight - 55 + 8, width, height);
		}

		// right-side mirror
		g.setColor(Color.black);
		g.fillRoundRect(right_mirror_x_1, right_mirror_y_1, 90, 60, 40, 20);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRoundRect(right_mirror_x_2, right_mirror_y_2, 80, 50, 40, 20);

		// If autocar is behind simcar in right lane, display car in right mirror
		if ((env.autocar.lane > this.lane) && (distance > 0) && (distance <= 80)) {
			double scale = (distance - 80) / (0 - 80);
			int width = (int) Math.rint(scale * 64.0);
			int height = (int) Math.rint(scale * 34.0);

			g.setColor(Color.blue);
			g.fillRect(right_mirror_x_1 + 12, Env.envHeight - dashHeight - 55 + 8, width, height);
		}
		g.setColor(new Color(14, 14, 14));
		g.fillRect(rect_x_1, rect_y_1, rect_width_1, rect_height_1);

		g.setColor(new Color(14, 14, 14));
		g.fillRect(rect_x_2, rect_y_2, rect_width_2, rect_height_2);

		if (startR + 4 > env.time) {
			double nS = Math.round(startR * 2) / 2.0;
			double nT = Math.round(env.time * 2) / 2.0;
			if (nS == nT || nS + 1 == nT || nS + 2 == nT)
				drawRightArrow(g);
		}

		if (startL + 4 > env.time) {
			double nS = Math.round(startL * 2) / 2.0;
			double nT = Math.round(env.time * 2) / 2.0;
			if (nS == nT || nS + 1 == nT || nS + 2 == nT)
				drawLeftArrow(g);
		}
	}

	// npk blinkers - draws right arrow in dashboard
	private void drawRightArrow(Graphics g) {
		int right_mirror_x_1 = (int) Math.rint(Env.envWidth * 0.637);
		int speedometer_y = (int) Math.rint(Env.envHeight * 0.83);

		int rightBlinker_x1 = (int) right_mirror_x_1;
		int rightBlinker_y = (int) speedometer_y;

		String cd = new File("").getAbsolutePath();
		String imageURL = cd + "\\src\\resources\\rightArrow.png";
		Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
		g.drawImage(image, rightBlinker_x1, rightBlinker_y - 50, null);
	}

	// npk blinkers - draws left arrow in dashboard
	private void drawLeftArrow(Graphics g) {
		int left_mirror_x_2 = (int) Math.rint(Env.envWidth * 0.141);
		int speedometer_y = (int) Math.rint(Env.envHeight * 0.83);

		int leftBlinker_x1 = (int) left_mirror_x_2;
		int leftBlinker_y = (int) speedometer_y;
		g.setColor(Color.ORANGE);

		String cd = new File("").getAbsolutePath();
		String imageURL = cd + "\\src\\resources\\leftArrow.png";
		Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
		g.drawImage(image, leftBlinker_x1, leftBlinker_y - 50, null);
	}

	double devscale = .0015;
	double devx = -.7;
	double devy = .5;

	double ifc2gl_x(double x) {
		return devx + (devscale * -(x - Driving.centerX));
	}

	double ifc2gl_y(double y) {
		return devy + (devscale * -(y - Driving.centerY));
	}

	double gl2ifc_x(double x) {
		return Driving.centerX - ((x - devx) / devscale);
	}

	double gl2ifc_y(double y) {
		return Driving.centerY - ((y - devy) / devscale);
	}
}
