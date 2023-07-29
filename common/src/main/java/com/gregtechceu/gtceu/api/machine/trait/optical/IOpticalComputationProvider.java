package com.gregtechceu.gtceu.api.machine.trait.optical;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * MUST be implemented on any multiblock which uses
 * Transmitter Computation Hatches in its structure.
 */
public interface IOpticalComputationProvider extends IMachineFeature {

    /**
     * Request some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Implementors should expect these requests to occur each tick that computation is required.
     *
     * @param cwut Maximum amount of CWU/t requested.
     * @return The amount of CWU/t that could be supplied.
     */
    default int requestCWUt(int cwut, boolean simulate) {
        Collection<IOpticalComputationProvider> list = new ArrayList<>();
        list.add(this);
        return requestCWUt(cwut, simulate, list);
    }

    /**
     * Request some amount of CWU/t (Compute Work Units per tick) from this Machine.
     * Implementors should expect these requests to occur each tick that computation is required.
     *
     * @param cwut Maximum amount of CWU/t requested.
     * @param seen The Optical Computation Providers already checked
     * @return The amount of CWU/t that could be supplied.
     */
    int requestCWUt(int cwut, boolean simulate, @Nonnull Collection<IOpticalComputationProvider> seen);

    /**
     * The maximum of CWU/t that this computation provider can provide.
     */
    default int getMaxCWUt() {
        Collection<IOpticalComputationProvider> list = new ArrayList<>();
        list.add(this);
        return getMaxCWUt(list);
    }

    /**
     * The maximum of CWU/t that this computation provider can provide.
     *
     * @param seen The Optical Computation Providers already checked
     */
    int getMaxCWUt(@Nonnull Collection<IOpticalComputationProvider> seen);

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     */
    default boolean canBridge() {
        Collection<IOpticalComputationProvider> list = new ArrayList<>();
        list.add(this);
        return canBridge(list);
    }

    /**
     * Whether this Computation Provider can "Bridge" with other Computation Providers.
     * Checked by machines like the Network Switch.
     *
     * @param seen The Optical Computation Providers already checked
     */
    boolean canBridge(@Nonnull Collection<IOpticalComputationProvider> seen);
}
