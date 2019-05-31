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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PenaltyHeuristic implements GeomHeuristic{

    private final static int PENALTY = 3;
    //Map<Point, int> penalties = new HashMap<>();
    List<Point> penalties = new ArrayList<>();

    public void addPenalty(Point p){
        penalties.add(p);
    }

    @Override
    public double estimateCost(Graph<?> graph, Point from, Point to) {
        double normalCost = GeomHeuristics.euclidean().estimateCost(graph, from, to);
        if (penalties.contains(from) || penalties.contains(to)) {
            return 3 * normalCost;
        }
        else return normalCost;
    }

    @Override
    public double calculateCost(Graph<?> graph, Point from, Point to) {
        double normalCost = GeomHeuristics.euclidean().calculateCost(graph, from, to);
        if (penalties.contains(from) || penalties.contains(to)) {
            return 3 * normalCost;
        }
        else return normalCost;
    }

    @Override
    public double calculateTravelTime(Graph<?> graph, Point from, Point to, Unit<Length> distanceUnit, Measure<Double, Velocity> speed, Unit<Duration> outputTimeUnit) {
        return GeomHeuristics.euclidean().calculateTravelTime(graph, from, to, distanceUnit, speed, outputTimeUnit);
    }
}
