package tctb;

import tctb.controller.ToyController;
import tctb.model.ActualState;
import tctb.model.DesiredState;
import tctb.sim.Event;
import tctb.sim.Simulator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class main {

    private static void printState(String label, Simulator sim) {
        int desired = sim.desired().getReplicas();
        int running = sim.actual().getRunningPods();
        System.out.printf("%s | desired=%d, running=%d%n", label, desired, running);
    }

    public static void main(String[] args) {
        // Initial states
        DesiredState desired = new DesiredState(0);
        ActualState actual = new ActualState(0);
        ToyController controller = new ToyController();

        Simulator sim = new Simulator(controller, desired, actual);

        printState("INIT", sim);

        // A deterministic sequence to validate Checkpoint C invariants
        runStep(sim, Event.setDesired(3), "SET_DESIRED 3");
        runStep(sim, Event.tick(), "TICK");
        runStep(sim, Event.tick(), "TICK");
        runStep(sim, Event.tick(), "TICK");   // should converge at running=3 (one-step-per-tick)
        runStep(sim, Event.tick(), "TICK");   // after desired == actual, tick() won't change actual value

        runStep(sim, Event.setDesired(1), "SET_DESIRED 1"); // SET_DESIRED doesn't change the actual
        runStep(sim, Event.tick(), "TICK");
        runStep(sim, Event.tick(), "TICK");   // should converge at running=1
        runStep(sim, Event.tick(), "TICK");   // after desired == actual, tick() won't change actual value

        // notice: if temporarily change ToyController-ReconcileOnce, delta value = 2,
        // main will throw error immediately

        System.out.println("DONE (If no exception was thrown, invariants held for this run.)");
    }

    private static void runStep(Simulator sim, Event e, String label) {
        // If an invariant fails, sim.step(e) should throw after executing the event
        sim.step(e);
        printState(label, sim);
    }

}
