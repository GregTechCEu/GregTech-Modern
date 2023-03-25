package com.gregtechceu.gtceu.common.data;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.RendererBlock;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlocks
 */
public class GTBlocks {

    //////////////////////////////////////
    //*****     Material Blocks    *****//
    //////////////////////////////////////

    public final static Table<TagPrefix, Material, BlockEntry<? extends MaterialBlock>> MATERIAL_BLOCKS;

    static {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_BLOCK);
        ImmutableTable.Builder<TagPrefix, Material, BlockEntry<? extends MaterialBlock>> builder = ImmutableTable.builder();
        for (Material material : GTRegistries.MATERIALS) {
            // Compressed Block
            if ((material.hasProperty(PropertyKey.INGOT) || material.hasProperty(PropertyKey.GEM) || material.hasFlag(FORCE_GENERATE_BLOCK))
                    && !TagPrefix.block.isIgnored(material)) {
                var entry = REGISTRATE.block("compressed_block_%s".formatted(material.getName()), properties -> new MaterialBlock(properties, TagPrefix.block, material))
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .transform(unificationBlock(TagPrefix.block, material))
                        .addLayer(() -> RenderType::solid)
                        .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                        .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                        .color(() -> () -> MaterialBlock::tintedColor)
                        .item(MaterialBlockItem::new)
                        .model(NonNullBiConsumer.noop())
                        .color(() -> () -> MaterialBlockItem::tintColor)
                        .transform(unificationItem(TagPrefix.block, material))
                        .build()
                        .register();
                builder.put(TagPrefix.block, material, entry);
            }

            // Frame Block
            if (material.hasProperty(PropertyKey.DUST) && material.hasFlag(GENERATE_FRAME)) {
                var entry = REGISTRATE.block("frame_block_%s".formatted(material.getName()), properties -> new MaterialBlock(properties, TagPrefix.frameGt, material))
                        .initialProperties(() -> Blocks.IRON_BLOCK)
                        .properties(BlockBehaviour.Properties::noOcclusion)
                        .transform(unificationBlock(TagPrefix.frameGt, material))
                        .addLayer(() -> RenderType::cutoutMipped)
                        .blockstate(NonNullBiConsumer.noop())
                        .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                        .tag(BlockTags.MINEABLE_WITH_PICKAXE, GTToolType.WRENCH.harvestTag)
                        .color(() -> () -> MaterialBlock::tintedColor)
                        .item(MaterialBlockItem::new)
                        .model(NonNullBiConsumer.noop())
                        .color(() -> () -> MaterialBlockItem::tintColor)
                        .transform(unificationItem(TagPrefix.frameGt, material))
                        .build()
                        .register();
                builder.put(TagPrefix.frameGt, material, entry);
            }

            // Ore Block
            if (material.hasProperty(PropertyKey.ORE)) {
                for (var ore : TagPrefix.ORES.entrySet()) {
                    var oreTag = ore.getKey();
                    var oreProperty = material.getProperty(PropertyKey.ORE);
                    var entry = REGISTRATE.block("%s_%s".formatted(FormattingUtil.toLowerCaseUnder(oreTag.name), material.getName()),
                                    properties -> new MaterialBlock(properties, oreTag, material, new OreBlockRenderer(ore.getValue(),
                                            Objects.requireNonNull(oreTag.materialIconType()).getBlockTexturePath(material.getMaterialIconSet()),
                                            oreProperty.isEmissive())))
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .properties(BlockBehaviour.Properties::noOcclusion)
                            .transform(unificationBlock(oreTag, material))
                            .addLayer(() -> RenderType::cutoutMipped)
                            .blockstate(NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .color(() -> () -> MaterialBlock::tintedColor)
                            // TODO whether we need loot for drops?
//                            .loot((lt, b) -> lt.add(b,
//                                    RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
//                                            RegistrateBlockLootTables.applyExplosionDecay(b, LootItem.lootTableItem(GTItems.MATERIAL_ITEMS.get(TagPrefix.dust, material).get())
//                                                    .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))))))
                            .item(MaterialBlockItem::new)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> () -> MaterialBlockItem::tintColor)
                            .transform(unificationItem(oreTag, material))
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
    public final static Table<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS;

    static {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_PIPE);

        ImmutableTable.Builder<TagPrefix, Material, BlockEntry<CableBlock>> builder = ImmutableTable.builder();
        for (Insulation insulation : Insulation.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                // Compressed Block
                if (material.hasProperty(PropertyKey.WIRE) && !insulation.tagPrefix.isIgnored(material)) {
                    var entry = REGISTRATE.block(insulation.name + "." + material.getName(), p -> new CableBlock(p, insulation, material))
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .properties(p -> p.dynamicShape().noOcclusion())
                            .tag(BlockTags.MINEABLE_WITH_PICKAXE, GTToolType.WRENCH.harvestTag, GTToolType.WIRE_CUTTER.harvestTag)
                            .transform(unificationBlock(insulation.tagPrefix, material))
                            .blockstate(NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .addLayer(() -> RenderType::cutoutMipped)
                            .color(() -> () -> MaterialPipeBlock::tintedColor)
                            .item(MaterialPipeBlockItem::new)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> () -> MaterialPipeBlockItem::tintColor)
                            .transform(unificationItem(insulation.tagPrefix, material))
                            .build()
                            .register();
                    builder.put(insulation.tagPrefix, material, entry);
                }
            }
        }
        CABLE_BLOCKS = builder.build();
    }

    public final static Table<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS;

    static {
        ImmutableTable.Builder<TagPrefix, Material, BlockEntry<FluidPipeBlock>> builder = ImmutableTable.builder();
        for (var fluidPipeType : FluidPipeType.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                // Compressed Block
                if (material.hasProperty(PropertyKey.FLUID_PIPE) && !fluidPipeType.tagPrefix.isIgnored(material)) {
                    var entry = REGISTRATE.block(fluidPipeType.name + "." + material.getName(), p -> new FluidPipeBlock(p, fluidPipeType, material))
                            .initialProperties(() -> Blocks.IRON_BLOCK)
                            .properties(p -> p.dynamicShape().noOcclusion())
                            .tag(BlockTags.MINEABLE_WITH_PICKAXE, GTToolType.WRENCH.harvestTag)
                            .transform(unificationBlock(fluidPipeType.tagPrefix, material))
                            .blockstate(NonNullBiConsumer.noop())
                            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                            .addLayer(() -> RenderType::cutoutMipped)
                            .color(() -> () -> MaterialPipeBlock::tintedColor)
                            .item(MaterialPipeBlockItem::new)
                            .model(NonNullBiConsumer.noop())
                            .color(() -> () -> MaterialPipeBlockItem::tintColor)
                            .transform(unificationItem(fluidPipeType.tagPrefix, material))
                            .build()
                            .register();
                    builder.put(fluidPipeType.tagPrefix, material, entry);
                }
            }
        }
        FLUID_PIPE_BLOCKS = builder.build();
    }

