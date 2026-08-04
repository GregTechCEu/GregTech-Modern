package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.GTValues;
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
import net.minecraftforge.common.Tags;

import lombok.experimental.UtilityClass;

@SuppressWarnings("deprecation")
@UtilityClass
public class GTTags {
    // spotless:off

    // region blocks
    @UtilityClass
    public static class Blocks {

        // region misc
        public static final TagKey<Block> RUBBER_LOGS = TagUtil.createModBlockTag("logs/rubber");
        public static final TagKey<Block> CONCRETES = TagUtil.createBlockTag("concretes");
        public static final TagKey<Block> CONCRETE_POWDERS = TagUtil.createBlockTag("concrete_powders");
        public static final TagKey<Block> TALL_PLANTS = TagUtil.createModBlockTag("tall_plants");
        // endregion misc

        // region block groups
        public static final TagKey<Block> SLOW_WALKABLE_BLOCKS = TagUtil.createBlockTag("slow_walkable_blocks");
        public static final TagKey<Block> FAST_WALKABLE_BLOCKS = TagUtil.createBlockTag("fast_walkable_blocks");
        public static final TagKey<Block> VERY_FAST_WALKABLE_BLOCKS = TagUtil.createBlockTag("very_fast_walkable_blocks");

        public static final TagKey<Block> CLEANROOM_DOORS = TagUtil.createModBlockTag("cleanroom_doors");
        public static final TagKey<Block> CLEANROOM_FLOORS = TagUtil.createModBlockTag("cleanroom_floors");
        public static final TagKey<Block> CHARCOAL_PILE_IGNITER_WALLS = TagUtil.createModBlockTag("charcoal_pile_igniter_walls");
        // endregion block groups

        // region Mineability tags
        public static final TagKey<Block> MINEABLE_WITH_WRENCH = TagUtil.createBlockTag("mineable/wrench");
        public static final TagKey<Block> MINEABLE_WITH_WIRE_CUTTER = TagUtil.createBlockTag("mineable/wire_cutter");
        public static final TagKey<Block> MINEABLE_WITH_SAW = TagUtil.createBlockTag("mineable/saw");
        public static final TagKey<Block> MINEABLE_WITH_HAMMER = TagUtil.createBlockTag("mineable/hammer");
        public static final TagKey<Block> MINEABLE_WITH_CROWBAR = TagUtil.createBlockTag("mineable/crowbar");
        public static final TagKey<Block> MINEABLE_WITH_KNIFE = TagUtil.createBlockTag("mineable/knife");
        public static final TagKey<Block> MINEABLE_WITH_SHEARS = TagUtil.createBlockTag("mineable/shears");
        public static final TagKey<Block> MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH = TagUtil.createModBlockTag("mineable/pickaxe_or_wrench");
        public static final TagKey<Block> MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER = TagUtil.createModBlockTag("mineable/pickaxe_or_wire_cutter");

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
        // endregion Mineability tags

        public static final TagKey<Block> ENDSTONE_ORE_REPLACEABLES = TagUtil.createBlockTag("end_stone_ore_replaceables");

        public static final TagKey<Block> CREATE_WRENCH_PICKUP = TagUtil.optionalTag(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(GTValues.MODID_CREATE, "wrench_pickup"));
    }
    // endregion blocks

    // region entity types
    @UtilityClass
    public static class EntityTypes {

        public static final TagKey<EntityType<?>> HEAT_IMMUNE = TagUtil.createModTag(Registries.ENTITY_TYPE, "heat_immune");
        public static final TagKey<EntityType<?>> CHEMICAL_IMMUNE = TagUtil.createModTag(Registries.ENTITY_TYPE, "chemical_immune");
    }
    // endregion entity types

    // region items
    @UtilityClass
    public static class Items {
        // region mirrors of block tags
        public static final TagKey<Item> RUBBER_LOGS = TagUtil.createModItemTag("rubber_logs");
        public static final TagKey<Item> CONCRETES = TagUtil.createItemTag("concretes");
        public static final TagKey<Item> CONCRETE_POWDERS = TagUtil.createItemTag("concrete_powders");
        // endregion mirrors of block tags

        // region misc
        public static final TagKey<Item> PISTONS = TagUtil.createItemTag("pistons");

