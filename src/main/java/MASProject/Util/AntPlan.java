package MASProject.Util;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.TimeSlot;
import Messages.AntAcceptor;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import java.util.*;


/**
 *
 */
public class AntPlan {

    private final Map<AntAcceptor, TimeSlot> schedule;
    private final PackageAgent objectivePackage;

    public AntPlan(LinkedHashMap<AntAcceptor,TimeSlot> schedule, PackageAgent p) {
        this.schedule = schedule;
        this.objectivePackage = p;
    }

    public LinkedHashMap<AntAcceptor,TimeSlot> getSchedule() {
        return (LinkedHashMap) schedule;
    }

    public AntAcceptor getDestination() {
        return getPath().get(getPath().size()-1);
    }

    public LinkedList<AntAcceptor> getPath() { return new LinkedList<>(schedule.keySet()); }

    public PackageAgent getObjectivePackage(){return objectivePackage;}

    //evaluation will be the time of arrival at final node  in milliseconds
    public double evaluate() {
        List<TimeSlot> points = new ArrayList<>( schedule.values());
        return points.get(points.size()-1).getEndTime();
    }

    public void removePoint(AntAcceptor r) {
        schedule.remove(r);
    }

}
