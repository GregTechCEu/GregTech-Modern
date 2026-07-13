package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class MEGridConnectedProvider extends MachineInfoProvider<MetaMachine, CompoundTag> {

    public MEGridConnectedProvider() {
        super(GTCEu.id("me_grid_connected"), MetaMachine.class);
    }

    @Override
    protected CompoundTag write(MetaMachine machine) {
        var tag = new CompoundTag();
        if (!(machine instanceof IGridConnectedMachine gridConnectedMachine)) return tag;

        tag.putBoolean("online", gridConnectedMachine.isOnline());
        return tag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!data.contains("online")) return;

        var online = data.getBoolean("online");
        var component = online ? Component.translatable("gtceu.gui.me_network.online") :
                Component.translatable("gtceu.gui.me_network.offline");
        tooltip.add(component);
    }
}
