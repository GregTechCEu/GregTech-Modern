package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.common.registry.GTRegistration;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.bus.api.IEventBus;

import java.util.Set;

public class WorldGenLayers {


    public static void init(IEventBus modBus) {
    }

    public static final SimpleWorldGenLayer STONE = GTRegistration.REGISTRATE.simpleWorldGenLayer(
            "stone", () -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final SimpleWorldGenLayer DEEPSLATE = GTRegistration.REGISTRATE.simpleWorldGenLayer(
            "deepslate", () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final SimpleWorldGenLayer NETHERRACK = GTRegistration.REGISTRATE.simpleWorldGenLayer(
            "netherrack", () -> new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
            Set.of(Level.NETHER));

    public static final SimpleWorldGenLayer ENDSTONE = GTRegistration.REGISTRATE.simpleWorldGenLayer(
            "endstone", () -> WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END));
}
