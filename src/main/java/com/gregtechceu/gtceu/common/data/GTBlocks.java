package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.block.*;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.*;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistancePipeBlock;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.block.explosive.IndustrialTNTBlock;
import com.gregtechceu.gtceu.common.block.explosive.PowderbarrelBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance.LDFluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.longdistance.LDItemPipeType;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeType;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeType;
import com.gregtechceu.gtceu.core.mixins.BlockPropertiesAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.Tags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("removal")
public class GTBlocks {
    //////////////////////////////////////
    // ***** Procedural Blocks *****//

    public static final BlockEntry<LaserPipeBlock>[] LASER_PIPES = new BlockEntry[LaserPipeType.values().length];
    public static final BlockEntry<OpticalPipeBlock>[] OPTICAL_PIPES = new BlockEntry[OpticalPipeType.values().length];
    public static final BlockEntry<DuctPipeBlock>[] DUCT_PIPES = new BlockEntry[DuctPipeType.VALUES.length];

    //////////////////////////////////////
    // Pipes Blocks //
    //////////////////////////////////////
    static {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_PIPE);
    }

    // Laser Pipe Blocks
    private static void generateLaserPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Laser Pipe Blocks...");
        for (int i = 0; i < LaserPipeType.values().length; ++i) {
            registerLaserPipeBlock(i);
        }
        GTCEu.LOGGER.debug("Generating GTCEu Laser Pipe Blocks... Complete!");
    }

    private static void registerLaserPipeBlock(int index) {
        var type = LaserPipeType.values()[index];
        var entry = REGISTRATE
                .block("%s_laser_pipe".formatted(type.getSerializedName()), (p) -> new LaserPipeBlock(p, type))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.dynamicShape().noOcclusion().forceSolidOn())
                .blockstate(NonNullBiConsumer.noop())
                .defaultLoot()
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER)
                .addLayer(() -> RenderType::cutoutMipped)
                .addLayer(() -> RenderType::translucent)
                .color(() -> LaserPipeBlock::tintedColor)
                .item(LaserPipeBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> LaserPipeBlockItem::tintColor)
                .build()
                .register();
        LASER_PIPES[index] = entry;
    }

    // Optical Pipe Blocks
    private static void generateOpticalPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Optical Pipe Blocks...");
        for (int i = 0; i < OpticalPipeType.values().length; ++i) {
            registerOpticalPipeBlock(i);
        }
        GTCEu.LOGGER.debug("Generating GTCEu Optical Pipe Blocks... Complete!");
    }

    private static void registerOpticalPipeBlock(int index) {
        var type = OpticalPipeType.values()[index];
        var entry = REGISTRATE
                .block("%s_optical_pipe".formatted(type.getSerializedName()), (p) -> new OpticalPipeBlock(p, type))
                .lang("Optical Fiber Cable")
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.dynamicShape().noOcclusion().forceSolidOn())
                .blockstate(NonNullBiConsumer.noop())
                .defaultLoot()
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER)
                .addLayer(() -> RenderType::cutoutMipped)
                .addLayer(() -> RenderType::translucent)
                .color(() -> OpticalPipeBlock::tintedColor)
                .item(OpticalPipeBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        OPTICAL_PIPES[index] = entry;
    }

    // Duct Pipe Blocks
    private static void generateDuctPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Duct Pipe Blocks...");
        for (int i = 0; i < DuctPipeType.VALUES.length; ++i) {
            registerDuctPipeBlock(i);
        }
        GTCEu.LOGGER.debug("Generating GTCEu Duct Pipe Blocks... Complete!");
    }

    private static void registerDuctPipeBlock(int index) {
        var type = DuctPipeType.VALUES[index];
        var entry = REGISTRATE
                .block("%s_duct_pipe".formatted(type.getSerializedName()), (p) -> new DuctPipeBlock(p, type))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.dynamicShape().noOcclusion().forceSolidOn())
                .blockstate(NonNullBiConsumer.noop())
                .defaultLoot()
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .addLayer(() -> RenderType::cutoutMipped)
                .addLayer(() -> RenderType::translucent)
                .item(DuctPipeBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        DUCT_PIPES[index] = entry;
    }

    // Long Distance Item Pipe Blocks
    public static final BlockEntry<LongDistancePipeBlock> LD_ITEM_PIPE = REGISTRATE
            .block("long_distance_item_pipeline",
                    properties -> new LongDistancePipeBlock(properties, LDItemPipeType.INSTANCE))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .exBlockstate(GTModels.cubeAllModel(GTCEu.id("block/pipe/ld_item_pipe/block")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, BlockTags.NEEDS_STONE_TOOL)
            .simpleItem()
            .register();

    // Long Distance Fluid Pipe Blocks
    public static final BlockEntry<LongDistancePipeBlock> LD_FLUID_PIPE = REGISTRATE
            .block("long_distance_fluid_pipeline",
                    properties -> new LongDistancePipeBlock(properties, LDFluidPipeType.INSTANCE))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .exBlockstate(GTModels.cubeAllModel(GTCEu.id("block/pipe/ld_fluid_pipe/block")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, BlockTags.NEEDS_STONE_TOOL)
            .simpleItem()
            .register();

    //////////////////////////////////////
    // ****** Casing Blocks *****//
    //////////////////////////////////////
    static {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.DECORATION);
    }

    // Multiblock Machine Casing Blocks
    public static final BlockEntry<Block> CASING_WOOD_WALL = createSidedCasingBlock("wood_wall",
            GTCEu.id("block/casings/wood_wall"));
    public static final BlockEntry<Block> CASING_COKE_BRICKS = createBrickCasingBlock("coke_oven_bricks",
            GTCEu.id("block/casings/solid/machine_coke_bricks"));
    public static final BlockEntry<Block> CASING_PRIMITIVE_BRICKS = createBrickCasingBlock("firebricks",
            GTCEu.id("block/casings/solid/machine_primitive_bricks"));
    public static final BlockEntry<Block> CASING_BRONZE_BRICKS = createCasingBlock("steam_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"));
    public static final BlockEntry<Block> CASING_INVAR_HEATPROOF = createCasingBlock("heatproof_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_heatproof"));
    public static final BlockEntry<Block> CASING_ALUMINIUM_FROSTPROOF = createCasingBlock("frostproof_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_frost_proof"));
    public static final BlockEntry<Block> CASING_STEEL_SOLID = createCasingBlock("solid_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
    public static final BlockEntry<Block> CASING_STAINLESS_CLEAN = createCasingBlock("clean_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_STABLE = createCasingBlock("stable_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_ROBUST = createCasingBlock("robust_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));
    public static final BlockEntry<Block> CASING_PTFE_INERT = createCasingBlock("inert_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"));
    public static final BlockEntry<Block> CASING_HSSE_STURDY = createCasingBlock("sturdy_machine_casing",
            GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"));
    public static final BlockEntry<Block> CASING_PALLADIUM_SUBSTATION = createCasingBlock("palladium_substation",
            GTCEu.id("block/casings/solid/machine_casing_palladium_substation"));
    public static final BlockEntry<GlassBlock> CASING_TEMPERED_GLASS = createGlassCasingBlock("tempered_glass",
            GTCEu.id("block/casings/transparent/tempered_glass"), () -> RenderType::translucent);
    public static final ImmutableMap<Material, BlockEntry<Block>> MATERIALS_TO_CASINGS;

    static {
        ImmutableMap.Builder<Material, BlockEntry<Block>> builder = ImmutableMap.builder();
        builder.put(GTMaterials.Bronze, CASING_BRONZE_BRICKS);
        builder.put(GTMaterials.Invar, CASING_INVAR_HEATPROOF);
        builder.put(GTMaterials.Aluminium, CASING_ALUMINIUM_FROSTPROOF);
        builder.put(GTMaterials.Steel, CASING_STEEL_SOLID);
        builder.put(GTMaterials.StainlessSteel, CASING_STAINLESS_CLEAN);
        builder.put(GTMaterials.Titanium, CASING_TITANIUM_STABLE);
        builder.put(GTMaterials.TungstenSteel, CASING_TUNGSTENSTEEL_ROBUST);
        builder.put(GTMaterials.Polytetrafluoroethylene, CASING_PTFE_INERT);
        builder.put(GTMaterials.HSSE, CASING_HSSE_STURDY);
        // GCYM
        builder.put(GTMaterials.HSLASteel, CASING_NONCONDUCTING);
        builder.put(GTMaterials.IncoloyMA956, CASING_VIBRATION_SAFE);
        builder.put(GTMaterials.WatertightSteel, CASING_WATERTIGHT);
        builder.put(GTMaterials.Zeron100, CASING_SECURE_MACERATION);
        builder.put(GTMaterials.TungstenCarbide, CASING_HIGH_TEMPERATURE_SMELTING);
        builder.put(GTMaterials.TitaniumTungstenCarbide, CASING_LASER_SAFE_ENGRAVING);
        builder.put(GTMaterials.Stellite100, CASING_LARGE_SCALE_ASSEMBLING);
        builder.put(GTMaterials.HastelloyC276, CASING_SHOCK_PROOF);

        MaterialCasingCollectionEvent event = new MaterialCasingCollectionEvent(builder);
        AddonFinder.getAddons().forEach(addon -> addon.collectMaterialCasings(event));

        MATERIALS_TO_CASINGS = builder.build();
    }

    // Assembly Line
    public static final BlockEntry<Block> CASING_GRATE = createCasingBlock("assembly_line_grating",
            GTCEu.id("block/casings/pipe/machine_casing_grate"));
    public static final BlockEntry<Block> CASING_ASSEMBLY_CONTROL = createCasingBlock("assembly_line_casing",
            GTCEu.id("block/casings/mechanic/machine_casing_assembly_control"));
    public static final BlockEntry<GlassBlock> CASING_LAMINATED_GLASS = createGlassCasingBlock("laminated_glass",
            GTCEu.id("block/casings/transparent/laminated_glass"), () -> RenderType::cutoutMipped);
    public static final BlockEntry<ActiveBlock> CASING_ASSEMBLY_LINE = createActiveCasing("assembly_line_unit",
            "block/variant/assembly_line");

    // Gear Boxes
    public static final BlockEntry<Block> CASING_BRONZE_GEARBOX = createCasingBlock("bronze_gearbox",
            GTCEu.id("block/casings/gearbox/machine_casing_gearbox_bronze"));
    public static final BlockEntry<Block> CASING_STEEL_GEARBOX = createCasingBlock("steel_gearbox",
            GTCEu.id("block/casings/gearbox/machine_casing_gearbox_steel"));
    public static final BlockEntry<Block> CASING_STAINLESS_STEEL_GEARBOX = createCasingBlock("stainless_steel_gearbox",
            GTCEu.id("block/casings/gearbox/machine_casing_gearbox_stainless_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_GEARBOX = createCasingBlock("titanium_gearbox",
            GTCEu.id("block/casings/gearbox/machine_casing_gearbox_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_GEARBOX = createCasingBlock("tungstensteel_gearbox",
            GTCEu.id("block/casings/gearbox/machine_casing_gearbox_tungstensteel"));

    // Turbine Casings
    public static final BlockEntry<Block> CASING_STEEL_TURBINE = createCasingBlock("steel_turbine_casing",
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_TURBINE = createCasingBlock("titanium_turbine_casing",
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"));
    public static final BlockEntry<Block> CASING_STAINLESS_TURBINE = createCasingBlock("stainless_steel_turbine_casing",
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_TURBINE = createCasingBlock(
            "tungstensteel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"));

    // Pipe casings
    public static final BlockEntry<Block> CASING_BRONZE_PIPE = createCasingBlock("bronze_pipe_casing",
            GTCEu.id("block/casings/pipe/machine_casing_pipe_bronze"));
    public static final BlockEntry<Block> CASING_STEEL_PIPE = createCasingBlock("steel_pipe_casing",
            GTCEu.id("block/casings/pipe/machine_casing_pipe_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_PIPE = createCasingBlock("titanium_pipe_casing",
            GTCEu.id("block/casings/pipe/machine_casing_pipe_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_PIPE = createCasingBlock("tungstensteel_pipe_casing",
            GTCEu.id("block/casings/pipe/machine_casing_pipe_tungstensteel"));
    public static final BlockEntry<Block> CASING_POLYTETRAFLUOROETHYLENE_PIPE = createCasingBlock("ptfe_pipe_casing",
            GTCEu.id("block/casings/pipe/machine_casing_pipe_polytetrafluoroethylene"));
    public static final BlockEntry<MinerPipeBlock> MINER_PIPE = REGISTRATE.block("miner_pipe", MinerPipeBlock::new)
            .initialProperties(() -> Blocks.BEDROCK)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .addLayer(() -> RenderType::cutoutMipped)
            .exBlockstate(GTModels.createModelBlockState(GTCEu.id("block/miner_pipe")))
            .tag(BlockTags.DRAGON_IMMUNE, BlockTags.WITHER_IMMUNE, BlockTags.INFINIBURN_END,
                    BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS)
            .register();

    // The Pump Deck
    public static final BlockEntry<Block> CASING_PUMP_DECK = REGISTRATE
            .block("pump_deck", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.WOOD))
            .blockstate(GTModels.createSidedCasingModel(GTCEu.id("block/casings/pump_deck")))
            .tag(CustomTags.MINEABLE_WITH_WRENCH, BlockTags.MINEABLE_WITH_AXE)
            .item(BlockItem::new)
            .build()
            .register();

    // spotless:off

    // Machine Casings
    public static final BlockEntry<Block> MACHINE_CASING_ULV = createMachineCasingBlock(ULV);
    public static final BlockEntry<Block> MACHINE_CASING_LV = createMachineCasingBlock(LV);
    public static final BlockEntry<Block> MACHINE_CASING_MV = createMachineCasingBlock(MV);
    public static final BlockEntry<Block> MACHINE_CASING_HV = createMachineCasingBlock(HV);
    public static final BlockEntry<Block> MACHINE_CASING_EV = createMachineCasingBlock(EV);
    public static final BlockEntry<Block> MACHINE_CASING_IV = createMachineCasingBlock(IV);
    public static final BlockEntry<Block> MACHINE_CASING_LuV = createMachineCasingBlock(LuV);
    public static final BlockEntry<Block> MACHINE_CASING_ZPM = createMachineCasingBlock(ZPM);
    public static final BlockEntry<Block> MACHINE_CASING_UV = createMachineCasingBlock(UV);
    public static final BlockEntry<Block> MACHINE_CASING_UHV = createMachineCasingBlock(UHV);
    public static final BlockEntry<Block> MACHINE_CASING_UEV = createMachineCasingBlock(UEV);
    public static final BlockEntry<Block> MACHINE_CASING_UIV = createMachineCasingBlock(UIV);
    public static final BlockEntry<Block> MACHINE_CASING_UXV = createMachineCasingBlock(UXV);
    public static final BlockEntry<Block> MACHINE_CASING_OpV = createMachineCasingBlock(OpV);
    public static final BlockEntry<Block> MACHINE_CASING_MAX = createMachineCasingBlock(MAX);

    // Hermetic Casings
    public static final BlockEntry<Block> HERMETIC_CASING_LV = createHermeticCasing(LV);
    public static final BlockEntry<Block> HERMETIC_CASING_MV = createHermeticCasing(MV);
    public static final BlockEntry<Block> HERMETIC_CASING_HV = createHermeticCasing(HV);
    public static final BlockEntry<Block> HERMETIC_CASING_EV = createHermeticCasing(EV);
    public static final BlockEntry<Block> HERMETIC_CASING_IV = createHermeticCasing(IV);
    public static final BlockEntry<Block> HERMETIC_CASING_LuV = createHermeticCasing(LuV);
    public static final BlockEntry<Block> HERMETIC_CASING_ZPM = createHermeticCasing(ZPM);
    public static final BlockEntry<Block> HERMETIC_CASING_UV = createHermeticCasing(UV);
    public static final BlockEntry<Block> HERMETIC_CASING_UHV = createHermeticCasing(UHV);

    public static final BlockEntry<Block> BRONZE_HULL = createSteamCasing("bronze_machine_casing", "bronze");
    public static final BlockEntry<Block> BRONZE_BRICKS_HULL = createSteamCasing("bronze_brick_casing", "bricked_bronze");
    public static final BlockEntry<Block> STEEL_HULL = createSteamCasing("steel_machine_casing", "steel");
    public static final BlockEntry<Block> STEEL_BRICKS_HULL = createSteamCasing("steel_brick_casing", "bricked_steel");

    // Heating Coils
    public static final BlockEntry<CoilBlock> COIL_CUPRONICKEL = createCoilBlock(CoilBlock.CoilType.CUPRONICKEL);
    public static final BlockEntry<CoilBlock> COIL_KANTHAL = createCoilBlock(CoilBlock.CoilType.KANTHAL);
    public static final BlockEntry<CoilBlock> COIL_NICHROME = createCoilBlock(CoilBlock.CoilType.NICHROME);
    public static final BlockEntry<CoilBlock> COIL_RTMALLOY = createCoilBlock(CoilBlock.CoilType.RTMALLOY);
    public static final BlockEntry<CoilBlock> COIL_HSSG = createCoilBlock(CoilBlock.CoilType.HSSG);
    public static final BlockEntry<CoilBlock> COIL_NAQUADAH = createCoilBlock(CoilBlock.CoilType.NAQUADAH);
    public static final BlockEntry<CoilBlock> COIL_TRINIUM = createCoilBlock(CoilBlock.CoilType.TRINIUM);
    public static final BlockEntry<CoilBlock> COIL_TRITANIUM = createCoilBlock(CoilBlock.CoilType.TRITANIUM);

    // PSS batteries
    public static final BlockEntry<BatteryBlock> BATTERY_EMPTY_TIER_I = createBatteryBlock(BatteryBlock.BatteryPartType.EMPTY_TIER_I);
    public static final BlockEntry<BatteryBlock> BATTERY_LAPOTRONIC_EV = createBatteryBlock(BatteryBlock.BatteryPartType.EV_LAPOTRONIC);
    public static final BlockEntry<BatteryBlock> BATTERY_LAPOTRONIC_IV = createBatteryBlock(BatteryBlock.BatteryPartType.IV_LAPOTRONIC);
    public static final BlockEntry<BatteryBlock> BATTERY_EMPTY_TIER_II = createBatteryBlock(BatteryBlock.BatteryPartType.EMPTY_TIER_II);
    public static final BlockEntry<BatteryBlock> BATTERY_LAPOTRONIC_LuV = createBatteryBlock(BatteryBlock.BatteryPartType.LuV_LAPOTRONIC);
    public static final BlockEntry<BatteryBlock> BATTERY_LAPOTRONIC_ZPM = createBatteryBlock(BatteryBlock.BatteryPartType.ZPM_LAPOTRONIC);
    public static final BlockEntry<BatteryBlock> BATTERY_EMPTY_TIER_III = createBatteryBlock(BatteryBlock.BatteryPartType.EMPTY_TIER_III);
    public static final BlockEntry<BatteryBlock> BATTERY_LAPOTRONIC_UV = createBatteryBlock(BatteryBlock.BatteryPartType.UV_LAPOTRONIC);
    public static final BlockEntry<BatteryBlock> BATTERY_ULTIMATE_UHV = createBatteryBlock(BatteryBlock.BatteryPartType.UHV_ULTIMATE);

    // Intake casing
    public static final BlockEntry<ActiveBlock> CASING_ENGINE_INTAKE = createActiveCasing("engine_intake_casing", "block/variant/engine_intake");
    public static final BlockEntry<ActiveBlock> CASING_EXTREME_ENGINE_INTAKE = createActiveCasing("extreme_engine_intake_casing", "block/variant/extreme_engine_intake");

    // Fusion
    public static final Map<IFusionCasingType, Supplier<FusionCasingBlock>> ALL_FUSION_CASINGS = new HashMap<>();
    public static final BlockEntry<FusionCasingBlock> SUPERCONDUCTING_COIL = createFusionCasing(FusionCasingBlock.CasingType.SUPERCONDUCTING_COIL);
    public static final BlockEntry<FusionCasingBlock> FUSION_COIL = createFusionCasing(FusionCasingBlock.CasingType.FUSION_COIL);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_MK2 = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING_MK2);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_MK3 = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING_MK3);
    public static final BlockEntry<GlassBlock> FUSION_GLASS = createGlassCasingBlock("fusion_glass",
            GTCEu.id("block/casings/transparent/fusion_glass"), () -> RenderType::cutoutMipped);

    // Cleanroom
    public static final BlockEntry<Block> PLASTCRETE = createCasingBlock("plascrete", GTCEu.id("block/casings/cleanroom/plascrete"));
    public static final BlockEntry<Block> FILTER_CASING = createCleanroomFilter(CleanroomFilterType.FILTER_CASING);
    public static final BlockEntry<Block> FILTER_CASING_STERILE = createCleanroomFilter(CleanroomFilterType.FILTER_CASING_STERILE);
    public static final BlockEntry<GlassBlock> CLEANROOM_GLASS = createGlassCasingBlock("cleanroom_glass", GTCEu.id("block/casings/transparent/cleanroom_glass"), () -> RenderType::cutoutMipped);

    // Fireboxes
    public static final Map<BoilerFireboxType, BlockEntry<ActiveBlock>> ALL_FIREBOXES = new HashMap<>();
    public static final BlockEntry<ActiveBlock> FIREBOX_BRONZE = createFireboxCasing(BoilerFireboxType.BRONZE_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_STEEL = createFireboxCasing(BoilerFireboxType.STEEL_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TITANIUM = createFireboxCasing(BoilerFireboxType.TITANIUM_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TUNGSTENSTEEL = createFireboxCasing(BoilerFireboxType.TUNGSTENSTEEL_FIREBOX);

    // spotless:on

    // HPCA, AT
    public static final BlockEntry<Block> COMPUTER_CASING = REGISTRATE
            .block("computer_casing", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .blockstate((ctx, prov) -> {
                prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(GTCEu.id("block/computer_casing")));
            })
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<Block> ADVANCED_COMPUTER_CASING = REGISTRATE
            .block("advanced_computer_casing", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .blockstate((ctx, prov) -> {
                prov.simpleBlock(ctx.getEntry(),
                        prov.models().getExistingFile(GTCEu.id("block/advanced_computer_casing")));
            })
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<Block> COMPUTER_HEAT_VENT = REGISTRATE
            .block("computer_heat_vent", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .blockstate((ctx, prov) -> {
                prov.simpleBlock(ctx.getEntry(), prov.models().cubeColumn("computer_heat_vent",
                        GTCEu.id("block/casings/hpca/computer_heat_vent_side"),
                        GTCEu.id("block/casings/hpca/computer_heat_vent_top_bot")));
            })
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new)
            .build()
            .register();
    public static final BlockEntry<Block> HIGH_POWER_CASING = createCasingBlock("high_power_casing",
            GTCEu.id("block/casings/hpca/high_power_casing"));

    @SuppressWarnings("SameParameterValue")
    private static BlockEntry<Block> createSidedCasingBlock(String name, ResourceLocation texture) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .blockstate(GTModels.createSidedCasingModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<Block> createBrickCasingBlock(String name, ResourceLocation texture) {
        // return createCasingBlock(name, GlassBlock::new, texture, () -> Blocks.GLASS, type);
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<GlassBlock> createGlassCasingBlock(String name, ResourceLocation texture,
                                                                 Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, GlassBlock::new)
                .initialProperties(() -> Blocks.GLASS)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, Block::new, texture, () -> Blocks.IRON_BLOCK,
                () -> RenderType::solid);
    }

    public static BlockEntry<Block> createCasingBlock(String name,
                                                      NonNullFunction<BlockBehaviour.Properties, Block> blockSupplier,
                                                      ResourceLocation texture,
                                                      NonNullSupplier<? extends Block> properties,
                                                      Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, blockSupplier)
                .initialProperties(properties)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<Block> createMachineCasingBlock(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        var entry = REGISTRATE
                .block("%s_machine_casing".formatted(tierName), Block::new)
                .lang("%s Machine Casing".formatted(GTValues.VN[tier]))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .blockstate(GTModels.createMachineCasingModel(tierName))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        if (!GTCEuAPI.isHighTier() && tier > GTValues.UHV) {
            REGISTRATE.setCreativeTab(entry, null);
        }
        return entry;
    }

    private static BlockEntry<Block> createHermeticCasing(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        var entry = REGISTRATE
                .block("%s_hermetic_casing".formatted(tierName), Block::new)
                .lang("Hermetic Casing %s".formatted(GTValues.LVT[tier]))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createHermeticCasingModel(tierName))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        if (!GTCEuAPI.isHighTier() && tier > GTValues.UHV) {
            REGISTRATE.setCreativeTab(entry, null);
        }
        return entry;
    }

    private static BlockEntry<Block> createSteamCasing(String name, String material) {
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate(GTModels.createSteamCasingModel(material))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<CoilBlock> createCoilBlock(ICoilType coilType) {
        var coilBlock = REGISTRATE
                .block("%s_coil_block".formatted(coilType.getName()), p -> new CoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createCoilModel(coilType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.HEATING_COILS.put(coilType, coilBlock);
        return coilBlock;
    }

    private static BlockEntry<BatteryBlock> createBatteryBlock(IBatteryData batteryData) {
        var batteryBlock = REGISTRATE.block("%s_battery".formatted(batteryData.getBatteryName()),
                p -> new BatteryBlock(p, batteryData))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, entityType) -> false))
                .blockstate(GTModels.createBatteryBlockModel(batteryData))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();

        GTCEuAPI.PSS_BATTERIES.put(batteryData, batteryBlock);
        return batteryBlock;
    }

    private static BlockEntry<FusionCasingBlock> createFusionCasing(IFusionCasingType casingType) {
        var casingBlock = REGISTRATE
                .block(casingType.getSerializedName(), p -> new FusionCasingBlock(p, casingType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(5.0f, 10.0f).sound(SoundType.METAL))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createFusionCasingModel(casingType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH,
                        CustomTags.TOOL_TIERS[casingType.getHarvestLevel()])
                .item(BlockItem::new)
                .build()
                .register();
        ALL_FUSION_CASINGS.put(casingType, casingBlock);
        return casingBlock;
    }

    private static BlockEntry<Block> createCleanroomFilter(IFilterType filterType) {
        var filterBlock = REGISTRATE.block(filterType.getSerializedName(), Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(2.0f, 8.0f).sound(SoundType.METAL)
                        .isValidSpawn((blockState, blockGetter, blockPos, entityType) -> false))
                .blockstate(GTModels.createCleanroomFilterModel(filterType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, CustomTags.TOOL_TIERS[1])
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.CLEANROOM_FILTERS.put(filterType, filterBlock);
        return filterBlock;
    }

    protected static BlockEntry<ActiveBlock> createActiveCasing(String name, String baseModelPath) {
        return REGISTRATE.block(name, ActiveBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createActiveModel(GTCEu.id(baseModelPath)))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(prov.name(ctx), GTCEu.id(baseModelPath)))
                .build()
                .register();
    }

    private static BlockEntry<ActiveBlock> createFireboxCasing(BoilerFireboxType type) {
        var block = REGISTRATE
                .block("%s_casing".formatted(type.name()), ActiveBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createFireboxModel(type))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        ALL_FIREBOXES.put(type, block);
        return block;
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    public static final BlockEntry<PowderbarrelBlock> POWDERBARREL = REGISTRATE
            .block("powderbarrel", PowderbarrelBlock::new)
            .lang("Powderbarrel")
            .properties(p -> p.destroyTime(0.5F).sound(SoundType.WOOD).mapColor(MapColor.STONE)
                    .pushReaction(PushReaction.BLOCK))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .simpleItem()
            .register();

    public static final BlockEntry<IndustrialTNTBlock> INDUSTRIAL_TNT = REGISTRATE
            .block("industrial_tnt", IndustrialTNTBlock::new)
            .lang("Industrial TNT")
            .properties(p -> p.mapColor(MapColor.FIRE).instabreak().sound(SoundType.GRASS).ignitedByLava())
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/misc/industrial_tnt_side"),
                    new ResourceLocation("minecraft", "block/tnt_bottom"),
                    new ResourceLocation("minecraft", "block/tnt_top"))))
            .simpleItem()
            .register();

    public static final BlockEntry<SaplingBlock> RUBBER_SAPLING = REGISTRATE
            .block("rubber_sapling", properties -> new SaplingBlock(new AbstractTreeGrower() {

                protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random,
                                                                                    boolean largeHive) {
                    return GTConfiguredFeatures.RUBBER;
                }
            }, properties))
            .initialProperties(() -> Blocks.OAK_SAPLING)
            .lang("Rubber Sapling")
            .blockstate(GTModels::createCrossBlockState)
            .addLayer(() -> RenderType::cutout)
            .tag(BlockTags.SAPLINGS)
            .item()
            .model(GTModels::rubberTreeSaplingModel)
            .tag(ItemTags.SAPLINGS)
            .build()
            .register();

    public static final BlockEntry<RubberLogBlock> RUBBER_LOG = REGISTRATE.block("rubber_log", RubberLogBlock::new)
            .properties(p -> p.strength(2.0F).sound(SoundType.WOOD))
            .loot((table, block) -> table.add(block, LootTable.lootTable()
                    .withPool(table
                            .applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)))
                            .add(LootItem.lootTableItem(block)))
                    .withPool(table
                            .applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)))
                            .add(LootItem.lootTableItem(GTItems.STICKY_RESIN.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(RubberLogBlock.NATURAL, true)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.85F))))))
            .lang("Rubber Log")
            .tag(BlockTags.LOGS_THAT_BURN, BlockTags.OVERWORLD_NATURAL_LOGS)
            .blockstate((ctx, provider) -> provider.logBlock(ctx.get()))
            .item()
            .tag(ItemTags.LOGS_THAT_BURN, CustomTags.RUBBER_LOGS)
            .build()
            .register();

    // Fortune Level
    public static final float[] RUBBER_LEAVES_DROPPING_CHANCE = new float[] { 0.05F, 0.0625F, 0.083333336F, 0.1F };

    @OnlyIn(Dist.CLIENT)
    public static BlockColor leavesBlockColor() {
        return (state, reader, pos, tintIndex) -> {
            if (reader != null && pos != null) {
                // return reader.getBlockTint(pos, (biome, x, z) -> biome.getFoliageColor());
                return BiomeColors.getAverageFoliageColor(reader, pos);
            }
            return FoliageColor.getDefaultColor();
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemColor leavesItemColor() {
        return (stack, tintIndex) -> FoliageColor.getDefaultColor();
    }

    public static final BlockEntry<LeavesBlock> RUBBER_LEAVES = REGISTRATE
            .block("rubber_leaves", LeavesBlock::new)
            .initialProperties(() -> Blocks.OAK_LEAVES)
            .lang("Rubber Leaves")
            .blockstate((ctx, prov) -> {
                prov.simpleBlock(ctx.get(), prov.models().leaves(ctx.getName(), prov.blockTexture(ctx.get())));
            })
            .loot((table, block) -> table.add(block,
                    table.createLeavesDrops(block, GTBlocks.RUBBER_SAPLING.get(), RUBBER_LEAVES_DROPPING_CHANCE)))
            .tag(BlockTags.LEAVES, BlockTags.MINEABLE_WITH_HOE)
            .color(() -> GTBlocks::leavesBlockColor)
            .item()
            .color(() -> GTBlocks::leavesItemColor)
            .tag(ItemTags.LEAVES)
            .build()
            .register();

    public static final BlockSetType RUBBER_SET = BlockSetType
            .register(new BlockSetType(GTCEu.id("rubber").toString()));
    public static final WoodType RUBBER_TYPE = WoodType
            .register(new WoodType(GTCEu.id("rubber").toString(), RUBBER_SET));

    public static final BlockEntry<RotatedPillarBlock> STRIPPED_RUBBER_LOG = REGISTRATE
            .block("stripped_rubber_log", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.STRIPPED_SPRUCE_LOG)
            .lang("Stripped Rubber Log")
            .blockstate((ctx, provider) -> provider.logBlock(ctx.get()))
            .tag(BlockTags.LOGS_THAT_BURN, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(ItemTags.LOGS_THAT_BURN, CustomTags.RUBBER_LOGS)
            .build()
            .register();
    public static final BlockEntry<RubberWoodBlock> RUBBER_WOOD = REGISTRATE
            .block("rubber_wood", RubberWoodBlock::new)
            .initialProperties(() -> Blocks.SPRUCE_WOOD)
            .lang("Rubber Wood")
            .blockstate((ctx, provider) -> provider.axisBlock(ctx.get(),
                    provider.blockTexture(GTBlocks.RUBBER_LOG.get()), provider.blockTexture(GTBlocks.RUBBER_LOG.get())))
            .tag(BlockTags.LOGS_THAT_BURN, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(ItemTags.LOGS_THAT_BURN, CustomTags.RUBBER_LOGS)
            .build()
            .register();
    public static final BlockEntry<RotatedPillarBlock> STRIPPED_RUBBER_WOOD = REGISTRATE
            .block("stripped_rubber_wood", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.STRIPPED_SPRUCE_WOOD)
            .lang("Stripped Rubber Wood")
            .blockstate((ctx, provider) -> provider.axisBlock(ctx.get(), provider.blockTexture(ctx.get()),
                    provider.blockTexture(ctx.get())))
            .tag(BlockTags.LOGS_THAT_BURN, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(ItemTags.LOGS_THAT_BURN, CustomTags.RUBBER_LOGS)
            .build()
            .register();

    public static final BlockEntry<Block> RUBBER_PLANK = REGISTRATE
            .block("rubber_planks", Block::new)
            .initialProperties(() -> Blocks.SPRUCE_PLANKS)
            .lang("Rubber Planks")
            .tag(BlockTags.PLANKS, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(ItemTags.PLANKS)
            .build()
            .register();

    public static final BlockEntry<SlabBlock> RUBBER_SLAB = REGISTRATE
            .block("rubber_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.SPRUCE_SLAB)
            .lang("Rubber Slab")
            .blockstate((ctx, prov) -> prov.slabBlock(ctx.getEntry(), prov.blockTexture(GTBlocks.RUBBER_PLANK.get()),
                    prov.blockTexture(GTBlocks.RUBBER_PLANK.get())))
            .tag(BlockTags.WOODEN_SLABS, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(ItemTags.WOODEN_SLABS)
            .build()
            .register();

    public static final BlockEntry<FenceBlock> RUBBER_FENCE = REGISTRATE
            .block("rubber_fence", FenceBlock::new)
            .initialProperties(() -> Blocks.SPRUCE_FENCE)
            .lang("Rubber Fence")
            .blockstate((ctx, prov) -> prov.fenceBlock(ctx.getEntry(), prov.blockTexture(RUBBER_PLANK.get())))
            .tag(BlockTags.WOODEN_FENCES, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .model((ctx, prov) -> prov.fenceInventory(ctx.getName(),
                    GTBlocks.RUBBER_PLANK.getId().withPrefix("block/")))
            .tag(ItemTags.WOODEN_FENCES)
            .build()
            .register();

    public static final BlockEntry<GTStandingSignBlock> RUBBER_SIGN = REGISTRATE
            .block("rubber_sign", (p) -> new GTStandingSignBlock(p, RUBBER_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_SIGN)
            .lang("Rubber Sign")
            .blockstate((ctx, prov) -> prov.signBlock(ctx.get(), GTBlocks.RUBBER_WALL_SIGN.get(),
                    prov.blockTexture(GTBlocks.RUBBER_PLANK.get())))
            .tag(BlockTags.STANDING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item((b, p) -> new SignItem(p, b, GTBlocks.RUBBER_WALL_SIGN.get()))
            .defaultModel()
            .tag(ItemTags.SIGNS)
            .build()
            .register();

    public static final BlockEntry<GTWallSignBlock> RUBBER_WALL_SIGN = REGISTRATE
            .block("rubber_wall_sign", (p) -> new GTWallSignBlock(p, RUBBER_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_WALL_SIGN)
            .lang("Rubber Wall Sign")
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.WALL_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .loot((table, block) -> table.dropOther(block, RUBBER_SIGN.asItem()))
            .register();

    public static final BlockEntry<GTCeilingHangingSignBlock> RUBBER_HANGING_SIGN = REGISTRATE
            .block("rubber_hanging_sign", (p) -> new GTCeilingHangingSignBlock(p, RUBBER_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_HANGING_SIGN)
            .lang("Rubber Hanging Sign")
            .blockstate((ctx, prov) -> {
                ModelFile model = prov.models().sign(ctx.getName(), prov.blockTexture(GTBlocks.RUBBER_PLANK.get()));
                prov.simpleBlock(ctx.get(), model);
            })
            .tag(BlockTags.CEILING_HANGING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item((b, p) -> new HangingSignItem(b, GTBlocks.RUBBER_WALL_HANGING_SIGN.get(), p))
            .defaultModel()
            .tag(ItemTags.HANGING_SIGNS)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .build()
            .register();

    public static final BlockEntry<GTWallHangingSignBlock> RUBBER_WALL_HANGING_SIGN = REGISTRATE
            .block("rubber_wall_hanging_sign", (p) -> new GTWallHangingSignBlock(p, RUBBER_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_WALL_HANGING_SIGN)
            .lang("Rubber Wall Hanging Sign")
            .blockstate((ctx, prov) -> {
                ModelFile model = prov.models().sign(ctx.getName(), prov.blockTexture(GTBlocks.RUBBER_PLANK.get()));
                prov.simpleBlock(ctx.get(), model);
            })
            .tag(BlockTags.WALL_HANGING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .loot((table, block) -> table.dropOther(block, RUBBER_HANGING_SIGN.asItem()))
            .register();

    public static final BlockEntry<PressurePlateBlock> RUBBER_PRESSURE_PLATE = REGISTRATE
            .block("rubber_pressure_plate",
                    (p) -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, p, RUBBER_SET))
            .initialProperties(() -> Blocks.SPRUCE_PRESSURE_PLATE)
            .lang("Rubber Pressure Plate")
            .tag(BlockTags.WOODEN_PRESSURE_PLATES, BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.pressurePlateBlock(ctx.getEntry(),
                    prov.blockTexture(GTBlocks.RUBBER_PLANK.get())))
            .item()
            .tag(ItemTags.WOODEN_PRESSURE_PLATES)
            .build()
            .register();
    public static final BlockEntry<TrapDoorBlock> RUBBER_TRAPDOOR = REGISTRATE
            .block("rubber_trapdoor", (p) -> new TrapDoorBlock(p, RUBBER_SET))
            .initialProperties(() -> Blocks.SPRUCE_TRAPDOOR)
            .lang("Rubber Trapdoor")
            .blockstate((ctx, prov) -> prov.trapdoorBlock(ctx.get(), prov.blockTexture(ctx.get()), true))
            .tag(BlockTags.WOODEN_TRAPDOORS, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .model((ctx, prov) -> prov.trapdoorOrientableBottom(ctx.getName(), ctx.getId().withPrefix("block/")))
            .tag(ItemTags.WOODEN_TRAPDOORS)
            .build()
            .register();
    public static final BlockEntry<StairBlock> RUBBER_STAIRS = REGISTRATE
            .block("rubber_stairs", (p) -> new StairBlock(RUBBER_PLANK::getDefaultState, p))
            .initialProperties(() -> Blocks.SPRUCE_STAIRS)
            .lang("Rubber Stairs")
            .tag(BlockTags.STAIRS, BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.stairsBlock(ctx.getEntry(), prov.blockTexture(GTBlocks.RUBBER_PLANK.get())))
            .item()
            .tag(ItemTags.STAIRS)
            .build()
            .register();
    public static final BlockEntry<ButtonBlock> RUBBER_BUTTON = REGISTRATE
            .block("rubber_button", (p) -> new ButtonBlock(p, RUBBER_SET, 30, true))
            .initialProperties(() -> Blocks.SPRUCE_BUTTON)
            .lang("Rubber Button")
            .tag(BlockTags.WOODEN_BUTTONS, BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.buttonBlock(ctx.getEntry(), prov.blockTexture(RUBBER_PLANK.get())))
            .item()
            .model((ctx, prov) -> prov.buttonInventory(ctx.getName(),
                    GTBlocks.RUBBER_PLANK.getId().withPrefix("block/")))
            .tag(ItemTags.WOODEN_BUTTONS)
            .build()
            .register();
    public static final BlockEntry<FenceGateBlock> RUBBER_FENCE_GATE = REGISTRATE
            .block("rubber_fence_gate", (p) -> new FenceGateBlock(p, RUBBER_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_FENCE_GATE)
            .lang("Rubber Fence Gate")
            .tag(BlockTags.FENCE_GATES, BlockTags.MINEABLE_WITH_AXE)
            .blockstate(
                    (ctx, prov) -> prov.fenceGateBlock(ctx.getEntry(), prov.blockTexture(GTBlocks.RUBBER_PLANK.get())))
            .item()
            .tag(ItemTags.FENCE_GATES)
            .build()
            .register();
    public static final BlockEntry<DoorBlock> RUBBER_DOOR = REGISTRATE
            .block("rubber_door", (p) -> new DoorBlock(p, RUBBER_SET))
            .initialProperties(() -> Blocks.SPRUCE_DOOR)
            .lang("Rubber Door")
            .loot((table, block) -> table.add(block, table.createDoorTable(block)))
            .addLayer(() -> RenderType::cutout)
            .blockstate((ctx, prov) -> prov.doorBlock(ctx.getEntry(), GTCEu.id("block/rubber_door_bottom"),
                    GTCEu.id("block/rubber_door_top")))
            .tag(BlockTags.WOODEN_DOORS, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .model((ctx, prov) -> prov.generated(ctx))
            .tag(ItemTags.WOODEN_DOORS)
            .build()
            .register();

    public static final BlockSetType TREATED_WOOD_SET = BlockSetType
            .register(new BlockSetType(GTCEu.id("treated_wood").toString()));
    public static final WoodType TREATED_WOOD_TYPE = WoodType
            .register(new WoodType(GTCEu.id("treated_wood").toString(), TREATED_WOOD_SET));

    public static final BlockEntry<Block> TREATED_WOOD_PLANK = REGISTRATE
            .block("treated_wood_planks", Block::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .lang("Treated Wood Planks")
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_GRAY))
            .tag(TagUtil.createBlockTag("treated_wood")) // matches IE treated wood tag
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(TagUtil.createItemTag("treated_wood"))
            .build()
            .register();

    public static final BlockEntry<SlabBlock> TREATED_WOOD_SLAB = REGISTRATE
            .block("treated_wood_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.SPRUCE_SLAB)
            .lang("Treated Wood Slab")
            .blockstate(
                    (ctx, prov) -> prov.slabBlock(ctx.getEntry(), prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get()),
                            prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get())))
            .tag(BlockTags.WOODEN_SLABS, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .tag(ItemTags.WOODEN_SLABS)
            .build()
            .register();

    public static final BlockEntry<FenceBlock> TREATED_WOOD_FENCE = REGISTRATE
            .block("treated_wood_fence", FenceBlock::new)
            .initialProperties(() -> Blocks.SPRUCE_FENCE)
            .lang("Treated Wood Fence")
            .blockstate((ctx, prov) -> prov.fenceBlock(ctx.getEntry(), prov.blockTexture(TREATED_WOOD_PLANK.get())))
            .tag(BlockTags.WOODEN_FENCES, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .model((ctx, prov) -> prov.fenceInventory(ctx.getName(),
                    GTBlocks.TREATED_WOOD_PLANK.getId().withPrefix("block/")))
            .tag(ItemTags.WOODEN_FENCES)
            .build()
            .register();

    public static final BlockEntry<GTStandingSignBlock> TREATED_WOOD_SIGN = REGISTRATE
            .block("treated_wood_sign", (p) -> new GTStandingSignBlock(p, TREATED_WOOD_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_SIGN)
            .lang("Treated Wood Sign")
            .blockstate((ctx, prov) -> prov.signBlock(ctx.get(), GTBlocks.TREATED_WOOD_WALL_SIGN.get(),
                    prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get())))
            .tag(BlockTags.STANDING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item((b, p) -> new SignItem(p, b, GTBlocks.TREATED_WOOD_WALL_SIGN.get()))
            .defaultModel()
            .tag(ItemTags.SIGNS)
            .build()
            .register();
    public static final BlockEntry<GTWallSignBlock> TREATED_WOOD_WALL_SIGN = REGISTRATE
            .block("treated_wood_wall_sign", (p) -> new GTWallSignBlock(p, TREATED_WOOD_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_WALL_SIGN)
            .lang("Treated Wood Wall Sign")
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.WALL_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .loot((table, block) -> table.dropOther(block, TREATED_WOOD_SIGN.asItem()))
            .register();
    public static final BlockEntry<GTCeilingHangingSignBlock> TREATED_WOOD_HANGING_SIGN = REGISTRATE
            .block("treated_wood_hanging_sign", (p) -> new GTCeilingHangingSignBlock(p, TREATED_WOOD_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_HANGING_SIGN)
            .lang("Treated Wood Hanging Sign")
            .blockstate((ctx, prov) -> {
                ModelFile model = prov.models().sign(ctx.getName(),
                        prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get()));
                prov.simpleBlock(ctx.get(), model);
            })
            .tag(BlockTags.CEILING_HANGING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item((b, p) -> new HangingSignItem(b, GTBlocks.TREATED_WOOD_WALL_HANGING_SIGN.get(), p))
            .defaultModel()
            .tag(ItemTags.HANGING_SIGNS)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .build()
            .register();
    public static final BlockEntry<GTWallHangingSignBlock> TREATED_WOOD_WALL_HANGING_SIGN = REGISTRATE
            .block("treated_wood_wall_hanging_sign", (p) -> new GTWallHangingSignBlock(p, TREATED_WOOD_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_WALL_HANGING_SIGN)
            .lang("Treated Wood Wall Hanging Sign")
            .blockstate((ctx, prov) -> {
                ModelFile model = prov.models().sign(ctx.getName(),
                        prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get()));
                prov.simpleBlock(ctx.get(), model);
            })
            .tag(BlockTags.WALL_HANGING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
            .loot((table, block) -> table.dropOther(block, TREATED_WOOD_HANGING_SIGN.asItem()))
            .register();
    public static final BlockEntry<PressurePlateBlock> TREATED_WOOD_PRESSURE_PLATE = REGISTRATE
            .block("treated_wood_pressure_plate",
                    (p) -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, p, TREATED_WOOD_SET))
            .initialProperties(() -> Blocks.SPRUCE_PRESSURE_PLATE)
            .lang("Treated Wood Pressure Plate")
            .tag(BlockTags.WOODEN_PRESSURE_PLATES, BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.pressurePlateBlock(ctx.getEntry(),
                    prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get())))
            .item()
            .tag(ItemTags.WOODEN_PRESSURE_PLATES)
            .build()
            .register();
    public static final BlockEntry<TrapDoorBlock> TREATED_WOOD_TRAPDOOR = REGISTRATE
            .block("treated_wood_trapdoor", (p) -> new TrapDoorBlock(p, RUBBER_SET))
            .initialProperties(() -> Blocks.SPRUCE_TRAPDOOR)
            .lang("Treated Wood Trapdoor")
            .blockstate((ctx, prov) -> prov.trapdoorBlock(ctx.get(), prov.blockTexture(ctx.get()), true))
            .tag(BlockTags.WOODEN_TRAPDOORS, BlockTags.MINEABLE_WITH_AXE)
            .item()
            .model((ctx, prov) -> prov.trapdoorOrientableBottom(ctx.getName(), ctx.getId().withPrefix("block/")))
            .tag(ItemTags.WOODEN_TRAPDOORS)
            .build()
            .register();
    public static final BlockEntry<StairBlock> TREATED_WOOD_STAIRS = REGISTRATE
            .block("treated_wood_stairs", (p) -> new StairBlock(TREATED_WOOD_PLANK::getDefaultState, p))
            .initialProperties(() -> Blocks.SPRUCE_STAIRS)
            .lang("Treated Wood Stairs")
            .tag(BlockTags.STAIRS, BlockTags.MINEABLE_WITH_AXE)
            .blockstate((ctx, prov) -> prov.stairsBlock(ctx.getEntry(),
                    prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get())))
            .item()
            .tag(ItemTags.STAIRS)
            .build()
            .register();
    public static final BlockEntry<ButtonBlock> TREATED_WOOD_BUTTON = REGISTRATE
            .block("treated_wood_button", (p) -> new ButtonBlock(p, TREATED_WOOD_SET, 30, true))
            .initialProperties(() -> Blocks.SPRUCE_BUTTON)
            .lang("Treated Wood Button")
            .tag(BlockTags.WOODEN_BUTTONS)
            .blockstate((ctx, prov) -> prov.buttonBlock(ctx.getEntry(), prov.blockTexture(TREATED_WOOD_PLANK.get())))
            .item()
            .model((ctx, prov) -> prov.buttonInventory(ctx.getName(),
                    GTBlocks.TREATED_WOOD_PLANK.getId().withPrefix("block/")))
            .tag(ItemTags.WOODEN_BUTTONS)
            .build()
            .register();
    public static final BlockEntry<FenceGateBlock> TREATED_WOOD_FENCE_GATE = REGISTRATE
            .block("treated_wood_fence_gate", (p) -> new FenceGateBlock(p, TREATED_WOOD_TYPE))
            .initialProperties(() -> Blocks.SPRUCE_FENCE_GATE)
            .lang("Treated Wood Fence Gate")
            .tag(BlockTags.FENCE_GATES)
            .blockstate((ctx, prov) -> prov.fenceGateBlock(ctx.getEntry(),
                    prov.blockTexture(GTBlocks.TREATED_WOOD_PLANK.get())))
            .item()
            .tag(ItemTags.FENCE_GATES)
            .build()
            .register();
    public static final BlockEntry<DoorBlock> TREATED_WOOD_DOOR = REGISTRATE
            .block("treated_wood_door", (p) -> new DoorBlock(p, TREATED_WOOD_SET))
            .initialProperties(() -> Blocks.SPRUCE_DOOR)
            .lang("Treated Wood Door")
            .loot((table, block) -> table.add(block, table.createDoorTable(block)))
            .addLayer(() -> RenderType::cutout)
            .blockstate((ctx, prov) -> prov.doorBlock(ctx.getEntry(), GTCEu.id("block/treated_wood_door_bottom"),
                    GTCEu.id("block/treated_wood_door_top")))
            .tag(BlockTags.WOODEN_DOORS)
            .item()
            .model((ctx, prov) -> prov.generated(ctx))
            .tag(ItemTags.WOODEN_DOORS)
            .build()
            .register();

    // Decoration Stuff
    public static final BlockEntry<Block> ACID_HAZARD_SIGN_BLOCK = createCasingBlock("acid_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_acidhazard"));
    public static final BlockEntry<Block> ANTIMATTER_HAZARD_SIGN_BLOCK = createCasingBlock(
            "antimatter_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_antimatterhazard"));
    public static final BlockEntry<Block> BIO_HAZARD_SIGN_BLOCK = createCasingBlock("bio_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_biohazard"));
    public static final BlockEntry<Block> BOSS_HAZARD_SIGN_BLOCK = createCasingBlock("boss_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_bosshazard"));
    public static final BlockEntry<Block> CAUSALITY_HAZARD_SIGN_BLOCK = createCasingBlock("causality_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_causalityhazard"));
    public static final BlockEntry<Block> EXPLOSION_HAZARD_SIGN_BLOCK = createCasingBlock("explosion_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_explosionhazard"));
    public static final BlockEntry<Block> FIRE_HAZARD_SIGN_BLOCK = createCasingBlock("fire_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_firehazard"));
    public static final BlockEntry<Block> FROST_HAZARD_SIGN_BLOCK = createCasingBlock("frost_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_frosthazard"));
    public static final BlockEntry<Block> GENERIC_HAZARD_SIGN_BLOCK = createCasingBlock("generic_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_generichazard"));
    public static final BlockEntry<Block> GREGIFICATION_HAZARD_SIGN_BLOCK = createCasingBlock(
            "gregification_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_gregificationhazard"));
    public static final BlockEntry<Block> HIGH_PRESSURE_HAZARD_SIGN_BLOCK = createCasingBlock(
            "high_pressure_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_highpressurehazard"));
    public static final BlockEntry<Block> HIGH_VOLTAGE_HAZARD_SIGN_BLOCK = createCasingBlock(
            "high_voltage_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_highvoltagehazard"));
    public static final BlockEntry<Block> HIGH_TEMPERATURE_HAZARD_SIGN_BLOCK = createCasingBlock(
            "high_temperature_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_hightemperaturehazard"));
    public static final BlockEntry<Block> LASER_HAZARD_SIGN_BLOCK = createCasingBlock("laser_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_laserhazard"));
    public static final BlockEntry<Block> MAGIC_HAZARD_SIGN_BLOCK = createCasingBlock("magic_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_magichazard"));
    public static final BlockEntry<Block> MAGNETIC_HAZARD_SIGN_BLOCK = createCasingBlock("magnetic_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_magneticfieldhazard"));
    public static final BlockEntry<Block> MOB_INFESTATION_HAZARD_SIGN_BLOCK = createCasingBlock(
            "mob_infestation_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_mobhazard"));
    public static final BlockEntry<Block> MOB_SPAWNER_HAZARD_SIGN_BLOCK = createCasingBlock(
            "mob_spawner_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_mobspawnhazard"));
    public static final BlockEntry<Block> NOISE_HAZARD_SIGN_BLOCK = createCasingBlock("noise_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_noisehazard"));
    public static final BlockEntry<Block> RADIOACTIVE_HAZARD_SIGN_BLOCK = createCasingBlock(
            "radioactive_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_radioactivehazard"));
    public static final BlockEntry<Block> SPATIAL_STORAGE_HAZARD_SIGN_BLOCK = createCasingBlock(
            "spatial_storage_hazard_sign_block", GTCEu.id("block/casings/signs/machine_casing_spatialhazard"));
    public static final BlockEntry<Block> TURRET_HAZARD_SIGN_BLOCK = createCasingBlock("turret_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_turrethazard"));
    public static final BlockEntry<Block> VOID_HAZARD_SIGN_BLOCK = createCasingBlock("void_hazard_sign_block",
            GTCEu.id("block/casings/signs/machine_casing_voidhazard"));
    public static final BlockEntry<Block> YELLOW_STRIPES_BLOCK_A = createCasingBlock("yellow_stripes_block_a",
            GTCEu.id("block/casings/signs/machine_casing_stripes_a"));
    public static final BlockEntry<Block> YELLOW_STRIPES_BLOCK_B = createCasingBlock("yellow_stripes_block_b",
            GTCEu.id("block/casings/signs/machine_casing_stripes_b"));

    public static Table<StoneBlockType, StoneTypes, BlockEntry<Block>> STONE_BLOCKS;
    public static Map<TagPrefix, Supplier<BlockState>> COBBLE_BLOCKS = new HashMap<>();

    public static BlockEntry<Block> RED_GRANITE;
    public static BlockEntry<Block> MARBLE;
    public static BlockEntry<Block> LIGHT_CONCRETE;
    public static BlockEntry<Block> DARK_CONCRETE;

    public static BlockEntry<Block> BRITTLE_CHARCOAL = REGISTRATE
            .block("brittle_charcoal", Block::new)
            .properties(p -> p.strength(0.5f).explosionResistance(8.0f).sound(SoundType.STONE))
            .loot((table, block) -> table.add(block,
                    table.createSingleItemTable(Items.CHARCOAL, UniformGenerator.between(1.0F, 3.0F))))
            .lang("Brittle Charcoal")
            .exBlockstate(GTModels.cubeAllModel(GTCEu.id("block/misc/brittle_charcoal")))
            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .item((b, p) -> new BlockItem(b, p) {

                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                            TooltipFlag isAdvanced) {
                    super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
                    tooltipComponents.add(1, Component.translatable("tile.gtceu.brittle_charcoal.tooltip.0"));
                    tooltipComponents.add(2, Component.translatable("tile.gtceu.brittle_charcoal.tooltip.1"));
                }
            })
            .build()
            .register();

    public static void generateStoneBlocks() {
        // Stone type blocks
        ImmutableTable.Builder<StoneBlockType, StoneTypes, BlockEntry<Block>> builder = ImmutableTable.builder();
        for (StoneTypes strata : StoneTypes.values()) {
            if (!strata.generateBlocks) continue;
            for (StoneBlockType type : StoneBlockType.values()) {
                String blockId = type.blockId.formatted(strata.getSerializedName());
                if (BuiltInRegistries.BLOCK.containsKey(new ResourceLocation(blockId))) continue;
                var entry = REGISTRATE.block(blockId, Block::new)
                        .initialProperties(() -> Blocks.STONE)
                        .properties(p -> p.strength(type.hardness, type.resistance).mapColor(strata.mapColor))
                        .transform(type == StoneBlockType.STONE ?
                                GTBlocks.unificationBlock(strata.getTagPrefix(), strata.getMaterial()) :
                                builder2 -> builder2)
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE, CustomTags.NEEDS_WOOD_TOOL)
                        .loot((tables, block) -> {
                            if (type == StoneBlockType.STONE) {
                                tables.add(block, tables.createSingleItemTableWithSilkTouch(block,
                                        STONE_BLOCKS.get(StoneBlockType.COBBLE, strata).get()));
                            } else {
                                tables.add(block, tables.createSingleItemTable(block));
                            }
                        })
                        .item()
                        .build();
                if (type == StoneBlockType.STONE && strata.isNatural()) {
                    entry.tag(BlockTags.STONE_ORE_REPLACEABLES, BlockTags.BASE_STONE_OVERWORLD,
                            BlockTags.DRIPSTONE_REPLACEABLE, BlockTags.MOSS_REPLACEABLE)
                            .blockstate(GTModels.randomRotatedModel(GTCEu.id(ModelProvider.BLOCK_FOLDER + "/stones/" +
                                    strata.getSerializedName() + "/" + type.id)));
                } else {
                    entry.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                            prov.models().singleTexture(ctx.getName(),
                                    prov.mcLoc(ModelProvider.BLOCK_FOLDER + "/cube_all"), "all",
                                    prov.modLoc(ModelProvider.BLOCK_FOLDER + "/stones/" + strata.getSerializedName() +
                                            "/" + type.id))));
                }
                if (type == StoneBlockType.STONE) {
                    entry.tag(Tags.Blocks.STONE);
                }
                if (type == StoneBlockType.COBBLE) {
                    entry.tag(Tags.Blocks.COBBLESTONE);
                }
                builder.put(type, strata, entry.register());
            }
        }
        STONE_BLOCKS = builder.build();

        STONE_BLOCKS.row(StoneBlockType.COBBLE).forEach((ore, block) -> {
            if (ore.generateBlocks) {
                GTBlocks.registerCobbleBlock(ore.getTagPrefix(), block::getDefaultState);
            }
        });

        RED_GRANITE = STONE_BLOCKS.get(StoneBlockType.STONE, StoneTypes.RED_GRANITE);
        MARBLE = STONE_BLOCKS.get(StoneBlockType.STONE, StoneTypes.MARBLE);
        LIGHT_CONCRETE = STONE_BLOCKS.get(StoneBlockType.STONE, StoneTypes.CONCRETE_LIGHT);
        DARK_CONCRETE = STONE_BLOCKS.get(StoneBlockType.STONE, StoneTypes.CONCRETE_DARK);
    }

    public static final BlockEntry<FoamBlock> FOAM = REGISTRATE
            .block("foam", p -> new FoamBlock(p, false))
            .properties(p -> p.strength(0.5F, 0.3F)
                    .randomTicks()
                    .sound(SoundType.SNOW)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion().noCollission().noLootTable())
            .simpleItem()
            .register();

    public static final BlockEntry<FoamBlock> REINFORCED_FOAM = REGISTRATE
            .block("reinforced_foam", p -> new FoamBlock(p, true))
            .initialProperties(FOAM)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> PETRIFIED_FOAM = REGISTRATE
            .block("petrified_foam", Block::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.strength(1.0F, 4.0F).sound(SoundType.SNOW))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .simpleItem()
            .register();
    public static final BlockEntry<Block> REINFORCED_STONE = REGISTRATE
            .block("reinforced_stone", Block::new)
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.strength(4.0F, 16.0F))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
            .simpleItem()
            .register();

    // Lamps
    public static final Map<DyeColor, BlockEntry<LampBlock>> LAMPS;
    public static final Map<DyeColor, BlockEntry<LampBlock>> BORDERLESS_LAMPS;
    static {
        ImmutableMap.Builder<DyeColor, BlockEntry<LampBlock>> lampBuilder = new ImmutableMap.Builder<>();
        DyeColor[] colors = DyeColor.values();
        for (DyeColor dyeColor : colors) {
            lampBuilder.put(dyeColor,
                    REGISTRATE.block("%s_lamp".formatted(dyeColor.getName()), (p) -> new LampBlock(p, dyeColor, true))
                            .initialProperties(() -> Blocks.GLASS)
                            .properties(p -> p.strength(0.3f, 8.0f).sound(SoundType.GLASS))
                            .addLayer(() -> RenderType::cutout)
                            .blockstate(GTModels.lampModel(dyeColor, true))
                            .item(LampBlockItem::new)
                            .build()
                            .register());
        }
        LAMPS = lampBuilder.build();
        ImmutableMap.Builder<DyeColor, BlockEntry<LampBlock>> borderlessLampBuilder = new ImmutableMap.Builder<>();
        for (DyeColor dyeColor : colors) {
            borderlessLampBuilder.put(dyeColor, REGISTRATE
                    .block("%s_borderless_lamp".formatted(dyeColor.getName()), (p) -> new LampBlock(p, dyeColor, false))
                    .initialProperties(() -> Blocks.GLASS)
                    .properties(p -> p.strength(0.3f, 8.0f).sound(SoundType.GLASS))
                    .blockstate(GTModels.lampModel(dyeColor, false))
                    .item(LampBlockItem::new)
                    .build()
                    .register());
        }
        BORDERLESS_LAMPS = borderlessLampBuilder.build();
    }

    // Decorations
    public static final Map<DyeColor, BlockEntry<Block>> METAL_SHEETS;
    public static final Map<DyeColor, BlockEntry<Block>> LARGE_METAL_SHEETS;
    public static final Map<DyeColor, BlockEntry<Block>> STUDS;

    static {
        DyeColor[] colors = DyeColor.values();
        ImmutableMap.Builder<DyeColor, BlockEntry<Block>> metalsheetBuilder = new ImmutableMap.Builder<>();
        for (DyeColor dyeColor : colors) {
            metalsheetBuilder.put(dyeColor, REGISTRATE.block("%s_metal_sheet".formatted(dyeColor.getName()), Block::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.strength(2.0F, 5.0F).mapColor(dyeColor))
                    .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                            prov.models().cubeAll(ctx.getName(),
                                    GTCEu.id("block/decoration/metalsheet_%s".formatted(dyeColor.getName())))))
                    .simpleItem()
                    .register());
        }
        METAL_SHEETS = metalsheetBuilder.build();

        ImmutableMap.Builder<DyeColor, BlockEntry<Block>> largeMetalsheetBuilder = new ImmutableMap.Builder<>();
        for (DyeColor dyeColor : colors) {
            largeMetalsheetBuilder.put(dyeColor,
                    REGISTRATE.block("%s_large_metal_sheet".formatted(dyeColor.getName()), Block::new)
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .properties(p -> p.strength(2.0F, 5.0F).mapColor(dyeColor))
                            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(), prov.models().cubeAll(ctx.getName(),
                                    GTCEu.id("block/decoration/large_metalsheet_%s".formatted(dyeColor.getName())))))
                            .simpleItem()
                            .register());
        }
        LARGE_METAL_SHEETS = largeMetalsheetBuilder.build();

        ImmutableMap.Builder<DyeColor, BlockEntry<Block>> studsBuilder = new ImmutableMap.Builder<>();
        for (DyeColor dyeColor : colors) {
            studsBuilder.put(dyeColor, REGISTRATE.block("%s_studs".formatted(dyeColor.getName()), Block::new)
                    .initialProperties(() -> Blocks.WHITE_WOOL)
                    .properties(p -> p.strength(1.5F, 2.5F).mapColor(dyeColor))
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, CustomTags.NEEDS_WOOD_TOOL)
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                            prov.models().cubeAll(ctx.getName(),
                                    GTCEu.id("block/decoration/studs_%s".formatted(dyeColor.getName())))))
                    .simpleItem()
                    .register());
        }
        STUDS = studsBuilder.build();
    }

    public static <P, T extends Block,
            S2 extends BlockBuilder<T, P>> NonNullFunction<S2, S2> unificationBlock(@NotNull TagPrefix tagPrefix,
                                                                                    @NotNull Material mat) {
        return builder -> {
            builder.onRegister(block -> {
                Supplier<Block> blockSupplier = GTMemoizer.memoizeBlockSupplier(() -> block);
                MaterialEntry entry = new MaterialEntry(tagPrefix, mat);
                GTMaterialItems.toUnify.put(entry, blockSupplier);
                ItemMaterialData.registerMaterialEntry(blockSupplier, entry);
            });
            return builder;
        };
    }

    public static void registerCobbleBlock(TagPrefix orePrefix, Supplier<BlockState> state) {
        COBBLE_BLOCKS.put(orePrefix, state);
    }

    public static void removeCobbleBlock(TagPrefix orePrefix) {
        COBBLE_BLOCKS.remove(orePrefix);
    }

    public static void init() {
        // Decor Blocks
        generateStoneBlocks();
        initializeCobbleReplacements();

        // Procedural Blocks
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_BLOCK);
        GTMaterialBlocks.generateMaterialBlocks();   // Compressed Blocks
        GTMaterialBlocks.generateOreBlocks();        // Ore Blocks
        GTMaterialBlocks.generateOreIndicators();    // Ore Indicators
        GTMaterialBlocks.MATERIAL_BLOCKS = GTMaterialBlocks.MATERIAL_BLOCKS_BUILDER.build();

        // Procedural Pipes/Wires
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_PIPE);
        GTMaterialBlocks.generateCableBlocks();        // Cable & Wire Blocks
        GTMaterialBlocks.generateFluidPipeBlocks();    // Fluid Pipe Blocks
        GTMaterialBlocks.generateItemPipeBlocks();     // Item Pipe Blocks
        generateLaserPipeBlocks();    // Laser Pipe Blocks
        generateOpticalPipeBlocks();  // Optical Pipe Blocks
        generateDuctPipeBlocks();     // Duct Pipe Blocks

        // Remove Builder Tables
        GTMaterialBlocks.MATERIAL_BLOCKS_BUILDER = null;
        GTMaterialBlocks.SURFACE_ROCK_BLOCKS_BUILDER = null;
        GTMaterialBlocks.CABLE_BLOCKS_BUILDER = null;
        GTMaterialBlocks.FLUID_PIPE_BLOCKS_BUILDER = null;
        GTMaterialBlocks.ITEM_PIPE_BLOCKS_BUILDER = null;

        // GCYM
        GCYMBlocks.init();
    }

    private static void initializeCobbleReplacements() {
        // replacement blocks for mc based stone types
        registerCobbleBlock(TagPrefix.ore, Blocks.COBBLESTONE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreDeepslate, Blocks.COBBLED_DEEPSLATE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreAndesite, Blocks.ANDESITE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreDiorite, Blocks.DIORITE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreGranite, Blocks.GRANITE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreSand, Blocks.SAND::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreGravel, Blocks.GRAVEL::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreRedSand, Blocks.RED_SAND::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreBasalt, Blocks.BASALT::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreBlackstone, Blocks.BLACKSTONE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreEndstone, Blocks.END_STONE::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreNetherrack, Blocks.NETHERRACK::defaultBlockState);
        registerCobbleBlock(TagPrefix.oreTuff, Blocks.TUFF::defaultBlockState);
    }

    public static boolean doMetalPipe(Material material) {
        return GTValues.FOOLS.getAsBoolean() && material.hasProperty(PropertyKey.INGOT) &&
                !material.hasProperty(PropertyKey.POLYMER) && !material.hasProperty(PropertyKey.WOOD);
    }

    /**
     * kinda nasty block property copy function because one doesn't exist.
     * 
     * @param props the props to copy
     * @return a shallow copy of the block properties like {@link BlockBehaviour.Properties#copy(BlockBehaviour)} does
     */
    public static BlockBehaviour.Properties copy(BlockBehaviour.Properties props, BlockBehaviour.Properties newProps) {
        if (props == null) {
            return newProps;
        }
        newProps.destroyTime(((BlockPropertiesAccessor) props).getDestroyTime());
        newProps.explosionResistance(((BlockPropertiesAccessor) props).getExplosionResistance());
        if (!((BlockPropertiesAccessor) props).isHasCollision()) newProps.noCollission();
        if (((BlockPropertiesAccessor) props).isIsRandomlyTicking()) newProps.randomTicks();
        newProps.lightLevel(((BlockPropertiesAccessor) props).getLightEmission());
        newProps.mapColor(((BlockPropertiesAccessor) props).getMapColor());
        newProps.sound(((BlockPropertiesAccessor) props).getSoundType());
        newProps.friction(((BlockPropertiesAccessor) props).getFriction());
        newProps.speedFactor(((BlockPropertiesAccessor) props).getSpeedFactor());
        if (((BlockPropertiesAccessor) props).isDynamicShape()) newProps.dynamicShape();
        if (!((BlockPropertiesAccessor) props).isCanOcclude()) newProps.noOcclusion();
        if (((BlockPropertiesAccessor) props).isIsAir()) newProps.air();
        if (((BlockPropertiesAccessor) props).isIgnitedByLava()) newProps.ignitedByLava();
        if (((BlockPropertiesAccessor) props).isLiquid()) newProps.liquid();
        if (((BlockPropertiesAccessor) props).isForceSolidOff()) newProps.forceSolidOff();
        if (((BlockPropertiesAccessor) props).isForceSolidOn()) newProps.forceSolidOn();
        newProps.pushReaction(((BlockPropertiesAccessor) props).getPushReaction());
        if (((BlockPropertiesAccessor) props).isRequiresCorrectToolForDrops()) newProps.requiresCorrectToolForDrops();
        ((BlockPropertiesAccessor) newProps).setOffsetFunction(((BlockPropertiesAccessor) props).getOffsetFunction());
        if (!((BlockPropertiesAccessor) props).isSpawnParticlesOnBreak()) newProps.noParticlesOnBreak();
        ((BlockPropertiesAccessor) newProps)
                .setRequiredFeatures(((BlockPropertiesAccessor) props).getRequiredFeatures());
        newProps.emissiveRendering(((BlockPropertiesAccessor) props).getEmissiveRendering());
        newProps.instrument(((BlockPropertiesAccessor) props).getInstrument());
        if (((BlockPropertiesAccessor) props).isReplaceable()) newProps.replaceable();
        return newProps;
    }
}
