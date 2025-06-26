package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class GUILang {

    public static void init(GTLangProvider provider) {
        generateKeybindKeys(provider);
        generateWidgetKeys(provider);
        generateTooltipKeys(provider);
        generateLDLibKeys(provider);
    }

    private static void generateLDLibKeys(GTLangProvider provider) {
        provider.add("gtceu.gui.editor.tips.citation", "Number of citations");
        provider.add("gtceu.gui.editor.group.recipe_type", "cap");
        provider.add("ldlib.gui.editor.register.editor.gtceu.rtui", "RecipeType UI Project");
        provider.add("ldlib.gui.editor.register.editor.gtceu.mui", "Machine UI Project");
        provider.add("ldlib.gui.editor.register.editor.gtceu.template_tab", "templates");
        provider.add("ldlib.gui.editor.group.widget.gtm_container", "GTM Container Widgets");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_item_slot", "GTM Item Slot");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_fluid_slot", "GTM Fluid Slot");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_phantom_item_slot", "GTM Phantom Item Slot");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_phantom_fluid_slot", "GTM Phantom Fluid Slot");
    }

    private static void generateKeybindKeys(GTLangProvider provider) {
        provider.add("gtceu.key.armor_mode_switch", "Armor Mode Switch");
        provider.add("gtceu.key.armor_hover", "Armor Hover Toggle");
        provider.add("gtceu.key.enable_jetpack", "Enable Jetpack");
        provider.add("gtceu.key.enable_boots", "Enable Boosted Jump");
        provider.add("gtceu.key.armor_charging", "Armor Charging to Inventory Toggle");
        provider.add("gtceu.key.tool_aoe_change", "Tool AoE Mode Switch");
        provider.add("gtceu.debug.f3_h.enabled",
                "GregTech has modified the debug info! For Developers: enable the misc:debug config option in the GregTech config file to see more");
    }

    private static void generateTooltipKeys(GTLangProvider provider) {
        // Part Sharing
        provider.add("gtceu.part_sharing.disabled", "Multiblock Sharing §4Disabled");
        provider.add("gtceu.part_sharing.enabled", "Multiblock Sharing §aEnabled");

        // GUI Units
        provider.add("gtceu.universal.liters", "%s mB");
        provider.add("gtceu.universal.kiloliters", "%s B");

        // EU Tooltip
        provider.add("gtceu.universal.tooltip.voltage_in", "§aVoltage IN: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.max_voltage_in", "§aMax Voltage IN: §f%d (%s§f)");
        provider.add("gtceu.universal.tooltip.voltage_out", "§aVoltage OUT: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.max_voltage_out", "§aMax Voltage OUT: §f%d (%s§f)");
        provider.add("gtceu.universal.tooltip.voltage_in_out", "§aVoltage IN/OUT: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.max_voltage_in_out", "§aMax Voltage IN/OUT: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.amperage_in", "§eAmperage IN: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_in_till", "§eAmperage IN up to: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_out", "§eAmperage OUT: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_out_till", "§eAmperage OUT up to: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_in_out", "§eAmperage IN/OUT: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_in_out_till", "§eAmperage IN/OUT up to: §f%dA");

        // EU Storage
        provider.add("gtceu.universal.tooltip.energy_storage_capacity", "§cEnergy Capacity: §r%d EU");
        provider.add("gtceu.universal.tooltip.energy_tier_range", "§aAllowed Voltage Tiers: §f%s §f- %s");

        provider.add("gtceu.gui.seconds", "%s second(s)");
        provider.add("gtceu.gui.years", "%s year(s)");

        // Item Storage/Transfer
        provider.add("gtceu.universal.tooltip.item_storage_capacity", "§6Item Slots: §f%d");
        provider.add("gtceu.universal.tooltip.item_storage_total", "§6Item Capacity: §f%d items");
        provider.add("gtceu.universal.tooltip.item_stored", "§dItem Stored: §f%s, %d items");
        provider.add("gtceu.universal.tooltip.item_transfer_rate", "§bTransfer Rate: §f%d items/s");
        provider.add("gtceu.universal.tooltip.item_transfer_rate_stacks", "§bTransfer Rate: §f%d stacks/s");

        // Fluid Storage/Transfer
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity", "§9Fluid Capacity: §f%d mB");
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                "§9Fluid Capacity: §f%d §7Tanks, §f%d mB §7each");
        provider.add("gtceu.universal.tooltip.fluid_stored", "§2Fluid Stored: §f%s, %d mB");
        provider.add("gtceu.universal.tooltip.fluid_transfer_rate", "§bTransfer Rate: §f%d mB/t");

        // Parallel
        provider.add("gtceu.universal.tooltip.parallel", "§dMax Parallel: §f%d");

        // Miner GUI
        provider.add("gtceu.universal.tooltip.working_area", "§bWorking Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.chunk_mode", "Chunk Mode: ");
        provider.add("gtceu.universal.tooltip.silk_touch", "Silk Touch: ");
        provider.add("gtceu.universal.tooltip.working_area_chunks", "§bWorking Area: §f%dx%d Chunks");
        provider.add("gtceu.universal.tooltip.working_area_max", "§bMax Working Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.working_area_chunks_max", "§bMax Working Area: §f%dx%d Chunks");

        // General Consumption
        provider.add("gtceu.universal.tooltip.uses_per_tick", "Uses §f%d EU/t §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_tick_steam", "Uses §f%d mB/t §7of §fSteam §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_hour_lubricant",
                "Uses §f%d mB/hr §7of §6Lubricant §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_second", "Uses §f%d EU/s §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_op", "Uses §f%d EU/operation");

        // General Production
        provider.add("gtceu.universal.tooltip.base_production_eut", "§eBase Production: §f%d EU/t");
        provider.add("gtceu.universal.tooltip.base_production_fluid", "§eBase Production: §f%d mB/t");
        provider.add("gtceu.universal.tooltip.produces_fluid", "§eProduces: §f%d mB/t");

        // Explosion
        provider.add("gtceu.universal.tooltip.terrain_resist",
                "This Machine will not explode when exposed to the Elements");

        // Redstone
        provider.add("gtceu.universal.tooltip.requires_redstone", "§4Requires Redstone power");

        // DEPRECATED
        provider.add("gtceu.universal.tooltip.deprecated",
                "§4§lWARNING:§r§4 DEPRECATED. WILL BE REMOVED IN A FUTURE VERSION.§r");

        // Tooltip Keybinds
        provider.add("gtceu.tooltip.hold_shift", "§7Hold SHIFT for more info");
        provider.add("gtceu.tooltip.hold_ctrl", "§7Hold CTRL for more info");
        provider.add("gtceu.tooltip.fluid_pipe_hold_shift", "§7Hold SHIFT to show Fluid Containment Info");
        provider.add("gtceu.tooltip.tool_fluid_hold_shift",
                "§7Hold SHIFT to show Fluid Containment and Tool Info");

        // Trinary
        provider.add("gtceu.tooltip.status.trinary.false", "False");
        provider.add("gtceu.tooltip.status.trinary.true", "True");
        provider.add("gtceu.tooltip.status.trinary.unknown", "Unknown");

        // Directionality
        provider.add("gtceu.direction.tooltip.up", "Up");
        provider.add("gtceu.direction.tooltip.down", "Down");
        provider.add("gtceu.direction.tooltip.left", "Left");
        provider.add("gtceu.direction.tooltip.right", "Right");
        provider.add("gtceu.direction.tooltip.back", "Back");
        provider.add("gtceu.direction.tooltip.front", "Front");
    }

    private static void generateWidgetKeys(GTLangProvider provider) {
        // General UI Keys
        provider.add("gtceu.gui.title_bar.back", "Back");
        provider.add("gtceu.gui.title_bar.page_switcher", "Pages");

        provider.add("gtceu.gui.fuel_amount", "Fuel Amount:");
        provider.add("gtceu.gui.fluid_amount", "Fluid Amount:");

        provider.add("gtceu.gui.toggle_view.disabled", "Toggle View (Fluids)");
        provider.add("gtceu.gui.toggle_view.enabled", "Toggle View (Items)");

        provider.addMultiline("gtceu.gui.overclock.enabled", "Overclocking Enabled.\nClick to Disable");
        provider.addMultiline("gtceu.gui.overclock.disabled", "Overclocking Disabled.\nClick to Enable");
        provider.addMultiline("gtceu.gui.overclock.description",
                "Overclock Button\n§7Recipes can overclock up to the set tier");
        provider.add("gtceu.gui.overclock.off", "X");

        provider.add("gtceu.gui.sort", "Sort");

        provider.add("gtceu.gui.fluid_auto_output.tooltip.enabled", "Fluid Auto-Output Enabled");
        provider.add("gtceu.gui.fluid_auto_output.tooltip.disabled", "Fluid Auto-Output Disabled");
        provider.add("gtceu.gui.fluid_auto_input.tooltip.enabled", "Fluid Auto-Input Enabled");
        provider.add("gtceu.gui.fluid_auto_input.tooltip.disabled", "Fluid Auto-Input Disabled");

        provider.add("gtceu.gui.item_auto_output.tooltip.enabled", "Item Auto-Output Enabled");
        provider.add("gtceu.gui.item_auto_output.tooltip.disabled", "Item Auto-Output Disabled");
        provider.add("gtceu.gui.item_auto_input.tooltip.enabled", "Item Auto-Input Enabled");
        provider.add("gtceu.gui.item_auto_input.tooltip.disabled", "Item Auto-Input Disabled");

        provider.addMultiline("gtceu.gui.charger_slot.tooltip",
                "§fCharger Slot§r\n§7Draws power from %s batteries§r\n§7Charges %s tools and batteries");
        provider.addMultiline("gtceu.gui.configurator_slot.tooltip",
                "§fConfigurator Slot§r\n§7Place a §6Programmed Circuit§7 in this slot to\n§7change its configured value.\n§7Hold §6Shift§7 when clicking buttons to change by §65.\n§aA Programmed Circuit in this slot is also valid for recipe inputs.§r");

        provider.add("gtceu.gui.fluid_lock.tooltip.enabled", "Fluid Locking Enabled");
        provider.add("gtceu.gui.fluid_lock.tooltip.disabled", "Fluid Locking Disabled");

        provider.add("gtceu.gui.fluid_voiding_partial.tooltip.enabled", "Fluid Voiding Enabled");
        provider.add("gtceu.gui.fluid_voiding_partial.tooltip.disabled", "Fluid Voiding Disabled");

        provider.add("gtceu.gui.item_lock.tooltip.enabled", "Item Locking Enabled");
        provider.add("gtceu.gui.item_lock.tooltip.disabled", "Item Locking Disabled");

        provider.add("gtceu.gui.item_voiding_partial.tooltip.enabled", "Item Voiding Enabled");
        provider.add("gtceu.gui.item_voiding_partial.tooltip.disabled", "Item Voiding Disabled");

        // Miner GUI
        provider.addMultiline("gtceu.gui.silktouch.enabled",
                "Silk Touch Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        provider.addMultiline("gtceu.gui.silktouch.disabled",
                "Silk Touch Disabled: Click to Enable.\n§7Switching requires an idle machine.");
        provider.addMultiline("gtceu.gui.chunkmode.enabled",
                "Chunk Mode Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        provider.addMultiline("gtceu.gui.chunkmode.disabled",
                "Chunk Mode Disabled: Click to Enable.\n§7Switching requires an idle machine.");

        // Recipe Capability Voiding
        provider.addMultiline("gtceu.gui.multiblock_item_voiding", "Voiding Mode\n§7Voiding §6Items");
        provider.addMultiline("gtceu.gui.multiblock_fluid_voiding", "Voiding Mode\n§7Voiding §9Fluids");
        provider.addMultiline("gtceu.gui.multiblock_item_fluid_voiding",
                "Voiding Mode\n§7Voiding §6Items §7and §9Fluids");
        provider.addMultiline("gtceu.gui.multiblock_no_voiding", "Voiding Mode\n§7Voiding Nothing");

        // Fisher
        provider.addMultiline("gtceu.gui.fisher_mode.tooltip",
                "Toggle junk items\nOff costs 2 string per operation");

        // Multiblock Page
        provider.add("gtceu.multiblock.page_switcher.io.import", "§2Inputs");
        provider.add("gtceu.multiblock.page_switcher.io.export", "§4Outputs");
        provider.add("gtceu.multiblock.page_switcher.io.both", "§5Combined Inputs + Outputs");

        // Generic Buttons
        provider.add("gui.widget.incrementButton.default_tooltip",
                "Hold Shift, Ctrl or both to change the amount");
        provider.add("gui.widget.recipeProgressWidget.default_tooltip", "Show Recipes");

        // NBT Clearing
        provider.add("gtceu.universal.clear_nbt_recipe.tooltip", "§cThis will destroy all contents!");

        // EIO style gui
        provider.add("gtceu.gui.cover_setting.title", "Cover Settings");
        provider.add("gtceu.gui.output_setting.title", "Output Settings");
        provider.add("gtceu.gui.circuit.title", "Circuit Settings");
        provider.addMultiLang("gtceu.gui.output_setting.tooltips", "left-click to tune the item auto output",
                "right-click to tune the fluid auto output.");
        provider.add("gtceu.gui.item_auto_output.allow_input.enabled",
                "allow items input from the output side");
        provider.add("gtceu.gui.item_auto_output.allow_input.disabled",
                "disable items input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.allow_input.enabled",
                "allow fluids input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.allow_input.disabled",
                "disable fluids input from the output side");
        provider.add("gtceu.gui.auto_output.name", "auto");

        // Overclocking Widget
        provider.add("gtceu.gui.overclock.title", "Overclock Tier");
        provider.add("gtceu.gui.overclock.range", "Available Tiers [%s, %s]");

        // Content
        provider.add("gtceu.gui.content.per_tick", "§aConsumed/Produced Per Tick§r");
        provider.add("gtceu.gui.content.tips.per_tick_short", "§a/tick§r");
        provider.add("gtceu.gui.content.tips.per_second_short", "§a/second§r");

        provider.add("gtceu.gui.content.units.per_tick", "/t");
        provider.add("gtceu.gui.content.units.per_second", "/s");

        // Recipe Logic
        provider.addMultiLang("gtceu.oc.tooltip", "Min: %s", "Left click to increase the OC",
                "Right click to decrease the OC", "Middle click to reset the OC",
                "Hold Shift to change by Perfect OC");

        // Recipe Capabilities
        provider.add("recipe.capability.eu.name", "GTCEu Energy");
        provider.add("recipe.capability.fluid.name", "Fluid");
        provider.add("recipe.capability.item.name", "Item");
        provider.add("gtceu.recipe_type.show_recipes", "Show Recipes");
        provider.add("gtceu.recipe_logic.insufficient_fuel", "Insufficient Fuel");
        provider.add("gtceu.recipe_logic.insufficient_in", "Insufficient Inputs");
        provider.add("gtceu.recipe_logic.insufficient_out", "Insufficient Outputs");
        provider.add("gtceu.recipe_logic.condition_fails", "Condition Fails");
        provider.add("gtceu.recipe_logic.no_contents", "Recipe has no Contents");
        provider.add("gtceu.recipe_logic.no_capabilities", "Machine has no Capabilities");

        // Chance Logic
        provider.add("gtceu.gui.content.chance_nc", "§cNot Consumed§r");
        provider.add("gtceu.gui.content.chance_nc_short", "§cNC§r");
        provider.add("gtceu.gui.content.chance_base", "Base Chance: %s%%");
        provider.add("gtceu.gui.content.chance_base_logic", "Base Chance: %s%% (%s)");
        provider.add("gtceu.gui.content.chance_tier_boost_plus", "Bonus Chance: +%s%%/tier");
        provider.add("gtceu.gui.content.chance_tier_boost_minus", "Bonus Chance: -%s%%/tier");
        provider.add("gtceu.gui.content.chance_boosted", "Chance at Tier: %s%%");
        provider.add("gtceu.gui.content.chance_boosted_logic", "Chance at Tier: %s%% (%s)");
        provider.add("gtceu.gui.content.count_range", "%s-%sx");

        provider.add("gtceu.chance_logic.or", "OR");
        provider.add("gtceu.chance_logic.and", "AND");
        provider.add("gtceu.chance_logic.xor", "XOR");
        provider.add("gtceu.chance_logic.first", "FIRST");
        provider.add("gtceu.chance_logic.none", "NONE");

        // AE Widgets
        provider.add("gtceu.gui.me_network.online", "Network Status: §2Online§r");
        provider.add("gtceu.gui.me_network.offline", "Network Status: §4Offline§r");
        provider.add("gtceu.gui.waiting_list", "Sending Queue:");
        provider.add("gtceu.gui.config_slot", "§fConfig Slot§r");
        provider.add("gtceu.gui.config_slot.set", "§7Click to §bset/select§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.scroll", "§7Scroll wheel to §achange§7 config amount.§r");
        provider.add("gtceu.gui.config_slot.remove", "§7Right click to §4clear§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.set_only", "§7Click to §bset§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.auto_pull_managed", "§4Disabled:§7 Managed by Auto-Pull");
        provider.add("gtceu.gui.me_bus.auto_pull_button", "Click to toggle automatic item pulling from ME");
    }
}
