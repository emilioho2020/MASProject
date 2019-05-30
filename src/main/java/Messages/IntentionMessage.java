package Messages;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.ResourceAgent;
import MASProject.Agents.TimeSlot;
import MASProject.Util.AntPlan;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import java.util.*;

public class IntentionMessage extends SmartMessage {
    private AntPlan antPlan;
    private boolean destinationReached = false;
    private boolean noReservation = false;
    private boolean refreshIntention = false;

    public IntentionMessage(String source,
                            RoadModel roadModel,
                            AntPlan antPlan) {
        super(source, roadModel);
        this.antPlan = antPlan;
    }

    public AntPlan getAntPlan(){return antPlan;}

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

    public boolean isRefreshIntention() {return refreshIntention;}

    public void setRefreshIntention(boolean refreshIntention) {
        this.refreshIntention = refreshIntention;
    }

    @Override
    public List<AntAcceptor> getPath() {return getAntPlan().getPath();}

    /**
     * reserves timeSlot in resource and checks if it has arrived.
     * If not arrived, propagate
     * If reservation failed, set NoReservation to true
     * @param resource
     */
    @Override
    public void visit(ResourceAgent resource) {
        Point position = resource.getPosition().get();
        boolean succ;

        if (isRefreshIntention()){succ = resource.refreshTimeSlot(getAntPlan().getSchedule().get(position));}
        else {succ = resource.reserveTimeSlot(getAntPlan().getSchedule().get(position), getSource());}
        setNoReservation(!succ);

        if (resource.equals(getDestination())) {
            //current node is at destination
            setDestinationReached(true);
            setRefreshIntention(true);
            propagate(resource);
        } else {
            propagate(resource);
        }
    }

    @Override
    public void visit(PackageAgent packageAgent) {
        //TODO commit to package
    }
}
