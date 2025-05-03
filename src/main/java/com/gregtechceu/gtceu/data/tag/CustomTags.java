package com.gregtechceu.gtceu.data.tag;

import com.gregtechceu.gtceu.api.tag.TagUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

public class CustomTags {

    // Added Vanilla tags
    public static final TagKey<Item> PISTONS = TagUtil.createItemTag("pistons");
    public static final TagKey<Item> DOUGHS = TagUtil.createItemTag("doughs");

    // Added Gregtech tags
    public static final TagKey<Item> TRANSISTORS = TagUtil.createModItemTag("transistors");
    public static final TagKey<Item> RESISTORS = TagUtil.createModItemTag("resistors");
    public static final TagKey<Item> CAPACITORS = TagUtil.createModItemTag("capacitors");
    public static final TagKey<Item> DIODES = TagUtil.createModItemTag("diodes");
    public static final TagKey<Item> INDUCTORS = TagUtil.createModItemTag("inductors");

    public static final TagKey<Item> CIRCUITS = TagUtil.createModItemTag("circuits");
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

    @SuppressWarnings("unchecked")
    public static final TagKey<Item>[] CIRCUITS_ARRAY = new TagKey[] {
            ULV_CIRCUITS,
            LV_CIRCUITS,
            MV_CIRCUITS,
            HV_CIRCUITS,
            EV_CIRCUITS,
            IV_CIRCUITS,
            LuV_CIRCUITS,
            ZPM_CIRCUITS,
            UV_CIRCUITS,
            UHV_CIRCUITS,
            UEV_CIRCUITS,
            UIV_CIRCUITS,
            UXV_CIRCUITS,
            OpV_CIRCUITS,
            MAX_CIRCUITS
    };

    public static final TagKey<Item> BATTERIES = TagUtil.createModItemTag("batteries");
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

    @SuppressWarnings("unchecked")
    public static final TagKey<Item>[] BATTERIES_ARRAY = new TagKey[] {
            ULV_BATTERIES,
            LV_BATTERIES,
            MV_BATTERIES,
            HV_BATTERIES,
            EV_BATTERIES,
            IV_BATTERIES,
            LuV_BATTERIES,
            ZPM_BATTERIES,
            UV_BATTERIES,
            UHV_BATTERIES
    };

    public static final TagKey<Item> ELECTRIC_MOTORS = TagUtil.createModItemTag("electric_motors");
    public static final TagKey<Item> ELECTRIC_PUMPS = TagUtil.createModItemTag("electric_pumps");
    public static final TagKey<Item> FLUID_REGULATORS = TagUtil.createModItemTag("fluid_regulators");
    public static final TagKey<Item> CONVEYOR_MODULES = TagUtil.createModItemTag("conveyor_modules");
    public static final TagKey<Item> ELECTRIC_PISTONS = TagUtil.createModItemTag("electric_pistons");
    public static final TagKey<Item> ROBOT_ARMS = TagUtil.createModItemTag("robot_arms");
    public static final TagKey<Item> FIELD_GENERATORS = TagUtil.createModItemTag("field_generators");
    public static final TagKey<Item> EMITTERS = TagUtil.createModItemTag("emitters");
    public static final TagKey<Item> SENSORS = TagUtil.createModItemTag("sensors");

    public static final TagKey<Item> PPE_ARMOR = TagUtil.createModItemTag("ppe_armor");
    public static final TagKey<Item> STEP_BOOTS = TagUtil.createModItemTag("step_boots");
    public static final TagKey<Item> RUBBER_LOGS = TagUtil.createModItemTag("rubber_logs");

    public static final TagKey<Item> BRICKS_FIREBRICK = TagUtil.createItemTag("bricks/firebrick");

    // Mineability tags
    public static final TagKey<Block> MINEABLE_WITH_WRENCH = TagUtil.createBlockTag("mineable/wrench");
    public static final TagKey<Block> MINEABLE_WITH_WIRE_CUTTER = TagUtil.createBlockTag("mineable/wire_cutter");
    public static final TagKey<Block> MINEABLE_WITH_SAW = TagUtil.createBlockTag("mineable/saw");
    public static final TagKey<Block> MINEABLE_WITH_HAMMER = TagUtil.createBlockTag("mineable/hammer");
    public static final TagKey<Block> MINEABLE_WITH_CROWBAR = TagUtil.createBlockTag("mineable/crowbar");
    public static final TagKey<Block> MINEABLE_WITH_KNIFE = TagUtil.createBlockTag("mineable/knife");
    public static final TagKey<Block> MINEABLE_WITH_SHEARS = TagUtil.createBlockTag("mineable/shears");
    public static final TagKey<Block> MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH = TagUtil
            .createModBlockTag("mineable/pickaxe_or_wrench");
    public static final TagKey<Block> MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER = TagUtil
            .createModBlockTag("mineable/pickaxe_or_wire_cutter");

    public static final TagKey<Block> NEEDS_DURANIUM_TOOL = TagUtil.createModBlockTag("needs_duranium_tool");
    public static final TagKey<Block> NEEDS_NEUTRONIUM_TOOL = TagUtil.createModBlockTag("needs_neutronium_tool");
    public static final TagKey<Block> INCORRECT_FOR_DURANIUM_TOOL = TagUtil
            .createModBlockTag("incorrect_for_duranium_tool");
    public static final TagKey<Block> INCORRECT_FOR_NEUTRONIUM_TOOL = TagUtil
            .createModBlockTag("incorrect_for_neutronium_tool");

