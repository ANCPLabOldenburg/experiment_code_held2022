package actr.tasks.driving;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Vector;

/**
 * The primary class that defines a road.
 * 
 * @author Dario Salvucci
 */
// public class Road
public class Road extends Driving {
	static Vector<Segment> segments = null;
	int lanes = 3;
	boolean construction;
	public Road(boolean con)
	{
		this.construction = con;
	}
	public class Segment {
		Position left, middle, right;
		Position h;
		Position l_left, r_right;
		Position ll_left, rr_right;
		Position lll_left, rrr_right;
		Position ll_mid, lr_mid, rl_mid, rr_mid;
		Position l_lmid, r_lmid, l_rmid, r_rmid;

		Segment(double a1, double a2, double a3, double a4, double a5, double a6) {
			left = new Position(a1, a2);
			middle = new Position(a3, a4);
			right = new Position(a5, a6);

			h = new Position(right.x - left.x, right.z - left.z);
			h = h.normalize();

			double HALF_STRIPW = 0.04; // 0.08
			double STRIPW = (2 * HALF_STRIPW);
			double SHOULDER = 1.5;
			double WALL = 40;

			double dx = .05 * h.x;
			double dz = .05 * h.z;

			l_left = new Position(left.x - 2 * STRIPW * h.x - dx, left.z - 2 * STRIPW * h.z - dz);
			r_right = new Position(right.x + 2 * STRIPW * h.x + dx, right.z + 2 * STRIPW * h.z + dz);

			ll_left = new Position(left.x - SHOULDER * h.x - dx, left.z - SHOULDER * h.z - dz);
			rr_right = new Position(right.x + SHOULDER * h.x + dx, right.z + SHOULDER * h.z + dz);

			lll_left = new Position(left.x - WALL * h.x, left.z - WALL * h.z);
			rrr_right = new Position(right.x + WALL * h.x, right.z + WALL * h.z);

			ll_mid = new Position(middle.x - 3 * HALF_STRIPW * h.x, middle.z - 3 * HALF_STRIPW * h.z);
			lr_mid = new Position(middle.x - HALF_STRIPW * h.x, middle.z - HALF_STRIPW * h.z);
			rl_mid = new Position(middle.x + HALF_STRIPW * h.x, middle.z + HALF_STRIPW * h.z);
			rr_mid = new Position(middle.x + 3 * HALF_STRIPW * h.x, middle.z + 3 * HALF_STRIPW * h.z);

			if (lanes == 4) {
				l_lmid = new Position((0.5 * ll_mid.x + 0.5 * left.x) - HALF_STRIPW * h.x,
						(0.5 * ll_mid.z + 0.5 * left.z) - HALF_STRIPW * h.z);
				r_lmid = new Position((0.5 * ll_mid.x + 0.5 * left.x) + HALF_STRIPW * h.x,
						(0.5 * ll_mid.z + 0.5 * left.z) + HALF_STRIPW * h.z);

				l_rmid = new Position((0.5 * rr_mid.x + 0.5 * right.x) - HALF_STRIPW * h.x,
						(0.5 * rr_mid.z + 0.5 * right.z) - HALF_STRIPW * h.z);
				r_rmid = new Position((0.5 * rr_mid.x + 0.5 * right.x) + HALF_STRIPW * h.x,
						(0.5 * rr_mid.z + 0.5 * right.z) + HALF_STRIPW * h.z);
			} else if (lanes == 3) {
				l_lmid = new Position((0.666 * middle.x + 0.334 * left.x) - HALF_STRIPW * h.x,
						(0.666 * middle.z + 0.334 * left.z) - HALF_STRIPW * h.z);
				r_lmid = new Position((0.666 * middle.x + 0.334 * left.x) + HALF_STRIPW * h.x,
						(0.666 * middle.z + 0.334 * left.z) + HALF_STRIPW * h.z);

				l_rmid = new Position((0.666 * middle.x + 0.334 * right.x) - HALF_STRIPW * h.x,
						(0.666 * middle.z + 0.334 * right.z) - HALF_STRIPW * h.z);
				r_rmid = new Position((0.666 * middle.x + 0.334 * right.x) + HALF_STRIPW * h.x,
						(0.666 * middle.z + 0.334 * right.z) + HALF_STRIPW * h.z);
			}
		}
	}

