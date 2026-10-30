package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class ToolLang {

    public static void init(GTLangProvider provider) {
        generateDeathMessages(provider);
        generateToolClassKeys(provider);
        generateTooltips(provider);
        generateEnchantKeys(provider);

        provider.add("item.gtceu.turbine_rotor_generic", "Turbine Rotor");
        provider.add("item.gtceu.tool_generic", "Non-electric tool");
        provider.add("item.gtceu.bucket", "%s Bucket");
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
        provider.add("item.gtceu.tool.class.plunger", "Plunger");
        provider.add("item.gtceu.tool.class.shears", "Shears");
        provider.add("item.gtceu.tool.class.drill", "Drill");
    }

    public static void generateTooltips(GTLangProvider provider) {
        // Generic Tool Behaviors
        provider.add("tool_behavior.gtceu.silk_ice", "§bIce Cutter: §fSilk Harvests Ice");
        provider.add("tool_behavior.gtceu.torch_place", "§eSpelunker: §fPlaces Torches on RIGHT CLICK");
        provider.add("tool_behavior.gtceu.tree_felling", "§4Lumberjack: §fTree Felling");
        provider.add("tool_behavior.gtceu.strip_log", "§5Artisan: §fStrips Logs");
        provider.add("tool_behavior.gtceu.scrape", "§bPolisher: §fRemoves Oxidation");
        provider.add("tool_behavior.gtceu.remove_wax", "§6Cleaner: §fRemoves Wax");
        provider.add("tool_behavior.gtceu.shield_disable", "§cBrute: §fDisables Shields");
        provider.add("tool_behavior.gtceu.relocate_mining", "§2Magnetic: §fRelocates Mined Blocks and Mob Drops");
        provider.add("tool_behavior.gtceu.aoe_mining", "§5Area-of-Effect: §f%sx%sx%s");
        provider.add("tool_behavior.gtceu.ground_tilling", "§eFarmer: §fTills Ground");
        provider.add("tool_behavior.gtceu.shears", "§aGardener: §fShears blocks and mobs");
        provider.add("tool_behavior.gtceu.grass_path", "§eLandscaper: §fCreates Grass Paths");
        provider.add("tool_behavior.gtceu.rail_rotation", "§eRailroad Engineer: §fRotates Rails");
        provider.add("tool_behavior.gtceu.crop_harvesting", "§aHarvester: §fHarvests Crops");
        provider.add("tool_behavior.gtceu.plunger", "§9Plumber: §fDrains Fluids");
        provider.add("tool_behavior.gtceu.block_rotation", "§2Mechanic: §fRotates Blocks");
        provider.add("tool_behavior.gtceu.dowse_campfire", "§6Firefighter: §fDowses Campfires");
        provider.add("tool_behavior.gtceu.damage_boost", "§4Damage Boost: §fExtra damage against %s");
        provider.add("tool_behavior.gtceu.prospecting.ore", "Found ore: %s");
        provider.add("tool_behavior.gtceu.prospecting.air", "Found an air pocket");
        provider.add("tool_behavior.gtceu.prospecting.water", "Found water");
        provider.add("tool_behavior.gtceu.prospecting.lava", "Found lava");
        provider.add("tool_behavior.gtceu.prospecting.changing", "Detected material change");

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
        provider.add("item.gtceu.tool.hv_screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gtceu.tool.iv_screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gtceu.tool.plunger.tooltip", "§8Removes Fluids from Machines");

        // Tool Stats Tooltips

        provider.add("tool.gtceu.tooltip.crafting_uses", "%s Crafting Uses");
        provider.add("tool.gtceu.tooltip.general_uses", "%s / %s Durability");
        provider.add("tool.gtceu.tooltip.attack_damage", "%s Attack Damage");
        provider.add("tool.gtceu.tooltip.attack_speed", "%s Attack Speed");
        provider.add("tool.gtceu.tooltip.mining_speed", "%s Mining Speed");
        provider.add("tool.gtceu.tooltip.harvest_level", "Harvest Level %s");
        provider.addMultiLang("tool.gtceu.tooltip.harvest_level_name",
                "§8Wood", "§7Stone", "§aIron", "§bDiamond",
                "§dNetherite", "§9Duranium", "§cNeutronium");

        // Repair Info
        provider.add("tool.gtceu.tooltip.show_repair_info", "Hold SHIFT to show Repair Info");
        provider.add("tool.gtceu.tooltip.repair_material", "Repair with: %s");
        provider.add("tool.gtceu.tooltip.default_enchantments", "Default Enchantments:");
        provider.add("tool.gtceu.tooltip.replace_tool_head", "Craft with a new Tool Head to replace it");
        provider.add("tool.gtceu.tooltip.usable_as", "Usable as: %s");

        // Rotors
        provider.add("item.gtceu.turbine_rotor.tooltip.primary_material", "§fMaterial: §e%s");
        provider.add("item.gtceu.turbine_rotor.tooltip.durability", "§fDurability: §a%d / %d");
        provider.add("item.gtceu.turbine_rotor.tooltip.efficiency", "Turbine Efficiency: §9%d%%");
        provider.add("item.gtceu.turbine_rotor.tooltip.power", "Turbine Power: §9%d%%");

        // AOE
        provider.add("gui.gtceu.tool_aoe.rows", "Rows");
        provider.add("gui.gtceu.tool_aoe.columns", "Columns");
        provider.add("gui.gtceu.tool_aoe.layers", "Layers");

        // Wrench Mode Switch
        provider.add("item.behavior.mode_switch.tooltip", "Use while sneaking to switch mode");
        provider.add("item.behavior.mode_switch.current_mode", "Mode: %s");

        provider.add("item.gtceu.wrench.tooltip.set_connections",
                "Use Wrench to set Connections, sneak to block Connections");
        provider.add("item.gtceu.wrench.wire_cutter.set_connections", "Use Wire Cutters to set Connections");
    }

    private static void generateDeathMessages(GTLangProvider provider) {
        // Death Messages
        provider.add("death.attack.gtceu.heat", "%s was boiled alive");
        provider.add("death.attack.gtceu.frost", "%s explored cryogenics");
        provider.add("death.attack.gtceu.chemical", "%s had a chemical accident");
        provider.add("death.attack.gtceu.electric", "%s was electrocuted");
        provider.add("death.attack.gtceu.radiation", "%s glows with joy now");
        provider.add("death.attack.gtceu.turbine", "%s put their head into a turbine");
        provider.add("death.attack.gtceu.heat.player", "%s was boiled alive by %s");
        provider.add("death.attack.gtceu.pickaxe", "%s was mined by %s");
        provider.add("death.attack.gtceu.shovel", "%s was dug up by %s");
        provider.add("death.attack.gtceu.axe", "%s was chopped by %s");
        provider.add("death.attack.gtceu.hoe", "%s had their head tilled by %s");
        provider.add("death.attack.gtceu.hammer", "%s was squashed by %s");
        provider.add("death.attack.gtceu.mallet", "%s was hammered to death by %s");
        provider.add("death.attack.gtceu.mining_hammer", "%s was mistaken for Ore by %s");
        provider.add("death.attack.gtceu.spade", "%s was excavated by %s");
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
        provider.add("death.attack.gtceu.wrench_iv", "%s had a wrench thrown into their plans by %s");
        provider.add("death.attack.gtceu.buzzsaw", "%s was buzzed by %s");
        provider.add("death.attack.gtceu.screwdriver_lv", "%s had their screws removed by %s");
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
