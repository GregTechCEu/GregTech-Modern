package com.gregtechceu.gtceu.api.graphnet.edge;

public final class SimulatorKey {

    private static int ID;
    private final int id;

    private SimulatorKey() {
        this.id = ID++;
    }

    /**
     * Claims a new, unique simulator instance for properly simulating flow edge limits without actually changing them.
     * <br>
     * This simulator must be discarded after use so that the garbage collector can clean up.
     */
    public static SimulatorKey getNewSimulatorInstance() {
        return new SimulatorKey();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
