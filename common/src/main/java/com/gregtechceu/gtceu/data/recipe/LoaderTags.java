package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.tag.TagUtil;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class LoaderTags {

    // Added Vanilla tags
    public static final TagKey<Item> TAG_PISTONS = TagUtil.createItemTag("pistons");

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
