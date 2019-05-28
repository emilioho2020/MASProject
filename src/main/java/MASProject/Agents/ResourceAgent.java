package MASProject.Agents;

import Messages.AntAcceptor;
import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import Messages.SmartMessage;
import SelfExpiringHashMap.SelfExpiringHashMap;
import SelfExpiringHashMap.SelfExpiringMap;
import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.*;

public class ResourceAgent implements CommUser, TickListener, RoadUser {

    private final static double TICK_LENGTH = 1000d;
    //Fields
    private final Point position;
    private final SelfExpiringMap<TimeSlot, String> schedule;
    private final RoadModel roadModel;
    //Communication
    private final double range = 4.2;
    private final double reliability = 1;
    Optional<CommDevice> device;

    //have to think about using the roadModel here
    public ResourceAgent(Point position, RoadModel rm) {
        this.position = position;
        schedule = new SelfExpiringHashMap<TimeSlot, String>(3000L);
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
        //Loop through the received messages
        for(Message message : messages) {
            SmartMessage ant = (SmartMessage) message.getContents();
            ant.visit(this);
        }
    }

    /**
     *
     * @param ant
     */


    private void handleAnt(IntentionMessage ant){
        List<Point> points = new ArrayList<>(ant.getPath());

        //if schedule already has a reservation in the given TimeSlot and this message is not a refresh send noReservation to source
                    if (alreadyBusy(ant.getScheduledPath().get(getPosition().get())) && !ant.isRefreshIntention()) {
            ant.setNoReservation(true);
        } else {
            TimeSlot timeSlot = calculateTimeSlot(ant.getScheduledPath().get(getPosition().get()));

            //handles both cases if intention is refresh or not
            if(schedule.containsKey(timeSlot)) {
                schedule.renewKey(timeSlot);
            } else {
                schedule.put(timeSlot, ant.getSource());
            }
        }

                    if (!getPosition().get().equals(points.get(points.size() - 1))) {
            //current node is not destination
            device.get().send(ant, ant.getNextResource(roadModel, getPosition().get()));
        } else {
            //current node is destination so set flags for destination reached and
            //to use ant for refreshing the intention
            ant.setDestinationReached(true);
            ant.setRefreshIntention(true);
        }
    }

    //Checks if time provided lives inside a slot
    private boolean alreadyBusy(Measure<Double,Duration> time) {
        if(time == null) {return false;}
        Set<TimeSlot> timeSlots = schedule.keySet();
        for(TimeSlot slot : timeSlots) {
            if(slot.getStartTime() <= time.doubleValue(time.getUnit()) && time.doubleValue(time.getUnit()) < slot.getEndTime()) {
                return true;
            }
        }
        return false;
    }

    private TimeSlot calculateTimeSlot(Measure<Double,Duration> time) {
        double doubleTime = time.doubleValue(time.getUnit());
        return new TimeSlot(Math.floor(doubleTime),Math.floor(doubleTime)+TICK_LENGTH);
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {
    }

    public RoadModel getRoadModel(){
        return roadModel;
    }

    /**
     * propagates this message to the next node
     * @param ant
     * @param next
     */
    public void propagate(SmartMessage ant,CommUser next) { //TODO implement AntAcceptor
        device.get().send(ant, next);
    }
}
