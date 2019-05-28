package Messages;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

public interface AntAcceptor {

    public void handleAnt(IntentionMessage ant);

    public void handleAnt(ExplorationMessage ant);

    public void propagate(SmartMessage ant, RoadModel rm, Point position);
}
