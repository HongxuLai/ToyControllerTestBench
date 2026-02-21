package tctb.controller;

import tctb.model.ActualState;
import tctb.model.DesiredState;

// converge the desiredState and actualState
public class ToyController {
    public void reconcileOnce(DesiredState desired, ActualState actual){
        int desiredReplicas = desired.getReplicas();
        int actualRunning = actual.getRunningPods();

        if(desiredReplicas > actualRunning){
            actual.setRunningPods(actualRunning + 1);
        }
        else if(desiredReplicas < actualRunning){
            actual.setRunningPods(actualRunning - 1);
        }
    }
}
