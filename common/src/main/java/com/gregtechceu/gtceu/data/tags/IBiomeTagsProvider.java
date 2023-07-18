package com.gregtechceu.gtceu.data.tags;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public interface IBiomeTagsProvider<T extends TagsProvider.TagAppender<Biome>> {

    default void generateTags() {
        //TODO what the hell?
//        tag(CustomTags.IS_SWAMP, Biomes.SWAMP, Biomes.MANGROVE_SWAMP);
//        addRubberTreeTag();
//        addSandyTag();
    }

    void addRubberTreeTag();

    void addSandyTag();

    @SafeVarargs
    private void tag(ResourceKey<Biome> biome, TagKey<Biome>... tags) {
        for (TagKey<Biome> key : tags) {
            this.tag(key).add(biome);
        }
    }

    @SafeVarargs
    private void tag(TagKey<Biome> key, ResourceKey<Biome>... biomes) {
        this.tag(key).add(biomes);
    }


    T tag(TagKey<Biome> tag);
}
