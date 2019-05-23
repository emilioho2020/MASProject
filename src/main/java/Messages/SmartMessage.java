package Messages;

import MASProject.TransportAgent;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.geom.Point;

public abstract class SmartMessage implements MessageContents {
   //better to pass an ID with the messages
    private String source;
    private Point destination;

    SmartMessage(String source, Point destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }
}
