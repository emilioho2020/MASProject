package MASProject.Agents;

import Messages.AntAcceptor;
import Messages.SmartMessage;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class PackageAgent extends Parcel implements CommUser, AntAcceptor {

    private double weight = 0;
    //private Point deliveryLocation; already in superclass
    private String committedAgent;
    //private int deliveryTime

    //Communication
    private final double range = 4.2;
    private final double reliability = 1;
    Optional<CommDevice> device;

    public PackageAgent(ParcelDTO dto) {
        super(dto);
    }

    PackageAgent(ParcelDTO dto, int w){
        super(dto);
        weight = w;
    }

    public double getWeight() {
        return weight;
    }

    private void acceptProposal(String agent){
        committedAgent = agent;
    }

    public boolean isScheduled(){
        return committedAgent != null;
    }


    @Override
    public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

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

    @Override
    public void deployAnt(SmartMessage ant) {

    }

    @Override
    public void propagate(SmartMessage ant, AntAcceptor next) {

    }
}