        public static final TagKey<Item> PPE_ARMOR = TagUtil.createModItemTag("ppe_armor");
        public static final TagKey<Item> STEP_BOOTS = TagUtil.createModItemTag("step_boots");

        public static final TagKey<Item> CHEM_BATH_WASHABLE = TagUtil.createModItemTag("chemical_bath_washable");
        public static final TagKey<Item> SKIP_ITEM_DETECTOR = TagUtil.createModItemTag("skip_item_detector");
        // endregion misc

        // region common/mod compat tags
        public static final TagKey<Item> DOUGHS = TagUtil.createItemTag("dough");
        public static final TagKey<Item> DOUGHS_WHEAT = TagUtil.createItemTag("dough/wheat");
        public static final TagKey<Item> GRAINS = TagUtil.createItemTag("grain");
        public static final TagKey<Item> GRAINS_WHEAT = TagUtil.createItemTag("grain/wheat");
        public static final TagKey<Item> WRENCH = TagUtil.createItemTag("tools/wrench");
        // endregion common/mod compat tags

        // region corals

        // Why did I put them all here? Why not? Science isn't about why, it's about why NOT!
        // Why is so much of our science dangerous? Why don't you marry safe science if you love it so much!?
        // IN FACT, WHY NOT INVENT A SAFETY DOOR THAT WON'T HIT YOU ON THE BUTT ON THE WAY OUT BECAUSE YOU'RE FIRED!!!
        // Not you test subject, you're doing fine.
        // YES, YOU. BOX. YOUR STUFF. OUT THE FRONT DOOR. PARKING LOT. CAR. GOODBYE!
        public static final TagKey<Item> CORAL_BLOCKS_ALIVE = TagUtil.createItemTag("coral_blocks/alive");
        public static final TagKey<Item> CORAL_BLOCKS_DEAD = TagUtil.createItemTag("coral_blocks/dead");
        public static final TagKey<Item> CORAL_BLOCKS = TagUtil.createItemTag("coral_blocks");
        public static final TagKey<Item> CORAL_PLANTS_ALIVE = TagUtil.createItemTag("coral_plants/alive");
        public static final TagKey<Item> CORAL_PLANTS_DEAD = TagUtil.createItemTag("coral_plants/dead");
        public static final TagKey<Item> CORAL_PLANTS = TagUtil.createItemTag("coral_plants");
        public static final TagKey<Item> CORAL_FANS_ALIVE = TagUtil.createItemTag("coral_fans/alive");
        public static final TagKey<Item> CORAL_FANS_DEAD = TagUtil.createItemTag("coral_fans/dead");
        public static final TagKey<Item> CORAL_FANS = TagUtil.createItemTag("coral_fans");
        public static final TagKey<Item> CORALS_ALIVE = TagUtil.createItemTag("corals/alive");
        public static final TagKey<Item> CORALS_DEAD = TagUtil.createItemTag("corals/dead");
        public static final TagKey<Item> CORALS = TagUtil.createItemTag("corals");
        // endregion corals

        // region GT 'parts'
        public static final TagKey<Item> TRANSISTORS = TagUtil.createModItemTag("transistors");
        public static final TagKey<Item> RESISTORS = TagUtil.createModItemTag("resistors");
        public static final TagKey<Item> CAPACITORS = TagUtil.createModItemTag("capacitors");
        public static final TagKey<Item> DIODES = TagUtil.createModItemTag("diodes");
        public static final TagKey<Item> INDUCTORS = TagUtil.createModItemTag("inductors");

        public static final TagKey<Item> CIRCUITS = TagUtil.createModItemTag("circuits");

