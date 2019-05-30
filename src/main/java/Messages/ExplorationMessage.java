package Messages;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.ResourceAgent;
import MASProject.Agents.TimeSlot;
import MASProject.Agents.TransportAgent;
import MASProject.PizzaExample;
import MASProject.Util.AntPlan;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
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
    private final LinkedHashMap<AntAcceptor, TimeSlot> tempSchedule;
    private final List<AntAcceptor> path;
    private Measure<Double, Duration> startCost;
    private Measure<Double, Duration> costSoFar;
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
        double start = startCost.doubleValue(SI.MILLI(SI.SECOND));
        double end = costSoFar.doubleValue(SI.MILLI(SI.SECOND));
        double temp = end - start;
        //if destination is reached reserve last resource for a longer time because it needs time to eplore again and to pickup/deliver
        double reachedPenalty = resource.equals(getDestination()) ? 20*1000 + 70*1000: 0;
        //reserve slot for [start + cost/2, start + cost + cost/2]
        TimeSlot slot = new TimeSlot(start + (temp/2), end + (temp/2) + reachedPenalty);
        addToScheduledPath(resource, slot);

        if(resource.equals(getDestination())){
            setDestinationReached(true);
        }
        else{
            Point position = resource.getPosition().get();
            double cost = calculateCost(
                    position, getNextAcceptor(resource).getPosition().get(),
                    Measure.valueOf(TransportAgent.SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
            addCost(Measure.valueOf(cost, SI.MILLI(SI.SECOND)));
            propagate(resource);
        }
    }

    public void sendFromTransportAgent(TimeLapse time, Point position){
        setInitialCost(time.getStartTime());
        AntAcceptor currentResource = PizzaExample.DMAS_MODEL.getAntAcceptor(position); //TODO this is so bad
        if (!currentResource.equals(getDestination())){
            double cost = calculateCost(
                position, PizzaExample.DMAS_MODEL.getLocation(getNextAcceptor(path.get(0))),
                Measure.valueOf(TransportAgent.SPEED_KMH, NonSI.KILOMETERS_PER_HOUR).to(SI.METERS_PER_SECOND));
            addCost(Measure.valueOf(cost,SI.MILLI(SI.SECOND)));
            propagate(currentResource);} //TODO this is so bad
    }


    @Override
    public void visit(PackageAgent packageAgent) {
        //TODO
    }

    /************************************************************************
     *SCHEDULE
     **************************************************************************/

    public void addToScheduledPath(AntAcceptor acceptor,TimeSlot slot) {
        tempSchedule.put(acceptor, slot);
        startCost = costSoFar;
    }

    public LinkedHashMap<AntAcceptor,TimeSlot> getTempSchedule() { return tempSchedule; }

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
    public double calculateCost(Point start, Point end, Measure<Double, Velocity> speed) {
        List<Point> rout = new LinkedList<>();
        rout.add(start);
        rout.add(end);
        Measure<Double,Length> distance = getRoadModel().getDistanceOfPath(rout);
        return RoadModels.computeTravelTime(speed, distance, SI.MILLI(SI.SECOND));
    }

    private Measure<Double, Duration> getCostSoFar() {
        return costSoFar;
    }

    public void setInitialCost(double cost) {
        startCost = Measure.valueOf(((path.size()*2+1)*1000)+cost,SI.MILLI(SI.SECOND));
    }

    public void addCost(Measure<Double,Duration> cost) {
        Unit<Duration> unit = SI.MILLI(SI.SECOND);
        costSoFar = Measure.valueOf(startCost.doubleValue(unit) + cost.doubleValue(unit),unit);

    }
}
