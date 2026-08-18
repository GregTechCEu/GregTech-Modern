package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.*;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ore;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.oreNetherrack;

@SuppressWarnings("unused")
public class GTOres {

    /**
     * The size of the largest registered vein.
     * This becomes available after all veins have been loaded.
     */
    @Getter
    private static int largestVeinSize = 0;

    @Getter
    private static int largestIndicatorOffset = 0;

    private static final Map<ResourceLocation, GTOreDefinition> toReRegister = new HashMap<>();

    private static GTOreDefinition create(String name, Consumer<GTOreDefinition> config) {
        return create(GTCEu.id(name), config);
    }

    public static GTOreDefinition create(ResourceLocation name, Consumer<GTOreDefinition> config) {
        GTOreDefinition def = blankOreDefinition();
        config.accept(def);

        def.register(name);
        toReRegister.put(name, def);

        return def;
    }

    private static Supplier<? extends Block> ore(TagPrefix oreTag, Material material) {
        var block = GTMaterialBlocks.MATERIAL_BLOCKS.get(oreTag, material);
        if (block == null) {
            ResourceLocation oreKey;
            if (oreTag == ore) {
                oreKey = new ResourceLocation("%s_ore".formatted(material.getName()));
            } else if (oreTag == oreNetherrack) {
                oreKey = new ResourceLocation("nether_%s_ore".formatted(material.getName()));
            } else {
                oreKey = new ResourceLocation("%s_%s_ore".formatted(oreTag.name, material.getName()));
            }
            return BuiltInRegistries.BLOCK.containsKey(oreKey) ? () -> BuiltInRegistries.BLOCK.get(oreKey) :
                    () -> Blocks.AIR;
        }
        return block;
    }

    public static void init() {
        toReRegister.forEach(GTRegistries.ORE_VEINS::registerOrOverride);
    }

    public static void updateLargestVeinSize() {
        // map to average of min & max values.
        GTOres.largestVeinSize = GTRegistries.ORE_VEINS.values().stream()
                .map(GTOreDefinition::clusterSize)
                .mapToInt(intProvider -> (intProvider.getMinValue() + intProvider.getMaxValue()) / 2)
                .max()
                .orElse(0);

        GTOres.largestIndicatorOffset = GTRegistries.ORE_VEINS.values().stream()
                .flatMapToInt(definition -> definition.indicatorGenerators().stream()
                        .mapToInt(indicatorGenerator -> indicatorGenerator.getSearchRadiusModifier(
                                (int) Math.ceil(definition.clusterSize().getMinValue() / 2.0))))
                .max()
                .orElse(0);
    }

    public static GTOreDefinition blankOreDefinition() {
        return new GTOreDefinition(
                ConstantInt.ZERO, 0, 0, IWorldGenLayer.NOWHERE, Set.of(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(0)),
                0, HolderSet::direct, BiomeWeightModifier.EMPTY, NoopVeinGenerator.INSTANCE,
                new ArrayList<>());
    }
}
