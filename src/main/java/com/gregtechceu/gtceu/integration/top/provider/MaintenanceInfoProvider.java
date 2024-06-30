package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.apiimpl.styles.ItemStyle;
import org.jetbrains.annotations.Nullable;

public class MaintenanceInfoProvider extends CapabilityInfoProvider<IMaintenanceMachine> {

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
    protected void addProbeInfo(IMaintenanceMachine maintenanceMachine, IProbeInfo iProbeInfo, Player player,
                                BlockEntity blockEntity, IProbeHitData iProbeHitData) {
        IProbeInfo verticalPane = iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle().spacing(0));
        if (maintenanceMachine.hasMaintenanceProblems()) {
            if (player.isShiftKeyDown()) {
                int problems = maintenanceMachine.getMaintenanceProblems();
                for (byte i = 0; i < 6; i++) {
                    if (((problems >> i) & 1) == 0) {
                        IProbeInfo horizontalPane = verticalPane
                                .horizontal(verticalPane.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                        var tuple = GTUtil.getMaintenanceText(i);
                        horizontalPane.item(tuple.getA(), new ItemStyle().width(16).height(16))
                                .text(tuple.getB());
                    }
                }
            } else {
                verticalPane.text(CompoundText.create().error(Component.translatable("gtceu.top.maintenance_broken")));
            }
        } else {
            verticalPane.text(CompoundText.create().ok(Component.translatable("gtceu.top.maintenance_fixed")));
        }
    }

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("maintenance_info");
    }
}
