package MASProject.delegateMAS;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.TransportAgent;
import MASProject.Util.AntPlan;
import Messages.AntAcceptor;
import Messages.ExplorationMessage;
import Messages.IntentionMessage;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommUser;
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
    private final RoadModel roadModel;

    Optional<CommDevice> device;

    //TODO frequency of reconsideration
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

    //TODO
    public List<AntPlan> exploreKShortestPathsTo(AntAcceptor objective, int k, TimeLapse time, PackageAgent objectivePackage){
        List<AntAcceptor> path = new LinkedList<>(); //TODO find shortest route
        ExplorationMessage ant = new ExplorationMessage(ID, getRoadModel(), path, objectivePackage);
        ant.setInitialCost(time.getStartTime());
        return null;
    }

    //TODO
    public List<AntPlan> explorePathsToKNearestParcels(int k, TimeLapse time){
        List<PackageAgent> possibleObjectives = RoadModels.findClosestObjects(getPosition(), getRoadModel(), PackageAgent.class, k);
        List<AntPlan> result = new ArrayList<>();
        for(PackageAgent objective: possibleObjectives) {
            if (alreadyExploring(objective)) {
                continue;
            }
            result.addAll(exploreKShortestPathsTo(objective, NUM_PATHS_EXPLORING, time, objective));
        }
        return result;
    }

    //if ant reaches its destination get its plan and remove ant //TODO
    public List<AntPlan> getExplorationResults() {
        List<ExplorationMessage> temp = new LinkedList<>();
        List<AntPlan> antPlans = new ArrayList<AntPlan>();
        for(ExplorationMessage ant : explorationAnts) {
            if(ant.isDestinationReached()) {
                antPlans.add(ant.createAntPlan());
                temp.add(ant);
            }
        }
        for(ExplorationMessage ant : temp) {
            explorationAnts.remove(ant);
        }
        return antPlans;
    }

    //check if objective AntAcceptor is already being explored
    public boolean alreadyExploring(AntAcceptor objective) {
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
