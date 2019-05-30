package MASProject.Util;

import Messages.AntAcceptor;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import java.util.*;


/**
 *
 */
public class AntPlan {

    private final Map<AntAcceptor, Measure<Double,Duration>> schedule;

    public AntPlan(LinkedHashMap<AntAcceptor,Measure<Double,Duration>> schedule) {
        this.schedule = schedule;
    }

    public LinkedHashMap<Point,Measure<Double,Duration>> getSchedule() {
        return (LinkedHashMap) schedule;
    }

    public AntAcceptor getDestination() {
        return getPath().get(getPath().size()-1);
    }

    public LinkedList<AntAcceptor> getPath() { return new LinkedList<>(schedule.keySet()); }

    //evaluation will be the time of arrival at final node //TODO
    public double evaluate() {
        List<Measure<Double,Duration>> points = new ArrayList<>( schedule.values());
        return points.get(points.size()-1).doubleValue(Duration.UNIT);
    }

    public void removePoint(AntAcceptor r) {
        schedule.remove(r);
    }
}