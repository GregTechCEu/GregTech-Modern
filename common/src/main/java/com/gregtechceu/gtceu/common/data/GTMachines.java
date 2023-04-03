package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.DrumMachineItem;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.*;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.PumpMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CrackerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ElectricBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.*;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.CokeOvenMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.PrimitiveBlastFurnaceMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;
import com.gregtechceu.gtceu.common.machine.storage.CreativeEnergyContainerMachine;
import com.gregtechceu.gtceu.common.machine.storage.DrumMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PyrolyseOvenMachine;
import com.gregtechceu.gtceu.common.machine.steam.SteamLiquidBoilerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.data.LangHandler;
import com.gregtechceu.gtceu.integration.kjs.events.MachineEventJS;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluids;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.*;
import static com.gregtechceu.gtceu.utils.FormattingUtil.*;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote GTMachines
 */
public class GTMachines {
    public final static int[] ALL_TIERS = new int[] {GTValues.ULV, GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV};
    public final static int[] ELECTRIC_TIERS = new int[] {GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV};
    public final static int[] LOW_TIERS = new int[] {GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV};
    public final static int[] HIGH_TIERS = new int[] {GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV, GTValues.UHV};
    public static final Int2LongFunction defaultTankSizeFunction = tier -> (tier <= GTValues.LV ? 8 : tier == GTValues.MV ? 12 : tier == GTValues.HV ? 16 : tier == GTValues.EV ? 32 : 64) * FluidHelper.getBucket();
    public static final Int2LongFunction hvCappedTankSizeFunction = tier -> (tier <= GTValues.LV ? 8: tier == GTValues.MV ? 12 : 16) * FluidHelper.getBucket();
    public static final Int2LongFunction largeTankSizeFunction = tier -> (tier <= GTValues.LV ? 32 : tier == GTValues.MV ? 48 : 64) * FluidHelper.getBucket();
    public static final Int2LongFunction steamGeneratorTankSizeFunction = tier -> Math.min(16 * (1 << (tier - 1)), 64) * FluidHelper.getBucket();
    public static final Int2LongFunction genericGeneratorTankSizeFunction = tier -> Math.min(4 * (1 << (tier - 1)), 16) * FluidHelper.getBucket();
    public static Object2IntMap<MachineDefinition> DRUM_CAPACITY = new Object2IntArrayMap<>();

    static {
        REGISTRATE.creativeModeTab(() -> MACHINE);
    }
    //////////////////////////////////////
    //******     Steam Machine    ******//
    //////////////////////////////////////
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_SOLID_BOILER = registerSteamMachines("steam_solid_boiler",
            SteamSolidBoilerMachine::new,
            (pressure, builder) -> builder.rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .workableSteamHullRenderer(pressure, GTCEu.id("block/generators/boiler/coal"))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid", (pressure ? 300 : 120) * FluidHelper.getBucket() / 20000))
                    .register());

