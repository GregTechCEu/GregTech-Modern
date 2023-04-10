package com.gregtechceu.gtceu.data.recipe.fabric;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CustomTagsImpl {

    public static TagKey<Item> getWoodChestTag() {
        // could also use "wooden_chests", unsure which is better
        return TagUtil.createItemTag("chests");
    }
}
