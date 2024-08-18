package com.gregtechceu.gtceu.integration.ae2.machine.feature;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.config.ConfigHolder;

import appeng.api.networking.IGridNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import net.minecraft.core.Direction;

import java.util.EnumSet;
import java.util.Set;

/**
 * A machine that can connect to ME network.
 *
 * @author GateGuardian
 * @date : 2024/7/14
 */
public interface IGridConnectedMachine extends IMachineFeature, IGridConnectedBlockEntity {

    int ME_UPDATE_INTERVAL = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;

    /**
     * @return return {@code true} if current machine connected to a valid ME network, {@code false} otherwise.
     */
    boolean isOnline();

    void setOnline(boolean online);

    /**
     * @return {@code true} if current machine should interact with ME network, {@code false} otherwise.
     */
    default boolean shouldSyncME() {
        return self().getOffsetTimer() % ME_UPDATE_INTERVAL == 0;
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

    @Override
    default Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.of(orientation.getSide(RelativeSide.FRONT));
    }
}
