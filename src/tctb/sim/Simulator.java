package tctb.sim;

import tctb.controller.ToyController;
import tctb.model.ActualState;
import tctb.model.DesiredState;

import static tctb.sim.EventType.SET_DESIRED;
import static tctb.sim.EventType.TICK;

public class Simulator {
    private final ToyController controller;
    private final DesiredState desired;
    private final ActualState actual;

    public Simulator(ToyController controller, DesiredState desired, ActualState actual){
        if (controller == null || desired == null || actual == null) {
            throw new IllegalArgumentException("controller/desired/actual cannot be null");
        }
        this.controller = controller;
        this.desired = desired;
        this.actual = actual;
    }

    public void step(Event event){
        switch (event.getType()) {
            case TICK -> controller.reconcileOnce(desired, actual);
            case SET_DESIRED -> {
                Integer k = event.getArg();
                if (k == null) {
                    throw new IllegalArgumentException("SET_DESIRED event must have an argument");
                }
                if (k < 0) {
                    throw new IllegalArgumentException("replicas must >= 0");
                }
                desired.setReplicas(k);
            }
        }
    }

    public DesiredState desired() { return desired; }
    public ActualState actual() { return actual; }
}
