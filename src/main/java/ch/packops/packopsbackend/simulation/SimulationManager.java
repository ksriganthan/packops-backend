package ch.packops.packopsbackend.simulation;

public class SimulationManager {

    private final InputSimulator inputSimulator;
    private final WeighingCore weighingCore;

    public SimulationManager(InputSimulator inputSimulator, WeighingCore weighingCore) {
        this.inputSimulator = inputSimulator;
        this.weighingCore = weighingCore;
    }

    public void startSimulation() {
        // TODO: implement
    }

    public void stopSimulation() {
        // TODO: implement
    }

    public Object getRuntimeSnapshot() {
        // TODO: implement
        return null;
    }
}