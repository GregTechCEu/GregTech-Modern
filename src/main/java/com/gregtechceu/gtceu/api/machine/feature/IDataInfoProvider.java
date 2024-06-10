package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IDataInfoProvider {

    @NotNull
    List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode);

    @Nullable
    default List<Component> getDebugInfo(Player player, int logLevel, PortableScannerBehavior.DisplayMode mode) {
        return null;
    }
}
