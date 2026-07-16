package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class WorldGenLayers {

    private static final DeferredRegister<IWorldGenLayer> WORLD_GEN_LAYERS = DeferredRegister
            .create(GTRegistries.Keys.WORLD_GEN_LAYER, GTCEu.MOD_ID);

    public static void init(IEventBus modBus) {
        WORLD_GEN_LAYERS.register(modBus);
    }

    public static final SimpleWorldGenLayer STONE = new SimpleWorldGenLayer(
            GTCEu.id("stone"), () -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final SimpleWorldGenLayer DEEPSLATE = new SimpleWorldGenLayer(
            GTCEu.id("deepslate"), () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final SimpleWorldGenLayer NETHERRACK = new SimpleWorldGenLayer(
            GTCEu.id("netherrack"), () -> new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
            Set.of(Level.NETHER));

    public static final SimpleWorldGenLayer ENDSTONE = new SimpleWorldGenLayer(
            GTCEu.id("endstone"), () -> WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END));
}
