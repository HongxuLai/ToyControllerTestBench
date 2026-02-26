package tctb.runner;

import tctb.controller.ToyController;
import tctb.model.ActualState;
import tctb.model.DesiredState;
import tctb.sim.Event;
import tctb.sim.Simulator;
import tctb.trace.Trace;

import java.nio.file.Path;

public class ReplayRunner {
    private static void usageAndExit(String msg) {
        System.err.println("ERROR: " + msg);
        System.err.println("Usage:");
        System.err.println("  --trace <path> --maxPods <int> --initDesired <int> --initRunning <int>");
        System.exit(1);
    }


    public static void main(String[] args) throws Exception {
        // Required args
        String tracePathStr = null;
        Integer maxPods = null;
        Integer initDesired = null;
        Integer initRunning = null;

        // parse the arguments and check the validation
        for (int i = 0; i < args.length; i += 2) {
            if (i >= args.length) break;
            String key = args[i];
            if (!key.startsWith("--")) usageAndExit("Unexpected token: " + key);
            if (i + 1 >= args.length) usageAndExit("Missing value for: " + key);

            String val = args[i + 1];
            try {
                switch (key) {
                    case "--trace" -> tracePathStr = val;
                    case "--maxPods" -> maxPods = Integer.parseInt(val);
                    case "--initDesired" -> initDesired = Integer.parseInt(val);
                    case "--initRunning" -> initRunning = Integer.parseInt(val);
                    default -> usageAndExit("Unknown arg: " + key);
                }
            } catch (NumberFormatException nfe) {
                usageAndExit("Bad number for " + key + ": " + val);
            }
        }
        if (tracePathStr == null || tracePathStr.isBlank()) usageAndExit("Missing required arg: --trace");
        if (maxPods == null) usageAndExit("Missing required arg: --maxPods");
        if (initDesired == null) usageAndExit("Missing required arg: --initDesired");
        if (initRunning == null) usageAndExit("Missing required arg: --initRunning");

        if (maxPods <= 0) usageAndExit("--maxPods must be > 0");
        if (initDesired < 0 || initDesired > maxPods) usageAndExit("--initDesired must be within [0, maxPods]");
        if (initRunning < 0 || initRunning > maxPods) usageAndExit("--initRunning must be within [0, maxPods]");

        Path tracePath = Path.of(tracePathStr);

        System.out.println("ReplayRunner config:");
        System.out.println("  trace=" + tracePath.toAbsolutePath());
        System.out.println("  maxPods=" + maxPods);
        System.out.println("  initDesired=" + initDesired);
        System.out.println("  initRunning=" + initRunning);

        // Load trace
        Trace trace = Trace.parse(tracePath);

        // Build simulator with the SAME initial state
        DesiredState desired = new DesiredState(initDesired);
        ActualState actual = new ActualState(initRunning);
        ToyController controller = new ToyController();
        Simulator sim = new Simulator(controller, desired, actual, maxPods);

        // Replay
        int stepIndex = -1;
        Event lastEvent = null;

        try {
            for (Event e : trace.events()) { // for-each loop
                stepIndex++;
                lastEvent = e;
                sim.step(e); // invariants checked inside step()
            }
        } catch (RuntimeException ex) {
            System.err.println("REPLAY FAIL");
            System.err.println("  stepIndex=" + stepIndex);
            System.err.println("  event=" + lastEvent);
            System.err.println("  desired=" + sim.desired().getReplicas());
            System.err.println("  running=" + sim.actual().getRunningPods());
            System.err.println("  exception=" + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            System.exit(1);
        }

        System.out.println("REPLAY OK");
        System.out.println("  steps=" + trace.size());
        System.out.println("  finalDesired=" + sim.desired().getReplicas());
        System.out.println("  finalRunning=" + sim.actual().getRunningPods());
        System.exit(0);
    }
}
