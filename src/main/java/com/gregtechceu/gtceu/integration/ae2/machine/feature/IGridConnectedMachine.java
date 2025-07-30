package com.gregtechceu.gtceu.integration.ae2.machine.feature;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Direction;

import appeng.api.networking.IGridNodeListener;
import appeng.api.util.AECableType;
import appeng.me.helpers.IGridConnectedBlockEntity;

/**
 * A machine that can connect to ME network.
 */
public interface IGridConnectedMachine extends IMachineFeature, IGridConnectedBlockEntity {

    /**
     * @return return {@code true} if current machine connected to a valid ME network, {@code false} otherwise.
     */
    boolean isOnline();

    void setOnline(boolean online);

    /**
     * @return {@code true} if current machine should interact with ME network, {@code false} otherwise.
     */
    default boolean shouldSyncME() {
        return self().getOffsetTimer() % ConfigHolder.INSTANCE.compat.ae2.updateIntervals == 0;
    }

    default AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    /**
     * Update me network connection status.
     * 
     * @return the updated status.
     */
    default boolean updateMEStatus() {
        var proxy = getMainNode();
        setOnline(proxy.isOnline() && proxy.isPowered());
        return isOnline();
    }

    @Override
    default void saveChanges() {
        self().onChanged();
    }

    @Override
    default void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateMEStatus();
    }
}
