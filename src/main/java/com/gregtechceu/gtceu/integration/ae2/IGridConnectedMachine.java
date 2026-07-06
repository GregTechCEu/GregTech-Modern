package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

import appeng.api.networking.IGridNodeListener;

/**
 * A machine that can connect to ME network.
 */
public interface IGridConnectedMachine extends IMachineFeature {

    /**
     * Called when the block entities main grid nodes power or channel assignment state changes. Primarily used to send
     * rendering updates to the client.
     */
    default void onMainNodeStateChanged(IGridNodeListener.State reason) {}
}
