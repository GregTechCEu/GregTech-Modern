package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.data.misc.GTAttachmentTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTCapabilityHelper {

    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        return itemStack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM);
    }

    @Nullable
    public static IEnergyStorage getForgeEnergyItem(ItemStack itemStack) {
        return itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, pos, side);
    }

    @Nullable
    public static IEnergyInfoProvider getEnergyInfoProvider(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, pos, side);
    }

    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_COVERABLE, pos, side);
    }

    @Nullable
    public static IToolable getToolable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_TOOLABLE, pos, side);
    }

    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_WORKABLE, pos, side);
    }

    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_CONTROLLABLE, pos, side);
    }

    @Nullable
    public static RecipeLogic getRecipeLogic(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_RECIPE_LOGIC, pos, side);
    }

    @Nullable
    public static IEnergyStorage getForgeEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockState(pos).hasBlockEntity()) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                return blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, blockEntity.getBlockPos(),
                        blockEntity.getBlockState(), blockEntity, side);
            }
        }
        return null;
    }

    @Nullable
    public static ICleanroomReceiver getCleanroomReceiver(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_CLEANROOM_RECEIVER, pos, side);
    }

    @Nullable
    public static IMaintenanceMachine getMaintenanceMachine(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_MAINTENANCE_MACHINE, pos, side);
    }

    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_LASER, pos, side);
    }

    @Nullable
    public static IOpticalComputationProvider getOpticalComputationProvider(Level level, BlockPos pos,
                                                                            @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, pos, side);
    }

    @Nullable
    public static IDataAccessHatch getDataAccess(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, pos, side);
    }

    @Nullable
    public static IHazardParticleContainer getHazardContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, pos, side);
    }

    public static IMedicalConditionTracker getMedicalConditionTracker(@NotNull Player entity) {
        return entity.getData(GTAttachmentTypes.MEDICAL_CONDITION_TRACKER);
    }
}
