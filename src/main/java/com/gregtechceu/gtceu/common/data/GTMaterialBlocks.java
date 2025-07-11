package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.api.item.SurfaceRockBlockItem;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.Map;

@SuppressWarnings("removal")
public class GTMaterialBlocks {

    // Reference Table Builders
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<? extends Block>> MATERIAL_BLOCKS_BUILDER = ImmutableTable
            .builder();
    static ImmutableMap.Builder<Material, BlockEntry<SurfaceRockBlock>> SURFACE_ROCK_BLOCKS_BUILDER = ImmutableMap
            .builder();
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS_BUILDER = ImmutableTable
            .builder();
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS_BUILDER = ImmutableTable
            .builder();
    static ImmutableTable.Builder<TagPrefix, Material, BlockEntry<ItemPipeBlock>> ITEM_PIPE_BLOCKS_BUILDER = ImmutableTable
            .builder();

    // Reference Tables
    public static Table<TagPrefix, Material, BlockEntry<? extends Block>> MATERIAL_BLOCKS;
    public static Map<Material, BlockEntry<SurfaceRockBlock>> SURFACE_ROCK_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<CableBlock>> CABLE_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<FluidPipeBlock>> FLUID_PIPE_BLOCKS;
    public static Table<TagPrefix, Material, BlockEntry<ItemPipeBlock>> ITEM_PIPE_BLOCKS;

    // Material Blocks
    public static void generateMaterialBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Material Blocks...");

