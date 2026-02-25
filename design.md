Checkpoint A: the smallest model
1. ActualState: record actual state by using the runningPods.
   DesiredState: record user's desired state by using the replicas.
   both runningPods and replicas >= 0
   Methods: get(): return replicas/runningPods
            set(); change replicas/runningPods

2. ToyController: core methods reconcileOnce
   if runningPods > replicas: runningPods --
   if runningPods < replicas: runningPods ++

3. Event & EventType
   TICK / SET_DESIRED(with argument k)

4. Simulator: execute events
   step method: also check if k >= 0
   Tick: make two states converge to the same by using reconcileOnce
   SET_DESIRED: change desired state by setReplicas(k)

Checkpoint B: Deterministic tests
purpose: 
1. check if tick can work normally: runningPods ++/ --
2. boundaries test: runningPods and replicas >= 0
3. stability test: if runningPods == replicas, tick cannot change runningPods
4. side effect test: when SET_DESIRED, change replicas doesn't impact runningPods

Checkpoint C: Invariants System
1. design the invariant interface
2. design Invariants rules, every time in Simulator call step(), check if that
satisfies the rules: 
a&b.runningPods' and replicas' boundaries; 
c.tick only change '1';
d.SET_DESIRED doesn't impact runningPods
3. everytime the invariants are changed, Simulator won't be influenced

Checkpoint D: Trace
purpose:
1. record every event when executing:
   a. recordStep: add an event e to the events list, and Simulator execute the event e
2. change every event into document:
   after recording, change events into Strings
   writeToFile(Path/File): write the string into files
3. parse the document and change document back into event
   static Trace.parse(File/Path f): recover the event from the document
   input: a line; output: a new trace 
   private helper parseLine(): change a line of documents into an event
4. replay the new event received from parse
   use the new trace, new simulator, and for-each-loop to go through 
   every event in trace
5. check the result of origin and replay
   compare two states' runningPods and replicas


