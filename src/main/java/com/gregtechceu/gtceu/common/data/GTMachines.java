package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.*;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.*;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTCreateMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.common.machine.electric.*;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.*;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.*;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CharcoalPileIgniterMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitivePumpMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
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
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModLoader;

import appeng.api.networking.pathing.ChannelMode;
import appeng.core.AEConfig;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MACHINE;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.STEAM_BOILER_RECIPES;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toRomanNumeral;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote GTMachines
 */
public class GTMachines {

    public static final int[] ALL_TIERS = GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? MAX : UHV);
    public static final int[] ELECTRIC_TIERS = GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? OpV : UV);
    public static final int[] LOW_TIERS = GTValues.tiersBetween(LV, EV);
    public static final int[] HIGH_TIERS = GTValues.tiersBetween(IV, GTCEuAPI.isHighTier() ? OpV : UHV);
    public static final int[] MULTI_HATCH_TIERS = GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV);
    public static final int[] DUAL_HATCH_TIERS = GTValues.tiersBetween(LuV, GTCEuAPI.isHighTier() ? MAX : UHV);

    public static final Int2IntFunction defaultTankSizeFunction = tier -> (tier <= GTValues.LV ? 8 :
            tier == GTValues.MV ? 12 : tier == GTValues.HV ? 16 : tier == GTValues.EV ? 32 : 64) *
            FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction hvCappedTankSizeFunction = tier -> (tier <= GTValues.LV ? 8 :
            tier == GTValues.MV ? 12 : 16) * FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction largeTankSizeFunction = tier -> (tier <= GTValues.LV ? 32 :
            tier == GTValues.MV ? 48 : 64) * FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction steamGeneratorTankSizeFunction = tier -> Math.min(16 * (1 << (tier - 1)), 64) *
            FluidType.BUCKET_VOLUME;
    public static final Int2IntFunction genericGeneratorTankSizeFunction = tier -> Math.min(4 * (1 << (tier - 1)),
            16) * FluidType.BUCKET_VOLUME;

    public static Object2IntMap<MachineDefinition> DRUM_CAPACITY = new Object2IntArrayMap<>();

    public static final PartAbility[] DUAL_INPUT_HATCH_ABILITIES = new PartAbility[] {
            PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
    };

    public static final PartAbility[] DUAL_OUTPUT_HATCH_ABILITIES = new PartAbility[] {
            PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS,
    };

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
    public static final MachineDefinition STEAM_MINER = REGISTRATE
            .machine("steam_miner", holder -> new SteamMinerMachine(holder, 320, 4, 0))
            .rotationState(RotationState.NON_Y_AXIS)
            .langValue("Steam Miner")
            .recipeType(DUMMY_RECIPES)
            .tier(0)
            .tooltips(Component.translatable("gtceu.universal.tooltip.uses_per_tick_steam", 16)
                    .append(ChatFormatting.GRAY + ", ")
                    .append(Component.translatable("gtceu.machine.miner.per_block", 320 / 20)))
            .tooltipBuilder((item, tooltip) -> {
                int maxArea = IMiner.getWorkingArea(4);
                tooltip.add(Component.translatable("gtceu.universal.tooltip.working_area", maxArea, maxArea));
            })
            .compassNode("steam_miner")
            .renderer(() -> new SteamMinerRenderer(false, GTCEu.id("block/machines/steam_miner")))
            .register();

    //////////////////////////////////////
    // *** SimpleTieredMachine ***//
    //////////////////////////////////////
    public static final MachineDefinition[] HULL = registerTieredMachines("machine_hull", HullMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .overlayTieredHullRenderer("hull")
                    .abilities(PartAbility.PASSTHROUGH_HATCH)
                    .langValue("%s Machine Hull".formatted(VN[tier]))
                    .tooltips(Component.translatable("gtceu.machine.hull.tooltip"))
                    .compassNode("machine_hull")
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
                    .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                    .workableTieredHullRenderer(GTCEu.id("block/machines/macerator"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTRecipeTypes.MACERATOR_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .compassNode("macerator")
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
                    .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                    .workableTieredHullRenderer(GTCEu.id("block/machines/rock_crusher"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTRecipeTypes.ROCK_BREAKER_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .tooltips(explosion())
                    .compassNode("rock_crusher")
                    .register(),
            ELECTRIC_TIERS);
    public static final MachineDefinition[] AIR_SCRUBBER = registerTieredMachines("air_scrubber",
            AirScrubberMachine::new, (tier, builder) -> builder
                    .langValue("%s Air Scrubber %s".formatted(VLVH[tier], VLVT[tier]))
                    .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("air_scrubber"),
                            GTRecipeTypes.AIR_SCRUBBER_RECIPES))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.AIR_SCRUBBER_RECIPES)
                    .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                    .workableTieredHullRenderer(GTCEu.id("block/machines/air_scrubber"))
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
                            GTRecipeTypes.AIR_SCRUBBER_RECIPES, defaultTankSizeFunction.apply(tier), true))
                    .tooltips(explosion())
                    .compassNode("air_scrubber")
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
            .compassNode("ld_item_pipeline")
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
            .compassNode("ld_fluid_pipeline")
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
                    .compassNode("pump")
                    .register(),
            LV, MV, HV, EV);

    public static final MachineDefinition[] FISHER = registerTieredMachines("fisher", FisherMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .editableUI(FisherMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("fisher"), (tier + 1) * (tier + 1)))
                    .tieredHullRenderer(GTCEu.id("block/machine/fisher_machine"))
                    .langValue("%s Fisher %s".formatted(VLVH[tier], VLVT[tier]))
                    .tooltips(Component.translatable("gtceu.machine.fisher.tooltip"),
                            Component.translatable("gtceu.machine.fisher.speed", 1000 - tier * 200L),
                            Component.translatable("gtceu.machine.fisher.requirement", FisherMachine.WATER_CHECK_SIZE,
                                    FisherMachine.WATER_CHECK_SIZE),
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]),
                                    GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64)))
                    .compassNode("fisher")
                    .register(),
            LV, MV, HV, EV);

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
                    .compassNode("block_breaker")
                    .register(),
            LV, MV, HV, EV);

    public static final MachineDefinition[] MINER = registerTieredMachines("miner",
            (holder, tier) -> new MinerMachine(holder, tier, 320 / (tier * 2), tier * 8, tier),
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("%s Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .editableUI(MinerMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("miner"), (tier + 1) * (tier + 1)))
                    .renderer(() -> new MinerRenderer(tier, GTCEu.id("block/machines/miner")))
                    .tooltipBuilder((stack, tooltip) -> {
                        int maxArea = IMiner.getWorkingArea(tier * 8);
                        long energyPerTick = GTValues.V[tier - 1];
                        int tickSpeed = 320 / (tier * 2);
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
                    .compassNode("miner")
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
                    .compassNode("world_accelerator")
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
                    .compassNode("item_collector")
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
                    .compassNode("buffer")
                    .register(),
            LV, MV, HV);

    public static final BiConsumer<ItemStack, List<Component>> CREATIVE_TOOLTIPS = (stack, components) -> components
            .add(Component.translatable("gtceu.creative_tooltip.1")
                    .append(Component.translatable("gtceu.creative_tooltip.2")
                            .withStyle(style -> style.withColor(TooltipHelper.RAINBOW_SLOW.getCurrent())))
                    .append(Component.translatable("gtceu.creative_tooltip.3")));

    public static BiConsumer<ItemStack, List<Component>> createCreativeTooltips(boolean share) {
        return (stack, list) -> {
            CREATIVE_TOOLTIPS.accept(stack, list);
            list.add(Component.translatable("gtceu.universal.%s".formatted(share ? "enabled" : "disabled")));
        };
    }

    public static final MachineDefinition CREATIVE_ENERGY = REGISTRATE
            .machine("creative_energy", CreativeEnergyContainerMachine::new)
            .rotationState(RotationState.NONE)
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .compassNodeSelf()
            .register();

    public static final MachineDefinition CREATIVE_COMPUTATION_PROVIDER = REGISTRATE
            .machine("creative_computation_provider", CreativeComputationProviderMachine::new)
            .rotationState(RotationState.NONE)
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .compassNodeSelf()
            .register();

    public static final MachineDefinition CREATIVE_FLUID = REGISTRATE.machine("creative_tank", CreativeTankMachine::new)
            .rotationState(RotationState.ALL)
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .renderer(() -> new QuantumTankRenderer(MAX, GTCEu.id("block/machine/creative_tank")))
            .hasTESR(true)
            .compassNodeSelf()
            .register();
    public static final MachineDefinition CREATIVE_ITEM = REGISTRATE
            .machine("creative_chest", CreativeChestMachine::new)
            .rotationState(RotationState.ALL)
            .tooltipBuilder(CREATIVE_TOOLTIPS)
            .renderer(() -> new QuantumChestRenderer(MAX, GTCEu.id("block/machine/creative_chest")))
            .hasTESR(true)
            .compassNodeSelf()
            .register();

    public static BiConsumer<ItemStack, List<Component>> CHEST_TOOLTIPS = (stack, list) -> {
        if (stack.hasTag()) {
            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            int storedAmount = stack.getOrCreateTag().getInt("storedAmount");
            list.add(1, Component.translatable("gtceu.universal.tooltip.item_stored", itemStack.getDescriptionId(),
                    storedAmount));
        }
    };

    public static final MachineDefinition[] SUPER_CHEST = registerTieredMachines("super_chest",
            (holder, tier) -> new QuantumChestMachine(holder, tier,
                    (int) Math.min(4000000L * (long) Math.pow(2, tier - 1), Integer.MAX_VALUE)),
            (tier, builder) -> builder
                    .langValue("Super Chest " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumChestRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(CHEST_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_total",
                                    FormattingUtil.formatNumbers(4000000L * (long) Math.pow(2, tier - 1))))
                    .compassNode("super_chest")
                    .register(),
            LOW_TIERS);

    public static final MachineDefinition[] QUANTUM_CHEST = registerTieredMachines("quantum_chest",
            (holder, tier) -> new QuantumChestMachine(holder, tier,
                    tier == GTValues.UHV ? Integer.MAX_VALUE :
                            (int) Math.min(4000000L * (long) Math.pow(2, tier - 1), Integer.MAX_VALUE)),
            (tier, builder) -> builder
                    .langValue("Quantum Chest " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumChestRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(CHEST_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_total",
                                    /* tier == GTValues.UHV ? Integer.MAX_VALUE : */ FormattingUtil
                                            .formatNumbers(4000000L * (long) Math.pow(2, tier - 1))))
                    .compassNode("super_chest")
                    .register(),
            HIGH_TIERS);

    public static BiConsumer<ItemStack, List<Component>> createTankTooltips(String nbtName,
                                                                            @Nullable Material material) {
        return (stack, list) -> {
            if (stack.hasTag()) {
                FluidStack tank = FluidStack.loadFluidStackFromNBT(stack.getOrCreateTagElement(nbtName));
                list.add(1, Component.translatable("gtceu.universal.tooltip.fluid_stored", tank.getDisplayName(),
                        FormattingUtil.formatNumbers(tank.getAmount())));
            }

            var item = stack.getItem();
            if (item instanceof DrumMachineItem drumItem && material != null) {
                if (material.hasProperty(PropertyKey.FLUID_PIPE)) {
                    FluidPipeProperties pipeprops = material.getProperty(PropertyKey.FLUID_PIPE);
                    pipeprops.appendTooltips(list, true, true);
                }
            }
        };
    }

    public static final MachineDefinition[] SUPER_TANK = registerTieredMachines("super_tank",
            (holder, tier) -> new QuantumTankMachine(holder, tier,
                    4000 * FluidType.BUCKET_VOLUME * (int) Math.pow(2, tier - 1)),
            (tier, builder) -> builder
                    .langValue("Super Tank " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumTankRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(createTankTooltips("stored", null))
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                    FormattingUtil.formatNumbers(4000000L * (long) Math.pow(2, tier - 1))))
                    .compassNode("super_tank")
                    .register(),
            LOW_TIERS);

    public static final MachineDefinition[] QUANTUM_TANK = registerTieredMachines("quantum_tank",
            (holder, tier) -> new QuantumTankMachine(holder, tier,
                    tier == GTValues.UHV ? Integer.MAX_VALUE :
                            4000 * FluidType.BUCKET_VOLUME * (int) Math.pow(2, tier - 1)),
            (tier, builder) -> builder
                    .langValue("Quantum Tank " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumTankRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(createTankTooltips("stored", null))
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                    /* tier == GTValues.UHV ? Integer.MAX_VALUE : */ FormattingUtil
                                            .formatNumbers(4000000L * (long) Math.pow(2, tier - 1))))
                    .compassNode("super_tank")
                    .register(),
            HIGH_TIERS);

    // Multiblock Tanks
    public static final MachineDefinition WOODEN_TANK_VALVE = registerTankValve(
            "wooden_tank_valve", "Wooden Tank Valve", false,
            (builder, overlay) -> builder.sidedWorkableCasingRenderer("block/casings/wood_wall", overlay));
    public static final MultiblockMachineDefinition WOODEN_MULTIBLOCK_TANK = registerMultiblockTank(
            "wooden_multiblock_tank", "Wooden Multiblock Tank", 250 * 1000,
            CASING_WOOD_WALL, WOODEN_TANK_VALVE::getBlock,
            new PropertyFluidFilter(340, false, false, false, false),
            (builder, overlay) -> builder.sidedWorkableCasingRenderer("block/casings/wood_wall", overlay));

    public static final MachineDefinition STEEL_TANK_VALVE = registerTankValve(
            "steel_tank_valve", "Steel Tank Valve", true,
            (builder, overlay) -> builder.workableCasingRenderer(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"), overlay));
    public static final MultiblockMachineDefinition STEEL_MULTIBLOCK_TANK = registerMultiblockTank(
            "steel_multiblock_tank", "Steel Multiblock Tank", 1000 * 1000,
            CASING_STEEL_SOLID, STEEL_TANK_VALVE::getBlock,
            null,
            (builder, overlay) -> builder.workableCasingRenderer(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"), overlay));

    public static MachineDefinition WOODEN_CRATE = registerCrate(GTMaterials.Wood, 27, "Wooden Crate");
    public static MachineDefinition BRONZE_CRATE = registerCrate(GTMaterials.Bronze, 54, "Bronze Crate");
    public static MachineDefinition STEEL_CRATE = registerCrate(GTMaterials.Steel, 72, "Steel Crate");
    public static MachineDefinition ALUMINIUM_CRATE = registerCrate(GTMaterials.Aluminium, 90, "Aluminium Crate");
    public static MachineDefinition STAINLESS_STEEL_CRATE = registerCrate(GTMaterials.StainlessSteel, 108,
            "Stainless Steel Crate");
    public static MachineDefinition TITANIUM_CRATE = registerCrate(GTMaterials.Titanium, 126, "Titanium Crate");
    public static MachineDefinition TUNGSTENSTEEL_CRATE = registerCrate(GTMaterials.TungstenSteel, 144,
            "Tungstensteel Crate");

    public static MachineDefinition WOODEN_DRUM = registerDrum(GTMaterials.Wood, (int) (16 * FluidType.BUCKET_VOLUME),
            "Wooden Barrel");
    public static MachineDefinition BRONZE_DRUM = registerDrum(GTMaterials.Bronze, (int) (32 * FluidType.BUCKET_VOLUME),
            "Bronze Drum");
    public static MachineDefinition STEEL_DRUM = registerDrum(GTMaterials.Steel, (int) (64 * FluidType.BUCKET_VOLUME),
            "Steel Drum");
    public static MachineDefinition ALUMINIUM_DRUM = registerDrum(GTMaterials.Aluminium,
            (int) (128 * FluidType.BUCKET_VOLUME), "Aluminium Drum");
    public static MachineDefinition STAINLESS_STEEL_DRUM = registerDrum(GTMaterials.StainlessSteel,
            (int) (256 * FluidType.BUCKET_VOLUME), "Stainless Steel Drum");
    public static MachineDefinition GOLD_DRUM = registerDrum(GTMaterials.Gold, (int) (32 * FluidType.BUCKET_VOLUME),
            "Gold Drum");
    public static MachineDefinition TITANIUM_DRUM = registerDrum(GTMaterials.Titanium,
            (int) (512 * FluidType.BUCKET_VOLUME), "Titanium Drum");
    public static MachineDefinition TUNGSTENSTEEL_DRUM = registerDrum(GTMaterials.TungstenSteel,
            (int) (1024 * FluidType.BUCKET_VOLUME), "Tungstensteel Drum");

    //////////////////////////////////////
    // ********** Part **********//
    //////////////////////////////////////
    public static final MachineDefinition[] ITEM_IMPORT_BUS = registerTieredMachines("input_bus",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Input Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.IMPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.import")
                    .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ITEM_EXPORT_BUS = registerTieredMachines("output_bus",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Output Bus")
                    .rotationState(RotationState.ALL)
                    .abilities(
                            tier == 0 ? new PartAbility[] { PartAbility.EXPORT_ITEMS, PartAbility.STEAM_EXPORT_ITEMS } :
                                    new PartAbility[] { PartAbility.EXPORT_ITEMS })
                    .overlayTieredHullRenderer("item_bus.export")
                    .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                    (1 + Math.min(9, tier)) * (1 + Math.min(9, tier))))
                    .compassNode("item_bus")
                    .register(),
            ALL_TIERS);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH = registerFluidHatches(
            "input_hatch", "Input Hatch", "fluid_hatch.import", "fluid_hatch.import",
            IO.IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1,
            ALL_TIERS, PartAbility.IMPORT_FLUIDS,
            PartAbility.IMPORT_FLUIDS_1X);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH_4X = registerFluidHatches(
            "input_hatch_4x", "Quadruple Input Hatch", "fluid_hatch.import_4x", "fluid_hatch.import",
            IO.IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
            MULTI_HATCH_TIERS, PartAbility.IMPORT_FLUIDS,
            PartAbility.IMPORT_FLUIDS_4X);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH_9X = registerFluidHatches(
            "input_hatch_9x", "Nonuple Input Hatch", "fluid_hatch.import_9x", "fluid_hatch.import",
            IO.IN, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_9X, 9,
            MULTI_HATCH_TIERS, PartAbility.IMPORT_FLUIDS,
            PartAbility.IMPORT_FLUIDS_9X);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH = registerFluidHatches(
            "output_hatch", "Output Hatch", "fluid_hatch.export", "fluid_hatch.export",
            IO.OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1,
            ALL_TIERS, PartAbility.EXPORT_FLUIDS,
            PartAbility.EXPORT_FLUIDS_1X);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH_4X = registerFluidHatches(
            "output_hatch_4x", "Quadruple Output Hatch", "fluid_hatch.export_4x", "fluid_hatch.export",
            IO.OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_4X, 4,
            MULTI_HATCH_TIERS, PartAbility.EXPORT_FLUIDS,
            PartAbility.EXPORT_FLUIDS_4X);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH_9X = registerFluidHatches(
            "output_hatch_9x", "Nonuple Output Hatch", "fluid_hatch.export_9x", "fluid_hatch.export",
            IO.OUT, FluidHatchPartMachine.INITIAL_TANK_CAPACITY_9X, 9,
            MULTI_HATCH_TIERS, PartAbility.EXPORT_FLUIDS,
            PartAbility.EXPORT_FLUIDS_9X);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH = registerTieredMachines("energy_input_hatch",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 2),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.energy_hatch.input.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input")
                    .compassNode("energy_hatch")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH = registerTieredMachines("energy_output_hatch",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 2),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.energy_hatch.output.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output")
                    .compassNode("energy_hatch")
                    .register(),
            ALL_TIERS);

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_4A = registerTieredMachines("energy_input_hatch_4a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 4A Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input_4a")
                    .compassNode("energy_hatch")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_4A = registerTieredMachines("energy_output_hatch_4a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 4),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 4A Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output_4a")
                    .compassNode("energy_hatch")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] ENERGY_INPUT_HATCH_16A = registerTieredMachines("energy_input_hatch_16a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 16),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 16A Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input_16a")
                    .compassNode("energy_hatch")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] ENERGY_OUTPUT_HATCH_16A = registerTieredMachines("energy_output_hatch_16a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 16),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 16A Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output_16a")
                    .compassNode("energy_hatch")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] SUBSTATION_ENERGY_INPUT_HATCH = registerTieredMachines(
            "substation_input_hatch_64a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 64),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 64A Substation Energy Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.SUBSTATION_INPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.substation_hatch.input.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.input_64a")
                    .compassNode("energy_hatch")
                    .register(),
            GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV));

    public static final MachineDefinition[] SUBSTATION_ENERGY_OUTPUT_HATCH = registerTieredMachines(
            "substation_output_hatch_64a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 64),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " 64A Substation Dynamo Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.SUBSTATION_OUTPUT_ENERGY)
                    .tooltips(Component.translatable("gtceu.machine.substation_hatch.output.tooltip"))
                    .overlayTieredHullRenderer("energy_hatch.output_64a")
                    .compassNode("energy_hatch")
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
                    .compassNode("muffler_hatch")
                    .register(),
            ELECTRIC_TIERS);

    public static final MachineDefinition STEAM_IMPORT_BUS = REGISTRATE
            .machine("steam_input_bus", holder -> new SteamItemBusPartMachine(holder, IO.IN))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM_IMPORT_ITEMS)
            .overlaySteamHullRenderer("item_bus.import")
            .langValue("Input Bus (Steam)")
            .compassSections(GTCompassSections.STEAM)
            .compassNode("item_bus")
            .register();

    public static final MachineDefinition STEAM_EXPORT_BUS = REGISTRATE
            .machine("steam_output_bus", holder -> new SteamItemBusPartMachine(holder, IO.OUT))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM_EXPORT_ITEMS)
            .overlaySteamHullRenderer("item_bus.export")
            .langValue("Output Bus (Steam)")
            .compassSections(GTCompassSections.STEAM)
            .compassNode("item_bus")
            .register();

    public static final MachineDefinition STEAM_HATCH = REGISTRATE
            .machine("steam_input_hatch", SteamHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM)
            .overlaySteamHullRenderer("steam_hatch")
            .tooltips(
                    Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            SteamHatchPartMachine.INITIAL_TANK_CAPACITY),
                    Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"))
            .compassSections(GTCompassSections.STEAM)
            .compassNode("steam_hatch")
            .register();

    public static final MachineDefinition COKE_OVEN_HATCH = REGISTRATE.machine("coke_oven_hatch", CokeOvenHatch::new)
            .rotationState(RotationState.ALL)
            .modelRenderer(() -> GTCEu.id("block/machine/part/coke_oven_hatch"))
            .compassSections(GTCompassSections.STEAM)
            .compassNode("coke_oven_hatch")
            .register();

    public static final MachineDefinition PUMP_HATCH = REGISTRATE.machine("pump_hatch", PumpHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.PUMP_FLUID_HATCH)
            .renderer(PumpHatchPartRenderer::new)
            .compassSections(GTCompassSections.STEAM)
            .compassNode("pump_hatch")
            .register();

    public static final MachineDefinition MAINTENANCE_HATCH = REGISTRATE
            .machine("maintenance_hatch", (blockEntity) -> new MaintenanceHatchPartMachine(blockEntity, false))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"))
            .renderer(() -> new MaintenanceHatchPartRenderer(1, GTCEu.id("block/machine/part/maintenance")))
            .compassNodeSelf()
            .register();

    public static final MachineDefinition CONFIGURABLE_MAINTENANCE_HATCH = REGISTRATE
            .machine("configurable_maintenance_hatch",
                    (blockEntity) -> new MaintenanceHatchPartMachine(blockEntity, true))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"))
            .renderer(
                    () -> new MaintenanceHatchPartRenderer(3, GTCEu.id("block/machine/part/maintenance.configurable")))
            .compassNodeSelf()
            .register();

    public static final MachineDefinition CLEANING_MAINTENANCE_HATCH = REGISTRATE
            .machine("cleaning_maintenance_hatch", CleaningMaintenanceHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"),
                    Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.0"),
                    Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.1"))
            .tooltipBuilder((stack, tooltips) -> {
                for (CleanroomType type : CleaningMaintenanceHatchPartMachine.getCleanroomTypes()) {
                    tooltips.add(Component.literal(String.format("  %s%s", ChatFormatting.GREEN,
                            Component.translatable(type.getTranslationKey()).getString())));
                }
            })
            .renderer(() -> new MaintenanceHatchPartRenderer(3, GTCEu.id("block/machine/part/maintenance.cleaning")))
            .compassNodeSelf()
            .register();

    public static final MachineDefinition AUTO_MAINTENANCE_HATCH = REGISTRATE
            .machine("auto_maintenance_hatch", AutoMaintenanceHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.MAINTENANCE)
            .tooltips(Component.translatable("gtceu.universal.disabled"))
            .renderer(() -> new MaintenanceHatchPartRenderer(3, GTCEu.id("block/machine/part/maintenance.full_auto")))
            .compassNodeSelf()
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
                    .compassNode("item_passthrough_hatch")
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
                    .compassNode("fluid_passthrough_hatch")
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
            .compassNodeSelf()
            .register();

    public static final MachineDefinition[] DUAL_IMPORT_HATCH = registerTieredMachines(
            "dual_input_hatch",
            (holder, tier) -> new DualHatchPartMachine(holder, tier, IO.IN),
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
                    .compassNode("dual_hatch")
                    .register(),
            DUAL_HATCH_TIERS);

    public static final MachineDefinition[] DUAL_EXPORT_HATCH = registerTieredMachines(
            "dual_output_hatch",
            (holder, tier) -> new DualHatchPartMachine(holder, tier, IO.OUT),
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
                    .compassNode("dual_hatch")
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
                    .compassNode("diode")
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
                    .compassNode("rotor_holder")
                    .register(),
            HV, EV, IV, LuV, ZPM, UV);

    public static final MachineDefinition[] LASER_INPUT_HATCH_256 = registerLaserHatch(IO.IN, 256,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_256 = registerLaserHatch(IO.OUT, 256,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_1024 = registerLaserHatch(IO.IN, 1024,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_1024 = registerLaserHatch(IO.OUT, 1024,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] LASER_INPUT_HATCH_4096 = registerLaserHatch(IO.IN, 4096,
            PartAbility.INPUT_LASER);
    public static final MachineDefinition[] LASER_OUTPUT_HATCH_4096 = registerLaserHatch(IO.OUT, 4096,
            PartAbility.OUTPUT_LASER);

    //////////////////////////////////////
    // ******* Multiblock *******//
    //////////////////////////////////////
    public static final MultiblockMachineDefinition LARGE_BOILER_BRONZE = registerLargeBoiler("bronze",
            CASING_BRONZE_BRICKS, CASING_BRONZE_PIPE, FIREBOX_BRONZE,
            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"), BoilerFireboxType.BRONZE_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.bronzeBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.bronzeBoilerHeatSpeed);
    public static final MultiblockMachineDefinition LARGE_BOILER_STEEL = registerLargeBoiler("steel",
            CASING_STEEL_SOLID, CASING_STEEL_PIPE, FIREBOX_STEEL,
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"), BoilerFireboxType.STEEL_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.steelBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.steelBoilerHeatSpeed);
    public static final MultiblockMachineDefinition LARGE_BOILER_TITANIUM = registerLargeBoiler("titanium",
            CASING_TITANIUM_STABLE, CASING_TITANIUM_PIPE, FIREBOX_TITANIUM,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"), BoilerFireboxType.TITANIUM_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.titaniumBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.titaniumBoilerHeatSpeed);
    public static final MultiblockMachineDefinition LARGE_BOILER_TUNGSTENSTEEL = registerLargeBoiler("tungstensteel",
            CASING_TUNGSTENSTEEL_ROBUST, CASING_TUNGSTENSTEEL_PIPE, FIREBOX_TUNGSTENSTEEL,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            BoilerFireboxType.TUNGSTENSTEEL_FIREBOX,
            ConfigHolder.INSTANCE.machines.largeBoilers.tungstensteelBoilerMaxTemperature,
            ConfigHolder.INSTANCE.machines.largeBoilers.tungstensteelBoilerHeatSpeed);

    public static final MultiblockMachineDefinition COKE_OVEN = REGISTRATE.multiblock("coke_oven", CokeOvenMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.COKE_OVEN_RECIPES)
            .appearanceBlock(CASING_COKE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XYX", "XXX")
                    .where('X',
                            blocks(CASING_COKE_BRICKS.get()).or(blocks(COKE_OVEN_HATCH.get()).setMaxGlobalLimited(5)))
                    .where('#', Predicates.air())
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_coke_bricks"),
                    GTCEu.id("block/multiblock/coke_oven"))
            .compassSections(GTCompassSections.STEAM)
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition PRIMITIVE_BLAST_FURNACE = REGISTRATE
            .multiblock("primitive_blast_furnace", PrimitiveBlastFurnaceMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES)
            .appearanceBlock(CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "X#X", "X#X")
                    .aisle("XXX", "XYX", "XXX", "XXX")
                    .where('X', blocks(CASING_PRIMITIVE_BRICKS.get()))
                    .where('#', Predicates.air())
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTCEu.id("block/multiblock/primitive_blast_furnace"))
            .compassSections(GTCompassSections.STEAM)
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition ELECTRIC_BLAST_FURNACE = REGISTRATE
            .multiblock("electric_blast_furnace", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifier(GTRecipeModifiers::ebfOverclock)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "CCC", "XXX")
                    .aisle("XXX", "C#C", "C#C", "XMX")
                    .aisle("XSX", "CCC", "CCC", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, false)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('C', heatingCoils())
                    .where('#', air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("ISO", "CCC", "CCC", "XMX")
                        .aisle("FXD", "C#C", "C#C", "XHX")
                        .aisle("EEX", "CCC", "CCC", "XXX")
                        .where('X', CASING_INVAR_HEATPROOF.getDefaultState())
                        .where('S', definition, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.WEST)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.EAST)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.UP)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH);
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .recoveryItems(
                    () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"))
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.0"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component
                                    .translatable(
                                            FormattingUtil
                                                    .formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) +
                                                    "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .compassSections(GTCompassSections.TIER[MV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition LARGE_CHEMICAL_REACTOR = REGISTRATE
            .multiblock("large_chemical_reactor", WorkableElectricMultiblockMachine::new)
            .tooltips(GTMachines.defaultEnvironmentRequirement())
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
            .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_PTFE_INERT)
            .pattern(definition -> {
                var casing = blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(10);
                var abilities = Predicates.autoAbilities(definition.getRecipeTypes())
                        .or(Predicates.autoAbilities(true, false, false));
                return FactoryBlockPattern.start()
                        .aisle("XXX", "XCX", "XXX")
                        .aisle("XCX", "CPC", "XCX")
                        .aisle("XXX", "XSX", "XXX")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('X', casing.or(abilities))
                        .where('P', blocks(CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where('C', blocks(COIL_CUPRONICKEL.get()).setExactLimit(1)
                                .or(abilities)
                                .or(casing))
                        .build();
            })
            .shapeInfos(definition -> {
                ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var baseBuilder = MultiblockShapeInfo.builder()
                        .where('S', definition, Direction.NORTH)
                        .where('X', CASING_PTFE_INERT.getDefaultState())
                        .where('P', CASING_POLYTETRAFLUOROETHYLENE_PIPE.getDefaultState())
                        .where('C', COIL_CUPRONICKEL.getDefaultState())
                        .where('I', ITEM_IMPORT_BUS[3], Direction.NORTH)
                        .where('E', ENERGY_INPUT_HATCH[3], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[3], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[3], Direction.NORTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('H', FLUID_EXPORT_HATCH[3], Direction.NORTH);
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "XPX", "XXX")
                        .aisle("XEX", "XCX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "XPX", "XCX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XCX", "XPX", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "CPX", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XMX")
                        .aisle("XXX", "XPC", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build());
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/large_chemical_reactor"))
            .compassSections(GTCompassSections.TIER[HV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition IMPLOSION_COMPRESSOR = REGISTRATE
            .multiblock("implosion_compressor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.IMPLOSION_RECIPES)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, true, false)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .compassSections(GTCompassSections.TIER[HV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition PYROLYSE_OVEN = REGISTRATE
            .multiblock("pyrolyse_oven", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.PYROLYSE_RECIPES)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers::pyrolyseOvenOverclock)
            .appearanceBlock(MACHINE_CASING_ULV)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.get())))
                    .where('X',
                            blocks(MACHINE_CASING_ULV.get()).setMinGlobalLimited(6)
                                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                    .or(Predicates.autoAbilities(true, true, false)))
                    .where('C', Predicates.heatingCoils())
                    .where('#', Predicates.air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("IXO", "XSX", "FMD")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("EEX", "XHX", "XXX")
                        .where('S', definition, Direction.NORTH)
                        .where('X', MACHINE_CASING_ULV.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/voltage/ulv/side"),
                    GTCEu.id("block/multiblock/pyrolyse_oven"))
            .tooltips(Component.translatable("gtceu.machine.pyrolyse_oven.tooltip"),
                    Component.translatable("gtceu.machine.pyrolyse_oven.tooltip.1"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.pyrolyse_oven.speed",
                            coilMachine.getCoilTier() == 0 ? 75 : 50 * (coilMachine.getCoilTier() + 1)));
                }
            })
            .compassSections(GTCompassSections.TIER[MV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition MULTI_SMELTER = REGISTRATE
            .multiblock("multi_smelter", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeTypes(GTRecipeTypes.FURNACE_RECIPES, GTRecipeTypes.ALLOY_SMELTER_RECIPES)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                    GTRecipeModifiers::multiSmelterParallel)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                    Component.translatable("gtceu.electric_furnace"), Component.translatable("gtceu.alloy_smelter")))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "XXX")
                    .aisle("XXX", "C#C", "XMX")
                    .aisle("XSX", "CCC", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, false)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('C', heatingCoils())
                    .where('#', air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("ISO", "CCC", "XMX")
                        .aisle("XXX", "C#C", "XHX")
                        .aisle("EEX", "CCC", "XXX")
                        .where('S', definition, Direction.NORTH)
                        .where('X', CASING_INVAR_HEATPROOF.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('H', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .recoveryItems(
                    () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/multi_furnace"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.multi_furnace.heating_coil_level",
                            coilMachine.getCoilType().getLevel()));
                    components.add(Component.translatable("gtceu.multiblock.multi_furnace.heating_coil_discount",
                            coilMachine.getCoilType().getEnergyDiscount()));
                }
            })
            .compassSections(GTCompassSections.TIER[MV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition CRACKER = REGISTRATE
            .multiblock("cracker", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.CRACKING_RECIPES)
            .recipeModifier(GTRecipeModifiers::crackerOverclock)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("HCHCH", "HCHCH", "HCHCH")
                    .aisle("HCHCH", "H###H", "HCHCH")
                    .aisle("HCHCH", "HCOCH", "HCHCH")
                    .where('O', Predicates.controller(blocks(definition.get())))
                    .where('H', blocks(CASING_STAINLESS_CLEAN.get()).setMinGlobalLimited(12)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, true, false)))
                    .where('#', Predicates.air())
                    .where('C', Predicates.heatingCoils())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("FCICD", "HCSCH", "HCMCH")
                        .aisle("ECHCH", "H###H", "HCHCH")
                        .aisle("ECHCH", "HCXCH", "HCHCH")
                        .where('S', definition, Direction.NORTH)
                        .where('H', CASING_STAINLESS_CLEAN.getDefaultState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV], Direction.WEST)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.NORTH)
                        .where('M', MAINTENANCE_HATCH, Direction.NORTH)
                        .where('X', MUFFLER_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('#', Blocks.AIR.defaultBlockState());
                GTCEuAPI.HEATING_COILS.entrySet().stream()
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(
                                coil -> shapeInfo.add(builder.shallowCopy().where('C', coil.getValue().get()).build()));
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/cracking_unit"))
            .tooltips(Component.translatable("gtceu.machine.cracker.tooltip.1"))
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.cracking_unit.energy",
                            100 - 10 * coilMachine.getCoilTier()));
                }
            })
            .compassSections(GTCompassSections.TIER[EV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition DISTILLATION_TOWER = REGISTRATE
            .multiblock("distillation_tower", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DISTILLATION_RECIPES)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("YSY", "YYY", "YYY")
                    .aisle("XXX", "X#X", "XXX").setRepeatable(1, 11)
                    .aisle("XXX", "XXX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('Y', blocks(CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1)))
                    .where('X', blocks(CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS_1X).setMinLayerLimited(1)
                                    .setMaxLayerLimited(1)))
                    .where('#', Predicates.air())
                    .build())
            .allowExtendedFacing(false)
            .partSorter(Comparator.comparingInt(a -> a.self().getPos().getY()))
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/distillation_tower"))
            .compassSections(GTCompassSections.TIER[EV])
            .compassNodeSelf()
            .register();

    public static final MachineDefinition EVAPORATION_PLANT = REGISTRATE
            .multiblock("evaporation_plant", WorkableElectricMultiblockMachine::new)
            .langValue("Evaporation Tower")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.EVAPORATION_RECIPES)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_STAINLESS_EVAPORATION)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("FYF", "YYY", "FYF")
                    .aisle("YSY", "Y#Y", "YYY")
                    .aisle("XXX", "X#X", "XXX").setRepeatable(2, 5)
                    .aisle(" Z ", "ZZZ", " Z ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('Y', blocks(CASING_STAINLESS_EVAPORATION.get())
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1)))
                    .where('X', blocks(CASING_STAINLESS_EVAPORATION.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS_1X).setMinLayerLimited(1)
                                    .setMaxLayerLimited(1)))
                    .where('Z', blocks(CASING_STAINLESS_EVAPORATION.get()))
                    .where('F', Predicates.frames(Aluminium))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .build())
            .allowExtendedFacing(false)
            .partSorter(Comparator.comparingInt(a -> a.self().getPos().getY()))
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_stainless_evaporation"),
                    GTCEu.id("block/multiblock/evaporation_plant"))
            .register();

    public static final MultiblockMachineDefinition VACUUM_FREEZER = REGISTRATE
            .multiblock("vacuum_freezer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .recipeModifiers(
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/vacuum_freezer"))
            .compassSections(GTCompassSections.TIER[HV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition ASSEMBLY_LINE = REGISTRATE
            .multiblock("assembly_line", AssemblyLineMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
            .alwaysTryModifyRecipe(true)
            .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start(BACK, UP, RIGHT)
                    .aisle("FIF", "RTR", "SAG", "#Y#")
                    .aisle("FIF", "RTR", "DAG", "#Y#").setRepeatable(3, 15)
                    .aisle("FOF", "RTR", "DAG", "#Y#")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('F', blocks(CASING_STEEL_SOLID.get())
                            .or(!ConfigHolder.INSTANCE.machines.orderedAssemblyLineFluids ?
                                    Predicates.abilities(PartAbility.IMPORT_FLUIDS_1X,
                                            PartAbility.IMPORT_FLUIDS_4X, PartAbility.IMPORT_FLUIDS_9X) :
                                    Predicates.abilities(PartAbility.IMPORT_FLUIDS_1X).setMaxGlobalLimited(4)))
                    .where('O',
                            Predicates.abilities(PartAbility.EXPORT_ITEMS)
                                    .addTooltips(Component.translatable("gtceu.multiblock.pattern.location_end")))
                    .where('Y',
                            blocks(CASING_STEEL_SOLID.get()).or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                                    .setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('I', blocks(ITEM_IMPORT_BUS[0].getBlock()))
                    .where('G', blocks(CASING_GRATE.get()))
                    .where('A', blocks(CASING_ASSEMBLY_CONTROL.get()))
                    .where('R', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('T', blocks(CASING_ASSEMBLY_LINE.get()))
                    .where('D', dataHatchPredicate(blocks(CASING_GRATE.get())))
                    .where('#', Predicates.any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .compassSections(GTCompassSections.TIER[IV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition PRIMITIVE_PUMP = REGISTRATE
            .multiblock("primitive_pump", PrimitivePumpMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(CASING_PUMP_DECK)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXXX", "##F#", "##F#")
                    .aisle("XXHX", "F##F", "FFFF")
                    .aisle("SXXX", "##F#", "##F#")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_PUMP_DECK.get()))
                    .where('F', Predicates.frames(GTMaterials.TreatedWood))
                    .where('H',
                            Predicates.abilities(PartAbility.PUMP_FLUID_HATCH)
                                    .or(blocks(FLUID_EXPORT_HATCH[LV].get(), FLUID_EXPORT_HATCH[MV].get())))
                    .where('#', Predicates.any())
                    .build())
            .allowExtendedFacing(false)
            .sidedWorkableCasingRenderer("block/casings/pump_deck", GTCEu.id("block/multiblock/primitive_pump"))
            .compassSections(GTCompassSections.STEAM)
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition STEAM_GRINDER = REGISTRATE
            .multiblock("steam_grinder", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where('X', blocks(CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(14)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/steam_grinder"))
            .compassSections(GTCompassSections.STEAM)
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition STEAM_OVEN = REGISTRATE
            .multiblock("steam_oven", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.FURNACE_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("FFF", "XXX", " X ")
                    .aisle("FFF", "X#X", " X ")
                    .aisle("FFF", "XSX", " X ")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('X', blocks(CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(6)
                            .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1)))
                    .where('F', blocks(FIREBOX_BRONZE.get())
                            .or(Predicates.abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .renderer(() -> new LargeBoilerRenderer(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    BoilerFireboxType.BRONZE_FIREBOX,
                    GTCEu.id("block/multiblock/steam_oven")))
            .compassSections(GTCompassSections.STEAM)
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition[] FUSION_REACTOR = registerTieredMultis("fusion_reactor",
            FusionReactorMachine::new, (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("Fusion Reactor Computer MK %s".formatted(toRomanNumeral(tier - 5)))
                    .recipeType(GTRecipeTypes.FUSION_RECIPES)
                    .recipeModifiers(GTRecipeModifiers.DEFAULT_ENVIRONMENT_REQUIREMENT,
                            FusionReactorMachine::recipeModifier)
                    .tooltips(
                            Component.translatable("gtceu.machine.fusion_reactor.capacity",
                                    FusionReactorMachine.calculateEnergyStorageFactor(tier, 16) / 1000000L),
                            Component.translatable("gtceu.machine.fusion_reactor.overclocking"),
                            Component.translatable("gtceu.multiblock.%s_fusion_reactor.description"
                                    .formatted(VN[tier].toLowerCase(Locale.ROOT))))
                    .appearanceBlock(() -> FusionReactorMachine.getCasingState(tier))
                    .pattern((definition) -> {
                        var casing = blocks(FusionReactorMachine.getCasingState(tier));
                        return FactoryBlockPattern.start()
                                .aisle("###############", "######OGO######", "###############")
                                .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                                .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                                .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                                .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                                .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                                .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                                .aisle("#C###########C#", "GAG#########GAG", "#C###########C#")
                                .aisle("#I###########I#", "OAO#########OAO", "#I###########I#")
                                .aisle("##C#########C##", "#GAG#######GAG#", "##C#########C##")
                                .aisle("##C#########C##", "#GAE#######EAG#", "##C#########C##")
                                .aisle("###C#######C###", "##EKEG###GEKE##", "###C#######C###")
                                .aisle("####CC###CC####", "###EAAOGOAAE###", "####CC###CC####")
                                .aisle("######ICI######", "####GGAAAGG####", "######ICI######")
                                .aisle("###############", "######OSO######", "###############")
                                .where('S', controller(blocks(definition.get())))
                                .where('G', blocks(FUSION_GLASS.get()).or(casing))
                                .where('E', casing.or(
                                        blocks(PartAbility.INPUT_ENERGY.getBlockRange(tier, UV).toArray(Block[]::new))
                                                .setMinGlobalLimited(1).setPreviewCount(16)))
                                .where('C', casing)
                                .where('K', blocks(FusionReactorMachine.getCoilState(tier)))
                                .where('O', casing.or(abilities(PartAbility.EXPORT_FLUIDS)))
                                .where('A', air())
                                .where('I', casing.or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(2)))
                                .where('#', any())
                                .build();
                    })
                    .shapeInfos((controller) -> {
                        List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();

                        MultiblockShapeInfo.ShapeInfoBuilder baseBuilder = MultiblockShapeInfo.builder()
                                .aisle("###############", "######NMN######", "###############")
                                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                                .aisle("####CC###CC####", "###w##SGS##e###", "####CC###CC####")
                                .aisle("###C#######C###", "##nKsG###GsKn##", "###C#######C###")
                                .aisle("##C#########C##", "#G#e#######w#G#", "##C#########C##")
                                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                                .aisle("#C###########C#", "G#G#########G#G", "#C###########C#")
                                .aisle("#D###########D#", "W#E#########W#E", "#U###########U#")
                                .aisle("##C#########C##", "#G#G#######G#G#", "##C#########C##")
                                .aisle("##C#########C##", "#G#e#######w#G#", "##C#########C##")
                                .aisle("###C#######C###", "##sKnG###GnKs##", "###C#######C###")
                                .aisle("####CC###CC####", "###w##NGN##e###", "####CC###CC####")
                                .aisle("######DCD######", "####GG###GG####", "######UCU######")
                                .aisle("###############", "######SGS######", "###############")
                                .where('M', controller, Direction.NORTH)
                                .where('C', FusionReactorMachine.getCasingState(tier))
                                .where('G', FUSION_GLASS.get())
                                .where('K', FusionReactorMachine.getCoilState(tier))
                                .where('W', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.WEST)
                                .where('E', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.EAST)
                                .where('S', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.SOUTH)
                                .where('N', GTMachines.FLUID_EXPORT_HATCH[tier], Direction.NORTH)
                                .where('w', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.WEST)
                                .where('e', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.EAST)
                                .where('s', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.SOUTH)
                                .where('n', GTMachines.ENERGY_INPUT_HATCH[tier], Direction.NORTH)
                                .where('U', GTMachines.FLUID_IMPORT_HATCH[tier], Direction.UP)
                                .where('D', GTMachines.FLUID_IMPORT_HATCH[tier], Direction.DOWN)
                                .where('#', Blocks.AIR.defaultBlockState());

                        shapeInfos.add(baseBuilder.shallowCopy()
                                .where('G', FusionReactorMachine.getCasingState(tier))
                                .build());
                        shapeInfos.add(baseBuilder.build());
                        return shapeInfos;
                    })
                    .renderer(() -> new FusionReactorRenderer(FusionReactorMachine.getCasingType(tier).getTexture(),
                            GTCEu.id("block/multiblock/fusion_reactor")))
                    .hasTESR(true)
                    .compassSections(GTCompassSections.TIER[LuV])
                    .compassNodeSelf()
                    .register(),
            LuV, ZPM, UV);

    public static final MultiblockMachineDefinition[] FLUID_DRILLING_RIG = registerTieredMultis(
            "fluid_drilling_rig", FluidDrillMachine::new, (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .langValue("%s Fluid Drilling Rig %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(DUMMY_RECIPES)
                    .tooltips(
                            Component.translatable("gtceu.machine.fluid_drilling_rig.description"),
                            Component.translatable("gtceu.machine.fluid_drilling_rig.depletion",
                                    FormattingUtil.formatNumbers(100.0 / FluidDrillMachine.getDepletionChance(tier))),
                            Component.translatable("gtceu.universal.tooltip.energy_tier_range", GTValues.VNF[tier],
                                    GTValues.VNF[tier + 1]),
                            Component.translatable("gtceu.machine.fluid_drilling_rig.production",
                                    FluidDrillMachine.getRigMultiplier(tier),
                                    FormattingUtil.formatNumbers(FluidDrillMachine.getRigMultiplier(tier) * 1.5)))
                    .appearanceBlock(() -> FluidDrillMachine.getCasingState(tier))
                    .pattern((definition) -> FactoryBlockPattern.start()
                            .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                            .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .where('S', controller(blocks(definition.get())))
                            .where('X', blocks(FluidDrillMachine.getCasingState(tier)).setMinGlobalLimited(3)
                                    .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                            .setMaxGlobalLimited(2))
                                    .or(abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1)))
                            .where('C', blocks(FluidDrillMachine.getCasingState(tier)))
                            .where('F', blocks(FluidDrillMachine.getFrameState(tier)))
                            .where('#', any())
                            .build())
                    .workableCasingRenderer(FluidDrillMachine.getBaseTexture(tier),
                            GTCEu.id("block/multiblock/fluid_drilling_rig"))
                    .compassSections(GTCompassSections.TIER[MV])
                    .compassNode("fluid_drilling_rig")
                    .register(),
            MV, HV, EV);

    public static final MultiblockMachineDefinition[] LARGE_MINER = registerTieredMultis("large_miner",
            (holder, tier) -> new LargeMinerMachine(holder, tier, 64 / tier, 2 * tier - 5, tier, 8 - (tier - 5)),
            (tier, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .langValue("%s Large Miner %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeType(GTRecipeTypes.MACERATOR_RECIPES)
                    .appearanceBlock(() -> LargeMinerMachine.getCasingState(tier))
                    .pattern((definition) -> FactoryBlockPattern.start()
                            .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                            .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                            .where('S', controller(blocks(definition.getBlock())))
                            .where('X', blocks(LargeMinerMachine.getCasingState(tier))
                                    .or(abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1).setPreviewCount(1))
                                    .or(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1).setPreviewCount(1))
                                    .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                            .setMaxGlobalLimited(2).setPreviewCount(1)))
                            .where('C', blocks(LargeMinerMachine.getCasingState(tier)))
                            .where('F', frames(LargeMinerMachine.getMaterial(tier)))
                            .where('#', any())
                            .build())
                    .allowExtendedFacing(false)
                    .renderer(() -> new LargeMinerRenderer(
                            MinerRenderer.MATERIALS_TO_CASING_MODELS.get(LargeMinerMachine.getMaterial(tier)),
                            GTCEu.id("block/multiblock/large_miner")))
                    .tooltips(
                            Component.translatable("gtceu.machine.large_miner.%s.tooltip"
                                    .formatted(VN[tier].toLowerCase(Locale.ROOT))),
                            Component.translatable("gtceu.machine.miner.multi.description"))
                    .tooltipBuilder((stack, tooltip) -> {
                        int workingAreaChunks = (2 * tier - 5) * 2 / LargeMinerMachine.CHUNK_LENGTH;
                        tooltip.add(Component.translatable("gtceu.machine.miner.multi.modes"));
                        tooltip.add(Component.translatable("gtceu.machine.miner.multi.production"));
                        tooltip.add(Component.translatable("gtceu.machine.miner.fluid_usage", 8 - (tier - 5),
                                DrillingFluid.getLocalizedName()));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.working_area_chunks",
                                workingAreaChunks, workingAreaChunks));
                        tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_tier_range",
                                GTValues.VNF[tier], GTValues.VNF[tier + 1]));
                    })
                    .compassSections(GTCompassSections.TIER[EV])
                    .compassNode("large_miner")
                    .register(),
            EV, IV, LuV);

    public static MultiblockMachineDefinition[] BEDROCK_ORE_MINER;

    public static final MultiblockMachineDefinition CLEANROOM = REGISTRATE
            .multiblock("cleanroom", CleanroomMachine::new)
            .rotationState(RotationState.NONE)
            .recipeType(DUMMY_RECIPES)
            .appearanceBlock(PLASTCRETE)
            .tooltips(Component.translatable("gtceu.machine.cleanroom.tooltip.0"),
                    Component.translatable("gtceu.machine.cleanroom.tooltip.1"),
                    Component.translatable("gtceu.machine.cleanroom.tooltip.2"),
                    Component.translatable("gtceu.machine.cleanroom.tooltip.3"))
            .tooltipBuilder((stack, tooltip) -> {
                if (GTUtil.isCtrlDown()) {
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.4"));
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.5"));
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.6"));
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.7"));
                    // tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.8"));
                    if (GTCEu.isAE2Loaded()) {
                        tooltip.add(
                                Component.translatable(AEConfig.instance().getChannelMode() == ChannelMode.INFINITE ?
                                        "gtceu.machine.cleanroom.tooltip.ae2.no_channels" :
                                        "gtceu.machine.cleanroom.tooltip.ae2.channels"));
                    }
                    tooltip.add(Component.empty());
                } else {
                    tooltip.add(Component.translatable("gtceu.machine.cleanroom.tooltip.hold_ctrl"));
                }
            })
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "X   X", "X   X", "X   X", "XFFFX")
                    .aisle("XXXXX", "X   X", "X   X", "X   X", "XFSFX")
                    .aisle("XXXXX", "X   X", "X   X", "X   X", "XFFFX")
                    .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .where('X', blocks(GTBlocks.PLASTCRETE.get())
                            .or(blocks(GTBlocks.CLEANROOM_GLASS.get()))
                            .or(abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30, 3))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3, 2))
                            .or(blocks(ConfigHolder.INSTANCE.machines.enableMaintenance ?
                                    GTMachines.MAINTENANCE_HATCH.getBlock() : PLASTCRETE.get()).setExactLimit(1))
                            .or(blocks(Blocks.IRON_DOOR).setMaxGlobalLimited(8)))
                    .where('S', controller(blocks(definition.getBlock())))
                    .where(' ', any())
                    .where('E', abilities(PartAbility.INPUT_ENERGY))
                    .where('F', cleanroomFilters())
                    .where('I', abilities(PartAbility.PASSTHROUGH_HATCH))
                    .build())
            .shapeInfos((controller) -> {
                ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX")
                        .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                        .aisle("XXXXX", "X   X", "G   G", "X   X", "XFSFX")
                        .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                        .aisle("XMXEX", "XXOXX", "XXRXX", "XXXXX", "XXXXX")
                        .where('X', GTBlocks.PLASTCRETE)
                        .where('G', GTBlocks.CLEANROOM_GLASS)
                        .where('S', GTMachines.CLEANROOM.getBlock())
                        .where(' ', Blocks.AIR)
                        .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('I', GTMachines.ITEM_PASSTHROUGH_HATCH[GTValues.LV], Direction.NORTH)
                        .where('L', GTMachines.FLUID_PASSTHROUGH_HATCH[GTValues.LV], Direction.NORTH)
                        .where('H', GTMachines.HULL[GTValues.HV], Direction.NORTH)
                        .where('D', GTMachines.DIODE[GTValues.HV], Direction.NORTH)
                        .where('O',
                                Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER))
                        .where('R', Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH)
                                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
                if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
                    builder.where('M', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH);
                } else {
                    builder.where('M', GTBlocks.PLASTCRETE.get());
                }
                GTCEuAPI.CLEANROOM_FILTERS.values()
                        .forEach(block -> shapeInfo.add(builder.where('F', block.get()).build()));
                return shapeInfo;
            })
            .allowExtendedFacing(false)
            .allowFlip(false)
            .workableCasingRenderer(GTCEu.id("block/casings/cleanroom/plascrete"),
                    GTCEu.id("block/multiblock/cleanroom"))
            .compassSections(GTCompassSections.TIER[HV])
            .compassNodeSelf()
            .register();

    public static final MultiblockMachineDefinition LARGE_COMBUSTION_ENGINE = registerLargeCombustionEngine(
            "large_combustion_engine", EV,
            CASING_TITANIUM_STABLE, CASING_TITANIUM_GEARBOX, CASING_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
            GTCEu.id("block/multiblock/generator/large_combustion_engine"));

    public static final MultiblockMachineDefinition EXTREME_COMBUSTION_ENGINE = registerLargeCombustionEngine(
            "extreme_combustion_engine", IV,
            CASING_TUNGSTENSTEEL_ROBUST, CASING_TUNGSTENSTEEL_GEARBOX, CASING_EXTREME_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
            GTCEu.id("block/multiblock/generator/extreme_combustion_engine"));

    public static final MultiblockMachineDefinition LARGE_STEAM_TURBINE = registerLargeTurbine("steam_large_turbine",
            HV,
            GTRecipeTypes.STEAM_TURBINE_FUELS,
            CASING_STEEL_TURBINE, CASING_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"),
            GTCEu.id("block/multiblock/generator/large_steam_turbine"));

    public static final MultiblockMachineDefinition LARGE_GAS_TURBINE = registerLargeTurbine("gas_large_turbine", EV,
            GTRecipeTypes.GAS_TURBINE_FUELS,
            CASING_STAINLESS_TURBINE, CASING_STAINLESS_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"),
            GTCEu.id("block/multiblock/generator/large_gas_turbine"));

    public static final MultiblockMachineDefinition LARGE_PLASMA_TURBINE = registerLargeTurbine("plasma_large_turbine",
            IV,
            GTRecipeTypes.PLASMA_GENERATOR_FUELS,
            CASING_TUNGSTENSTEEL_TURBINE, CASING_TUNGSTENSTEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"),
            GTCEu.id("block/multiblock/generator/large_plasma_turbine"));

    public static final MultiblockMachineDefinition ACTIVE_TRANSFORMER = REGISTRATE
            .multiblock("active_transformer", ActiveTransformerMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(HIGH_POWER_CASING)
            .tooltips(Component.translatable("gtceu.machine.active_transformer.tooltip.0"),
                    Component.translatable("gtceu.machine.active_transformer.tooltip.1"))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.active_transformer.tooltip.2")
                             .append(Component.translatable("gtceu.machine.active_transformer.tooltip.3")
                                     .withStyle(style -> style.withColor(TooltipHelper.RAINBOW_SLOW.getCurrent())))))
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XCX", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(GTBlocks.HIGH_POWER_CASING.get()).setMinGlobalLimited(12)
                            .or(ActiveTransformerMachine.getHatchPredicates()))
                    .where('C', blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/hpca/high_power_casing"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    public static final MultiblockMachineDefinition POWER_SUBSTATION = REGISTRATE
            .multiblock("power_substation", PowerSubstationMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("gtceu.machine.power_substation.tooltip.0"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.1"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.2",
                            PowerSubstationMachine.MAX_BATTERY_LAYERS),
                    Component.translatable("gtceu.machine.power_substation.tooltip.3"),
                    Component.translatable("gtceu.machine.power_substation.tooltip.4",
                            PowerSubstationMachine.PASSIVE_DRAIN_MAX_PER_STORAGE / 1000))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.power_substation.tooltip.5")
                             .append(Component.translatable("gtceu.machine.power_substation.tooltip.6")
                                     .withStyle(style -> style.withColor(TooltipHelper.RAINBOW_SLOW.getCurrent())))))
            .appearanceBlock(CASING_PALLADIUM_SUBSTATION)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                    .aisle("XXXXX", "XCCCX", "XCCCX", "XCCCX", "XXXXX")
                    .aisle("GGGGG", "GBBBG", "GBBBG", "GBBBG", "GGGGG")
                    .setRepeatable(1, PowerSubstationMachine.MAX_BATTERY_LAYERS)
                    .aisle("GGGGG", "GGGGG", "GGGGG", "GGGGG", "GGGGG")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_PALLADIUM_SUBSTATION.get()))
                    .where('X',
                            blocks(CASING_PALLADIUM_SUBSTATION.get())
                                    .setMinGlobalLimited(PowerSubstationMachine.MIN_CASINGS)
                                    .or(autoAbilities(true, false, false))
                                    .or(abilities(PartAbility.INPUT_ENERGY, PartAbility.SUBSTATION_INPUT_ENERGY,
                                            PartAbility.INPUT_LASER).setMinGlobalLimited(1))
                                    .or(abilities(PartAbility.OUTPUT_ENERGY, PartAbility.SUBSTATION_OUTPUT_ENERGY,
                                            PartAbility.OUTPUT_LASER).setMinGlobalLimited(1)))
                    .where('G', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('B', Predicates.powerSubstationBatteries())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("ICSCO", "NCMCT", "GGGGG", "GGGGG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GBBBG", "GBBBG", "GGGGG")
                        .aisle("CCCCC", "CCCCC", "GGGGG", "GGGGG", "GGGGG")
                        .where('S', definition, Direction.NORTH)
                        .where('C', CASING_PALLADIUM_SUBSTATION)
                        .where('G', CASING_LAMINATED_GLASS)
                        .where('I', GTMachines.ENERGY_INPUT_HATCH[HV], Direction.NORTH)
                        .where('N', GTMachines.SUBSTATION_ENERGY_INPUT_HATCH[EV], Direction.NORTH)
                        .where('O', GTMachines.ENERGY_OUTPUT_HATCH[HV], Direction.NORTH)
                        .where('T', GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH[EV], Direction.NORTH)
                        .where('M',
                                ConfigHolder.INSTANCE.machines.enableMaintenance ?
                                        GTMachines.MAINTENANCE_HATCH.getBlock().defaultBlockState().setValue(
                                                GTMachines.MAINTENANCE_HATCH.get().getRotationState().property,
                                                Direction.NORTH) :
                                        CASING_PALLADIUM_SUBSTATION.get().defaultBlockState());

                GTCEuAPI.PSS_BATTERIES.entrySet().stream()
                        // filter out empty batteries in example structures, though they are still
                        // allowed in the predicate (so you can see them on right-click)
                        .filter(entry -> entry.getKey().getCapacity() > 0)
                        .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                        .forEach(entry -> shapeInfo.add(builder.where('B', entry.getValue().get()).build()));

                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_palladium_substation"),
                    GTCEu.id("block/multiblock/power_substation"))
            .register();

    public static final MultiblockMachineDefinition CHARCOAL_PILE_IGNITER = REGISTRATE
            .multiblock("charcoal_pile_igniter", CharcoalPileIgniterMachine::new)
            .rotationState(RotationState.NONE)
            .recipeType(DUMMY_RECIPES)
            .appearanceBlock(BRONZE_HULL)
            .pattern((def) -> FactoryBlockPattern.start()
                    .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                    .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                    .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DSD ")
                    .aisle(" BBB ", "XCCCX", "XCCCX", "XCCCX", " DDD ")
                    .aisle("     ", " XXX ", " XXX ", " XXX ", "     ")
                    .where('S', controller(blocks(def.getBlock())))
                    .where('B', blocks(Blocks.BRICKS))
                    .where('X', blocks(Blocks.DIRT))
                    .where('D', blocks(Blocks.DIRT))
                    .where('C', blocks(Blocks.OAK_LOG))
                    .build())
            .allowFlip(false)
            .allowExtendedFacing(false)
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/multiblock/charcoal_pile_igniter"))
            .register();

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////
    public static Pair<MachineDefinition, MachineDefinition> registerSteamMachines(String name,
                                                                                   BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
                                                                                   BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        MachineDefinition lowTier = builder.apply(false,
                REGISTRATE.machine("lp_%s".formatted(name), holder -> factory.apply(holder, false))
                        .langValue("Low Pressure " + FormattingUtil.toEnglishName(name))
                        .compassSections(GTCompassSections.STEAM)
                        .compassNode(name)
                        .compassPreNodes(GTCompassNodes.STEAM)
                        .tier(0));
        MachineDefinition highTier = builder.apply(true,
                REGISTRATE.machine("hp_%s".formatted(name), holder -> factory.apply(holder, true))
                        .langValue("High Pressure " + FormattingUtil.toEnglishName(name))
                        .compassSections(GTCompassSections.STEAM)
                        .compassNode(name)
                        .compassPreNodes(GTCompassNodes.STEAM)
                        .tier(1));
        return Pair.of(lowTier, highTier);
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    private static MachineDefinition[] registerFluidHatches(String name, String displayname, String model,
                                                            String tooltip, IO io, int initialCapacity, int slots,
                                                            int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new FluidHatchPartMachine(holder, tier, io, initialCapacity, slots),
                (tier, builder) -> {
                    builder.langValue(VNF[tier] + ' ' + displayname)
                            .rotationState(RotationState.ALL)
                            .overlayTieredHullRenderer(model)
                            .abilities(abilities)
                            .compassNode("fluid_hatch")
                            .tooltips(Component.translatable("gtceu.machine." + tooltip + ".tooltip"));

                    if (slots == 1) {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil
                                        .formatNumbers(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier))));
                    } else {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                slots, FormattingUtil
                                        .formatNumbers(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier))));
                    }

                    return builder.register();
                },
                tiers);
    }

    public static MachineDefinition[] registerTransformerMachines(String langName, int baseAmp) {
        return registerTieredMachines("transformer_%da".formatted(baseAmp),
                (holder, tier) -> new TransformerMachine(holder, tier, baseAmp),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier + 1] :
                                index == 3 ? GTValues.VC[tier] :
                                        index == 1 ? Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor)
                                                .intValue() : -1)
                        .renderer(() -> new TransformerRenderer(tier, baseAmp))
                        .langValue("%s %sTransformer".formatted(VOLTAGE_NAMES[tier], langName))
                        .tooltips(Component.translatable("gtceu.machine.transformer.description"),
                                Component.translatable("gtceu.machine.transformer.tooltip_tool_usage"),
                                Component.translatable("gtceu.machine.transformer.tooltip_transform_down",
                                        baseAmp, FormattingUtil.formatNumbers(GTValues.V[tier + 1]),
                                        GTValues.VNF[tier + 1],
                                        baseAmp * 4, FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.machine.transformer.tooltip_transform_up",
                                        baseAmp * 4, FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier],
                                        baseAmp, FormattingUtil.formatNumbers(GTValues.V[tier + 1]),
                                        GTValues.VNF[tier + 1]))
                        .compassNode("transformer")
                        .register(),
                GTValues.tiersBetween(ULV, GTCEuAPI.isHighTier() ? OpV : UV)); // UHV not needed, as a UV transformer
                                                                               // transforms up to UHV
    }

    public static MachineDefinition[] registerSimpleMachines(String name,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             boolean hasPollutionDebuff,
                                                             int... tiers) {
        return registerTieredMachines(name,
                (holder, tier) -> new SimpleTieredMachine(holder, tier, tankScalingFunction), (tier, builder) -> {
                    if (hasPollutionDebuff) {
                        builder.recipeModifiers(GTRecipeModifiers.ENVIRONMENT_REQUIREMENT
                                .apply(GTMedicalConditions.CARBON_MONOXIDE_POISONING, 100 * tier),
                                GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                                .tooltips(defaultEnvironmentRequirement());
                    } else {
                        builder.recipeModifier(
                                GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK));
                    }
                    return builder
                            .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                            .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                            .rotationState(RotationState.NON_Y_AXIS)
                            .recipeType(recipeType)
                            .workableTieredHullRenderer(GTCEu.id("block/machines/" + name))
                            .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                    tankScalingFunction.apply(tier), true))
                            .compassNode(name)
                            .register();
                },
                tiers);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             boolean hasPollutionDebuff) {
        return registerSimpleMachines(name, recipeType, tankScalingFunction, hasPollutionDebuff, ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction) {
        return registerSimpleMachines(name, recipeType, tankScalingFunction, false);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType) {
        return registerSimpleMachines(name, recipeType, defaultTankSizeFunction);
    }

    public static MachineDefinition[] registerSimpleGenerator(String name,
                                                              GTRecipeType recipeType,
                                                              Int2IntFunction tankScalingFunction,
                                                              float hazardStrengthPerOperation,
                                                              int... tiers) {
        return registerTieredMachines(name,
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, hazardStrengthPerOperation * tier,
                        tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                        .addOutputLimit(ItemRecipeCapability.CAP, 0)
                        .addOutputLimit(FluidRecipeCapability.CAP, 0)
                        .renderer(() -> new SimpleGeneratorMachineRenderer(tier, GTCEu.id("block/generators/" + name)))
                        .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType,
                                tankScalingFunction.apply(tier), false))
                        .compassNode(name)
                        .register(),
                tiers);
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSimpleSteamMachines(String name,
                                                                                         GTRecipeType recipeType) {
        return registerSteamMachines("steam_" + name, SimpleSteamMachine::new, (pressure, builder) -> builder
                .rotationState(RotationState.ALL)
                .recipeType(recipeType)
                .recipeModifier(SimpleSteamMachine::recipeModifier)
                .renderer(() -> new WorkableSteamMachineRenderer(pressure, GTCEu.id("block/machines/" + name)))
                .register());
    }

    public static MachineDefinition[] registerBatteryBuffer(int batterySlotSize) {
        return registerTieredMachines("battery_buffer_" + batterySlotSize + "x",
                (holder, tier) -> new BatteryBufferMachine(holder, tier, batterySlotSize),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .renderer(() -> new BatteryBufferRenderer(tier, batterySlotSize))
                        .langValue("%s %s%s".formatted(VOLTAGE_NAMES[tier], batterySlotSize, "x Battery Buffer"))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                        batterySlotSize),
                                Component.translatable("gtceu.universal.tooltip.voltage_in_out",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in_till",
                                        batterySlotSize * BatteryBufferMachine.AMPS_PER_BATTERY),
                                Component.translatable("gtceu.universal.tooltip.amperage_out_till", batterySlotSize))
                        .compassNode("battery_buffer")
                        .register(),
                ALL_TIERS);
    }

    public static MachineDefinition[] registerCharger(int itemSlotSize) {
        return registerTieredMachines("charger_" + itemSlotSize + "x",
                (holder, tier) -> new ChargerMachine(holder, tier, itemSlotSize),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .renderer(() -> new ChargerRenderer(tier))
                        .langValue("%s %s%s".formatted(VOLTAGE_NAMES[tier], itemSlotSize, "x Turbo Charger"))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity", itemSlotSize),
                                Component.translatable("gtceu.universal.tooltip.voltage_in_out",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in_till",
                                        itemSlotSize * ChargerMachine.AMPS_PER_ITEM))
                        .compassNode("charger")
                        .register(),
                ALL_TIERS);
    }

    public static MachineDefinition[] registerLaserHatch(IO io, int amperage, PartAbility ability) {
        String name = io == IO.IN ? "target" : "source";
        return registerTieredMachines(amperage + "a_laser_" + name + "_hatch",
                (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage), (tier, builder) -> builder
                        .langValue(VNF[tier] + " " + FormattingUtil.formatNumbers(amperage) + "A Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .tooltips(Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"),
                                Component.translatable("gtceu.machine.laser_hatch.both.tooltip"),
                                Component.translatable("gtceu.universal.disabled"))
                        .abilities(ability)
                        .overlayTieredHullRenderer("laser_hatch." + name)
                        .register(),
                HIGH_TIERS);
    }

    public static MultiblockMachineDefinition[] registerTieredMultis(String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MultiblockControllerMachine> factory,
                                                                     BiFunction<Integer, MultiblockMachineBuilder, MultiblockMachineDefinition> builder,
                                                                     int... tiers) {
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .multiblock(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    private static MachineDefinition registerTankValve(
                                                       String name, String displayName, boolean isMetal,
                                                       BiConsumer<MachineBuilder<?>, ResourceLocation> rendererSetup) {
        MachineBuilder<MachineDefinition> builder = REGISTRATE
                .machine(name, holder -> new TankValvePartMachine(holder, isMetal))
                .langValue(displayName)
                .tooltips(Component.translatable("gtceu.machine.tank_valve.tooltip"))
                .rotationState(RotationState.ALL)
                .compassSections(GTCompassSections.MULTIBLOCK)
                .compassNode("tank_valve");

        rendererSetup.accept(builder, GTCEu.id("block/multiblock/tank_valve"));
        return builder.register();
    }

    private static MultiblockMachineDefinition registerMultiblockTank(
                                                                      String name, String displayName, int capacity,
                                                                      Supplier<? extends Block> casing,
                                                                      Supplier<? extends Block> valve,
                                                                      @Nullable PropertyFluidFilter filter,
                                                                      BiConsumer<MultiblockMachineBuilder, ResourceLocation> rendererSetup) {
        MultiblockMachineBuilder builder = REGISTRATE
                .multiblock(name, holder -> new MultiblockTankMachine(holder, capacity, filter))
                .langValue(displayName)
                .tooltips(
                        Component.translatable("gtceu.machine.multiblock.tank.tooltip"),
                        Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", capacity))
                .rotationState(RotationState.ALL)
                .recipeType(DUMMY_RECIPES)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("CCC", "CCC", "CCC")
                        .aisle("CCC", "C#C", "CCC")
                        .aisle("CCC", "CSC", "CCC")
                        .where('S', controller(blocks(definition.get())))
                        .where('C', blocks(casing.get())
                                .or(blocks(valve.get()).setMaxGlobalLimited(2, 0)))
                        .where('#', air())
                        .build())

                .appearanceBlock(casing)
                .compassSections(GTCompassSections.MULTIBLOCK)
                .compassNode("multiblock_tank");

        rendererSetup.accept(builder, GTCEu.id("block/multiblock/multiblock_tank"));
        return builder.register();
    }

    public static MultiblockMachineDefinition registerLargeBoiler(String name, Supplier<? extends Block> casing,
                                                                  Supplier<? extends Block> pipe,
                                                                  Supplier<? extends Block> fireBox,
                                                                  ResourceLocation texture, BoilerFireboxType firebox,
                                                                  int maxTemperature, int heatSpeed) {
        return REGISTRATE
                .multiblock("%s_large_boiler".formatted(name),
                        holder -> new LargeBoilerMachine(holder, maxTemperature, heatSpeed))
                .langValue("Large %s Boiler".formatted(FormattingUtil.toEnglishName(name)))
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.LARGE_BOILER_RECIPES)
                .recipeModifier(LargeBoilerMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .partAppearance((controller, part,
                                 side) -> controller.self().getPos().below().getY() == part.self().getPos().getY() ?
                                         fireBox.get().defaultBlockState() : casing.get().defaultBlockState())
                .pattern((definition) -> {
                    TraceabilityPredicate fireboxPred = blocks(ALL_FIREBOXES.get(firebox).get()).setMinGlobalLimited(3)
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1));

                    if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
                        fireboxPred = fireboxPred.or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1));
                    }

                    return FactoryBlockPattern.start()
                            .aisle("XXX", "CCC", "CCC", "CCC")
                            .aisle("XXX", "CPC", "CPC", "CCC")
                            .aisle("XXX", "CSC", "CCC", "CCC")
                            .where('S', Predicates.controller(blocks(definition.getBlock())))
                            .where('P', blocks(pipe.get()))
                            .where('X', fireboxPred)
                            .where('C', blocks(casing.get()).setMinGlobalLimited(20)
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)
                                            .setPreviewCount(1)))
                            .build();
                })
                .recoveryItems(
                        () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .renderer(() -> new LargeBoilerRenderer(texture, firebox,
                        GTCEu.id("block/multiblock/generator/large_%s_boiler".formatted(name))))
                .tooltips(
                        Component.translatable("gtceu.multiblock.large_boiler.max_temperature",
                                (int) (maxTemperature + 274.15), maxTemperature),
                        Component.translatable("gtceu.multiblock.large_boiler.heat_time_tooltip",
                                maxTemperature / heatSpeed / 20),
                        Component.translatable("gtceu.multiblock.large_boiler.explosion_tooltip")
                                .withStyle(ChatFormatting.DARK_RED))
                .compassSections(GTCompassSections.STEAM)
                .compassNode("large_boiler")
                .register();
    }

    public static MultiblockMachineDefinition registerLargeCombustionEngine(String name, int tier,
                                                                            Supplier<? extends Block> casing,
                                                                            Supplier<? extends Block> gear,
                                                                            Supplier<? extends Block> intake,
                                                                            ResourceLocation casingTexture,
                                                                            ResourceLocation overlayModel) {
        return REGISTRATE.multiblock(name, holder -> new LargeCombustionEngineMachine(holder, tier))
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                .generator(true)
                .recipeModifier(LargeCombustionEngineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("XXX", "XDX", "XXX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("AAA", "AYA", "AAA")
                        .where('X', blocks(casing.get()))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()).setMinGlobalLimited(3)
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .where('D',
                                ability(PartAbility.OUTPUT_ENERGY,
                                        Stream.of(ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV).filter(t -> t >= tier)
                                                .mapToInt(Integer::intValue).toArray())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                GTValues.VN[tier])))
                        .where('A',
                                blocks(intake.get())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_1")))
                        .where('Y', controller(blocks(definition.getBlock())))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingRenderer(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier]),
                        Component.translatable("gtceu.universal.tooltip.uses_per_hour_lubricant",
                                FluidType.BUCKET_VOLUME),
                        tier > EV ?
                                Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_extreme",
                                        V[tier] * 4) :
                                Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_regular",
                                        V[tier] * 3))
                .compassSections(GTCompassSections.TIER[EV])
                .compassNode("large_combustion")
                .register();
    }

    public static MultiblockMachineDefinition registerLargeTurbine(String name, int tier, GTRecipeType recipeType,
                                                                   Supplier<? extends Block> casing,
                                                                   Supplier<? extends Block> gear,
                                                                   ResourceLocation casingTexture,
                                                                   ResourceLocation overlayModel) {
        return REGISTRATE.multiblock(name, holder -> new LargeTurbineMachine(holder, tier))
                .rotationState(RotationState.ALL)
                .recipeType(recipeType)
                .generator(true)
                .recipeModifier(LargeTurbineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("CCCC", "CHHC", "CCCC")
                        .aisle("CHHC", "RGGR", "CHHC")
                        .aisle("CCCC", "CSHC", "CCCC")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()))
                        .where('R',
                                new TraceabilityPredicate(
                                        new SimplePredicate(
                                                state -> MetaMachine.getMachine(state.getWorld(),
                                                        state.getPos()) instanceof IRotorHolderMachine rotorHolder &&
                                                        state.getWorld()
                                                                .getBlockState(state.getPos()
                                                                        .relative(rotorHolder.self().getFrontFacing()))
                                                                .isAir(),
                                                () -> PartAbility.ROTOR_HOLDER.getAllBlocks().stream()
                                                        .map(BlockInfo::fromBlock).toArray(BlockInfo[]::new)))
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                VN[tier]))
                                        .setExactLimit(1)
                                        .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                        .where('H', blocks(casing.get())
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingRenderer(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier] * 2),
                        Component.translatable("gtceu.multiblock.turbine.efficiency_tooltip", VNF[tier]))
                .compassSections(GTCompassSections.TIER[HV])
                .compassNode("large_turbine")
                .register();
    }

    public static MachineDefinition registerCrate(Material material, int capacity, String lang) {
        boolean wooden = material.hasProperty(PropertyKey.WOOD);

        return REGISTRATE.machine(material.getName() + "_crate", holder -> new CrateMachine(holder, material, capacity))
                .langValue(lang)
                .rotationState(RotationState.NONE)
                .tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity", capacity))
                .renderer(() -> new CrateRenderer(
                        GTCEu.id("block/machine/crate/" + (wooden ? "wooden" : "metal") + "_crate")))
                .paintingColor(wooden ? 0xFFFFFF : material.getMaterialRGB())
                .itemColor((s, t) -> wooden ? 0xFFFFFF : material.getMaterialRGB())
                .compassNode("crate")
                .register();
    }

    public static MachineDefinition registerDrum(Material material, int capacity, String lang) {
        boolean wooden = material.hasProperty(PropertyKey.WOOD);
        var definition = REGISTRATE
                .machine(material.getName() + "_drum", MachineDefinition::createDefinition,
                        holder -> new DrumMachine(holder, material, capacity), MetaMachineBlock::new,
                        (holder, prop) -> DrumMachineItem.create(holder, prop, material),
                        MetaMachineBlockEntity::createBlockEntity)
                .langValue(lang)
                .rotationState(RotationState.NONE)
                .renderer(
                        () -> new MachineRenderer(GTCEu.id("block/machine/" + (wooden ? "wooden" : "metal") + "_drum")))
                .tooltipBuilder(createTankTooltips("Fluid", material))
                .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"),
                        Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil.formatNumbers(capacity)))
                .paintingColor(wooden ? 0xFFFFFF : material.getMaterialRGB())
                .itemColor((s, i) -> wooden ? 0xFFFFFF : material.getMaterialRGB())
                .compassNode("drum")
                .register();
        DRUM_CAPACITY.put(definition, capacity);
        return definition;
    }

    public static MachineDefinition[] registerConverter(int amperage) {
        return registerTieredMachines(amperage + "a_energy_converter",
                (holder, tier) -> new ConverterMachine(holder, tier, amperage),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .langValue("%s %sA Energy Converter".formatted(VN[tier], amperage))
                        .renderer(() -> new ConverterRenderer(tier, amperage))
                        .tooltips(Component.translatable("gtceu.machine.energy_converter.description"),
                                Component.translatable("gtceu.machine.energy_converter.tooltip_tool_usage"),
                                Component.translatable("gtceu.machine.energy_converter.tooltip_conversion_native",
                                        FeCompat.toFeLong(V[tier] * amperage,
                                                FeCompat.ratio(true)),
                                        amperage, V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.machine.energy_converter.tooltip_conversion_eu", amperage,
                                        V[tier], GTValues.VNF[tier],
                                        FeCompat.toFeLong(V[tier] * amperage,
                                                FeCompat.ratio(false))))
                        .compassNode("converter")
                        .register(),
                ALL_TIERS);
    }

    public static Component explosion() {
        if (ConfigHolder.INSTANCE.machines.doTerrainExplosion)
            return Component.translatable("gtceu.universal.tooltip.terrain_resist");
        return null;
    }

    public static Component environmentRequirement(MedicalCondition condition) {
        return Component.translatable("gtceu.recipe.environmental_hazard.reverse",
                Component.translatable("gtceu.medical_condition." + condition.name));
    }

    public static Component defaultEnvironmentRequirement() {
        return environmentRequirement(GTMedicalConditions.CARBON_MONOXIDE_POISONING);
    }

    public static Component[] workableTiered(int tier, long voltage, long energyCapacity, GTRecipeType recipeType,
                                             long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents
                .add(input ?
                        Component.translatable("gtceu.universal.tooltip.voltage_in",
                                FormattingUtil.formatNumbers(voltage), GTValues.VNF[tier]) :
                        Component.translatable("gtceu.universal.tooltip.voltage_out",
                                FormattingUtil.formatNumbers(voltage), GTValues.VNF[tier]));
        tooltipComponents
                .add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                        FormattingUtil.formatNumbers(energyCapacity)));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 ||
                recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents
                    .add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                            FormattingUtil.formatNumbers(tankCapacity)));
        return tooltipComponents.toArray(Component[]::new);
    }

    private static TraceabilityPredicate dataHatchPredicate(TraceabilityPredicate def) {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.INSTANCE.machines.enableResearch) {
            return abilities(PartAbility.DATA_ACCESS, PartAbility.OPTICAL_DATA_RECEPTION)
                    .setExactLimit(1)
                    .or(def);
        }
        return def;
    }

    public static void init() {
        GCYMMachines.init();
        GTResearchMachines.init();

        if (GTCEu.isCreateLoaded()) {
            GTCreateMachines.init();
        }
        if (GTCEu.isAE2Loaded()) {
            GTAEMachines.init();
        }
        if (ConfigHolder.INSTANCE.machines.doBedrockOres || Platform.isDevEnv()) {
            BEDROCK_ORE_MINER = registerTieredMultis(
                    "bedrock_ore_miner", BedrockOreMinerMachine::new, (tier, builder) -> builder
                            .rotationState(RotationState.NON_Y_AXIS)
                            .langValue("%s Bedrock Ore Miner %s".formatted(VLVH[tier], VLVT[tier]))
                            .recipeType(new GTRecipeType(GTCEu.id("bedrock_ore_miner"), "dummy"))
                            .tooltips(
                                    Component.translatable("gtceu.machine.bedrock_ore_miner.description"),
                                    Component.translatable("gtceu.machine.bedrock_ore_miner.depletion",
                                            FormattingUtil.formatNumbers(
                                                    100.0 / BedrockOreMinerMachine.getDepletionChance(tier))),
                                    Component.translatable("gtceu.universal.tooltip.energy_tier_range",
                                            GTValues.VNF[tier], GTValues.VNF[tier + 1]),
                                    Component.translatable("gtceu.machine.bedrock_ore_miner.production",
                                            BedrockOreMinerMachine.getRigMultiplier(tier),
                                            FormattingUtil.formatNumbers(
                                                    BedrockOreMinerMachine.getRigMultiplier(tier) * 1.5)))
                            .appearanceBlock(() -> BedrockOreMinerMachine.getCasingState(tier))
                            .pattern((definition) -> FactoryBlockPattern.start()
                                    .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                                    .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                                    .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                                    .where('S', controller(blocks(definition.get())))
                                    .where('X',
                                            blocks(BedrockOreMinerMachine.getCasingState(tier)).setMinGlobalLimited(3)
                                                    .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                                            .setMaxGlobalLimited(2))
                                                    .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1)))
                                    .where('C', blocks(BedrockOreMinerMachine.getCasingState(tier)))
                                    .where('F', blocks(BedrockOreMinerMachine.getFrameState(tier)))
                                    .where('#', any())
                                    .build())
                            .workableCasingRenderer(BedrockOreMinerMachine.getBaseTexture(tier),
                                    GTCEu.id("block/multiblock/bedrock_ore_miner"))
                            .register(),
                    MV, HV, EV);
        }
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.MACHINES.getRegistryName());
        }
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.MACHINES, MachineDefinition.class));
        GTRegistries.MACHINES.freeze();
    }

    public static MachineDefinition get(String name) {
        return GTRegistries.MACHINES.get(GTCEu.id(name));
    }
}
