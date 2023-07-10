package com.gregtechceu.gtceu.common.data;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.*;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.RendererBlockItem;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.client.renderer.block.CTMModelRenderer;
import com.gregtechceu.gtceu.client.renderer.block.OreBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.Platform;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;
import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

/**
 * @author KilaBash
 * @date 2023/2/13
 * @implNote GTBlocks
 */
public class GTBlocks {

    // Externally Accessible Values
    public static Table<TagPrefix, Material, BlockEntry<? extends MaterialBlock>> MATERIAL_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS;

    // Table Builders
    private static final ImmutableTable.Builder<TagPrefix, Material, BlockEntry<? extends MaterialBlock>> MATERIAL_BLOCKS_BUILDER = ImmutableTable.builder();
    private static final ImmutableTable.Builder<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS_BUILDER = ImmutableTable.builder();
    private static final ImmutableTable.Builder<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS_BUILDER = ImmutableTable.builder();


    //////////////////////////////////////
    //******   Generated Blocks    *****//
    //////////////////////////////////////

    // Material Block Generation
    private static boolean allowedBlockMaterial(@NotNull Material material) {
        boolean hasIngot = material.hasProperty(PropertyKey.INGOT);
        boolean hasGem = material.hasProperty(PropertyKey.GEM);
        boolean isForceGenerated = material.hasFlag(FORCE_GENERATE_BLOCK);
        boolean isNotIgnored = !TagPrefix.block.isIgnored(material);
        return isNotIgnored && (hasIngot || hasGem || isForceGenerated);
    }
    private static void registerMaterialBlock(@NotNull Material material) {
        var entry = REGISTRATE.block("%s_block".formatted(material.getName()), properties -> new MaterialBlock(properties.noLootTable(), TagPrefix.block, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .transform(unificationBlock(TagPrefix.block, material))
                .addLayer(() -> RenderType::solid)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .color(() -> MaterialBlock::tintedColor)
                .item(MaterialBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> MaterialBlockItem::tintColor)
                .build()
                .register();
        MATERIAL_BLOCKS_BUILDER.put(TagPrefix.block, material, entry);
    }
    private static void registerMaterialBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_BLOCK);
        for (Material material : GTRegistries.MATERIALS) {
            if (allowedBlockMaterial(material)) {
                registerMaterialBlock(material);
            }
        }
        MATERIAL_BLOCKS = MATERIAL_BLOCKS_BUILDER.build();
    }

    // Frame Block Generation
    private static boolean allowedFrameMaterial(@NotNull Material material) {
        boolean hasDust = material.hasProperty(PropertyKey.DUST);
        boolean hasFrame = material.hasFlag(GENERATE_FRAME);
        return hasDust && hasFrame;
    }
    private static void registerFrameBlock(@NotNull Material material) {
        var entry = REGISTRATE.block("%s_frame".formatted(material.getName()), properties -> new MaterialBlock(properties.noLootTable(), TagPrefix.frameGt, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .transform(unificationBlock(TagPrefix.frameGt, material))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .color(() -> MaterialBlock::tintedColor)
                .item(MaterialBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> MaterialBlockItem::tintColor)
                .build()
                .register();
        MATERIAL_BLOCKS_BUILDER.put(TagPrefix.frameGt, material, entry);
    }
    private static void registerFrameBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_BLOCK);
        for (Material material : GTRegistries.MATERIALS) {
            if (allowedFrameMaterial(material)) {
                registerFrameBlock(material);
            }
        }
        MATERIAL_BLOCKS = MATERIAL_BLOCKS_BUILDER.build();
    }

