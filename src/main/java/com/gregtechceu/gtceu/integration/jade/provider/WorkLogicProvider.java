package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IWorkLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class WorkLogicProvider extends CapabilityBlockProvider<WorkLogic> {

    public WorkLogicProvider() {
        super(GTCEu.id("work_logic_provider"));
    }

    @Nullable
    @Override
    protected WorkLogic getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity mbe) {
            var machine = mbe.getMetaMachine();
            if (machine instanceof IWorkLogicMachine workLogicMachine) {
                return workLogicMachine.getWorkLogic();
            }
            for (MachineTrait trait : machine.getTraits()) {
                if (trait instanceof WorkLogic workLogic) {
                    return workLogic;
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, WorkLogic capability) {
        var waitingReason = capability.getWaitingReason();
        if (capability.isWaiting() && waitingReason != null && capability.isWorkingEnabled()) {
            data.putString("WaitingReason", Component.Serializer.toJson(waitingReason));
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (capData.contains("WaitingReason")) {
            var waitingReason = Component.Serializer.fromJson(capData.getString("WaitingReason"));
            if (waitingReason != null) {
                tooltip.add(Component.translatable("gtceu.recipe_logic.recipe_waiting").withStyle(ChatFormatting.YELLOW));
                tooltip.add(waitingReason);
            }
        }
    }

    @Override
    public int getDefaultPriority() {
        return super.getDefaultPriority();
    }
}
