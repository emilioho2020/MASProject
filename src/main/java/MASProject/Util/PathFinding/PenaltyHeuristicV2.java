package MASProject.Util.PathFinding;

import com.github.rinde.rinsim.geom.GeomHeuristic;
import com.github.rinde.rinsim.geom.GeomHeuristics;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Point;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.Unit;
import java.util.HashMap;
import java.util.Map;

public class PenaltyHeuristicV2 implements GeomHeuristic{

    private final static int PENALTY = 2;
    Map<Point, Map<Point, Integer>> penalties = new HashMap<>();
    //List<Point> penalties = new ArrayList<>();

    public void addPenalty(Point start, Point end){
        if(penalties.containsKey(start)){
            Map<Point, Integer> map = penalties.get(start);
            if (map.containsKey(end)){
                map.put(end, map.get(end)+1);
            }
            else map.put(end, 1);
        }
        else{
            Map<Point, Integer> newMap = new HashMap<Point, Integer>();
            newMap.put(end,1);
            penalties.put(start,newMap);
        }
    }

    @Override
    public double estimateCost(Graph<?> graph, Point from, Point to) {
        double normalCost = GeomHeuristics.euclidean().estimateCost(graph, from, to);
        if (penalties.containsKey(from)){
            if (penalties.get(from).containsKey(to)){
                return penalties.get(from).get(to) * PENALTY * normalCost;
            }
        }
        return normalCost;
    }

    @Override
    public double calculateCost(Graph<?> graph, Point from, Point to) {
        double normalCost = GeomHeuristics.euclidean().calculateCost(graph, from, to);
        if (penalties.containsKey(from)) {
            if (penalties.get(from).containsKey(to)) {
                return penalties.get(from).get(to) * PENALTY * normalCost;
            }
        }
        return normalCost;
    }

    @Override
    public double calculateTravelTime(Graph<?> graph, Point from, Point to, Unit<Length> distanceUnit, Measure<Double, Velocity> speed, Unit<Duration> outputTimeUnit) {
        return GeomHeuristics.euclidean().calculateTravelTime(graph, from, to, distanceUnit, speed, outputTimeUnit);
    }
}
