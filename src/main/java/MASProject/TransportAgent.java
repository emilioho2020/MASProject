package MASProject;

import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Our implementation of a simple agent in a simple PDP problem : delivering pizzas in time.
 */
public class TransportAgent extends Vehicle implements CommUser {
    private static final double SPEED = 1;
    private static AtomicLong idCounter = new AtomicLong();
    private Optional<Parcel> curr;
    private BehaviourModule behaviormodule = new BehaviourModule();
    private final String ID;

    //Communication
    private final double range = 4.2;  //TODO set range
    private final double reliability = 1;
    Optional<CommDevice> device;


    /**
     */
    //TODO
    public TransportAgent(Point startPosition, int capacity){
        super(VehicleDTO.builder()
        .capacity(capacity)
        .startPosition(startPosition)
        .speed(SPEED)
        .build());
        curr = Optional.absent();
        ID = createID();
    }

    @Override
    //TODO
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();

        if (!time.hasTimeLeft()) {
            return;
        }
        if (!curr.isPresent()) {
            curr = Optional.fromNullable(RoadModels.findClosestObject(
                    rm.getPosition(this), rm, Parcel.class));
        }

        if (curr.isPresent()) {
            final boolean inCargo = pm.containerContains(this, curr.get());
            // sanity check: if it is not in our cargo AND it is also not on the
            // RoadModel, we cannot go to curr anymore.
            if (!inCargo && !rm.containsObject(curr.get())) {
                curr = Optional.absent();
            } else if (inCargo) {
                // if it is in cargo, go to its destination
                Queue<Point> path = new LinkedList<>(rm.getShortestPathTo(this,curr.get().getDeliveryLocation()));
                rm.followPath(this, path, time);
                if (rm.getPosition(this).equals(curr.get().getDeliveryLocation())) {
                    // deliver when we arrive
                    pm.deliver(this, curr.get(), time);
                }
            } else {
                // it is still available, go there as fast as possible
                Queue<Point> path = new LinkedList<>(rm.getShortestPathTo(this,curr.get().getPickupLocation()));
                rm.followPath(this, path, time);
                if (rm.equalPosition(this, curr.get())) {
                    // pickup customer
                    pm.pickup(this, curr.get(), time);
                }
            }
        }
    }

    @Override
    public Optional<Point> getPosition() {
        if (getRoadModel().containsObject(this)) {
            return Optional.of(getRoadModel().getPosition(this));
        }
        return Optional.absent();
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

    public static String createID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }
}
