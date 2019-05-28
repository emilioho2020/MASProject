package MASProject.Agents;

import MASProject.Plan;
import MASProject.delegateMAS.delegateMAS;
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
public class TransportAgent extends Vehicle {

    //Static fields
    private static AtomicLong idCounter = new AtomicLong();
    public static final double SPEED_KMH = 1d;
    private static final int NUM_OF_POSSIBILITIES = 3;

    //from PDP model
    private Optional<Parcel> curr;

    //the plans-from BDI
    private Optional<Plan> preferredPlan;
    private Optional<Plan> reservedPlan;
    //private List<Plan> plans;

    //Needed so that agent can follow this.
    private Queue<Point> path;

    private final String ID;
    private final RoadModel roadModel;

    //Ants
    private final long frequencyOfExploring = 4000L;
    private final long frequencyOfCommitting = 1000L;

    delegateMAS delegate;

    public TransportAgent(Point startPosition, int capacity, RoadModel rm){
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED_KMH)
                .build()
            );
        curr = Optional.absent();
        ID = createID();
        roadModel = rm;
        reservedPlan = Optional.absent();
        preferredPlan = Optional.absent();
        delegate = new delegateMAS(this, roadModel);
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
        if (!curr.isPresent()) {
            List<Plan> plans2 = findPlansToParcel(time, rm);
            choosePlan(plans2);
        }
        /* In this section we send an intention ant to register the preferred
         * plan. After registering the agent follows the path and refreshes
         * regularly its current intention.
         */
        if (curr.isPresent()) {
            //Basically checks if current objective is already delivered
            //if so return to create new plan
            final boolean inCargo = pm.containerContains(this, curr.get());
            if (!inCargo && !rm.containsObject(curr.get())) {
                // sanity check: if it is not in our cargo AND it is also not on the
                // RoadModel, we cannot go to curr anymore.
                clearObjective();
            } else {
                if (hasReserved()){followObjective(time, rm, pm);}
                else {}
                }
        }
    }

    private List<Plan> findPlansToParcel(TimeLapse time, RoadModel rm) {
        //if no objective yet search for one
        //Get results from ants if any
        List<Plan> plans2 = delegate.getExplorationResults();

        //TODO resend ants!
        if (time.getStartTime() % frequencyOfExploring == 0) {
            //every frequencyOfExploring send ants to explore
            delegate.explorePossibilities(rm);
        }
        return plans2;
    }

    private void choosePlan(List<Plan> plans2){
        //evaluate plans if any found
        preferredPlan = evaluatePlans(plans2);

        if(preferredPlan.isPresent()) {
            //if a preferred plan exists set a current objective
            curr = Optional.of(preferredPlan.get().getObjective());
            delegate.sendIntentionAnt(roadModel, preferredPlan.get());
        }
    }

    /**
     * checks whether the plan has successfully been reserved
     * @return
     */
    private Boolean hasReserved() {
        try {
            return delegate.getIntentionAntResult();
        } catch (Exception e) {
            System.out.println("No reservation.");
            clearObjective();
            return false;
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
        //TODO
        if (time.getStartTime() % frequencyOfCommitting == 0) {
            //every frequencyOfCommitting refresh the reservation
            delegate.refreshReservation(rm);
        }
        if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
            // deliver when we arrive
            pm.deliver(this, curr.get(), time);
        } else {
            // it is still available, go there as fast as possible
            followPlan(rm, time);
            if (rm.equalPosition(this, curr.get())) {
                // pickup customer
                pm.pickup(this, curr.get(), time);
            }
        }
    }

    /**
     * @param plans2
     * @return The best plan according to our heuristics
     * HEURISTIC: travel time
     */
    private Optional<Plan> evaluatePlans(List<Plan> plans2) {
        if(plans2.isEmpty()) {
            return Optional.absent();
        }
        List<Double> durations = new LinkedList<>();
        for(Plan plan : plans2) {
            durations.add(plan.evaluate());
        }
        int minIndex = durations.indexOf(Collections.min(durations));
        return Optional.of(plans2.get(minIndex));
    }


    //get path from plan and follow it.
    //TODO: probably some more code in how agent follows the schedule
    private void followPlan(RoadModel rm, TimeLapse time) {
        if(path.isEmpty()) {
            path.addAll(reservedPlan.get().getPath());
        }

        if(Point.Comparators.XY.compare(path.peek(), rm.getPosition(this)) >= 0) {
            Point temp = path.remove();
            reservedPlan.get().removePoint(temp);
            delegate.removePoint(temp);
        }
        rm.followPath(this, path, time);
    }

    //method to clear the state of the agent
    private void clearObjective() {
        curr = Optional.absent();
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

}
