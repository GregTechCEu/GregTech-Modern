package com.gregtechceu.gtceu.api.capability.forge;

import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote EnergyContainerHelperImpl
 */
public class GTCapabilityHelperImpl {
    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_COVERABLE, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static IToolable getToolable(Level level, BlockPos pos, Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_TOOLABLE, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_WORKABLE, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_CONTROLLABLE, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static RecipeLogic getRecipeLogic(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_RECIPE_LOGIC, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        return itemStack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM).resolve().orElse(null);
    }

    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergyItem(ItemStack itemStack) {
        IEnergyStorage energyItemStorage = itemStack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        return energyItemStorage == null ? null : GTEnergyHelperImpl.toPlatformEnergyStorage(energyItemStorage);
    }

    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
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
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_CLEANROOM_RECEIVER, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static IMaintenanceMachine getMaintenanceMachine(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_MAINTENANCE_MACHINE, side).resolve().orElse(null);
            }
        }
        return null;
    }

    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_LASER, side).resolve().orElse(null);
            }
        }
        return null;
    }
}
