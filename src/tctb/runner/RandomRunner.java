package tctb.runner;

import tctb.controller.ToyController;
import tctb.model.ActualState;
import tctb.model.DesiredState;
import tctb.sim.Event;
import tctb.sim.Simulator;
import tctb.trace.Trace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class RandomRunner {
    private static void usageAndExit(String msg) {
        System.err.println("ERROR: " + msg);
        System.err.println("Usage:");
        // print example valid argument
        System.err.println("  --seed <long> --maxPods <int> [--runs <int>] [--steps <int>] [--traceOut <path>]");
        System.exit(1); // fail: return 1
    }

    public static void main(String[] args) throws IOException {
        // Defaults / required markers
        Long seed = null;          // required
        Integer maxPods = null;    // required
        int runs = 1000;           // default
        int steps = 100;           // default
        String traceOut = "examples"; // default

        // below: parse the arguments and check arguments' validation
        for (int i = 0; i < args.length; i += 2) {
            if (i >= args.length) break;
            String key = args[i];
            if (!key.startsWith("--")) usageAndExit("Unexpected token: " + key);
            if (i + 1 >= args.length) usageAndExit("Missing value for: " + key);

            String val = args[i + 1];

            try {
                switch (key) {
                    case "--seed" -> seed = Long.parseLong(val);
                    case "--maxPods" -> maxPods = Integer.parseInt(val);
                    case "--runs" -> runs = Integer.parseInt(val);
                    case "--steps" -> steps = Integer.parseInt(val);
                    case "--traceOut" -> traceOut = val;
                    default -> usageAndExit("Unknown arg: " + key);
                }
            } catch (NumberFormatException nfe) { // check "seed = 12.a" error
                usageAndExit("Bad number for " + key + ": " + val);
            }
        }

        // check if set seed and maxPods
        if (seed == null) usageAndExit("Missing required arg: --seed");
        if (maxPods == null) usageAndExit("Missing required arg: --maxPods");
        // check if number is valid
        if (maxPods <= 0) usageAndExit("--maxPods must be > 0");
        if (runs <= 0) usageAndExit("--runs must be > 0");
        if (steps <= 0) usageAndExit("--steps must be > 0");
        if (traceOut == null || traceOut.isBlank()) usageAndExit("--traceOut must be non-empty");

        System.out.println("RandomRunner config:");
        System.out.println("  seed=" + seed);
        System.out.println("  runs=" + runs);
        System.out.println("  steps=" + steps);
        System.out.println("  maxPods=" + maxPods);
        System.out.println("  traceOut=" + traceOut);

        // loop part
        // outer loop
        for (int runIndex = 0; runIndex < runs; runIndex++){
            long runSeed = seed ^ (long) runIndex;
            Random rng = new Random(runSeed);
            // generate random number [0, maxPods + 1), initialize desired and actual states
            int initDesired = rng.nextInt(maxPods + 1);
            int initRunning = rng.nextInt(maxPods + 1);

            DesiredState desired = new DesiredState(initDesired);
            ActualState actual = new ActualState(initRunning);
            ToyController controller = new ToyController();
            Simulator sim = new Simulator(controller, desired, actual, maxPods);

            Trace trace = new Trace();

            int thisStepIndex = 0;
            Event lastevent = null;

            try{
                for (int stepIndex = 0; stepIndex < steps; stepIndex++) {
                    thisStepIndex = stepIndex;
                    Event e = nextRandomEvent(rng, maxPods);
                    lastevent = e;
                    trace.add(e); // record BEFORE execution
                    sim.step(e); // invariants checked inside step()
                }
            }   catch (RuntimeException ex) {
                // when failing, print the information
                System.err.println("FAIL");
                System.err.println("  seed=" + seed);
                System.err.println("  runIndex=" + runIndex);
                System.err.println("  runSeed=" + runSeed);
                System.err.println("  stepIndex=" + thisStepIndex);
                System.err.println("  initDesired=" + initDesired);
                System.err.println("  initRunning=" + initRunning);
                System.err.println("  Event=" + lastevent);
                System.err.println("  maxPods=" + maxPods);
                System.err.println("  lastDesired=" + sim.desired().getReplicas());
                System.err.println("  lastRunning=" + sim.actual().getRunningPods());
                System.err.println("  exception=" + ex.getClass().getSimpleName() + ": " + ex.getMessage());

                String fileName = "failing_trace_seed" + seed + "_run" + runIndex + ".txt";
                Path outPath = Path.of(traceOut, fileName);
                trace.writeToFile(outPath);
                System.err.println("  trace=" + outPath.toAbsolutePath());

                System.exit(1); // non-zero for CI
            }
        }
        System.out.println("PASS: all runs completed.");
        System.exit(0);
    }


    // set the probability of different events, here 10% SET_DESIRED, 90% TICK
    private static Event nextRandomEvent(Random rng, int maxPods) {
        int p = rng.nextInt(100); // 0..99
        if (p < 10) {
            int k = rng.nextInt(maxPods + 1);
            return Event.setDesired(k);
        } else {
            return Event.tick();
        }
    }

}
