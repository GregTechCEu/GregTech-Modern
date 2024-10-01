package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.cover.*;
import com.gregtechceu.gtceu.common.cover.detector.*;
import com.gregtechceu.gtceu.common.cover.voiding.AdvancedFluidVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.AdvancedItemVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.FluidVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.ItemVoidingCover;

import net.minecraftforge.fml.ModLoader;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote GTCovers
 */
public class GTCovers {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.LV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);
    public static final int[] ALL_TIERS_WITH_ULV = GTValues.tiersBetween(GTValues.ULV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);

    static {
        GTRegistries.COVERS.unfreeze();
    }

    public final static CoverDefinition FACADE = register("facade", FacadeCover::new);
    public final static CoverDefinition ITEM_FILTER = register("item_filter", ItemFilterCover::new);
    public final static CoverDefinition FLUID_FILTER = register("fluid_filter", FluidFilterCover::new);
    public final static CoverDefinition INFINITE_WATER = register("infinite_water", InfiniteWaterCover::new);
    public final static CoverDefinition SHUTTER = register("shutter", ShutterCover::new);
    public final static CoverDefinition[] CONVEYORS = registerTiered("conveyor", ConveyorCover::new, ALL_TIERS);
    public final static CoverDefinition[] ROBOT_ARMS = registerTiered("robot_arm", RobotArmCover::new, ALL_TIERS);
    public final static CoverDefinition[] PUMPS = registerTiered("pump", PumpCover::new, ALL_TIERS);
    public final static CoverDefinition[] FLUID_REGULATORS = registerTiered("fluid_regulator", FluidRegulatorCover::new,
            ALL_TIERS);

    public final static CoverDefinition COMPUTER_MONITOR = register("computer_monitor", ComputerMonitorCover::new);
    public final static CoverDefinition MACHINE_CONTROLLER = register("machine_controller",
            MachineControllerCover::new);

    // Voiding
    public final static CoverDefinition ITEM_VOIDING = register("item_voiding", ItemVoidingCover::new);
    public final static CoverDefinition ITEM_VOIDING_ADVANCED = register("item_voiding_advanced",
            AdvancedItemVoidingCover::new);
    public final static CoverDefinition FLUID_VOIDING = register("fluid_voiding", FluidVoidingCover::new);
    public final static CoverDefinition FLUID_VOIDING_ADVANCED = register("fluid_voiding_advanced",
            AdvancedFluidVoidingCover::new);

    // Detectors
    public final static CoverDefinition ACTIVITY_DETECTOR = register("activity_detector", ActivityDetectorCover::new);
    public final static CoverDefinition ACTIVITY_DETECTOR_ADVANCED = register("activity_detector_advanced",
            AdvancedActivityDetectorCover::new);
    public final static CoverDefinition FLUID_DETECTOR = register("fluid_detector", FluidDetectorCover::new);
    public final static CoverDefinition FLUID_DETECTOR_ADVANCED = register("fluid_detector_advanced",
            AdvancedFluidDetectorCover::new);
    public final static CoverDefinition ITEM_DETECTOR = register("item_detector", ItemDetectorCover::new);
    public final static CoverDefinition ITEM_DETECTOR_ADVANCED = register("item_detector_advanced",
            AdvancedItemDetectorCover::new);
    public final static CoverDefinition ENERGY_DETECTOR = register("energy_detector", EnergyDetectorCover::new);
    public final static CoverDefinition ENERGY_DETECTOR_ADVANCED = register("energy_detector_advanced",
            AdvancedEnergyDetectorCover::new);
    public final static CoverDefinition MAINTENANCE_DETECTOR = register("maintenance_detector",
            MaintenanceDetectorCover::new);

    // Solar Panels
    public final static CoverDefinition[] SOLAR_PANEL = registerTiered("solar_panel", CoverSolarPanel::new,
            ALL_TIERS_WITH_ULV);

    ///////////////////////////////////////////////
    // *********** UTIL METHODS ***********//
    ///////////////////////////////////////////////

    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator) {
        var definition = new CoverDefinition(GTCEu.id(id), behaviorCreator);
        GTRegistries.COVERS.register(GTCEu.id(id), definition);
        return definition;
    }

    public static CoverDefinition[] registerTiered(String id,
                                                   CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                   int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerCovers);
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.COVERS, CoverDefinition.class));
        GTRegistries.COVERS.freeze();
    }
}
