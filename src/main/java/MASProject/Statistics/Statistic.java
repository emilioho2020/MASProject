package MASProject.Statistics;

import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

public class Statistic {
    private final List<Pair<Integer,Integer>> records;

    public Statistic() {
        records = new LinkedList<>();
    }

    public void registerRecord(Pair<Integer,Integer> record) {
        records.add(record);
    }

    public float computeMeanTravelTime() {
        return 0;
    }

    public float computeMeanWaitingTime() {
        return 0;
    }

    public int computeMinTravelTime() {
        return 0;
    }

    public int computeMinWaitingTime() {
        return 0;
    }

    public int computeMaxTravelTime() {
        return 0;
    }

    public int computeMaxWaitingTime() {
        return 0;
    }
}
