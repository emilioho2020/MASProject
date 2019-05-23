package emileTest;

import com.github.rinde.rinsim.core.model.comm.*;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

public class Agent extends Vehicle implements CommUser{

    static final private double RANGE = 1.0;
    static final private double RELIABILITY = 0.7;
    static final private double SPEED = 50;

    private Queue<Point> path;
    private Optional<CommDevice> device;

    private final double range;
    private final double reliability;

    private final String name;
    private final Set<CommUser> knownAgents;
    private Optional<Parcel> curr;

    Agent(Point startPosition, int capacity, String nm) {
        super(VehicleDTO.builder()
            .capacity(capacity)
            .startPosition(startPosition)
            .speed(SPEED)
            .build());
        //A name for the agent
        name = nm;

        //The communication device
        device = Optional.absent();
        knownAgents = new HashSet<>();

        //Needed for building the communication device
        range = RANGE;
        reliability = RELIABILITY;

        //We need to keep track of the path to destination to propagate it
        //through messages
        path = new LinkedList<>();

        //What Parcel is currently on the agent
        curr = Optional.absent();
    }

    @Override
    public Optional<Point> getPosition() {
        return Optional.of(getRoadModel().getPosition(this));
    }

    @Override
    public void setCommDevice(CommDeviceBuilder builder) {
        if (range >= 0) {
            builder.setMaxRange(range);
        }
        device = Optional.of(builder
                .setReliability(reliability)
                .build());
    }

    @Override
    public void tickImpl(TimeLapse timeLapse) {

    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}

    @Override
    public double getSpeed() {
        return SPEED;
    }

    @Override
    public String toString() {
        return name;
    }

    enum Ant implements MessageContents {
        EXPLORATION_ANT, FEASIBILITY_ANT, INTENTION_ANT;
    }

    static class MyNameIs implements MessageContents {
        private final String name;

        MyNameIs(String nm) {
            name = nm;
        }

        String getName() {
            return name;
        }
    }
}

