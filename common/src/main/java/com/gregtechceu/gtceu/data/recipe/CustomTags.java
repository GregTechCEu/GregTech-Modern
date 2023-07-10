package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class CustomTags {

    // Added Vanilla tags
    public static final TagKey<Item> TAG_PISTONS = TagUtil.createItemTag("pistons");
    public static final TagKey<Item> GLASS_PANES = TagUtil.createItemTag("glass_panes");

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
    public static final TagKey<Item> TAG_BLUE_DYES = TagUtil.createPlatformItemTag("dyes/blue", "blue_dyes");

    public static final TagKey<Block> NEEDS_WOOD_TOOL = TagUtil.createPlatformUnprefixedTag(Registry.BLOCK, "forge:needs_wood_tool", "fabric:needs_tool_level_0");
    public static final TagKey<Block> NEEDS_GOLD_TOOL = TagUtil.createPlatformUnprefixedTag(Registry.BLOCK, "forge:needs_gold_tool", "fabric:needs_tool_level_0");
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagUtil.createPlatformUnprefixedTag(Registry.BLOCK, "forge:needs_netherite_tool", "fabric:needs_tool_level_4");
    public static final TagKey<Block> NEEDS_NAQ_ALLOY_TOOL = TagUtil.createPlatformUnprefixedTag(Registry.BLOCK, "forge:needs_naquadah_alloy_tool", "fabric:needs_tool_level_5");
    public static final TagKey<Block> NEEDS_NEUTRONIUM_TOOL = TagUtil.createPlatformUnprefixedTag(Registry.BLOCK, "forge:needs_neutronium_tool", "fabric:needs_tool_level_6");

    public static final TagKey<Block> CONCRETE = TagUtil.createBlockTag("concrete");
    public static final TagKey<Block> CONCRETE_POWDER = TagUtil.createBlockTag("concrete_powder");
    public static final TagKey<Block> GLASS_BLOCKS = TagUtil.createPlatformBlockTag("glass", "glass_blocks", false);
    public static final TagKey<Block> GLASS_PANES_BLOCK = TagUtil.createBlockTag("glass_panes");
    public static final TagKey<Block> CREATE_SEATS = TagUtil.optionalTag(Registry.BLOCK, new ResourceLocation(GTValues.MODID_CREATE, "seats"));
    public static final TagKey<Block> ORE_BLOCKS = TagUtil.createBlockTag("ores");

    @SuppressWarnings("unchecked")
    public static final TagKey<Block>[] TOOL_TIERS = new TagKey[] {
            NEEDS_WOOD_TOOL,
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.NEEDS_DIAMOND_TOOL,
            NEEDS_NETHERITE_TOOL,
            NEEDS_NAQ_ALLOY_TOOL,
            NEEDS_NEUTRONIUM_TOOL
    };

    public static final TagKey<Biome> IS_SWAMP = TagUtil.createPlatformTag(BuiltinRegistries.BIOME, "is_swamp", "is_swamp", false);
    public static final TagKey<Biome> HAS_RUBBER_TREE = TagUtil.createModTag(BuiltinRegistries.BIOME, "has_rubber_tree");
}