        public static final TagKey<Item> CIRCUITS_ULV = TagUtil.createModItemTag("circuits/ulv");
        public static final TagKey<Item> CIRCUITS_LV = TagUtil.createModItemTag("circuits/lv");
        public static final TagKey<Item> CIRCUITS_MV = TagUtil.createModItemTag("circuits/mv");
        public static final TagKey<Item> CIRCUITS_HV = TagUtil.createModItemTag("circuits/hv");
        public static final TagKey<Item> CIRCUITS_EV = TagUtil.createModItemTag("circuits/ev");
        public static final TagKey<Item> CIRCUITS_IV = TagUtil.createModItemTag("circuits/iv");
        public static final TagKey<Item> CIRCUITS_LuV = TagUtil.createModItemTag("circuits/luv");
        public static final TagKey<Item> CIRCUITS_ZPM = TagUtil.createModItemTag("circuits/zpm");
        public static final TagKey<Item> CIRCUITS_UV = TagUtil.createModItemTag("circuits/uv");
        public static final TagKey<Item> CIRCUITS_UHV = TagUtil.createModItemTag("circuits/uhv");
        public static final TagKey<Item> CIRCUITS_UEV = TagUtil.createModItemTag("circuits/uev");
        public static final TagKey<Item> CIRCUITS_UIV = TagUtil.createModItemTag("circuits/uiv");
        public static final TagKey<Item> CIRCUITS_UXV = TagUtil.createModItemTag("circuits/uxv");
        public static final TagKey<Item> CIRCUITS_OpV = TagUtil.createModItemTag("circuits/opv");
        public static final TagKey<Item> CIRCUITS_MAX = TagUtil.createModItemTag("circuits/max");
        @SuppressWarnings("unchecked")
        public static final TagKey<Item>[] CIRCUITS_ARRAY = new TagKey[] {
                CIRCUITS_ULV,
                CIRCUITS_LV,
                CIRCUITS_MV,
                CIRCUITS_HV,
                CIRCUITS_EV,
                CIRCUITS_IV,
                CIRCUITS_LuV,
                CIRCUITS_ZPM,
                CIRCUITS_UV,
                CIRCUITS_UHV,
                CIRCUITS_UEV,
                CIRCUITS_UIV,
                CIRCUITS_UXV,
                CIRCUITS_OpV,
                CIRCUITS_MAX
        };

        public static final TagKey<Item> BATTERIES = TagUtil.createModItemTag("batteries");

        public static final TagKey<Item> BATTERIES_ULV = TagUtil.createModItemTag("batteries/ulv");
        public static final TagKey<Item> BATTERIES_LV = TagUtil.createModItemTag("batteries/lv");
        public static final TagKey<Item> BATTERIES_MV = TagUtil.createModItemTag("batteries/mv");
        public static final TagKey<Item> BATTERIES_HV = TagUtil.createModItemTag("batteries/hv");
        public static final TagKey<Item> BATTERIES_EV = TagUtil.createModItemTag("batteries/ev");
        public static final TagKey<Item> BATTERIES_IV = TagUtil.createModItemTag("batteries/iv");
        public static final TagKey<Item> BATTERIES_LuV = TagUtil.createModItemTag("batteries/luv");
        public static final TagKey<Item> BATTERIES_ZPM = TagUtil.createModItemTag("batteries/zpm");
        public static final TagKey<Item> BATTERIES_UV = TagUtil.createModItemTag("batteries/uv");
        public static final TagKey<Item> BATTERIES_UHV = TagUtil.createModItemTag("batteries/uhv");

