package MASProject.Agents;

import javax.measure.Measure;
import javax.measure.unit.SI;

public class TimeSlot {
    private final double start;
    private final double end;
    public TimeSlot(double start,double end) {
        this.start = start;
        this.end = end;
    }

    public double getStartTime() {
        return start;
    }

    public double getEndTime() {
        return end;
    }

    @Override
    public String toString() {
        return "["+ Measure.valueOf(start, SI.MILLI(SI.SECOND)).to(SI.SECOND)+","+Measure.valueOf(end, SI.MILLI(SI.SECOND)).to(SI.SECOND)+")";
    }
}
