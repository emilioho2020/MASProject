package MASProject.Agents;

public class TimeSlot {
    private final double start;
    private final double end;
    TimeSlot(double start,double end) {
        this.start = start;
        this.end = end;
    }

    public double getStartTime() {
        return start;
    }

    public double getEndTime() {
        return end;
    }

}
