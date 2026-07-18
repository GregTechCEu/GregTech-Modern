package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.registry.registrate.entry.PlainEntry;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.bus.api.IEventBus;

import java.util.Set;

public class WorldGenLayers {

    public static final PlainEntry<IWorldGenLayer> STONE = GTRegistration.REGISTRATE.worldGenLayer("stone",
            () -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final PlainEntry<IWorldGenLayer> DEEPSLATE = GTRegistration.REGISTRATE.worldGenLayer("deepslate",
            () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final PlainEntry<IWorldGenLayer> NETHERRACK = GTRegistration.REGISTRATE.worldGenLayer("netherrack",
            () -> new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
            Set.of(Level.NETHER));

    public static final PlainEntry<IWorldGenLayer> ENDSTONE = GTRegistration.REGISTRATE.worldGenLayer("endstone",
            () -> WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END));

    public static void init(IEventBus modBus) {}
}
