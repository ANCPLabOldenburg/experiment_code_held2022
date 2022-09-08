package actr.tasks.driving;

/**
 * The class that defines a collected data sample at a given point in time.
 * 
 * @author Dario Salvucci
 */
public class Sample {
    double time;
    Position simcarPos, simcarHeading;
    double simcarFracIndex, simcarSpeed;
    long simcarRoadIndex;
    Position nearPoint, farPoint;
    double steerAngle, accelerator, brake;
    Position autocarPos, autocarHeading;
    double autocarFracIndex, autocarSpeed;
    boolean autocarBraking;
    String visAttention;


    // mlh
    int currentspeed;

    int event;
    double lanepos;

    // @MdM added: signPassed, signVisible, indicator.
    boolean signPassed;
    boolean signVisible;

    //Nilma added:
    String indicator;

    public String listVars() {
        return "time" + "\t" + "simcarPos" + "\t" + "simcarHeading" + "\t" + "simcarFracIndex" + "\t" + "simcarSpeed"
                + "\t" + "simcarRoadIndex" + "\t" + "nearPoint" + "\t" + "farPoint" + "\t"
                + "steerAngle" + "\t" + "accelerator" + "\t" + "brake" + "\t" + "autocarPos" + "\t" + "autocarHeading"
                + "\t" + "autocarFracIndex" + "\t" + "autocarSpeed" + "\t" + "currentspeed" + "\t"
                + "lanepos" + "\t" + "visAttention" + "\t" + "signPassed" + "\t" + "signVisible" + "\t" + "indicator";
    }

    public String listVarsSep(){
        return "time" + "|" + "simcarPos" + "|" + "simcarHeading" + "|" + "simcarFracIndex" + "|" + "simcarSpeed"
        + "|" + "simcarRoadIndex" + "|" + "nearPoint" + "|" + "farPoint" + "|"
        + "steerAngle" + "|" + "accelerator" + "|" + "brake" + "|" + "autocarPos" + "|" + "autocarHeading"
        + "|" + "autocarFracIndex" + "|" + "autocarSpeed" + "|" + "currentspeed" + "|"
        + "lanepos" + "|" + "visAttention" + "|" + "signPassed" + "|" + "signVisible" + "|" + "indicator";
    }

    public String toString() {
        return + time + "\t" + simcarPos + "\t" + simcarHeading + "\t" + simcarFracIndex + "\t" + simcarSpeed + "\t"
                + simcarRoadIndex + "\t" + nearPoint + "\t" + farPoint + "\t" + steerAngle + "\t"
                + accelerator + "\t" + brake + "\t" + autocarPos + "\t" + autocarHeading + "\t" + autocarFracIndex
                + "\t" + autocarSpeed + "\t" + currentspeed + "\t" + lanepos + "\t" + visAttention
                + "\t" + signPassed + "\t" + signVisible + "\t" + indicator;
    }

    public String toStringSep(){
        return +time + "|" + simcarPos + "|" + simcarHeading + "|" + simcarFracIndex + "|" + simcarSpeed + "|"
        + simcarRoadIndex + "|" + nearPoint + "|" + farPoint + "|" + steerAngle + "|"
        + accelerator + "|" + brake + "|" + autocarPos + "|" + autocarHeading + "|" + autocarFracIndex
        + "|" + autocarSpeed + "|" + currentspeed + "|" + lanepos + "|" + visAttention + "|" + signPassed + "|" + signVisible + "|" + indicator;
    }
}
