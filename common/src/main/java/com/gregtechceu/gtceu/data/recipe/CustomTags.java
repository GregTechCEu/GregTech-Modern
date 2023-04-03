package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CustomTags {

    // Added Vanilla tags
    public static final TagKey<Item> TAG_PISTONS = TagUtil.createItemTag("pistons");

    // Added Gregtech tags
    public static final TagKey<Item> TRANSISTORS = TagUtil.createItemTag("transistors");
    public static final TagKey<Item> RESISTORS = TagUtil.createItemTag("resistors");
    public static final TagKey<Item> CAPACITORS = TagUtil.createItemTag("capacitors");
    public static final TagKey<Item> DIODES = TagUtil.createItemTag("diodes");
    public static final TagKey<Item> INDUCTORS = TagUtil.createItemTag("inductors");

    public static final TagKey<Item> ULV_CIRCUITS = TagUtil.createItemTag("ulv_circuits");
    public static final TagKey<Item> LV_CIRCUITS = TagUtil.createItemTag("lv_circuits");
    public static final TagKey<Item> MV_CIRCUITS = TagUtil.createItemTag("mv_circuits");
    public static final TagKey<Item> HV_CIRCUITS = TagUtil.createItemTag("hv_circuits");
    public static final TagKey<Item> EV_CIRCUITS = TagUtil.createItemTag("ev_circuits");
    public static final TagKey<Item> IV_CIRCUITS = TagUtil.createItemTag("iv_circuits");
    public static final TagKey<Item> LuV_CIRCUITS = TagUtil.createItemTag("luv_circuits");
    public static final TagKey<Item> ZPM_CIRCUITS = TagUtil.createItemTag("zpm_circuits");
    public static final TagKey<Item> UV_CIRCUITS = TagUtil.createItemTag("uv_circuits");
    public static final TagKey<Item> UHV_CIRCUITS = TagUtil.createItemTag("uhv_circuits");

    public static final TagKey<Item> ULV_BATTERIES = TagUtil.createItemTag("ulv_batteries");
    public static final TagKey<Item> LV_BATTERIES = TagUtil.createItemTag("lv_batteries");
    public static final TagKey<Item> MV_BATTERIES = TagUtil.createItemTag("mv_batteries");
    public static final TagKey<Item> HV_BATTERIES = TagUtil.createItemTag("hv_batteries");
    public static final TagKey<Item> EV_BATTERIES = TagUtil.createItemTag("ev_batteries");
    public static final TagKey<Item> IV_BATTERIES = TagUtil.createItemTag("iv_batteries");
    public static final TagKey<Item> LuV_BATTERIES = TagUtil.createItemTag("luv_batteries");
    public static final TagKey<Item> ZPM_BATTERIES = TagUtil.createItemTag("zpm_batteries");
    public static final TagKey<Item> UV_BATTERIES = TagUtil.createItemTag("uv_batteries");
    public static final TagKey<Item> UHV_BATTERIES = TagUtil.createItemTag("uhv_batteries");

    // Platform-dependent tags
    public static final TagKey<Item> TAG_WOODEN_CHESTS = getWoodChestTag();

    @ExpectPlatform
    public static TagKey<Item> getWoodChestTag() {
        throw new AssertionError();
    }
}
