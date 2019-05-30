package MASProject.Statistics;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Statistic {
    //Pair<waitTime,travelTime>
    private final List<Pair<Integer,Integer>> records;

    public Statistic() {
        records = new LinkedList<>();
    }

    public void registerRecord(Pair<Integer,Integer> record) {
        records.add(record);
    }

    public List<Integer> getWaitingTimes() {
        List<Integer> waitingTimes = new LinkedList<>();
        for (Pair<Integer,Integer> record: records) {
            waitingTimes.add(record.getLeft());
        }
        return waitingTimes;
    }

    public List<Integer> getTravelTimes() {
        List<Integer> travelTimes = new LinkedList<>();
        for (Pair<Integer,Integer> record: records) {
            travelTimes.add(record.getRight());
        }
        return travelTimes;
    }

    public Pair<Integer,Integer> sum() {
        int l = 0;
        int r = 0;
        for(Pair<Integer,Integer> record: records) {
            l += record.getLeft();
            r += record.getRight();
        }
        return Pair.of(l,r);
    }

    public float computeMeanWaitingTime() {
        return sum().getLeft()/records.size();
    }

    public float computeMeanTravelTime() {
        return sum().getRight()/records.size();
    }

    public int computeMinWaitingTime() {
        List<Integer> waitingTimes = getWaitingTimes();
        int minIndex = waitingTimes.indexOf(Collections.min(waitingTimes));
        return waitingTimes.get(minIndex);
    }

    public int computeMinTravelTime() {
        List<Integer> travelTimes = getTravelTimes();
        int minIndex = travelTimes.indexOf(Collections.min(travelTimes));
        return travelTimes.get(minIndex);
    }

    public int computeMaxWaitingTime() {
        List<Integer> waitingTimes = getWaitingTimes();
        int minIndex = waitingTimes.indexOf(Collections.max(waitingTimes));
        return waitingTimes.get(minIndex);
    }

    public int computeMaxTravelTime() {
        List<Integer> travelTimes = getTravelTimes();
        int minIndex = travelTimes.indexOf(Collections.max(travelTimes));
        return travelTimes.get(minIndex);
    }

    public void printStatistics(){
        System.out.println();
        System.out.println("%~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~%");
        System.out.println("                   STATISTICS");
        System.out.println("            Mean waiting time: "+ computeMeanWaitingTime());
        System.out.println("            Mean travel time: "+computeMeanTravelTime());
        System.out.println("            Min waiting time: "+computeMinWaitingTime());
        System.out.println("            Min travel time: "+computeMinTravelTime());
        System.out.println("            Max waiting time: "+computeMaxWaitingTime());
        System.out.println("            Max travel time: "+computeMaxTravelTime());
        System.out.println("%~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~%");
    }
}
