package Messages;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.ResourceAgent;
import MASProject.Agents.TransportAgent;
import MASProject.Util.AntPlan;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.*;

public class ExplorationMessage extends SmartMessage {
    private final LinkedHashMap<AntAcceptor,Measure<Double,Duration>> tempSchedule;
    private final List<AntAcceptor> path;
    private Measure<Double, Duration> costSoFar = Measure.valueOf(0d, SI.SECOND);
    private boolean destinationReached = false;

    public ExplorationMessage(String source, RoadModel roadModel, List<AntAcceptor> path, PackageAgent objectivePackage) {
        super(source, roadModel,objectivePackage);
        //linked hash map to preserve insertion order
        this.tempSchedule = new LinkedHashMap<>();
        this.path = path;
    }

    @Override
    public List<AntAcceptor> getPath() {
        return path;
    }

    @Override
    public void visit(ResourceAgent resource) {
        //Add the cost to come to this node to ants plan
        addToScheduledPath(resource, getCostSoFar());

        if(resource.equals(getDestination())){
            setDestinationReached(true);
        }
        else{
            Point position = resource.getPosition().get();
            double cost = calculateCost(
                    position, getNextAcceptor(resource).getPosition().get(),
                    Measure.valueOf(TransportAgent.SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
            addCost(Measure.valueOf(cost, Duration.UNIT));
            propagate(resource);
        }
    }

    @Override
    public void visit(PackageAgent packageAgent) {
        //TODO
    }

    /************************************************************************
     *SCHEDULE
     **************************************************************************/

    public void addToScheduledPath(AntAcceptor acceptor, Measure<Double,Duration> time) {
        tempSchedule.put(acceptor, time);
    }

    public LinkedHashMap<AntAcceptor,Measure<Double,Duration>> getTempSchedule() { return tempSchedule; }

    public AntPlan createAntPlan() throws RuntimeException{
        if(!isDestinationReached()){throw new RuntimeException("No completed AntPlan yet");}
        else{return new AntPlan(getTempSchedule(),getObjectivePackage());}
    }

    /**********************************************************************************
     * DESTINATION
     * *******************************************************************************/
    public boolean isDestinationReached() {
        return destinationReached;
    }

    public void setDestinationReached(boolean destinationReached) {
        this.destinationReached = destinationReached;
    }


    /*********************************************************************************************
     *COSTS
     *********************************************************************************/
    //TODO change metric, calculate cost based on ResourceAgent (not points)
    private double calculateCost(Point start, Point end, Measure<Double, Velocity> speed) {
        List<Point> rout = new LinkedList<>();
        rout.add(start);
        rout.add(end);
        Measure<Double,Length> distance = getRoadModel().getDistanceOfPath(rout);
        return RoadModels.computeTravelTime(speed, distance, SI.SECOND);
    }

    private Measure<Double, Duration> getCostSoFar() {
        return costSoFar;
    }

    public void setInitialCost(double cost) {
        costSoFar = Measure.valueOf(cost,SI.SECOND);
    }

    public void addCost(Measure<Double,Duration> cost) {
        Unit<Duration> unit = costSoFar.getUnit();
        costSoFar = Measure.valueOf(costSoFar.doubleValue(unit) + cost.doubleValue(unit),unit);

    }
}
