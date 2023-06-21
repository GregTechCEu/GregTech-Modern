package com.gregtechceu.gtceu.api.capability.forge;

import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
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

    @SuppressWarnings({"DataFlowIssue", "ConstantValue"})
    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                IEnergyStorage energyStorage = blockEntity.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                return energyStorage == null ? null : toPlatformEnergyStorage(energyStorage);
            }
        }
        return null;
    }

    public static IPlatformEnergyStorage toPlatformEnergyStorage(IEnergyStorage handler) {
        return new IPlatformEnergyStorage() {
            @Override
            public long insert(long maxAmount, boolean simulate) {
                return handler.receiveEnergy((int) maxAmount, simulate);
            }

            @Override
            public long extract(long maxAmount, boolean simulate) {
                return handler.extractEnergy((int) maxAmount, simulate);
            }

            @Override
            public long getAmount() {
                return handler.getEnergyStored();
            }

            @Override
            public long getCapacity() {
                return handler.getMaxEnergyStored();
            }

            @Override
            public boolean supportsInsertion() {
                return handler.canReceive();
            }

            @Override
            public boolean supportsExtraction() {
                return handler.canExtract();
            }
        };
    }

    public static IEnergyStorage toEnergyStorage(IPlatformEnergyStorage energyStorage) {
        return new IEnergyStorage() {

            @Override
            public int receiveEnergy(int i, boolean bl) {
                return (int) Math.min(energyStorage.insert(i, bl), Integer.MAX_VALUE);
            }

            @Override
            public int extractEnergy(int i, boolean bl) {
                return (int) Math.min(energyStorage.extract(i, bl), Integer.MAX_VALUE);
            }

            @Override
            public int getEnergyStored() {
                return (int) Math.min(energyStorage.getAmount(), Integer.MAX_VALUE);
            }

            @Override
            public int getMaxEnergyStored() {
                return (int) Math.min(energyStorage.getCapacity(), Integer.MAX_VALUE);
            }

            @Override
            public boolean canExtract() {
                return energyStorage.supportsExtraction();
            }

            @Override
            public boolean canReceive() {
                return energyStorage.supportsInsertion();
            }
        };
    }
}
