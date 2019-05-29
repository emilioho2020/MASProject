package Messages;

import com.github.rinde.rinsim.core.model.comm.CommUser;

public interface AntAcceptor extends CommUser {

    public void deployAnt(SmartMessage ant);

    public void propagate(SmartMessage ant, AntAcceptor next);

}
