package tctb.model;

// set the desired replicas >= 0

public class DesiredState {
    private int replicas;

    public DesiredState(int replicas){
        if (replicas < 0){
            throw new IllegalArgumentException("replicas must >= 0");
        }
        this.replicas = replicas;
    }

    public int getReplicas(){
        return replicas;
    }

    public void setReplicas(int replicas){
        if (replicas < 0){
            throw new IllegalArgumentException("replicas must >= 0");
        }
        this.replicas = replicas;
    }
}
