package Messages;

import MASProject.ResourceAgent;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.util.*;

public class ExplorationMessage extends SmartMessage {
    private final Queue<Point> path;
    private final Map<Point,TimeLapse> scheduledPath;
    private boolean destinationReached = false;

    public ExplorationMessage(String source, Parcel destination, Queue<Point> path) {
        super(source, destination);
        //linked hash map to preserve insertion order
        scheduledPath = new LinkedHashMap<>();
        this.path = path;
    }

    public void addToSchedule(Point point, TimeLapse time) {
        scheduledPath.put(point, time);
    }

    public LinkedHashMap<Point,TimeLapse> getScheduledPath() { return (LinkedHashMap<Point,TimeLapse>) scheduledPath; }

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
}
