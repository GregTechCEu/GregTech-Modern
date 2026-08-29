package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.Set;

public class WorldGenLayers {

    public static final Holder<IWorldGenLayer> STONE = register("stone",
            () -> new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final Holder<IWorldGenLayer> DEEPSLATE = register("deepslate",
            () -> new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
            Set.of(Level.OVERWORLD));

    public static final Holder<IWorldGenLayer> NETHERRACK = register("netherrack",
            () -> new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES),
            Set.of(Level.NETHER));

    public static final Holder<IWorldGenLayer> ENDSTONE = register("endstone",
            () -> WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END));

    private static Holder<IWorldGenLayer> register(String name, IWorldGenLayer.RuleTestSupplier target,
                                                   Set<ResourceKey<Level>> levels) {
        return GTRegistration.REGISTRATE.simple(name, GTRegistries.Keys.WORLD_GEN_LAYER, () -> new SimpleWorldGenLayer(GTCEu.id(name), target, levels));
    }

    public static void init() {}
}
