package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class LDPEndpointProvider extends MachineInfoProvider<LongDistanceEndpointMachine, CompoundTag> {

    public LDPEndpointProvider() {
        super(GTCEu.id("ldp_endpoint"), LongDistanceEndpointMachine.class);
    }

    @Override
    protected CompoundTag write(LongDistanceEndpointMachine machine) {
        var data = new CompoundTag();
        data.putBoolean("isFormed", machine.getLink() != null);
        data.putString("ioType", machine.getIoType().getTooltip());
        data.putString("outputDirection", machine.getOutputFacing().getName());
        return data;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        boolean isFormed = data.getBoolean("isFormed");
        String ioType = data.getString("ioType");
        String outputDirection = data.getString("outputDirection");

        tooltip.add(Component.translatable(
                isFormed ? "gtceu.top.ldp_endpoint.is_formed" : "gtceu.top.ldp_endpoint.not_formed"));
        tooltip.add(Component.translatable("gtceu.top.ldp_endpoint.io_type", Component.translatable(ioType)
                .withStyle(ioType.contains("import") ? ChatFormatting.GREEN : ChatFormatting.RED)));
        tooltip.add(Component.translatable("gtceu.top.ldp_endpoint.output_direction",
                FormattingUtil.toEnglishName((outputDirection))));
    }
}
