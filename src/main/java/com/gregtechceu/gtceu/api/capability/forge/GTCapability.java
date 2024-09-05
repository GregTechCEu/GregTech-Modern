package com.gregtechceu.gtceu.api.capability.forge;

import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.data.IComputationProvider;
import com.gregtechceu.gtceu.api.capability.data.IComputationUser;
import com.gregtechceu.gtceu.api.capability.data.IDataAccess;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

/**
 * @author KilaBash
 * @date 2023/2/18
 * @implNote GTCapabilities
 */
public class GTCapability {

    public static final Capability<IEnergyContainer> CAPABILITY_ENERGY_CONTAINER = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IEnergyInfoProvider> CAPABILITY_ENERGY_INFO_PROVIDER = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<ICoverable> CAPABILITY_COVERABLE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IToolable> CAPABILITY_TOOLABLE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IWorkable> CAPABILITY_WORKABLE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IControllable> CAPABILITY_CONTROLLABLE = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<RecipeLogic> CAPABILITY_RECIPE_LOGIC = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IElectricItem> CAPABILITY_ELECTRIC_ITEM = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<ICleanroomReceiver> CAPABILITY_CLEANROOM_RECEIVER = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IMaintenanceMachine> CAPABILITY_MAINTENANCE_MACHINE = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<ILaserRelay> CAPABILITY_LASER = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IComputationProvider> CAPABILITY_COMPUTATION_PROVIDER = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IComputationUser> CAPABILITY_COMPUTATION_USER = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IDataAccess> CAPABILITY_DATA_ACCESS = CapabilityManager
            .get(new CapabilityToken<>() {});
    public static final Capability<IHazardParticleContainer> CAPABILITY_HAZARD_CONTAINER = CapabilityManager
            .get(new CapabilityToken<>() {});

    public static final Capability<IMedicalConditionTracker> CAPABILITY_MEDICAL_CONDITION_TRACKER = CapabilityManager
            .get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IEnergyContainer.class);
        event.register(IEnergyInfoProvider.class);
        event.register(ICoverable.class);
        event.register(IToolable.class);
        event.register(IWorkable.class);
        event.register(IControllable.class);
        event.register(RecipeLogic.class);
        event.register(IElectricItem.class);
        event.register(ICleanroomReceiver.class);
        event.register(IMaintenanceMachine.class);
        event.register(ILaserRelay.class);
        event.register(IComputationProvider.class);
        event.register(IComputationUser.class);
        event.register(IDataAccess.class);
        event.register(IMedicalConditionTracker.class);
        event.register(IHazardParticleContainer.class);
    }
}