    static {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.DECORATION);
    }

    //////////////////////////////////////
    //******     Casing Blocks     *****//
    //////////////////////////////////////

    public static final BlockEntry<Block> CASING_BRONZE_BRICKS = createCasingBlock("bronze_bricks", GTCEu.id("block/casings/solid/machine_bronze_plated_bricks"));
    public static final BlockEntry<Block> CASING_PRIMITIVE_BRICKS = createCasingBlock("primitive_bricks", GTCEu.id("block/casings/solid/machine_primitive_bricks"));
    public static final BlockEntry<Block> CASING_INVAR_HEATPROOF = createCasingBlock("invar_heatproof", GTCEu.id("block/casings/solid/machine_casing_heatproof"));
    public static final BlockEntry<Block> CASING_ALUMINIUM_FROSTPROOF = createCasingBlock("aluminium_frostproof", GTCEu.id("block/casings/solid/machine_casing_frost_proof"));
    public static final BlockEntry<Block> CASING_STEEL_SOLID = createCasingBlock("steel_solid", GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
    public static final BlockEntry<Block> CASING_STAINLESS_CLEAN = createCasingBlock("stainless_clean", GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_STABLE = createCasingBlock("titanium_stable", GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_ROBUST = createCasingBlock("tungstensteel_robust", GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));
    public static final BlockEntry<Block> CASING_COKE_BRICKS = createCasingBlock("coke_bricks", GTCEu.id("block/casings/solid/machine_coke_bricks"));
    public static final BlockEntry<Block> CASING_PTFE_INERT = createCasingBlock("ptfe_inert", GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"));
    public static final BlockEntry<Block> CASING_HSSE_STURDY = createCasingBlock("hsse_sturdy", GTCEu.id("block/casings/solid/machine_casing_study_hsse"));
    public static final BlockEntry<Block> CASING_GRATE = createCasingBlock("grate", GTCEu.id("block/casings/pipe/machine_casing_grate"));
    public static final BlockEntry<Block> CASING_ASSEMBLY_CONTROL = createCasingBlock("assembly_control", GTCEu.id("block/casings/mechanic/machine_casing_assembly_control"));
    public static final BlockEntry<Block> CASING_ASSEMBLY_LINE_GRATE = createCasingBlock("assembly_line", GTCEu.id("block/casings/pipe/machine_casing_grate"));
    public static final BlockEntry<Block> CASING_POLYTETRAFLUOROETHYLENE_PIPE = createCasingBlock("polytetrafluoroethylene_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_polytetrafluoroethylene"));
    public static final BlockEntry<Block> CASING_LAMINATED_GLASS = createCasingBlock("laminated_glass", GTCEu.id("block/casings/transparent/laminated_glass"));
    public static final BlockEntry<Block> CASING_BRONZE_GEARBOX = createCasingBlock("bronze_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_bronze"));
    public static final BlockEntry<Block> CASING_STEEL_GEARBOX = createCasingBlock("steel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_steel"));
    public static final BlockEntry<Block> CASING_STAINLESS_STEEL_GEARBOX = createCasingBlock("stainless_steel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_stainless_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_GEARBOX = createCasingBlock("titanium_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_GEARBOX = createCasingBlock("tungstensteel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_tungstensteel"));
    public static final BlockEntry<Block> CASING_STEEL_TURBINE = createCasingBlock("steel_turbine", GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_TURBINE = createCasingBlock("titanium_turbine", GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"));
    public static final BlockEntry<Block> CASING_STAINLESS_TURBINE = createCasingBlock("stainless_turbine", GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_TURBINE = createCasingBlock("tungstensteel_turbine", GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"));
    public static final BlockEntry<Block> CASING_BRONZE_PIPE = createCasingBlock("bronze_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_bronze"));
    public static final BlockEntry<Block> CASING_STEEL_PIPE = createCasingBlock("steel_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_steel"));
    public static final BlockEntry<Block> CASING_TITANIUM_PIPE = createCasingBlock("titanium_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_titanium"));
    public static final BlockEntry<Block> CASING_TUNGSTENSTEEL_PIPE = createCasingBlock("tungstensteel_pipe", GTCEu.id("block/casings/pipe/machine_casing_pipe_tungstensteel"));
    // todo primitive pump
    //PUMP_DECK("pump_deck", GTCEu.id("")),
    //WOOD_WALL("wood_wall", GTCEu.id(""));

    public static final BlockEntry<Block> MACHINE_CASING_ULV = createMachineCasingBlock(GTValues.ULV);
    public static final BlockEntry<Block> MACHINE_CASING_LV = createMachineCasingBlock(GTValues.LV);
    public static final BlockEntry<Block> MACHINE_CASING_MV = createMachineCasingBlock(GTValues.MV);
    public static final BlockEntry<Block> MACHINE_CASING_HV = createMachineCasingBlock(GTValues.HV);
    public static final BlockEntry<Block> MACHINE_CASING_EV = createMachineCasingBlock(GTValues.EV);
    public static final BlockEntry<Block> MACHINE_CASING_IV = createMachineCasingBlock(GTValues.IV);
    public static final BlockEntry<Block> MACHINE_CASING_LuV = createMachineCasingBlock(GTValues.LuV);
    public static final BlockEntry<Block> MACHINE_CASING_ZPM = createMachineCasingBlock(GTValues.ZPM);
    public static final BlockEntry<Block> MACHINE_CASING_UV = createMachineCasingBlock(GTValues.UV);
    public static final BlockEntry<Block> MACHINE_CASING_UHV = createMachineCasingBlock(GTValues.UHV);
    public static final BlockEntry<Block> MACHINE_CASING_UEV = createMachineCasingBlock(GTValues.UEV);
    public static final BlockEntry<Block> MACHINE_CASING_UIV = createMachineCasingBlock(GTValues.UIV);
    public static final BlockEntry<Block> MACHINE_CASING_UXV = createMachineCasingBlock(GTValues.UXV);
    public static final BlockEntry<Block> MACHINE_CASING_OpV = createMachineCasingBlock(GTValues.OpV);
    public static final BlockEntry<Block> MACHINE_CASING_MAX = createMachineCasingBlock(GTValues.MAX);

    public static final BlockEntry<Block> HERMETIC_CASING_ULV = createHermeticCasing(GTValues.ULV);
    public static final BlockEntry<Block> HERMETIC_CASING_LV = createHermeticCasing(GTValues.LV);
    public static final BlockEntry<Block> HERMETIC_CASING_MV = createHermeticCasing(GTValues.MV);
    public static final BlockEntry<Block> HERMETIC_CASING_HV = createHermeticCasing(GTValues.HV);
    public static final BlockEntry<Block> HERMETIC_CASING_EV = createHermeticCasing(GTValues.EV);
    public static final BlockEntry<Block> HERMETIC_CASING_IV = createHermeticCasing(GTValues.IV);
    public static final BlockEntry<Block> HERMETIC_CASING_LuV = createHermeticCasing(GTValues.LuV);
    public static final BlockEntry<Block> HERMETIC_CASING_ZPM = createHermeticCasing(GTValues.ZPM);
    public static final BlockEntry<Block> HERMETIC_CASING_UV = createHermeticCasing(GTValues.UV);
    public static final BlockEntry<Block> HERMETIC_CASING_UHV = createHermeticCasing(GTValues.UHV);

    public static final BlockEntry<Block> BRONZE_HULL = createSteamCasing("bronze");
    public static final BlockEntry<Block> BRONZE_BRICKS_HULL = createSteamCasing("bricked_bronze");
    public static final BlockEntry<Block> STEEL_HULL = createSteamCasing("steel");
    public static final BlockEntry<Block> STEEL_BRICKS_HULL = createSteamCasing("bricked_steel");

    public static final Map<CoilBlock.CoilType, BlockEntry<CoilBlock>> ALL_COILS = new HashMap<>();
    public static final BlockEntry<CoilBlock> COIL_CUPRONICKEL = createCoilBlock(CoilBlock.CoilType.CUPRONICKEL);
    public static final BlockEntry<CoilBlock> COIL_KANTHAL = createCoilBlock(CoilBlock.CoilType.KANTHAL);
    public static final BlockEntry<CoilBlock> COIL_NICHROME = createCoilBlock(CoilBlock.CoilType.NICHROME);
    public static final BlockEntry<CoilBlock> COIL_TUNGSTENSTEEL = createCoilBlock(CoilBlock.CoilType.TUNGSTENSTEEL);
    public static final BlockEntry<CoilBlock> COIL_HSSG = createCoilBlock(CoilBlock.CoilType.HSSG);
    public static final BlockEntry<CoilBlock> COIL_NAQUADAH = createCoilBlock(CoilBlock.CoilType.NAQUADAH);
    public static final BlockEntry<CoilBlock> COIL_TRINIUM = createCoilBlock(CoilBlock.CoilType.TRINIUM);
    public static final BlockEntry<CoilBlock> COIL_TRITANIUM = createCoilBlock(CoilBlock.CoilType.TRITANIUM);

    public static final BlockEntry<ActiveBlock> CASING_ENGINE_INTAKE = createActiveCasing("engine_intake", "block/variant/engine_intake");
    public static final BlockEntry<ActiveBlock> CASING_EXTREME_ENGINE_INTAKE = createActiveCasing("extreme_engine_intake", "block/variant/extreme_engine_intake");
    public static final BlockEntry<ActiveBlock> CASING_ASSEMBLY_LINE = createActiveCasing("assembly_line", "block/variant/assembly_line");

    public static final Map<BoilerFireboxType, BlockEntry<ActiveBlock>> ALL_FIREBOXES = new HashMap<>();
    public static final BlockEntry<ActiveBlock> FIREBOX_BRONZE = createFireboxCasing(BoilerFireboxType.BRONZE_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_STEEL = createFireboxCasing(BoilerFireboxType.STEEL_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TITANIUM = createFireboxCasing(BoilerFireboxType.TITANIUM_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TUNGSTENSTEEL = createFireboxCasing(BoilerFireboxType.TUNGSTENSTEEL_FIREBOX);

    private static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return REGISTRATE.block("casing_%s".formatted(name), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                                Map.of("all", texture))))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<Block> createMachineCasingBlock(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase();
        return REGISTRATE.block("hull_casing_%s".formatted(tierName), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer( GTCEu.id("block/cube_bottom_top_tintindex"),
                                Map.of("bottom",  GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)),
                                        "top",  GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)),
                                        "side",  GTCEu.id("block/casings/voltage/%s/side".formatted(tierName))))))
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
        String tierName = GTValues.VN[tier].toLowerCase();
        return REGISTRATE.block("hermetic_casing_%s".formatted(tierName), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer( GTCEu.id("block/hermetic_casing"),
                                Map.of("bot_bottom",  GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)),
                                        "bot_top",  GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)),
                                        "bot_side",  GTCEu.id("block/casings/voltage/%s/side".formatted(tierName)),
                                        "top_side",  GTCEu.id("block/casings/hermetic_casing/hermetic_casing_overlay")))))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<Block> createSteamCasing(String name) {
        return REGISTRATE.block("steam_casing_%s".formatted(name), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("bottom",  GTCEu.id("block/casings/steam/%s/bottom".formatted(name)),
                                        "top",  GTCEu.id("block/casings/steam/%s/top".formatted(name)),
                                        "side",  GTCEu.id("block/casings/steam/%s/side".formatted(name))))))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<CoilBlock> createCoilBlock(CoilBlock.CoilType coilType) {
        BlockEntry<CoilBlock> coilBlock = REGISTRATE.block("wire_coil_%s".formatted(coilType.getName()), p -> new CoilBlock(p, coilType))
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

    private static BlockEntry<ActiveBlock> createActiveCasing(String name, String baseModelPath) {
        String finalName = "active_casing_%s".formatted(name);
        return REGISTRATE.block(finalName, p -> new ActiveBlock(p,
                        new CTMModelRenderer(GTCEu.id(baseModelPath)),
                        new CTMModelRenderer(GTCEu.id("%s_active".formatted(baseModelPath)))))
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
        BlockEntry<ActiveBlock> block = REGISTRATE.block(type.getName(), p -> new ActiveBlock(p,
                        new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("bottom", type.getBottom(),
                                        "top", type.getTop(),
                                        "side", type.getSide())),
                        new TextureOverrideRenderer(GTCEu.id("block/fire_box_active"),
                                Map.of("bottom", type.getBottom(),
                                        "top", type.getTop(),
                                        "side", type.getSide()))))
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

    public static <P, T extends Block, S2 extends BlockBuilder<T, P>> NonNullFunction<S2, S2> unificationBlock(@Nonnull TagPrefix tagPrefix, @Nonnull Material mat) {
        return builder -> {
            builder.onRegister(block -> ChemicalHelper.registerUnificationItems(tagPrefix, mat, block));
            return builder;
        };
    }

    public static <P, T extends Item, S2 extends ItemBuilder<T, P>> NonNullFunction<S2, S2> unificationItem(@Nonnull TagPrefix tagPrefix, @Nonnull Material mat) {
        return builder -> {
            builder.onRegister(item -> ChemicalHelper.registerUnificationItems(tagPrefix, mat, item));
            return builder;
        };
    }

    public static void init() {
    }
}
