package com.gregtechceu.gtceu.data.fabric;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.tags.IBiomeTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.concurrent.CompletableFuture;

public class BiomeTagsProviderImpl extends FabricTagProvider<Biome> implements IBiomeTagsProvider<FabricTagProvider<Biome>.FabricTagBuilder> {

    protected BiomeTagsProviderImpl(FabricDataOutput dataGenerator, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(dataGenerator, Registries.BIOME, registriesFuture);
    }

    @Override
    public void addRubberTreeTag() {
        tag(CustomTags.HAS_RUBBER_TREE).forceAddTag(CustomTags.IS_SWAMP).forceAddTag(BiomeTags.IS_FOREST).forceAddTag(BiomeTags.IS_JUNGLE);
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
