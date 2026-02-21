package tctb.invariant;

import tctb.sim.Simulator;

public interface Invariant {
    void check(Simulator s);
}
