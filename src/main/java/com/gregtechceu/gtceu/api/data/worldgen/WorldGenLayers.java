package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryObjectBuilderTypes;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author Screret
 * @date 2023/6/9
 * @implNote WorldGenLayers
 */
public enum WorldGenLayers implements IWorldGenLayer, StringRepresentable {
    STONE(
            "stone", OreFeatures.STONE_ORE_REPLACEABLES,
            Set.of(Level.OVERWORLD.location())
    ),
    DEEPSLATE(
            "deepslate", OreFeatures.DEEPSLATE_ORE_REPLACEABLES,
            Set.of(Level.OVERWORLD.location())
    ),
    NETHERRACK(
            "netherrack", OreFeatures.NETHER_ORE_REPLACEABLES,
            Set.of(Level.NETHER.location())
    ),
    ENDSTONE(
            "endstone", WorldGeneratorUtils.END_ORE_REPLACEABLES,
            Set.of(Level.END.location())
    );

    private final String name;

    @SuppressWarnings("NonFinalFieldInEnum")
    @Getter @Setter
    private Set<ResourceLocation> levels;

    @SuppressWarnings("NonFinalFieldInEnum")
    @Getter @Setter
    private RuleTest target;

    WorldGenLayers(String name, RuleTest target, Set<ResourceLocation> levels) {
        this.name = name;
        this.target = target;
        this.levels = levels;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    public static void registerAll() {
        AddonFinder.getAddons().forEach(IGTAddon::registerWorldgenLayers);
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryObjectBuilderTypes.registerFor(GTRegistryObjectBuilderTypes.WORLD_GEN_LAYER.registryKey);
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
    public boolean isApplicableForLevel(ResourceLocation level) {
        return levels.contains(level);
    }
}
