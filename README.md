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

## How to Run  (haven't been implemented yet 2.21 // finish at 2.24)


### Option 1: IntelliJ
- Run `main` (if you have one), or run the tests under `runner/` / test directory.

### Option 2: Tests
- Run deterministic tests (and randomized tests if implemented) from the green run icons.

---

## Notes / Semantics
- State setters enforce **non-negative** values.
- Controller reconciliation is **one-step-per-tick**.
- Invariants are **safety checks** (must hold after every event).
- Liveness properties (eventual convergence) are intended as **post-run checks** in the runner/tests.

## Checkpoint D: Trace (Reproducible Runs)

This checkpoint adds a **Trace** component that records an event sequence, exports it to a text file, and later parses the file back to **replay** the same run deterministically.

### What Trace Does
A `Trace` is an ordered list of `Event`s (the “recording” of a simulation run).

It supports:
- `add(Event e)`  
  Appends one event to the trace (used during recording).
- `serialize()`  
  Converts the full event list into a text format (one event per line).
- `parse(File/Path f)` *(static)*  
  Reads a trace file and reconstructs a `Trace` object containing the same sequence of events.

(Optional) Trace may also record state snapshots after each step to help debug divergences during replay.

---

### Trace File Format
Each line is one event. Supported lines (current checkpoints):
- `TICK`
- `SET_DESIRED k`

Example:
SET_DESIRED 3
TICK
TICK
SET_DESIRED 1
TICK

Notes:
- `SET_DESIRED` **must** have a space before the integer argument.
- Blank lines may be ignored (if enabled in the parser).
- Parsing errors should report the line number (and ideally the original line content).

---

### Record → Serialize → Parse → Replay Workflow

1. **Record (during a normal run)**
  - For each event `e`:
    - `trace.add(e)`
    - `sim.step(e)`

2. **Export**
  - `trace.serialize()` produces the text representation.
  - Write the text to a file (e.g., `trace_demo.txt`).

3. **Import**
  - `Trace.parse(trace_demo.txt)` reads the file and reconstructs the same event sequence.

4. **Replay**
  - Create a new `Simulator` with the **same initial state** as the original run.
  - Iterate events in the parsed trace and call `sim.step(e)` for each event.

5. **Validation (Checkpoint D acceptance)**
  - After replay, the final results must match the original run:
    - `desired.replicas` is identical
    - `actual.runningPods` is identical

---

### How to Run (Typical)
- Use a small demo runner / `Main` to:
  1) execute a deterministic event sequence while recording,
  2) write the trace to a file,
  3) parse the file into a new trace,
  4) replay on a fresh simulator,
  5) compare final `(desired, actual)` between original run and replay.
