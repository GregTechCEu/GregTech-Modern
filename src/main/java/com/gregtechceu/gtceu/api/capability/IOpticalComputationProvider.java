package com.gregtechceu.gtceu.api.capability;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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
        Map<IOpticalComputationProvider, Object> seenWithContext = new HashMap<>();
        return requestCWUt(cwut, simulate, seenWithContext);
    }

    /**
     * Request some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Implementors should expect these requests to occur each tick that computation is required.
     *
     * @param cwut            Maximum amount of CWU/t requested.
     * @param seenWithContext The Optical Computation Providers already checked, with provider-specific context for
     *                        simulation case
     * @return The amount of CWU/t that could be supplied.
     */
    int requestCWUt(int cwut, boolean simulate, @NotNull Map<IOpticalComputationProvider, Object> seenWithContext);

    /**
     * The maximum of CWU/t that this computation provider can provide.
     */
    default int getMaxCWUt() {
        Map<IOpticalComputationProvider, Object> seenWithContext = new HashMap<>();
        return getMaxCWUt(seenWithContext);
    }

    /**
     * The maximum of CWU/t that this computation provider can provide.
     *
     * @param seenWithContext The Optical Computation Providers already checked
     */
    int getMaxCWUt(@NotNull Map<IOpticalComputationProvider, Object> seenWithContext);

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     */
    default boolean canBridge() {
        Map<IOpticalComputationProvider, Object> seenWithContext = new HashMap<>();
        return canBridge(seenWithContext);
    }

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     *
     * @param seenWithContext The Optical Computation Providers already checked
     */
    boolean canBridge(@NotNull Map<IOpticalComputationProvider, Object> seenWithContext);
}
