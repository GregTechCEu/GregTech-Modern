package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IDataInfoProvider {

    @NotNull
    List<Component> getDataInfo();

    @Nullable
    default List<Component> getDebugInfo(Player player, int logLevel) {
        return null;
    }
}