        for (TagPrefix tagPrefix : TagPrefix.values()) {
            if (!TagPrefix.ORES.containsKey(tagPrefix) && tagPrefix.doGenerateBlock()) {
                for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                    GTRegistrate registrate = registry.getRegistrate();
                    for (Material material : registry.getAllMaterials()) {
                        if (tagPrefix.doGenerateBlock(material)) {
                            registerMaterialBlock(tagPrefix, material, registrate);
                        }
                    }
                }
            }
        }
        GTCEu.LOGGER.debug("Generating GTCEu Material Blocks... Complete!");
    }

    private static void registerMaterialBlock(TagPrefix tagPrefix, Material material, GTRegistrate registrate) {
        MATERIAL_BLOCKS_BUILDER.put(tagPrefix, material, registrate
                .block(tagPrefix.idPattern().formatted(material.getName()),
                        properties -> tagPrefix.blockConstructor().create(properties, tagPrefix, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> tagPrefix.blockProperties().properties().apply(p).noLootTable())
                .transform(GTBlocks.unificationBlock(tagPrefix, material))
                .addLayer(tagPrefix.blockProperties().renderType())
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .color(() -> MaterialBlock::tintedColor)
                .item((b, p) -> tagPrefix.blockItemConstructor().create(b, p, tagPrefix, material))
                .model(NonNullBiConsumer.noop())
                .color(() -> () -> MaterialBlockItem.tintColor(material))
                .build()
                .register());
    }

    // Material Ore Blocks
    public static void generateOreBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Ore Blocks...");
        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            GTRegistrate registrate = registry.getRegistrate();
            for (Material material : registry.getAllMaterials()) {
                if (allowOreBlock(material)) {
                    registerOreBlock(material, registrate);
                }
            }
        }
        GTCEu.LOGGER.debug("Generating GTCEu Ore Blocks... Complete!");
    }

    private static boolean allowOreBlock(Material material) {
        return material.hasProperty(PropertyKey.ORE);
    }

    private static void registerOreBlock(Material material, GTRegistrate registrate) {
        for (var ore : TagPrefix.ORES.entrySet()) {
            if (ore.getKey().isIgnored(material)) continue;
            var oreTag = ore.getKey();
            final TagPrefix.OreType oreType = ore.getValue();
            String typePrefix = "";
            if (oreTag != TagPrefix.ore) {
                typePrefix = FormattingUtil.toLowerCaseUnderscore(oreTag.name) + "_";
            }
            var entry = registrate.block("%s%s_ore".formatted(typePrefix, material.getName()),
                    properties -> oreTag.blockConstructor().create(properties, oreTag, material))
                    .initialProperties(() -> {
                        if (oreType.stoneType().get().isAir()) {
                            // if the block is not registered (yet), fallback to stone
                            return Blocks.IRON_ORE;
                        }
                        return oreType.stoneType().get().getBlock();
                    })
                    .properties(properties -> GTBlocks.copy(oreType.template().get(), properties).noLootTable())
                    .transform(GTBlocks.unificationBlock(oreTag, material))
                    .blockstate(NonNullBiConsumer.noop())
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                    .color(() -> MaterialBlock::tintedColor)
                    .item((b, p) -> oreTag.blockItemConstructor().create(b, p, oreTag, material))
                    .model(NonNullBiConsumer.noop())
                    .color(() -> () -> MaterialBlockItem.tintColor(material))
                    .build()
                    .register();
            MATERIAL_BLOCKS_BUILDER.put(oreTag, material, entry);
        }
    }

    // Material Ore Indicator Piles
    public static void generateOreIndicators() {
        GTCEu.LOGGER.debug("Generating GTCEu Surface Rock Indicator Blocks...");
        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            GTRegistrate registrate = registry.getRegistrate();
            for (Material material : registry.getAllMaterials()) {
                if (allowOreIndicator(material)) {
                    registerOreIndicator(material, registrate);
                }
            }
        }
        SURFACE_ROCK_BLOCKS = SURFACE_ROCK_BLOCKS_BUILDER.build();
        GTCEu.LOGGER.debug("Generating GTCEu Surface Rock Indicator Blocks... Complete!");
    }

    private static boolean allowOreIndicator(Material material) {
        return material.hasProperty(PropertyKey.ORE);
    }

    private static void registerOreIndicator(Material material, GTRegistrate registrate) {
        var entry = registrate
                .block("%s_indicator".formatted(material.getName()), p -> new SurfaceRockBlock(p, material))
                .initialProperties(() -> Blocks.GRAVEL)
                .properties(p -> p.noLootTable().strength(0.25f))
                .transform(GTBlocks.unificationBlock(TagPrefix.surfaceRock, material))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .addLayer(() -> RenderType::cutoutMipped)
                .color(() -> SurfaceRockBlock::tintedBlockColor)
                .item((b, p) -> SurfaceRockBlockItem.create(b, p, material))
                .color(() -> SurfaceRockBlock::tintedItemColor)
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .build()
                .register();
        SURFACE_ROCK_BLOCKS_BUILDER.put(material, entry);
    }

    // Material Cable & Wire Blocks
    public static void generateCableBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Cable/Wire Blocks...");
        for (Insulation insulation : Insulation.values()) {
            for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                GTRegistrate registrate = registry.getRegistrate();
                for (Material material : registry.getAllMaterials()) {
                    if (allowCableBlock(material, insulation)) {
                        registerCableBlock(material, insulation, registrate);
                    }
                }
            }
        }
        CABLE_BLOCKS = CABLE_BLOCKS_BUILDER.build();
        GTCEu.LOGGER.debug("Generating GTCEu Cable/Wire Blocks... Complete!");
    }

    private static boolean allowCableBlock(Material material, Insulation insulation) {
        return material.hasProperty(PropertyKey.WIRE) && !insulation.tagPrefix.isIgnored(material) &&
                !(insulation.isCable && material.getProperty(PropertyKey.WIRE).isSuperconductor());
    }

    private static void registerCableBlock(Material material, Insulation insulation, GTRegistrate registrate) {
        var entry = registrate
                .block("%s_%s".formatted(material.getName(), insulation.name),
                        p -> new CableBlock(p, insulation, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.dynamicShape().noOcclusion().noLootTable().forceSolidOn())
                .transform(GTBlocks.unificationBlock(insulation.tagPrefix, material))
                .blockstate(NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .addLayer(() -> RenderType::cutoutMipped)
                .addLayer(() -> RenderType::translucent)
                .color(() -> MaterialPipeBlock::tintedColor)
                .item(MaterialPipeBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> MaterialPipeBlockItem::tintColor)
                .build()
                .register();
        CABLE_BLOCKS_BUILDER.put(insulation.tagPrefix, material, entry);
    }

    // Material Fluid Pipe Blocks
    public static void generateFluidPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Fluid Pipe Blocks...");
        for (var fluidPipeType : FluidPipeType.values()) {
            for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                GTRegistrate registrate = registry.getRegistrate();
                for (Material material : registry.getAllMaterials()) {
                    if (allowFluidPipeBlock(material, fluidPipeType)) {
                        registerFluidPipeBlock(material, fluidPipeType, registrate);
                    }
                }
            }
        }
        FLUID_PIPE_BLOCKS = FLUID_PIPE_BLOCKS_BUILDER.build();
        GTCEu.LOGGER.debug("Generating GTCEu Fluid Pipe Blocks... Complete!");
    }

    private static boolean allowFluidPipeBlock(Material material, FluidPipeType fluidPipeType) {
        return material.hasProperty(PropertyKey.FLUID_PIPE) && !fluidPipeType.tagPrefix.isIgnored(material);
    }

    private static void registerFluidPipeBlock(Material material, FluidPipeType fluidPipeType,
                                               GTRegistrate registrate) {
        var entry = registrate
                .block("%s_%s_fluid_pipe".formatted(material.getName(), fluidPipeType.name),
                        p -> new FluidPipeBlock(p, fluidPipeType, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> {
                    if (GTBlocks.doMetalPipe(material)) {
                        p.sound(GTSoundTypes.METAL_PIPE);
                    }
                    return p.dynamicShape().noOcclusion().noLootTable().forceSolidOn();
                })
                .transform(GTBlocks.unificationBlock(fluidPipeType.tagPrefix, material))
                .blockstate(NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .addLayer(() -> RenderType::cutoutMipped)
                .addLayer(() -> RenderType::translucent)
                .color(() -> MaterialPipeBlock::tintedColor)
                .item(MaterialPipeBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> MaterialPipeBlockItem::tintColor)
                .build()
                .register();
        FLUID_PIPE_BLOCKS_BUILDER.put(fluidPipeType.tagPrefix, material, entry);
    }

    // Material Item Pipe Blocks
    public static void generateItemPipeBlocks() {
        GTCEu.LOGGER.debug("Generating GTCEu Item Pipe Blocks...");
        for (var itemPipeType : ItemPipeType.values()) {
            for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
                GTRegistrate registrate = registry.getRegistrate();
                for (Material material : registry.getAllMaterials()) {
                    if (allowItemPipeBlock(material, itemPipeType)) {
                        registerItemPipeBlock(material, itemPipeType, registrate);
                    }
                }
            }
        }
        ITEM_PIPE_BLOCKS = ITEM_PIPE_BLOCKS_BUILDER.build();
        GTCEu.LOGGER.debug("Generating GTCEu Item Pipe Blocks... Complete!");
    }

    private static boolean allowItemPipeBlock(Material material, ItemPipeType itemPipeType) {
        return material.hasProperty(PropertyKey.ITEM_PIPE) && !itemPipeType.getTagPrefix().isIgnored(material);
    }

    private static void registerItemPipeBlock(Material material, ItemPipeType itemPipeType, GTRegistrate registrate) {
        var entry = registrate
                .block("%s_%s_item_pipe".formatted(material.getName(), itemPipeType.name),
                        p -> new ItemPipeBlock(p, itemPipeType, material))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> {
                    if (GTBlocks.doMetalPipe(material)) {
                        p.sound(GTSoundTypes.METAL_PIPE);
                    }
                    return p.dynamicShape().noOcclusion().noLootTable().forceSolidOn();
                })
                .transform(GTBlocks.unificationBlock(itemPipeType.getTagPrefix(), material))
                .blockstate(NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .addLayer(() -> RenderType::cutoutMipped)
                .addLayer(() -> RenderType::translucent)
                .color(() -> MaterialPipeBlock::tintedColor)
                .item(MaterialPipeBlockItem::new)
                .model(NonNullBiConsumer.noop())
                .color(() -> MaterialPipeBlockItem::tintColor)
                .build()
                .register();
        ITEM_PIPE_BLOCKS_BUILDER.put(itemPipeType.getTagPrefix(), material, entry);
    }
}
