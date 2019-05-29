package MASProject.Util;

import MASProject.Agents.PackageAgent;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import java.util.*;


/**
 *
 */
public class Plan {

    private final Map<Point, Measure<Double,Duration>> schedule;
    //private final PackageAgent objective;


    public Plan(LinkedHashMap<Point,Measure<Double,Duration>> schedule) {
        this.schedule = schedule;
        //this.objective = objective;
    }

    public LinkedHashMap<Point,Measure<Double,Duration>> getSchedule() {
        return (LinkedHashMap) schedule;
    }

    /**
    public PackageAgent getObjective() {
        return objective;
    }
    */

    public Queue<Point> getPath() { return new LinkedList<>(schedule.keySet()); }

    //evaluation will be the time of arrival at final node
    public double evaluate() {
        List<Measure<Double,Duration>> points = new ArrayList<>( schedule.values());
        return points.get(points.size()-1).doubleValue(Duration.UNIT);
    }

    public void removePoint(Point p) {
        schedule.remove(p);
    }
}
