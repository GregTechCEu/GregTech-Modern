package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class CommonLang {

    public static void init(GTLangProvider provider) {
        generateEnumLang(provider);
        generateCommonLang(provider);
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
