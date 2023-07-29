package com.gregtechceu.gtceu.data.fabric;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.tags.IBiomeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.concurrent.CompletableFuture;

public class BiomeTagsProviderImpl extends FabricTagProvider<Biome> implements IBiomeTagsProvider<FabricTagProvider<Biome>.FabricTagBuilder> {

    protected BiomeTagsProviderImpl(FabricDataOutput dataGenerator, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataGenerator, Registries.BIOME, registriesFuture);
    }

    @Override
    public void addRubberTreeTag() {
        tag(CustomTags.HAS_RUBBER_TREE).forceAddTag(CustomTags.IS_SWAMP).forceAddTag(BiomeTags.IS_FOREST).forceAddTag(BiomeTags.IS_JUNGLE);
    }

    public void addSandyTag() {
        tag(CustomTags.IS_SANDY).forceAddTag(ConventionalBiomeTags.CLIMATE_DRY).addOptional(Biomes.DESERT).addOptional(Biomes.BEACH).addOptional(Biomes.BADLANDS).addOptional(Biomes.WOODED_BADLANDS);
    }

    @Override
    public FabricTagProvider<Biome>.FabricTagBuilder tag(TagKey<Biome> tag) {
        return this.getOrCreateTagBuilder(tag);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        generateTags();
    }
}
