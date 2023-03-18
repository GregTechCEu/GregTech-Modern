package com.lowdragmc.gtceu.api.capability.fabric;

import com.lowdragmc.gtceu.api.capability.*;
import com.lowdragmc.gtceu.api.machine.trait.RecipeLogic;
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
        return GTCapability.CAPABILITY_ELECTRIC_ITEM.find(itemStack, null);
    }
}
