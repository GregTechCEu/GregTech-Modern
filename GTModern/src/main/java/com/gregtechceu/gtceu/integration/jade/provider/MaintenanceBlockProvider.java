package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class MaintenanceBlockProvider extends CapabilityBlockProvider<IMaintenanceMachine> {

    public MaintenanceBlockProvider() {
        super(GTCEu.id("maintenance_info"));
    }

    @Nullable
    @Override
    protected IMaintenanceMachine getCapability(Level level, BlockPos blockPos, @Nullable Direction direction) {
        var cap = GTCapabilityHelper.getMaintenanceMachine(level, blockPos, direction);
        if (cap != null) {
            return cap;
        }
        if (MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
            for (var part : controller.getParts()) {
                if (part instanceof IMaintenanceMachine maintenanceMachine) {
                    return maintenanceMachine;
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag compoundTag, IMaintenanceMachine maintenanceMachine) {
        compoundTag.putBoolean("hasProblems", maintenanceMachine.hasMaintenanceProblems());
        if (maintenanceMachine.hasMaintenanceProblems()) {
            compoundTag.putInt("maintenanceProblems", maintenanceMachine.getMaintenanceProblems());
        }
    }

    @Override
    protected void addTooltip(CompoundTag compoundTag, ITooltip iTooltip, Player player, BlockAccessor blockAccessor,
                              BlockEntity blockEntity, IPluginConfig iPluginConfig) {
        if (compoundTag.contains("hasProblems", Tag.TAG_BYTE)) {
            if (compoundTag.getBoolean("hasProblems")) {
                if (blockAccessor.showDetails()) {
                    int problems = compoundTag.getInt("maintenanceProblems");
                    for (byte i = 0; i < 6; i++) {
                        if (((problems >> i) & 1) == 0) {
                            var tuple = GTUtil.getMaintenanceText(i);
                            IElementHelper helper = iTooltip.getElementHelper();
                            iTooltip.add(helper.smallItem(tuple.getA()));
                            iTooltip.append(tuple.getB());
                        }
                    }
                } else {
                    iTooltip.add(Component.translatable("gtceu.top.maintenance_broken").withStyle(ChatFormatting.RED));
                }
            } else {
                iTooltip.add(Component.translatable("gtceu.top.maintenance_fixed").withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
