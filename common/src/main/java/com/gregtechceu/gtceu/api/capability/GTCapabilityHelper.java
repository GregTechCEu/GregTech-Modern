package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote EnergyContainerHelper
 */
public class GTCapabilityHelper {
    @ExpectPlatform
    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergyItem(ItemStack itemStack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IToolable getToolable(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static RecipeLogic getRecipeLogic(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IPlatformEnergyStorage getPlatformEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static ICleanroomReceiver getCleanroomReceiver(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static IMaintenanceMachine getMaintenanceMachine(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        throw new AssertionError();
    }
}
