# ToyControllerTestBench (TCTB)

A tiny test bench that simulates a Kubernetes-like control loop:
- **DesiredState**: what we want (replica count)
- **ActualState**: what we have (running pods)
- **ToyController**: reconciles Actual toward Desired, **one step at a time**
- **Simulator + Events**: drives the system via `SET_DESIRED` and `TICK`
- **Invariants**: safety rules checked after every simulation step

---

## Project Structure

- `src/tctb/model/`
    - `DesiredState`: mutable desired replicas
    - `ActualState`: mutable running pods
- `src/tctb/controller/`
    - `ToyController`: `reconcileOnce(desired, actual)` changes running pods by at most 1
- `src/tctb/sim/`
    - `EventType`: `SET_DESIRED`, `TICK` (future: `CRASH/RESTART`)
    - `Event`: `tick()` and `setDesired(k)`
    - `Simulator`: holds states and executes one event per `step(e)`
- `src/tctb/invariant/`
    - `Invariant`: rule interface
    - `Invariants`: default rules registry + `checkAll(sim)`
    - `impl/`: concrete invariants
- `src/tctb/runner/` (or tests folder)
    - deterministic / randomized tests (if present)

---
// Listed below are the work done by 2.21
## Checkpoint A: Core State Model

### DesiredState
- Field: `replicas`
- Non-negative only (negative => `IllegalArgumentException`)
- Getter + setter

### ActualState
- Field: `runningPods`
- Non-negative only (negative => `IllegalArgumentException`)
- Getter + setter

---

## Checkpoint B: Controller + Simulation Events

### ToyController
- `reconcileOnce(desired, actual)`:
    - if `running < desired`: `running++`
    - else if `running > desired`: `running--`
    - else do nothing
- **No loops** / no instant convergence (enables better testing & fault injection later)

### Events + Simulator
- `EventType`: `SET_DESIRED`, `TICK`
- `Event`:
    - `Event.tick()`
    - `Event.setDesired(k)`
- `Simulator.step(Event e)`:
    - `SET_DESIRED(k)` updates desired only
    - `TICK` calls controller once

---

## Checkpoint C: Safety Invariants (checked after every step)

After every `Simulator.step(event)`, the simulator calls:
- `Invariants.checkAll(this)`

### Invariant framework
- `Invariant`: `void check(Simulator s)`
- `Invariants`: holds a default list of invariants and runs them in order

### Enabled safety invariants
1. `0 <= desired.replicas <= maxPods`
2. `0 <= actual.runningPods <= maxPods`
3. (TICK only) `abs(deltaRunningPods) <= 1`
4. (SET_DESIRED only) `SET_DESIRED` does **not** directly change `actual.runningPods`
5. (CRASH placeholder, if present) `CRASH` does not directly change actual (future checkpoint) // haven't implement yet(2.21)

---

## How to Run // haven't implement yet(2.21)

### Option 1: IntelliJ
- Run `Main` (if you have one), or run the tests under `runner/` / test directory.

### Option 2: Tests
- Run deterministic tests (and randomized tests if implemented) from the green run icons.

---

## Notes / Semantics
- State setters enforce **non-negative** values.
- Controller reconciliation is **one-step-per-tick**.
- Invariants are **safety checks** (must hold after every event).
- Liveness properties (eventual convergence) are intended as **post-run checks** in the runner/tests.