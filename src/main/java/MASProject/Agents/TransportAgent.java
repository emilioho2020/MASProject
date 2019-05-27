package MASProject.Agents;

import MASProject.Plan;
import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
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
    private static final double SPEED = 1d;
    private static final int NUM_OF_POSSIBILITIES = 3;

    //from PDP model
    private Optional<Parcel> curr;

    //the plans-from BDI
    private Optional<Plan> preferredPlan;
    private Optional<Plan> intendedPlan;
    private List<Plan> plans;
    //probably all plans should should contain path from curr location
    //until pickUpDelivery location.

    private BehaviourModule behaviormodule = new BehaviourModule();
    private final String ID;
    private final RoadModel roadModel;

    //Communication
    private final double range = 4.2;  //TODO set range
    private final double reliability = 1;
    Optional<CommDevice> device;

    //Ants
    private final long frequencyOfExploring = 4000L;
    private final long frequencyOfCommitting = 1000L;
    private List<ExplorationMessage> explorationAnts;
    private Optional<IntentionMessage> intentionAnt;

    /**
     */
    //TODO
    public TransportAgent(Point startPosition, int capacity, RoadModel rm){
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build()
            );
        curr = Optional.absent();
        ID = createID();
        roadModel = rm;
        intendedPlan = Optional.absent();
        preferredPlan = Optional.absent();
        plans = new LinkedList<>();
        explorationAnts = new LinkedList<>();
        intentionAnt = Optional.absent();
    }

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
            //if no objective yet search for one
            //Get results from ants if any
            getExplorationResults();

            if(time.getStartTime() % frequencyOfExploring == 0) {
                //every frequencyOfExploring send ants to explore
                explorePossibilities(rm);
            }
            //evaluate plans if any found
            evaluatePlans();

            if(preferredPlan.isPresent()) {
                //if a preferred plan exists set a current objective
                curr = Optional.of(preferredPlan.get().getObjective());
            }
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
                return;

            } else {

                if(!intentionAnt.isPresent()) {
                    //if no intentionAnt send one to reserve route
                    sendIntentionAnt(rm);
                }
                if(!intendedPlan.isPresent()) {
                    //if no intendedPlan query intentionAnt for one
                    try {
                        getIntentionAntResult();
                    } catch (Exception e) {
                        System.out.println("No reservation.");
                        clearObjective();
                        return;
                    }
                } else {
                    //an intendedPlan exists
                    if (time.getStartTime() % frequencyOfCommitting == 0) {
                        //every frequencyOfCommitting refresh the reservation
                        refreshReservation(rm);
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
            }
        }
    }

    /* This method is responsible for exploring the different possible objectives.
     * For each "new" objective a new Exploration Ant is created to be propagated
     * through the ResourceAgents network. The ants are stored in a list so that
     * the results they find can be later evaluated.
     */
    private void explorePossibilities(RoadModel rm) {
        List<Parcel> possibleObjectives = RoadModels.findClosestObjects(rm.getPosition(this),rm, Parcel.class, NUM_OF_POSSIBILITIES);
        for(Parcel objective: possibleObjectives) {
            if(alreadyExploring(objective)) { continue;}
            Queue<Point> path = new LinkedList<>(rm.getShortestPathTo(this,objective.getPickupLocation()));
            ExplorationMessage ant = new ExplorationMessage(ID, objective, path);
            device.get().send(ant, ant.getNextResource(rm, rm.getPosition(this)));
            explorationAnts.add(ant);
        }
    }

    //sends the intention ant to the preferredPlans destination
    private void sendIntentionAnt(RoadModel rm) {
        IntentionMessage ant = new IntentionMessage(ID, curr.get(), preferredPlan.get().getSchedule());
        device.get().send(ant, ant.getNextResource(rm, rm.getPosition(this)));
        intentionAnt = Optional.of(ant);
    }

    //TODO: still need to implement this
    private void refreshReservation(RoadModel rm) {

    }

    //if ant reaches its destination get its plan and remove ant
    private void getExplorationResults() {
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.isDestinationReached()) {
                plans.add(new Plan(ant.getScheduledPath(), ant.getDestination()));
                explorationAnts.remove(ant);
            }
        }
    }

    //if destination is reached get plan from ant
    private void getIntentionAntResult() throws Exception{
        if(intentionAnt.get().isNoReservation()) {
            throw new Exception();
        }
        if(intentionAnt.get().isDestinationReached()) {
            intendedPlan = Optional.of(preferredPlan.get());
        }
    }

    //evaluate by shortest travel time
    private void evaluatePlans() {
        if(plans.isEmpty()) {
            return;
        }
        List<Long> durations = new LinkedList<>();
        for(Plan plan : plans) {
            //still need to implement plan.evaluate()
            durations.add(plan.evaluate());
        }
        int minIndex = durations.indexOf(Collections.min(durations));
        preferredPlan = Optional.of(plans.get(minIndex));
    }

    //check if objective is already being explored
    private boolean alreadyExploring(Parcel objective) {
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.getDestination().equals(objective))
                return true;
        }
        return false;
    }

    //TODO: still need to implement this
    private void followPlan(RoadModel rm, TimeLapse time) {
        //rm.followPath(this, path, time);
    }

    //method to clear the state of the agent
    private void clearObjective() {
        curr = Optional.absent();
        preferredPlan = Optional.absent();
        intendedPlan = Optional.absent();
        intentionAnt = Optional.absent();
    }

    @Override
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
    }

    //creates a unique ID for the agent to be used in reservations
    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }
}
