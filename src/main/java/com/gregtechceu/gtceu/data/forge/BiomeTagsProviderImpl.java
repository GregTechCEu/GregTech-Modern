package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.tags.IBiomeTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BiomeTagsProviderImpl extends BiomeTagsProvider {

    public BiomeTagsProviderImpl(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(CustomTags.IS_SWAMP).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
        tag(CustomTags.HAS_RUBBER_TREE).addTags(CustomTags.IS_SWAMP).addOptionalTag(BiomeTags.IS_FOREST.location()).addOptionalTag(BiomeTags.IS_JUNGLE.location());
        tag(CustomTags.IS_SANDY).addOptionalTag(Tags.Biomes.IS_SANDY.location()).addOptional(Biomes.DESERT.location()).addOptional(Biomes.BEACH.location()).addOptional(Biomes.BADLANDS.location()).addOptional(Biomes.WOODED_BADLANDS.location());
    }
}
