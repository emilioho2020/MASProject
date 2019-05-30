package MASProject.Agents;

import MASProject.Model.DMASNode;
import Messages.AntAcceptor;
import Messages.SmartMessage;
import MASProject.Util.SelfExpiringHashMap.SelfExpiringHashMap;
import MASProject.Util.SelfExpiringHashMap.SelfExpiringMap;
import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import java.util.*;

public class ResourceAgent implements CommUser, TickListener, RoadUser, AntAcceptor, DMASNode {

    private final static double TICK_LENGTH = 1000d;
    //Fields
    private final Point position;
    private final SelfExpiringMap<TimeSlot, String> schedule;
    private final RoadModel roadModel;
    //Communication
    private final double range = 4.2;
    private final double reliability = 1;
    Optional<CommDevice> device;

    public ResourceAgent(Point position, RoadModel rm) {
        this.position = position;
        schedule = new SelfExpiringHashMap<TimeSlot, String>(3000L);
        this.roadModel = rm;
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
            deployAnt(ant);
        }
    }

    /************************************************************************
     * ANTS
     *********************************************************************/

    /**
     * propagates this message to the next node
     * @param ant
     * @param next
     */
    @Override
    public void propagate(SmartMessage ant, AntAcceptor next) {
        device.get().send(ant, next);
    }

    @Override
    public void deployAnt(SmartMessage ant) {
        ant.visit(this);
    }

    /******************************************************************************
     * Scheduling
     ***********************************************************************/
    /**
     * Tries to reserve given timeSlot
     * @param slot
     * @return true if succeeded, false if not
     */
    public boolean reserveTimeSlot(TimeSlot slot, String id){
        //if schedule already has a reservation in the given TimeSlot and this message is not a refresh send noReservation to source
        if (alreadyBusy(slot)) {
            return false;
        } else {
            schedule.put(slot, id);
            return true;
            }
    }

    //TODO
    public boolean refreshTimeSlot(TimeSlot slot){
        return schedule.renewKey(slot);
    }

    //Checks if time provided lives inside a slot
    private boolean alreadyBusy(TimeSlot slot) {
        Set<TimeSlot> timeSlots = schedule.keySet();
        for (TimeSlot timeSlot: timeSlots) {
            //Checking if either start or end time of the provided slot lives inside the schedule
            if ((timeSlot.getStartTime() <= slot.getStartTime() && slot.getStartTime() < timeSlot.getEndTime()) ||
                    (timeSlot.getStartTime() <= slot.getEndTime() && slot.getEndTime() < timeSlot.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    /*
    private TimeSlot calculateTimeSlot(Measure<Double,Duration> time) {
        double doubleTime = time.doubleValue(time.getUnit());
        return new TimeSlot(Math.floor(doubleTime),Math.floor(doubleTime)+TICK_LENGTH);
    }*/

    /**************************************************************************
     *
     ***********************************/

    @Override
    public void afterTick(TimeLapse timeLapse) {
    }

    public RoadModel getRoadModel(){
        return roadModel;
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
    public String toString() {
        return "ResourceAgent@"+getPosition().get();
    }

}
