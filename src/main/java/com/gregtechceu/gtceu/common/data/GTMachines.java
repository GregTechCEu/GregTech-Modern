package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.machine.*;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.machines.*;
import com.gregtechceu.gtceu.common.machine.electric.*;
import com.gregtechceu.gtceu.common.machine.multiblock.part.*;
import com.gregtechceu.gtceu.common.machine.steam.SteamLiquidBoilerMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolarBoiler;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;
import com.gregtechceu.gtceu.common.machine.storage.*;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance.LDFluidEndpointMachine;
import com.gregtechceu.gtceu.common.pipelike.item.longdistance.LDItemEndpointMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModLoader;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;
import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.OUT;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MACHINE;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.STEAM_BOILER_RECIPES;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ALL_TIERS;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote GTMachines
 */
public class GTMachines {

    static {
        REGISTRATE.creativeModeTab(() -> MACHINE);
        GTRegistries.MACHINES.unfreeze();
    }

    //////////////////////////////////////
    // ****** Steam Machine ******//
    //////////////////////////////////////
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_SOLID_BOILER = registerSteamMachines(
            "steam_solid_boiler",
            SteamSolidBoilerMachine::new,
            (pressure, builder) -> builder.rotationState(RotationState.ALL)
                    .recipeType(STEAM_BOILER_RECIPES)
                    .recipeModifier(SteamBoilerMachine::recipeModifier)
                    .workableSteamHullRenderer(pressure, GTCEu.id("block/generators/boiler/coal"))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (pressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpSolidBoilerBaseOutput :
                                    ConfigHolder.INSTANCE.machines.smallBoilers.solidBoilerBaseOutput) *
                                    FluidType.BUCKET_VOLUME / 20000))
                    .register());

    public static final Pair<MachineDefinition, MachineDefinition> STEAM_LIQUID_BOILER = registerSteamMachines(
            "steam_liquid_boiler",
            SteamLiquidBoilerMachine::new,
            (pressure, builder) -> builder.rotationState(RotationState.ALL)
                    .recipeType(STEAM_BOILER_RECIPES)
                    .recipeModifier(SteamBoilerMachine::recipeModifier)
                    .workableSteamHullRenderer(pressure, GTCEu.id("block/generators/boiler/lava"))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (pressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpLiquidBoilerBaseOutput :
                                    ConfigHolder.INSTANCE.machines.smallBoilers.liquidBoilerBaseOutput) *
                                    FluidType.BUCKET_VOLUME / 20000))
                    .register());

    public static final Pair<MachineDefinition, MachineDefinition> STEAM_SOLAR_BOILER = registerSteamMachines(
            "steam_solar_boiler",
            SteamSolarBoiler::new,
            (pressure, builder) -> builder.rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(STEAM_BOILER_RECIPES)
                    .recipeModifier(SteamBoilerMachine::recipeModifier)
                    .workableSteamHullRenderer(pressure, GTCEu.id("block/generators/boiler/solar"))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid",
                            (pressure ? ConfigHolder.INSTANCE.machines.smallBoilers.hpSolarBoilerBaseOutput :
                                    ConfigHolder.INSTANCE.machines.smallBoilers.solarBoilerBaseOutput) *
                                    FluidType.BUCKET_VOLUME / 20000))
                    .register());

    public static final Pair<MachineDefinition, MachineDefinition> STEAM_EXTRACTOR = registerSimpleSteamMachines(
            "extractor", GTRecipeTypes.EXTRACTOR_RECIPES);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_MACERATOR = registerSteamMachines(
            "steam_macerator", SimpleSteamMachine::new, (pressure, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                    .recipeModifier(SimpleSteamMachine::recipeModifier)
                    .addOutputLimit(ItemRecipeCapability.CAP, 1)
                    .renderer(() -> new WorkableSteamMachineRenderer(pressure, GTCEu.id("block/machines/macerator")))
                    .register());
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_COMPRESSOR = registerSimpleSteamMachines(
            "compressor", GTRecipeTypes.COMPRESSOR_RECIPES);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_HAMMER = registerSimpleSteamMachines(
            "forge_hammer", GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_FURNACE = registerSimpleSteamMachines(
            "furnace", GTRecipeTypes.FURNACE_RECIPES);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_ALLOY_SMELTER = registerSimpleSteamMachines(
            "alloy_smelter", GTRecipeTypes.ALLOY_SMELTER_RECIPES);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_ROCK_CRUSHER = registerSimpleSteamMachines(
            "rock_crusher", GTRecipeTypes.ROCK_BREAKER_RECIPES);
    public static final Pair<MachineDefinition, MachineDefinition> STEAM_MINER = registerSteamMachines(
            "steam_miner",
            (holder, isHP) -> isHP ? new SteamMinerMachine(holder, true, 240, 6, 0, 32) :
                    new SteamMinerMachine(holder, false, 320, 4, 0, 16),
            (isHP, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(DUMMY_RECIPES)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.uses_per_tick_steam", isHP ? 32 : 16)
                            .append(ChatFormatting.GRAY + ", ")
                            .append(Component.translatable("gtceu.machine.miner.per_block",
                                    isHP ? 240 / 20 : 280 / 20)))
                    .tooltipBuilder((item, tooltip) -> {
                        int maxArea = IMiner.getWorkingArea(isHP ? 6 : 4);
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.working_area", maxArea, maxArea));
                    })
                    .renderer(() -> new SteamMinerRenderer(isHP,
                            isHP ? GTCEu.id("block/machines/high_pressure_steam_miner") :
                                    GTCEu.id("block/machines/steam_miner")))
                    .register());

    //////////////////////////////////////
    // *** SimpleTieredMachine ***//
    //////////////////////////////////////
    public static final MachineDefinition[] HULL = GTMachineUtils.registerTieredMachines("machine_hull",
            HullMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .overlayTieredHullRenderer("hull")
                    .abilities(PartAbility.PASSTHROUGH_HATCH)
                    .langValue("%s §fMachine Hull".formatted(VNF[tier]))
                    .tooltips(Component.translatable("gtceu.machine.hull.tooltip"))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ELECTRIC_FURNACE = registerSimpleMachines("electric_furnace",
            GTRecipeTypes.FURNACE_RECIPES);
    public static final MachineDefinition[] ALLOY_SMELTER = registerSimpleMachines("alloy_smelter",
            GTRecipeTypes.ALLOY_SMELTER_RECIPES);
    public static final MachineDefinition[] ARC_FURNACE = registerSimpleMachines("arc_furnace",
            GTRecipeTypes.ARC_FURNACE_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] ASSEMBLER = registerSimpleMachines("assembler",
            GTRecipeTypes.ASSEMBLER_RECIPES, hvCappedTankSizeFunction, true);
    public static final MachineDefinition[] AUTOCLAVE = registerSimpleMachines("autoclave",
            GTRecipeTypes.AUTOCLAVE_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] BENDER = registerSimpleMachines("bender", GTRecipeTypes.BENDER_RECIPES);
    public static final MachineDefinition[] BREWERY = registerSimpleMachines("brewery", GTRecipeTypes.BREWING_RECIPES,
            hvCappedTankSizeFunction);
    public static final MachineDefinition[] CANNER = registerSimpleMachines("canner", GTRecipeTypes.CANNER_RECIPES);
    public static final MachineDefinition[] CENTRIFUGE = registerSimpleMachines("centrifuge",
            GTRecipeTypes.CENTRIFUGE_RECIPES, largeTankSizeFunction);
    public static final MachineDefinition[] CHEMICAL_BATH = registerSimpleMachines("chemical_bath",
            GTRecipeTypes.CHEMICAL_BATH_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] CHEMICAL_REACTOR = registerSimpleMachines("chemical_reactor",
            GTRecipeTypes.CHEMICAL_RECIPES, tier -> 16 * FluidType.BUCKET_VOLUME, true);
    public static final MachineDefinition[] COMPRESSOR = registerSimpleMachines("compressor",
            GTRecipeTypes.COMPRESSOR_RECIPES);
    public static final MachineDefinition[] CUTTER = registerSimpleMachines("cutter", GTRecipeTypes.CUTTER_RECIPES);
    public static final MachineDefinition[] DISTILLERY = registerSimpleMachines("distillery",
            GTRecipeTypes.DISTILLERY_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] ELECTROLYZER = registerSimpleMachines("electrolyzer",
            GTRecipeTypes.ELECTROLYZER_RECIPES, largeTankSizeFunction);
    public static final MachineDefinition[] ELECTROMAGNETIC_SEPARATOR = registerSimpleMachines(
            "electromagnetic_separator", GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES);
    public static final MachineDefinition[] EXTRACTOR = registerSimpleMachines("extractor",
            GTRecipeTypes.EXTRACTOR_RECIPES);
    public static final MachineDefinition[] EXTRUDER = registerSimpleMachines("extruder",
            GTRecipeTypes.EXTRUDER_RECIPES);
    public static final MachineDefinition[] FERMENTER = registerSimpleMachines("fermenter",
            GTRecipeTypes.FERMENTING_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] FLUID_HEATER = registerSimpleMachines("fluid_heater",
            GTRecipeTypes.FLUID_HEATER_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] FLUID_SOLIDIFIER = registerSimpleMachines("fluid_solidifier",
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES, hvCappedTankSizeFunction);
    public static final MachineDefinition[] FORGE_HAMMER = registerSimpleMachines("forge_hammer",
            GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public static final MachineDefinition[] FORMING_PRESS = registerSimpleMachines("forming_press",
            GTRecipeTypes.FORMING_PRESS_RECIPES);
    public static final MachineDefinition[] LATHE = registerSimpleMachines("lathe", GTRecipeTypes.LATHE_RECIPES);
    public static final MachineDefinition[] SCANNER = registerSimpleMachines("scanner", GTRecipeTypes.SCANNER_RECIPES);
    public static final MachineDefinition[] MIXER = registerSimpleMachines("mixer", GTRecipeTypes.MIXER_RECIPES,
            hvCappedTankSizeFunction);
    public static final MachineDefinition[] ORE_WASHER = registerSimpleMachines("ore_washer",
            GTRecipeTypes.ORE_WASHER_RECIPES);
    public static final MachineDefinition[] PACKER = registerSimpleMachines("packer", GTRecipeTypes.PACKER_RECIPES);
    public static final MachineDefinition[] POLARIZER = registerSimpleMachines("polarizer",
            GTRecipeTypes.POLARIZER_RECIPES);
    public static final MachineDefinition[] LASER_ENGRAVER = registerSimpleMachines("laser_engraver",
            GTRecipeTypes.LASER_ENGRAVER_RECIPES, defaultTankSizeFunction, true);
    public static final MachineDefinition[] SIFTER = registerSimpleMachines("sifter", GTRecipeTypes.SIFTER_RECIPES);
    public static final MachineDefinition[] THERMAL_CENTRIFUGE = registerSimpleMachines("thermal_centrifuge",
            GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES);
    public static final MachineDefinition[] WIREMILL = registerSimpleMachines("wiremill",
            GTRecipeTypes.WIREMILL_RECIPES);
    public static final MachineDefinition[] CIRCUIT_ASSEMBLER = registerSimpleMachines("circuit_assembler",
            GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES, hvCappedTankSizeFunction, true);
    public static final MachineDefinition[] MACERATOR = registerTieredMachines("macerator",
            (holder, tier) -> new SimpleTieredMachine(holder, tier, defaultTankSizeFunction), (tier, builder) -> builder
                    .langValue("%s Macerator %s".formatted(VLVH[tier], VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("macerator"),
                            GTRecipeTypes.MACERATOR_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                    .addOutputLimit(ItemRecipeCapability.CAP, switch (tier) {
                        case 1, 2 -> 1;
                        case 3 -> 3;
                        default -> 4;
                    })
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullRenderer(GTCEu.id("block/machines/macerator"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTRecipeTypes.MACERATOR_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .register(),
            ELECTRIC_TIERS);
    public static final MachineDefinition[] GAS_COLLECTOR = registerSimpleMachines("gas_collector",
            GTRecipeTypes.GAS_COLLECTOR_RECIPES, largeTankSizeFunction, true);
    public static final MachineDefinition[] ROCK_CRUSHER = registerTieredMachines("rock_crusher",
            RockCrusherMachine::new, (tier, builder) -> builder
                    .langValue("%s Rock Crusher %s".formatted(VLVH[tier], VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("rock_crusher"),
                            GTRecipeTypes.ROCK_BREAKER_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.ROCK_BREAKER_RECIPES)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullRenderer(GTCEu.id("block/machines/rock_crusher"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTRecipeTypes.ROCK_BREAKER_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .tooltips(explosion())
                    .register(),
            ELECTRIC_TIERS);
    public static final MachineDefinition[] AIR_SCRUBBER = registerTieredMachines("air_scrubber",
            AirScrubberMachine::new, (tier, builder) -> builder
                    .langValue("%s Air Scrubber %s".formatted(VLVH[tier], VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("air_scrubber"),
                            GTRecipeTypes.AIR_SCRUBBER_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.AIR_SCRUBBER_RECIPES)
                    .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
                    .workableTieredHullRenderer(GTCEu.id("block/machines/air_scrubber"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTRecipeTypes.AIR_SCRUBBER_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .tooltips(explosion())
                    .register(),
            LOW_TIERS);

    //////////////////////////////////////
    // **** Simple Generator ****//
    //////////////////////////////////////
    public static final MachineDefinition[] COMBUSTION = registerSimpleGenerator("combustion",
            GTRecipeTypes.COMBUSTION_GENERATOR_FUELS, genericGeneratorTankSizeFunction, 0.1f, GTValues.LV, GTValues.MV,
            GTValues.HV);
    public static final MachineDefinition[] STEAM_TURBINE = registerSimpleGenerator("steam_turbine",
            GTRecipeTypes.STEAM_TURBINE_FUELS, steamGeneratorTankSizeFunction, 0.0f, GTValues.LV, GTValues.MV,
            GTValues.HV);
    public static final MachineDefinition[] GAS_TURBINE = registerSimpleGenerator("gas_turbine",
            GTRecipeTypes.GAS_TURBINE_FUELS, genericGeneratorTankSizeFunction, 0.1f, GTValues.LV, GTValues.MV,
            GTValues.HV);

    //////////////////////////////////////
    // ******** Electric ********//
    //////////////////////////////////////
    public static final MachineDefinition[] TRANSFORMER = registerTransformerMachines("", 1);
    public static final MachineDefinition[] HI_AMP_TRANSFORMER_2A = registerTransformerMachines("Hi-Amp (2x) ", 2);
    public static final MachineDefinition[] HI_AMP_TRANSFORMER_4A = registerTransformerMachines("Hi-Amp (4x) ", 4);
    public static final MachineDefinition[] POWER_TRANSFORMER = registerTransformerMachines("Power ", 16);

    public static final MachineDefinition[] ENERGY_CONVERTER_1A = registerConverter(1);
    public static final MachineDefinition[] ENERGY_CONVERTER_4A = registerConverter(4);
    public static final MachineDefinition[] ENERGY_CONVERTER_8A = registerConverter(8);
    public static final MachineDefinition[] ENERGY_CONVERTER_16A = registerConverter(16);

    public static final MachineDefinition LONG_DIST_ITEM_ENDPOINT = REGISTRATE
            .machine("long_distance_item_pipeline_endpoint", LDItemEndpointMachine::new)
            .langValue("Long Distance Item Pipeline Endpoint")
            .rotationState(RotationState.ALL)
            .tier(LV)
            .tieredHullRenderer(GTCEu.id("block/machine/ld_item_endpoint_machine"))
            .tooltips(LangHandler.getMultiLang("gtceu.machine.endpoint.tooltip").toArray(Component[]::new))
            .tooltipBuilder((stack, tooltip) -> {
                if (ConfigHolder.INSTANCE.machines.ldItemPipeMinDistance > 0) {
                    tooltip.add(Component.translatable("gtceu.machine.endpoint.tooltip.min_length",
                            ConfigHolder.INSTANCE.machines.ldItemPipeMinDistance));
                }
            })
            .register();

    public static final MachineDefinition LONG_DIST_FLUID_ENDPOINT = REGISTRATE
            .machine("long_distance_fluid_pipeline_endpoint", LDFluidEndpointMachine::new)
            .langValue("Long Distance Fluid Pipeline Endpoint")
            .rotationState(RotationState.ALL)
            .tier(LV)
            .tieredHullRenderer(GTCEu.id("block/machine/ld_fluid_endpoint_machine"))
            .tooltips(Component.translatable("gtceu.machine.endpoint.tooltip.0"),
                    Component.translatable("gtceu.machine.endpoint.tooltip.1"),
                    Component.translatable("gtceu.machine.endpoint.tooltip.2"))
            .tooltipBuilder((stack, tooltip) -> {
                if (ConfigHolder.INSTANCE.machines.ldFluidPipeMinDistance > 0) {
                    tooltip.add(Component.translatable("gtceu.machine.endpoint.tooltip.min_length",
                            ConfigHolder.INSTANCE.machines.ldItemPipeMinDistance));
                }
            })
            .register();

    public static final MachineDefinition[] BATTERY_BUFFER_4 = registerBatteryBuffer(4);

    public static final MachineDefinition[] BATTERY_BUFFER_8 = registerBatteryBuffer(8);

    public static final MachineDefinition[] BATTERY_BUFFER_16 = registerBatteryBuffer(16);

    public static final MachineDefinition[] CHARGER_4 = registerCharger(4);

    public static final MachineDefinition[] PUMP = registerTieredMachines("pump", PumpMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .tieredHullRenderer(GTCEu.id("block/machine/pump_machine"))
                    .langValue("%s Pump %s".formatted(VLVH[tier], VLVT[tier]))
                    .tooltips(Component.translatable("gtceu.machine.pump.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]),
                                    GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64)),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                    FormattingUtil.formatNumbers(16 * FluidType.BUCKET_VOLUME * Math.max(1, tier))),
                            Component.translatable("gtceu.universal.tooltip.working_area",
                                    PumpMachine.getMaxPumpRadius(tier) * 2,
                                    PumpMachine.getMaxPumpRadius(tier) * 2))
                    .register(),
            LV, MV, HV, EV);

    public static final MachineDefinition[] FISHER = registerTieredMachines("fisher", FisherMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .editableUI(FisherMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("fisher"), (tier + 1) * (tier + 1)))
                    .tieredHullRenderer(GTCEu.id("block/machine/fisher_machine"))
                    .langValue("%s Fisher %s".formatted(VLVH[tier], VLVT[tier]))
                    .tooltips(Component.translatable("gtceu.machine.fisher.tooltip"),
                            Component.translatable("gtceu.machine.fisher.speed", FisherMachine.calcMaxProgress(tier)),
                            Component.translatable("gtceu.machine.fisher.requirement", FisherMachine.WATER_CHECK_SIZE,
                                    FisherMachine.WATER_CHECK_SIZE),
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]),
                                    GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64)))
                    .register(),
            LV, MV, HV, EV, IV, LuV);

    public static final MachineDefinition[] BLOCK_BREAKER = registerTieredMachines("block_breaker",
            BlockBreakerMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .editableUI(BlockBreakerMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("block_breaker"),
                            (tier + 1) * (tier + 1)))
                    .tieredHullRenderer(GTCEu.id("block/machine/block_breaker_machine"))
                    .langValue("%s Block Breaker %s".formatted(VLVH[tier], VLVT[tier]))
                    .tooltips(Component.translatable("gtceu.machine.block_breaker.tooltip"),
                            Component.translatable("gtceu.machine.block_breaker.speed_bonus",
                                    (int) (BlockBreakerMachine.getEfficiencyMultiplier(tier) * 100)),
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]),
                                    GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64)))
                    .register(),
            LV, MV, HV, EV);

    public static final MachineDefinition[] MINER = registerTieredMachines("miner",
            (holder, tier) -> new MinerMachine(holder, tier, ConfigHolder.INSTANCE.machines.minerSpeed / (tier * 2),
                    tier * 8, tier),
            (tier, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .langValue("%s Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .editableUI(MinerMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("miner"), (tier + 1) * (tier + 1)))
                    .renderer(() -> new MinerRenderer(tier, GTCEu.id("block/machines/miner")))
                    .tooltipBuilder((stack, tooltip) -> {
                        int maxArea = IMiner.getWorkingArea(tier * 8);
                        long energyPerTick = GTValues.V[tier - 1];
                        int tickSpeed = ConfigHolder.INSTANCE.machines.minerSpeed / (tier * 2);
                        tooltip.add(Component.translatable("gtceu.machine.miner.tooltip", maxArea, maxArea));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.uses_per_tick", energyPerTick)
                                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable("gtceu.machine.miner.per_block", tickSpeed / 20)));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(GTValues.V[tier]),
                                GTValues.VNF[tier]));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(GTValues.V[tier] * 64L)));

                        tooltip.add(
                                Component.translatable("gtceu.universal.tooltip.working_area_max", maxArea, maxArea));
                    })
                    .register(),
            LV, MV, HV);

    public static final MachineDefinition[] WORLD_ACCELERATOR = registerTieredMachines("world_accelerator",
            WorldAcceleratorMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.NONE)
                    .langValue("%s World Accelerator %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .renderer(() -> new WorldAcceleratorRenderer(tier, GTCEu.id("block/machines/world_accelerator_te"),
                            GTCEu.id("block/machines/world_accelerator")))
                    .tooltipBuilder((stack, tooltip) -> {
                        int randTickWorkingArea = 3 + (tier - 1) * 2;
                        tooltip.add(Component.translatable("gtceu.machine.world_accelerator.description"));

                        tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(GTValues.V[tier]),
                                GTValues.VNF[tier]));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(GTValues.V[tier] * 64L)));

                        tooltip.add(Component.translatable("gtceu.machine.world_accelerator.working_area"));
                        tooltip.add(Component.translatable("gtceu.machine.world_accelerator.working_area_tile"));
                        tooltip.add(Component.translatable("gtceu.machine.world_accelerator.working_area_random",
                                randTickWorkingArea, randTickWorkingArea));
                    })
                    .register(),
            LV, MV, HV, EV, IV, LuV, ZPM, UV);

    public static final MachineDefinition[] ITEM_COLLECTOR = registerTieredMachines("item_collector",
            ItemCollectorMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.NONE)
                    .langValue("%s Item Collector %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .editableUI(ItemCollectorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("item_collector"),
                            ItemCollectorMachine.getINVENTORY_SIZES()[tier]))
                    .renderer(() -> new WorkableTieredHullMachineRenderer(tier,
                            GTCEu.id("block/machines/item_collector")))
                    .tooltips(
                            Component.translatable("gtceu.machine.item_collector.tooltip"),
                            Component.translatable("gtceu.machine.item_collector.gui.collect_range",
                                    IntMath.pow(2, tier + 2), IntMath.pow(2, tier + 2)),
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]),
                                    GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64L)))
                    .register(),
            LV, MV, HV, EV);

    //////////////////////////////////////
    // ********* Storage *********//
    //////////////////////////////////////

    public static final MachineDefinition[] BUFFER = registerTieredMachines("buffer",
            BufferMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Buffer %s".formatted(VLVH[tier], VLVT[tier]))
                    .rotationState(RotationState.NONE)
                    .tieredHullRenderer(GTCEu.id("block/machine/buffer"))
                    .tooltips(
                            Component.translatable("gtceu.machine.buffer.tooltip"),
                            Component.translatable(
                                    "gtceu.universal.tooltip.item_storage_capacity",
                                    BufferMachine.getInventorySize(tier)),
                            Component.translatable(
                                    "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                    BufferMachine.getTankSize(tier), BufferMachine.TANK_SIZE))
                    .register(),
            LV, MV, HV);

    public static final BiConsumer<ItemStack, List<Component>> CREATIVE_TOOLTIPS = (stack, list) -> list.add(
            Component.translatable("gtceu.creative_tooltip.1")
                    .append(Component.translatable("gtceu.creative_tooltip.2")
                            .withStyle(TooltipHelper.RAINBOW_HSL_SLOW))
                    .append(Component.translatable("gtceu.creative_tooltip.3")));

    public static final MachineDefinition CREATIVE_ENERGY = REGISTRATE
            .machine("creative_energy", CreativeEnergyContainerMachine::new)
            .rotationState(RotationState.NONE)
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .register();

    public static final MachineDefinition CREATIVE_COMPUTATION_PROVIDER = REGISTRATE
            .machine("creative_computation_provider", CreativeComputationProviderMachine::new)
            .rotationState(RotationState.NONE)
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .register();

    public static final MachineDefinition CREATIVE_FLUID = REGISTRATE
            .machine("creative_tank", CreativeTankMachine::new)
            .rotationState(RotationState.ALL)
            .tooltipBuilder((stack, list) -> {
                CREATIVE_TOOLTIPS.accept(stack, list);
                if (stack.hasTag()) {
                    FluidStack f = FluidStack.loadFluidStackFromNBT(stack.getOrCreateTagElement("stored"));
                    int perCycle = stack.getOrCreateTag().getInt("mBPerCycle");
                    list.add(1, Component.translatable("gtceu.universal.tooltip.fluid_stored", f.getDisplayName(),
                            FormattingUtil.formatNumbers(perCycle)));
                }
            })
            .renderer(() -> new QuantumTankRenderer(MAX, GTCEu.id("block/machine/creative_tank")))
            .hasTESR(true)
            .register();

    public static final MachineDefinition CREATIVE_ITEM = REGISTRATE
            .machine("creative_chest", CreativeChestMachine::new)
            .rotationState(RotationState.ALL)
            .tooltipBuilder((stack, list) -> {
                CREATIVE_TOOLTIPS.accept(stack, list);
                if (stack.hasTag()) {
                    ItemStack i = ItemStack.of(stack.getOrCreateTagElement("stored"));
                    int perCycle = stack.getOrCreateTag().getInt("itemsPerCycle");
                    list.add(1, Component.translatable("gtceu.universal.tooltip.item_stored", i.getHoverName(),
                            FormattingUtil.formatNumbers(perCycle)));
                }
            })
            .renderer(() -> new QuantumChestRenderer(MAX, GTCEu.id("block/machine/creative_chest")))
            .hasTESR(true)
            .register();

    public static BiConsumer<ItemStack, List<Component>> CHEST_TOOLTIPS = (stack, list) -> {
        if (stack.hasTag()) {
            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            long storedAmount = stack.getOrCreateTag().getLong("storedAmount");
            list.add(1, Component.translatable("gtceu.universal.tooltip.item_stored", itemStack.getHoverName(),
                    FormattingUtil.formatNumbers(storedAmount)));
        }
    };

    public static final MachineDefinition[] SUPER_CHEST = registerTieredMachines("super_chest",
            (holder, tier) -> new QuantumChestMachine(holder, tier, 4_000_000 * (long) Math.pow(2, tier - 1)),
            (tier, builder) -> builder
                    .langValue("Super Chest " + LVT[tier])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumChestRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(CHEST_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_total",
                                    FormattingUtil.formatNumbers(4_000_000 * (long) Math.pow(2, tier - 1))))
                    .register(),
            LOW_TIERS);

    public static final MachineDefinition[] QUANTUM_CHEST = registerTieredMachines("quantum_chest",
            (holder, tier) -> new QuantumChestMachine(holder, tier,
                    tier == MAX ? Long.MAX_VALUE : 4_000_000 * (long) Math.pow(2, tier - 1)),
            (tier, builder) -> builder
                    .langValue("Quantum Chest " + LVT[tier])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumChestRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(CHEST_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_total",
                                    FormattingUtil.formatNumbers(4_000_000 * (long) Math.pow(2, tier - 1))))
                    .register(),
            HIGH_TIERS);

    public static final MachineDefinition[] SUPER_TANK = registerTieredMachines("super_tank",
            (holder, tier) -> new QuantumTankMachine(holder, tier,
                    4000 * FluidType.BUCKET_VOLUME * (long) Math.pow(2, tier - 1)),
            (tier, builder) -> builder
                    .langValue("Super Tank " + LVT[tier])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumTankRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(TANK_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                    FormattingUtil.formatNumbers(4_000_000 * (long) Math.pow(2, tier - 1))))
                    .register(),
            LOW_TIERS);

    public static final MachineDefinition[] QUANTUM_TANK = registerTieredMachines("quantum_tank",
            (holder, tier) -> new QuantumTankMachine(holder, tier,
                    4000 * FluidType.BUCKET_VOLUME * (long) Math.pow(2, tier - 1)),
            (tier, builder) -> builder
                    .langValue("Quantum Tank " + LVT[tier])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumTankRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(TANK_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                    FormattingUtil.formatNumbers(4_000_000 * (long) Math.pow(2, tier - 1))))
                    .register(),
            HIGH_TIERS);

    public static MachineDefinition WOODEN_CRATE = registerCrate(GTMaterials.Wood, 27, "Wooden Crate");
    public static MachineDefinition BRONZE_CRATE = registerCrate(GTMaterials.Bronze, 54, "Bronze Crate");
    public static MachineDefinition STEEL_CRATE = registerCrate(GTMaterials.Steel, 72, "Steel Crate");
    public static MachineDefinition ALUMINIUM_CRATE = registerCrate(GTMaterials.Aluminium, 90, "Aluminium Crate");
    public static MachineDefinition STAINLESS_STEEL_CRATE = registerCrate(GTMaterials.StainlessSteel, 108,
            "Stainless Steel Crate");
    public static MachineDefinition TITANIUM_CRATE = registerCrate(GTMaterials.Titanium, 126, "Titanium Crate");
    public static MachineDefinition TUNGSTENSTEEL_CRATE = registerCrate(GTMaterials.TungstenSteel, 144,
            "Tungstensteel Crate");

    public static MachineDefinition WOODEN_DRUM = registerDrum(GTMaterials.Wood, (16 * FluidType.BUCKET_VOLUME),
            "Wooden Barrel");
    public static MachineDefinition BRONZE_DRUM = registerDrum(GTMaterials.Bronze, (32 * FluidType.BUCKET_VOLUME),
            "Bronze Drum");
    public static MachineDefinition STEEL_DRUM = registerDrum(GTMaterials.Steel, (64 * FluidType.BUCKET_VOLUME),
            "Steel Drum");
    public static MachineDefinition ALUMINIUM_DRUM = registerDrum(GTMaterials.Aluminium,
            (128 * FluidType.BUCKET_VOLUME), "Aluminium Drum");
    public static MachineDefinition STAINLESS_STEEL_DRUM = registerDrum(GTMaterials.StainlessSteel,
            (256 * FluidType.BUCKET_VOLUME), "Stainless Steel Drum");
    public static MachineDefinition GOLD_DRUM = registerDrum(GTMaterials.Gold, (32 * FluidType.BUCKET_VOLUME),
            "Gold Drum");
    public static MachineDefinition TITANIUM_DRUM = registerDrum(GTMaterials.Titanium,
            (512 * FluidType.BUCKET_VOLUME), "Titanium Drum");
    public static MachineDefinition TUNGSTENSTEEL_DRUM = registerDrum(GTMaterials.TungstenSteel,
            (1024 * FluidType.BUCKET_VOLUME), "Tungstensteel Drum");

    //////////////////////////////////////
    // ********** Part **********//
    //////////////////////////////////////
    public static final MachineDefinition[] ITEM_IMPORT_BUS = registerTieredMachines("input_bus",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.IMPORT_ITEMS)
                    .overlayTieredHullRenderer("item_bus.import")
                    .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ITEM_EXPORT_BUS = registerTieredMachines("output_bus",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Output Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.EXPORT_ITEMS)
                    .overlayTieredHullRenderer("item_bus.export")
                    .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .register(),
            ALL_TIERS);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH = registerFluidHatches(
            "input_hatch", "Input Hatch", "fluid_hatch.import", "fluid_hatch.import",
            IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1,
            ALL_TIERS, PartAbility.IMPORT_FLUIDS,
            PartAbility.IMPORT_FLUIDS_1X);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH_4X = registerFluidHatches(
            "input_hatch_4x", "Quadruple Input Hatch", "fluid_hatch.import_4x", "fluid_hatch.import",
            IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
            MULTI_HATCH_TIERS, PartAbility.IMPORT_FLUIDS,
            PartAbility.IMPORT_FLUIDS_4X);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH_9X = registerFluidHatches(
            "input_hatch_9x", "Nonuple Input Hatch", "fluid_hatch.import_9x", "fluid_hatch.import",
            IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_9X, 9,
            MULTI_HATCH_TIERS, PartAbility.IMPORT_FLUIDS,
            PartAbility.IMPORT_FLUIDS_9X);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH = registerFluidHatches(
            "output_hatch", "Output Hatch", "fluid_hatch.export", "fluid_hatch.export",
            OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1,
            ALL_TIERS, PartAbility.EXPORT_FLUIDS,
            PartAbility.EXPORT_FLUIDS_1X);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH_4X = registerFluidHatches(
            "output_hatch_4x", "Quadruple Output Hatch", "fluid_hatch.export_4x", "fluid_hatch.export",
            OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
            MULTI_HATCH_TIERS, PartAbility.EXPORT_FLUIDS,
            PartAbility.EXPORT_FLUIDS_4X);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH_9X = registerFluidHatches(
            "output_hatch_9x", "Nonuple Output Hatch", "fluid_hatch.export_9x", "fluid_hatch.export",
            OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_9X, 9,
            MULTI_HATCH_TIERS, PartAbility.EXPORT_FLUIDS,
            PartAbility.EXPORT_FLUIDS_9X);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH = registerTieredMachines("energy_input_hatch",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IN, 2),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 2),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 2))),
                            Component.translatable("gtceu.machine.energy_hatch.input.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH = registerTieredMachines("energy_output_hatch",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, OUT, 2),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 2),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 2))),
                            Component.translatable("gtceu.machine.energy_hatch.output.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_4A = registerTieredMachines("energy_input_hatch_4a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IN, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 4A Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 4),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 4))),
                            Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input_4a")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_4A = registerTieredMachines("energy_output_hatch_4a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, OUT, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 4A Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 4),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 4))),
                            Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output_4a")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_16A = registerTieredMachines("energy_input_hatch_16a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IN, 16),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 16A Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 16),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 16))),
                            Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input_16a")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_16A = registerTieredMachines("energy_output_hatch_16a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, OUT, 16),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 16A Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 16),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 16))),
                            Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output_16a")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] SUBSTATION_ENERGY_INPUT_HATCH = registerTieredMachines(
            "substation_input_hatch_64a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IN, 64),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 64A Substation Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.SUBSTATION_INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_in",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 64),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 64))),
                            Component.translatable("gtceu.machine.substation_hatch.input.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input_64a")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] SUBSTATION_ENERGY_OUTPUT_HATCH = registerTieredMachines(
            "substation_output_hatch_64a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, OUT, 64),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 64A Substation Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.SUBSTATION_OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                            FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 64),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil
                                            .formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, 64))),
                            Component.translatable("gtceu.machine.substation_hatch.output.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output_64a")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] MUFFLER_HATCH = registerTieredMachines("muffler_hatch",
            MufflerPartMachine::new,
            (tier, builder) -> builder
                    .langValue("Muffler Hatch " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.MUFFLER)
                    .overlayTieredHullRenderer("muffler_hatch")
                    .tooltips(LangHandler.getFromMultiLang("gtceu.machine.muffler_hatch.tooltip", 0),
                            Component.translatable("gtceu.muffler.recovery_tooltip", Math.max(1, tier * 10)),
                            Component.translatable("gtceu.universal.enabled"),
                            LangHandler.getFromMultiLang("gtceu.machine.muffler_hatch.tooltip", 1)
                                    .withStyle(ChatFormatting.DARK_RED))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition STEAM_IMPORT_BUS = REGISTRATE
            .machine("steam_input_bus", holder -> new SteamItemBusPartMachine(holder, IN))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM_IMPORT_ITEMS)
            .overlaySteamHullRenderer("item_bus.import")
            .langValue("Steam Input Bus")
            .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                    Component.translatable("gtceu.machine.steam_bus.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.item_storage_capacity", 4))
            .register();

    public static final MachineDefinition STEAM_EXPORT_BUS = REGISTRATE
            .machine("steam_output_bus", holder -> new SteamItemBusPartMachine(holder, OUT))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM_EXPORT_ITEMS)
            .overlaySteamHullRenderer("item_bus.export")
            .langValue("Steam Output Bus")
            .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                    Component.translatable("gtceu.machine.steam_bus.tooltip"),
                    Component.translatable("gtceu.universal.tooltip.item_storage_capacity", 4))
            .register();

    public static final MachineDefinition STEAM_HATCH = REGISTRATE
            .machine("steam_input_hatch", SteamHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM)
            .overlaySteamHullRenderer("steam_hatch")
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                    SteamHatchPartMachine.INITIAL_TANK_CAPACITY),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
            .register();

    public static final MachineDefinition COKE_OVEN_HATCH = REGISTRATE.machine("coke_oven_hatch", CokeOvenHatch::new)
            .rotationState(RotationState.ALL)
            .modelRenderer(() -> GTCEu.id("block/machine/part/coke_oven_hatch"))
            .register();

    public static final MachineDefinition PUMP_HATCH = REGISTRATE.machine("pump_hatch", PumpHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.PUMP_FLUID_HATCH)
            .renderer(PumpHatchPartRenderer::new)
            .register();

    public static final MachineDefinition MAINTENANCE_HATCH = REGISTRATE
            .machine("maintenance_hatch", (blockEntity) -> new MaintenanceHatchPartMachine(blockEntity, false))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"))
            .renderer(() -> new MaintenanceHatchPartRenderer(1, GTCEu.id("block/machine/part/maintenance")))
            .register();

    public static final MachineDefinition CONFIGURABLE_MAINTENANCE_HATCH = REGISTRATE
            .machine("configurable_maintenance_hatch",
                    (blockEntity) -> new MaintenanceHatchPartMachine(blockEntity, true))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"))
            .renderer(
                    () -> new MaintenanceHatchPartRenderer(3, GTCEu.id("block/machine/part/maintenance.configurable")))
            .register();

    public static final MachineDefinition CLEANING_MAINTENANCE_HATCH = REGISTRATE
            .machine("cleaning_maintenance_hatch",
                    holder -> new CleaningMaintenanceHatchPartMachine(holder, CleanroomType.CLEANROOM))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"),
                    Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.0"),
                    Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.1"))
            .tooltipBuilder((stack, tooltips) -> {
                tooltips.add(Component.literal("  ").append(Component
                        .translatable(CleanroomType.CLEANROOM.getTranslationKey()).withStyle(ChatFormatting.GREEN)));
            })
            .renderer(() -> new MaintenanceHatchPartRenderer(3, GTCEu.id("block/machine/part/maintenance.cleaning")))
            .register();

    public static final MachineDefinition AUTO_MAINTENANCE_HATCH = REGISTRATE
            .machine("auto_maintenance_hatch", AutoMaintenanceHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"))
            .renderer(() -> new MaintenanceHatchPartRenderer(3, GTCEu.id("block/machine/part/maintenance.full_auto")))
            .register();

    public static final MachineDefinition[] ITEM_PASSTHROUGH_HATCH = registerTieredMachines("item_passthrough_hatch",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.BOTH),
            (tier, builder) -> builder
                    .langValue("%s Item Passthrough Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.PASSTHROUGH_HATCH)
                    .overlayTieredHullRenderer("item_passthrough_hatch")
                    .tooltips(
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))),
                            Component.translatable("gtceu.universal.enabled"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] FLUID_PASSTHROUGH_HATCH = registerTieredMachines("fluid_passthrough_hatch",
            (holder, tier) -> new FluidHatchPartMachine(holder, tier, IO.BOTH,
                    FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1),
            (tier, builder) -> builder
                    .langValue("%s Fluid Passthrough Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.PASSTHROUGH_HATCH)
                    .overlayTieredHullRenderer("fluid_passthrough_hatch")
                    .tooltips(
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult", tier + 1,
                                    16 * FluidType.BUCKET_VOLUME),
                            Component.translatable("gtceu.universal.enabled"))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition RESERVOIR_HATCH = REGISTRATE
            .machine("reservoir_hatch", ReservoirHatchPartMachine::new)
            .langValue("Reservoir Hatch")
            .tier(EV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .tooltips(
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            FormattingUtil.formatNumbers(ReservoirHatchPartMachine.FLUID_AMOUNT)),
                    Component.translatable("gtceu.universal.enabled"))
            .overlayTieredHullRenderer("reservoir_hatch")
            .register();

    public static final MachineDefinition[] DUAL_IMPORT_HATCH = registerTieredMachines(
            "dual_input_hatch",
            (holder, tier) -> new DualHatchPartMachine(holder, tier, IN),
            (tier, builder) -> builder
                    .langValue("%s Dual Input Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(DUAL_INPUT_HATCH_ABILITIES)
                    .overlayTieredHullRenderer("dual_hatch.import")
                    .tooltips(
                            Component.translatable("gtceu.machine.dual_hatch.import.tooltip"),
                            Component.translatable(
                                    "gtceu.universal.tooltip.item_storage_capacity",
                                    (int) Math.pow((tier - 4), 2)),
                            Component.translatable(
                                    "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                    (tier - 4),
                                    DualHatchPartMachine.getTankCapacity(DualHatchPartMachine.INITIAL_TANK_CAPACITY,
                                            tier)),
                            Component.translatable("gtceu.universal.enabled"))
                    .register(),
            DUAL_HATCH_TIERS);

    public static final MachineDefinition[] DUAL_EXPORT_HATCH = registerTieredMachines(
            "dual_output_hatch",
            (holder, tier) -> new DualHatchPartMachine(holder, tier, OUT),
            (tier, builder) -> builder
                    .langValue("%s Dual Output Hatch".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(DUAL_OUTPUT_HATCH_ABILITIES)
                    .overlayTieredHullRenderer("dual_hatch.export")
                    .tooltips(
                            Component.translatable("gtceu.machine.dual_hatch.export.tooltip"),
                            Component.translatable(
                                    "gtceu.universal.tooltip.item_storage_capacity",
                                    (int) Math.pow((tier - 4), 2)),
                            Component.translatable(
                                    "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                    (tier - 4),
                                    DualHatchPartMachine.getTankCapacity(
                                            DualHatchPartMachine.INITIAL_TANK_CAPACITY, tier)),
                            Component.translatable("gtceu.universal.enabled"))
                    .register(),
            DUAL_HATCH_TIERS);

    public static final MachineDefinition[] DIODE = registerTieredMachines("diode",
            DiodePartMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Diode".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.PASSTHROUGH_HATCH)
                    .renderer(() -> new DiodeRenderer(tier))
                    .tooltips(Component.translatable("gtceu.machine.diode.tooltip_general"),
                            Component.translatable("gtceu.machine.diode.tooltip_starts_at"),
                            Component.translatable("gtceu.universal.tooltip.voltage_in_out",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]),
                                    GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in_out_till",
                                    DiodePartMachine.MAX_AMPS))
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition[] ROTOR_HOLDER = registerTieredMachines("rotor_holder",
            RotorHolderPartMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Rotor Holder".formatted(VNF[tier]))
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.ROTOR_HOLDER)
                    .renderer(() -> new RotorHolderMachineRenderer(tier))
                    .tooltips(LangHandler.getFromMultiLang("gtceu.machine.rotor_holder.tooltip", 0),
                            LangHandler.getFromMultiLang("gtceu.machine.rotor_holder.tooltip", 1),
                            Component.translatable("gtceu.universal.disabled"))
                    .register(),
            GTValues.tiersBetween(HV, GTCEuAPI.isHighTier() ? OpV : UV));

    public static final MachineDefinition[] LASER_INPUT_HATCH_256 = registerLaserHatch(IN, 256,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_256 = registerLaserHatch(OUT, 256,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_1024 = registerLaserHatch(IN, 1024,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_1024 = registerLaserHatch(OUT, 1024,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_4096 = registerLaserHatch(IN, 4096,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_4096 = registerLaserHatch(OUT, 4096,
            PartAbility.OUTPUT_LASER);

    public static void init() {
        GTMultiMachines.init();
        GCYMMachines.init();
        GTResearchMachines.init();

        if (GTCEu.Mods.isAE2Loaded()) {
            GTAEMachines.init();
        }

        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.MACHINES.getRegistryName());
        }
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.MACHINES, MachineDefinition.class));
        GTRegistries.MACHINES.freeze();
    }

    public static MachineDefinition get(String name) {
        return GTRegistries.MACHINES.get(GTCEu.id(name));
    }
}
