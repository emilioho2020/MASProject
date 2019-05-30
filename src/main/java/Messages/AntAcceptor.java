package Messages;

import MASProject.Model.DMASNode;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.road.RoadUser;

public interface AntAcceptor extends CommUser, RoadUser, DMASNode {

    void deployAnt(SmartMessage ant);

    void propagate(SmartMessage ant, AntAcceptor next);

}
