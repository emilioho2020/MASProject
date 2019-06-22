package MASProject.Util.PathFinding;

import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {
    public PathFinder() {

    }

    public static List<List<Point>> findKPenaltyPaths(RoadModel rm, Point start, Point destination, int k){
        PenaltyHeuristicV2 heur = new PenaltyHeuristicV2();
        System.out.println("Searching"+ k + "penalty driven routes from point " + start + "to point " + destination);
        List<List<Point>> S = new ArrayList<>();
        int numFails = 0;
        int maxFails = 15;
        double alpha = Math.random();

        DynamicGraphRoadModel graphRm = (DynamicGraphRoadModel) rm;

        while(S.size() < k & numFails < maxFails){
            List<Point> p = Graphs.shortestPath((graphRm.getGraph()) , start, destination, heur);
            if (S.contains(p)){numFails +=1;}
            else {
                S.add(p);
                numFails = 0;}

            for(int i = 0; i < p.size()-1; i++){
                Point point = p.get(i);
                double beta = Math.random();
                if (beta < alpha){
                    heur.addPenalty(point,p.get(i+1));
                }
            }
        }
        System.out.println("found paths:" + S + "\n"+ "end of paths");
        return S;
    }
}
