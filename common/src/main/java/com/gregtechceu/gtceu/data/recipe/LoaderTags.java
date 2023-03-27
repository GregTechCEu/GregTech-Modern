package com.gregtechceu.gtceu.data.recipe;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class LoaderTags {

    // Vanilla tags

    // Platform-dependent tags
    public static final TagKey<Item> TAG_WOODEN_CHESTS;

    static {
        TAG_WOODEN_CHESTS = getWoodChestTag();
    }

    @ExpectPlatform
    public static TagKey<Item> getWoodChestTag() {
        throw new AssertionError();
    }
}
