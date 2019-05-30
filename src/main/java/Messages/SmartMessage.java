package Messages;

import MASProject.Agents.PackageAgent;
import MASProject.Agents.ResourceAgent;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.road.RoadModel;

import java.util.*;

public abstract class SmartMessage implements MessageContents {
    private final String source;
    private RoadModel roadModel;
    private final PackageAgent objectivePackage;

    SmartMessage(String source, RoadModel rm, PackageAgent objectivePackage) {
        this.source = source;
        this.roadModel = rm;
        this.objectivePackage = objectivePackage;
    }

    public String getSource() {
        return source;
    }

    public AntAcceptor getDestination() {return getPath().get(getPath().size()-1);}

    public PackageAgent getObjectivePackage(){return objectivePackage;}

    public RoadModel getRoadModel() {return roadModel;}

    /**
     * Returns the acceptor this ant has to visit after the given one
     * If given acceptor is last one on path, throw exception //TODO
     * @param acceptor
     * @return the next acceptor this ant has to visit
     */
    public AntAcceptor getNextAcceptor(AntAcceptor acceptor){
        int currentIndex = getPath().indexOf(acceptor);
        //if (currentIndex == path.size()-1){throw new Exception("This already is the last acceptor node");}
        return getPath().get(currentIndex+1);
    }

    public void propagate(AntAcceptor acceptor){
        acceptor.propagate(this, getNextAcceptor(acceptor));
    }

    public AntAcceptor getFirstAcceptor(){
        return getPath().get(0);
    }

    /*********************************************************
     * ABSTRACT
     ***********************************************************/

    public abstract List<AntAcceptor> getPath();

    public abstract void visit(ResourceAgent resource);

    public abstract void visit(PackageAgent packageAgent);

}
