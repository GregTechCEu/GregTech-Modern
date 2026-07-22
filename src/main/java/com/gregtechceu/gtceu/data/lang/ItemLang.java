package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class ItemLang {

    public static void init(GTLangProvider provider) {
        initGeneratedNames(provider);
        initItemNames(provider);
        initItemTooltips(provider);
        generateBehaviorKeys(provider);
    }

    private static void initGeneratedNames(GTLangProvider provider) {
        // All TagPrefixes
        for (TagPrefix tagPrefix : TagPrefix.values()) {
            provider.add(tagPrefix.getUnlocalizedName(), tagPrefix.langValue);
        }

        // All GTToolTypes
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            provider.add(toolType.getUnlocalizedName(), toEnglishName(toolType.name));
        }

        // Tag Prefix
        provider.add("tagprefix.polymer.plate", "%s Sheet");
        provider.add("tagprefix.polymer.foil", "Thin %s Sheet");
        provider.add("tagprefix.polymer.nugget", "%s Chip");
        provider.add("tagprefix.polymer.dense_plate", "Dense %s Sheet");
        provider.add("tagprefix.polymer.double_plate", "Double %s Sheet");
        provider.add("tagprefix.polymer.tiny_dust", "Tiny Pile of %s Pulp");
        provider.add("tagprefix.polymer.small_dust", "Small Pile of %s Pulp");
        provider.add("tagprefix.polymer.dust", "%s Pulp");
        provider.add("tagprefix.polymer.ingot", "%s Ingot");

        // Material Items
        provider.add("item.gtceu.tiny_gunpowder_dust", "Tiny Pile of Gunpowder");
        provider.add("item.gtceu.small_gunpowder_dust", "Small Pile of Gunpowder");

        provider.add("item.gtceu.tiny_paper_dust", "Tiny Pile of Chad");
        provider.add("item.gtceu.small_paper_dust", "Small Pile of Chad");
        provider.add("item.gtceu.paper_dust", "Chad");

        provider.add("item.gtceu.tiny_rare_earth_dust", "Tiny Pile of Rare Earth");
        provider.add("item.gtceu.small_rare_earth_dust", "Small Pile of Rare Earth");
        provider.add("item.gtceu.rare_earth_dust", "Rare Earth");

        provider.add("item.gtceu.tiny_ash_dust", "Tiny Pile of Ashes");
        provider.add("item.gtceu.small_ash_dust", "Small Pile of Ashes");
        provider.add("item.gtceu.ash_dust", "Ashes");

        provider.add("item.gtceu.tiny_bone_dust", "Tiny Pile of Bone Meal");
        provider.add("item.gtceu.small_bone_dust", "Small Pile of Bone Meal");
        provider.add("item.gtceu.bone_dust", "Bone Meal");

        provider.add("item.gtceu.refined_cassiterite_sand_ore", "Refined Cassiterite Sand");
        provider.add("item.gtceu.purified_cassiterite_sand_ore", "Purified Cassiterite Sand");
        provider.add("item.gtceu.crushed_cassiterite_sand_ore", "Ground Cassiterite Sand");
        provider.add("item.gtceu.tiny_cassiterite_sand_dust", "Tiny Pile of Cassiterite Sand");
        provider.add("item.gtceu.small_cassiterite_sand_dust", "Small Pile of Cassiterite Sand");
        provider.add("item.gtceu.impure_cassiterite_sand_dust", "Impure Pile of Cassiterite Sand");
        provider.add("item.gtceu.pure_cassiterite_sand_dust", "Purified Pile of Cassiterite Sand");
        provider.add("item.gtceu.cassiterite_sand_dust", "Cassiterite Sand");

        provider.add("item.gtceu.tiny_dark_ash_dust", "Tiny Pile of Dark Ashes");
        provider.add("item.gtceu.small_dark_ash_dust", "Small Pile of Dark Ashes");
        provider.add("item.gtceu.dark_ash_dust", "Dark Ashes");

        provider.add("item.gtceu.tiny_ice_dust", "Tiny Pile of Crushed Ice");
        provider.add("item.gtceu.small_ice_dust", "Small Pile of Crushed Ice");
        provider.add("item.gtceu.ice_dust", "Crushed Ice");

        provider.add("item.gtceu.sugar_gem", "Sugar Cube");
        provider.add("item.gtceu.chipped_sugar_gem", "Small Sugar Cubes");
        provider.add("item.gtceu.flawed_sugar_gem", "Tiny Sugar Cube");

        provider.add("item.gtceu.tiny_rock_salt_dust", "Tiny Pile of Rock Salt");
        provider.add("item.gtceu.small_rock_salt_dust", "Small Pile of Rock Salt");
        provider.add("item.gtceu.impure_rock_salt_dust", "Impure Pile of Rock Salt");
        provider.add("item.gtceu.pure_rock_salt_dust", "Purified Pile of Rock Salt");
        provider.add("item.gtceu.rock_salt_dust", "Rock Salt");

        provider.add("item.gtceu.tiny_salt_dust", "Tiny Pile of Salt");
        provider.add("item.gtceu.small_salt_dust", "Small Pile of Salt");
        provider.add("item.gtceu.impure_salt_dust", "Impure Pile of Salt");
        provider.add("item.gtceu.pure_salt_dust", "Purified Pile of Salt");
        provider.add("item.gtceu.salt_dust", "Salt");

        provider.add("item.gtceu.tiny_wood_dust", "Tiny Pile of Wood Pulp");
        provider.add("item.gtceu.small_wood_dust", "Small Pile of Wood Pulp");
        provider.add("item.gtceu.wood_dust", "Wood Pulp");
        provider.add("item.gtceu.wood_plate", "Wood Plank");
        provider.add("item.gtceu.long_wood_rod", "Long Wood Stick");
        provider.add("item.gtceu.wood_bolt", "Short Wood Stick");

        provider.add("item.gtceu.tiny_treated_wood_dust", "Tiny Pile of Treated Wood Pulp");
        provider.add("item.gtceu.small_treated_wood_dust", "Small Pile of Treated Wood Pulp");
        provider.add("item.gtceu.treated_wood_dust", "Treated Wood Pulp");
        provider.add("item.gtceu.treated_wood_plate", "Treated Wood Plank");
        provider.add("item.gtceu.treated_wood_rod", "Treated Wood Stick");
        provider.add("item.gtceu.long_treated_wood_rod", "Long Treated Wood Stick");
        provider.add("item.gtceu.treated_wood_bolt", "Short Treated Wood Stick");

        provider.add("item.gtceu.glass_gem", "Glass Crystal");
        provider.add("item.gtceu.chipped_glass_gem", "Chipped Glass Crystal");
        provider.add("item.gtceu.flawed_glass_gem", "Flawed Glass Crystal");
        provider.add("item.gtceu.flawless_glass_gem", "Flawless Glass Crystal");
        provider.add("item.gtceu.exquisite_glass_gem", "Exquisite Glass Crystal");
        provider.add("item.gtceu.glass_plate", "Glass Pane");
        provider.add("item.gtceu.glass_lens", "Glass Lens (White)");

        provider.add("item.gtceu.tiny_blaze_dust", "Tiny Pile of Blaze Powder");
        provider.add("item.gtceu.small_blaze_dust", "Small Pile of Blaze Powder");

        provider.add("item.gtceu.tiny_sugar_dust", "Tiny Pile of Sugar");
        provider.add("item.gtceu.small_sugar_dust", "Small Pile of Sugar");

        provider.add("item.gtceu.tiny_basaltic_mineral_sand_dust", "Tiny Pile of Basaltic Mineral Sand");
        provider.add("item.gtceu.small_basaltic_mineral_sand_dust", "Small Pile of Basaltic Mineral Sand");
        provider.add("item.gtceu.basaltic_mineral_sand_dust", "Basaltic Mineral Sand");

        provider.add("item.gtceu.tiny_granitic_mineral_sand_dust", "Tiny Pile of Granitic Mineral Sand");
        provider.add("item.gtceu.small_granitic_mineral_sand_dust", "Small Pile of Granitic Mineral Sand");
        provider.add("item.gtceu.granitic_mineral_sand_dust", "Granitic Mineral Sand");

        provider.add("item.gtceu.tiny_garnet_sand_dust", "Tiny Pile of Garnet Sand");
        provider.add("item.gtceu.small_garnet_sand_dust", "Small Pile of Garnet Sand");
        provider.add("item.gtceu.garnet_sand_dust", "Garnet Sand");

        provider.add("item.gtceu.tiny_quartz_sand_dust", "Tiny Pile of Quartz Sand");
        provider.add("item.gtceu.small_quartz_sand_dust", "Small Pile of Quartz Sand");
        provider.add("item.gtceu.quartz_sand_dust", "Quartz Sand");

        provider.add("item.gtceu.tiny_glauconite_sand_dust", "Tiny Pile of Glauconite Sand");
        provider.add("item.gtceu.small_glauconite_sand_dust", "Small Pile of Glauconite Sand");
        provider.add("item.gtceu.glauconite_sand_dust", "Glauconite Sand");

        provider.add("item.gtceu.refined_bentonite_ore", "Refined Bentonite");
        provider.add("item.gtceu.purified_bentonite_ore", "Purified Bentonite");
        provider.add("item.gtceu.crushed_bentonite_ore", "Ground Bentonite");
        provider.add("item.gtceu.tiny_bentonite_dust", "Tiny Pile of Bentonite");
        provider.add("item.gtceu.small_bentonite_dust", "Small Pile of Bentonite");
        provider.add("item.gtceu.impure_bentonite_dust", "Impure Pile of Bentonite");
        provider.add("item.gtceu.pure_bentonite_dust", "Purified Pile of Bentonite");
        provider.add("item.gtceu.bentonite_dust", "Bentonite");

        provider.add("item.gtceu.tiny_fullers_earth_dust", "Tiny Pile of Fullers Earth");
        provider.add("item.gtceu.small_fullers_earth_dust", "Small Pile of Fullers Earth");
        provider.add("item.gtceu.fullers_earth_dust", "Fullers Earth");

        provider.add("item.gtceu.refined_pitchblende_ore", "Refined Pitchblende");
        provider.add("item.gtceu.purified_pitchblende_ore", "Purified Pitchblende");
        provider.add("item.gtceu.crushed_pitchblende_ore", "Ground Pitchblende");
        provider.add("item.gtceu.tiny_pitchblende_dust", "Tiny Pile of Pitchblende");
        provider.add("item.gtceu.small_pitchblende_dust", "Small Pile of Pitchblende");
        provider.add("item.gtceu.impure_pitchblende_dust", "Impure Pile of Pitchblende");
        provider.add("item.gtceu.pure_pitchblende_dust", "Purified Pile of Pitchblende");
        provider.add("item.gtceu.pitchblende_dust", "Pitchblende");

        provider.add("item.gtceu.refined_talc_ore", "Refined Talc");
        provider.add("item.gtceu.purified_talc_ore", "Purified Talc");
        provider.add("item.gtceu.crushed_talc_ore_ore", "Ground Talc");
        provider.add("item.gtceu.tiny_talc_dust", "Tiny Pile of Talc");
        provider.add("item.gtceu.small_talc_dust", "Small Pile of Talc");
        provider.add("item.gtceu.impure_talc_dust", "Impure Pile of Talc");
        provider.add("item.gtceu.pure_talc_dust", "Purified Pile of Talc");
        provider.add("item.gtceu.talc_dust", "Talc");

        provider.add("item.gtceu.tiny_wheat_dust", "Tiny Pile of Flour");
        provider.add("item.gtceu.small_wheat_dust", "Small Pile of Flour");
        provider.add("item.gtceu.wheat_dust", "Flour");

        provider.add("item.gtceu.tiny_meat_dust", "Tiny Pile of Mince Meat");
        provider.add("item.gtceu.small_meat_dust", "Small Pile of Mince Meat");
        provider.add("item.gtceu.meat_dust", "Mince Meat");

        provider.add("item.gtceu.borosilicate_glass_ingot", "Borosilicate Glass Bar");
        provider.add("item.gtceu.fine_borosilicate_glass_wire", "Borosilicate Glass Fibers");

        provider.add("item.gtceu.tiny_platinum_group_sludge_dust", "Tiny Clump of Platinum Group Sludge");
        provider.add("item.gtceu.small_platinum_group_sludge_dust", "Small Clump of Platinum Group Sludge");
        provider.add("item.gtceu.platinum_group_sludge_dust", "Platinum Group Sludge");

        provider.add("item.gtceu.tiny_platinum_raw_dust", "Tiny Pile of Raw Platinum Powder");
        provider.add("item.gtceu.small_platinum_raw_dust", "Small Pile of Raw Platinum Powder");
        provider.add("item.gtceu.platinum_raw_dust", "Raw Platinum Powder");

        provider.add("item.gtceu.tiny_palladium_raw_dust", "Tiny Pile of Raw Palladium Powder");
        provider.add("item.gtceu.small_palladium_raw_dust", "Small Pile of Raw Palladium Powder");
        provider.add("item.gtceu.palladium_raw_dust", "Raw Palladium Powder");

        provider.add("item.gtceu.tiny_inert_metal_mixture_dust", "Tiny Pile of Inert Metal Mixture");
        provider.add("item.gtceu.small_inert_metal_mixture_dust", "Small Pile of Inert Metal Mixture");
        provider.add("item.gtceu.inert_metal_mixture_dust", "Inert Metal Mixture");

        provider.add("item.gtceu.tiny_rarest_metal_mixture_dust", "Tiny Pile of Rarest Metal Mixture");
        provider.add("item.gtceu.small_rarest_metal_mixture_dust", "Small Pile of Rarest Metal Mixture");
        provider.add("item.gtceu.rarest_metal_mixture_dust", "Rarest Metal Mixture");

        provider.add("item.gtceu.tiny_platinum_sludge_residue_dust", "Tiny Pile of Platinum Sludge Residue");
        provider.add("item.gtceu.small_platinum_sludge_residue_dust", "Small Pile of Platinum Sludge Residue");
        provider.add("item.gtceu.platinum_sludge_residue_dust", "Platinum Sludge Residue");

        provider.add("item.gtceu.tiny_iridium_metal_residue_dust", "Tiny Pile of Iridium Metal Residue");
        provider.add("item.gtceu.small_iridium_metal_residue_dust", "Small Pile of Iridium Metal Residue");
        provider.add("item.gtceu.iridium_metal_residue_dust", "Iridium Metal Residue");
    }

    private static void initItemNames(GTLangProvider provider) {
        provider.add("item.gtceu.tungsten_steel_fluid_cell", "%s Tungstensteel Cell");
    }

    private static void initItemTooltips(GTLangProvider provider) {
        // Battery Behavior
        provider.add("item.gtceu.battery.charge_time", "§aHolds %s %s of Power (%s)");
        provider.add("item.gtceu.battery.charge_detailed", "%s/%s EU§7 - Tier %s §7(%s/%s remaining§7)");

        // Cover Tooltips
        provider.add("item.gtceu.electric.pump.tooltip", "§7Transfers §fFluids§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.fluid.regulator.tooltip", "§7Limits §fFluids§7 to specific quantities as §fCover§7.");
        provider.add("item.gtceu.conveyor.module.tooltip", "§7Transfers §fItems§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.robot.arm.tooltip", "§7Limits §fItems§7 to specific quantities as §fCover§7.");

        // Programmed Circuit
        provider.add("item.gtceu.circuit.integrated.gui", "§7Programmed Circuit Configuration");
        provider.add("item.int_circuit.configuration", "Configuration: %d");

        // Turbines
        provider.add("item.gtceu.turbine_rotor.tooltip", "Turbine Rotors for your power station");

        provider.add("item.gtceu.fluid_container.tooltip", "%d/%dL %s");

        // General Electric Tools
        provider.add("item.gtceu.electric.tooltip", "%d/%d EU - Tier %s");
        provider.add("item.gtceu.electric.stored", "%d/%d EU (%s)");
        provider.add("item.electric.discharge_mode.enabled", "§eDischarge Mode Enabled");
        provider.add("item.electric.discharge_mode.disabled", "§eDischarge Mode Disabled");
        provider.add("item.electric.discharge_mode.tooltip", "Use while sneaking to toggle discharge mode");

    }

    public static void generateBehaviorKeys(GTLangProvider provider) {
        // Item Behaviors
        provider.add("item.gtceu.behavior.hoe", "Can till dirt");
        provider.add("item.gtceu.behavior.soft_hammer", "Activates and Deactivates Machines");
        provider.add("item.gtceu.behavior.soft_hammer.enabled", "Working Enabled");
        provider.add("item.gtceu.behavior.soft_hammer.disabled", "Working Disabled");
        provider.add("item.gtceu.behavior.soft_hammer.disabled_cycle", "Working disabled after current cycle");

        provider.add("item.gtceu.behavior.lighter.tooltip.description", "Can light things on fire");
        provider.add("item.gtceu.behavior.lighter.tooltip.usage", "Shift-right click to open/close");
        provider.add("item.gtceu.behavior.lighter.fluid.tooltip", "Can light things on fire with Butane or Propane");
        provider.add("item.gtceu.behavior.lighter.uses", "Remaining uses: %d");

        provider.add("item.gtceu.behavior.toggle_energy.tooltip", "Use to toggle mode");
        provider.add("item.gtceu.behavior.hammer", "Turns on and off Muffling for Machines (by hitting them)");
        
        // Magnet
        provider.add("item.gtceu.behavior.item_magnet.enabled", "§aMagnetic Field Enabled");
        provider.add("item.gtceu.behavior.item_magnet.disabled", "§cMagnetic Field Disabled");

        // Wrench Configuration
        provider.add("item.gtceu.behavior.tool_mode.mode", "§aConfiguration Mode:§r %s");
        provider.add("item.gtceu.behavior.tool_mode.fluid", "§9Fluid§r");
        provider.add("item.gtceu.behavior.tool_mode.item", "§6Item§r");
        provider.add("item.gtceu.behavior.tool_mode.both", "§dBoth (Fluid And Item)§r");

        // Spray can
        provider.add("item.gtceu.behavior.spray_paint.solvent.tooltip", "Can remove color from things");
        provider.add("item.gtceu.behavior.spray_paint.white.tooltip", "Can paint things in White");
        provider.add("item.gtceu.behavior.spray_paint.orange.tooltip", "Can paint things in Orange");
        provider.add("item.gtceu.behavior.spray_paint.magenta.tooltip", "Can paint things in Magenta");
        provider.add("item.gtceu.behavior.spray_paint.light_blue.tooltip", "Can paint things in Light Blue");
        provider.add("item.gtceu.behavior.spray_paint.yellow.tooltip", "Can paint things in Yellow");
        provider.add("item.gtceu.behavior.spray_paint.lime.tooltip", "Can paint things in Lime");
        provider.add("item.gtceu.behavior.spray_paint.pink.tooltip", "Can paint things in Pink");
        provider.add("item.gtceu.behavior.spray_paint.gray.tooltip", "Can paint things in Gray");
        provider.add("item.gtceu.behavior.spray_paint.light_gray.tooltip", "Can paint things in Light Gray");
        provider.add("item.gtceu.behavior.spray_paint.cyan.tooltip", "Can paint things in Cyan");
        provider.add("item.gtceu.behavior.spray_paint.purple.tooltip", "Can paint things in Purple");
        provider.add("item.gtceu.behavior.spray_paint.blue.tooltip", "Can paint things in Blue");
        provider.add("item.gtceu.behavior.spray_paint.brown.tooltip", "Can paint things in Brown");
        provider.add("item.gtceu.behavior.spray_paint.green.tooltip", "Can paint things in Green");
        provider.add("item.gtceu.behavior.spray_paint.red.tooltip", "Can paint things in Red");
        provider.add("item.gtceu.behavior.spray_paint.black.tooltip", "Can paint things in Black");
        provider.add("item.gtceu.behavior.spray_paint.uses", "Remaining Uses: %d");

        // Copy/Paste Configuration
        provider.add("item.gtceu.behavior.memory_card.tooltip.copy",
                "§7Sneak + R-Click to copy configuration, or clear stored data if a block other than a machine or pipe is targeted.");
        provider.add("item.gtceu.behavior.memory_card.tooltip.paste", "§7R-Click to paste machine configuration");
        provider.add("item.gtceu.behavior.memory_card.tooltip.view_stored", "§8<Sneak to view stored configuration>");
        provider.add("item.gtceu.behavior.memory_card.client_msg.cleared", "Stored configuration cleared");
        provider.add("item.gtceu.behavior.memory_card.client_msg.copied", "Copied machine configuration");
        provider.add("item.gtceu.behavior.memory_card.client_msg.pasted", "Applied machine configuration");
        provider.add("item.gtceu.behavior.memory_card.client_msg.missing_items", "Missing items required to paste configuration");
        provider.add("item.gtceu.behavior.memory_card.tooltip.items_to_paste",
                "The following items are needed to paste this configuration:");
        provider.add("item.gtceu.behavior.memory_card.enabled", "§aEnabled§r");
        provider.add("item.gtceu.behavior.memory_card.disabled", "§cDisabled§r");
        provider.add("item.gtceu.behavior.memory_card.copy_target", "Copying: %s");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.item_io", "Item Output: %s (%s)");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.fluid_io", "Fluid Output: %s (%s)");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.auto_output", "§2Auto Output§r");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.allow_input", "§2Allow Input§r");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.auto_output_allow_input", "§2Auto Output/Allow Input§r");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.pipe_connections", "Pipe connections: %s");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.pipe_blocked_connections", "Pipe shuttered sides: %s");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.muffled", "Muffling %s");
        provider.add("item.gtceu.behavior.memory_card.setting.tooltip.circuit_config", "Programmed Circuit: ");

        // Prospector
        provider.add("item.prospector.mode.ores", "§aOre Prospection Mode§r");
        provider.add("item.prospector.mode.fluid", "§bFluid Prospection Mode§r");
        provider.add("item.prospector.mode.bedrock_ore", "§bBedrock Ore Prospection Mode§r");
        provider.add("item.prospector.tooltip.radius", "Scans range in a %s Chunk Radius");
        provider.add("item.prospector.tooltip.modes", "Available Modes:");
        provider.add("item.gtceu.behavior.prospector.not_enough_energy", "Not Enough Energy!");
        provider.add("item.gtceu.behavior.prospector.added_waypoint", "Created waypoint named %s!");
        
        provider.add("item.gtceu.behavior.portable_scanner.bedrock_fluid.amount", "Fluid In Deposit: %s %s - %s%%");
        provider.add("item.gtceu.behavior.portable_scanner.bedrock_fluid.amount_unknown", "Fluid In Deposit: %s%%");
        provider.add("item.gtceu.behavior.portable_scanner.bedrock_fluid.nothing", "Fluid In Deposit: §6Nothing§r");

        provider.add("item.gtceu.behavior.portable_scanner.environmental_hazard", "Environmental Hazard In Chunk: %s§r - %s ppm");
        provider.add("item.gtceu.behavior.portable_scanner.environmental_hazard.nothing",
                "Environmental Hazard In Chunk: §6Nothing§r");

        provider.add("item.gtceu.behavior.portable_scanner.local_hazard", "Local Hazard In Area: %s§r - %s ppm");
        provider.add("item.gtceu.behavior.portable_scanner.local_hazard.nothing", "Local Hazard In Area: §6Nothing§r");

        provider.add("item.gtceu.behavior.portable_scanner.block_hardness", "Hardness: %s Blast Resistance: %s");
        provider.add("item.gtceu.behavior.portable_scanner.block_name", "Name: %s MetaData: %s");

        provider.add("item.gtceu.behavior.portable_scanner.debug_cpu_load",
                "Average CPU load of ~%sns over %s ticks with worst time of %sns.");
        provider.add("item.gtceu.behavior.portable_scanner.debug_cpu_load_seconds", "This is %s seconds.");
        provider.add("item.gtceu.behavior.portable_scanner.debug_lag_count",
                "Caused %s Lag Spike Warnings (anything taking longer than %sms) on the Server.");
        provider.add("item.gtceu.behavior.portable_scanner.debug_machine", "Meta-ID: %s");
        provider.add("item.gtceu.behavior.portable_scanner.debug_machine_invalid", " invalid!");
        provider.add("item.gtceu.behavior.portable_scanner.debug_machine_valid", " valid");

        provider.add("item.gtceu.behavior.portable_scanner.divider", "=========================");

        provider.add("item.gtceu.behavior.portable_scanner.energy_container_in", "Max IN: %s (%s) EU at %s A");
        provider.add("item.gtceu.behavior.portable_scanner.energy_container_out", "Max OUT: %s (%s) EU at %s A");
        provider.add("item.gtceu.behavior.portable_scanner.energy_container_storage", "Energy: %s EU / %s EU");

        provider.add("item.gtceu.behavior.portable_scanner.eu_per_sec", "Average (last second): %s EU/t");
        provider.add("item.gtceu.behavior.portable_scanner.amp_per_sec", "Average (last second): %s A");
        provider.add("item.gtceu.behavior.portable_scanner.machine_disabled", "Disabled.");

        provider.add("item.gtceu.behavior.portable_scanner.machine_front_facing", "Front Facing: %s");
        provider.add("item.gtceu.behavior.portable_scanner.machine_upwards_facing", "Upwards Facing: %s");

        provider.add("item.gtceu.behavior.portable_scanner.machine_ownership", "§2Machine Owner Type: %s§r");
        provider.add("item.gtceu.behavior.portable_scanner.guild_name", "§2Guild Name: %s§r");
        provider.add("item.gtceu.behavior.portable_scanner.team_name", "§2Team Name: %s§r");
        provider.add("item.gtceu.behavior.portable_scanner.player_name", "§2Player Name: %s§r, §7Player Online: %s§r");

        provider.add("item.gtceu.behavior.portable_scanner.machine_power_loss", "Shut down due to power loss.");
        provider.add("item.gtceu.behavior.portable_scanner.machine_progress", "Progress/Load: %s / %s");

        provider.add("item.gtceu.behavior.portable_scanner.muffled", "Muffled.");

        provider.add("item.gtceu.behavior.portable_scanner.multiblock_energy_input",
                "Max Energy Income: %s EU/t Tier: %s");
        provider.add("item.gtceu.behavior.portable_scanner.multiblock_energy_output",
                "Max Energy Output: %s EU/t Tier: %s");
        provider.add("item.gtceu.behavior.portable_scanner.multiblock_maintenance", "Problems: %s");
        provider.add("item.gtceu.behavior.portable_scanner.multiblock_parallel", "Multi Processing: %s");

        provider.add("item.gtceu.behavior.portable_scanner.position", "----- X: %s Y: %s Z: %s D: %s -----");
        provider.add("item.gtceu.behavior.portable_scanner.state", "%s: %s");
        provider.add("item.gtceu.behavior.portable_scanner.tank", "Tank %s: %s mB / %s mB %s");
        provider.add("item.gtceu.behavior.portable_scanner.tanks_empty", "All Tanks Empty");

        provider.add("item.gtceu.behavior.portable_scanner.workable_consumption", "Probably Uses: %s EU/t at %s A");
        provider.add("item.gtceu.behavior.portable_scanner.workable_production", "Probably Produces: %s EU/t at %s A");
        provider.add("item.gtceu.behavior.portable_scanner.workable_progress", "Progress: %s s / %s s");
        provider.add("item.gtceu.behavior.portable_scanner.workable_stored_energy", "Stored Energy: %s EU / %s EU");

        provider.add("item.gtceu.behavior.portable_scanner.mode.caption", "Display mode: %s");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_all_info", "Show all info (excluding internal info)");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_block_info", "Show block info");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_machine_info", "Show machine info");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_electrical_info", "Show electrical info");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_recipe_info", "Show recipe info");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_environmental_info", "Show environmental info");
        provider.add("item.gtceu.behavior.portable_scanner.mode.show_internal_info", "Show internal debugging info");

        // Data Stick
        provider.add("item.gtceu.behavior.data_item.title", "§n%s Construction Data:");
        provider.add("item.gtceu.behavior.data_item.data", "- §a%s");

    }
}
