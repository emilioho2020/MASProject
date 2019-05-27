package MASProject;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import java.util.*;

public class Plan {

    private final Map<Point, Measure<Double,Duration>> schedule;
    private final Parcel objective;


    public Plan(LinkedHashMap<Point,Measure<Double,Duration>> schedule, Parcel objective) {
        this.schedule = schedule;
        this.objective = objective;
    }

    public LinkedHashMap<Point,Measure<Double,Duration>> getSchedule() {
        return (LinkedHashMap) schedule;
    }

    public Parcel getObjective() {
        return objective;
    }

    //todo check Collection.addAll
    public Queue<Point> getPath() {
        Queue<Point> path = new LinkedList<>();
        for(Point point : schedule.keySet()) {
            path.add(point);
        }
        return path;
    }

    //TODO: not sure about evaluatioin metric; temporarily long
    public long evaluate() {
        return 0;
    }
}
