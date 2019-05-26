package MASProject;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Plan {

    private final Map<Point,TimeLapse> schedule;
    private final Parcel objective;


    public Plan(LinkedHashMap<Point,TimeLapse> schedule, Parcel objective) {
        this.schedule = schedule;
        this.objective = objective;
    }

    public LinkedHashMap<Point,TimeLapse> getSchedule() {
        return (LinkedHashMap) schedule;
    }

    public Parcel getObjective() {
        return objective;
    }

    //TODO: not sure about evaluatioin metric; temporarily long
    public long evaluate() {
        return 0;
    }
}
