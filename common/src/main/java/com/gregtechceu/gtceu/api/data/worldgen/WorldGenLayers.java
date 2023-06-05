package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.gregtechceu.gtceu.common.data.GTOres;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryObjectBuilderTypes;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

public enum WorldGenLayers implements IWorldGenLayer, StringRepresentable {
    STONE("stone", OreFeatures.STONE_ORE_REPLACEABLES),
    DEEPSLATE("deepslate", OreFeatures.DEEPSLATE_ORE_REPLACEABLES),
    NETHERRACK("netherrack", OreFeatures.NETHER_ORE_REPLACEABLES),
    ENDSTONE("endstone", GTOres.END_ORE_REPLACEABLES);

    private final String name;
    @Getter @Setter
    private RuleTest target;

    WorldGenLayers(String name, RuleTest target) {
        this.name = name;
        this.target = target;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    public static void registerAll() {
        AddonFinder.getAddons().forEach(IGTAddon::registerWorldgenLayers);
        if (GTCEu.isKubeJSLoaded()) {
            GTRegistryObjectBuilderTypes.registerAndModifyFor(GTRegistryObjectBuilderTypes.WORLD_GEN_LAYER.registryKey);
        }
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }
}
