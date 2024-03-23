package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.forge.GTEnergyHelperImpl;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote EnergyContainerHelper
 */
public class GTCapabilityHelper {
    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        return itemStack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM).resolve().orElse(null);
    }

    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergyItem(ItemStack itemStack) {
        IEnergyStorage energyItemStorage = itemStack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        return energyItemStorage == null ? null : GTEnergyHelperImpl.toPlatformEnergyStorage(energyItemStorage);
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, level, pos, side);
    }

    @Nullable
    public static IEnergyInfoProvider getEnergyInfoProvider(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, level, pos, side);
    }

    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_COVERABLE, level, pos, side);
    }

    @Nullable
    public static IToolable getToolable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_TOOLABLE, level, pos, side);
    }

    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_WORKABLE, level, pos, side);
    }

    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_CONTROLLABLE, level, pos, side);
    }

    @Nullable
    public static RecipeLogic getRecipeLogic(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_RECIPE_LOGIC, level, pos, side);
    }

    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                IEnergyStorage energyStorage = blockEntity.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
                return energyStorage == null ? null : GTEnergyHelperImpl.toPlatformEnergyStorage(energyStorage);
            }
        }
        return null;
    }

    @Nullable
    public static ICleanroomReceiver getCleanroomReceiver(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_CLEANROOM_RECEIVER, level, pos, side);
    }

    @Nullable
    public static IMaintenanceMachine getMaintenanceMachine(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_MAINTENANCE_MACHINE, level, pos, side);
    }

    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_LASER, level, pos, side);
    }

    @Nullable
    public static IOpticalComputationProvider getOpticalComputationProvider(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CABABILITY_COMPUTATION_PROVIDER, level, pos, side);
    }

    @Nullable
    public static IDataAccessHatch getDataAccess(Level level, BlockPos pos, @Nullable Direction side) {
        return getBlockEntityCapability(GTCapability.CAPABILITY_DATA_ACCESS, level, pos, side);
    }

    @Nullable
    private static <T> T getBlockEntityCapability(Capability<T> capability, Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(capability, side).resolve().orElse(null);
            }
        }
        return null;
    }
}
