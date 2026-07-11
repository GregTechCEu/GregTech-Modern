package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.WorldGenLayerEventJS;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.Set;

public class WorldGenLayers {

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

    public static void init() {
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.postEvent();
        }
    }

    private static final class KJSCallWrapper {

        private static void postEvent() {
            GTCEuStartupEvents.WORLD_GEN_LAYERS.post(new WorldGenLayerEventJS());
        }
    }
}
