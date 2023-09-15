package com.gregtechceu.gtceu.common.data;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.block.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.registry.registrate.CompassNode;
import com.gregtechceu.gtceu.api.registry.registrate.CompassSection;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.FORCE_GENERATE_BLOCK;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FRAME;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GCyMBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTModels.createModelBlockState;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlocks
 */
public class GTBlocks {

    //////////////////////////////////////
    //*****     Material Blocks    *****//
    //////////////////////////////////////

    public static Table<TagPrefix, Material, BlockEntry<? extends MaterialBlock>> MATERIAL_BLOCKS;
    public static void generateMaterialBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_BLOCK);
        ImmutableTable.Builder<TagPrefix, Material, BlockEntry<? extends MaterialBlock>> builder = ImmutableTable.builder();
        for (Material material : GTRegistries.MATERIALS) {
            // Compressed Block
            if ((material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) || material.hasFlag(FORCE_GENERATE_BLOCK))
                    && !TagPrefix.block.isIgnored(material)) {
                var entry = REGISTRATE.block("%s_block".formatted(material.getName()), properties -> new MaterialBlock(properties.noLootTable(), TagPrefix.block, material))
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .transform(unificationBlock(TagPrefix.block, material))
                        .addLayer(() -> RenderType::solid)
                        .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                        .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                        .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                        .color(() -> MaterialBlock::tintedColor)
                        .item(MaterialBlockItem::create)
                        .onRegister(MaterialBlockItem::onRegister)
                        .model(NonNullBiConsumer.noop())
                        .color(() -> MaterialBlockItem::tintColor)
                        .build()
                        .register();
                builder.put(TagPrefix.block, material, entry);
            }

            // Frame Block
            if (material.hasProperty(PropertyKey.DUST) && material.hasFlag(GENERATE_FRAME)) {
                var entry = REGISTRATE.block("%s_frame".formatted(material.getName()), properties -> new MaterialBlock(properties.noLootTable(), TagPrefix.frameGt, material))
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .properties(BlockBehaviour.Properties::noOcclusion)
                        .transform(unificationBlock(TagPrefix.frameGt, material))
                        .addLayer(() -> RenderType::cutoutMipped)
                        .blockstate(NonNullBiConsumer.noop())
                        .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                        .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                        .color(() -> MaterialBlock::tintedColor)
                        .item(MaterialBlockItem::create)
                        .onRegister(MaterialBlockItem::onRegister)
                        .model(NonNullBiConsumer.noop())
                        .color(() -> MaterialBlockItem::tintColor)
                        .build()
                        .register();
                builder.put(TagPrefix.frameGt, material, entry);
            }

            // Ore Block
            if (material.hasProperty(PropertyKey.ORE)) {
                if (!TagPrefix.rawOreBlock.isIgnored(material) && TagPrefix.rawOreBlock.generationCondition().test(material)) {
                    var entry = REGISTRATE.block("raw_%s_block".formatted(material.getName()), properties -> new MaterialBlock(properties.noLootTable(), TagPrefix.rawOreBlock, material))
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .transform(unificationBlock(TagPrefix.rawOreBlock, material))
                            .addLayer(() -> RenderType::solid)
                            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                            .color(() -> MaterialBlock::tintedColor)
                            .item(MaterialBlockItem::create)
                            .onRegister(MaterialBlockItem::onRegister)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> MaterialBlockItem::tintColor)
                            .onRegister(compassNodeExist(GTCompassSections.GENERATIONS, "raw_ore", GTCompassNodes.ORE))
                            .build()
                            .register();
                    builder.put(TagPrefix.rawOreBlock, material, entry);
                }

                var oreProperty = material.getProperty(PropertyKey.ORE);
                for (var ore : TagPrefix.ORES.entrySet()) {
                    if (ore.getKey().isIgnored(material)) continue;
                    var oreTag = ore.getKey();
                    final TagPrefix.OreType oreType = ore.getValue();
                    var entry = REGISTRATE.block("%s%s_ore".formatted(oreTag != TagPrefix.ore ? FormattingUtil.toLowerCaseUnder(oreTag.name) + "_" : "", material.getName()),
//                                    oreType.material(),
                                    properties -> new MaterialBlock(properties, oreTag, material, Platform.isClient() ? new OreBlockRenderer(oreType.stoneType(),
                                            Suppliers.memoize(() -> Objects.requireNonNull(oreTag.materialIconType()).getBlockTexturePath(material.getMaterialIconSet(), true)),
                                            oreProperty.isEmissive()) : null))
                            .initialProperties(() -> oreType.stoneType().get().getBlock())
                            .properties(properties -> {
                                properties.noLootTable();
                                if (oreType.color() != null) properties.mapColor(oreType.color());
                                if (oreType.sound() != null) properties.sound(oreType.sound());
                                return properties;
                            })
                            .transform(unificationBlock(oreTag, material))
                            .addLayer(() -> RenderType::cutoutMipped)
                            .blockstate(NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                            .color(() -> MaterialBlock::tintedColor)
                            .item(MaterialBlockItem::create)
                            .onRegister(MaterialBlockItem::onRegister)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> MaterialBlockItem::tintColor)
                            .onRegister(compassNodeExist(GTCompassSections.GENERATIONS, oreTag.name, GTCompassNodes.ORE))
                            .build()
                            .register();
                    builder.put(oreTag, material, entry);
                }
            }
        }
        MATERIAL_BLOCKS = builder.build();
    }

    //////////////////////////////////////
    //*****     Material Pipes    ******//
    //////////////////////////////////////
    public static Table<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS;
    public static void generateCableBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_PIPE);

        // Cable/Wire Blocks
        ImmutableTable.Builder<TagPrefix, Material, BlockEntry<CableBlock>> builder = ImmutableTable.builder();
        for (Insulation insulation : Insulation.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                if (material.hasProperty(PropertyKey.WIRE) && !insulation.tagPrefix.isIgnored(material)) {
                    var entry = REGISTRATE.block("%s_%s".formatted(material.getName(), insulation.name), p -> new CableBlock(p.noLootTable(), insulation, material))
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .properties(p -> p.dynamicShape().noOcclusion())
                            .transform(unificationBlock(insulation.tagPrefix, material))
                            .blockstate(NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                            .addLayer(() -> RenderType::cutoutMipped)
                            .color(() -> MaterialPipeBlock::tintedColor)
                            .item(MaterialPipeBlockItem::new)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> MaterialPipeBlockItem::tintColor)
                            .build()
                            .register();
                    builder.put(insulation.tagPrefix, material, entry);
                }
            }
        }
        CABLE_BLOCKS = builder.build();
    }

    public static Table<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS;

    public static void generatePipeBlocks() {
        // Fluid Pipe Blocks
        ImmutableTable.Builder<TagPrefix, Material, BlockEntry<FluidPipeBlock>> builder = ImmutableTable.builder();
        for (var fluidPipeType : FluidPipeType.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                if (material.hasProperty(PropertyKey.FLUID_PIPE) && !fluidPipeType.tagPrefix.isIgnored(material)) {
                    var entry = REGISTRATE.block( "%s_%s_fluid_pipe".formatted(material.getName(), fluidPipeType.name), p -> new FluidPipeBlock(p.noLootTable(), fluidPipeType, material))
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .properties(p -> {
                                if (doMetalPipe(material)) {
                                    p.sound(GTSoundTypes.METAL_PIPE);
                                }
                                return p.dynamicShape().noOcclusion();
                            })
                            .transform(unificationBlock(fluidPipeType.tagPrefix, material))
                            .blockstate(NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                            .addLayer(() -> RenderType::cutoutMipped)
                            .color(() -> MaterialPipeBlock::tintedColor)
                            .item(MaterialPipeBlockItem::new)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> MaterialPipeBlockItem::tintColor)
                            .build()
                            .register();
                    builder.put(fluidPipeType.tagPrefix, material, entry);
                }
            }
        }
        FLUID_PIPE_BLOCKS = builder.build();
    }

    @SuppressWarnings("unchecked")
    private static TagKey<Block>[] getPipeTags(Material material) {
        TagKey<Block>[] tags = new TagKey[2];
        tags[0] = GTToolType.WRENCH.harvestTag;
        if (material.hasProperty(PropertyKey.WOOD)) {
            tags[1] = BlockTags.MINEABLE_WITH_AXE;
        } else tags[1] = BlockTags.MINEABLE_WITH_PICKAXE;
        return tags;
    }

    static {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.DECORATION);
    }

    //////////////////////////////////////
    //******     Casing Blocks     *****//
    //////////////////////////////////////



    // Multiblock Machine Casing Blocks
    public static final BlockEntry<Block> CASING_COKE_BRICKS = createCasingBlock("coke_oven_bricks", GTCEu.id("block/casings/solid/machine_coke_bricks"));
    public static final BlockEntry<Block> CASING_PRIMITIVE_BRICKS = createCasingBlock("firebricks", GTCEu.id("block/casings/solid/machine_primitive_bricks"));
    public static final BlockEntry<Block> CASING_BRONZE_BRICKS = createCasingBlock("steam_machine_casing", GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"));
    public static final BlockEntry<Block> CASING_INVAR_HEATPROOF = createCasingBlock("heatproof_machine_casing", GTCEu.id("block/casings/solid/machine_casing_heatproof"));
    public static final BlockEntry<Block> CASING_ALUMINIUM_FROSTPROOF = createCasingBlock("frostproof_machine_casing", GTCEu.id("block/casings/solid/machine_casing_frost_proof"));
    public static final BlockEntry<Block> CASING_STEEL_SOLID = createCasingBlock("solid_machine_casing", GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
    public static final BlockEntry<Block> CASING_STAINLESS_CLEAN = createCasingBlock("clean_machine_casing", GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_STABLE = createCasingBlock("stable_machine_casing", GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_ROBUST = createCasingBlock("robust_machine_casing", GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));
    public static final BlockEntry<Block> CASING_PTFE_INERT = createCasingBlock("inert_machine_casing", GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"));
    public static final BlockEntry<Block> CASING_HSSE_STURDY = createCasingBlock("sturdy_machine_casing", GTCEu.id("block/casings/solid/machine_casing_study_hsse"));
    public static final BlockEntry<Block> CASING_TEMPERED_GLASS = createGlassCasingBlock("tempered_glass", GTCEu.id("block/casings/transparent/tempered_glass"), () -> RenderType::translucent);


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
        //GCyM
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
    public static final BlockEntry<Block> CASING_GRATE = createCasingBlock("assembly_line_grating", GTCEu.id("block/casings/pipe/machine_casing_grate"));
    public static final BlockEntry<Block> CASING_ASSEMBLY_CONTROL = createCasingBlock("assembly_line_casing", GTCEu.id("block/casings/mechanic/machine_casing_assembly_control"));
    public static final BlockEntry<Block> CASING_LAMINATED_GLASS = createGlassCasingBlock("laminated_glass", GTCEu.id("block/casings/transparent/laminated_glass"), () -> RenderType::cutoutMipped);
    public static final BlockEntry<ActiveBlock> CASING_ASSEMBLY_LINE = createActiveCasing("assembly_line_unit", "block/variant/assembly_line");


    // Gear Boxes
    public static final BlockEntry<Block> CASING_BRONZE_GEARBOX = createCasingBlock("bronze_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_bronze"));
    public static final BlockEntry<Block> CASING_STEEL_GEARBOX = createCasingBlock("steel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_steel"));
    public static final BlockEntry<Block> CASING_STAINLESS_STEEL_GEARBOX = createCasingBlock("stainless_steel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_stainless_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_GEARBOX = createCasingBlock("titanium_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_GEARBOX = createCasingBlock("tungstensteel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_tungstensteel"));

    // Turbine Casings
    public static final BlockEntry<Block> CASING_STEEL_TURBINE = createCasingBlock("steel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_TURBINE = createCasingBlock("titanium_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"));
    public static final BlockEntry<Block> CASING_STAINLESS_TURBINE = createCasingBlock("stainless_steel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_TURBINE = createCasingBlock("tungstensteel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"));

    // Pipe casings
    public static final BlockEntry<Block> CASING_BRONZE_PIPE = createCasingBlock("bronze_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_bronze"));
    public static final BlockEntry<Block> CASING_STEEL_PIPE = createCasingBlock("steel_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_PIPE = createCasingBlock("titanium_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_PIPE = createCasingBlock("tungstensteel_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_tungstensteel"));
    public static final BlockEntry<Block> CASING_POLYTETRAFLUOROETHYLENE_PIPE = createPipeCasingBlock("ptfe", GTCEu.id("block/casings/pipe/machine_casing_pipe_polytetrafluoroethylene"));
    public static final BlockEntry<MinerPipeBlock> MINER_PIPE = REGISTRATE.block("miner_pipe", MinerPipeBlock::new)
            .initialProperties(() -> Blocks.BEDROCK)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> createModelBlockState(ctx, prov, GTCEu.id("block/miner_pipe")))
            .tag(BlockTags.DRAGON_IMMUNE, BlockTags.WITHER_IMMUNE, BlockTags.INFINIBURN_END, BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS)
            .register();

    // The Pump Deck
    public static final BlockEntry<Block> CASING_PUMP_DECK = REGISTRATE.block("pump_deck", p -> (Block) new RendererBlock(p,
                    Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                            Map.of("bottom",  GTCEu.id("block/casings/pump_deck/bottom"),
                                    "top",  GTCEu.id("block/casings/pump_deck/top"),
                                    "side",  GTCEu.id("block/casings/pump_deck/side"))) : null))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.WOOD).mapColor(MapColor.WOOD))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(NonNullBiConsumer.noop())
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_AXE)
            .item(RendererBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    // todo multiblock tanks
    //WOOD_WALL("wood_wall", GTCEu.id(""));


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
    public static final Map<ICoilType, Supplier<CoilBlock>> ALL_COILS = new HashMap<>();
    public static final BlockEntry<CoilBlock> COIL_CUPRONICKEL = createCoilBlock(CoilBlock.CoilType.CUPRONICKEL);
    public static final BlockEntry<CoilBlock> COIL_KANTHAL = createCoilBlock(CoilBlock.CoilType.KANTHAL);
    public static final BlockEntry<CoilBlock> COIL_NICHROME = createCoilBlock(CoilBlock.CoilType.NICHROME);
    public static final BlockEntry<CoilBlock> COIL_TUNGSTENSTEEL = createCoilBlock(CoilBlock.CoilType.TUNGSTENSTEEL);
    public static final BlockEntry<CoilBlock> COIL_HSSG = createCoilBlock(CoilBlock.CoilType.HSSG);
    public static final BlockEntry<CoilBlock> COIL_NAQUADAH = createCoilBlock(CoilBlock.CoilType.NAQUADAH);
    public static final BlockEntry<CoilBlock> COIL_TRINIUM = createCoilBlock(CoilBlock.CoilType.TRINIUM);
    public static final BlockEntry<CoilBlock> COIL_TRITANIUM = createCoilBlock(CoilBlock.CoilType.TRITANIUM);

    public static final BlockEntry<ActiveBlock> CASING_ENGINE_INTAKE = createActiveCasing("engine_intake_casing", "block/variant/engine_intake");
    public static final BlockEntry<ActiveBlock> CASING_EXTREME_ENGINE_INTAKE = createActiveCasing("extreme_engine_intake_casing", "block/variant/extreme_engine_intake");

    //Fusion
    public static final Map<IFusionCasingType, Supplier<FusionCasingBlock>> ALL_FUSION_CASINGS = new HashMap<>();
    public static final BlockEntry<FusionCasingBlock> SUPERCONDUCTING_COIL = createFusionCasing(FusionCasingBlock.CasingType.SUPERCONDUCTING_COIL);
    public static final BlockEntry<FusionCasingBlock> FUSION_COIL = createFusionCasing(FusionCasingBlock.CasingType.FUSION_COIL);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_MK2 = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING_MK2);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_MK3 = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING_MK3);
    public static final BlockEntry<Block> FUSION_GLASS = createGlassCasingBlock("fusion_glass", GTCEu.id("block/casings/transparent/fusion_glass"), () -> RenderType::cutoutMipped);

    // Cleanroom
    public static final Map<IFilterType, Supplier<Block>> ALL_FILTERS = new HashMap<>();
    public static final BlockEntry<Block> PLASTCRETE = createCasingBlock("plascrete", GTCEu.id("block/casings/cleanroom/plascrete"));
    public static final BlockEntry<Block> FILTER_CASING = createCleanroomFilter(CleanroomFilterType.FILTER_CASING);
    public static final BlockEntry<Block> FILTER_CASING_STERILE = createCleanroomFilter(CleanroomFilterType.FILTER_CASING_STERILE);
    public static final BlockEntry<Block> CLEANROOM_GLASS = createGlassCasingBlock("cleanroom_glass", GTCEu.id("block/casings/transparent/cleanroom_glass"), () -> RenderType::cutoutMipped);


    // Fireboxes
    public static final Map<BoilerFireboxType, BlockEntry<ActiveBlock>> ALL_FIREBOXES = new HashMap<>();
    public static final BlockEntry<ActiveBlock> FIREBOX_BRONZE = createFireboxCasing(BoilerFireboxType.BRONZE_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_STEEL = createFireboxCasing(BoilerFireboxType.STEEL_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TITANIUM = createFireboxCasing(BoilerFireboxType.TITANIUM_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TUNGSTENSTEEL = createFireboxCasing(BoilerFireboxType.TUNGSTENSTEEL_FIREBOX);



    private static BlockEntry<Block> createPipeCasingBlock(String name, ResourceLocation texture) {
        return createPipeCasingBlock(name, texture, () -> Blocks.IRON_BLOCK);
    }

    private static BlockEntry<Block> createPipeCasingBlock(String name, ResourceLocation texture, NonNullSupplier<? extends Block> properties) {
        return REGISTRATE.block("%s_pipe_casing".formatted(name.toLowerCase(Locale.ROOT)), p -> (Block) new RendererBlock(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                                Map.of("all", texture)) : null))
                .lang("%s Pipe Casing".formatted(name))
                .initialProperties(properties)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }


    // THIS IS JUST FOR PTFE PIPE CASING
    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, RendererBlock::new, texture, () -> Blocks.IRON_BLOCK, () -> RenderType::cutoutMipped);
    }

    private static BlockEntry<Block> createGlassCasingBlock(String name, ResourceLocation texture, Supplier<Supplier<RenderType>> type) {
        return createCasingBlock(name, RendererGlassBlock::new, texture, () -> Blocks.GLASS, type);
    }

    public static BlockEntry<Block> createCasingBlock(String name, BiFunction<BlockBehaviour.Properties, IRenderer, ? extends RendererBlock> blockSupplier, ResourceLocation texture, NonNullSupplier<? extends Block> properties, Supplier<Supplier<RenderType>> type) {
        return REGISTRATE.block(name, p -> (Block) blockSupplier.apply(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                        Map.of("all", texture)) : null))
                .initialProperties(properties)
                .addLayer(type)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<Block> createMachineCasingBlock(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        return REGISTRATE.block("%s_machine_casing".formatted(tierName), p -> (Block) new RendererBlock(p,
                        Platform.isClient() ? new TextureOverrideRenderer( GTCEu.id("block/cube_bottom_top_tintindex"),
                                Map.of("bottom",  GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)),
                                        "top",  GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)),
                                        "side",  GTCEu.id("block/casings/voltage/%s/side".formatted(tierName)))) : null))
                .lang("%s Machine Casing".formatted(GTValues.VN[tier]))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<Block> createHermeticCasing(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        return REGISTRATE.block("%s_hermetic_casing".formatted(tierName), p -> (Block) new RendererBlock(p,
                        Platform.isClient() ? new TextureOverrideRenderer( GTCEu.id("block/hermetic_casing"),
                                Map.of("bot_bottom",  GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)),
                                        "bot_top",  GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)),
                                        "bot_side",  GTCEu.id("block/casings/voltage/%s/side".formatted(tierName)),
                                        "top_side",  GTCEu.id("block/casings/hermetic_casing/hermetic_casing_overlay"))) : null))
                .lang("Hermetic Casing %s".formatted(GTValues.LVT[tier]))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<Block> createSteamCasing(String name, String material) {
        return REGISTRATE.block(name, p -> (Block) new RendererBlock(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("bottom",  GTCEu.id("block/casings/steam/%s/bottom".formatted(material)),
                                        "top",  GTCEu.id("block/casings/steam/%s/top".formatted(material)),
                                        "side",  GTCEu.id("block/casings/steam/%s/side".formatted(material)))) : null))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<CoilBlock> createCoilBlock(ICoilType coilType) {
        BlockEntry<CoilBlock> coilBlock = REGISTRATE.block("%s_coil_block".formatted(coilType.getName()), p -> new CoilBlock(p, coilType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        ALL_COILS.put(coilType, coilBlock);
        return coilBlock;
    }

    private static BlockEntry<FusionCasingBlock> createFusionCasing(IFusionCasingType casingType) {
        BlockEntry<FusionCasingBlock> casingBlock = REGISTRATE.block(casingType.getSerializedName(), p -> new FusionCasingBlock(p, casingType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(5.0f, 10.0f).sound(SoundType.METAL))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, CustomTags.TOOL_TIERS[casingType.getHarvestLevel()])
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        ALL_FUSION_CASINGS.put(casingType, casingBlock);
        return casingBlock;
    }

    private static BlockEntry<Block> createCleanroomFilter(IFilterType filterType) {
        var filterBlock = REGISTRATE.block(filterType.getSerializedName(), p -> (Block) new RendererBlock(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                            Map.of("all", GTCEu.id("block/casings/cleanroom/" + filterType))) : null))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(2.0f, 8.0f).sound(SoundType.METAL).isValidSpawn((blockState, blockGetter, blockPos, entityType) -> false))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, CustomTags.TOOL_TIERS[1])
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        ALL_FILTERS.put(filterType, filterBlock);
        return filterBlock;
    }

    private static BlockEntry<ActiveBlock> createActiveCasing(String name, String baseModelPath) {
        String finalName = "%s".formatted(name);
        return REGISTRATE.block(finalName, p -> new ActiveBlock(p,
                        Platform.isClient() ? new CTMModelRenderer(GTCEu.id(baseModelPath)) : null,
                        Platform.isClient() ? new CTMModelRenderer(GTCEu.id("%s_active".formatted(baseModelPath))) : null))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<ActiveBlock> createFireboxCasing(BoilerFireboxType type) {
        BlockEntry<ActiveBlock> block = REGISTRATE
                .block("%s_casing".formatted(type.name()), p -> new ActiveBlock(p,
                        Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("bottom", type.bottom(),
                                        "top", type.top(),
                                        "side", type.side())) : null,
                        Platform.isClient() ? new TextureOverrideRenderer(GTCEu.id("block/fire_box_active"),
                                Map.of("bottom", type.bottom(),
                                        "top", type.top(),
                                        "side", type.side())) : null))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
        ALL_FIREBOXES.put(type, block);
        return block;
    }

    //////////////////////////////////////
    //**********     Misc     **********//
    //////////////////////////////////////

    public static final BlockEntry<SaplingBlock> RUBBER_SAPLING = REGISTRATE.block("rubber_sapling", properties -> new SaplingBlock(new AbstractTreeGrower() {
                protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@Nonnull RandomSource random, boolean largeHive) {
                    return GTConfiguredFeatures.RUBBER;
                }
            }, properties))
            .initialProperties(() -> Blocks.OAK_SAPLING)
            .lang("Rubber Sapling")
            .blockstate(GTModels::createCrossBlockState)
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.SAPLINGS)
            .item()
            .model(GTModels::rubberTreeSaplingModel)
            .tag(ItemTags.SAPLINGS)
            .onRegister(compassNode(GTCompassSections.GENERATIONS))
            .build()
            .register();


    public static final BlockEntry<RubberLogBlock> RUBBER_LOG = REGISTRATE.block("rubber_log", RubberLogBlock::new)
            .properties(p -> p.strength(2.0F).sound(SoundType.WOOD))
            .loot((table, block) -> table.add(block, LootTable.lootTable()
                    .withPool(table.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)))
                            .add(LootItem.lootTableItem(block)))
                    .withPool(table.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)))
                            .add(LootItem.lootTableItem(GTItems.STICKY_RESIN.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(RubberLogBlock.NATURAL, true)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.85F))))))
            .lang("Rubber Log")
            .tag(BlockTags.LOGS)
            .blockstate((ctx, provider) -> provider.logBlock(ctx.get()))
            .item()
            .tag(ItemTags.LOGS)
            .onRegister(compassNode(GTCompassSections.GENERATIONS))
            .build()
            .register();

    // Fortune Level
    public static final float[] RUBBER_LEAVES_DROPPING_CHANCE = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};

    @Environment(value= EnvType.CLIENT)
    public static BlockColor leavesBlockColor() {
        return (state, reader, pos, tintIndex) -> {
            if (reader != null && pos != null) {
                //return reader.getBlockTint(pos, (biome, x, z) -> biome.getFoliageColor());
                return BiomeColors.getAverageFoliageColor(reader, pos);
            }
            return FoliageColor.getDefaultColor();
        };
    }

    @Environment(value= EnvType.CLIENT)
    public static ItemColor leavesItemColor() {
        return (stack, tintIndex) -> FoliageColor.getDefaultColor();
    }

    public static final BlockEntry<LeavesBlock> RUBBER_LEAVES = REGISTRATE
            .block("rubber_leaves", LeavesBlock::new)
            .initialProperties(() -> Blocks.OAK_LEAVES)
            .lang("Rubber Leaves")
            .blockstate((ctx, prov) -> createModelBlockState(ctx, prov, GTCEu.id("block/rubber_leaves")))
            .loot((table, block) -> table.add(block, table.createLeavesDrops(block, GTBlocks.RUBBER_SAPLING.get(), RUBBER_LEAVES_DROPPING_CHANCE)))
            .tag(BlockTags.LEAVES)
            .color(() -> GTBlocks::leavesBlockColor)
            .item()
            .color(() -> GTBlocks::leavesItemColor)
            .tag(ItemTags.LEAVES)
            .onRegister(compassNode(GTCompassSections.GENERATIONS))
            .build()
            .register();

    public static final BlockEntry<Block> RUBBER_PLANK = REGISTRATE
            .block("rubber_planks", Block::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .lang("Rubber Planks")
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_GRAY))
            .tag(BlockTags.PLANKS)
            .item()
            .tag(ItemTags.PLANKS)
            .onRegister(compassNode(GTCompassSections.GENERATIONS))
            .build()
            .register();

    public static final BlockEntry<Block> TREATED_WOOD_PLANK = REGISTRATE
            .block("treated_wood_planks", Block::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .lang("Treated Wood Planks")
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_GRAY))
            .tag(BlockTags.PLANKS)
            .item()
            // purposefully omit planks item tag as this block is treated differently from wood in recipes
            .tag(TagUtil.createItemTag("treated_wood")) // matches IE treated wood tag
            .build()
            .register();


    public static <P, T extends Block, S2 extends BlockBuilder<T, P>> NonNullFunction<S2, S2> unificationBlock(@Nonnull TagPrefix tagPrefix, @Nonnull Material mat) {
        return builder -> {
            builder.onRegister(block -> ChemicalHelper.registerUnificationItems(tagPrefix, mat, block));
            return builder;
        };
    }

    public static <T extends ItemLike> NonNullConsumer<T> compassNode(CompassSection section, CompassNode... preNodes) {
        return item -> CompassNode.getOrCreate(section, item::asItem).addPreNode(preNodes);
    }

    public static <T extends ItemLike> NonNullConsumer<T> compassNodeExist(CompassSection section, String node, CompassNode... preNodes) {
        return item -> CompassNode.getOrCreate(section, node).addPreNode(preNodes).addItem(item::asItem);
    }

    public static void init() {
        generateMaterialBlocks();
        generateCableBlocks();
        generatePipeBlocks();
        GCyMBlocks.init();
    }

    public static boolean doMetalPipe(Material material) {
        return GTValues.FOOLS.get() && material.hasProperty(PropertyKey.INGOT) && !material.hasProperty(PropertyKey.POLYMER) && !material.hasProperty(PropertyKey.WOOD);
    }
}
