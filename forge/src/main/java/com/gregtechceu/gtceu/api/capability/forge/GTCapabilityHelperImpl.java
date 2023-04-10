package com.gregtechceu.gtceu.api.capability.forge;

import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
}
