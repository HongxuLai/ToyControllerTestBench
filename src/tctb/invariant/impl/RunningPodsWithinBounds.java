package tctb.invariant.impl;

import tctb.invariant.Invariant;
import tctb.sim.Simulator;

public class RunningPodsWithinBounds implements Invariant {
    @Override
    public void check(Simulator s) {
        int runningPods = s.actual().getRunningPods();
        int max = s.getMaxPods();
        if(runningPods < 0 || runningPods > max){
            throw new IllegalStateException(
                    "Invariant failed: desired.runningPods out of bounds. " +
                            "runningPods=" + runningPods + ", maxPods=" + max +
                            ", step=" + s.getStepIndex() +
                            ", event=" + s.getLastEvent()
            );
        }
    }
}