    public final static Pair<MachineDefinition, MachineDefinition> STEAM_LIQUID_BOILER = registerSteamMachines("steam_liquid_boiler",
            SteamLiquidBoilerMachine::new,
            (pressure, builder) -> builder.rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.STEAM_BOILER_RECIPES)
                    .workableSteamHullRenderer(pressure, GTCEu.id("block/generators/boiler/lava"))
                    .tooltips(Component.translatable("gtceu.universal.tooltip.produces_fluid", (pressure ? 600 : 240) * FluidHelper.getBucket() / 20000))
                    .register());

    public final static Pair<MachineDefinition, MachineDefinition> STEAM_EXTRACTOR = registerSimpleSteamMachines("extractor", GTRecipeTypes.EXTRACTOR_RECIPES);
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_MACERATOR = registerSimpleSteamMachines("macerator", GTRecipeTypes.MACERATOR_RECIPES);
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_COMPRESSOR = registerSimpleSteamMachines("compressor", GTRecipeTypes.COMPRESSOR_RECIPES);
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_HAMMER = registerSimpleSteamMachines("forge_hammer", GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_FURNACE = registerSimpleSteamMachines("furnace", GTRecipeTypes.FURNACE_RECIPES);
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_ALLOY_SMELTER = registerSimpleSteamMachines("alloy_smelter", GTRecipeTypes.ALLOY_SMELTER_RECIPES);
    public final static Pair<MachineDefinition, MachineDefinition> STEAM_ROCK_CRUSHER = registerSimpleSteamMachines("rock_crusher", GTRecipeTypes.ROCK_BREAKER_RECIPES);

    //////////////////////////////////////
    //***     SimpleTieredMachine    ***//
    //////////////////////////////////////
    public final static MachineDefinition[] HULL = registerTieredMachines("hull", TieredPartMachine::new, (tier, builder) -> builder
            .rotationState(RotationState.ALL)
            .overlayTieredHullRenderer("hull")
            .tooltips(Component.translatable("gtceu.machine.hull.tooltip"))
            .register(), ALL_TIERS);

    public final static MachineDefinition[] ELECTRIC_FURNACE = registerSimpleMachines("electric_furnace", GTRecipeTypes.FURNACE_RECIPES);
    public final static MachineDefinition[] ALLOY_SMELTER = registerSimpleMachines("alloy_smelter", GTRecipeTypes.ALLOY_SMELTER_RECIPES);
    public final static MachineDefinition[] ARC_FURNACE = registerSimpleMachines("arc_furnace", GTRecipeTypes.ARC_FURNACE_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] ASSEMBLER = registerSimpleMachines("assembler", GTRecipeTypes.ASSEMBLER_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] AUTOCLAVE = registerSimpleMachines("autoclave", GTRecipeTypes.AUTOCLAVE_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] BENDER = registerSimpleMachines("bender", GTRecipeTypes.BENDER_RECIPES);
    public final static MachineDefinition[] BREWERY = registerSimpleMachines("brewery", GTRecipeTypes.BREWING_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] CANNER = registerSimpleMachines("canner", GTRecipeTypes.CANNER_RECIPES);
    public final static MachineDefinition[] CENTRIFUGE = registerSimpleMachines("centrifuge", GTRecipeTypes.CENTRIFUGE_RECIPES, largeTankSizeFunction);
    public final static MachineDefinition[] CHEMICAL_BATH = registerSimpleMachines("chemical_bath", GTRecipeTypes.CHEMICAL_BATH_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] CHEMICAL_REACTOR = registerSimpleMachines("chemical_reactor", GTRecipeTypes.CHEMICAL_RECIPES);
    public final static MachineDefinition[] COMPRESSOR = registerSimpleMachines("compressor", GTRecipeTypes.COMPRESSOR_RECIPES);
    public final static MachineDefinition[] CUTTER = registerSimpleMachines("cutter", GTRecipeTypes.CUTTER_RECIPES);
    public final static MachineDefinition[] DISTILLERY = registerSimpleMachines("distillery", GTRecipeTypes.DISTILLERY_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] ELECTROLYZER = registerSimpleMachines("electrolyzer", GTRecipeTypes.ELECTROLYZER_RECIPES, largeTankSizeFunction);
    public final static MachineDefinition[] ELECTROMAGNETIC_SEPARATOR = registerSimpleMachines("electromagnetic_separator", GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES);
    public final static MachineDefinition[] EXTRACTOR = registerSimpleMachines("extractor", GTRecipeTypes.EXTRACTOR_RECIPES);
    public final static MachineDefinition[] EXTRUDER = registerSimpleMachines("extruder", GTRecipeTypes.EXTRUDER_RECIPES);
    public final static MachineDefinition[] FERMENTER = registerSimpleMachines("fermenter", GTRecipeTypes.FERMENTING_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] FLUID_HEATER = registerSimpleMachines("fluid_heater", GTRecipeTypes.FLUID_HEATER_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] FLUID_SOLIDIFIER = registerSimpleMachines("fluid_solidifier", GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] FORGE_HAMMER = registerSimpleMachines("forge_hammer", GTRecipeTypes.FORGE_HAMMER_RECIPES);
    public final static MachineDefinition[] FORMING_PRESS = registerSimpleMachines("forming_press", GTRecipeTypes.FORMING_PRESS_RECIPES);
    public final static MachineDefinition[] LATHE = registerSimpleMachines("lathe", GTRecipeTypes.LATHE_RECIPES);
    public final static MachineDefinition[] MIXER = registerSimpleMachines("mixer", GTRecipeTypes.MIXER_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] ORE_WASHER = registerSimpleMachines("ore_washer", GTRecipeTypes.ORE_WASHER_RECIPES);
    public final static MachineDefinition[] PACKER = registerSimpleMachines("packer", GTRecipeTypes.PACKER_RECIPES);
    public final static MachineDefinition[] POLARIZER = registerSimpleMachines("polarizer", GTRecipeTypes.POLARIZER_RECIPES);
    public final static MachineDefinition[] LASER_ENGRAVER = registerSimpleMachines("laser_engraver", GTRecipeTypes.LASER_ENGRAVER_RECIPES);
    public final static MachineDefinition[] SIFTER = registerSimpleMachines("sifter", GTRecipeTypes.SIFTER_RECIPES);
    public final static MachineDefinition[] THERMAL_CENTRIFUGE = registerSimpleMachines("thermal_centrifuge", GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES);
    public final static MachineDefinition[] WIREMILL = registerSimpleMachines("wiremill", GTRecipeTypes.WIREMILL_RECIPES);
    public final static MachineDefinition[] CIRCUIT_ASSEMBLER = registerSimpleMachines("circuit_assembler", GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES, hvCappedTankSizeFunction);
    public final static MachineDefinition[] MACERATOR = registerSimpleMachines("macerator", GTRecipeTypes.MACERATOR_RECIPES);
    public final static MachineDefinition[] GAS_COLLECTOR = registerSimpleMachines("gas_collector", GTRecipeTypes.GAS_COLLECTOR_RECIPES);
    public final static MachineDefinition[] ROCK_CRUSHER = registerSimpleMachines("rock_crusher", GTRecipeTypes.ROCK_BREAKER_RECIPES);

    //////////////////////////////////////
    //****     Simple Generator     ****//
    //////////////////////////////////////
    public final static MachineDefinition[] COMBUSTION = registerSimpleGenerator("combustion", GTRecipeTypes.COMBUSTION_GENERATOR_FUELS, genericGeneratorTankSizeFunction, GTValues.LV, GTValues.MV, GTValues.HV);
    public final static MachineDefinition[] STEAM_TURBINE = registerSimpleGenerator("steam_turbine", GTRecipeTypes.STEAM_TURBINE_FUELS, steamGeneratorTankSizeFunction, GTValues.LV, GTValues.MV, GTValues.HV);
    public final static MachineDefinition[] GAS_TURBINE = registerSimpleGenerator("gas_turbine", GTRecipeTypes.GAS_TURBINE_FUELS, genericGeneratorTankSizeFunction, GTValues.LV, GTValues.MV, GTValues.HV);


    //////////////////////////////////////
    //********     Electric     ********//
    //////////////////////////////////////
    public final static MachineDefinition[] TRANSFORMER = registerTieredMachines("transformer", TransformerMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new TransformerRenderer(tier))
                    .tooltips(explosion())
                    .tooltips(Component.translatable("gtceu.machine.transformer.description"),
                            Component.translatable("gtceu.machine.transformer.tooltip_tool_usage"),
                            Component.translatable("gtceu.machine.transformer.tooltip_transform_down", 4, GTValues.V[tier + 1], GTValues.VNF[tier + 1], 1, GTValues.V[tier], GTValues.VNF[tier]),
                            Component.translatable("gtceu.machine.transformer.tooltip_transform_up", 1, GTValues.V[tier], GTValues.VNF[tier], 4, GTValues.V[tier + 1], GTValues.VNF[tier + 1]))
                    .register(),
            GTValues.ULV, GTValues.LV, GTValues.MV, GTValues.HV, GTValues.EV, GTValues.IV, GTValues.LuV, GTValues.ZPM, GTValues.UV); // UHV not needed, as a UV transformer transforms up to UHV

    public final static MachineDefinition[] BATTERY_BUFFER_4 = registerBatteryBuffer(4);

    public final static MachineDefinition[] BATTERY_BUFFER_8 = registerBatteryBuffer(8);

    public final static MachineDefinition[] BATTERY_BUFFER_16 = registerBatteryBuffer(16);

    public final static MachineDefinition[] PUMP = registerTieredMachines("pump", PumpMachine::new,
            (tier, builder) -> builder
                    .rotationState(RotationState.NON_Y_AXIS)
                    .hasTESR(true)
                    .renderer(() -> new TieredHullMachineRenderer(tier, GTCEu.id("block/machine/pump_machine")))
                    .tooltips(Component.translatable("gtceu.machine.pump.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.voltage_in", GTValues.V[tier], GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", GTValues.V[tier] * 64),
                            Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 16 * FluidHelper.getBucket() * Math.max(1, tier)),
                            Component.translatable("gtceu.universal.tooltip.working_area", PumpMachine.BASE_PUMP_RANGE + PumpMachine.EXTRA_PUMP_RANGE * tier, PumpMachine.BASE_PUMP_RANGE + PumpMachine.EXTRA_PUMP_RANGE * tier))
                    .register(),
            LV, MV, HV, EV);

    //////////////////////////////////////
    //*********     Storage    *********//
    //////////////////////////////////////
    public final static MachineDefinition CREATIVE_ENERGY = REGISTRATE.machine("infinite_energy", CreativeEnergyContainerMachine::new)
            .rotationState(RotationState.NONE)
            .tooltips(Component.translatable("gtceu.creative_tooltip.1"),
                    Component.translatable("gtceu.creative_tooltip.2"),
                    Component.translatable("gtceu.creative_tooltip.3"))
            .register();

    public static BiConsumer<ItemStack, List<Component>> CHEST_TOOLTIPS = (stack, list) -> {
        if (stack.hasTag()) {
            ItemStack itemStack = ItemStack.of(stack.getOrCreateTagElement("stored"));
            int storedAmount = stack.getOrCreateTag().getInt("storedAmount");
            list.add(1, Component.translatable("gtceu.universal.tooltip.item_stored", itemStack.getDescriptionId(), storedAmount));
        }
    };

    public final static MachineDefinition[] SUPER_CHEST = registerTieredMachines("super_chest",
            (holder, tier) -> new QuantumChestMachine(holder, tier, 4000000 * (int) Math.pow(2, tier)),
            (tier, builder) -> builder
                    .langValue("Super Chest " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumChestRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(CHEST_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"), Component.translatable("gtceu.universal.tooltip.item_storage_total", 4000000 * (int) Math.pow(2, tier)))
                    .register(),
            LOW_TIERS);

    // TODO do we really need UHV+?
    public final static MachineDefinition[] QUANTUM_CHEST = registerTieredMachines("quantum_chest",
            (holder, tier) -> new QuantumChestMachine(holder, tier, tier == GTValues.UHV ? Integer.MAX_VALUE : 4000000 * (int) Math.pow(2, tier)),
            (tier, builder) -> builder
                    .langValue("Quantum Chest " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumChestRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(CHEST_TOOLTIPS)
                    .tooltips(Component.translatable("gtceu.machine.quantum_chest.tooltip"), Component.translatable("gtceu.universal.tooltip.item_storage_total", /*tier == GTValues.UHV ? Integer.MAX_VALUE :*/ 4000000 * (int) Math.pow(2, tier)))
                    .register(),
            HIGH_TIERS);

    public static BiConsumer<ItemStack, List<Component>> createTankTooltips(String nbtName) {
        return (stack, list) -> {
            if (stack.hasTag()) {
                FluidStack tank = FluidStack.loadFromTag(stack.getOrCreateTagElement(nbtName));
                list.add(1, Component.translatable("gtceu.universal.tooltip.fluid_stored", tank.getDisplayName(), tank.getAmount()));
            }
        };
    }

    public final static MachineDefinition[] SUPER_TANK = registerTieredMachines("super_tank",
            (holder, tier) -> new QuantumTankMachine(holder, tier, 4000 * FluidHelper.getBucket() * (int) Math.pow(2, tier)),
            (tier, builder) -> builder
                    .langValue("Super Tank " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumTankRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(createTankTooltips("stored"))
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"), Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",4000000 * (int) Math.pow(2, tier)))
                    .register(),
            LOW_TIERS);

    // TODO do we really need UHV+?
    public final static MachineDefinition[] QUANTUM_TANK = registerTieredMachines("quantum_tank",
            (holder, tier) -> new QuantumTankMachine(holder, tier, tier == GTValues.UHV ? Integer.MAX_VALUE : 4000 * FluidHelper.getBucket() * (int) Math.pow(2, tier)),
            (tier, builder) -> builder
                    .langValue("Quantum Tank " + LVT[tier + 1 - LOW_TIERS[0]])
                    .blockProp(BlockBehaviour.Properties::dynamicShape)
                    .rotationState(RotationState.ALL)
                    .renderer(() -> new QuantumTankRenderer(tier))
                    .hasTESR(true)
                    .tooltipBuilder(createTankTooltips("stored"))
                    .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"), Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", /*tier == GTValues.UHV ? Integer.MAX_VALUE :*/ 4000000 * (int) Math.pow(2, tier)))
                    .register(),
            HIGH_TIERS);

    public static MachineDefinition WOODEN_CRATE = registerCrate(GTMaterials.Wood, 27, "Wooden Crate");
    public static MachineDefinition BRONZE_CRATE = registerCrate(GTMaterials.Bronze, 54, "Bronze Crate");
    public static MachineDefinition STEEL_CRATE = registerCrate(GTMaterials.Steel, 72, "Steel Crate");
    public static MachineDefinition ALUMINIUM_CRATE = registerCrate(GTMaterials.Aluminium, 90, "Aluminium Crate");
    public static MachineDefinition STAINLESS_STEEL_CRATE = registerCrate(GTMaterials.StainlessSteel, 108, "Stainless Steel Crate");
    public static MachineDefinition TITANIUM_CRATE = registerCrate(GTMaterials.Titanium, 126, "Titanium Crate");
    public static MachineDefinition TUNGSTENSTEEL_CRATE = registerCrate(GTMaterials.TungstenSteel, 144, "Tungstensteel Crate");

    public static MachineDefinition WOODEN_DRUM = registerDrum(GTMaterials.Wood, (int) (16 * FluidHelper.getBucket()), "Wooden Barrel");
    public static MachineDefinition BRONZE_DRUM = registerDrum(GTMaterials.Bronze, (int) (32 * FluidHelper.getBucket()), "Bronze Drum");
    public static MachineDefinition STEEL_DRUM = registerDrum(GTMaterials.Steel, (int) (64 * FluidHelper.getBucket()), "Steel Drum");
    public static MachineDefinition ALUMINIUM_DRUM = registerDrum(GTMaterials.Aluminium, (int) (128 * FluidHelper.getBucket()), "Aluminium Drum");
    public static MachineDefinition STAINLESS_STEEL_DRUM = registerDrum(GTMaterials.StainlessSteel, (int) (256 * FluidHelper.getBucket()), "Stainless Steel Drum");


    //////////////////////////////////////
    //**********     Part     **********//
    //////////////////////////////////////
    public final static MachineDefinition[] ITEM_IMPORT_BUS = registerTieredMachines("item_bus.import",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue("Item Import Bus " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .abilities(tier == 0 ? new PartAbility[] {PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS} : new PartAbility[]{PartAbility.IMPORT_ITEMS})
                    .overlayTieredHullRenderer("item_bus.import")
                    .register(),
            ELECTRIC_TIERS);

    public final static MachineDefinition[] ITEM_EXPORT_BUS = registerTieredMachines("item_bus.export",
            (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue("Item Export Bus " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .abilities(tier == 0 ? new PartAbility[] {PartAbility.EXPORT_ITEMS, PartAbility.STEAM_EXPORT_ITEMS} : new PartAbility[]{PartAbility.EXPORT_ITEMS})
                    .overlayTieredHullRenderer("item_bus.export")
                    .register(),
            ELECTRIC_TIERS);

    public final static MachineDefinition[] FLUID_IMPORT_HATCH = registerTieredMachines("fluid_hatch.import",
            (holder, tier) -> new FluidHatchPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue("Fluid Import Hatch " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.IMPORT_FLUIDS)
                    .overlayTieredHullRenderer("fluid_hatch.import")
                    .register(),
            ELECTRIC_TIERS);

    public final static MachineDefinition[] FLUID_EXPORT_HATCH = registerTieredMachines("fluid_hatch.export",
            (holder, tier) -> new FluidHatchPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue("Fluid Export Hatch " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier] : -1)
                    .abilities(PartAbility.EXPORT_FLUIDS)
                    .overlayTieredHullRenderer("fluid_hatch.export")
                    .register(),
            ELECTRIC_TIERS);

    public final static MachineDefinition[] ENERGY_INPUT_HATCH = registerTieredMachines("energy_hatch.input",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 2),
            (tier, builder) -> builder
                    .langValue("Energy Import Hatch " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier] : -1)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .overlayTieredHullRenderer("energy_hatch.input")
                    .register(),
            ALL_TIERS);

    public final static MachineDefinition[] ENERGY_OUTPUT_HATCH = registerTieredMachines("energy_hatch.output",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 2),
            (tier, builder) -> builder
                    .langValue("Energy Export Hatch " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier] : -1)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .overlayTieredHullRenderer("energy_hatch.output")
                    .register(),
            ALL_TIERS);

    public final static MachineDefinition[] ENERGY_INPUT_HATCH_4A = registerTieredMachines("energy_hatch.input_4a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 4),
            (tier, builder) -> builder
                    .langValue("Energy Import Hatch (4A) " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier] : -1)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .overlayTieredHullRenderer("energy_hatch.input_4a")
                    .register(),
            EV, IV, LuV, ZPM, UV, UHV);

    public final static MachineDefinition[] ENERGY_OUTPUT_HATCH_4A = registerTieredMachines("energy_hatch.output_4a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 4),
            (tier, builder) -> builder
                    .langValue("Energy Export Hatch (4A) " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier] : -1)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .overlayTieredHullRenderer("energy_hatch.output_4a")
                    .register(),
            EV, IV, LuV, ZPM, UV, UHV);

    public final static MachineDefinition[] ENERGY_INPUT_HATCH_16A = registerTieredMachines("energy_hatch.input_16a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 16),
            (tier, builder) -> builder
                    .langValue("Energy Import Hatch (16A) " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .itemColor((itemStack, index) -> index == 2 ? GTValues.VC[tier] : -1)
                    .abilities(PartAbility.INPUT_ENERGY)
                    .overlayTieredHullRenderer("energy_hatch.input_16a")
                    .register(),
            EV, IV, LuV, ZPM, UV, UHV);

    public final static MachineDefinition[] ENERGY_OUTPUT_HATCH_16A = registerTieredMachines("energy_hatch.output_16a",
            (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 16),
            (tier, builder) -> builder
                    .langValue("Energy Export Hatch (16A) " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .overlayTieredHullRenderer("energy_hatch.output_16a")
                    .register(),
            EV, IV, LuV, ZPM, UV, UHV);

    public final static MachineDefinition[] MUFFLER_HATCH = registerTieredMachines("muffler_hatch",
            MufflerPartMachine::new,
            (tier, builder) -> builder
                    .langValue("Muffler Hatch " + VNF[tier])
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.MUFFLER)
                    .overlayTieredHullRenderer("muffler_hatch")
                    .tooltips(LangHandler.getFromMultiLang("gtceu.machine.muffler_hatch.tooltip", 0),
                            Component.translatable("gtceu.muffler.recovery_tooltip", Math.max(1, tier * 10)),
                            Component.translatable("gtceu.universal.enabled"),
                            LangHandler.getFromMultiLang("gtceu.machine.muffler_hatch.tooltip", 1).withStyle(ChatFormatting.DARK_RED))
                    .register(),
            ELECTRIC_TIERS);

    public final static MachineDefinition STEAM_HATCH = REGISTRATE.machine("steam_hatch", SteamHatchPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.STEAM)
            .overlaySteamHullRenderer("steam_hatch")
            .tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", SteamHatchPartMachine.INITIAL_TANK_CAPACITY),
                    Component.translatable("gregtech.machine.steam.steam_hatch.tooltip"))
            .register();

    public final static MachineDefinition COKE_OVEN_HATCH = REGISTRATE.machine("coke_oven_hatch", CokeOvenHatch::new)
            .rotationState(RotationState.ALL)
            .renderer(() -> new CTMModelRenderer(GTCEu.id("block/machine/part/coke_oven_hatch")))
            .register();


    //////////////////////////////////////
    //*******     Multiblock     *******//
    //////////////////////////////////////
    public final static MultiblockMachineDefinition LARGE_BOILER_BRONZE = registerLargeBoiler("large_bronze_boiler", CASING_BRONZE_BRICKS, CASING_BRONZE_PIPE, FIREBOX_BRONZE,
            GTCEu.id("block/casings/solid/machine_bronze_plated_bricks"), BoilerFireboxType.BRONZE_FIREBOX, 800, 1);
    public final static MultiblockMachineDefinition LARGE_BOILER_STEEL = registerLargeBoiler("large_steel_boiler", CASING_STEEL_SOLID, CASING_STEEL_PIPE, FIREBOX_STEEL,
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"), BoilerFireboxType.STEEL_FIREBOX, 1800, 1);
    public final static MultiblockMachineDefinition LARGE_BOILER_TITANIUM = registerLargeBoiler("large_titanium_boiler", CASING_TITANIUM_STABLE, CASING_TITANIUM_PIPE, FIREBOX_TITANIUM,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"), BoilerFireboxType.TITANIUM_FIREBOX, 3200, 1);
    public final static MultiblockMachineDefinition LARGE_BOILER_TUNGSTENSTEEL = registerLargeBoiler("large_tungstensteel_boiler", CASING_TUNGSTENSTEEL_ROBUST, CASING_TUNGSTENSTEEL_PIPE, FIREBOX_TUNGSTENSTEEL,
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"), BoilerFireboxType.TUNGSTENSTEEL_FIREBOX, 6400, 2);

    public final static MultiblockMachineDefinition COKE_OVEN = REGISTRATE.multiblock("coke_oven", CokeOvenMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.COKE_OVEN_RECIPES)
            .appearanceBlock(CASING_COKE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XYX", "XXX")
                    .where('X', blocks(CASING_COKE_BRICKS.get()).or(blocks(COKE_OVEN_HATCH.get()).setMaxGlobalLimited(5)))
                    .where('#', Predicates.air())
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_coke_bricks"),
                    GTCEu.id("block/multiblock/coke_oven"), false)
            .register();

    public final static MultiblockMachineDefinition PRIMITIVE_BLAST_FURNACE = REGISTRATE.multiblock("primitive_blast_furnace", PrimitiveBlastFurnaceMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.PRIMITIVE_BLAST_FURNACE_RECIPES)
            .appearanceBlock(CASING_PRIMITIVE_BRICKS)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX", "XXX")
                    .aisle("XXX", "X&X", "X#X", "X#X")
                    .aisle("XXX", "XYX", "XXX", "XXX")
                    .where('X', blocks(CASING_PRIMITIVE_BRICKS.get()))
                    .where('#', Predicates.air())
                    .where('&', Predicates.fluids(Fluids.LAVA)) // this won't stay in the structure, and will be broken while running
                    .where('Y', Predicates.controller(blocks(definition.getBlock())))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_primitive_bricks"),
                    GTCEu.id("block/multiblock/primitive_blast_furnace"), false)
            .register();

    public final static MultiblockMachineDefinition ELECTRIC_BLAST_FURNACE = REGISTRATE.multiblock("electric_blast_furnace", ElectricBlastFurnaceMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "CCC", "CCC", "XXX")
                    .aisle("XXX", "C#C", "C#C", "XMX")
                    .aisle("XSX", "CCC", "CCC", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .or(Predicates.autoAbilities(definition.getRecipeType())))
                    .where('M', Predicates.abilities(PartAbility.MUFFLER))
                    .where('C', Predicates.heatingCoils())
                    .where('#', Predicates.air())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .aisle("ISO", "CCC", "CCC", "XXX")
                        .aisle("FXD", "C#C", "C#C", "XHX")
                        .aisle("EEX", "CCC", "CCC", "XXX")
                        .where('X', CASING_INVAR_HEATPROOF.getDefaultState())
                        .where('S', definition, Direction.NORTH)
                        .where('#', Blocks.AIR.defaultBlockState())
                        .where('E', ENERGY_INPUT_HATCH[GTValues.LV - 1], Direction.SOUTH)
                        .where('I', ITEM_IMPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('O', ITEM_EXPORT_BUS[GTValues.LV], Direction.NORTH)
                        .where('F', FLUID_IMPORT_HATCH[GTValues.LV], Direction.WEST)
                        .where('D', FLUID_EXPORT_HATCH[GTValues.LV], Direction.EAST)
                        .where('H', MUFFLER_HATCH[GTValues.LV - 1], Direction.UP);
                Arrays.stream(CoilBlock.CoilType.values())
                        .sorted(Comparator.comparingInt(CoilBlock.CoilType::getTier))
                        .forEach(coil -> shapeInfo.add(builder.where('C', GTBlocks.ALL_COILS.get(coil)).build()));
                return shapeInfo;
            })
            .recoveryItems(() -> new ItemLike[]{GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get()})
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("block/multiblock/electric_blast_furnace"), false)
            .tooltips(Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.1",
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.2"),
                    Component.translatable("gtceu.machine.electric_blast_furnace.tooltip.3")))
            .register();

    public final static MultiblockMachineDefinition LARGE_CHEMICAL_REACTOR = REGISTRATE.multiblock("large_chemical_reactor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.LARGE_CHEMICAL_RECIPES)
            .appearanceBlock(CASING_PTFE_INERT)
            .pattern(definition -> {
                var casing = blocks(CASING_PTFE_INERT.get()).setMinGlobalLimited(10);
                var abilities = Predicates.autoAbilities(definition.getRecipeType());
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
                        .where('H', FLUID_EXPORT_HATCH[3], Direction.NORTH);
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XXX")
                        .aisle("XXX", "XPX", "XXX")
                        .aisle("XEX", "XCX", "XXX")
                        .build()
                );
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XXX")
                        .aisle("XXX", "XPX", "XCX")
                        .aisle("XEX", "XXX", "XXX")
                        .build()
                );
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XXX")
                        .aisle("XCX", "XPX", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build()
                );
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XXX")
                        .aisle("XXX", "CPX", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build()
                );
                shapeInfo.add(baseBuilder.shallowCopy()
                        .aisle("IXO", "FSH", "XXX")
                        .aisle("XXX", "XPC", "XXX")
                        .aisle("XEX", "XXX", "XXX")
                        .build()
                );
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"),
                    GTCEu.id("block/multiblock/large_chemical_reactor"), false)
            .register();

    public final static MultiblockMachineDefinition IMPLOSION_COMPRESSOR = REGISTRATE.multiblock("implosion_compressor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.IMPLOSION_RECIPES)
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeType())))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"), false)
            .register();

    public final static MultiblockMachineDefinition PYROLYSE_OVEN = REGISTRATE.multiblock("pyrolyse_oven", PyrolyseOvenMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.PYROLYSE_RECIPES)
            .appearanceBlock(MACHINE_CASING_ULV)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("CCC", "C#C", "CCC")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.get())))
                    .where('X', blocks(MACHINE_CASING_ULV.get()).setMinGlobalLimited(6).or(Predicates.autoAbilities(definition.getRecipeType())))
                    .where('C', Predicates.heatingCoils())
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer( GTCEu.id("block/casings/voltage/ulv/side"),
                    GTCEu.id("block/multiblock/pyrolyse_oven"), false)
            .tooltips(Component.translatable("gtceu.machine.pyrolyse_oven.tooltip.1"))
            .register();

    public final static MultiblockMachineDefinition CRACKER = REGISTRATE.multiblock("cracker", CrackerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.CRACKING_RECIPES)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("HCHCH", "HCHCH", "HCHCH")
                    .aisle("HCHCH", "H###H", "HCHCH")
                    .aisle("HCHCH", "HCOCH", "HCHCH")
                    .where('O', Predicates.controller(blocks(definition.get())))
                    .where('H', blocks(CASING_STAINLESS_CLEAN.get()).setMinGlobalLimited(12).or(Predicates.autoAbilities(definition.getRecipeType())))
                    .where('#', Predicates.air())
                    .where('C', Predicates.heatingCoils())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/cracking_unit"), false)
            .tooltips(Component.translatable("gtceu.machine.cracker.tooltip.1"))
            .register();

    public final static MultiblockMachineDefinition DISTILLATION_TOWER = REGISTRATE.multiblock("distillation_tower", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DISTILLATION_RECIPES)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start(RIGHT, BACK, UP)
                    .aisle("YSY", "YYY", "YYY")
                    .aisle("XXX", "X#X", "XXX").setRepeatable(1, 11)
                    .aisle("XXX", "XXX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('Y', blocks(CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1)))
                    .where('X', blocks(CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinLayerLimited(1).setMaxLayerLimited(1)))
                    .where('#', Predicates.air())
                    .build())
            .partSorter(Comparator.comparingInt(a -> a.self().getPos().getY()))
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/distillation_tower"), false)
            .register();

    public final static MultiblockMachineDefinition VACUUM_FREEZER = REGISTRATE.multiblock("vacuum_freezer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeType())))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/vacuum_freezer"), false)
            .register();

    public final static MultiblockMachineDefinition ASSEMBLY_LINE = REGISTRATE.multiblock("assembly_line", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start(BACK, UP, RIGHT)
                    .aisle("FIF", "RTR", "SAG", "#Y#")
                    .aisle("FIF", "RTR", "GAG", "#Y#").setRepeatable(3, 15)
                    .aisle("FOF", "RTR", "GAG", "#Y#")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('F', blocks(CASING_STEEL_SOLID.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4)))
                    .where('O', Predicates.abilities(PartAbility.EXPORT_ITEMS).addTooltips(Component.translatable("gtceu.multiblock.pattern.location_end")))
                    .where('Y', blocks(CASING_STEEL_SOLID.get()).or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                    .where('I', blocks(ITEM_IMPORT_BUS[0].getBlock()))
                    .where('G', blocks(CASING_GRATE.get()))
                    .where('A', blocks(CASING_ASSEMBLY_CONTROL.get()))
                    .where('R', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('T', blocks(CASING_ASSEMBLY_LINE.get()))
                    .where('#', Predicates.any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/assembly_line"), false)
            .register();






    //////////////////////////////////////
    //**********     Misc     **********//
    //////////////////////////////////////
    public static Pair<MachineDefinition, MachineDefinition> registerSteamMachines(String name, BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
                                              BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
        MachineDefinition lowTier = builder.apply(false, REGISTRATE.machine(name + "." + "bronze", holder -> factory.apply(holder, false))
                .langValue("Small " + name)
                .tier(0));
        MachineDefinition highTier = builder.apply(true, REGISTRATE.machine(name + "." + "steel", holder -> factory.apply(holder, true))
                .langValue("High Pressure " + name)
                .tier(1));
        return Pair.of(lowTier, highTier);
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                              BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                              BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                              int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            var register =  REGISTRATE.machine(name + "." + GTValues.VN[tier].toLowerCase(), holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[i] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition[] registerSimpleMachines(String name,
                                                              GTRecipeType recipeType,
                                                              Int2LongFunction tankScalingFunction,
                                                              int... tiers) {
        return registerTieredMachines(name, (holder, tier) -> new SimpleTieredMachine(holder, tier, tankScalingFunction), (tier, builder) -> builder
                .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(recipeType)
                .workableTieredHullRenderer(GTCEu.id("block/machines/" + name))
                .tooltips(explosion())
                .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType, (long)tankScalingFunction.apply(tier), true))
                .register(), tiers);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType, Int2LongFunction tankScalingFunction) {
        return registerSimpleMachines(name, recipeType, tankScalingFunction, ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType) {
        return registerSimpleMachines(name, recipeType, defaultTankSizeFunction);
    }

    public static MachineDefinition[] registerSimpleGenerator(String name,
                                                              GTRecipeType recipeType,
                                                              Int2LongFunction tankScalingFunction,
                                                              int... tiers) {
        return registerTieredMachines(name, (holder, tier) -> new SimpleGeneratorMachine(holder, tier, tankScalingFunction), (tier, builder) -> builder
                .langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(recipeType)
                .renderer(() -> new SimpleGeneratorMachineRenderer(tier, GTCEu.id("block/generators/" + name)))
                .overclockingLogic(OverclockingLogic.PERFECT_OVERCLOCK)
                .tooltips(explosion())
                .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType, (long)tankScalingFunction.apply(tier), false))
                .register(), tiers);
    }

    public static Pair<MachineDefinition, MachineDefinition> registerSimpleSteamMachines(String name, GTRecipeType recipeType) {
        return registerSteamMachines("steam_" + name, SimpleSteamMachine::new, (pressure, builder) -> builder
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(recipeType)
                .renderer(() -> new WorkableSteamMachineRenderer(pressure, GTCEu.id("block/machines/" + name)))
                .register());
    }

    public static MachineDefinition[] registerBatteryBuffer(int batterySlotSize){
        return registerTieredMachines("battery_buffer." + batterySlotSize,
                (holder, tier) -> new BatteryBufferMachine(holder, tier, batterySlotSize),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .renderer(() -> new BatteryBufferRenderer(tier, batterySlotSize))
                        .tooltips(explosion())
                        .tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity", batterySlotSize),
                                Component.translatable("gtceu.universal.tooltip.voltage_in_out", GTValues.V[tier], GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in_till", batterySlotSize * BatteryBufferMachine.AMPS_PER_BATTERY),
                                Component.translatable("gtceu.universal.tooltip.amperage_out_till", batterySlotSize))
                        .register(),
                ALL_TIERS);
    }

    public static MultiblockMachineDefinition registerLargeBoiler(String name, Supplier<? extends Block> casing, Supplier<? extends Block> pipe, Supplier<? extends Block> fireBox, ResourceLocation texture, BoilerFireboxType firebox, int maxTemperature, int heatSpeed) {
        return REGISTRATE.multiblock(name, holder -> new LargeBoilerMachine(holder, maxTemperature, heatSpeed))
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.LARGE_BOILER_RECIPES)
                .appearanceBlock(casing)
                .partAppearance((controller, part, side) -> controller.self().getPos().below().getY() == part.self().getPos().getY() ? fireBox.get().defaultBlockState() : casing.get().defaultBlockState())
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("XXX", "CCC", "CCC", "CCC")
                        .aisle("XXX", "CPC", "CPC", "CCC")
                        .aisle("XXX", "CSC", "CCC", "CCC")
                        .where('S', Predicates.controller(blocks(definition.getBlock())))
                        .where('P', blocks(pipe.get()))
                        .where('X', states(ALL_FIREBOXES.get(firebox).getDefaultState()).setMinGlobalLimited(4)
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1)))
                        .where('C', blocks(casing.get()).setMinGlobalLimited(20)
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1)))
                        .build())
                .recoveryItems(() -> new ItemLike[]{GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get()})
                .renderer(() -> new LargeBoilerRenderer(texture, firebox, GTCEu.id("block/multiblock/generator/" + name)))
                .tooltips(
                        Component.translatable("gtceu.multiblock.large_boiler.max_temperature", (int)(maxTemperature * 274.15), maxTemperature),
                        Component.translatable("gtceu.multiblock.large_boiler.heat_time_tooltip", maxTemperature / heatSpeed / 20),
                        Component.translatable("gtceu.multiblock.large_boiler.explosion_tooltip").withStyle(ChatFormatting.DARK_RED))
                .register();
    }

    public static MachineDefinition registerCrate(Material material, int capacity, String lang) {
        boolean wooden = GTMaterials.Wood.equals(material) || GTMaterials.TreatedWood.equals(material);

        return REGISTRATE.machine("crate." + material, holder -> new CrateMachine(holder, material, capacity))
                .langValue(lang)
                .rotationState(RotationState.NONE)
                .tooltips(Component.translatable("gtceu.universal.tooltip.item_storage_capacity", capacity))
                .renderer(() -> new MachineRenderer(GTCEu.id("block/machine/crate/" + (wooden ? "wooden" : "metal") + "_crate")))
                .paintingColor(wooden ? 0xFFFFFF : material.getMaterialRGB())
                .itemColor((s, t) -> wooden ? 0xFFFFFF : material.getMaterialRGB())
                .register();
    }

    public static MachineDefinition registerDrum(Material material, int capacity, String lang) {
        boolean wooden = GTMaterials.Wood.equals(material) || GTMaterials.TreatedWood.equals(material);
        var definition = REGISTRATE.machine("drum." + material, MachineDefinition::createDefinition, holder -> new DrumMachine(holder, material, capacity), MetaMachineBlock::new, DrumMachineItem::create, MetaMachineBlockEntity::createBlockEntity)
                .langValue(lang)
                .rotationState(RotationState.NONE)
                .renderer(() -> new MachineRenderer(GTCEu.id("block/machine/" + (wooden ? "wooden" : "metal") + "_drum")))
                .tooltipBuilder(createTankTooltips("Fluid"))
                .tooltips(Component.translatable("gtceu.machine.quantum_tank.tooltip"), Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", 16 * FluidHelper.getBucket()))
                .paintingColor(wooden ? 0xFFFFFF : material.getMaterialRGB())
                .itemColor((s, i) -> wooden ? 0xFFFFFF : material.getMaterialRGB())
                .register();
        DRUM_CAPACITY.put(definition, capacity);
        return definition;
    }

    public static Component explosion() {
        if (ConfigHolder.machines.doTerrainExplosion)
            return Component.translatable("gtceu.universal.tooltip.terrain_resist");
        return null;
    }

    public static Component[] workableTiered(int tier, long voltage, long energyCapacity, GTRecipeType recipeType, long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents.add(input ? Component.translatable("gtceu.universal.tooltip.voltage_in", voltage, GTValues.VNF[tier]) :
                Component.translatable("gtceu.universal.tooltip.voltage_out", voltage, GTValues.VNF[tier]));
        tooltipComponents.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", energyCapacity));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 || recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents.add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", tankCapacity));
        return tooltipComponents.toArray(Component[]::new);
    }

    public static void init() {
        if (GTCEu.isCreateLoaded()) {
            new GTCreateMachines();
        }
        if (GTCEu.isKubeJSLoaded()) {
            new MachineEventJS().post();
        }
    }

    public static MachineDefinition get(String name) {
        return GTRegistries.MACHINES.get(GTCEu.id(name));
    }

}