	void startup() {
		boolean curved = Env.scenario.curvedRoad;
		segments = new Vector<Segment>();

		Position p = new Position(0.0, 0.0);
		Position h = new Position(1.0, 0.0);

		h = h.normalize();

		int seglen = 200;
		int segcount = 0;
		boolean curving = false;
		double da = 0;
		double dascale = .02;

		// j = blocks
		for (int j = 0; j <= 25; j++) {
			double lanewidth;
			if(construction)
			{
				lanewidth = Env.scenario.lanewidth[1];
			}
			else
			{
				lanewidth = Env.scenario.lanewidth[0];
			}

			for (int i = 1; i <= Env.scenario.block_length; i++) {
				double d = lanes * lanewidth / 2.0;

				if (segcount >= seglen) {
					segcount = 0;
					seglen = 100;
					if (curved) {
						curving = !curving;
						if (curving)
							da = ((da > 0) ? -1 : +1) * dascale * 17; // (i % 17);
					}
				}
				if (curving)
					h = h.rotate(da);
				p = p.add(h);
				Segment s = new Segment(p.x + d * h.z, p.z - d * h.x, p.x, p.z, p.x - d * h.z, p.z + d * h.x);
				segments.addElement(s);
				segcount++;
			}
		}
	}

	void addSegments(Vector<Segment> segments) {

	}

	static Segment getSegment(int i) {
		return (Segment) (segments.elementAt(i));
	}

	static Position location(double fracIndex, double lanePos) {
		int i = (int) (Math.floor(fracIndex));
		double r = fracIndex - i;
		double laner = (lanePos - 1) / 3;
		if (i == fracIndex) {
			Position locL = getSegment(i).left;
			Position locR = getSegment(i).right;
			return locL.average(locR, laner);
		} else {
			Position loc1L = getSegment(i).left;
			Position loc1R = getSegment(i).right;
			Position loc1 = loc1L.average(loc1R, laner);
			Position loc2L = getSegment(i + 1).left;
			Position loc2R = getSegment(i + 1).right;
			Position loc2 = loc2L.average(loc2R, laner);
			return loc1.average(loc2, r);
		}
	}

	Position left(double fracIndex) {
		return location(fracIndex, 4);
	}

	Position left(double fracIndex, int lane) {
		return location(fracIndex, lane);
	}

	Position middle(double fracIndex) {
		return location(fracIndex, 2.5);
	}

	Position middle(double fracIndex, int lane) {
		return location(fracIndex, lane + .5);
	}

	Position middle(double fracIndex, double lane) {
		return location(fracIndex, lane + .5);
	}

	Position right(double fracIndex) {
		return location(fracIndex, 1);
	}

	Position right(double fracIndex, int lane) {
		return location(fracIndex, lane + 1);
	}

	Position heading(double fracIndex) {
		Position locdiff = (middle(fracIndex + 1)).subtract(middle(fracIndex - 1));
		return locdiff.normalize();
	}

	void vehicleReset(Vehicle v, int lane, double fracIndex) {
		Position p = middle(fracIndex, lane);
		Position h = heading(fracIndex);
		v.p.x = p.x;
		v.p.z = p.z;
		v.h.x = h.x;
		v.h.z = h.z;
		v.fracIndex = fracIndex;
	}

	public static boolean sign(double x) {
		return (x >= 0);
	}

	public static double sqr(double x) {
		return (x * x);
	}

	double vehicleLanePosition(Vehicle v) {
		double i = v.fracIndex;
		Position lloc = left(i);
		Position rloc = right(i);
		Position head = heading(i);
		double ldx = head.x * (v.p.z - rloc.z);
		double ldz = head.z * (v.p.x - rloc.x);
		double wx = head.x * (lloc.z - rloc.z);
		double wz = head.z * (lloc.x - rloc.x);
		double ldist = Math.abs(ldx) + Math.abs(ldz);
		double width = Math.abs(wx) + Math.abs(wz);
		double lanepos = (ldist / width) * 3;
		if (((Math.abs(wx) > Math.abs(wz)) && (sign(ldx) != sign(wx)))
				|| ((Math.abs(wz) > Math.abs(wx)) && (sign(ldz) != sign(wz))))
			lanepos = -lanepos;
		lanepos += 1;
		lanepos = Math.max(lanepos, 1.01);
		return lanepos;
	}

	int vehicleLane(Vehicle v) {
		return (int) Math.floor(vehicleLanePosition(v));
	}

	double nearDistance = 10.0;
	double farDistance = 100.0;
	double nearTime = 0.5;
	double farTime = 4.0;

	Position nearPoint(Simcar simcar) {
		return middle(simcar.fracIndex + nearDistance);
	}

	public Position nearPoint(Simcar simcar, int lane) {
		return middle(simcar.fracIndex + nearDistance, lane);
	}

	public Position lanePoint(Simcar simcar, int lane) {
		return middle(simcar.fracIndex + 15, lane);
	}

	String fpText = "";
	double fpTPfracIndex = 0;

