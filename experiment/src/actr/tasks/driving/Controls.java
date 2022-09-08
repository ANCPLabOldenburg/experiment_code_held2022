package actr.tasks.driving;

import net.java.games.input.Controller;

import net.java.games.input.Component;

import net.java.games.input.Component.Identifier;
import net.java.games.input.ControllerEnvironment;

/**
 * This class deals with getting values from the external steering wheel &
 * accelerator/brake pedals.
 * 
 * @author Nilma Kelapanda
 */

// npk
public class Controls {

    Controller steerPedals = null;

    // function to locate the steering wheel & pedals connected to PC.
    public void startUp() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        // Find the steering wheel
        for (int i = 0; i < controllers.length && steerPedals == null; i++) {
            if (controllers[i].getType() == Controller.Type.WHEEL) { //try WHEEL or STICK depending on pc
                steerPedals = controllers[i];
                break;
            }
        }
        if (steerPedals == null) {
            System.out.println("Found no steering wheel!");
            //System.exit(0);
        }
    }

    public double getAccelerator() {
        //return -getValue(Component.Identifier.Axis.Y);
        return 0.5;
        //return -getValue(Component.Identifier.Axis.SLIDER); //changes with system and driver installed
    }

    public double getSteering() {
        return 0;
        //return getValue(Component.Identifier.Axis.X);
    }

    public boolean buttonXpressed() {
        return true;
        //return (getValue(Component.Identifier.Button._6) == 1.0);
    }

    public String getIndicator() {
        return "";
/*         String indicator;
        if (getValue(Component.Identifier.Button._4) == 1) {
            indicator = "right";
        } else if (getValue(Component.Identifier.Button._5) == 1) {
            indicator = "left";
        } else {
            indicator = "";
        }
        return indicator; */
    }

    public double getValue(Identifier identifier) {
        steerPedals.poll();
        Component[] components = steerPedals.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            Identifier ident = component.getIdentifier();
            if (ident == identifier){
                return component.getPollData();
            }
        }
        throw new java.lang.Error("These aren't the components you're looking for.");
    }
}
