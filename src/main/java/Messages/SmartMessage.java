package Messages;

import MASProject.Agents.ResourceAgent;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

import java.util.*;

public abstract class SmartMessage implements MessageContents {
   //better to pass an ID with the messages
    private int id = 0;
    private final String source;
    private final AntAcceptor destination;
    //private delegateMAS delegate =new delegateMAS();
    //private List<ResourceAgent> path = new ArrayList<>();
    private RoadModel roadModel;

    SmartMessage(String source, AntAcceptor destination, RoadModel rm) {
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

    public AntAcceptor getDestination() {
        return destination;
    }
    public abstract void visit(ResourceAgent acceptor);

    public RoadModel getRoadModel() {
        return roadModel;
    }

    public AntAcceptor getNextAcceptor(Point point) {
        return null;
        //TODO
    }


    public void propagate(ResourceAgent resource){
        resource.propagate(this, getNextAcceptor(resource.getPosition().get()));
    }

    public abstract Point getNextPoint(Point currentLocation);
}
