package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.networking.IGridNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.config.ConfigHolder;

public interface IGridConnectedMachine extends IMachineFeature, IGridConnectedBlockEntity {

    int ME_UPDATE_INTERVAL = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;

    boolean isOnline();
    void setOnline(boolean online);

    default boolean shouldSyncME() {
        return self().getOffsetTimer() % ME_UPDATE_INTERVAL == 0;
    }

    /**
     * Update me network connection status.
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
