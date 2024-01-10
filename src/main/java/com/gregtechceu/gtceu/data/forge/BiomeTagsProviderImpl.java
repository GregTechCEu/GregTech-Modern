package com.gregtechceu.gtceu.data.forge;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.tags.IBiomeTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BiomeTagsProviderImpl extends BiomeTagsProvider implements IBiomeTagsProvider<TagsProvider.TagAppender<Biome>> {

    public BiomeTagsProviderImpl(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, "gtceu", existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.generateTags();
    }

    @Override
    public void generateTags() {
        IBiomeTagsProvider.super.generateTags();
    }

    @Override
    public void addRubberTreeTag() {
        //noinspection unchecked
        tag(CustomTags.HAS_RUBBER_TREE).addTags(CustomTags.IS_SWAMP, BiomeTags.IS_FOREST, BiomeTags.IS_JUNGLE);
    }

    @Override
    public void addSandyTag() {
        tag(CustomTags.IS_SANDY).addTag(Tags.Biomes.IS_SANDY).add(Biomes.DESERT, Biomes.BEACH, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
    }

    @Override
    public TagAppender<Biome> tag(TagKey<Biome> tag) {
        return super.tag(tag);
    }
}
