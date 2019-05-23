package Messages;

import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IntentionMessage extends SmartMessage {
    private Map<Point,TimeLapse> scheduledPath;

    IntentionMessage(String source, Point destination, Map<Point,TimeLapse> scheduledPath) {
        super(source, destination);
        this.scheduledPath = scheduledPath;
    }

    public Map<Point,TimeLapse> getScheduledPath() {
        return scheduledPath;
    }

    public Point getNextPoint(Point curr) {
        List<Point> points = new ArrayList<>(scheduledPath.keySet());
        return points.get(points.indexOf(curr)+1);
    }
}
