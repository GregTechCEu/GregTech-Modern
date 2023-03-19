package com.gregtechceu.gtceu.common.data;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.MaterialPipeBlockItem;
import com.gregtechceu.gtceu.common.block.CompressedBlock;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.block.FrameBlock;
import com.gregtechceu.gtceu.common.block.variant.*;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.VariantBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import javax.annotation.Nonnull;

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
                var entry = REGISTRATE.block("compressed_block_%s".formatted(material.getName()), properties -> new CompressedBlock(properties, material))
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
                var entry = REGISTRATE.block("frame_block_%s".formatted(material.getName()), properties -> new FrameBlock(properties, material))
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

    public final static BlockEntry<CasingBlock> CASING = REGISTRATE.block("casing", CasingBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(blockState -> ConfiguredModel.builder()
                    .modelFile(prov.models().cubeAll("block/%s/%s".formatted(ctx.getName(), ctx.getEntry().getVariant(blockState)),
                            ctx.getEntry().getVariant(blockState).getTexture()))
                    .build()))
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(VariantBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public final static BlockEntry<HullCasingBlock> HULL_CASING = REGISTRATE.block("hull_casing", HullCasingBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(blockState -> ConfiguredModel.builder()
                    .modelFile(prov.models().withExistingParent("block/%s/%s".formatted(ctx.getName(), ctx.getEntry().getVariant(blockState)),
                            GTCEu.id("block/cube_bottom_top_tintindex"))
                            .texture("bottom", ctx.getEntry().getVariant(blockState).getBottomTexture())
                            .texture("top", ctx.getEntry().getVariant(blockState).geTopTexture())
                            .texture("side", ctx.getEntry().getVariant(blockState).getSideTexture()))
                    .build()))
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(VariantBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public final static BlockEntry<CoilBlock> WIRE_COIL = REGISTRATE.block("wire_coil", CoilBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(blockState -> ConfiguredModel.builder()
                    .modelFile(
                            ctx.getEntry().isActive(blockState) ?
                                    prov.models().withExistingParent("block/%s/%s_active".formatted(ctx.getName(), ctx.getEntry().getVariant(blockState)), "gtceu:block/cube_2_layer_all")
                                            .texture("bot_all", ctx.getEntry().getVariant(blockState).getTexture())
                                            .texture("top_all", ctx.getEntry().getVariant(blockState).getTexture().toString() + "_bloom")
                                    :
                                    prov.models().cubeAll("block/%s/%s".formatted(ctx.getName(), ctx.getEntry().getVariant(blockState)),
                                            ctx.getEntry().getVariant(blockState).getTexture()))
                    .build()))
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(VariantBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public final static BlockEntry<ActiveCasingBlock> ACTIVE_CASING = REGISTRATE.block("active_casing", ActiveCasingBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(blockState -> ConfiguredModel.builder()
                    .modelFile(
                            ctx.getEntry().isActive(blockState) ?
                                    prov.models().getExistingFile(ctx.getEntry().getVariant(blockState).getActiveModel())
                                    :
                                    prov.models().getExistingFile(ctx.getEntry().getVariant(blockState).getModel()))
                    .build()))
            .lang("Casing")
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(VariantBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();

    public final static BlockEntry<BoilerFireBoxCasingBlock> BOILER_FIREBOX_CASING = REGISTRATE.block("boiler_firebox_casing", BoilerFireBoxCasingBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(blockState -> ConfiguredModel.builder()
                    .modelFile(
                            ctx.getEntry().isActive(blockState) ?
                                    prov.models().withExistingParent("block/%s/%s_active".formatted(ctx.getName(), ctx.getEntry().getVariant(blockState)), "gtceu:block/fire_box_active")
                                            .texture("bottom", ctx.getEntry().getVariant(blockState).getBottom())
                                            .texture("top", ctx.getEntry().getVariant(blockState).getTop())
                                            .texture("side", ctx.getEntry().getVariant(blockState).getSide())
                                    :
                                    prov.models().cubeBottomTop("block/%s/%s".formatted(ctx.getName(), ctx.getEntry().getVariant(blockState)),
                                            ctx.getEntry().getVariant(blockState).getSide(), ctx.getEntry().getVariant(blockState).getBottom(), ctx.getEntry().getVariant(blockState).getTop()))
                    .build()))
            .lang("Casing")
            .tag(GTToolType.WRENCH.harvestTag, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(VariantBlockItem::new)
            .model(NonNullBiConsumer.noop())
            .build()
            .register();


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
