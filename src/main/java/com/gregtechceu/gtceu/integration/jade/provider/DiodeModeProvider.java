package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class DiodeModeProvider extends MachineInfoProvider<DiodePartMachine, CompoundTag> {

    public DiodeModeProvider() {
        super(GTCEu.id("diode_provider"), DiodePartMachine.class);
    }

    @Override
    protected CompoundTag write(DiodePartMachine machine) {
        var tag = new CompoundTag();
        tag.putInt("voltage", machine.getTier());
        tag.putInt("amps", machine.getAmps());
        return tag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        tooltip.add(Component.translatable(
                "gtceu.top.transform_output",
                (GTValues.VNF[data.getInt("voltage")] + " §r(" + data.getInt("amps") + "A)")));
    }
}
