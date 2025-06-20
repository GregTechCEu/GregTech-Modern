package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;

public class ToolLang {

    public static void init(RegistrateLangProvider provider) {
        initDeathMessages(provider);
        generateToolClassKey(provider);
        generateToolKeys(provider);
        generateTooltips(provider);
        generateBehaviorKey(provider);
        generateActionKeys(provider);
        initToolInfo(provider);
    }

    private static void initDeathMessages(RegistrateLangProvider provider) {
        provider.add("death.attack.gtceu.heat", "%s was boiled alive");
        provider.add("death.attack.gtceu.frost", "%s explored cryogenics");
        provider.add("death.attack.gtceu.chemical", "%s had a chemical accident");
        provider.add("death.attack.gtceu.electric", "%s was electrocuted");
        provider.add("death.attack.gtceu.radiation", "%s glows with joy now");
        provider.add("death.attack.gtceu.turbine", "%s put their head into a turbine");
        provider.add("death.attack.gtceu.explosion", "%s exploded");
        provider.add("death.attack.gtceu.explosion.player", "%s exploded with help of %s");
        provider.add("death.attack.gtceu.heat.player", "%s was boiled alive by %s");
        provider.add("death.attack.gtceu.pickaxe", "%s got mined by %s");
        provider.add("death.attack.gtceu.shovel", "%s got dug up by %s");
        provider.add("death.attack.gtceu.axe", "%s has been chopped by %s");
        provider.add("death.attack.gtceu.hoe", "%s had their head tilled by %s");
        provider.add("death.attack.gtceu.hammer", "%s was squashed by %s");
        provider.add("death.attack.gtceu.mallet", "%s got hammered to death by %s");
        provider.add("death.attack.gtceu.mining_hammer", "%s was mistaken for Ore by %s");
        provider.add("death.attack.gtceu.spade", "%s got excavated by %s");
        provider.add("death.attack.gtceu.wrench", "%s gave %s a whack with the Wrench!");
        provider.add("death.attack.gtceu.file", "%s has been filed D for 'Dead' by %s");
        provider.add("death.attack.gtceu.crowbar", "%s lost half a life to %s");
        provider.add("death.attack.gtceu.screwdriver", "%s has screwed with %s for the last time!");
        provider.add("death.attack.gtceu.mortar", "%s was ground to dust by %s");
        provider.add("death.attack.gtceu.wire_cutter", "%s has cut the cable for the Life Support Machine of %s");
        provider.add("death.attack.gtceu.scythe", "%s had their soul taken by %s");
        provider.add("death.attack.gtceu.knife", "%s was gently poked by %s");
        provider.add("death.attack.gtceu.butchery_knife", "%s was butchered by %s");
        provider.add("death.attack.gtceu.drill_lv", "%s was drilled with 32V by %s");
        provider.add("death.attack.gtceu.drill_mv", "%s was drilled with 128V by %s");
        provider.add("death.attack.gtceu.drill_hv", "%s was drilled with 512V by %s");
        provider.add("death.attack.gtceu.drill_ev", "%s was drilled with 2048V by %s");
        provider.add("death.attack.gtceu.drill_iv", "%s was drilled with 8192V by %s");
        provider.add("death.attack.gtceu.chainsaw_lv", "%s was massacred by %s");
        provider.add("death.attack.gtceu.wrench_lv", "%s's pipes were loosened by %s");
        provider.add("death.attack.gtceu.wrench_hv", "%s's pipes were loosened by %s");
        provider.add("death.attack.gtceu.wrench_iv", "%s had a Monkey Wrench thrown into their plans by %s");
        provider.add("death.attack.gtceu.buzzsaw", "%s got buzzed by %s");
        provider.add("death.attack.gtceu.screwdriver_lv", "%s had their screws removed by %s");

        provider.add("death.attack.gtceu.medical_condition.asbestosis", "%s got mesothelioma");
        provider.add("death.attack.gtceu.medical_condition.chemical_burns", "%s had a chemical accident");
        provider.add("death.attack.gtceu.medical_condition.poison",
                "%s forgot that poisonous materials are, in fact, poisonous");
        provider.add("death.attack.gtceu.medical_condition.silicosis",
                "%s didn't die of tuberculosis. it was silicosis.");
        provider.add("death.attack.gtceu.medical_condition.arsenicosis", "%s got arsenic poisoning");
        provider.add("death.attack.gtceu.medical_condition.berylliosis", "%s mined emeralds a bit too greedily");
        provider.add("death.attack.gtceu.medical_condition.carcinogen", "%s got leukemia");
        provider.add("death.attack.gtceu.medical_condition.irritant", "%s got a §n§lREALLY§r bad rash");
        provider.add("death.attack.gtceu.medical_condition.methanol_poisoning",
                "%s tried to drink moonshine during the prohibition");
        provider.add("death.attack.gtceu.medical_condition.nausea", "%s died of nausea");
        provider.add("death.attack.gtceu.medical_condition.none", "%s died of... nothing?");
        provider.add("death.attack.gtceu.medical_condition.weak_poison", "%s ate lead (or mercury!)");
        provider.add("death.attack.gtceu.medical_condition.carbon_monoxide_poisoning", "%s left the stove on");
    }

