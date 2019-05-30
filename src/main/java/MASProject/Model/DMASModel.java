package MASProject.Model;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.ResourceAgent;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Point;

import java.util.HashMap;
import java.util.Map;

public class DMASModel {
    private Map<Point,DMASNode> model = new HashMap<>();

    public DMASModel(){};

    public void register(DMASNode node){
        model.put(node.getPosition().get(), node);
    }

    public DMASNode getNode(Point point){
        return model.get(point);
    }

}
