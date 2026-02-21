package tctb.invariant.impl;

import tctb.invariant.Invariant;
import tctb.sim.Simulator;

public final class DesiredReplicasWithinBounds implements Invariant {
    @Override
    public void check(Simulator s) {
        int replicas = s.desired().getReplicas();
        int max = s.getMaxPods();
        if (replicas < 0 || replicas > max) {
            throw new IllegalStateException(
                    "Invariant failed: desired.replicas out of bounds. " +
                            "replicas=" + replicas + ", maxPods=" + max +
                            ", step=" + s.getStepIndex() +
                            ", event=" + s.getLastEvent()
            );
        }
    }
}