    public static void generateToolClassKey(RegistrateLangProvider provider) {
        provider.add("tool.gtceu.class.sword", "Sword");
        provider.add("tool.gtceu.class.pickaxe", "Pickaxe");
        provider.add("tool.gtceu.class.shovel", "Shovel");
        provider.add("tool.gtceu.class.axe", "Axe");
        provider.add("tool.gtceu.class.hoe", "Hoe");
        provider.add("tool.gtceu.class.mining_hammer", "Mining Hammer");
        provider.add("tool.gtceu.class.spade", "Spade");
        provider.add("tool.gtceu.class.saw", "Saw");
        provider.add("tool.gtceu.class.hammer", "Hammer");
        provider.add("tool.gtceu.class.mallet", "Soft Mallet");
        provider.add("tool.gtceu.class.wrench", "Wrench");
        provider.add("tool.gtceu.class.file", "File");
        provider.add("tool.gtceu.class.crowbar", "Crowbar");
        provider.add("tool.gtceu.class.screwdriver", "Screwdriver");
        provider.add("tool.gtceu.class.mortar", "Mortar");
        provider.add("tool.gtceu.class.wire_cutter", "Wire Cutter");
        provider.add("tool.gtceu.class.knife", "Knife");
        provider.add("tool.gtceu.class.butchery_knife", "Butchery Knife");
        provider.add("tool.gtceu.class.scythe", "Scythe");
        provider.add("tool.gtceu.class.rolling_pin", "Rolling Pin");
        provider.add("tool.gtceu.class.plunger", "Plunger");
        provider.add("tool.gtceu.class.shears", "Shears");
        provider.add("tool.gtceu.class.drill", "Drill");
    }

    public static void generateBehaviorKey(RegistrateLangProvider provider) {
        provider.add("tool.gtceu.behavior.silk_ice", "§bIce Cutter: §fSilk Harvests Ice");
        provider.add("tool.gtceu.behavior.torch_place", "§eSpelunker: §fPlaces Torches on Right-Click");
        provider.add("tool.gtceu.behavior.tree_felling", "§4Lumberjack: §fTree Felling");
        provider.add("tool.gtceu.behavior.strip_log", "§5Artisan: §fStrips Logs");
        provider.add("tool.gtceu.behavior.scrape", "§bPolisher: §fRemoves Oxidation");
        provider.add("tool.gtceu.behavior.remove_wax", "§6Cleaner: §fRemoves Wax");
        provider.add("tool.gtceu.behavior.shield_disable", "§cBrute: §fDisables Shields");
        provider.add("tool.gtceu.behavior.relocate_mining", "§2Magnetic: §fRelocates Mined Blocks and Mob Drops");
        provider.add("tool.gtceu.behavior.aoe_mining", "§5Area-of-Effect: §f%sx%sx%s");
        provider.add("tool.gtceu.behavior.ground_tilling", "§eFarmer: §fTills Ground");
        provider.add("tool.gtceu.behavior.grass_path", "§eLandscaper: §fCreates Grass Paths");
        provider.add("tool.gtceu.behavior.rail_rotation", "§eRailroad Engineer: §fRotates Rails");
        provider.add("tool.gtceu.behavior.crop_harvesting", "§aHarvester: §fHarvests Crops");
        provider.add("tool.gtceu.behavior.plunger", "§9Plumber: §fDrains Fluids");
        provider.add("tool.gtceu.behavior.block_rotation", "§2Mechanic: §fRotates Blocks");
        provider.add("tool.gtceu.behavior.damage_boost", "§4Damage Boost: §fExtra damage against %s");

        // mode switching
        provider.add("item.behavior.mode_switch.tooltip", "Use while sneaking to switch mode");
        provider.add("item.behavior.mode_switch.mode_switched", "§eMode Set to: %s");
        provider.add("item.behavior.mode_switch.current_mode", "Mode: %s");
    }

