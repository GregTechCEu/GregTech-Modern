package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class CustomTags {

    // Added Vanilla tags
    public static final TagKey<Item> TAG_PISTONS = TagUtil.createItemTag("pistons");
    public static final TagKey<Item> GLASS_BLOCKS = TagUtil.createPlatformItemTag("glass", "glass_blocks");
    public static final TagKey<Item> GLASS_PANES = TagUtil.createItemTag("glass_panes");
    public static final TagKey<Item> SEEDS = TagUtil.createItemTag("seeds");

    // Added Gregtech tags
    public static final TagKey<Item> TRANSISTORS = TagUtil.createModItemTag("transistors");
    public static final TagKey<Item> RESISTORS = TagUtil.createModItemTag("resistors");
    public static final TagKey<Item> CAPACITORS = TagUtil.createModItemTag("capacitors");
    public static final TagKey<Item> DIODES = TagUtil.createModItemTag("diodes");
    public static final TagKey<Item> INDUCTORS = TagUtil.createModItemTag("inductors");

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
    public static final TagKey<Item> UEV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/uev", "uev_circuits");
    public static final TagKey<Item> UIV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/uiv", "uiv_circuits");
    public static final TagKey<Item> UXV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/uxv", "uxv_circuits");
    public static final TagKey<Item> OpV_CIRCUITS = TagUtil.createPlatformItemTag("circuits/opv", "opv_circuits");
    public static final TagKey<Item> MAX_CIRCUITS = TagUtil.createPlatformItemTag("circuits/max", "max_circuits");

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

    public static final TagKey<Item> AOE_TOOLS = TagUtil.createPlatformItemTag("tools/aoe", "aoe_tools");
    public static final TagKey<Item> TREE_FELLING_TOOLS = TagUtil.createPlatformItemTag("tools/tree_felling", "tree_felling_tools");

    // Platform-dependent tags
    public static final TagKey<Item> TAG_WOODEN_CHESTS = TagUtil.createPlatformItemTag("chests/wooden", "chests");

    public static final TagKey<Block> NEEDS_WOOD_TOOL = TagUtil.createPlatformUnprefixedTag(BuiltInRegistries.BLOCK, "forge:needs_wood_tool", "fabric:needs_tool_level_0");
    public static final TagKey<Block> NEEDS_GOLD_TOOL = TagUtil.createPlatformUnprefixedTag(BuiltInRegistries.BLOCK, "forge:needs_gold_tool", "fabric:needs_tool_level_0");
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagUtil.createPlatformUnprefixedTag(BuiltInRegistries.BLOCK, "forge:needs_netherite_tool", "fabric:needs_tool_level_4");
    public static final TagKey<Block> NEEDS_NAQ_ALLOY_TOOL = TagUtil.createPlatformUnprefixedTag(BuiltInRegistries.BLOCK, "forge:needs_naquadah_alloy_tool", "fabric:needs_tool_level_5");
    public static final TagKey<Block> NEEDS_NEUTRONIUM_TOOL = TagUtil.createPlatformUnprefixedTag(BuiltInRegistries.BLOCK, "forge:needs_neutronium_tool", "fabric:needs_tool_level_6");

    @SuppressWarnings("unchecked")
    public static final TagKey<Block>[] TOOL_TIERS = new TagKey[] {
            NEEDS_WOOD_TOOL,
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.NEEDS_DIAMOND_TOOL,
            NEEDS_NETHERITE_TOOL,
            NEEDS_NAQ_ALLOY_TOOL,
            NEEDS_NEUTRONIUM_TOOL,
    };

    public static final TagKey<Block> ENDSTONE_ORE_REPLACEABLES = TagUtil.createBlockTag("end_stone_ore_replaceables");
    public static final TagKey<Block> CONCRETE = TagUtil.createBlockTag("concrete");
    public static final TagKey<Block> CONCRETE_POWDER = TagUtil.createBlockTag("concrete_powder");
    public static final TagKey<Block> GLASS_BLOCKS_BLOCK = TagUtil.createPlatformBlockTag("glass", "glass_blocks", false);
    public static final TagKey<Block> GLASS_PANES_BLOCK = TagUtil.createBlockTag("glass_panes");
    public static final TagKey<Block> CREATE_SEATS = TagUtil.optionalTag(BuiltInRegistries.BLOCK, new ResourceLocation(GTValues.MODID_CREATE, "seats"));
    public static final TagKey<Block> ORE_BLOCKS = TagUtil.createBlockTag("ores");


    public static final TagKey<Biome> IS_SWAMP = TagUtil.createTag(Registries.BIOME, "is_swamp", false);
    public static final TagKey<Biome> IS_SANDY = TagUtil.createModTag(Registries.BIOME, "is_sandy");
    public static final TagKey<Biome> HAS_RUBBER_TREE = TagUtil.createModTag(Registries.BIOME, "has_rubber_tree");


    public static final TagKey<Fluid> STEAM = TagUtil.createFluidTag("steam");
}
