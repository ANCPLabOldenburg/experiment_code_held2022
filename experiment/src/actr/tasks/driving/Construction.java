package actr.tasks.driving;

import networking.ServerMain;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Polygon;
//import java.io.*;
import java.util.Vector;

/**
 * The class that defines the specifics of the construction site.
 * 
 * @author Moritz Held
 */
public class Construction extends Road {

    int pyno;
    double start_con = 0;
    double stop_con = 0;
    Vector<Double> pylons = new Vector<>();
    double block_length = 10000; // replace with length of construction site in meters
    double onset = 0;
    double distAhead = 150;
    boolean construction_bool;
    boolean construction_vis;
    double start_trans;
    int block = 1;
    boolean construction;
    boolean once = true;
    public Construction(boolean con)
    {
        super(con);
        this.construction = con;
        System.out.println("Construction site: " + con);

    }
    /* Update function checking when the construction site should be displayed */
    void update(Env env) {
        if(construction)
        {
            if(once)
            {
                for(int i = 0; i < 41; i++)
                {
                    pylons.add(90+ 5.0*i);
                }
                once = false;
            }
            if (env.simcar.fracIndex > start_con && start_con != 0) {
                if (!construction_bool) {
                    //ServerMain.participant.sendConstructionStart();
                }
                construction_bool = true;
            }
    
            if (env.simcar.fracIndex + distAhead > onset) {
                construction_vis = true;
                // define the onset/offset of the construction site
                start_con = ((int) Math.round(env.simcar.fracIndex / 10.0) * 10);
                stop_con = start_con + block_length;
    
                onset = onset + block_length * 2;
            }
    
            // adding a new pylon every x meters
            int meters = 5;
            if (construction_vis && env.simcar.fracIndex + distAhead - meters > pylons.get(pylons.size() - 1)) {
                if (env.simcar.fracIndex + distAhead - meters < stop_con){
                    pylons.add((Math.round(env.simcar.fracIndex / 5.0)) * 5 + distAhead);
                }
            }
    
            if (env.simcar.fracIndex > stop_con && start_con != 0) {
                if (construction_bool) {
                    //ServerMain.participant.sendConstructionStop();
                }
                construction_bool = false;
                construction_vis = false;
                start_con = 0;
            }
    
            if (env.simcar.fracIndex > block * block_length)
                block++;
        }
    }

    void drawConstruction(Graphics g, Env env) {
        if (start_con > 0 && env.simcar.roadIndex < stop_con) {
            pyno = pylons.size() - 100; // simply don't attempt to draw all the pylons in pylons[]
            if (pyno < 0)
                pyno = 0;
            drawPylon(g, env, pylons.get(pyno));
        }
    }

    void drawPylon(Graphics g, Env env, double pylonFrac) {
        double lane = 2;

        // pylon
        Coordinate im1;
        Coordinate im2;
        Coordinate testloc1 = env.world2image(location(pylonFrac, lane-0.6));
        Coordinate testloc2 = env.world2image(location(pylonFrac, lane+0.6));

        if (testloc1 != null && testloc2 !=null) {
            g.setColor(Color.white);
            im1 = env.world2image(location(pylonFrac, lane - 0.03), 0.9);
            im2 = env.world2image(location(pylonFrac, lane + 0.03), 0.0);
            g.fillRect(im1.x, im1.y, im2.x - im1.x, im2.y - im1.y);

            // lamp
            int r = (im2.y - im1.y) / 5;
            int r2 = (int) ((im2.y - im1.y) / 5.5);
            g.setColor(Color.yellow);
            g.fillOval(im1.x + ((im2.x - im1.x) / 2) - r, im1.y - r * 2, r * 2, r * 2);
            g.setColor(Color.orange);
            g.fillOval(im1.x + (im2.x - im1.x) / 2 - r2, im1.y - r2 * 2 - (r - r2), r2 * 2, r2 * 2);

            // stripes
            for (int i = 1; i < 8; i++) {
                if (i % 5 == 0 || i == 1) {
                    for (int j = 0; j < 2; j++) {
                        double x = i + j;
                        x = x * 0.1;
                        Polygon p = new Polygon();
                        Coordinate newLoc;
                        g.setColor(Color.red);
                        newLoc = env.world2image(location(pylonFrac, lane + 0.03), x + 0.1);
                        p.addPoint(newLoc.x, newLoc.y);
                        newLoc = env.world2image(location(pylonFrac, lane - 0.03), x + 0.2);
                        p.addPoint(newLoc.x, newLoc.y);
                        newLoc = env.world2image(location(pylonFrac, lane - 0.03), x + 0.1);
                        p.addPoint(newLoc.x, newLoc.y);
                        newLoc = env.world2image(location(pylonFrac, lane + 0.03), x);
                        p.addPoint(newLoc.x, newLoc.y);
                        g.fillPolygon(p);
                        // g.drawRect(im1.x, im1.y, 10, 30, true);
                    }
                }
            }

            // foot
            Polygon p = new Polygon();
            Coordinate newLoc;
            g.setColor(Color.black);
            newLoc = env.world2image(location(pylonFrac, lane + 0.06));
            p.addPoint(newLoc.x, newLoc.y);
            newLoc = env.world2image(location(pylonFrac, lane + 0.04), 0.1);
            p.addPoint(newLoc.x, newLoc.y);
            newLoc = env.world2image(location(pylonFrac, lane - 0.04), 0.1);
            p.addPoint(newLoc.x, newLoc.y);
            newLoc = env.world2image(location(pylonFrac, lane - 0.06));
            p.addPoint(newLoc.x, newLoc.y);
            g.fillPolygon(p);

            pyno++;
            if (pyno < pylons.size()) {
                drawPylon(g, env, pylons.get(pyno));
            }
        } else if (pylons.size() > 1) {
            pyno++;
            if (pyno < pylons.size())
                drawPylon(g, env, pylons.get(pyno));
        }
    }
}