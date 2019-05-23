package MASProject;

import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceAgent implements CommUser, TickListener, RoadUser {

    //Fields
    private final Point position;
    private final Map<TimeLapse, String> schedule;
    private final RoadModel roadModel;
    //Communication
    private final double range = 10;  //TODO set range
    private final double reliability = 1;
    Optional<CommDevice> device;

    //have to think about using the roadmodel here
    ResourceAgent(Point position, RoadModel rm) {
        this.position = position;
        schedule = new HashMap<>();
        this.roadModel = rm;
    }

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(position);
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        if (range >= 0) {
            builder.setMaxRange(range);
        }
        device = Optional.of(builder
                .setReliability(reliability)
                .build());
    }

    //TODO: need to register objects in order to seatch them in getNextResource method but if so, Transport object may collide with them.
    @Override
    public void initRoadUser(RoadModel roadModel) {

    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if(!(device.get().getUnreadCount() > 0)) {
            return;
        }
        List<Message> messages = device.get().getUnreadMessages();
        for(Message message : messages) {
            if(message.getContents() instanceof ExplorationMessage){
                ExplorationMessage msg =(ExplorationMessage) message.getContents();
                if(msg.getPath().contains(getPosition().get())) {
                    //if path contains the position of this agent we don't want to broadcast from it
                    continue;
                }
                msg.addPointToPath(getPosition().get());
                device.get().broadcast(msg);
            }
            else if(message.getContents() instanceof IntentionMessage) {
                IntentionMessage msg = (IntentionMessage) message.getContents();
                //if schedule already has a reservation in the given TimeLaps send noReservation to source
                if(schedule.containsKey(msg.getScheduledPath().get(getPosition()))) {
                    //TODO: make noReservation method to let source know that reservation has failed;proably different kind of msg
                }
                else {
                    schedule.put(msg.getScheduledPath().get(getPosition()),msg.getSource());
                    device.get().send(msg, getNextResource(msg, getPosition().get()));
                }
            }
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }

    private CommUser getNextResource(IntentionMessage msg, Point point) {
        Set<ResourceAgent> allAgents = roadModel.getObjectsOfType(ResourceAgent.class);
        for(ResourceAgent agent : allAgents) {
            if(agent.getPosition().get().equals(msg.getNextPoint(point))) {
                return agent;
            }
        }
        return null;
    }

}
