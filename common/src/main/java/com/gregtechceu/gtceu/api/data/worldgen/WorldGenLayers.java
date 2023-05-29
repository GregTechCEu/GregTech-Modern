package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import com.gregtechceu.gtceu.common.data.GTOres;
import com.mojang.serialization.Codec;
import lombok.Getter;
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
    @Getter
    private final RuleTest target;

    WorldGenLayers(String name, RuleTest target) {
        this.name = name;
        this.target = target;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }
}
