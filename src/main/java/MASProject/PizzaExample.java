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

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.*;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Listener;
import MASProject.TaxiRenderer.Language;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import Graph.GraphCreator;

import javax.annotation.Nullable;
import javax.measure.unit.SI;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
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
    private static final int NUM_BIKES = 1;
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

  private PizzaExample() {}

  /**
   * Starts the {@link PizzaExample}.
   * @param args The first option may optionally indicate the end time of the
   *          simulation.
   */
  public static void main(@Nullable String[] args) {
    final long endTime = args != null && args.length >= 1 ? Long
      .parseLong(args[0]) : Long.MAX_VALUE;
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
    for (int i = 0; i < NUM_BIKES; i++) {
      simulator.register(new TransportAgent(roadModel.getRandomPosition(rng),
        TAXI_CAPACITY, roadModel));
    }
    for (int i = 0; i < NUM_CUSTOMERS; i++) {
      Point start = roadModel.getRandomPosition(rng);
      Point stop = roadModel.getRandomPosition(rng);
      simulator.register(new packageAgent(
        Parcel.builder(start, stop)
          .serviceDuration(SERVICE_DURATION)
          .neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
          .buildDTO()));
    }

    Set<Point> nodes = roadModel.get(GraphRoadModel.class).getGraph().getNodes();
    for(Point node: nodes) {
      simulator.register(new ResourceAgent(node, roadModel));
    }


    simulator.addTickListener(new TickListener() {
      @Override
      public void tick(TimeLapse time) {
        if (time.getStartTime() > endTime) {
          simulator.stop();
        } else if (rng.nextDouble() < NEW_CUSTOMER_PROB) {
          simulator.register(new packageAgent(
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
        .withImageAssociation(
          Taxi.class, "/graphics/flat/taxi-32.png")
        .withImageAssociation(
          packageAgent.class, "/graphics/flat/person-red-32.png"))
      //.with(TaxiRenderer.builder(Language.ENGLISH))
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