        @SuppressWarnings("unchecked")
        public static final TagKey<Item>[] BATTERIES_ARRAY = new TagKey[] {
                BATTERIES_ULV,
                BATTERIES_LV,
                BATTERIES_MV,
                BATTERIES_HV,
                BATTERIES_EV,
                BATTERIES_IV,
                BATTERIES_LuV,
                BATTERIES_ZPM,
                BATTERIES_UV,
                BATTERIES_UHV
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
        // endregion GT 'parts'

        // region Tool tags
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

        // region Tool Crafting Tags
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
        // endregion Tool Crafting Tags
        // endregion Tool tags

        // region lenses
        public static final TagKey<Item> LENSES = TagUtil.createModItemTag("lenses");
        public static final TagKey<Item> LENSES_GLASS = TagUtil.createModItemTag("lenses/glass");

        public static final TagKey<Item> LENSES_WHITE = TagUtil.createModItemTag("lenses/white");
        public static final TagKey<Item> LENSES_ORANGE = TagUtil.createModItemTag("lenses/orange");
        public static final TagKey<Item> LENSES_MAGENTA = TagUtil.createModItemTag("lenses/magenta");
        public static final TagKey<Item> LENSES_LIGHT_BLUE = TagUtil.createModItemTag("lenses/light_blue");
        public static final TagKey<Item> LENSES_YELLOW = TagUtil.createModItemTag("lenses/yellow");
        public static final TagKey<Item> LENSES_LIME = TagUtil.createModItemTag("lenses/lime");
        public static final TagKey<Item> LENSES_PINK = TagUtil.createModItemTag("lenses/pink");
        public static final TagKey<Item> LENSES_GRAY = TagUtil.createModItemTag("lenses/gray");
        public static final TagKey<Item> LENSES_LIGHT_GRAY = TagUtil.createModItemTag("lenses/light_gray");
        public static final TagKey<Item> LENSES_CYAN = TagUtil.createModItemTag("lenses/cyan");
        public static final TagKey<Item> LENSES_PURPLE = TagUtil.createModItemTag("lenses/purple");
        public static final TagKey<Item> LENSES_BLUE = TagUtil.createModItemTag("lenses/blue");
        public static final TagKey<Item> LENSES_BROWN = TagUtil.createModItemTag("lenses/brown");
        public static final TagKey<Item> LENSES_GREEN = TagUtil.createModItemTag("lenses/green");
        public static final TagKey<Item> LENSES_RED = TagUtil.createModItemTag("lenses/red");
        public static final TagKey<Item> LENSES_BLACK = TagUtil.createModItemTag("lenses/black");

        @SuppressWarnings("unchecked")
        public static final TagKey<Item>[] LENSES_ARRAY = new TagKey[] {
                LENSES_WHITE,
                LENSES_ORANGE,
                LENSES_MAGENTA,
                LENSES_LIGHT_BLUE,
                LENSES_YELLOW,
                LENSES_LIME,
                LENSES_PINK,
                LENSES_GRAY,
                LENSES_LIGHT_GRAY,
                LENSES_CYAN,
                LENSES_PURPLE,
                LENSES_BLUE,
                LENSES_BROWN,
                LENSES_GREEN,
                LENSES_RED,
                LENSES_BLACK
        };
        // endregion lenses

        // region dyes
        @SuppressWarnings("unchecked")
        public static final TagKey<Item>[] DYES_ARRAY = new TagKey[] {
                Tags.Items.DYES_WHITE,
                Tags.Items.DYES_ORANGE,
                Tags.Items.DYES_MAGENTA,
                Tags.Items.DYES_LIGHT_BLUE,
                Tags.Items.DYES_YELLOW,
                Tags.Items.DYES_LIME,
                Tags.Items.DYES_PINK,
                Tags.Items.DYES_GRAY,
                Tags.Items.DYES_LIGHT_GRAY,
                Tags.Items.DYES_CYAN,
                Tags.Items.DYES_PURPLE,
                Tags.Items.DYES_BLUE,
                Tags.Items.DYES_BROWN,
                Tags.Items.DYES_GREEN,
                Tags.Items.DYES_RED,
                Tags.Items.DYES_BLACK
        };
        // endregion dyes
    }
    // endregion items

    // region fluids
    @UtilityClass
    public static class Fluids {

        // region misc
        public static final TagKey<Fluid> LIGHTER_FLUIDS = TagUtil.createModFluidTag("lighter_fluids");
        public static final TagKey<Fluid> HPCA_COOLANTS = TagUtil.createModFluidTag("hpca_coolants");
        public static final TagKey<Fluid> POTION = TagUtil.createFluidTag("potion");
        // endregion misc

        // region state
        public static final TagKey<Fluid> MOLTEN = TagUtil.createFluidTag("molten");
        public static final TagKey<Fluid> LIQUID = TagUtil.createFluidTag("liquid");
        public static final TagKey<Fluid> PLASMATIC = TagUtil.createFluidTag("plasmatic");
        // endregion state
    }
    // endregion fluids

    // region biomes
    @UtilityClass
    public static class Biomes {

        public static final TagKey<Biome> IS_SWAMP = TagUtil.createTag(Registries.BIOME, "is_swamp", false);
        public static final TagKey<Biome> IS_SANDY = TagUtil.createModTag(Registries.BIOME, "is_sandy");
        public static final TagKey<Biome> HAS_RUBBER_TREE = TagUtil.createModTag(Registries.BIOME, "has_rubber_tree");
    }
    // endregion biomes

// spotless:on
}
