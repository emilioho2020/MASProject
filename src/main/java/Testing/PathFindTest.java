package Testing;

import MASProject.Util.PathFinding.PathFinder;
import MASProject.Util.PathFinding.PenaltyHeuristicV2;
import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.GeomHeuristics;
import com.github.rinde.rinsim.geom.Graph;
import Graph.GraphCreator;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class PathFindTest {

    private static final int CARDINALITY = 8;
    private static final double VEHICLE_LENGTH = 2d;

    public static void main(String[] args) {
        Graph graph = GraphCreator.createGraph(VEHICLE_LENGTH, CARDINALITY);
        //System.out.println(graph);
        Point start = new Point(4,4);
        Point destination = new Point(8,8);
        List<Point> p = Graphs.shortestPathEuclideanDistance(graph , start, destination);
        //double cost = GeomHeuristics.euclidean().calculateCost(graph, start, destination);
        System.out.println(p);
        //System.out.println("cost: " + cost);
        List<List<Point>> S = findKPenaltyPaths(graph,start, destination, 3);
        System.out.println(S);
    }


    public static List<List<Point>> findKPenaltyPaths(Graph g, Point start, Point destination, int k){
        PenaltyHeuristicV2 heur = new PenaltyHeuristicV2();
        System.out.println("Searching"+ k + "penalty driven routes from point " + start + "to point " + destination);
        List<List<Point>> S = new ArrayList<>();
        int numFails = 0;
        int maxFails = 15;
        double alpha = Math.random();

        while(S.size() < k & numFails < maxFails){
            List<Point> p = Graphs.shortestPath(g, start, destination, heur);
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
