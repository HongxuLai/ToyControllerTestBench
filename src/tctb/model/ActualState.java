package tctb.model;

// set actual runningPod >+ 0

public class ActualState {
    private int runningPods;

    public ActualState(int runningPods) {
        if (runningPods < 0) {
            throw new IllegalArgumentException("runningPods must be >= 0");
        }
        this.runningPods = runningPods;
    }

    public int getRunningPods() {
        return runningPods;
    }

    public void setRunningPods(int runningPods) {
        if (runningPods < 0) {
            throw new IllegalArgumentException("runningPods must be >= 0");
        }
        this.runningPods = runningPods;
    }

}
