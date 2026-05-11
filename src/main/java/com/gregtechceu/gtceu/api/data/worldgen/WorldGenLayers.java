package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.WorldGenLayerEventJS;

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

    STONE("stone", new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), Set.of(Level.OVERWORLD)),
    DEEPSLATE("deepslate", new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), Set.of(Level.OVERWORLD)),
    NETHERRACK("netherrack", new TagMatchTest(BlockTags.NETHER_CARVER_REPLACEABLES), Set.of(Level.NETHER)),
    ENDSTONE("endstone", WorldGeneratorUtils.END_ORE_REPLACEABLES, Set.of(Level.END));

    private final String name;

    @SuppressWarnings("NonFinalFieldInEnum")
    @Getter
    @Setter
    private Set<ResourceKey<Level>> levels;

    @SuppressWarnings("NonFinalFieldInEnum")
    @Getter
    @Setter
    private RuleTest target;

    WorldGenLayers(String name, RuleTest target, Set<ResourceKey<Level>> levels) {
        this.name = name;
        this.target = target;
        this.levels = levels;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    public static void registerAll() {
        AddonFinder.getAddonList().forEach(IGTAddon::registerWorldgenLayers);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.postEvent();
        }
    }

    public static IWorldGenLayer getByName(String name) {
        return WorldGeneratorUtils.WORLD_GEN_LAYERS.get(name);
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }

    @Override
    public boolean isApplicableForLevel(ResourceKey<Level> level) {
        return levels.contains(level);
    }

    private static final class KJSCallWrapper {

        private static void postEvent() {
            GTCEuStartupEvents.WORLD_GEN_LAYERS.post(new WorldGenLayerEventJS());
        }
    }
}
