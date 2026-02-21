package tctb.invariant;

import tctb.invariant.impl.DesiredReplicasWithinBounds;
import tctb.invariant.impl.RunningPodsWithinBounds;
import tctb.invariant.impl.SetDesiredDoesNotChangeActual;
import tctb.invariant.impl.TickDeltaAtMostOne;
import tctb.sim.Simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Invariants {

    private static final List<Invariant> defaults = buildDefaults();

    private Invariants() {}

    public static void checkAll(Simulator sim) {
        for (Invariant inv : defaults) {
            inv.check(sim); // go through all the default method in impl
        }
    }

    public static List<Invariant> defaults() {
        return defaults;
    }

    private static List<Invariant> buildDefaults() {
        List<Invariant> list = new ArrayList<>();
        list.add(new DesiredReplicasWithinBounds());
        list.add(new RunningPodsWithinBounds());
        list.add(new TickDeltaAtMostOne());
        list.add(new SetDesiredDoesNotChangeActual());
        // list.add(new CrashDoesNotChangeActual()); // CRASH

        return Collections.unmodifiableList(list);
    }
}