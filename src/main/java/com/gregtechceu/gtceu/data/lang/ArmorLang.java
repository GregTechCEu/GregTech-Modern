package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class ArmorLang {

    public static void init(GTLangProvider provider) {
        generateArmorMessageKeys(provider);
        generateHudKeys(provider);

        provider.add("item.gtceu.armor.helmet", "%s Helmet");
        provider.add("item.gtceu.armor.chestplate", "%s Chestplate");
        provider.add("item.gtceu.armor.leggings", "%s Leggings");
        provider.add("item.gtceu.armor.boots", "%s Boots");
    }

    private static void generateArmorMessageKeys(GTLangProvider provider) {
        // Jetpacks
        provider.add("armor.gtceu.jetpack.flight.enabled", "Jetpack: §aFlight Enabled");
        provider.add("armor.gtceu.jetpack.flight.disabled", "Jetpack: Flight Disabled");
        provider.add("armor.gtceu.jetpack.hover.enabled", "Jetpack: §aHover Mode Enabled");
        provider.add("armor.gtceu.jetpack.hover.disabled", "Jetpack: Hover Mode Disabled");
        provider.add("armor.gtceu.jetpack.emergency_hover_mode", "§aEmergency Hover Mode Enabled!");

        // Night Vision Goggles
        provider.add("armor.gtceu.message.night_vision.enabled", "§7NightVision: §aOn");
        provider.add("armor.gtceu.message.night_vision.disabled", "§7NightVision: §cOff");
        provider.add("armor.gtceu.message.night_vision.error", "§7NightVision Goggles: §cNot enough power!");
    }

    private static void generateHudKeys(GTLangProvider provider) {
        // HUD Information
        provider.add("armor.gtceu.hud.enabled", "§aON"); // change this to generic tooltip?
        provider.add("armor.gtceu.hud.disabled", "§cOFF");
        provider.add("armor.gtceu.hud.energy_level", "Energy Level: %s");
        provider.add("armor.gtceu.hud.engine", "Engine: %s");
        provider.add("armor.gtceu.hud.fuel_level", "Fuel Level: %s");
        provider.add("armor.gtceu.hud.hover_mode", "Hover Mode: %s");
        provider.add("armor.gtceu.hud.supply_mode", "Supply Mode: %s");
        provider.add("armor.gtceu.hud.gravi_engine", "GraviEngine: %s");
    }
}
