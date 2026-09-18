package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.Set;

public class WorldGenLayers {

    public static final SimpleWorldGenLayer STONE = new SimpleWorldGenLayer(
            GTCEu.id("stone"), () -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD.location()));

    public static final SimpleWorldGenLayer DEEPSLATE = new SimpleWorldGenLayer(
            GTCEu.id("deepslate"), () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD.location()));

    public static final SimpleWorldGenLayer NETHERRACK = new SimpleWorldGenLayer(
            GTCEu.id("netherrack"), () -> new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
            Set.of(Level.NETHER.location()));
    public static final SimpleWorldGenLayer ENDSTONE = new SimpleWorldGenLayer(
            GTCEu.id("endstone"), () -> WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END.location()));

    public static void init() {}
}
