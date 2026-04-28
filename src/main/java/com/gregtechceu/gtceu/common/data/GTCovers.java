package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.*;

import net.minecraft.resources.Identifier;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

public class GTCovers {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.LV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);
    public static final int[] ALL_TIERS_WITH_ULV = GTValues.tiersBetween(GTValues.ULV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);

    private static final String COVER_PACKAGE = "com.gregtechceu.gtceu.common.cover.";
    private static final String DETECTOR_PACKAGE = COVER_PACKAGE + "detector.";
    private static final String ENDER_PACKAGE = COVER_PACKAGE + "ender.";
    private static final String VOIDING_PACKAGE = COVER_PACKAGE + "voiding.";

    public final static CoverDefinition FACADE = register("facade",
            CoverBehaviourProviders.of(COVER_PACKAGE + "FacadeCover"),
            () -> () -> FacadeCoverRenderer.INSTANCE);

    public final static CoverDefinition ITEM_FILTER = register("item_filter",
            CoverBehaviourProviders.of(COVER_PACKAGE + "ItemFilterCover"));
    public final static CoverDefinition FLUID_FILTER = register("fluid_filter",
            CoverBehaviourProviders.of(COVER_PACKAGE + "FluidFilterCover"));

    public final static CoverDefinition INFINITE_WATER = register("infinite_water",
            CoverBehaviourProviders.of(COVER_PACKAGE + "InfiniteWaterCover"));
    public final static CoverDefinition ENDER_FLUID_LINK = register("ender_fluid_link",
            CoverBehaviourProviders.of(ENDER_PACKAGE + "EnderFluidLinkCover"));
    public final static CoverDefinition ENDER_ITEM_LINK = register("ender_item_link",
            CoverBehaviourProviders.of(ENDER_PACKAGE + "EnderItemLinkCover"));
    public final static CoverDefinition ENDER_REDSTONE_LINK = register("ender_redstone_link",
            CoverBehaviourProviders.of(ENDER_PACKAGE + "EnderRedstoneLinkCover"));
    public final static CoverDefinition SHUTTER = register("shutter",
            CoverBehaviourProviders.of(COVER_PACKAGE + "ShutterCover"));
    public final static CoverDefinition COVER_STORAGE = register("storage",
            CoverBehaviourProviders.of(COVER_PACKAGE + "StorageCover"));
    public final static CoverDefinition WIRELESS_TRANSMITTER = register("wireless_transmitter",
            CoverBehaviourProviders.of(COVER_PACKAGE + "WirelessTransmitterCover"));

    public final static CoverDefinition[] CONVEYORS = registerTiered("conveyor",
            CoverBehaviourProviders.tiered(COVER_PACKAGE + "ConveyorCover"),
            () -> tier -> new IOCoverRenderer(
                    GTCEu.id("block/cover/conveyor"),
                    null,
                    GTCEu.id("block/cover/conveyor_emissive"),
                    GTCEu.id("block/cover/conveyor_inverted_emissive")),
            ALL_TIERS);

    public final static CoverDefinition[] ROBOT_ARMS = registerTiered("robot_arm",
            CoverBehaviourProviders.tiered(COVER_PACKAGE + "RobotArmCover"),
            () -> tier -> new IOCoverRenderer(
                    GTCEu.id("block/cover/arm"),
                    null,
                    GTCEu.id("block/cover/arm_emissive"),
                    GTCEu.id("block/cover/arm_inverted_emissive")),
            ALL_TIERS);

    public final static CoverDefinition[] PUMPS = registerTiered("pump",
            CoverBehaviourProviders.tiered(COVER_PACKAGE + "PumpCover"),
            () -> tier -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER, ALL_TIERS);

    public final static CoverDefinition[] FLUID_REGULATORS = registerTiered("fluid_regulator",
            CoverBehaviourProviders.tiered(COVER_PACKAGE + "FluidRegulatorCover"),
            () -> tier -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER, ALL_TIERS);

    public final static CoverDefinition COMPUTER_MONITOR = register("computer_monitor",
            CoverBehaviourProviders.of(COVER_PACKAGE + "ComputerMonitorCover"));

    public final static CoverDefinition MACHINE_CONTROLLER = register("machine_controller",
            CoverBehaviourProviders.of(COVER_PACKAGE + "MachineControllerCover"));

    // Voiding
    public final static CoverDefinition ITEM_VOIDING = register("item_voiding",
            CoverBehaviourProviders.of(VOIDING_PACKAGE + "ItemVoidingCover"));
    public final static CoverDefinition ITEM_VOIDING_ADVANCED = register("item_voiding_advanced",
            CoverBehaviourProviders.of(VOIDING_PACKAGE + "AdvancedItemVoidingCover"));
    public final static CoverDefinition FLUID_VOIDING = register("fluid_voiding",
            CoverBehaviourProviders.of(VOIDING_PACKAGE + "FluidVoidingCover"));
    public final static CoverDefinition FLUID_VOIDING_ADVANCED = register("fluid_voiding_advanced",
            CoverBehaviourProviders.of(VOIDING_PACKAGE + "AdvancedFluidVoidingCover"));

    // Detectors
    public final static CoverDefinition ACTIVITY_DETECTOR = register("activity_detector",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "ActivityDetectorCover"));
    public final static CoverDefinition ACTIVITY_DETECTOR_ADVANCED = register("activity_detector_advanced",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "AdvancedActivityDetectorCover"));
    public final static CoverDefinition FLUID_DETECTOR = register("fluid_detector",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "FluidDetectorCover"));
    public final static CoverDefinition FLUID_DETECTOR_ADVANCED = register("fluid_detector_advanced",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "AdvancedFluidDetectorCover"));
    public final static CoverDefinition ITEM_DETECTOR = register("item_detector",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "ItemDetectorCover"));
    public final static CoverDefinition ITEM_DETECTOR_ADVANCED = register("item_detector_advanced",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "AdvancedItemDetectorCover"));
    public final static CoverDefinition ENERGY_DETECTOR = register("energy_detector",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "EnergyDetectorCover"));
    public final static CoverDefinition ENERGY_DETECTOR_ADVANCED = register("energy_detector_advanced",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "AdvancedEnergyDetectorCover"));
    public final static CoverDefinition MAINTENANCE_DETECTOR = register("maintenance_detector",
            CoverBehaviourProviders.of(DETECTOR_PACKAGE + "MaintenanceDetectorCover"));

    // Solar Panels
    public final static CoverDefinition SOLAR_PANEL_BASIC = register("solar_panel",
            CoverBehaviourProviders.of(COVER_PACKAGE + "CoverSolarPanel"));
    public final static CoverDefinition[] SOLAR_PANEL = registerTiered("solar_panel",
            CoverBehaviourProviders.tiered(COVER_PACKAGE + "CoverSolarPanel"),
            () -> tier -> new SimpleCoverRenderer(GTCEu.id("block/cover/solar_panel")), ALL_TIERS_WITH_ULV);

    ///////////////////////////////////////////////
    // *********** UTIL METHODS ***********//
    ///////////////////////////////////////////////

    private static CoverDefinition register(String id,
                                            Supplier<CoverDefinition.CoverBehaviourProvider> behaviorCreator) {
        return register(id, behaviorCreator, () -> () -> new SimpleCoverRenderer(GTCEu.id("block/cover/" + id)));
    }

    private static CoverDefinition register(String id, Supplier<CoverDefinition.CoverBehaviourProvider> behaviorCreator,
                                            Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        return register(GTCEu.id(id), behaviorCreator, coverRenderer);
    }

    public static CoverDefinition register(Identifier id,
                                           Supplier<CoverDefinition.CoverBehaviourProvider> behaviorCreator,
                                           Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        var definition = new CoverDefinition(id, behaviorCreator, coverRenderer);
        GTRegistries.register(GTRegistries.COVERS, definition.getId(), definition);
        return definition;
    }

    private static CoverDefinition[] registerTiered(String id,
                                                    Supplier<CoverDefinition.TieredCoverBehaviourProvider> behaviorCreator,
                                                    Supplier<Int2ObjectFunction<ICoverRenderer>> coverRenderer,
                                                    int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name,
                    () -> (def, coverable, side) -> behaviorCreator.get().create(def, coverable, side, tier),
                    () -> () -> coverRenderer.get().apply(tier));
        }).toArray(CoverDefinition[]::new);
    }

    private static CoverDefinition[] registerTiered(String id,
                                                    Supplier<CoverDefinition.TieredCoverBehaviourProvider> behaviorCreator,
                                                    int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return register(name,
                    () -> (def, coverable, side) -> behaviorCreator.get().create(def, coverable, side, tier));
        }).toArray(CoverDefinition[]::new);
    }

    public static void init() {}
}
