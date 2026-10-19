package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class CommonLang {

    public static void init(GTLangProvider provider) {
        generateEnumLang(provider);
        generateCommonLang(provider);
        generateCommonTooltips(provider);
        generateKeybindKeys(provider);
    }

    public static void generateEnumLang(GTLangProvider provider) {
        // Working status
        provider.add("common.gtceu.workable.enabled", "Working Enabled");
        provider.add("common.gtceu.workable.disabled", "Working Disabled");
        provider.add("common.gtceu.workable.cannot_disable", "Cannot be disabled");
        provider.add("common.gtceu.workable.disabled_next_cycle", "Working disabled after current cycle");

        // IO
        provider.add("common.gtceu.io.title", "IO Mode");
        provider.add("common.gtceu.io.import", "Import");
        provider.add("common.gtceu.io.export", "Export");
        provider.add("common.gtceu.io.both", "Both");
        provider.add("common.gtceu.io.none", "None");

        // Voiding Mode
        provider.add("common.gtceu.voiding_mode", "Voiding Mode:");
        provider.add("common.gtceu.voiding_mode.item", "§7Voiding §6Items");
        provider.add("common.gtceu.voiding_mode.fluid", "§7Voiding §9Fluids");
        provider.add("common.gtceu.voiding_mode.all", "§7Voiding §cAll");
        provider.add("common.gtceu.voiding_mode.none", "§7Voiding Nothing");
    }

    public static void generateCommonLang(GTLangProvider provider) {
        provider.add("common.gtceu.disabled", "Disabled");
        provider.add("common.gtceu.enabled", "Enabled");

        provider.add("common.gtceu.min", "Min");
        provider.add("common.gtceu.max", "Max");

        provider.add("common.gtceu.false", "False");
        provider.add("common.gtceu.true", "True");
        provider.add("common.gtceu.unknown", "Unknown");

        provider.add("common.gtceu.empty", "Empty");

        provider.add("common.gtceu.fluid_amount", "Fluid Amount: ");
        provider.add("common.gtceu.fluid_stored", "Fluid Stored: ");
        provider.add("common.gtceu.transfer_rate", "Transfer Rate: ");
        provider.add("common.gtceu.item_slots", "Item Slots: ");
        provider.add("common.gtceu.item_capacity", "Item Capacity: ");
        provider.add("common.gtceu.item_stored", "Item Stored: ");
        provider.add("common.gtceu.energy_capacity", "Energy Capacity: ");

        // Units & Amounts

        provider.add("common.gtceu.millibucket", "mB");
        provider.add("common.gtceu.bucket", "B");
        provider.add("common.gtceu.mb_per_tick", "mB/t");

        provider.add("common.gtceu.amperage", "A");
        provider.add("common.gtceu.eu", "EU");
        provider.add("common.gtceu.eu_per_tick", "EU/t");
        provider.add("common.gtceu.eu_per_sec", "EU/s");

        // Base mc has gui.minutes/hours/days
        provider.add("common.gtceu.seconds", "%s second(s)");
        provider.add("common.gtceu.years", "%s year(s)");
    }

    private static void generateCommonTooltips(GTLangProvider provider) {
        // EU Tooltips
        provider.add("common.gtceu.tooltip.voltage_in", "Voltage IN: ");
        provider.add("common.gtceu.tooltip.voltage_out", "Voltage OUT: ");
        provider.add("common.gtceu.tooltip.voltage_both", "Voltage IN/OUT: ");

        provider.add("common.gtceu.tooltip.max_voltage_in", "Max Voltage IN: ");
        provider.add("common.gtceu.tooltip.max_voltage_out", "Max Voltage OUT: ");
        provider.add("common.gtceu.tooltip.max_voltage_both", "Max Voltage IN/OUT: ");

        provider.add("common.gtceu.tooltip.amperage_in", "Amperage IN: ");
        provider.add("common.gtceu.tooltip.amperage_out", "Amperage OUT: ");
        provider.add("common.gtceu.tooltip.amperage_both", "Amperage IN/OUT: ");

        provider.add("common.gtceu.tooltip.amperage_up_to_in", "Amperage IN up to: ");
        provider.add("common.gtceu.tooltip.amperage_up_to_out", "Amperage OUT up to: ");
        provider.add("common.gtceu.tooltip.amperage_up_to_both", "Amperage IN/OUT up to: ");

        // EU Storage
        provider.add("common.gtceu.tooltip.energy_storage_capacity", "§cEnergy Capacity: §r%d EU");
        provider.add("common.gtceu.tooltip.energy_tier_range", "§aAllowed Voltage Tiers: §f%s §f- %s");

        // Item Storage/Transfer
        provider.add("common.gtceu.tooltip.item_stored", "%s items");
        provider.add("common.gtceu.tooltip.item_transfer_rate", "§bTransfer Rate: §f%d items/s");
        provider.add("common.gtceu.tooltip.item_transfer_rate_stacks", "§bTransfer Rate: §f%d stacks/s");

        // Fluid Storage/Transfer
        provider.add("common.gtceu.tooltip.fluid_storage_capacity_mult",
                "%s §7Tanks, §f%s mB §7each");

        // General Consumption
        provider.add("common.gtceu.tooltip.uses_per_tick", "Uses §f%d EU/t §7while working");
        provider.add("common.gtceu.tooltip.uses_per_tick_steam", "Uses §f%d mB/t §7of §fSteam §7while working");

        // General Production
        provider.add("common.gtceu.tooltip.base_production_eut", "§eBase Production: §f%d EU/t");
        provider.add("common.gtceu.tooltip.base_production_fluid", "§eBase Production: §f%d mB/t");
        provider.add("common.gtceu.tooltip.produces_fluid", "§eProduces: §f%d mB/t");

        // Tooltip Keybinds
        provider.add("common.gtceu.tooltip.hold_shift", "Hold SHIFT for more info");
        provider.add("common.gtceu.tooltip.fluid_pipe_hold_shift", "Hold SHIFT to show Fluid Containment Info");
        provider.add("common.gtceu.tooltip.tool_fluid_hold_shift",
                "Hold SHIFT to show Fluid Containment and Tool Info");
    }

    private static void generateKeybindKeys(GTLangProvider provider) {
        provider.add("keybind.gtceu.armor_mode_switch", "Armor Mode Switch");
        provider.add("keybind.gtceu.armor_hover", "Armor Hover Toggle");
        provider.add("keybind.gtceu.enable_jetpack", "Enable Jetpack");
        provider.add("keybind.gtceu.enable_boots", "Enable Boosted Jump");
        provider.add("keybind.gtceu.armor_charging", "Armor Charging to Inventory Toggle");
        provider.add("keybind.gtceu.tool_aoe_change", "Tool AoE Mode Switch");
        provider.add("keybind.gtceu.enable_step_assist", "Enable StepAssist");
    }
}
