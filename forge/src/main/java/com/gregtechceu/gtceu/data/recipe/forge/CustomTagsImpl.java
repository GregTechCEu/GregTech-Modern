package com.gregtechceu.gtceu.data.recipe.forge;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CustomTagsImpl {

    public static TagKey<Item> getWoodChestTag() {
        return TagUtil.createItemTag("chests/wooden");
    }
}
