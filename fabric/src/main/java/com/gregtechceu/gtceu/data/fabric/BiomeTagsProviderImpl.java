package com.gregtechceu.gtceu.data.fabric;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.tags.IBiomeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.Registry;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class BiomeTagsProviderImpl extends FabricTagProvider.DynamicRegistryTagProvider<Biome> implements IBiomeTagsProvider<FabricTagProvider<Biome>.FabricTagBuilder<Biome>> {

    protected BiomeTagsProviderImpl(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.BIOME_REGISTRY);
    }

    @Override
    public void generateTags() {
        IBiomeTagsProvider.super.generateTags();
    }

    @Override
    public void addRubberTreeTag() {
        tag(CustomTags.HAS_RUBBER_TREE).forceAddTag(CustomTags.IS_SWAMP).forceAddTag(BiomeTags.IS_FOREST).forceAddTag(BiomeTags.IS_JUNGLE);
    }

    @Override
    public void addSandyTag() {
        tag(CustomTags.IS_SANDY).forceAddTag(ConventionalBiomeTags.CLIMATE_DRY).add(Biomes.DESERT, Biomes.BEACH, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
    }

    @Override
    public FabricTagProvider<Biome>.FabricTagBuilder<Biome> tag(TagKey<Biome> tag) {
        return this.getOrCreateTagBuilder(tag);
    }
}
