package MASProject;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;


/**
 * Our implementation of a simple agent in a simple PDP problem : delivering pizzas in time.
 */
public class Bike extends Vehicle {
    private static final double SPEED = 1000d;
    private Optional<Parcel> curr;
    private BehaviourModule behaviormodule = new BehaviourModule();
    /**
     */
    //TODO
    public Bike(Point startPosition, int capacity){
        super(VehicleDTO.builder()
        .capacity(capacity)
        .startPosition(startPosition)
        .speed(SPEED)
        .build());
        curr = Optional.absent();
    }


    @Override
    //TODO
    protected void tickImpl(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();

        behaviormodule.move(rm.getPosition(this));
    }
}
