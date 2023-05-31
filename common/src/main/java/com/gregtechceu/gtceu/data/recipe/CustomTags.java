package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;
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

    public static final TagKey<Item> ULV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/ulv", "ulv_circuits");
    public static final TagKey<Item> LV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/lv", "lv_circuits");
    public static final TagKey<Item> MV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/mv", "mv_circuits");
    public static final TagKey<Item> HV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/hv", "hv_circuits");
    public static final TagKey<Item> EV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/ev", "ev_circuits");
    public static final TagKey<Item> IV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/iv", "iv_circuits");
    public static final TagKey<Item> LuV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/luv", "luv_circuits");
    public static final TagKey<Item> ZPM_CIRCUITS = TagUtil.createPlatformItemTag("circuits/zpm", "zpm_circuits");
    public static final TagKey<Item> UV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/uv", "uv_circuits");
    public static final TagKey<Item> UHV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/uhv", "uhv_circuits");

    public static final TagKey<Item> ULV_BATTERIES = TagUtil.createPlatformItemTag("batteries/ulv", "ulv_batteries");
    public static final TagKey<Item> LV_BATTERIES = TagUtil.createPlatformItemTag("batteries/lv", "lv_batteries");
    public static final TagKey<Item> MV_BATTERIES = TagUtil.createPlatformItemTag("batteries/mv", "mv_batteries");
    public static final TagKey<Item> HV_BATTERIES = TagUtil.createPlatformItemTag("batteries/hv", "hv_batteries");
    public static final TagKey<Item> EV_BATTERIES = TagUtil.createPlatformItemTag("batteries/ev", "ev_batteries");
    public static final TagKey<Item> IV_BATTERIES = TagUtil.createPlatformItemTag("batteries/iv", "iv_batteries");
    public static final TagKey<Item> LuV_BATTERIES = TagUtil.createPlatformItemTag("batteries/luv", "luv_batteries");
    public static final TagKey<Item> ZPM_BATTERIES = TagUtil.createPlatformItemTag("batteries/zpm", "zpm_batteries");
    public static final TagKey<Item> UV_BATTERIES = TagUtil.createPlatformItemTag("batteries/uv", "uv_batteries");
    public static final TagKey<Item> UHV_BATTERIES = TagUtil.createPlatformItemTag("batteries/uhv", "uhv_batteries");

    // Platform-dependent tags
    public static final TagKey<Item> TAG_WOODEN_CHESTS = TagUtil.createPlatformItemTag("chests/wooden", "chests");
}
