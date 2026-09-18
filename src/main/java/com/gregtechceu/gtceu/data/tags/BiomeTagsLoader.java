package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BiomeTagsLoader extends BiomeTagsProvider {

    public BiomeTagsLoader(PackOutput arg, CompletableFuture<HolderLookup.Provider> completableFuture,
                           @Nullable ExistingFileHelper existingFileHelper) {
        super(arg, completableFuture, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(GTTags.Biomes.IS_SWAMP).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
        tag(GTTags.Biomes.HAS_RUBBER_TREE).addTag(GTTags.Biomes.IS_SWAMP).addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_JUNGLE);
        tag(GTTags.Biomes.IS_SANDY).addTag(Tags.Biomes.IS_SANDY).add(Biomes.DESERT).add(Biomes.BEACH)
                .add(Biomes.BADLANDS).add(Biomes.WOODED_BADLANDS);
    }
}
