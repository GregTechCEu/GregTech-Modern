package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.Set;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class WorldGenLayers {

    public static final Holder<SimpleWorldGenLayer> STONE = REGISTRATE.simpleWorldGenLayer(
            "stone", () -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final Holder<SimpleWorldGenLayer> DEEPSLATE = REGISTRATE.simpleWorldGenLayer(
            "deepslate", () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final Holder<SimpleWorldGenLayer> NETHERRACK = REGISTRATE.simpleWorldGenLayer(
            "netherrack", () -> new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
            Set.of(Level.NETHER));
    public static final Holder<SimpleWorldGenLayer> ENDSTONE = REGISTRATE.simpleWorldGenLayer(
            "endstone", () -> WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END));

    public static void init() {}
}
