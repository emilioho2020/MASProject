package Messages;

import com.github.rinde.rinsim.geom.Point;

import java.util.LinkedList;
import java.util.Queue;

public class ExplorationMessage extends SmartMessage {
    private final Queue<Point> path;

    ExplorationMessage(String source,Point destination) {
        super(source, destination);
        path = new LinkedList<>();
    }

    public void addPointToPath(Point point) {
        path.add(point);
    }

    public Queue<Point> getPath() {
        return path;
    }
}
