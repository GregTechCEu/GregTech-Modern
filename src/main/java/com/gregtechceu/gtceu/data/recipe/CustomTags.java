package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
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
    public static final TagKey<Item> GLASS_BLOCKS = TagUtil.createItemTag("glass");
    public static final TagKey<Item> GLASS_PANES = TagUtil.createItemTag("glass_panes");
    public static final TagKey<Item> SEEDS = TagUtil.createItemTag("seeds");
    public static final TagKey<Item> CONCRETE_ITEM = TagUtil.createItemTag("concrete");
    public static final TagKey<Item> CONCRETE_POWDER_ITEM = TagUtil.createItemTag("concrete_powder");

    // Added Gregtech tags
    public static final TagKey<Item> TRANSISTORS = TagUtil.createModItemTag("transistors");
    public static final TagKey<Item> RESISTORS = TagUtil.createModItemTag("resistors");
    public static final TagKey<Item> CAPACITORS = TagUtil.createModItemTag("capacitors");
    public static final TagKey<Item> DIODES = TagUtil.createModItemTag("diodes");
    public static final TagKey<Item> INDUCTORS = TagUtil.createModItemTag("inductors");

    public static final TagKey<Item> ULV_CIRCUITS = TagUtil.createModItemTag("circuits/ulv");
    public static final TagKey<Item> LV_CIRCUITS = TagUtil.createModItemTag("circuits/lv");
    public static final TagKey<Item> MV_CIRCUITS = TagUtil.createModItemTag("circuits/mv");
    public static final TagKey<Item> HV_CIRCUITS = TagUtil.createModItemTag("circuits/hv");
    public static final TagKey<Item> EV_CIRCUITS = TagUtil.createModItemTag("circuits/ev");
    public static final TagKey<Item> IV_CIRCUITS = TagUtil.createModItemTag("circuits/iv");
    public static final TagKey<Item> LuV_CIRCUITS = TagUtil.createModItemTag("circuits/luv");
    public static final TagKey<Item> ZPM_CIRCUITS = TagUtil.createModItemTag("circuits/zpm");
    public static final TagKey<Item> UV_CIRCUITS = TagUtil.createModItemTag("circuits/uv");
    public static final TagKey<Item> UHV_CIRCUITS = TagUtil.createModItemTag("circuits/uhv");
    public static final TagKey<Item> UEV_CIRCUITS = TagUtil.createModItemTag("circuits/uev");
    public static final TagKey<Item> UIV_CIRCUITS = TagUtil.createModItemTag("circuits/uiv");
    public static final TagKey<Item> UXV_CIRCUITS = TagUtil.createModItemTag("circuits/uxv");
    public static final TagKey<Item> OpV_CIRCUITS = TagUtil.createModItemTag("circuits/opv");
    public static final TagKey<Item> MAX_CIRCUITS = TagUtil.createModItemTag("circuits/max");

    public static final TagKey<Item> ULV_BATTERIES = TagUtil.createModItemTag("batteries/ulv");
    public static final TagKey<Item> LV_BATTERIES = TagUtil.createModItemTag("batteries/lv");
    public static final TagKey<Item> MV_BATTERIES = TagUtil.createModItemTag("batteries/mv");
    public static final TagKey<Item> HV_BATTERIES = TagUtil.createModItemTag("batteries/hv");
    public static final TagKey<Item> EV_BATTERIES = TagUtil.createModItemTag("batteries/ev");
    public static final TagKey<Item> IV_BATTERIES = TagUtil.createModItemTag("batteries/iv");
    public static final TagKey<Item> LuV_BATTERIES = TagUtil.createModItemTag("batteries/luv");
    public static final TagKey<Item> ZPM_BATTERIES = TagUtil.createModItemTag("batteries/zpm");
    public static final TagKey<Item> UV_BATTERIES = TagUtil.createModItemTag("batteries/uv");
    public static final TagKey<Item> UHV_BATTERIES = TagUtil.createModItemTag("batteries/uhv");


    // Platform-dependent tags
    public static final TagKey<Item> TAG_WOODEN_CHESTS = TagUtil.createItemTag("chests/wooden");

    public static final TagKey<Block> NEEDS_WOOD_TOOL = TagUtil.createBlockTag("needs_wood_tool");
    public static final TagKey<Block> NEEDS_GOLD_TOOL = TagUtil.createBlockTag("needs_gold_tool");
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagUtil.createBlockTag("needs_netherite_tool");
    public static final TagKey<Block> NEEDS_DURANIUM_TOOL = TagUtil.createBlockTag("needs_duranium_tool");
    public static final TagKey<Block> NEEDS_NEUTRONIUM_TOOL = TagUtil.createBlockTag("needs_neutronium_tool");

    @SuppressWarnings("unchecked")
    public static final TagKey<Block>[] TOOL_TIERS = new TagKey[] {
            NEEDS_WOOD_TOOL,
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.NEEDS_DIAMOND_TOOL,
            NEEDS_NETHERITE_TOOL,
            NEEDS_DURANIUM_TOOL,
            NEEDS_NEUTRONIUM_TOOL,
    };

    public static final TagKey<Block> ENDSTONE_ORE_REPLACEABLES = TagUtil.createBlockTag("end_stone_ore_replaceables");
    public static final TagKey<Block> CONCRETE_BLOCK = TagUtil.createBlockTag("concrete");
    public static final TagKey<Block> CONCRETE_POWDER_BLOCK = TagUtil.createBlockTag("concrete_powder");
    public static final TagKey<Block> GLASS_BLOCKS_BLOCK = TagUtil.createBlockTag("glass");
    public static final TagKey<Block> GLASS_PANES_BLOCK = TagUtil.createBlockTag("glass_panes");
    public static final TagKey<Block> CREATE_SEATS = TagUtil.optionalTag(Registries.BLOCK, new ResourceLocation(GTValues.MODID_CREATE, "seats"));
    public static final TagKey<Block> ORE_BLOCKS = TagUtil.createBlockTag("ores");


    public static final TagKey<Biome> IS_SWAMP = TagUtil.createTag(Registries.BIOME, "is_swamp", false);
    public static final TagKey<Biome> IS_SANDY = TagUtil.createModTag(Registries.BIOME, "is_sandy");
    public static final TagKey<Biome> HAS_RUBBER_TREE = TagUtil.createModTag(Registries.BIOME, "has_rubber_tree");


    public static final TagKey<Fluid> STEAM = TagUtil.createFluidTag("steam");
}
