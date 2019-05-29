package MASProject.delegateMAS;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.TransportAgent;
import MASProject.Plan;
import Messages.AntAcceptor;
import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class delegateMAS {

    private static final int NUM_PATHS_EXPLORING = 3;
    private final TransportAgent agent;

    private List<ExplorationMessage> explorationAnts;
    private Optional<IntentionMessage> intentionAnt;

    private static AtomicLong idCounter = new AtomicLong();
    public static final double SPEED_KMH = 1d;
    private static final int NUM_OF_POSSIBILITIES = 3;

    private final String ID;
    private final RoadModel roadModel;

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

    }

    public void setDevice(CommDevice device) {
        this.device = Optional.of(device);
    }

    /**
     * creates a unique ID for the agent to be used in reservations
     */
    public static String createID(){return String.valueOf(idCounter.getAndIncrement());}

    public Point getPosition(){return agent.getPosition().get();}


    /*************************************************************************
     * EXPLORATION
     *****************************************************************************/
    /* This method is responsible for exploring the different possible objectives.
     * For each "new" objective a new Exploration Ant is created to be propagated
     * through the ResourceAgents network. The ants are stored in a list so that
     * the results they find can be later evaluated.
     */
    /**
    public void explorePossibilities(TimeLapse time, boolean delivering) {
        RoadModel rm = getRoadModel();
        if(!delivering) {
            List<Parcel> possibleObjectives = RoadModels.findClosestObjects(getRoadModel().getPosition(agent), rm, Parcel.class, NUM_OF_POSSIBILITIES);
            for(Parcel objective: possibleObjectives) {
                if(alreadyExploring(objective)) { continue;}
                Queue<Point> path = new LinkedList<>(rm.getShortestPathTo(agent,objective.getPickupLocation()));
                ExplorationMessage ant = new ExplorationMessage(ID, objective, path, getRoadModel());
                ant.setInitialCost(time.getStartTime());
                CommUser nextResource = ant.getNextResource(rm.getPosition(agent));

                double cost = ant.calculateCost(
                        rm.getPosition(agent), nextResource.getPosition().get(),
                        Measure.valueOf(SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
                ant.addCost(Measure.valueOf(cost, Duration.UNIT));
                device.get().send(ant, nextResource);
                explorationAnts.add(ant);
            }
        } else if(delivering) {
            Parcel objective = agent.getCurr();
            Queue<Point> path = new LinkedList<>(rm.getShortestPathTo(agent,objective.getDeliveryLocation()));
            ExplorationMessage ant = new ExplorationMessage(ID, objective, path, getRoadModel());
            ant.setInitialCost(time.getStartTime());
            CommUser nextResource = ant.getNextResource(rm.getPosition(agent));

            double cost = ant.calculateCost(
                    rm.getPosition(agent), nextResource.getPosition().get(),
                    Measure.valueOf(SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
            ant.addCost(Measure.valueOf(cost, Duration.UNIT));
            device.get().send(ant, nextResource);
            explorationAnts.add(ant);
        }

    }
     */
    //TODO
    public List<Plan> exploreKShortestPathsTo(AntAcceptor objective, int k, TimeLapse time){
        Queue<Point> path = new LinkedList<>(getRoadModel().getShortestPathTo(agent,objective);
        ExplorationMessage ant = new ExplorationMessage(ID, objective, path, getRoadModel());
        ant.setInitialCost(time.getStartTime());
        CommUser nextResource = ant.getNextResource(getRoadModel().getPosition(agent));
        return null;
    }

    //TODO
    public List<Plan> explorePathsToKNearestParcels(int k, TimeLapse time){
        List<PackageAgent> possibleObjectives = RoadModels.findClosestObjects(getPosition(), getRoadModel(), PackageAgent.class, k);
        List<Plan> result = new ArrayList<>();
        for(PackageAgent objective: possibleObjectives) {
            if (alreadyExploring(objective)) {
                continue;
            }
            result.addAll(exploreKShortestPathsTo(objective, NUM_PATHS_EXPLORING, time));
        }
        return result;
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
    /**
     * Sends an intention ant to involved Resource agents
     * @param plan
     */
    public void sendIntentionAnt(Plan plan) {
        IntentionMessage ant = new IntentionMessage(ID, plan.getObjective(), plan.getSchedule(), getRoadModel());
        device.get().send(ant, ant.getNextResource(getRoadModel().getPosition(agent)));
        intentionAnt = Optional.of(ant);
    }

    public void refreshReservation() {
        Point curr = getRoadModel().getPosition(agent);
        Point flooredPoint = new Point(Math.floor(curr.x),Math.floor(curr.y));
        IntentionMessage ant = intentionAnt.get();
        List<Point> points = new ArrayList<>(ant.getPath());
        if(flooredPoint.equals(points.get(points.size() - 1))) {
            device.get().send(ant, ant.getResourceAt(flooredPoint));
        }else{
            device.get().send(ant, ant.getNextResource(flooredPoint));
        }
    }

    /**
     *
     * @return boolean indicating whether ant has made reservation yet
     * @throws Exception if timeslot already taken
     */
    public Boolean getIntentionAntResult() throws Exception{
        if(intentionAnt.isPresent()) {
            if (intentionAnt.get().isNoReservation()) {
                throw new Exception();
            }
            return (intentionAnt.get().isDestinationReached());
        } else {
            return false;
        }
    }

    /**
     * deletes all ants
     */
    public void clearObjective() {
        intentionAnt = Optional.absent();
        explorationAnts.clear();
    }

    //TODO what is this
    public void removePoint(Point point) {
        intentionAnt.get().removePoint(point);
    }

    protected RoadModel getRoadModel(){
        return roadModel;
    }
}
