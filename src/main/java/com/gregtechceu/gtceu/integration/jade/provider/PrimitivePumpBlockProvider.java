package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.nbt.LongTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PrimitivePumpBlockProvider extends MachineInfoProvider<PrimitivePumpMachine, LongTag>
                                        implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public PrimitivePumpBlockProvider() {
        super(GTCEu.id("primitive_pump"), PrimitivePumpMachine.class);
    }

    @Override
    protected LongTag write(PrimitivePumpMachine machine) {
        return LongTag.valueOf(machine.getFluidProduction());
    }

    @Override
    protected void addTooltip(LongTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        tooltip.add(Component.translatable("gtceu.top.primitive_pump_production",
                FormattingUtil.formatNumbers(data.getAsLong())));
    }
}
