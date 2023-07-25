package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.*;
import com.gregtechceu.gtceu.common.cover.*;
import com.gregtechceu.gtceu.common.cover.detector.ActivityDetectorCover;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedActivityDetectorCover;
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


    // Detectors
    public final static CoverDefinition ACTIVITY_DETECTOR = register(
            "activity_detector", ActivityDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_activity_detector"))
    );
    public final static CoverDefinition ACTIVITY_DETECTOR_ADVANCED = register(
            "activity_detector_advanced", AdvancedActivityDetectorCover::new,
            new SimpleCoverRenderer(GTCEu.id("block/cover/overlay_activity_detector_advanced"))
    );
    public final static CoverDefinition COVER_FLUID_DETECTOR = null; // TODO
    public final static CoverDefinition COVER_FLUID_DETECTOR_ADVANCED = null; // TODO
    public final static CoverDefinition COVER_ITEM_DETECTOR = null; // TODO
    public final static CoverDefinition COVER_ITEM_DETECTOR_ADVANCED = null; // TODO
    public final static CoverDefinition COVER_ENERGY_DETECTOR = null; // TODO
    public final static CoverDefinition COVER_ENERGY_DETECTOR_ADVANCED = null; // TODO


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
