package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public enum WorldGenLayers implements IWorldGenLayer, StringRepresentable {

    STONE("stone", new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Level.OVERWORLD),
    DEEPSLATE("deepslate", new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), Level.OVERWORLD),
    NETHERRACK("netherrack", new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES), Level.NETHER),
    ENDSTONE("endstone", WorldGeneratorUtils.END_ORE_REPLACEABLES, Level.END);

    private final String name;

    @SuppressWarnings("NonFinalFieldInEnum")
    @Getter
    @Setter
    private Set<ResourceKey<Level>> dimensions;

    @SuppressWarnings("NonFinalFieldInEnum")
    @Getter
    @Setter
    private RuleTest target;

    @SafeVarargs
    WorldGenLayers(String name, RuleTest target, ResourceKey<Level>... dimensions) {
        this.name = name;
        this.target = target;
        this.dimensions = Set.of(dimensions);
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    public static void registerAll() {
        AddonFinder.getAddons().forEach(IGTAddon::registerWorldgenLayers);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistryInfo.WORLD_GEN_LAYER.registryKey);
        }
    }

    public static IWorldGenLayer getByName(String name) {
        return WorldGeneratorUtils.WORLD_GEN_LAYERS.get(name);
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public boolean isApplicableForLevel(ResourceKey<Level> dimension) {
        return this.dimensions.contains(dimension);
    }
}
