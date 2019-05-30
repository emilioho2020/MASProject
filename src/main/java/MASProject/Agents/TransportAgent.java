package MASProject.Agents;

import MASProject.Util.AntPlan;
import MASProject.delegateMAS.delegateMAS;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Our implementation of a simple agent in a simple PDP problem : delivering pizzas in time.
 */
public class TransportAgent extends Vehicle implements CommUser {

    //Static fields
    private static AtomicLong idCounter = new AtomicLong();
    public static final double SPEED_KMH = 1d;

    //from PDP model
    private Optional<PackageAgent> currentPackage;

    //the plans-from BDI
    private Optional<AntPlan> preferredPlan;
    private Optional<AntPlan> reservedPlan;

    //Needed so that agent can follow this.
    private Queue<Point> path;

    //Communication
    private final double range = 4.2;
    private final double reliability = 1;
    Optional<CommDevice> device;

    delegateMAS delegate;


    public TransportAgent(Point startPosition, int capacity){
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED_KMH)
                .build()
            );
        currentPackage = Optional.absent();
        reservedPlan = Optional.absent();
        preferredPlan = Optional.absent();
        path = new LinkedList<>();
        device = Optional.absent();
        delegate = new delegateMAS(this, getRoadModel());
    }

    //todo here we should consider separate plans for pickup and only after pickup a plan for delivery
    @Override
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();

        if (!time.hasTimeLeft()) {
            //if no time left do nothing!!
            return;
        }
        /* In this section we explore the environment, evaluate the results
         * and pick a preferred plan. The objective of that plan is set as
         * the current parcel to pick.
         */
        if (!currentPackage.isPresent()) {
            List<AntPlan> plans2 = findPlansToParcel();
            choosePlan(plans2);
        }
        /* In this section we send an intention ant to register the preferred
         * plan. After registering the agent follows the path and refreshes
         * regularly its current intention.
         */
        if (currentPackage.isPresent()) {
            //Basically checks if current objective is already delivered
            //if so return to create new plan
            if (!isDelivering() && !rm.containsObject(currentPackage.get())) {
                // sanity check: if it is not in our cargo AND it is also not on the
                // RoadModel, we cannot go to currentPackage anymore.
                clearObjective();
                currentPackage = Optional.absent();
            } else if (isDelivering() && !reservedPlan.isPresent()) {
                //make plan
                //should be same as when no objective
                //todo check here
                if(!preferredPlan.isPresent()) {
                    List<AntPlan> plans3 = findPlansToParcel();
                    choosePlan(plans3);
                }
                setReserved();
            } else if (isDelivering()){
                followObjective(time, rm, pm);
            } else if (!isDelivering() && !reservedPlan.isPresent()){
                //make plan
                setReserved();
            } else if (!isDelivering()) {
                //follow plan
                followObjective(time, rm, pm);
            }
        }
    }

    private List<AntPlan> findPlansToParcel() {
        List<AntPlan> plans2 = delegate.getExplorationResults();
        return plans2;
    }

    private void choosePlan(List<AntPlan> plans2){
        //evaluate plans if any found
        preferredPlan = evaluatePlans(plans2);

        if(preferredPlan.isPresent()) {
            System.out.println(preferredPlan.get().getSchedule());
            //if a preferred plan exists set a current objective
            currentPackage = Optional.of(preferredPlan.get().getDestination());
            delegate.sendIntentionAnt(preferredPlan.get());
        }
    }

    /**
     * sets the preferred plan as reserved plan if ant reaches destination
     * @return
     */
    private void setReserved() {
        try {
            Boolean result = delegate.isReservationMade();
            if (result){reservedPlan = preferredPlan;}
        } catch (Exception e) {
            System.out.println("No reservation.");
            clearObjective();
            currentPackage = Optional.absent();
        }
    }

    /**
     * Follows a successfully reserved path
     * @param time
     * @param rm
     * @param pm
     */
    private void followObjective(TimeLapse time, RoadModel rm, PDPModel pm){
        //a reservedPlan exists
        if (rm.getPosition(this).equals(currentPackage.get().getDeliveryLocation())) {
            // deliver when we arrive
            pm.deliver(this, currentPackage.get(), time);
            clearObjective();
        } else {
            // it is still available, go there as fast as possible
            followPlan(rm, time, preferredPlan.get());
            if (rm.equalPosition(this, currentPackage.get())) {
                // pickup customer
                pm.pickup(this, currentPackage.get(), time);
                clearObjective();
            }
        }
    }

    /**
     * @param plans2
     * @return The best plan according to our heuristics
     * HEURISTIC: travel time
     */
    private Optional<AntPlan> evaluatePlans(List<AntPlan> plans2) {
        if(plans2.isEmpty()) {
            return Optional.absent();
        }
        List<Double> durations = new LinkedList<>();
        for(AntPlan antPlan : plans2) {
            durations.add(antPlan.evaluate());
        }
        int minIndex = durations.indexOf(Collections.min(durations));
        return Optional.of(plans2.get(minIndex));
    }

    //get path from antPlan and follow it.
    //TODO: probably some more code in how agent follows the schedule
    //TODO: !!!!!!!better method to compare points!!!!!!!!!!!
    private void followPlan(RoadModel rm, TimeLapse time, AntPlan antPlan) {
        if(path.isEmpty()) {
            path.addAll(antPlan.getPath());
        }

        if(Point.Comparators.XY.compare(path.peek(), rm.getPosition(this)) >= 0) {
            Point temp = path.remove();
            antPlan.removePoint(temp);
            delegate.removePoint(temp);
        }
        rm.followPath(this, path, time);
    }

    //method to clear the state of the agent
    private void clearObjective() {
        preferredPlan = Optional.absent();
        reservedPlan = Optional.absent();
        delegate.clearObjective();
    }

    public Optional<Point> getPosition() {
        if (getRoadModel().containsObject(this)) {
            return Optional.of(getRoadModel().getPosition(this));
        }
        return Optional.absent();
    }

    //creates a unique ID for the agent to be used in reservations
    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        if (range >= 0) {
            builder.setMaxRange(range);
        }
        device = Optional.of(builder
                .setReliability(reliability)
                .build());
        delegate.setDevice(device.get());
    }

    /**
     * @return
     */
    public Parcel getCurrentPackage() {
        if(currentPackage.isPresent()) {
            return currentPackage.get();
        } else {
            return null;
        }
    }

    /**
     * @return true if agent is currently transporting something to it's destingtion
     */
    public boolean isDelivering(){
        return getPDPModel().containerContains(this, currentPackage.get());
    }
}