    public static void generateToolKeys(RegistrateLangProvider provider) {
        // tool names
        replace(provider, "item.gtceu.tool.sword", "%s Sword");
        replace(provider, "item.gtceu.tool.pickaxe", "%s Pickaxe");
        replace(provider, "item.gtceu.tool.shovel", "%s Shovel");
        replace(provider, "item.gtceu.tool.axe", "%s Axe");
        replace(provider, "item.gtceu.tool.hoe", "%s Hoe");
        replace(provider, "item.gtceu.tool.saw", "%s Saw");

        replace(provider, "item.gtceu.tool.hammer", "%s Hammer");
        replace(provider, "item.gtceu.tool.mallet", "%s Soft Mallet");
        replace(provider, "item.gtceu.tool.wrench", "%s Wrench");
        replace(provider, "item.gtceu.tool.file", "%s File");
        replace(provider, "item.gtceu.tool.crowbar", "%s Crowbar");
        replace(provider, "item.gtceu.tool.screwdriver", "%s Screwdriver");
        replace(provider, "item.gtceu.tool.mortar", "%s Mortar");
        replace(provider, "item.gtceu.tool.wire_cutter", "%s Wire Cutter");
        replace(provider, "item.gtceu.tool.knife", "%s Knife");
        replace(provider, "item.gtceu.tool.butchery_knife", "%s Butchery Knife");
        replace(provider, "item.gtceu.tool.scythe", "%s Scythe");
        replace(provider, "item.gtceu.tool.rolling_pin", "%s Rolling Pin");
        replace(provider, "item.gtceu.tool.lv_drill", "%s Drill (LV)");
        replace(provider, "item.gtceu.tool.mv_drill", "%s Drill (MV)");
        replace(provider, "item.gtceu.tool.hv_drill", "%s Drill (HV)");
        replace(provider, "item.gtceu.tool.ev_drill", "%s Drill (EV)");
        replace(provider, "item.gtceu.tool.iv_drill", "%s Drill (IV)");
        replace(provider, "item.gtceu.tool.lv_wirecutter", "%s Wire Cutter (LV)");
        replace(provider, "item.gtceu.tool.hv_wirecutter", "%s Wire Cutter (HV)");
        replace(provider, "item.gtceu.tool.iv_wirecutter", "%s Wire Cutter (IV)");
        replace(provider, "item.gtceu.tool.mining_hammer", "%s Mining Hammer");
        replace(provider, "item.gtceu.tool.spade", "%s Spade");
        replace(provider, "item.gtceu.tool.lv_chainsaw", "%s Chainsaw (LV)");
        replace(provider, "item.gtceu.tool.mv_chainsaw", "%s Chainsaw (MV)");
        replace(provider, "item.gtceu.tool.hv_chainsaw", "%s Chainsaw (HV)");
        replace(provider, "item.gtceu.tool.lv_wrench", "%s Wrench (LV)");
        replace(provider, "item.gtceu.tool.hv_wrench", "%s Wrench (HV)");
        replace(provider, "item.gtceu.tool.iv_wrench", "%s Wrench (IV)");
        replace(provider, "item.gtceu.tool.buzzsaw", "%s Buzzsaw (LV)");
        replace(provider, "item.gtceu.tool.lv_screwdriver", "%s Screwdriver (LV)");
        replace(provider, "item.gtceu.tool.plunger", "%s Plunger");
        replace(provider, "item.gtceu.tool.shears", "%s Shears");

        provider.add("item.gtceu.tool.hammer.tooltip", "§8Crushes Blocks when harvesting them");
        multiLang(provider, "item.gtceu.tool.mallet.tooltip",
                "§8Sneak to Pause Machine After Current Recipe.",
                "§8Stops/Starts Machines");
        provider.add("item.gtceu.tool.wrench.tooltip", "§8Hold L-CLICK to dismantle Machines");
        provider.add("item.gtceu.tool.crowbar.tooltip", "§8Dismounts Covers");
        provider.add("item.gtceu.tool.screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gtceu.tool.butchery_knife.tooltip", "§8Has a slow Attack Rate");
        provider.add("item.gtceu.tool.scythe.tooltip", "§8Because a Scythe doesn't make Sense");
        provider.add("item.gtceu.tool.mining_hammer.tooltip",
                "§8Mines a large area at once (unless you're crouching)");
        provider.add("item.gtceu.tool.spade.tooltip", "§8Mines a large area at once (unless you're crouching)");
        // provider.add("item.gtceu.tool.lv_wrench.tooltip", "§8Hold a L-CLICK to dismantle Machines");
        // provider.add("item.gtceu.tool.hv_wrench.tooltip", "§8Hold a L-CLICK to dismantle Machines");
        // provider.add("item.gtceu.tool.iv_wrench.tooltip", "§8Hold a L-CLICK to dismantle Machines");
        provider.add("item.gtceu.tool.buzzsaw.tooltip", "§8Not suitable for harvesting Blocks");
        provider.add("item.gtceu.tool.lv_screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gtceu.tool.plunger.tooltip", "§8Removes Fluids from Machines");
    }

