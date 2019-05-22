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

    /**
     */
    //TODO
    public Bike(){
        super(VehicleDTO.builder().build());
    };


    @Override
    protected void tickImpl(TimeLapse time) {

    }
}
