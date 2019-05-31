/*
 * Copyright (C) 2011-2018 Rinde R.S. van Lon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package MASProject;

import MASProject.Agents.ResourceAgent;
import MASProject.Agents.TransportAgent;
import MASProject.Agents.PackageAgent;
import MASProject.Model.DMASModel;
import MASProject.Statistics.Statistic;
import MASProject.Util.PathFinder;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.*;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import Graph.GraphCreator;

import javax.annotation.Nullable;
import javax.measure.unit.SI;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;


//Set<Point> nodes = roadModel.get(GraphRoadModel.class).getGraph().getNodes();


/**
 * Example showing a fleet of taxis that have to pickup and transport customers
 * around the city of Leuven.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 * @author Rinde van Lon
 */
public final class PizzaExample {

    private static final int NUM_RESTAURANTS = 0;
    private static final int NUM_BIKES = 2;
    private static final int NUM_CUSTOMERS = 5;

    // time in ms
    private static final long SERVICE_DURATION = 60000;
    private static final int TAXI_CAPACITY = 10;
    private static final int RESTAURANT_CAPACITY = 100;

    private static final int SPEED_UP = 4;
    private static final int MAX_CAPACITY = 3;
    private static final double NEW_CUSTOMER_PROB = 0.007;

    private static final int CARDINALITY = 8;
    private static final double VEHICLE_LENGTH = 2d;

    //This variable holds the position of every resource in the graph
    public static final DMASModel DMAS_MODEL= new DMASModel();

    public static final Statistic STATISTIC = new Statistic();

  private PizzaExample() {}

  /**
   * Starts the {@link PizzaExample}.
   * @param args The first option may optionally indicate the end time of the
   *          simulation.
   */
  public static void main(@Nullable String[] args) {
    final long endTime = args != null && args.length >= 1 ? Long
      .parseLong(args[0]) : Long.MAX_VALUE;//1000*7000;
    run(endTime, null /* new Display() */, null, null);
  }

  /**
   * Run the example.
   */
  public static void run() {
    run(Long.MAX_VALUE, null, null, null);
  }

  /**
   * Starts the example.
   * @param endTime The time at which simulation should stop.
   * @param display The display that should be used to show the ui on.
   * @param m The monitor that should be used to show the ui on.
   * @param list A listener that will receive callbacks from the ui.
   * @return The simulator instance.
   */
  public static Simulator run(final long endTime, @Nullable Display display, @Nullable Monitor m, @Nullable Listener list) {

    final View.Builder view = createGui(display, m, list);

    // use simple Graph (8*8 fully connected matrix)
    final Simulator simulator = Simulator.builder()
      .addModel(
          RoadModelBuilders.dynamicGraph(GraphCreator.createGraph(VEHICLE_LENGTH, CARDINALITY))
              .withCollisionAvoidance()
              .withDistanceUnit(SI.METER)
              .withVehicleLength(VEHICLE_LENGTH))
      .addModel(DefaultPDPModel.builder())
      .addModel(view)
      .addModel(CommModel.builder())
      .build();
    final RandomGenerator rng = simulator.getRandomGenerator();

    final RoadModel roadModel = simulator.getModelProvider().getModel(
      RoadModel.class);
    // add depots, taxis and parcels to simulator
    for (int i = 0; i < NUM_RESTAURANTS; i++) {
      simulator.register(new TaxiBase(roadModel.getRandomPosition(rng),
              RESTAURANT_CAPACITY));
    }
    //initialize transport agents
    for (int i = 0; i < NUM_BIKES; i++) {
      simulator.register(new TransportAgent(roadModel.getRandomPosition(rng),
        TAXI_CAPACITY));
    }
    //initialize tasks
    for (int i = 0; i < NUM_CUSTOMERS; i++) {
      Point start = roadModel.getRandomPosition(rng);
      Point stop = roadModel.getRandomPosition(rng);
      simulator.register(new PackageAgent(
        Parcel.builder(start, stop)
          .serviceDuration(SERVICE_DURATION)
          .neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
          .buildDTO()));
    }

    //set up resource agents
    Set<Point> nodes = roadModel.get(GraphRoadModel.class).getGraph().getNodes();
    for(Point node: nodes) {
       ResourceAgent agent = new ResourceAgent(node, roadModel);
       //register agent to pathFinder
        DMAS_MODEL.addAntAcceptor(node, agent);
        DMAS_MODEL.addLocation(agent, node);
       simulator.register(agent);
    }



     //randomly add new task
    simulator.addTickListener(new TickListener() {
      @Override
      public void tick(TimeLapse time) {
        if (time.getStartTime() > endTime) {
          simulator.stop();
          STATISTIC.printStatistics();
        } else if (rng.nextDouble() < NEW_CUSTOMER_PROB) {
          simulator.register(new PackageAgent(
            Parcel
              .builder(roadModel.getRandomPosition(rng),
                roadModel.getRandomPosition(rng))
              .serviceDuration(SERVICE_DURATION)
              .neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
              .buildDTO()));
        }
      }

      @Override
      public void afterTick(TimeLapse timeLapse) {}
    });
    simulator.start();

    return simulator;
  }

  static View.Builder createGui(
      @Nullable Display display,
      @Nullable Monitor m,
      @Nullable Listener list) {

    View.Builder view = View.builder()
      .with(WarehouseRenderer.builder()
          .withMargin(VEHICLE_LENGTH))
      .with(RoadUserRenderer.builder()
        .withImageAssociation(
          TaxiBase.class, "/graphics/perspective/tall-building-64.png")
        //.withImageAssociation(
        //  TransportAgent.class, "/graphics/flat/taxi-32.png")
        .withImageAssociation(
          PackageAgent.class, "/graphics/flat/person-red-32.png"))
      .with(TaxiRenderer.builder(TaxiRenderer.Language.ENGLISH))
      .with(AGVRenderer.builder()
          .withDifferentColorsForVehicles())
      //.with(CommRenderer.builder().withMessageCount())
      .withTitleAppendix("Pizza example");

    if (m != null && list != null && display != null) {
      view = view.withMonitor(m)
        .withSpeedUp(SPEED_UP)
        .withResolution(m.getClientArea().width, m.getClientArea().height)
        .withDisplay(display)
        .withCallback(list)
        .withAsync()
        .withAutoPlay()
        .withAutoClose();
    }
    return view;
  }

  // currently has no function
  static class TaxiBase extends Depot {
    TaxiBase(Point position, double capacity) {
      super(position);
      setCapacity(capacity);
    }

    @Override
    public void initRoadPDP(RoadModel pvRoadModel, PDPModel pPdpModel) {}
  }

}
