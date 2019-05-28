package Messages;

import MASProject.Agents.ResourceAgent;
import MASProject.delegateMAS.delegateMAS;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class SmartMessage implements MessageContents {
   //better to pass an ID with the messages
    private int id = 0;
    private final String source;
    private final Parcel destination;
    //private delegateMAS delegate =new delegateMAS();
    private List<ResourceAgent> path = new ArrayList<>();


    SmartMessage(String source, Parcel destination) {
        this.source = source;
        this.destination = destination;
    }
/**
    SmartMessage(int id, String source, Parcel destination, delegateMAS d) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.delegate = d;
    }
**/
    public String getSource() {
        return source;
    }

    public Parcel getDestination() {
        return destination;
    }

    public void trigger(){} //TODO
}
