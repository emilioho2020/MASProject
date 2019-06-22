package MASProject.delegateMAS;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.TransportAgent;
import MASProject.PizzaExample;
import MASProject.Util.AntPlan;
import MASProject.Util.PathFinding.PathFinder;
import Messages.AntAcceptor;
import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
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

    private final String ID;
    private RoadModel roadModel;

    Optional<CommDevice> device;

    //TODO frequency of reconsideration
    private final long frequencyOfExploring = 4000L;
    private final long frequencyOfCommitting = 1000L;

    /*****************************************************************************
     * CONSTRUCTORS
     ********************************************************************************/
    public delegateMAS(TransportAgent a){
        agent = a;
        ID = createID();
        explorationAnts = new LinkedList<>();
        intentionAnt = Optional.absent();

    }

    public void setDevice(CommDevice device) {
        this.device = Optional.of(device);
    }
    public void setRoadModel(RoadModel rm) {
        roadModel = rm;
    }

    /**
     * creates a unique ID for the agent to be used in reservations
     */
    public static String createID(){return String.valueOf(idCounter.getAndIncrement());}

    public Point getPosition(){return agent.getPosition().get();}


    /*************************************************************************
     * EXPLORATION
     *****************************************************************************/
    //should be void return type because ants don't make plans in one tick
    public void exploreKShortestPathsTo(AntAcceptor objective, int k, TimeLapse time, PackageAgent objectivePackage){
        //List<Point> temp = roadModel.getShortestPathTo(agent,objective); // this is shortest rout but has points instead of AntAcceptors

        Random rng = new Random();

        Point thisPosition = repairPoint(this.getPosition());
        Point objPosition = repairPoint(objective.getPosition().get());
        List<List<Point>> pathsList = PathFinder.findKPenaltyPaths(roadModel,thisPosition,objPosition,3);
        for(List<Point> temp : pathsList) {
            List<AntAcceptor> path = new LinkedList<>();                     // so we convert the points to ant acceptors
            for (Point point : /*pathsList.get(rng.nextInt(pathsList.size()))*/temp) {
                //fixed here
                Point newPoint = point;
                if (!goodPoint(point)) {
                    newPoint = repairPoint(point);
                }
                path.add(PizzaExample.DMAS_MODEL.getAntAcceptor(newPoint));
            }
            ExplorationMessage ant = new ExplorationMessage(ID, getRoadModel(), path, objectivePackage);

            //fixed here
            Point currPos = getPosition();
            if (!goodPoint(currPos)) {
                currPos = repairPoint(currPos);
            }
            ant.sendFromTransportAgent(time, PizzaExample.DMAS_MODEL.getLocation(path.get(0))); //TODO badly written will give problems as soon as we modify something
            explorationAnts.add(ant);
        }
    }

    public boolean goodPoint(Point point) {
        return (point.x % 4 == 0) && (point.y % 4 == 0);
    }

    public Point repairPoint(Point point) {
        double x = point.x;
        double y = point.y;

        if (x % 4 != 0) {
            x = x - (x % 4);
        }
        if (y % 4 != 0) {
            y = y - (y % 4);
        }
        System.out.println(x+" "+y);
        return new Point(x, y);
    }

    public void explorePathsToKNearestParcels(int k, TimeLapse time){
        List<PackageAgent> possibleObjectives = RoadModels.findClosestObjects(getPosition(), getRoadModel(), PackageAgent.class, k);
        for (PackageAgent objective: agent.badPackages) {
            if(possibleObjectives.contains(objective)){
                possibleObjectives.remove(objective);
            }
        }
        for(PackageAgent objective: possibleObjectives) {
            if (alreadyExploring(objective)) {
                continue;
            }
            exploreKShortestPathsTo(objective, NUM_PATHS_EXPLORING, time, objective);
        }
    }

    //if ant reaches its destination get its plan and remove ant
    public List<AntPlan> getExplorationResults() {
        List<ExplorationMessage> temp = new LinkedList<>();
        List<AntPlan> antPlans = new ArrayList<>();
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.isDestinationReached()) {
                antPlans.add(ant.createAntPlan());
                temp.add(ant);
            }
        }
        //for(ExplorationMessage ant : temp) {
        //    explorationAnts.remove(ant);
        //}
        return antPlans;
    }

    //check if objective AntAcceptor is already being explored
    public boolean alreadyExploring(AntAcceptor objective) {
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.getDestination().equals(objective) || agent.badPackages.contains(objective))
                return true;
        }
        return false;
    }


    /******************************************************************************************
     *  INTENTION
     *************************************************************************************/
    /**
     * Sends an intention ant to involved Resource agents
     * @param antPlan
     */
    public void sendIntentionAnt(AntPlan antPlan) {
        IntentionMessage ant = new IntentionMessage(ID, getRoadModel(), antPlan);
        device.get().send(ant, ant.getFirstAcceptor());
        intentionAnt = Optional.of(ant);
    }

    /**
     *
     * @return boolean indicating whether ant has made reservation yet
     * @throws Exception if timeslot already taken
     */
    public Boolean isReservationMade() throws Exception{
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

    protected RoadModel getRoadModel(){
        return roadModel;
    }
}
