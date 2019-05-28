package MASProject.delegateMAS;

import MASProject.Agents.TransportAgent;
import MASProject.Plan;
import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class delegateMAS implements CommUser{

    private final TransportAgent agent;
    //Ants

    private List<ExplorationMessage> explorationAnts;
    private Optional<IntentionMessage> intentionAnt;

    private static AtomicLong idCounter = new AtomicLong();
    public static final double SPEED_KMH = 1d;
    private static final int NUM_OF_POSSIBILITIES = 3;

    private final String ID;
    private final RoadModel roadModel;

    //Communication
    private final double range = 4.2;
    private final double reliability = 1;
    Optional<CommDevice> device;

    private final long frequencyOfExploring = 4000L;
    private final long frequencyOfCommitting = 1000L;

    /*****************************************************************************
     * CONSTRUCTORS
     ********************************************************************************/
    public delegateMAS(TransportAgent a, RoadModel rm){
        agent = a;
        ID = createID();
        roadModel = rm;
        explorationAnts = new LinkedList<>();
        intentionAnt = Optional.absent();
        device = Optional.absent();
    }

    /*************************************************************************
     * EXPLORATION
     *****************************************************************************/


    /* This method is responsible for exploring the different possible objectives.
     * For each "new" objective a new Exploration Ant is created to be propagated
     * through the ResourceAgents network. The ants are stored in a list so that
     * the results they find can be later evaluated.
     */

    /**
     * Sends exploration ants to nearest parcels
     * saves explorationAnts in list
     * @param rm
     */
    public void explorePossibilities(RoadModel rm) {
        List<Parcel> possibleObjectives = RoadModels.findClosestObjects(rm.getPosition(agent),rm, Parcel.class, NUM_OF_POSSIBILITIES);
        for(Parcel objective: possibleObjectives) {
            if(alreadyExploring(objective)) { continue;}
            Queue<Point> path = new LinkedList<>(rm.getShortestPathTo(agent,objective.getPickupLocation()));
            ExplorationMessage ant = new ExplorationMessage(ID, objective, path);
            CommUser nextResource = ant.getNextResource(rm, rm.getPosition(agent));

            double cost = ant.calculateCost(
                    rm, rm.getPosition(agent), nextResource.getPosition().get(),
                    Measure.valueOf(SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
            ant.addCost(Measure.valueOf(cost, Duration.UNIT));
            device.get().send(ant, nextResource);
            explorationAnts.add(ant);
        }
    }

    //if ant reaches its destination get its plan and remove ant
    public List<Plan> getExplorationResults() {
        List<ExplorationMessage> temp = new LinkedList<>();
        List<Plan> plans = new ArrayList<Plan>();
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.isDestinationReached()) {
                plans.add(new Plan(ant.getScheduledPath(), ant.getDestination()));
                temp.add(ant);
            }
        }
        for(ExplorationMessage ant : temp) {
            explorationAnts.remove(ant);
        }
        return plans;
    }


    //check if objective is already being explored
    public boolean alreadyExploring(Parcel objective) {
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.getDestination().equals(objective))
                return true;
        }
        return false;
    }


    /******************************************************************************************
     *  INTENTION
     *************************************************************************************/
    //sends the intention ant to the preferredPlans destination todo add time

    /**
     * Sends an intention ant to involved Resource agents
     * @param rm
     * @param plan
     */
    public void sendIntentionAnt(RoadModel rm, Plan plan) {
        IntentionMessage ant = new IntentionMessage(ID, plan.getObjective(), plan.getSchedule());
        device.get().send(ant, ant.getNextResource(rm, rm.getPosition(agent)));
        intentionAnt = Optional.of(ant);
    }

    //since the agent may have moved the current location may not be in the plan
    //all nodes have integer coordinates so we floor the point of the current location,
    //then we send the ant to the next point.(following getNextResource method)
    public void refreshReservation(RoadModel rm) {
        Point curr = rm.getPosition(agent);
        Point flooredPoint = new Point(Math.floor(curr.x),Math.floor(curr.y));
        IntentionMessage ant = intentionAnt.get();
        device.get().send(ant, ant.getNextResource(rm, flooredPoint));
    }

    /**
     *
     * @return boolean indicating whether ant has made reservation yet
     * @throws Exception if timeslot already taken
     */
    public Boolean getIntentionAntResult() throws Exception{
        if(intentionAnt.get().isNoReservation()) {
            throw new Exception();
        }
        return (intentionAnt.get().isDestinationReached());
    }

    //method to clear the state of the agent
    public void clearObjective() {
        intentionAnt = Optional.absent();
    }


    //creates a unique ID for the agent to be used in reservations
    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }


    @Override
    public Optional<Point> getPosition() {
        return agent.getPosition();
    }

    public void removePoint(Point point) {
        intentionAnt.get().removePoint(point);
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

    protected RoadModel getRoadModel(){
        return roadModel;
    }
}
