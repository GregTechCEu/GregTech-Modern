package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.common.data.GTOres;
import com.mojang.serialization.Codec;
import lombok.Getter;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public enum WorldGenLayer implements StringRepresentable {
    STONE("stone", OreFeatures.STONE_ORE_REPLACEABLES),
    DEEPSLATE("deepslate", OreFeatures.DEEPSLATE_ORE_REPLACEABLES),
    NETHERRACK("netherrack", OreFeatures.NETHER_ORE_REPLACEABLES),
    ENDSTONE("endstone", GTOres.END_ORE_REPLACEABLES);

    public static final Codec<WorldGenLayer> CODEC = Codec.STRING.xmap(WorldGenLayer::valueOf, layer -> layer.name);

    @Getter
    private final String name;
    @Getter
    private final RuleTest target;

    WorldGenLayer(String name, RuleTest target) {
        this.name = name;
        this.target = target;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
