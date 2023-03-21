package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTLayerPattern;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOres
 */
public class GTOres {
    public static final GTOreFeatureEntry COIL = create("coil", 120, 1 / 12f, -63, 70)
            .standardDatagenExt()
            .withBlock(GTBlocks.CASING)
            .biomeTag(BiomeTags.IS_OVERWORLD)
            .parent();

    public static final NonNullSupplier<GTLayerPattern> LIMESTONE = () -> GTLayerPattern.builder()
            .layer(l -> l.weight(2).passiveBlock())
            .layer(l -> l.weight(2)
                    .block(GTBlocks.WIRE_COIL)
                    .size(1, 4))
            .build();

    public static final NonNullSupplier<GTLayerPattern> SCORIA = () -> GTLayerPattern.builder()
            .layer(l -> l.weight(1)
                    .passiveBlock())
            .layer(l -> l.weight(2)
                    .block(GTBlocks.CASING)
                    .size(1, 3))
            .layer(l -> l.weight(2)
                    .block(Blocks.GLASS)
                    .block(Blocks.GRASS_BLOCK)
                    .size(1, 2))
            .layer(l -> l.weight(1)
                    .blocks(Blocks.DIAMOND_BLOCK, Blocks.ANDESITE))
            .layer(l -> l.weight(1)
                    .block(Blocks.STONE))
            .build();


    public static final GTOreFeatureEntry STRIATED_ORES_OVERWORLD =
            create("layer_coil", 32, 1 / 12f, 40, 90)
                    .layeredDatagenExt()
                    .withLayerPattern(LIMESTONE)
                    .biomeTag(BiomeTags.IS_OVERWORLD)
                    .parent();

    private static GTOreFeatureEntry create(String name, int clusterSize, float frequency, int minHeight, int maxHeight) {
        return new GTOreFeatureEntry(GTCEu.id(name), clusterSize, frequency, minHeight, maxHeight);
    }

    public static void init() {
        register();
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }
}
