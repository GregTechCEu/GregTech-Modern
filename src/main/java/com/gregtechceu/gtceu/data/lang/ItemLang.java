package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class ItemLang {

    public static void init(GTLangProvider provider) {
        initItemTooltips(provider);
        generateBehaviorKeys(provider);
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
