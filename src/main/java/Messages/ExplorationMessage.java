package Messages;

import MASProject.Agents.ResourceAgent;
import MASProject.Agents.TransportAgent;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.*;

public class ExplorationMessage extends SmartMessage {
    private final Queue<Point> path;
    private final Map<Point,Measure<Double,Duration>> scheduledPath;
    private Measure<Double, Duration> costSoFar = Measure.valueOf(0d, SI.SECOND);
    private boolean destinationReached = false;

    public ExplorationMessage(String source, AntAcceptor destination, Queue<Point> path, RoadModel roadModel) {
        super(source, destination, roadModel);
        //linked hash map to preserve insertion order
        scheduledPath = new LinkedHashMap<>();
        this.path = path;
    }

    public void setInitialCost(double cost) {
        costSoFar = Measure.valueOf(cost,SI.SECOND);
    }

    public void addToScheduledPath(Point point, Measure<Double,Duration> time) {
        scheduledPath.put(point, time);
    }

    public LinkedHashMap<Point,Measure<Double,Duration>> getScheduledPath() { return (LinkedHashMap<Point,Measure<Double,Duration>>) scheduledPath; }

    @Override
    public Point getNextPoint(Point curr) {
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


    /*********************************************************************************************
     *COSTS
     *********************************************************************************/
    public double calculateCost(Point start, Point end, Measure<Double, Velocity> speed) {
        List<Point> rout = new LinkedList<>();
        rout.add(start);
        rout.add(end);
        Measure<Double,Length> distance = getRoadModel().getDistanceOfPath(rout);
        return RoadModels.computeTravelTime(speed, distance, SI.SECOND);
    }

    public Measure<Double, Duration> getCostSoFar() {
        return costSoFar;
    }

    @Override
    public void visit(ResourceAgent resource) {
        List<Point> points = new ArrayList<Point>(getPath());
        Point position = resource.getPosition().get();
        RoadModel roadModel = resource.getRoadModel();

        //Add the cost to come to this node to ants plan
        addToScheduledPath(position, getCostSoFar());

        if(!position.equals(points.get(points.size()-1))) {
            //current node is not destination
            //send to next resource; calculate travel cost
            CommUser nextResource = getNextAcceptor(position);
            double cost = calculateCost(
                    position, nextResource.getPosition().get(),
                    Measure.valueOf(TransportAgent.SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
            addCost(Measure.valueOf(cost, Duration.UNIT));
            propagate(resource);
        }else{
            //current node is destination
            setDestinationReached(true);
        }
    }

}
