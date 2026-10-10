package com.gregtechceu.gtceu.api.capability;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MUST be implemented on any multiblock which uses
 * Transmitter Computation Hatches in its structure.
 */
public interface IOpticalComputationProvider {

    /**
     * Request some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Implementors should expect these requests to occur each tick that computation is required.
     *
     * @param cwut Maximum amount of CWU/t requested.
     * @return The amount of CWU/t that could be supplied.
     */
    default int requestCWUt(int cwut, boolean simulate) {
        Set<IOpticalComputationProvider> seen = new HashSet<>();
        Map<IOpticalComputationProvider, Object> simulationState = new HashMap<>();
        return requestCWUt(cwut, simulate, seen, simulationState);
    }

    /**
     * Simulates a request of some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Simulation state allows sharing the simulation across multiple requests.
     *
     * @param cwut            Maximum amount of CWU/t requested.
     * @param simulationState simulation state to allow sharing simulation across multiple requests
     * @return The amount of CWU/t that could be supplied.
     */
    default int requestCWUtSimulated(int cwut, Map<IOpticalComputationProvider, Object> simulationState) {
        Set<IOpticalComputationProvider> seen = new HashSet<>();
        return requestCWUt(cwut, true, seen, simulationState);
    }

    /**
     * Request some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Implementors should expect these requests to occur each tick that computation is required.
     *
     * @param cwut            Maximum amount of CWU/t requested.
     * @param seen            The Optical Computation Providers already checked
     * @param simulationState state of simulation for each provider
     *
     * @return The amount of CWU/t that could be supplied.
     */
    int requestCWUt(int cwut, boolean simulate,
                    @NotNull Set<IOpticalComputationProvider> seen,
                    @NotNull Map<IOpticalComputationProvider, Object> simulationState);

    /**
     * The maximum of CWU/t that this computation provider can provide.
     */
    default int getMaxCWUt() {
        Set<IOpticalComputationProvider> seen = new HashSet<>();
        Map<IOpticalComputationProvider, Object> simulationState = new HashMap<>();
        return getMaxCWUt(seen, simulationState);
    }

    /**
     * The maximum of CWU/t that this computation provider can provide.
     */
    default int getMaxCWUtInSimulation(Map<IOpticalComputationProvider, Object> simulationState) {
        Set<IOpticalComputationProvider> seen = new HashSet<>();
        return getMaxCWUt(seen, simulationState);
    }

    /**
     * The maximum of CWU/t that this computation provider can provide.
     *
     * @param seen The Optical Computation Providers already checked
     */
    int getMaxCWUt(@NotNull Set<IOpticalComputationProvider> seen,
                   @NotNull Map<IOpticalComputationProvider, Object> simulationState);

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     */
    default boolean canBridge() {
        Set<IOpticalComputationProvider> seen = new HashSet<>();
        Map<IOpticalComputationProvider, Object> simulationState = new HashMap<>();
        return canBridge(seen, simulationState);
    }

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     */
    default boolean canBridgeInSimulation(Map<IOpticalComputationProvider, Object> simulationState) {
        Set<IOpticalComputationProvider> seen = new HashSet<>();
        return canBridge(seen, simulationState);
    }

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     *
     * @param seen The Optical Computation Providers already checked
     */
    boolean canBridge(@NotNull Set<IOpticalComputationProvider> seen,
                      @NotNull Map<IOpticalComputationProvider, Object> simulationState);
}
