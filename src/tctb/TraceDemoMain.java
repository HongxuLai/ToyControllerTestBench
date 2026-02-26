package tctb;

import tctb.controller.ToyController;
import tctb.model.ActualState;
import tctb.model.DesiredState;
import tctb.sim.Event;
import tctb.sim.EventType;
import tctb.sim.Simulator;
import tctb.trace.Trace;

import java.nio.file.Path;

public class TraceDemoMain {
    private static void recordStep(Simulator sim, Trace t, Event e){
        t.add(e);
        sim.step(e);
    }

    private static Simulator newSimulator() {
        DesiredState desired = new DesiredState(0);
        ActualState actual = new ActualState(0);
        ToyController controller = new ToyController();
        int maxPods = 100;
        return new Simulator(controller, desired, actual, maxPods);
    }

    public static void main(String[] args) throws Exception {
        Path file = Path.of("trace_demo.txt");

        // record
        Simulator sim1 = newSimulator();
        Trace trace = new Trace();

        recordStep(sim1, trace, Event.setDesired(3));
        recordStep(sim1, trace, Event.tick());
        recordStep(sim1, trace, Event.tick());
        recordStep(sim1, trace, Event.setDesired(1));
        recordStep(sim1, trace, Event.tick());

        int d1 = sim1.desired().getReplicas();
        int r1 = sim1.actual().getRunningPods();

        trace.writeToFile(file);
        System.out.println("Wrote trace to: " + file.toAbsolutePath());
        System.out.println("--- trace file ---");
        System.out.print(trace.serialize());

        // parse + replay
        Trace replayTrace = Trace.parse(file);
        Simulator sim2 = newSimulator();

        for(Event e : replayTrace.events()) {
            sim2.step(e);
        }
        int d2 = sim2.desired().getReplicas();
        int r2 = sim2.actual().getRunningPods();
        System.out.println("\nResult #1: desired=" + d1 + " running=" + r1);
        System.out.println("Result #2: desired=" + d2 + " running=" + r2);

        if(d1 != d2 || r1 != r2){
            throw new AssertionError("Replay mismatch!");
        }
        System.out.println("Replay OK: results match.");
    }
}
