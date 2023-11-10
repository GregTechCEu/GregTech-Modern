package com.gregtechceu.gtceu.api.capability.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote EnergyContainerHelperImpl
 */
public class GTCapabilityHelperImpl {

    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_ENERGY.find(level, pos, side);
    }

    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_COVERABLE.find(level, pos, side);
    }

    @Nullable
    public static IToolable getToolable(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_TOOLABLE.find(level, pos, side);
    }

    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_WORKABLE.find(level, pos, side);
    }

    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_CONTROLLABLE.find(level, pos, side);
    }

    @Nullable
    public static RecipeLogic getRecipeLogic(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_RECIPE_LOGIC.find(level, pos, side);
    }

    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        return GTCapability.CAPABILITY_ELECTRIC_ITEM.find(itemStack, ContainerItemContext.withConstant(itemStack));
    }

    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergyItem(ItemStack itemStack) {
        if (GTCEu.isRebornEnergyLoaded()) {
            var energyItem = ContainerItemContext.withConstant(itemStack).find(EnergyStorage.ITEM);
            return energyItem == null ? null : GTEnergyHelperImpl.toPlatformEnergyStorage(energyItem);
        }
        return null;
    }

    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        if (GTCEu.isRebornEnergyLoaded()) {
            var energyStorage = EnergyStorage.SIDED.find(level, pos, side);
            return energyStorage == null ? null : GTEnergyHelperImpl.toPlatformEnergyStorage(energyStorage);
        }
        return null;
    }

    @Nullable
    public static ICleanroomReceiver getCleanroomReceiver(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_CLEANROOM_RECEIVER.find(level, pos, side);
    }

    @Nullable
    public static IMaintenanceMachine getMaintenanceMachine(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_MAINTENANCE_MACHINE.find(level, pos, side);
    }

    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapability.CAPABILITY_LASER.find(level, pos, side);
    }
}
