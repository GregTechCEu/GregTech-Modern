package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class ToolLang {

    public static void init(GTLangProvider provider) {
        initDeathMessages(provider);
        generateToolClassKeys(provider);
        generateToolKeys(provider);
        generateTooltips(provider);
        generateBehaviorKeys(provider);
        generateActionKeys(provider);
        generateEnchantKeys(provider);
    }

    private static void initDeathMessages(GTLangProvider provider) {
        // Death Messages
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
    }

    public static void generateToolClassKeys(GTLangProvider provider) {
        // Tool Names
        provider.add("item.gtceu.tool.class.sword", "Sword");
        provider.add("item.gtceu.tool.class.pickaxe", "Pickaxe");
        provider.add("item.gtceu.tool.class.shovel", "Shovel");
        provider.add("item.gtceu.tool.class.axe", "Axe");
        provider.add("item.gtceu.tool.class.hoe", "Hoe");
        provider.add("item.gtceu.tool.class.mining_hammer", "Mining Hammer");
        provider.add("item.gtceu.tool.class.spade", "Spade");
        provider.add("item.gtceu.tool.class.saw", "Saw");
        provider.add("item.gtceu.tool.class.hammer", "Hammer");
        provider.add("item.gtceu.tool.class.mallet", "Soft Mallet");
        provider.add("item.gtceu.tool.class.wrench", "Wrench");
        provider.add("item.gtceu.tool.class.file", "File");
        provider.add("item.gtceu.tool.class.crowbar", "Crowbar");
        provider.add("item.gtceu.tool.class.screwdriver", "Screwdriver");
        provider.add("item.gtceu.tool.class.mortar", "Mortar");
        provider.add("item.gtceu.tool.class.wire_cutter", "Wire Cutter");
        provider.add("item.gtceu.tool.class.knife", "Knife");
        provider.add("item.gtceu.tool.class.butchery_knife", "Butchery Knife");
        provider.add("item.gtceu.tool.class.scythe", "Scythe");
        provider.add("item.gtceu.tool.class.rolling_pin", "Rolling Pin");
        provider.add("item.gtceu.tool.class.plunger", "Plunger");
        provider.add("item.gtceu.tool.class.shears", "Shears");
        provider.add("item.gtceu.tool.class.drill", "Drill");
    }

    public static void generateBehaviorKeys(GTLangProvider provider) {
        // Generic Tool Behaviors
        provider.add("item.gtceu.tool.behavior.silk_ice", "§bIce Cutter: §fSilk Harvests Ice");
        provider.add("item.gtceu.tool.behavior.torch_place", "§eSpelunker: §fPlaces Torches on Right-Click");
        provider.add("item.gtceu.tool.behavior.tree_felling", "§4Lumberjack: §fTree Felling");
        provider.add("item.gtceu.tool.behavior.strip_log", "§5Artisan: §fStrips Logs");
        provider.add("item.gtceu.tool.behavior.scrape", "§bPolisher: §fRemoves Oxidation");
        provider.add("item.gtceu.tool.behavior.remove_wax", "§6Cleaner: §fRemoves Wax");
        provider.add("item.gtceu.tool.behavior.shield_disable", "§cBrute: §fDisables Shields");
        provider.add("item.gtceu.tool.behavior.relocate_mining", "§2Magnetic: §fRelocates Mined Blocks and Mob Drops");
        provider.add("item.gtceu.tool.behavior.aoe_mining", "§5Area-of-Effect: §f%sx%sx%s");
        provider.add("item.gtceu.tool.behavior.ground_tilling", "§eFarmer: §fTills Ground");
        provider.add("item.gtceu.tool.behavior.grass_path", "§eLandscaper: §fCreates Grass Paths");
        provider.add("item.gtceu.tool.behavior.rail_rotation", "§eRailroad Engineer: §fRotates Rails");
        provider.add("item.gtceu.tool.behavior.crop_harvesting", "§aHarvester: §fHarvests Crops");
        provider.add("item.gtceu.tool.behavior.plunger", "§9Plumber: §fDrains Fluids");
        provider.add("item.gtceu.tool.behavior.block_rotation", "§2Mechanic: §fRotates Blocks");
        provider.add("item.gtceu.tool.behavior.damage_boost", "§4Damage Boost: §fExtra damage against %s");

        // AOE
        provider.add("item.gtceu.tool.aoe.rows", "Rows");
        provider.add("item.gtceu.tool.aoe.columns", "Columns");
        provider.add("item.gtceu.tool.aoe.layers", "Layers");

        // Wrench Mode Switch
        provider.add("item.behavior.mode_switch.tooltip", "Use while sneaking to switch mode");
        provider.add("item.behavior.mode_switch.mode_switched", "§eMode Set to: %s");
        provider.add("item.behavior.mode_switch.current_mode", "Mode: %s");
    }

    public static void generateToolKeys(GTLangProvider provider) {
        // Tool Names
        provider.add("item.gtceu.tool.sword", "%s Sword");
        provider.add("item.gtceu.tool.pickaxe", "%s Pickaxe");
        provider.add("item.gtceu.tool.shovel", "%s Shovel");
        provider.add("item.gtceu.tool.axe", "%s Axe");
        provider.add("item.gtceu.tool.hoe", "%s Hoe");
        provider.add("item.gtceu.tool.saw", "%s Saw");
        provider.add("item.gtceu.bucket", "%s Bucket");

        provider.add("item.gtceu.tool.hammer", "%s Hammer");
        provider.add("item.gtceu.tool.mallet", "%s Soft Mallet");
        provider.add("item.gtceu.tool.wrench", "%s Wrench");
        provider.add("item.gtceu.tool.file", "%s File");
        provider.add("item.gtceu.tool.crowbar", "%s Crowbar");
        provider.add("item.gtceu.tool.screwdriver", "%s Screwdriver");
        provider.add("item.gtceu.tool.mortar", "%s Mortar");
        provider.add("item.gtceu.tool.wire_cutter", "%s Wire Cutter");
        provider.add("item.gtceu.tool.knife", "%s Knife");
        provider.add("item.gtceu.tool.butchery_knife", "%s Butchery Knife");
        provider.add("item.gtceu.tool.scythe", "%s Scythe");
        provider.add("item.gtceu.tool.rolling_pin", "%s Rolling Pin");

        provider.add("item.gtceu.tool.lv_drill", "%s Drill (LV)");
        provider.add("item.gtceu.tool.mv_drill", "%s Drill (MV)");
        provider.add("item.gtceu.tool.hv_drill", "%s Drill (HV)");
        provider.add("item.gtceu.tool.ev_drill", "%s Drill (EV)");
        provider.add("item.gtceu.tool.iv_drill", "%s Drill (IV)");

        provider.add("item.gtceu.tool.lv_wirecutter", "%s Wire Cutter (LV)");
        provider.add("item.gtceu.tool.hv_wirecutter", "%s Wire Cutter (HV)");
        provider.add("item.gtceu.tool.iv_wirecutter", "%s Wire Cutter (IV)");

        provider.add("item.gtceu.tool.mining_hammer", "%s Mining Hammer");
        provider.add("item.gtceu.tool.spade", "%s Spade");

        provider.add("item.gtceu.tool.lv_chainsaw", "%s Chainsaw (LV)");
        provider.add("item.gtceu.tool.mv_chainsaw", "%s Chainsaw (MV)");
        provider.add("item.gtceu.tool.hv_chainsaw", "%s Chainsaw (HV)");

        provider.add("item.gtceu.tool.lv_wrench", "%s Wrench (LV)");
        provider.add("item.gtceu.tool.hv_wrench", "%s Wrench (HV)");
        provider.add("item.gtceu.tool.iv_wrench", "%s Wrench (IV)");

        provider.add("item.gtceu.tool.buzzsaw", "%s Buzzsaw (LV)");
        provider.add("item.gtceu.tool.lv_screwdriver", "%s Screwdriver (LV)");
        provider.add("item.gtceu.tool.plunger", "%s Plunger");
        provider.add("item.gtceu.tool.shears", "%s Shears");
    }

    public static void generateTooltips(GTLangProvider provider) {
        // Tool Tooltips
        provider.add("item.gtceu.tool.hammer.tooltip", "§8Crushes Blocks when harvesting them");
        provider.addMultiline("item.gtceu.tool.mallet.tooltip",
                "§8Sneak to Pause Machine After Current Recipe.\n§8Stops/Starts Machines");
        provider.add("item.gtceu.tool.wrench.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gtceu.tool.crowbar.tooltip", "§8Dismounts Covers");
        provider.add("item.gtceu.tool.screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gtceu.tool.butchery_knife.tooltip", "§8Has a slow Attack Rate");
        provider.add("item.gtceu.tool.scythe.tooltip", "§8Because a Scythe doesn't make Sense");
        provider.add("item.gtceu.tool.mining_hammer.tooltip",
                "§8Mines a large area at once (unless you're crouching)");
        provider.add("item.gtceu.tool.spade.tooltip", "§8Mines a large area at once (unless you're crouching)");
        provider.add("item.gtceu.tool.lv_wrench.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gtceu.tool.hv_wrench.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gtceu.tool.iv_wrench.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gtceu.tool.buzzsaw.tooltip", "§8Not suitable for harvesting Blocks");
        provider.add("item.gtceu.tool.lv_screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gtceu.tool.plunger.tooltip", "§8Removes Fluids from Machines");

        // Tool Stats Tooltips
        provider.add("tool.gtceu.tooltip.crafting_uses", "%s §aCrafting Uses");
        provider.add("tool.gtceu.tooltip.max_uses", "%s §eTotal Durability");
        provider.add("tool.gtceu.tooltip.general_uses", "%s §bDurability");
        provider.add("tool.gtceu.tooltip.attack_damage", "%s §cAttack Damage");
        provider.add("tool.gtceu.tooltip.attack_speed", "%s §9Attack Speed");
        provider.add("tool.gtceu.tooltip.mining_speed", "%s §dMining Speed");
        provider.add("tool.gtceu.tooltip.harvest_level", "§eHarvest Level %s");
        provider.add("tool.gtceu.tooltip.harvest_level_extra", "§eHarvest Level %s §f(%s§f)");
        provider.addMultiLang("tool.gtceu.tooltip.harvest_level_name",
                "§8Wood", "§7Stone", "§aIron", "§bDiamond",
                "§dNetherite", "§9Duranium", "§cNeutronium");

        // Repair Info
        provider.add("tool.gtceu.tooltip.show_repair_info", "§8Hold SHIFT to show Repair Info");
        provider.add("tool.gtceu.tooltip.repair_material", "§8Repair with: §f§a%s");
        provider.add("tool.gtceu.tooltip.replace_tool_head", "Craft with a new Tool Head to replace it");
        provider.add("tool.gtceu.tooltip.usable_as", "§8Usable as: §f%s");

        // Rotors
        provider.add("item.gtceu.turbine_rotor.tooltip.primary_material", "§fMaterial: §e%s");
        provider.add("item.gtceu.turbine_rotor.tooltip.durability", "§fDurability: §a%d / %d");
        provider.add("item.gtceu.turbine_rotor.tooltip.efficiency", "Turbine Efficiency: §9%d%%");
        provider.add("item.gtceu.turbine_rotor.tooltip.power", "Turbine Power: §9%d%%");
    }

    public static void generateActionKeys(GTLangProvider provider) {
        // Tool Actions?
        provider.add("tool_action.gtceu.show_tooltips", "Hold SHIFT to show Tool Info");

        provider.add("tool_action.gtceu.screwdriver.auto_output_covers",
                "§8Use Screwdriver to Allow Input from Output Side or access Covers");
        provider.add("tool_action.gtceu.screwdriver.toggle_mode_covers",
                "§8Use Screwdriver to toggle Modes or access Covers");
        provider.add("tool_action.gtceu.screwdriver.access_covers", "§8Use Screwdriver to access Covers");
        provider.add("tool_action.gtceu.screwdriver.auto_collapse",
                "§8Use Screwdriver to toggle Item collapsing");
        provider.add("tool_action.gtceu.screwdriver.auto_output", "§8Use Screwdriver to toggle Auto-Output");
        provider.add("tool_action.gtceu.screwdriver.toggle_mode", "§8Use Screwdriver to toggle Modes");

        provider.add("tool_action.gtceu.wrench.set_facing", "§8Use Wrench to set Facing");
        provider.add("tool_action.gtceu.wrench.connect",
                "§8Use Wrench to set Connections, sneak to block Connections");

        provider.add("tool_action.gtceu.wire_cutter.connect", "§8Use Wire Cutters to set Connections");

        provider.add("tool_action.gtceu.soft_mallet.reset", "§8Use Soft Mallet to toggle Working");
        provider.add("tool_action.gtceu.soft_mallet.toggle_mode", "§8Use Soft Mallet to toggle Modes");

        provider.add("tool_action.gtceu.hammer", "§8Use Hard Hammer to muffle Sounds");
        provider.add("tool_action.gtceu.crowbar", "§8Use Crowbar to remove Covers");

        provider.add("tool_action.gtceu.tape", "§8Use Tape to fix Maintenance Problems");
    }

    private static void generateEnchantKeys(GTLangProvider provider) {
        // Enchantments
        provider.add("enchantment.gtceu.disjunction", "Disjunction");
        provider.add("enchantment.gtceu.disjunction.description",
                "Applies Weakness and Slowness to Ender-related mobs.");

        provider.add("enchantment.gtceu.hard_hammer", "Hammering");
        provider.add("enchantment.gtceu.hard_hammer.description",
                "Breaks blocks as if they were mined with a GregTech Hammer.");
    }
}
