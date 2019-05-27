package Messages;

import MASProject.Agents.ResourceAgent;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.*;

public class ExplorationMessage extends SmartMessage {
    private final Queue<Point> path;
    private final Map<Point,Measure<Double,Duration>> scheduledPath;
    private Measure<Double, Duration> costSoFar = Measure.valueOf(0d, SI.SECOND);
    private boolean destinationReached = false;

    public ExplorationMessage(String source, Parcel destination, Queue<Point> path) {
        super(source, destination);
        //linked hash map to preserve insertion order
        scheduledPath = new LinkedHashMap<>();
        this.path = path;
    }

    public void addToSchedule(Point point, Measure<Double,Duration> time) {
        scheduledPath.put(point, time);
    }

    public LinkedHashMap<Point,Measure<Double,Duration>> getScheduledPath() { return (LinkedHashMap<Point,Measure<Double,Duration>>) scheduledPath; }

    private Point getNextPoint(Point curr) {
        List<Point> points = new ArrayList<>(path);
        return points.get(points.indexOf(curr)+1);
    }

    public Queue<Point> getPath() {
        return path;
    }

    public boolean isDestinationReached() {
        return destinationReached;
    }

    public void addCost(Measure<Double,Duration> cost) {
        Unit<Duration> unit = costSoFar.getUnit();
        costSoFar = Measure.valueOf(costSoFar.doubleValue(unit) + cost.doubleValue(unit),unit);

    }

    public void setDestinationReached(boolean destinationReached) {
        this.destinationReached = destinationReached;
    }

    public CommUser getNextResource(RoadModel roadModel, Point point) {
        Set<ResourceAgent> allAgents = roadModel.getObjectsOfType(ResourceAgent.class);
        for(ResourceAgent agent : allAgents) {
            if(agent.getPosition().get().equals(getNextPoint(point))) {
                return agent;
            }
        }
        return null;
    }

    public double calculateCost(RoadModel rm, Point start, Point end, Measure<Double, Velocity> speed) {
        List<Point> rout = new LinkedList<>();
        rout.add(start);
        rout.add(end);
        Measure<Double,Length> distance = rm.getDistanceOfPath(rout);
        return RoadModels.computeTravelTime(speed, distance, SI.SECOND);
    }

    public Measure<Double, Duration> getCostSoFar() {
        return costSoFar;
    }
}
