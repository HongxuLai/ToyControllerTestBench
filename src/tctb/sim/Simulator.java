package tctb.sim;

import tctb.controller.ToyController;
import tctb.invariant.Invariants;
import tctb.model.ActualState;
import tctb.model.DesiredState;

import static tctb.sim.EventType.SET_DESIRED;
import static tctb.sim.EventType.TICK;

public class Simulator {
    private final ToyController controller;
    private final DesiredState desired;
    private final ActualState actual;
    private int stepIndex;
    private Event lastEvent;
    private int runningPodsBeforeStep;
    private final int maxPods;

    public Simulator(ToyController controller, DesiredState desired, ActualState actual, int maxPods){
        if (controller == null || desired == null || actual == null) {
            throw new IllegalArgumentException("controller/desired/actual cannot be null");
        }
        this.controller = controller;
        this.desired = desired;
        this.actual = actual;
        this.stepIndex = 0;
        this.lastEvent = null;
        this.runningPodsBeforeStep = actual().getRunningPods();
        this.maxPods = maxPods;
    }

    public void step(Event event){
        runningPodsBeforeStep = actual().getRunningPods();
        // store the value of runningPods before step

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
            // other cases to be added
        }
        lastEvent = event; // write down event now
        stepIndex ++; // write down which step now
        Invariants.checkAll(this); // check by using all default methods
    }

    public DesiredState desired() { return desired; }
    public ActualState actual() { return actual; }
    public int getStepIndex() { return stepIndex; }
    public Event getLastEvent() { return lastEvent; }
    public int getRunningPodsBeforeStep() { return runningPodsBeforeStep; }
    public int getMaxPods() { return maxPods; }
}