    // Ore Block Generation
    private static boolean allowedOreMaterial(@NotNull Material material, Map.@NotNull Entry<TagPrefix, TagPrefix.OreType> ore) {
        boolean hasOre = material.hasProperty(PropertyKey.ORE);
        boolean isNotIgnored = !ore.getKey().isIgnored(material);
        return isNotIgnored && hasOre;
    }
    private static void registerOreBlock(@NotNull Material material, Map.@NotNull Entry<TagPrefix, TagPrefix.OreType> ore) {
        // TODO: Tidy up this registry
        var oreProperty = material.getProperty(PropertyKey.ORE);
        var oreTag = ore.getKey();
        final TagPrefix.OreType oreType = ore.getValue();
        var entry = REGISTRATE.block("%s%s_ore".formatted(FormattingUtil.toLowerCaseUnder(oreTag.name), material.getName()),
                        oreType.material(),
                        properties -> new MaterialBlock(properties, oreTag, material, new OreBlockRenderer(oreType.stoneType(),
                                Objects.requireNonNull(oreTag.materialIconType()).getBlockTexturePath(material.getMaterialIconSet(), true),
                                oreProperty.isEmissive())))
                .initialProperties(() -> oreType.stoneType().get().getBlock())
                .properties(properties -> {
                    properties.noLootTable();
                    if (oreType.color() != null) properties.color(oreType.color());
                    if (oreType.material() == net.minecraft.world.level.material.Material.SAND) {
                        properties.strength(1.0f, 0.5f);
                    }
                    if (oreType.sound() != null) properties.sound(oreType.sound());
                    return properties;
                })
                .transform(unificationBlock(oreTag, material))
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .color(() -> MaterialBlock::tintedColor)
                .item(MaterialBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> MaterialBlockItem::tintColor)
                .build()
                .register();
        MATERIAL_BLOCKS_BUILDER.put(oreTag, material, entry);
    }
    private static void registerOreBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_BLOCK);
        for (Material material : GTRegistries.MATERIALS) {
            for (var ore : TagPrefix.ORES.entrySet()) {
                if (allowedOreMaterial(material, ore)) {
                    registerOreBlock(material, ore);
                }
            }
        }
        MATERIAL_BLOCKS = MATERIAL_BLOCKS_BUILDER.build();
    }

    // Cable Block Generation
    private static boolean allowedCableMaterial(@NotNull Material material, @NotNull Insulation insulation) {
        boolean hasWire = material.hasProperty(PropertyKey.WIRE);
        boolean insulationIsNotIgnored = !insulation.tagPrefix.isIgnored(material);
        return insulationIsNotIgnored && hasWire;
    }
    private static void registerCableBlock(@NotNull Material material, @NotNull Insulation insulation) {
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
        CABLE_BLOCKS_BUILDER.put(insulation.tagPrefix, material, entry);
    }
    private static void registerCableBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_PIPE);
        for (Insulation insulation : Insulation.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                if (allowedCableMaterial(material, insulation)) {
                    registerCableBlock(material, insulation);
                }
            }
        }
        CABLE_BLOCKS = CABLE_BLOCKS_BUILDER.build();
    }

    // Fluid Pipe Block Generation
    private static boolean allowedFluidPipeMaterial(@NotNull FluidPipeType fluidPipeType, @NotNull Material material) {
        boolean hasFluidPipe = material.hasProperty(PropertyKey.FLUID_PIPE);
        boolean fluidPipeIsNotIgnored = !fluidPipeType.tagPrefix.isIgnored(material);
        return fluidPipeIsNotIgnored && hasFluidPipe;
    }
    private static void generateFluidPipeBlock(@NotNull FluidPipeType fluidPipeType, @NotNull Material material) {
        var entry = REGISTRATE.block( "%s_%s_fluid_pipe".formatted(material.getName(), fluidPipeType.name), p -> new FluidPipeBlock(p.noLootTable(), fluidPipeType, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.dynamicShape().noOcclusion())
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
        FLUID_PIPE_BLOCKS_BUILDER.put(fluidPipeType.tagPrefix, material, entry);
    }
    private static void generateFluidPipeBlocks() {
        REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MATERIAL_PIPE);
        for (var fluidPipeType : FluidPipeType.values()) {
            for (Material material : GTRegistries.MATERIALS) {
                if (allowedFluidPipeMaterial(fluidPipeType, material)) {
                    generateFluidPipeBlock(fluidPipeType, material);
                }
            }
        }
        FLUID_PIPE_BLOCKS = FLUID_PIPE_BLOCKS_BUILDER.build();
    }


    // Establish Default Tab
    static { REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.DECORATION); }

    //////////////////////////////////////
    //******        Casings        *****//
    //////////////////////////////////////

    // Machine Casings
    private static final String defaultMachineCasingDirectory = "block/casings/solid";
    private static BlockEntry<Block> registerMachineCasing(String id) {
        return registerMachineCasing(id, GTCEu.id("%s/%s".formatted(defaultMachineCasingDirectory, id)));
    }
    private static BlockEntry<Block> registerMachineCasing(String id, ResourceLocation texture) {
        return REGISTRATE.block(id, p -> (Block) new RendererBlock(p, new TextureOverrideRenderer(new ResourceLocation("block/cube_all"), Map.of("all", texture))))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }
    public static final BlockEntry<Block>
            STEAM_MACHINE_CASING = registerMachineCasing("steam_machine_casing"),
            HEATPROOF_MACHINE_CASING = registerMachineCasing("heatproof_machine_casing"),
            FROSTPROOF_MACHINE_CASING = registerMachineCasing("frostproof_machine_casing"),
            SOLID_MACHINE_CASING = registerMachineCasing("solid_machine_casing"),
            CLEAN_MACHINE_CASING = registerMachineCasing("clean_machine_casing"),
            STABLE_MACHINE_CASING = registerMachineCasing("stable_machine_casing"),
            ROBUST_MACHINE_CASING = registerMachineCasing("robust_machine_casing"),
            INERT_MACHINE_CASING = registerMachineCasing("inert_machine_casing"),
            STURDY_MACHINE_CASING = registerMachineCasing("sturdy_machine_casing");

    // Steam Casings
    private static final String defaultSteamCasingDirectory = "block/casings/steam";
    private static BlockEntry<Block> registerSteamCasing(String id) {
        return registerSteamCasing(id, "%s/%s".formatted(defaultSteamCasingDirectory, id));
    }
    private static BlockEntry<Block> registerSteamCasing(String id, String textures) {
        return REGISTRATE.block(id, p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("bottom",  GTCEu.id("%s_bottom".formatted(textures)),
                                        "top",  GTCEu.id("%s_top".formatted(textures)),
                                        "side",  GTCEu.id("%s_side".formatted(textures))))))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }
    public static final BlockEntry<Block>
            BRONZE_MACHINE_CASING = registerSteamCasing("bronze_machine_casing"),
            BRONZE_BRICK_CASING = registerSteamCasing("bronze_brick_casing"),
            STEEL_MACHINE_CASING = registerSteamCasing("steel_machine_casing"),
            STEEL_BRICK_CASING = registerSteamCasing("steel_brick_casing");

    // Voltage Casings
    private static final String defaultVoltageCasingDirectory = "block/casings/voltage";
    private static BlockEntry<Block> registerVoltageCasing(int voltage) {
        String tierName = GTValues.VN[voltage].toLowerCase();
        return REGISTRATE.block("%s_machine_casing".formatted(tierName), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer( GTCEu.id("block/cube_bottom_top_tintindex"),
                                Map.of("bottom",  GTCEu.id("%s/%s/bottom".formatted(defaultVoltageCasingDirectory, tierName)),
                                        "top",  GTCEu.id("%s/%s/top".formatted(defaultVoltageCasingDirectory, tierName)),
                                        "side",  GTCEu.id("%s/%s/side".formatted(defaultVoltageCasingDirectory, tierName))))))
                .lang("%s Machine Casing".formatted(GTValues.VN[voltage]))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }
    public static final BlockEntry<Block>
            MACHINE_CASING_ULV = registerVoltageCasing(GTValues.ULV),
            MACHINE_CASING_LV = registerVoltageCasing(GTValues.LV),
            MACHINE_CASING_MV = registerVoltageCasing(GTValues.MV),
            MACHINE_CASING_HV = registerVoltageCasing(GTValues.HV),
            MACHINE_CASING_EV = registerVoltageCasing(GTValues.EV),
            MACHINE_CASING_IV = registerVoltageCasing(GTValues.IV),
            MACHINE_CASING_LuV = registerVoltageCasing(GTValues.LuV),
            MACHINE_CASING_ZPM = registerVoltageCasing(GTValues.ZPM),
            MACHINE_CASING_UV = registerVoltageCasing(GTValues.UV),
            MACHINE_CASING_UHV = registerVoltageCasing(GTValues.UHV);














    // Just Pickaxe Blocks
    private static BlockEntry<Block> createPickaxeBlock(String id, ResourceLocation texture) {
        return createPickaxeBlock(id, texture, () -> Blocks.IRON_BLOCK);
    }
    private static BlockEntry<Block> createPickaxeBlock(String id, ResourceLocation texture, NonNullSupplier<? extends Block> properties) {
        return REGISTRATE.block(id, p -> (Block) new RendererBlock(p, new TextureOverrideRenderer(new ResourceLocation("block/cube_all"), Map.of("all", texture))))
                .initialProperties(properties)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }
    public static final BlockEntry<Block>
            COKE_OVEN_BRICKS = createPickaxeBlock("coke_oven_bricks", GTCEu.id("block/casings/solid/coke_oven_bricks")),
            FIREBRICKS = createPickaxeBlock("firebricks", GTCEu.id("block/casings/solid/firebricks")),
            TEMPERED_GLASS = createPickaxeBlock("tempered_glass", GTCEu.id("block/casings/transparent/tempered_glass"), () -> Blocks.GLASS),
            LAMINATED_GLASS = createPickaxeBlock("laminated_glass", GTCEu.id("block/casings/transparent/laminated_glass"), () -> Blocks.GLASS);
    public static final BlockEntry<Block> PLASTCRETE = createPickaxeBlock("plascrete", GTCEu.id("block/casings/cleanroom/plascrete"));
    public static final BlockEntry<Block> CLEANROOM_GLASS = createPickaxeBlock("cleanroom_glass", GTCEu.id("block/casings/transparent/cleanroom_glass"), () -> Blocks.GLASS);













    public static final BlockEntry<ActiveBlock> CASING_ENGINE_INTAKE = createActiveCasing("engine_intake_casing", "block/variant/engine_intake");
    public static final BlockEntry<ActiveBlock> CASING_EXTREME_ENGINE_INTAKE = createActiveCasing("extreme_engine_intake_casing", "block/variant/extreme_engine_intake");











    // Cleanroom
    public static final Map<IFilterType, Supplier<Block>> ALL_FILTERS = new HashMap<>();

    public static final BlockEntry<Block> FILTER_CASING = createCleanroomFilter(CleanroomFilterType.FILTER_CASING);
    public static final BlockEntry<Block> FILTER_CASING_STERILE = createCleanroomFilter(CleanroomFilterType.FILTER_CASING_STERILE);










    private static BlockEntry<Block> createCasingBlock(String id, ResourceLocation texture) {
        return createCasingBlock(id, texture, () -> Blocks.IRON_BLOCK);
    }
    private static BlockEntry<Block> createCasingBlock(String id, ResourceLocation texture, NonNullSupplier<? extends Block> properties) {
        return REGISTRATE.block(id, p -> (Block) new RendererBlock(p, new TextureOverrideRenderer(new ResourceLocation("block/cube_all"), Map.of("all", texture))))
                .initialProperties(properties)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(NonNullBiConsumer.noop())
                .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
                .item(RendererBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .build()
                .register();
    }

    // Assembly Line Casings
    public static final BlockEntry<Block> ASSEMBLY_LINE_GRATING = createCasingBlock("assembly_line_grating", GTCEu.id("block/casings/pipe/machine_casing_grate"));
    public static final BlockEntry<Block> ASSEMBLY_LINE_CASING = createCasingBlock("assembly_line_casing", GTCEu.id("block/casings/mechanic/machine_casing_assembly_control"));
    public static final BlockEntry<ActiveBlock> ASSEMBLY_LINE_UNIT = createActiveCasing("assembly_line_unit", "block/variant/assembly_line");

    // Gear Boxes
    public static final BlockEntry<Block> BRONZE_GEARBOX = createCasingBlock("bronze_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_bronze"));
    public static final BlockEntry<Block> STEEL_GEARBOX = createCasingBlock("steel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_steel"));
    public static final BlockEntry<Block> STAINLESS_STEEL_GEARBOX = createCasingBlock("stainless_steel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_stainless_steel"));
    public static final BlockEntry<Block> TITANIUM_GEARBOX = createCasingBlock("titanium_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_titanium"));
    public static final BlockEntry<Block> TUNGSTENSTEEL_GEARBOX = createCasingBlock("tungstensteel_gearbox", GTCEu.id("block/casings/gearbox/machine_casing_gearbox_tungstensteel"));

    // Turbine Casings
    public static final BlockEntry<Block> STEEL_TURBINE_CASING = createCasingBlock("steel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"));
    public static final BlockEntry<Block> TITANIUM_TURBINE_CASING = createCasingBlock("titanium_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"));
    public static final BlockEntry<Block> STAINLESS_STEEL_TURBINE_CASING = createCasingBlock("stainless_steel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"));
    public static final BlockEntry<Block> TUNGSTENSTEEL_TURBINE_CASING = createCasingBlock("tungstensteel_turbine_casing", GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"));

    // Pipe casings
    private static BlockEntry<Block> createPipeCasingBlock(String name, ResourceLocation texture) {
        return createPipeCasingBlock(name, texture, () -> Blocks.IRON_BLOCK);
    }
    private static BlockEntry<Block> createPipeCasingBlock(String name, ResourceLocation texture, NonNullSupplier<? extends Block> properties) {
        return REGISTRATE.block("%s_pipe_casing".formatted(name.toLowerCase()), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                                Map.of("all", texture))))
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
    public static final BlockEntry<Block> BRONZE_PIPE_CASING = createCasingBlock("bronze_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_bronze"));
    public static final BlockEntry<Block> STEEL_PIPE_CASING = createCasingBlock("steel_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_steel"));
    public static final BlockEntry<Block> TITANIUM_PIPE_CASING = createCasingBlock("titanium_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_titanium"));
    public static final BlockEntry<Block> TUNGSTENSTEEL_PIPE_CASING = createCasingBlock("tungstensteel_pipe_casing", GTCEu.id("block/casings/pipe/machine_casing_pipe_tungstensteel"));
    public static final BlockEntry<Block> PTFE_PIPE_CASING = createPipeCasingBlock("PTFE", GTCEu.id("block/casings/pipe/machine_casing_pipe_polytetrafluoroethylene"));

    // The Pump Deck
    public static final BlockEntry<Block> PUMP_DECK = REGISTRATE.block("pump_deck", p -> (Block) new RendererBlock(p,
                    new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                            Map.of("bottom",  GTCEu.id("block/casings/pump_deck/bottom"),
                                    "top",  GTCEu.id("block/casings/pump_deck/top"),
                                    "side",  GTCEu.id("block/casings/pump_deck/side")))))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.WOOD).color(MaterialColor.WOOD))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(NonNullBiConsumer.noop())
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_AXE)
            .item(RendererBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    // TODO: Multiblock Tanks




    // Hermetic Casings
    private static BlockEntry<Block> createHermeticCasing(int tier) {
        String tierName = GTValues.VN[tier].toLowerCase();
        return REGISTRATE.block("%s_hermetic_casing".formatted(tierName), p -> (Block) new RendererBlock(p,
                        new TextureOverrideRenderer( GTCEu.id("block/hermetic_casing"),
                                Map.of("bot_bottom",  GTCEu.id("block/casings/voltage/%s/bottom".formatted(tierName)),
                                        "bot_top",  GTCEu.id("block/casings/voltage/%s/top".formatted(tierName)),
                                        "bot_side",  GTCEu.id("block/casings/voltage/%s/side".formatted(tierName)),
                                        "top_side",  GTCEu.id("block/casings/hermetic_casing/hermetic_casing_overlay")))))
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
    public static final BlockEntry<Block> HERMETIC_CASING_LV = createHermeticCasing(GTValues.LV);
    public static final BlockEntry<Block> HERMETIC_CASING_MV = createHermeticCasing(GTValues.MV);
    public static final BlockEntry<Block> HERMETIC_CASING_HV = createHermeticCasing(GTValues.HV);
    public static final BlockEntry<Block> HERMETIC_CASING_EV = createHermeticCasing(GTValues.EV);
    public static final BlockEntry<Block> HERMETIC_CASING_IV = createHermeticCasing(GTValues.IV);
    public static final BlockEntry<Block> HERMETIC_CASING_LuV = createHermeticCasing(GTValues.LuV);
    public static final BlockEntry<Block> HERMETIC_CASING_ZPM = createHermeticCasing(GTValues.ZPM);
    public static final BlockEntry<Block> HERMETIC_CASING_UV = createHermeticCasing(GTValues.UV);
    public static final BlockEntry<Block> HERMETIC_CASING_UHV = createHermeticCasing(GTValues.UHV);


    // Heating Coils
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
    public static final Map<ICoilType, Supplier<CoilBlock>> ALL_COILS = new HashMap<>();
    public static final BlockEntry<CoilBlock> COIL_CUPRONICKEL = createCoilBlock(CoilBlock.CoilType.CUPRONICKEL);
    public static final BlockEntry<CoilBlock> COIL_KANTHAL = createCoilBlock(CoilBlock.CoilType.KANTHAL);
    public static final BlockEntry<CoilBlock> COIL_NICHROME = createCoilBlock(CoilBlock.CoilType.NICHROME);
    public static final BlockEntry<CoilBlock> COIL_TUNGSTENSTEEL = createCoilBlock(CoilBlock.CoilType.TUNGSTENSTEEL);
    public static final BlockEntry<CoilBlock> COIL_HSSG = createCoilBlock(CoilBlock.CoilType.HSSG);
    public static final BlockEntry<CoilBlock> COIL_NAQUADAH = createCoilBlock(CoilBlock.CoilType.NAQUADAH);
    public static final BlockEntry<CoilBlock> COIL_TRINIUM = createCoilBlock(CoilBlock.CoilType.TRINIUM);
    public static final BlockEntry<CoilBlock> COIL_TRITANIUM = createCoilBlock(CoilBlock.CoilType.TRITANIUM);

    //Fusion
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
    public static final Map<IFusionCasingType, Supplier<FusionCasingBlock>> ALL_FUSION_CASINGS = new HashMap<>();
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_SUPERCONDUCTOR = createFusionCasing(FusionCasingBlock.CasingType.FUSION_COIL);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_FUSION_COIL = createFusionCasing(FusionCasingBlock.CasingType.FUSION_COIL);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_MK2 = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING_MK2);
    public static final BlockEntry<FusionCasingBlock> FUSION_CASING_MK3 = createFusionCasing(FusionCasingBlock.CasingType.FUSION_CASING_MK3);
    public static final BlockEntry<Block> FUSION_GLASS = createCasingBlock("fusion_glass", GTCEu.id("block/casings/transparent/fusion_glass"), () -> Blocks.GLASS);

    // Fireboxes
    private static BlockEntry<ActiveBlock> createFireboxCasing(BoilerFireboxType type) {
        BlockEntry<ActiveBlock> block = REGISTRATE
                .block("%s_casing".formatted(type.name()), p -> new ActiveBlock(p,
                        new TextureOverrideRenderer(new ResourceLocation("block/cube_bottom_top"),
                                Map.of("bottom", type.bottom(),
                                        "top", type.top(),
                                        "side", type.side())),
                        new TextureOverrideRenderer(GTCEu.id("block/fire_box_active"),
                                Map.of("bottom", type.bottom(),
                                        "top", type.top(),
                                        "side", type.side()))))
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
    public static final Map<BoilerFireboxType, BlockEntry<ActiveBlock>> ALL_FIREBOXES = new HashMap<>();
    public static final BlockEntry<ActiveBlock> FIREBOX_BRONZE = createFireboxCasing(BoilerFireboxType.BRONZE_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_STEEL = createFireboxCasing(BoilerFireboxType.STEEL_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TITANIUM = createFireboxCasing(BoilerFireboxType.TITANIUM_FIREBOX);
    public static final BlockEntry<ActiveBlock> FIREBOX_TUNGSTENSTEEL = createFireboxCasing(BoilerFireboxType.TUNGSTENSTEEL_FIREBOX);



    // Others
    private static BlockEntry<ActiveBlock> createActiveCasing(String name, String baseModelPath) {
        String finalName = "%s".formatted(name);
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



    //////////////////////////////////////
    //**********     Misc     **********//
    //////////////////////////////////////

    public static final BlockEntry<SaplingBlock> RUBBER_SAPLING = REGISTRATE.block("rubber_sapling", properties -> new SaplingBlock(new AbstractTreeGrower() {
                protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(@Nonnull RandomSource random, boolean largeHive) {
                    return GTConfiguredFeatures.RUBBER;
                }
            }, properties))
            .initialProperties(() -> Blocks.OAK_SAPLING)
            .lang("Rubber Sapling")
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cross(Registry.BLOCK.getKey(ctx.getEntry()).getPath(), prov.blockTexture(ctx.getEntry()))))
            .addLayer(() -> RenderType::cutoutMipped)
            .tag(BlockTags.SAPLINGS)
            .item()
            .tag(ItemTags.SAPLINGS)
            .build()
            .register();

    public static final BlockEntry<RubberLogBlock> RUBBER_LOG = REGISTRATE.block("rubber_log", RubberLogBlock::new,
                    () -> BlockBehaviour.Properties.of(net.minecraft.world.level.material.Material.WOOD,
                            (state) -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MaterialColor.TERRACOTTA_GRAY : MaterialColor.COLOR_YELLOW))
            .properties(p -> p.strength(2.0F).sound(SoundType.WOOD))
            .loot((lt, b) -> lt.add(b, LootTable.lootTable()
                    .withPool(BlockLoot.applyExplosionCondition(b, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)))
                            .add(LootItem.lootTableItem(b)))
                    .withPool(BlockLoot.applyExplosionCondition(b, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)))
                            .add(LootItem.lootTableItem(GTItems.STICKY_RESIN.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(b)
                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                    .hasProperty(RubberLogBlock.NATURAL, true)))
                                    .when(LootItemRandomChanceCondition.randomChance(0.85F))))))
            .lang("Rubber Log")
            .tag(BlockTags.LOGS)
            .blockstate((ctx, provider) -> provider.logBlock(ctx.get()))
            .item()
            .tag(ItemTags.LOGS)
            .build()
            .register();

    // Fortune Level
    public static final float[] RUBBER_LEAVES_DROPPING_CHANCE = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};

    @Environment(value= EnvType.CLIENT)
    private static BlockColor leavesBlockColor() {
        return (state, reader, pos, tintIndex) -> {
            if (reader != null && pos != null) {
                //return reader.getBlockTint(pos, (biome, x, z) -> biome.getFoliageColor());
                return BiomeColors.getAverageFoliageColor(reader, pos);
            }
            return FoliageColor.getDefaultColor();
        };
    }

    @Environment(value= EnvType.CLIENT)
    private static ItemColor leavesItemColor() {
        return (stack, tintIndex) -> FoliageColor.getDefaultColor();
    }

    public static final BlockEntry<LeavesBlock> RUBBER_LEAVES = REGISTRATE
            .block("rubber_leaves", LeavesBlock::new)
            .initialProperties(() -> Blocks.OAK_LEAVES)
            .lang("Rubber Leaves")
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().singleTexture(Registry.BLOCK.getKey(ctx.getEntry()).getPath(), prov.mcLoc(BLOCK_FOLDER + "/leaves"), "all", prov.blockTexture(ctx.getEntry()))))
            .loot((table, block) -> table.add(block, RegistrateBlockLootTables.createLeavesDrops(block, GTBlocks.RUBBER_SAPLING.get(), RUBBER_LEAVES_DROPPING_CHANCE)))
            .tag(BlockTags.LEAVES)
            .color(() -> GTBlocks::leavesBlockColor)
            .item()
            .color(() -> GTBlocks::leavesItemColor)
            .tag(ItemTags.LEAVES)
            .build()
            .register();

    public static final BlockEntry<Block> RUBBER_PLANK = REGISTRATE
            .block("rubber_planks", Block::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .lang("Rubber Planks")
            .properties(p -> p.color(MaterialColor.TERRACOTTA_GRAY))
            .tag(BlockTags.PLANKS)
            .item()
            .tag(ItemTags.PLANKS)
            .build()
            .register();

    public static final BlockEntry<Block> TREATED_WOOD_PLANK = REGISTRATE
            .block("treated_wood_planks", Block::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .lang("Treated Wood Planks")
            .properties(p -> p.color(MaterialColor.TERRACOTTA_GRAY))
            .tag(BlockTags.PLANKS)
            .item()
            // purposefully omit planks item tag as this block is treated differently from wood in recipes
            .tag(TagUtil.createItemTag("treated_wood")) // matches IE treated wood tag
            .build()
            .register();


    private static <P, T extends Block, S2 extends BlockBuilder<T, P>> NonNullFunction<S2, S2> unificationBlock(@Nonnull TagPrefix tagPrefix, @Nonnull Material mat) {
        return builder -> {
            builder.onRegister(block -> ChemicalHelper.registerUnificationItems(tagPrefix, mat, block));
            return builder;
        };
    }

    public static void init() {
        registerMaterialBlocks();
        registerFrameBlocks();
        registerOreBlocks();
        registerCableBlocks();
        generateFluidPipeBlocks();
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
}