	Position farPoint(Simcar simcar, int lane) {
		double fracNearestRP = simcar.fracIndex;
		long nearestRP = (int) Math.floor(fracNearestRP);
		long j = nearestRP + 1;
		Position simcarLoc = new Position(simcar.p.x, simcar.p.z);
		int turn = 0; // left=1, right=2
		double aheadMin = nearDistance + 10;
		double aheadMax = Math.max(aheadMin, simcar.speed * farTime);

		int rln = (lane != 0) ? lane : 1;
		int lln = (lane != 0) ? lane : 2;

		Position h_l = (left(j, lln)).subtract(simcarLoc);
		Position hrd_l = (left(j, lln)).subtract(left(j - 1, lln));
		Position h_r = (right(j, rln)).subtract(simcarLoc);
		Position hrd_r = (right(j, rln)).subtract(right(j - 1, rln));

		double lxprod1 = (h_l.x * hrd_l.z) - (h_l.z * hrd_l.x);
		double norm_lxp1 = Math
				.abs(lxprod1 / (Math.sqrt(sqr(h_l.x) + sqr(h_l.z)) + Math.sqrt(sqr(hrd_l.x) + sqr(hrd_l.z))));
		double rxprod1 = (h_r.x * hrd_r.z) - (h_r.z * hrd_r.x);
		// note: below, lisp code has lxprod1 instead!!
		double norm_rxp1 = Math
				.abs(rxprod1 / (Math.sqrt(sqr(h_r.x) + sqr(h_r.z)) + Math.sqrt(sqr(hrd_r.x) + sqr(hrd_r.z))));

		boolean go_on = true;

		while (go_on) {
			j += 1;

			h_l = (left(j, lln)).subtract(simcarLoc);
			hrd_l = (left(j, lln)).subtract(left(j - 1, lln));
			h_r = (right(j, rln)).subtract(simcarLoc);
			hrd_r = (right(j, rln)).subtract(right(j - 1, rln));

			double lxprod2 = (h_l.x * hrd_l.z) - (h_l.z * hrd_l.x);
			double norm_lxp2 = Math
					.abs(lxprod1 / (Math.sqrt(sqr(h_l.x) + sqr(h_l.z)) + Math.sqrt(sqr(hrd_l.x) + sqr(hrd_l.z))));
			double rxprod2 = (h_r.x * hrd_r.z) - (h_r.z * hrd_r.x);
			// note: below, lisp code has lxprod1 instead!!
			double norm_rxp2 = Math
					.abs(rxprod1 / (Math.sqrt(sqr(h_r.x) + sqr(h_r.z)) + Math.sqrt(sqr(hrd_r.x) + sqr(hrd_r.z))));

			if (sign(lxprod1) != sign(lxprod2)) {
				turn = 1;
				go_on = false;
			}
			if (sign(rxprod1) != sign(rxprod2)) {
				turn = 2;
				go_on = false;
			}

			lxprod1 = lxprod2;
			norm_lxp1 = norm_lxp2;
			rxprod1 = rxprod2;
			norm_rxp1 = norm_rxp2;

			if (j >= (fracNearestRP + aheadMax)) {
				turn = 0;
				go_on = false;
			}
			if (j <= (fracNearestRP + aheadMin)) {
				j = (long) (fracNearestRP + aheadMin);
			}

			if (lane != 0) {
				if (turn == 1) // left
				{
					double fi = ((norm_lxp1 * (j - 1)) + (norm_lxp2 * (j - 2))) / (norm_lxp1 + norm_lxp2);
					fpText = "ltp";
					fpTPfracIndex = fi;
					return left(fi, lane);
				} else if (turn == 2) // right
				{
					double fi = ((norm_rxp1 * (j - 1)) + (norm_rxp2 * (j - 2))) / (norm_rxp1 + norm_rxp2);
					fpText = "rtp";
					fpTPfracIndex = fi;
					return right(fi, lane);
				} else {
					double fi = fracNearestRP + aheadMax;
					fpText = "vp";
					fpTPfracIndex = 0;
					return middle(fi, lane);
				}
			} else {
				// XXX not implemented -- only for lane changes
			}
		}
		return null;
	}

	float distAhead = 400;

