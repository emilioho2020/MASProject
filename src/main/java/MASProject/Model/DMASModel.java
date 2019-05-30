package MASProject.Model;

import MASProject.Agents.ResourceAgent;
import Messages.AntAcceptor;
import com.github.rinde.rinsim.geom.Point;

import java.util.HashMap;
import java.util.Map;

public class DMASModel {

    private final Map<Point, AntAcceptor> locationToAcceptorMap;
    private final Map<AntAcceptor, Point> acceptorToLocationMap;

    public DMASModel(){
        locationToAcceptorMap = new HashMap<>();
        acceptorToLocationMap = new HashMap<>();
    }

    public void addAntAcceptor(Point point, AntAcceptor acceptor) {
        locationToAcceptorMap.put(point, acceptor);
    }

    public void addLocation(AntAcceptor acceptor, Point point) {
        acceptorToLocationMap.put(acceptor, point);
    }

    public AntAcceptor getAntAcceptor(Point point) {
        if (locationToAcceptorMap.containsKey(point)) {
            return locationToAcceptorMap.get(point);
        } else {
            System.out.println("No resourceAgent in point: "+point);
        }
        return null;
    }

    public Point getLocation(AntAcceptor acceptor) {
        if (acceptorToLocationMap.containsKey(acceptor)) {
            return acceptorToLocationMap.get(acceptor);
        } else {
            System.out.println("No location for agent: "+acceptor);
        }
        return null;
    }
}
