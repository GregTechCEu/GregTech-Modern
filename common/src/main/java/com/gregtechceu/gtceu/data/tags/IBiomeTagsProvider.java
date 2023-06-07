package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface IBiomeTagsProvider<T extends TagsProvider.TagAppender<Biome>> {

    default void generateTags() {
        tag(CustomTags.IS_SWAMP, Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
        addRubberTreeTag();
    }

    void addRubberTreeTag();

    @SafeVarargs
    private void tag(ResourceKey<Biome> biome, TagKey<Biome>... tags) {
        for (TagKey<Biome> key : tags) {
            this.tag(key).add(biome);
        }
    }

    @SafeVarargs
    private void tag(TagKey<Biome> key, ResourceKey<Biome>... biomes) {
        for (ResourceKey<Biome> biome : biomes) {
            this.tag(key).add(biome);
        }
    }


    T tag(TagKey<Biome> tag);
}
