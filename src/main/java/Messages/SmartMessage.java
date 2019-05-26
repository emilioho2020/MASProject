package Messages;

import MASProject.TransportAgent;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;

public abstract class SmartMessage implements MessageContents {
   //better to pass an ID with the messages
    private final String source;
    private final Parcel destination;

    SmartMessage(String source, Parcel destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public Parcel getDestination() {
        return destination;
    }
}
