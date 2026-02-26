package tctb;

import org.junit.jupiter.api.Test;
import tctb.controller.ToyController;
import tctb.model.ActualState;
import tctb.model.DesiredState;
import tctb.sim.Event;
import tctb.sim.Simulator;

import static org.junit.jupiter.api.Assertions.*;

public class DeterministicTests {
    @Test
    public void sanity() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testConvergesUp() {
        ToyController controller = new ToyController();
        DesiredState desired = new DesiredState(5);
        ActualState actual = new ActualState(0);
        int maxPods = 100;
        Simulator sim = new Simulator(controller, desired, actual, maxPods);

        for(int i = 0; i < 5; i++){
            sim.step(Event.tick()); // runningPods converge up to 5
        }

        assertEquals(5, sim.actual().getRunningPods());
    }

    @Test
    public void testConvergesDown() {
        ToyController controller = new ToyController();
        DesiredState desired = new DesiredState(0);
        ActualState actual = new ActualState(7);
        int maxPods = 100;
        Simulator sim = new Simulator(controller, desired, actual, maxPods);

        for(int i = 0; i < 7; i++){
            sim.step(Event.tick()); //runningPods converge down to 0
        }

        assertEquals(0, sim.actual().getRunningPods());
    }

    @Test
    public void testStability() {
        ToyController controller = new ToyController();
        DesiredState desired = new DesiredState(3);
        ActualState actual = new ActualState(3);
        int maxPods = 100;
        Simulator sim = new Simulator(controller, desired, actual, maxPods);

        for(int i = 0; i < 10; i ++){
            sim.step(Event.tick()); // actual == desired, runningPods don't change
        }

        assertEquals(3, sim.actual().getRunningPods());
    }

    @Test
    public void testSet_impact_actual() {
        ToyController controller = new ToyController();
        DesiredState desired = new DesiredState(5);
        ActualState actual = new ActualState(0);
        int maxPods = 100;
        Simulator sim = new Simulator(controller, desired, actual, maxPods);

        sim.step(Event.setDesired(2)); // change desired to 2
        assertEquals(0, sim.actual().getRunningPods());
        // don't do tick(), runningPods don't change

        for (int i = 0; i < 5; i++){
            sim.step(Event.tick());
            // do tick() for 5 times, but actual runningPods only converge up to 2
        }
        assertEquals(2, sim.actual().getRunningPods());
    }

    @Test
    public void IllegalDesiredState() {
        assertThrows(IllegalArgumentException.class, () -> new DesiredState(-1));
    }

    @Test
    public void IllegalActualState() {
        assertThrows(IllegalArgumentException.class, () -> new ActualState(-1));
    }

    @Test
    public void IllegalSetDesired() {
        ToyController controller = new ToyController();
        DesiredState desired = new DesiredState(5);
        ActualState actual = new ActualState(0);
        int maxPods = 100;
        Simulator sim = new Simulator(controller, desired, actual, maxPods);

        assertThrows(IllegalArgumentException.class, () -> sim.step(Event.setDesired(-1)));
    }

    @Test
    public void IllegalArg() {
        assertThrows(IllegalArgumentException.class, () -> Event.setDesired(-1));
    }
}