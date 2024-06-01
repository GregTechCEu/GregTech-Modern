package com.gregtechceu.gtceu.api.capability.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote GTCapabilities
 */
public class GTCapability {

    public static final BlockCapability<IEnergyContainer, Direction> CAPABILITY_ENERGY_CONTAINER = BlockCapability
            .createSided(GTCEu.id("energy_container"), IEnergyContainer.class);
    public static final BlockCapability<IEnergyInfoProvider, Direction> CAPABILITY_ENERGY_INFO_PROVIDER = BlockCapability
            .createSided(GTCEu.id("energy_info_provider"), IEnergyInfoProvider.class);
    public static final BlockCapability<ICoverable, Direction> CAPABILITY_COVERABLE = BlockCapability
            .createSided(GTCEu.id("coverable"), ICoverable.class);
    public static final BlockCapability<IToolable, Direction> CAPABILITY_TOOLABLE = BlockCapability
            .createSided(GTCEu.id("toolable"), IToolable.class);
    public static final BlockCapability<IWorkable, Direction> CAPABILITY_WORKABLE = BlockCapability
            .createSided(GTCEu.id("workable"), IWorkable.class);
    public static final BlockCapability<IControllable, Direction> CAPABILITY_CONTROLLABLE = BlockCapability
            .createSided(GTCEu.id("controllable"), IControllable.class);
    public static final BlockCapability<RecipeLogic, Direction> CAPABILITY_RECIPE_LOGIC = BlockCapability
            .createSided(GTCEu.id("recipe_logic"), RecipeLogic.class);
    public static final ItemCapability<IElectricItem, Void> CAPABILITY_ELECTRIC_ITEM = ItemCapability
            .createVoid(GTCEu.id("electric_item"), IElectricItem.class);
    public static final BlockCapability<ICleanroomReceiver, Direction> CAPABILITY_CLEANROOM_RECEIVER = BlockCapability
            .createSided(GTCEu.id("cleanroom_receiver"), ICleanroomReceiver.class);
    public static final BlockCapability<IMaintenanceMachine, Direction> CAPABILITY_MAINTENANCE_MACHINE = BlockCapability
            .createSided(GTCEu.id("maintenance"), IMaintenanceMachine.class);
    public static final BlockCapability<ILaserContainer, Direction> CAPABILITY_LASER = BlockCapability
            .createSided(GTCEu.id("laser_container"), ILaserContainer.class);
    public static final BlockCapability<IOpticalComputationProvider, Direction> CAPABILITY_COMPUTATION_PROVIDER = BlockCapability
            .createSided(GTCEu.id("computation_provider"), IOpticalComputationProvider.class);
    public static final BlockCapability<IDataAccessHatch, Direction> CAPABILITY_DATA_ACCESS = BlockCapability
            .createSided(GTCEu.id("data_access"), IDataAccessHatch.class);

    /*
     * public static void register(RegisterCapabilitiesEvent event) {
     * event.register(IEnergyContainer.class);
     * event.register(IEnergyInfoProvider.class);
     * event.register(ICoverable.class);
     * event.register(IToolable.class);
     * event.register(IWorkable.class);
     * event.register(IControllable.class);
     * event.register(RecipeLogic.class);
     * event.register(IElectricItem.class);
     * event.register(ICleanroomReceiver.class);
     * event.register(IMaintenanceMachine.class);
     * event.register(ILaserContainer.class);
     * event.register(IOpticalComputationProvider.class);
     * event.register(IDataAccessHatch.class);
     * }
     */
}
