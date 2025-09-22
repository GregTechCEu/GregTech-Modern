package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.tag.TagUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class CustomTags {

    // Added Vanilla tags
    public static final TagKey<Item> PISTONS = TagUtil.createItemTag("pistons");
    public static final TagKey<Item> CONCRETE_ITEM = TagUtil.createItemTag("concretes");
    public static final TagKey<Item> CONCRETE_POWDER_ITEM = TagUtil.createItemTag("concrete_powders");
    // common/mod compat tags
    public static final TagKey<Item> DOUGHS = TagUtil.createItemTag("dough");
    public static final TagKey<Item> WHEAT_DOUGHS = TagUtil.createItemTag("dough/wheat");
    public static final TagKey<Item> GRAINS = TagUtil.createItemTag("grain");
    public static final TagKey<Item> WHEAT_GRAINS = TagUtil.createItemTag("grain/wheat");

    // Why did I put them all here? Why not? Science isn't about why, it's about why NOT!
    // Why is so much of our science dangerous? Why don't you marry safe science if you love it so much!?
    // IN FACT, WHY NOT INVENT A SAFETY DOOR THAT WON'T HIT YOU ON THE BUTT ON THE WAY OUT BECAUSE YOU'RE FIRED!!!
    // Not you test subject, you're doing fine.
    // YES, YOU. BOX. YOUR STUFF. OUT THE FRONT DOOR. PARKING LOT. CAR. GOODBYE!
    public static final TagKey<Item> ALIVE_CORAL_BLOCK_ITEMS = TagUtil.createItemTag("coral_blocks/alive");
    public static final TagKey<Item> DEAD_CORAL_BLOCK_ITEMS = TagUtil.createItemTag("coral_blocks/dead");
    public static final TagKey<Item> CORAL_BLOCK_ITEMS = TagUtil.createItemTag("coral_blocks");
    public static final TagKey<Item> ALIVE_CORAL_PLANT_ITEMS = TagUtil.createItemTag("coral_plants/alive");
    public static final TagKey<Item> DEAD_CORAL_PLANT_ITEMS = TagUtil.createItemTag("coral_plants/dead");
    public static final TagKey<Item> CORAL_PLANT_ITEMS = TagUtil.createItemTag("coral_plants");
    public static final TagKey<Item> ALIVE_CORAL_FAN_ITEMS = TagUtil.createItemTag("coral_fans/alive");
    public static final TagKey<Item> DEAD_CORAL_FAN_ITEMS = TagUtil.createItemTag("coral_fans/dead");
    public static final TagKey<Item> CORAL_FAN_ITEMS = TagUtil.createItemTag("coral_fans");
    public static final TagKey<Item> ALIVE_CORAL_ITEMS = TagUtil.createItemTag("corals/alive");
    public static final TagKey<Item> DEAD_CORAL_ITEMS = TagUtil.createItemTag("corals/dead");
    public static final TagKey<Item> CORAL_ITEMS = TagUtil.createItemTag("corals");

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

    public static final TagKey<Block> CLEANROOM_DOORS = TagUtil.createModBlockTag("cleanroom_doors");

    public static final TagKey<Item> CHEM_BATH_WASHABLE = TagUtil.createModItemTag("chemical_bath_washable");

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

    public static final TagKey<Block> NEEDS_WOOD_TOOL = TagUtil.createBlockTag("needs_wood_tool");
    public static final TagKey<Block> NEEDS_GOLD_TOOL = TagUtil.createBlockTag("needs_gold_tool");
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagUtil.createBlockTag("needs_netherite_tool");
    public static final TagKey<Block> NEEDS_DURANIUM_TOOL = TagUtil.createBlockTag("needs_duranium_tool");
    public static final TagKey<Block> NEEDS_NEUTRONIUM_TOOL = TagUtil.createBlockTag("needs_neutronium_tool");

    // Tool tags
    public static final TagKey<Item> TOOLS_IGNITER = TagUtil.createItemTag("tools/igniter");

    public static final TagKey<Item> BUTCHERY_KNIVES = TagUtil.createItemTag("tools/butchery_knives");
    public static final TagKey<Item> BUZZSAWS = TagUtil.createItemTag("tools/buzzsaws");
    public static final TagKey<Item> CHAINSAWS = TagUtil.createItemTag("tools/chainsaws");
    public static final TagKey<Item> CROWBARS = TagUtil.createItemTag("tools/crowbars");
    public static final TagKey<Item> DRILLS = TagUtil.createItemTag("tools/drills");
    public static final TagKey<Item> FILES = TagUtil.createItemTag("tools/files");
    public static final TagKey<Item> HAMMERS = TagUtil.createItemTag("tools/hammers");
    public static final TagKey<Item> KNIVES = TagUtil.createItemTag("tools/knives");
    public static final TagKey<Item> MALLETS = TagUtil.createItemTag("tools/mallets");
    public static final TagKey<Item> MINING_HAMMERS = TagUtil.createItemTag("tools/mining_hammers");
    public static final TagKey<Item> MORTARS = TagUtil.createItemTag("tools/mortars");
    public static final TagKey<Item> PLUNGERS = TagUtil.createItemTag("tools/plungers");
    public static final TagKey<Item> SAWS = TagUtil.createItemTag("tools/saws");
    public static final TagKey<Item> SCREWDRIVERS = TagUtil.createItemTag("tools/screwdrivers");
    public static final TagKey<Item> SCYTHES = TagUtil.createItemTag("tools/scythes");
    public static final TagKey<Item> SHEARS = TagUtil.createItemTag("tools/shears");
    public static final TagKey<Item> SPADES = TagUtil.createItemTag("tools/spades");
    public static final TagKey<Item> WIRE_CUTTERS = TagUtil.createItemTag("tools/wire_cutters");
    public static final TagKey<Item> WRENCHES = TagUtil.createItemTag("tools/wrenches");
    public static final TagKey<Item> WRENCH = TagUtil.createItemTag("tools/wrench"); // Compat with other mods

    // Tool Crafting Tags
    public static final TagKey<Item> CRAFTING_CROWBARS = TagUtil.createModItemTag("tools/crafting_crowbars");
    public static final TagKey<Item> CRAFTING_FILES = TagUtil.createModItemTag("tools/crafting_files");
    public static final TagKey<Item> CRAFTING_HAMMERS = TagUtil.createModItemTag("tools/crafting_hammers");
    public static final TagKey<Item> CRAFTING_KNIVES = TagUtil.createModItemTag("tools/crafting_knives");
    public static final TagKey<Item> CRAFTING_MALLETS = TagUtil.createModItemTag("tools/crafting_mallets");
    public static final TagKey<Item> CRAFTING_MORTARS = TagUtil.createModItemTag("tools/crafting_mortars");
    public static final TagKey<Item> CRAFTING_SAWS = TagUtil.createModItemTag("tools/crafting_saws");
    public static final TagKey<Item> CRAFTING_SCREWDRIVERS = TagUtil.createModItemTag("tools/crafting_screwdrivers");
    public static final TagKey<Item> CRAFTING_WIRE_CUTTERS = TagUtil.createModItemTag("tools/crafting_wire_cutters");
    public static final TagKey<Item> CRAFTING_WRENCHES = TagUtil.createModItemTag("tools/crafting_wrenches");

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
    public static final TagKey<Block> CONCRETE_BLOCK = TagUtil.createBlockTag("concretes");
    public static final TagKey<Block> CONCRETE_POWDER_BLOCK = TagUtil.createBlockTag("concrete_powders");
    public static final TagKey<Block> CLEANROOM_FLOORS = TagUtil.createModBlockTag("cleanroom_floors");
    public static final TagKey<Block> CHARCOAL_PILE_IGNITER_WALLS = TagUtil.createModBlockTag(
            "charcoal_pile_igniter_walls");
    public static final TagKey<Block> CREATE_WRENCH_PICKUP = TagUtil.optionalTag(BuiltInRegistries.BLOCK,
            new ResourceLocation("create", "wrench_pickup"));

    public static final TagKey<Block> TALL_PLANTS = TagUtil.createModBlockTag("tall_plants");

    public static final TagKey<Biome> IS_SWAMP = TagUtil.createTag(Registries.BIOME, "is_swamp", false);
    public static final TagKey<Biome> IS_SANDY = TagUtil.createModTag(Registries.BIOME, "is_sandy");
    public static final TagKey<Biome> HAS_RUBBER_TREE = TagUtil.createModTag(Registries.BIOME, "has_rubber_tree");

    public static final TagKey<EntityType<?>> HEAT_IMMUNE = TagUtil.createModTag(Registries.ENTITY_TYPE, "heat_immune");
    public static final TagKey<EntityType<?>> CHEMICAL_IMMUNE = TagUtil.createModTag(Registries.ENTITY_TYPE,
            "chemical_immune");

    public static final TagKey<Fluid> LIGHTER_FLUIDS = TagUtil.createModFluidTag("lighter_fluids");
    public static final TagKey<Fluid> POTION_FLUIDS = TagUtil.createFluidTag("potion");

    public static final TagKey<Fluid> MOLTEN_FLUIDS = TagUtil.createFluidTag("molten");
    public static final TagKey<Fluid> LIQUID_FLUIDS = TagUtil.createFluidTag("liquid");
    public static final TagKey<Fluid> PLASMA_FLUIDS = TagUtil.createFluidTag("plasmatic");

    public static final TagKey<Fluid> HPCA_COOLANTS = TagUtil.createModFluidTag("hpca_coolants");
}
