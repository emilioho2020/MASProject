package Messages;

import MASProject.Agents.ResourceAgent;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import java.util.*;

public class IntentionMessage extends SmartMessage {
    private Map<Point,Measure<Double,Duration>> scheduledPath;
    private boolean destinationReached = false;
    private boolean noReservation = false;
    private boolean refreshIntention = false;

    public IntentionMessage(String source, Parcel destination, Map<Point, Measure<Double,Duration>> scheduledPath) {
        super(source, destination);
        this.scheduledPath = scheduledPath;
    }

    public Map<Point,Measure<Double,Duration>> getScheduledPath() {
        return scheduledPath;
    }

    public Point getNextPoint(Point curr) {
        List<Point> points = new ArrayList<>(scheduledPath.keySet());
        return points.get(points.indexOf(curr)+1);
    }

    //TODO
    public CommUser getNextResource(RoadModel roadModel, Point point) {
        Set<ResourceAgent> allAgents = roadModel.getObjectsOfType(ResourceAgent.class);
        for(ResourceAgent agent : allAgents) {
            if(agent.getPosition().get().equals(getNextPoint(point))) {
                return agent;
            }
        }
        return null;
    }


    public CommUser getResourceAt(RoadModel roadModel, Point point) {
        Set<ResourceAgent> allAgents = roadModel.getObjectsOfType(ResourceAgent.class);
        for(ResourceAgent agent : allAgents) {
            if(agent.getPosition().get().equals(point)) {
                return agent;
            }
        }
        return null;
    }

    public void removePoint(Point p) {
        scheduledPath.remove(p);
    }

    public Queue<Point> getPath() { return new LinkedList<>(scheduledPath.keySet()); }

    //Getters and Setters

    public boolean isDestinationReached() {
        return destinationReached;
    }

    public void setDestinationReached(boolean destinationReached) {
        this.destinationReached = destinationReached;
    }

    public boolean isNoReservation() {
        return noReservation;
    }

    public void setNoReservation(boolean noReservation) {
        this.noReservation = noReservation;
    }

    public boolean isRefreshIntention() {
        return refreshIntention;
    }

    public void setRefreshIntention(boolean refreshIntention) {
        this.refreshIntention = refreshIntention;
    }
}