    // Tool tags
    public static final TagKey<Item> TOOLS_BUTCHERY_KNIFE = TagUtil.createItemTag("tools/butchery_knife");
    public static final TagKey<Item> TOOLS_BUZZSAW = TagUtil.createItemTag("tools/buzzsaw");
    public static final TagKey<Item> TOOLS_CHAINSAW = TagUtil.createItemTag("tools/chainsaw");
    public static final TagKey<Item> TOOLS_CROWBAR = TagUtil.createItemTag("tools/crowbar");
    public static final TagKey<Item> TOOLS_DRILL = TagUtil.createItemTag("tools/drill");
    public static final TagKey<Item> TOOLS_FILE = TagUtil.createItemTag("tools/file");
    public static final TagKey<Item> TOOLS_HAMMER = TagUtil.createItemTag("tools/hammer");
    public static final TagKey<Item> TOOLS_KNIFE = TagUtil.createItemTag("tools/knife");
    public static final TagKey<Item> TOOLS_MALLET = TagUtil.createItemTag("tools/mallet");
    public static final TagKey<Item> TOOLS_MINING_HAMMER = TagUtil.createItemTag("tools/mining_hammer");
    public static final TagKey<Item> TOOLS_MORTAR = TagUtil.createItemTag("tools/mortar");
    public static final TagKey<Item> TOOLS_PLUNGER = TagUtil.createItemTag("tools/plunger");
    public static final TagKey<Item> TOOLS_SAW = TagUtil.createItemTag("tools/saw");
    public static final TagKey<Item> TOOLS_SCREWDRIVER = TagUtil.createItemTag("tools/screwdriver");
    public static final TagKey<Item> TOOLS_SCYTHE = TagUtil.createItemTag("tools/scythe");
    public static final TagKey<Item> TOOLS_SHEAR = Tags.Items.TOOLS_SHEAR;
    public static final TagKey<Item> TOOLS_SPADE = TagUtil.createItemTag("tools/spade");
    public static final TagKey<Item> TOOLS_WIRE_CUTTER = TagUtil.createItemTag("tools/wire_cutter");
    public static final TagKey<Item> TOOLS_WRENCH = Tags.Items.TOOLS_WRENCH;

    // Crafting Tool Tags
    public static final TagKey<Item> CRAFTING_CROWBARS = TagUtil.createModItemTag("crafting_tools/crowbar");
    public static final TagKey<Item> CRAFTING_FILES = TagUtil.createModItemTag("crafting_tools/file");
    public static final TagKey<Item> CRAFTING_HAMMERS = TagUtil.createModItemTag("crafting_tools/hammer");
    public static final TagKey<Item> CRAFTING_KNIVES = TagUtil.createModItemTag("crafting_tools/knife");
    public static final TagKey<Item> CRAFTING_MALLETS = TagUtil.createModItemTag("crafting_tools/mallet");
    public static final TagKey<Item> CRAFTING_MORTARS = TagUtil.createModItemTag("crafting_tools/mortar");
    public static final TagKey<Item> CRAFTING_SAWS = TagUtil.createModItemTag("crafting_tools/saw");
    public static final TagKey<Item> CRAFTING_SCREWDRIVERS = TagUtil.createModItemTag("crafting_tools/screwdriver");
    public static final TagKey<Item> CRAFTING_WIRE_CUTTERS = TagUtil.createModItemTag("crafting_tools/wire_cutter");
    public static final TagKey<Item> CRAFTING_WRENCHES = TagUtil.createModItemTag("crafting_tools/wrench");

    @SuppressWarnings("unchecked")
    public static final TagKey<Block>[] TOOL_TIERS = new TagKey[] {
            Tags.Blocks.NEEDS_WOOD_TOOL,
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.NEEDS_DIAMOND_TOOL,
            Tags.Blocks.NEEDS_NETHERITE_TOOL,
            NEEDS_DURANIUM_TOOL,
            NEEDS_NEUTRONIUM_TOOL,
    };

    @SuppressWarnings("unchecked")
    public static final TagKey<Block>[] INCORRECT_TOOL_TIERS = new TagKey[] {
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            BlockTags.INCORRECT_FOR_IRON_TOOL,
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            INCORRECT_FOR_DURANIUM_TOOL,
            INCORRECT_FOR_NEUTRONIUM_TOOL,
    };

    public static final TagKey<Block> ENDSTONE_ORE_REPLACEABLES = TagUtil.createBlockTag("end_stone_ore_replaceables");
    public static final TagKey<Block> CLEANROOM_FLOORS = TagUtil.createModBlockTag("cleanroom_floors");

    public static final TagKey<Biome> HAS_RUBBER_TREE = TagUtil.createModTag(Registries.BIOME, "has_rubber_tree");

    public static final TagKey<EntityType<?>> HEAT_IMMUNE = TagUtil.createModTag(Registries.ENTITY_TYPE, "heat_immune");
    public static final TagKey<EntityType<?>> CHEMICAL_IMMUNE = TagUtil.createModTag(Registries.ENTITY_TYPE,
            "chemical_immune");
    public static final TagKey<EntityType<?>> IRON_GOLEMS = TagUtil.createTag(Registries.ENTITY_TYPE, "iron_golems",
            false);
    public static final TagKey<EntityType<?>> SPIDERS = TagUtil.createTag(Registries.ENTITY_TYPE, "spiders", false);

    public static final TagKey<Fluid> LIGHTER_FLUIDS = TagUtil.createFluidTag("lighter_fluids");
    public static final TagKey<Fluid> MOLTEN_FLUIDS = TagUtil.createFluidTag("molten");
    public static final TagKey<Fluid> POTION_FLUIDS = TagUtil.createFluidTag("potion");
    public static final TagKey<Fluid> LIQUID_FLUIDS = TagUtil.createFluidTag("liquid");
    public static final TagKey<Fluid> PLASMA_FLUIDS = TagUtil.createFluidTag("plasmatic");
}
