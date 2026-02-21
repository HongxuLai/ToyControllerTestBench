package tctb.sim;

import static tctb.sim.EventType.SET_DESIRED;
import static tctb.sim.EventType.TICK;

public class Event {
    private final EventType type;
    private final Integer arg; // store the argument of different cases

    private Event(EventType type, Integer arg) {
        this.type = type;
        this.arg = arg;
    }

    public static Event tick(){
        return new Event(EventType.TICK, null);
    }

    public static Event setDesired(int k) {
        if (k < 0) {
            throw new IllegalArgumentException("replicas must >= 0");
        }
        return new Event(EventType.SET_DESIRED, k);
    }

    public EventType getType() {
        return type;
    }

    public Integer getArg() {
        return arg;
    }

}
