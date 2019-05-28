package Messages;

import MASProject.Agents.ResourceAgent;
import MASProject.delegateMAS.delegateMAS;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class SmartMessage implements MessageContents {
   //better to pass an ID with the messages
    private int id = 0;
    private final String source;
    private final Parcel destination;
    //private delegateMAS delegate =new delegateMAS();
    //private List<ResourceAgent> path = new ArrayList<>();
    private RoadModel roadModel;

    SmartMessage(String source, Parcel destination, RoadModel rm) {
        this.source = source;
        this.destination = destination;
        this.roadModel = rm;
    }
/**
    SmartMessage(int id, String source, Parcel destination, delegateMAS d) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.delegate = d;
    }
**/
    public String getSource() {
        return source;
    }

    public Parcel getDestination() {
        return destination;
    }
    public abstract void visit(ResourceAgent acceptor);

    public RoadModel getRoadModel() {
        return roadModel;
    }

    public CommUser getNextResource(Point point) {
        Set<ResourceAgent> allAgents = getRoadModel().getObjectsOfType(ResourceAgent.class);
        for(ResourceAgent agent : allAgents) {
            if(agent.getPosition().get().equals(getNextPoint(point))) {
                return agent;
            }
        }
        return null;
    }


    public void propagate(ResourceAgent resource){
        resource.propagate(this, getNextResource(resource.getPosition().get()));
    }

    public abstract Point getNextPoint(Point currentLocation);
}
