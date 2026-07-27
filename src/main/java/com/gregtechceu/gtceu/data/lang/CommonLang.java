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
        provider.add("common.gtceu.workable.disabled_next_cycle", "Working disabled after current cycle");

        // IO
        provider.add("common.gtceu.io.import", "Import");
        provider.add("common.gtceu.io.export", "Export");
        provider.add("common.gtceu.io.both", "Both");
        provider.add("common.gtceu.io.none", "None");

        // Voiding Mode
        provider.add("gtceu.gui.multiblock.voiding_mode", "Voiding Mode:");
        provider.add("gtceu.gui.item_voiding", "§7Voiding §6Items");
        provider.add("gtceu.gui.fluid_voiding", "§7Voiding §9Fluids");
        provider.add("gtceu.gui.all_voiding", "§7Voiding §cAll");
        provider.add("gtceu.gui.no_voiding", "§7Voiding Nothing");
    }

    public static void generateCommonLang(GTLangProvider provider) {

        provider.add("gtceu.universal.liters", "%s mB");
        provider.add("gtceu.universal.kiloliters", "%s B");

        provider.add("gtceu.gui.seconds", "%s second(s)");
        provider.add("gtceu.gui.years", "%s year(s)");

        provider.add("gtceu.universal.parentheses", "(%s)");
        provider.add("gtceu.universal.spaced_parentheses", "( %s )");
        provider.add("gtceu.universal.padded_parentheses", " (%s) ");
        provider.add("gtceu.universal.padded_spaced_parentheses", " ( %s ) ");

        provider.add("gtceu.tooltip.status.trinary.false", "False");
        provider.add("gtceu.tooltip.status.trinary.true", "True");
        provider.add("gtceu.tooltip.status.trinary.unknown", "Unknown");
    }

    private static void generateCommonTooltips(GTLangProvider provider) {
        // EU Tooltips
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

        // Tooltip Keybinds
        provider.add("gtceu.tooltip.hold_shift", "§7Hold SHIFT for more info");
        provider.add("gtceu.tooltip.hold_ctrl", "§7Hold CTRL for more info");
        provider.add("gtceu.tooltip.fluid_pipe_hold_shift", "§7Hold SHIFT to show Fluid Containment Info");
        provider.add("gtceu.tooltip.tool_fluid_hold_shift",
                "§7Hold SHIFT to show Fluid Containment and Tool Info");
    }

    private static void generateKeybindKeys(GTLangProvider provider) {
        provider.add("keybind.gtceu.armor_mode_switch", "Armor Mode Switch");
        provider.add("keybind.gtceu.armor_hover", "Armor Hover Toggle");
        provider.add("keybind.gtceu.enable_jetpack", "Enable Jetpack");
        provider.add("keybind.gtceu.enable_boots", "Enable Boosted Jump");
        provider.add("keybind.gtceu.armor_charging", "Armor Charging to Inventory Toggle");
        provider.add("keybind.gtceu.tool_aoe_change", "Tool AoE Mode Switch");
        provider.add("keybind.gtceu.enable_step_assist", "Enable StepAssist");
        provider.add("gtceu.debug.f3_h.enabled",
                "GregTech has modified the debug info! For Developers: enable the misc:debug config option in the GregTech config file to see more");
    }
}
