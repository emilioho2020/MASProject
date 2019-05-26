package Messages;

import MASProject.ResourceAgent;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IntentionMessage extends SmartMessage {
    private Map<Point,TimeLapse> scheduledPath;
    private boolean destinationReached = false;
    private boolean noReservation = false;

    public IntentionMessage(String source, Parcel destination, Map<Point,TimeLapse> scheduledPath) {
        super(source, destination);
        this.scheduledPath = scheduledPath;
    }

    public Map<Point,TimeLapse> getScheduledPath() {
        return scheduledPath;
    }

    public Point getNextPoint(Point curr) {
        List<Point> points = new ArrayList<>(scheduledPath.keySet());
        return points.get(points.indexOf(curr)+1);
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
}
