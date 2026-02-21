package tctb.invariant.impl;

import tctb.invariant.Invariant;
import tctb.sim.Event;
import tctb.sim.EventType;
import tctb.sim.Simulator;

public final class TickDeltaAtMostOne implements Invariant {
    @Override
    public void check(Simulator s) {
        Event last = s.getLastEvent();
        if (last == null) return;

        if (last.getType() != EventType.TICK) return;

        int before = s.getRunningPodsBeforeStep();
        int after = s.actual().getRunningPods();
        int delta = after - before;
        if (Math.abs(delta) > 1) { // every tick can only change 1
            throw new IllegalStateException(
                    "Invariant failed: TICK delta too large. " +
                            "before=" + before + ", after=" + after + ", delta=" + delta +
                            ", step=" + s.getStepIndex() +
                            ", event=" + last
            );
        }
    }
}
