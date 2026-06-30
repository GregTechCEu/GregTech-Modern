package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.fml.ModLoader;

import java.util.Set;

public class WorldGenLayers {

    static {
        GTRegistries.WORLD_GEN_LAYERS.unfreeze();
    }

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

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerWorldgenLayers);
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.WORLD_GEN_LAYERS, IWorldGenLayer.class));
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.WORLD_GEN_LAYERS.getRegistryName());
        }
        GTRegistries.WORLD_GEN_LAYERS.freeze();
    }
}
