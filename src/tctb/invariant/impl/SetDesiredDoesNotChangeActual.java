package tctb.invariant.impl;

import tctb.invariant.Invariant;
import tctb.sim.Event;
import tctb.sim.EventType;
import tctb.sim.Simulator;

public class SetDesiredDoesNotChangeActual implements Invariant {
    @Override
    public void check(Simulator s) {
        Event last = s.getLastEvent();
        if (last == null) return;

        if (last.getType() != EventType.SET_DESIRED) return;
        int before = s.getRunningPodsBeforeStep();
        int after = s.actual().getRunningPods();

        if(after != before){ // SET_DESIRED doesn't change the actual runningPods
            throw new IllegalStateException(
                    "Invariant failed: SET_DESIRED changed actual.runningPods. " +
                            "before=" + before + ", after=" + after +
                            ", step=" + s.getStepIndex() +
                            ", event=" + last
            );
        }
    }
}
