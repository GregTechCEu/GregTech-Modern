package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.*;
import com.gregtechceu.gtceu.common.cover.*;
import com.gregtechceu.gtceu.common.cover.detector.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2023/2/24
 * @implNote GTCovers
 */
public class GTCovers {

    public final static CoverDefinition FACADE = register(
            "facade", FacadeCover::new,
            FacadeCoverRenderer.INSTANCE
    );

    public final static CoverDefinition ITEM_FILTER = register(
            "item_filter", ItemFilterCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_item_filter"))
    );

    public final static CoverDefinition FLUID_FILTER = register(
            "fluid_filter", FluidFilterCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_fluid_filter"))
    );

    public final static CoverDefinition INFINITE_WATER = register(
            "infinite_water", InfiniteWaterCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_infinite_water"))
    );

    public final static CoverDefinition[] CONVEYORS = registerTiered(
            "conveyor", ConveyorCover::new,
            tier -> ConveyorCoverRenderer.INSTANCE, GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV
    );

    public final static CoverDefinition[] ROBOT_ARMS = registerTiered(
            "robot_arm", RobotArmCover::new,
            tier -> RobotArmCoverRenderer.INSTANCE, GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV
    );

    public final static CoverDefinition[] PUMPS = registerTiered(
            "pump", PumpCover::new,
            tier -> PumpCoverRenderer.INSTANCE, GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV
    );

    public final static CoverDefinition COMPUTER_MONITOR = register(
            "computer_monitor", ComputerMonitorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_display"))
    );

    public final static CoverDefinition MACHINE_CONTROLLER = register(
            "machine_controller", MachineControllerCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_controller"))
    );


    // Detectors
    public final static CoverDefinition ACTIVITY_DETECTOR = register(
            "activity_detector", ActivityDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_activity_detector"))
    );
    public final static CoverDefinition ACTIVITY_DETECTOR_ADVANCED = register(
            "activity_detector_advanced", AdvancedActivityDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_activity_detector_advanced"))
    );
    public final static CoverDefinition FLUID_DETECTOR = register(
            "fluid_detector", FluidDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_fluid_detector"))
    );
    public final static CoverDefinition FLUID_DETECTOR_ADVANCED = register(
            "fluid_detector_advanced", AdvancedFluidDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_fluid_detector_advanced"))
    );
    public final static CoverDefinition ITEM_DETECTOR = register(
            "item_detector", ItemDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_item_detector"))
    );
    public final static CoverDefinition ITEM_DETECTOR_ADVANCED = register(
            "item_detector_advanced", AdvancedItemDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_item_detector_advanced"))
    );
    public final static CoverDefinition ENERGY_DETECTOR = register(
            "energy_detector", EnergyDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_energy_detector"))
    );
    public final static CoverDefinition ENERGY_DETECTOR_ADVANCED = register(
            "energy_detector_advanced", AdvancedEnergyDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_energy_detector_advanced"))
    );
    public final static CoverDefinition MAINTENANCE_DETECTOR = register(
            "maintenance_detector", MaintenanceDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_maintenance_detector"))
    );


    ///////////////////////////////////////////////
    //***********     UTIL METHODS    ***********//
    ///////////////////////////////////////////////

    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator) {
        return register(id, behaviorCreator, new SimpleCoverRenderer(GTCEu.id("block/cover/" + id)));
    }

    public static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator, ICoverRenderer coverRenderer) {
        var definition = new CoverDefinition(GTCEu.id(id), behaviorCreator, coverRenderer);
        GTRegistries.COVERS.register(GTCEu.id(id), definition);
        return definition;
    }

    public static CoverDefinition[] registerTiered(String id, CoverDefinition.TieredCoverBehaviourProvider behaviorCreator, Int2ObjectFunction<ICoverRenderer> coverRenderer, int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase();
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier), coverRenderer.apply(tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static CoverDefinition[] registerTiered(String id, CoverDefinition.TieredCoverBehaviourProvider behaviorCreator, int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase();
            return register(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerCovers);
    }
}
