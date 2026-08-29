package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.client.renderer.cover.*;
import com.gregtechceu.gtceu.common.cover.*;
import com.gregtechceu.gtceu.common.cover.detector.*;
import com.gregtechceu.gtceu.common.cover.ender.EnderFluidLinkCover;
import com.gregtechceu.gtceu.common.cover.ender.EnderItemLinkCover;
import com.gregtechceu.gtceu.common.cover.ender.EnderRedstoneLinkCover;
import com.gregtechceu.gtceu.common.cover.voiding.AdvancedFluidVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.AdvancedItemVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.FluidVoidingCover;
import com.gregtechceu.gtceu.common.cover.voiding.ItemVoidingCover;

import com.tterrag.registrate.util.entry.RegistryEntry;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minecraft.core.Holder;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class GTCovers {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(GTValues.LV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);
    public static final int[] ALL_TIERS_WITH_ULV = GTValues.tiersBetween(GTValues.ULV,
            GTCEuAPI.isHighTier() ? GTValues.OpV : GTValues.UV);

    public static final Holder<CoverDefinition> FACADE = REGISTRATE.cover("facade", FacadeCover::new,
            () -> () -> FacadeCoverRenderer.INSTANCE);

    public static final Holder<CoverDefinition> ITEM_FILTER = REGISTRATE.cover("item_filter", ItemFilterCover::new);
    public static final Holder<CoverDefinition> FLUID_FILTER = REGISTRATE.cover("fluid_filter", FluidFilterCover::new);

    public static final Holder<CoverDefinition> INFINITE_WATER = REGISTRATE.cover("infinite_water", InfiniteWaterCover::new);
    public static final Holder<CoverDefinition> ENDER_FLUID_LINK = REGISTRATE.cover("ender_fluid_link", EnderFluidLinkCover::new);
    public static final Holder<CoverDefinition> ENDER_ITEM_LINK = REGISTRATE.cover("ender_item_link", EnderItemLinkCover::new);
    public static final Holder<CoverDefinition> ENDER_REDSTONE_LINK = REGISTRATE.cover("ender_redstone_link",
            EnderRedstoneLinkCover::new);
    public static final Holder<CoverDefinition> SHUTTER = REGISTRATE.cover("shutter", ShutterCover::new);
    public static final Holder<CoverDefinition> COVER_STORAGE = REGISTRATE.cover("storage", StorageCover::new);
    public static final Holder<CoverDefinition> WIRELESS_TRANSMITTER = REGISTRATE.cover("wireless_transmitter",
            WirelessTransmitterCover::new);

    public static final Holder<CoverDefinition>[] CONVEYORS = registerTiered(REGISTRATE, "conveyor", ConveyorCover::new,
            () -> tier -> new IOCoverRenderer(
                    GTCEu.id("block/cover/conveyor"),
                    null,
                    GTCEu.id("block/cover/conveyor_emissive"),
                    GTCEu.id("block/cover/conveyor_inverted_emissive")),
            ALL_TIERS);

    public static final Holder<CoverDefinition>[] ROBOT_ARMS = registerTiered(REGISTRATE, "robot_arm", RobotArmCover::new,
            () -> tier -> new IOCoverRenderer(
                    GTCEu.id("block/cover/arm"),
                    null,
                    GTCEu.id("block/cover/arm_emissive"),
                    GTCEu.id("block/cover/arm_inverted_emissive")),
            ALL_TIERS);

    public static final Holder<CoverDefinition>[] PUMPS = registerTiered(REGISTRATE,"pump", PumpCover::new,
            () -> tier -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER, ALL_TIERS);

    public static final Holder<CoverDefinition>[] FLUID_REGULATORS = registerTiered(REGISTRATE,"fluid_regulator", FluidRegulatorCover::new,
            () -> tier -> IOCoverRenderer.PUMP_LIKE_COVER_RENDERER, ALL_TIERS);

    public static final Holder<CoverDefinition> COMPUTER_MONITOR = REGISTRATE.cover("computer_monitor", ComputerMonitorCover::new);

    public static final Holder<CoverDefinition> MACHINE_CONTROLLER = REGISTRATE.cover("machine_controller",
            MachineControllerCover::new);

    // Voiding
    public static final Holder<CoverDefinition> ITEM_VOIDING = REGISTRATE.cover("item_voiding", ItemVoidingCover::new);
    public static final Holder<CoverDefinition> ITEM_VOIDING_ADVANCED = REGISTRATE.cover("item_voiding_advanced",
            AdvancedItemVoidingCover::new);
    public static final Holder<CoverDefinition> FLUID_VOIDING = REGISTRATE.cover("fluid_voiding", FluidVoidingCover::new);
    public static final Holder<CoverDefinition> FLUID_VOIDING_ADVANCED = REGISTRATE.cover("fluid_voiding_advanced",
            AdvancedFluidVoidingCover::new);

    // Detectors
    public static final Holder<CoverDefinition> ACTIVITY_DETECTOR = REGISTRATE.cover("activity_detector", ActivityDetectorCover::new);
    public static final Holder<CoverDefinition> ACTIVITY_DETECTOR_ADVANCED = REGISTRATE.cover("activity_detector_advanced",
            AdvancedActivityDetectorCover::new);
    public static final Holder<CoverDefinition> FLUID_DETECTOR = REGISTRATE.cover("fluid_detector", FluidDetectorCover::new);
    public static final Holder<CoverDefinition> FLUID_DETECTOR_ADVANCED = REGISTRATE.cover("fluid_detector_advanced",
            AdvancedFluidDetectorCover::new);
    public static final Holder<CoverDefinition> ITEM_DETECTOR = REGISTRATE.cover("item_detector", ItemDetectorCover::new);
    public static final Holder<CoverDefinition> ITEM_DETECTOR_ADVANCED = REGISTRATE.cover("item_detector_advanced",
            AdvancedItemDetectorCover::new);
    public static final Holder<CoverDefinition> ENERGY_DETECTOR = REGISTRATE.cover("energy_detector", EnergyDetectorCover::new);
    public static final Holder<CoverDefinition> ENERGY_DETECTOR_ADVANCED = REGISTRATE.cover("energy_detector_advanced",
            AdvancedEnergyDetectorCover::new);
    public static final Holder<CoverDefinition> MAINTENANCE_DETECTOR = REGISTRATE.cover("maintenance_detector",
            MaintenanceDetectorCover::new);

    // Solar Panels
    public static final Holder<CoverDefinition> SOLAR_PANEL_BASIC = REGISTRATE.cover("solar_panel", CoverSolarPanel::new);
    public static final Holder<CoverDefinition>[] SOLAR_PANEL = registerTiered(REGISTRATE, "solar_panel", CoverSolarPanel::new,
            () -> tier -> new SimpleCoverRenderer(GTCEu.id("block/cover/solar_panel")), ALL_TIERS_WITH_ULV);

    ///////////////////////////////////////////////
    // *********** UTIL METHODS ***********//
    ///////////////////////////////////////////////
    
    @SuppressWarnings("unchecked")
    public static Holder<CoverDefinition>[] registerTiered(GTRegistrate registrate, String id,
                                                   CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                   Supplier<Int2ObjectFunction<ICoverRenderer>> coverRenderer,
                                                   int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            return registrate.cover(name, (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier),
                    () -> () -> coverRenderer.get().apply(tier));
        }).toArray(RegistryEntry[]::new);
    }

    public static void init() {}
}