	void draw(Graphics g, Env env) {
		double ri = env.simcar.roadIndex;
		int hardS = lanes + 1; // outer line
		double block_end = Env.scenario.block_length * env.construction.block;

		g.setColor(Color.darkGray);
		Polygon p = new Polygon();
		Coordinate newLoc = env.world2image(location(ri + 1, 1));
		if (newLoc == null)
			newLoc = newLoc = env.world2image(location(ri + 3, 1));
		p.addPoint(newLoc.x, newLoc.y);
		if (block_end < ri + 3) 
			block_end = ri + 3;


		if (ri + distAhead < block_end) {
			newLoc = env.world2image(location(ri + distAhead, 1));
			p.addPoint(newLoc.x, newLoc.y);
			newLoc = env.world2image(location(ri + distAhead, hardS));
			p.addPoint(newLoc.x, newLoc.y);
		} else {
			newLoc = env.world2image(location(block_end - 1, 1));
			p.addPoint(newLoc.x, newLoc.y);
			newLoc = env.world2image(location(block_end, 1));
			p.addPoint(newLoc.x, newLoc.y);
			newLoc = env.world2image(location(ri + distAhead, 1));
			p.addPoint(newLoc.x, newLoc.y);
			newLoc = env.world2image(location(ri + distAhead, hardS));
			p.addPoint(newLoc.x, newLoc.y);
			newLoc = env.world2image(location(block_end, hardS));
			p.addPoint(newLoc.x, newLoc.y);
			newLoc = env.world2image(location(block_end - 1, hardS));
			p.addPoint(newLoc.x, newLoc.y);
		}

		newLoc = env.world2image(location(ri + 3, hardS));
		p.addPoint(newLoc.x, newLoc.y);
		g.fillPolygon(p);

		double di = 3;
		int[] lps = { 1, 2, 3, 4, 5 };
		Coordinate[] oldLocs;
		oldLocs = new Coordinate[] { null, null, null, null, null };
		while (di <= distAhead) {
			g.setColor(Color.white);
			for (int i = 0; i < lanes + 1; i++) {
				double lp = lps[i];
				Coordinate oldLoc = oldLocs[i];
				newLoc = env.world2image(location(ri + di, lp));
				if (oldLoc != null && newLoc != null && (lp == 1 || lp == hardS || ((ri + di) % 5 < 2)))
					g.drawLine(oldLoc.x, oldLoc.y, newLoc.x, newLoc.y);
				if (newLoc != null)
					oldLocs[i] = newLoc;
			}
			if (di < 50)
				di += 0.2;
			else if (di < 100)
				di += 3;
			else
				di += 25;
		}
		// construction site

		long cri_start;
		long cri_stop;
		int lane_blocked = 2;
		
		cri_start = env.construction.start_con != 0 ? (long) env.construction.start_con : 0;
		cri_stop = env.construction.stop_con != 0 ? (long) env.construction.stop_con - 1 : 0;

		if (ri >= cri_start)
			cri_start = env.simcar.roadIndex + 3;

		if (cri_start + distAhead < cri_stop)
			cri_stop = cri_start + (long) distAhead;

		/*
		 * if (env.construction.display == false) { if (env.construction.start <= 0) {
		 * 
		 * } else if (env.construction.start > 0) { construction_ri =
		 * env.simcar.roadIndex + (long) env.construction.distAhead; } } else { if
		 * (env.world2image(location(construction_ri + 3, lane_blocked - 0.05)) != null)
		 * {
		 * 
		 * } else { construction_ri = env.simcar.roadIndex; } }
		 */
		for (int i = 0; i < 3; i++) {
			lane_blocked = 2;
			lane_blocked += i;
			if (env.world2image(location(cri_stop, lane_blocked - 0.05)) != null) {
				Polygon yellow_line = new Polygon();
				g.setColor(Color.yellow);
				Coordinate b_left = env.world2image(location(cri_start, lane_blocked - 0.05));
				if(b_left == null)
					return;
				Coordinate t_left = env.world2image(location(cri_stop, lane_blocked - 0.05));
				yellow_line.addPoint(b_left.x, b_left.y);
				yellow_line.addPoint(t_left.x, t_left.y);
				Coordinate b_right = env.world2image(location(cri_start, lane_blocked + 0.05));
				Coordinate t_right = env.world2image(location(cri_stop, lane_blocked + 0.05));
				yellow_line.addPoint(t_right.x, t_right.y);
				yellow_line.addPoint(b_right.x, b_right.y);
				g.fillPolygon(yellow_line);
			}
		}
	}

	void drawInstructions(Graphics g, Env env) {
		if (instructionsSeen) {
			Font myFont = new Font("Helvetica", Font.BOLD, 16); // instruction font
			g.setFont(myFont);
			g.setColor(Color.black);
			g.drawString(Driving.currentNBack, 200, 50);
		}
	}

	void drawWarning(Graphics g, Env env) {
		if (warningSeen) {
			Font myFont = new Font("Helvetica", Font.BOLD, 18); // instruction font
			g.setFont(myFont);
			g.setColor(Color.red);
			g.drawString("Please keep to the speed limit!", 200, 100);
		}
	}
}