package MASProject.Agents;

import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import SelfExpiringHashMap.SelfExpiringHashMap;
import SelfExpiringHashMap.SelfExpiringMap;
import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import java.util.*;

public class ResourceAgent implements CommUser, TickListener, RoadUser {

    //Fields
    private final Point position;
    private final SelfExpiringMap<TimeLapse, String> schedule;
    private final RoadModel roadModel;
    //Communication
    private final double range = 4.2;
    private final double reliability = 1;
    Optional<CommDevice> device;

    //have to think about using the roadmodel here
    public ResourceAgent(Point position, RoadModel rm) {
        this.position = position;
        schedule = new SelfExpiringHashMap<TimeLapse, String>();
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

    @Override
    public void initRoadUser(RoadModel roadModel) {
        roadModel.addObjectAt(this, position);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if(!(device.get().getUnreadCount() > 0)) {
            return;
        }

        List<Message> messages = device.get().getUnreadMessages();
        for(Message message : messages) {
            if(message.getContents() instanceof ExplorationMessage){
                ExplorationMessage msg = (ExplorationMessage) message.getContents();
                List<Point> points = new ArrayList<>(((ExplorationMessage) message.getContents()).getPath());
                if(!getPosition().get().equals(points.get(points.size()-1))) {
                    //System.out.println("Pos of resource agent that picked the message");
                    //System.out.println(getPosition().get());
                    CommUser cu = msg.getNextResource(roadModel, getPosition().get());
                    //System.out.println("Pos of next resource agent");
                    //System.out.println(cu.getPosition().get());
                    device.get().send(message.getContents(), cu);
                }else{
                    msg.setDestinationReached(true);
                    //System.out.println("Pos of last resource agent");
                    //System.out.println(getPosition().get());
                }

            }
            /*if(message.getContents() instanceof ExplorationMessage){
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
                }
                else {
                    schedule.put(msg.getScheduledPath().get(getPosition()),msg.getSource());
                    device.get().send(msg, getNextResource(msg, getPosition().get()));
                }
            }*/
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {

    }

}