    public static void generateTooltips(RegistrateLangProvider provider) {
        provider.add("tool.gtceu.crafting_uses.tooltip", "%s §aCrafting Uses");
        provider.add("tool.gtceu.max_uses.tooltip", "%s §eTotal Durability");
        provider.add("tool.gtceu.general_uses.tooltip", "%s §bDurability");
        provider.add("tool.gtceu.attack_damage.tooltip", "%s §cAttack Damage");
        provider.add("tool.gtceu.attack_speed.tooltip", "%s §9Attack Speed");
        provider.add("tool.gtceu.mining_speed.tooltip", "%s §dMining Speed");
        provider.add("tool.gtceu.harvest_level.tooltip", "§eHarvest Level %s");
        provider.add("tool.gtceu.harvest_level_extra.tooltip", "§eHarvest Level %s §f(%s§f)");
        multiLang(provider, "item.gtceu.tool.harvest_level",
                "§8Wood", "§7Stone", "§aIron", "§bDiamond",
                "§dNetherite", "§9Duranium", "§cNeutronium");
        provider.add("tool.gtceu.repair_info.tooltip", "§8Hold SHIFT to show Repair Info");
        provider.add("tool.gtceu.repair_material.tooltip", "§8Repair with: §f§a%s");
    }

    public static void generateActionKeys(RegistrateLangProvider provider) {
        provider.add("gtceu.tool_action.show_tooltips", "§7Hold SHIFT to show Tool Info");
        provider.add("gtceu.tool_action.screwdriver.auto_output_covers",
                "§8Use Screwdriver to Allow Input from Output Side or access Covers");
        provider.add("gtceu.tool_action.screwdriver.toggle_mode_covers",
                "§8Use Screwdriver to toggle Modes or access Covers");
        provider.add("gtceu.tool_action.screwdriver.access_covers", "§8Use Screwdriver to access Covers");
        provider.add("gtceu.tool_action.screwdriver.auto_collapse",
                "§8Use Screwdriver to toggle Item collapsing");
        provider.add("gtceu.tool_action.screwdriver.auto_output", "§8Use Screwdriver to toggle Auto-Output");
        provider.add("gtceu.tool_action.screwdriver.toggle_mode", "§8Use Screwdriver to toggle Modes");
        provider.add("gtceu.tool_action.wrench.set_facing", "§8Use Wrench to set Facing");
        provider.add("gtceu.tool_action.wrench.connect",
                "§8Use Wrench to set Connections, sneak to block Connections");
        provider.add("gtceu.tool_action.wire_cutter.connect", "§8Use Wire Cutters to set Connections");
        provider.add("gtceu.tool_action.soft_mallet.reset", "§8Use Soft Mallet to toggle Working");
        provider.add("gtceu.tool_action.soft_mallet.toggle_mode", "§8Use Soft Mallet to toggle Modes");
        provider.add("gtceu.tool_action.hammer", "§8Use Hard Hammer to muffle Sounds");
        provider.add("gtceu.tool_action.crowbar", "§8Use Crowbar to remove Covers");
        provider.add("gtceu.tool_action.tape", "§8Use Tape to fix Maintenance Problems");
    }

    private static void initToolInfo(RegistrateLangProvider provider) {}
}
