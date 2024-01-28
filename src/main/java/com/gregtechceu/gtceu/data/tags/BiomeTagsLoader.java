package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BiomeTagsLoader extends BiomeTagsProvider {

    public BiomeTagsLoader(DataGenerator arg, @Nullable ExistingFileHelper existingFileHelper) {
        super(arg, GTCEu.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(CustomTags.IS_SWAMP).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP);
        tag(CustomTags.HAS_RUBBER_TREE).addTag(CustomTags.IS_SWAMP).addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_JUNGLE);
        tag(CustomTags.IS_SANDY).addTag(Tags.Biomes.IS_SANDY).add(Biomes.DESERT).add(Biomes.BEACH).add(Biomes.BADLANDS).add(Biomes.WOODED_BADLANDS);
    }
}
