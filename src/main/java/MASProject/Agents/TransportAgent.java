package MASProject.Agents;

import MASProject.PizzaExample;
import MASProject.Util.AntPlan;
import MASProject.delegateMAS.delegateMAS;
import Messages.AntAcceptor;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
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
    private static final int NUM_OF_POSSIBILITIES = 3;
    private static final int NUM_PATHS_EXPLORING = 3;

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
        delegate = new delegateMAS(this);
    }

    @Override
    protected void tickImpl(TimeLapse time) {
        if (!time.hasTimeLeft()) {
            return;
        }//currentPackage

        if (reservedPlan.isPresent()) {
            final boolean inCargo = getPDPModel().containerContains(this, currentPackage.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !getRoadModel().containsObject(currentPackage.get())) {
                clearObjective();
                currentPackage = Optional.absent();
            }else {
                followObjective(time);
            }
        } else {
            if (preferredPlan.isPresent()){
                //has chosen plan but has to be reserved
                setReserved();

            } else {

                if (!isDelivering()) {
                    findPlansToParcel(time);
                } else {
                    findPlansToDeliveryLocation(time);
                }

                List<AntPlan> candidates = getPlansToObjective();
                choosePreferredPlan(candidates);

            }
        }
    }

    private List<AntPlan> getPlansToObjective() {
        return delegate.getExplorationResults();
    }

    private void findPlansToParcel(TimeLapse time) {
        delegate.explorePathsToKNearestParcels(NUM_OF_POSSIBILITIES, time);
    }

    private void findPlansToDeliveryLocation(TimeLapse time){
        delegate.exploreKShortestPathsTo(PizzaExample.DMAS_MODEL.getAntAcceptor(getCurrentPackage().getDeliveryLocation()), NUM_PATHS_EXPLORING, time, getCurrentPackage());
    }
    /**
     * Sets the preferred plan to best plan of given plans
     * @param plans2
     */
    private void choosePreferredPlan(List<AntPlan> plans2){
        //evaluate plans if any found
        preferredPlan = evaluatePlans(plans2);

        if(preferredPlan.isPresent()) {
            System.out.println(preferredPlan.get().getSchedule());
            //if a preferred plan exists set a current objective
            currentPackage = Optional.of(preferredPlan.get().getObjectivePackage());
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
            //currentPackage = Optional.absent();
        }
    }

    /**
     * Follows a successfully reserved path
     * RESERVEDPLAN HAS TO BE INSTANTIATED
     * @param time
     */
    private void followObjective(TimeLapse time){
        if (isDelivering()) {
            if (getRoadModel().getPosition(this).equals(currentPackage.get().getDeliveryLocation())) {
                // deliver when we arrive
                getPDPModel().deliver(this, currentPackage.get(), time);
                clearObjective();
            } else {
                followPlan(getRoadModel(), time);
            }
        }
        else{//not delivering
            if (getRoadModel().equalPosition(this, currentPackage.get())) {
                // pickup customer
                getPDPModel().pickup(this, currentPackage.get(), time);
                clearObjective();
            }
            else{followPlan(getRoadModel(), time);}
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
    private void followPlan(RoadModel rm, TimeLapse time) {
        if (path.isEmpty()) {
            List<AntAcceptor> temp = new LinkedList<>(reservedPlan.get().getPath());
            for (AntAcceptor acceptor : temp) {
                path.add(PizzaExample.DMAS_MODEL.getLocation(acceptor));
            }
        } else {
            rm.followPath(this, path, time);
        }
    }

    //method to clear the state of the agent
    private void clearObjective() {
        preferredPlan = Optional.absent();
        reservedPlan = Optional.absent();
        delegate.clearObjective();
        //path.remove();
    }

    //creates a unique ID for the agent to be used in reservations
    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }

    /**
     * @return
     */
    public PackageAgent getCurrentPackage() {
        if(currentPackage.isPresent()) {
            return currentPackage.get();
        } else {
            return null;
        }
    }

    /**
     * @return true if agent is currently transporting something to it's destination
     */
    public boolean isDelivering(){
        if(currentPackage.isPresent()) {
            return getPDPModel().containerContains(this, currentPackage.get());
        }
        else {
            return false;
        }
    }

    public Optional<Point> getPosition() {
        if (getRoadModel().containsObject(this)) {
            return Optional.of(getRoadModel().getPosition(this));
        }
        return Optional.absent();
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
        delegate.setRoadModel(getRoadModel());
    }

}
