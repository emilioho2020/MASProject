package MASProject.Util.PathFinding;

import Messages.AntAcceptor;
import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadPath;
import com.github.rinde.rinsim.geom.GeomHeuristics;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {
    private PenaltyHeuristic heur = new PenaltyHeuristic();

    public PathFinder() {

    }

    public static List<RoadPath> findKShortestPaths(RoadModel rm, Point start, Point end){
        return null;
    }

    public List<List<Point>> findKPenaltyPaths(RoadModel rm, Point start, Point destination, int k){
        List<List<Point>> S = new ArrayList<>();
        int numFails = 0;
        int maxFails = 3;
        double alpha = Math.random();

        DynamicGraphRoadModel graphRm = (DynamicGraphRoadModel) rm;

        while(S.size() < k & numFails < maxFails){
            List<Point> p = Graphs.shortestPath((graphRm.getGraph()) , start, destination, heur);
            if (S.contains(p)){numFails +=1;}
            else {
                S.add(p);
                numFails = 0;}

            for(Point point : p){
                double beta = Math.random();
                if (beta < alpha){
                    heur.addPenalty(point);
                }
            }
        }
        return S;
    }
}
