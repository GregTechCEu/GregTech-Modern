package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.tag.TagUtil;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CommonTags {

    // Vanilla tags
    public static final TagKey<Item> TAG_PLANKS = TagUtil.createItemTag("planks", true);
    public static final TagKey<Item> TAG_LOGS = TagUtil.createItemTag("logs", true);
    public static final TagKey<Item> TAG_WOODEN_SLABS = TagUtil.createItemTag("wooden_slabs", true);
    public static final TagKey<Item> TAG_WOODEN_FENCES = TagUtil.createItemTag("wooden_fences", true);
    public static final TagKey<Item> TAG_WOODEN_TRAP_DOORS = TagUtil.createItemTag("wooden_trapdoors", true);
    public static final TagKey<Item> TAG_CARPETS = TagUtil.createItemTag("wool_carpets", true);
    public static final TagKey<Item> TAG_WOOL = TagUtil.createItemTag("wool", true);
    public static final TagKey<Item> TAG_SIGNS = TagUtil.createItemTag("signs", true);
    public static final TagKey<Item> TAG_COALS = TagUtil.createItemTag("coals", true);
    public static final TagKey<Item> TAG_SAPLINGS = TagUtil.createItemTag("saplings", true);
    public static final TagKey<Item> TAG_SAND = TagUtil.createItemTag("sand", true);

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
