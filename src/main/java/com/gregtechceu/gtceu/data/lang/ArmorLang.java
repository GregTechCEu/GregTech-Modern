package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ArmorLang {

    public static void init(RegistrateLangProvider provider) {
        generateArmorMessageKeys(provider);
        generateTooltipKeys(provider);
        generateHudKeys(provider);
    }

    private static void generateArmorMessageKeys(RegistrateLangProvider provider) {
        // NanoMuscle Suit
        provider.add("armor.gtceu.nano_muscle_suite.nightvision.enabled", "NanoMuscle™ Suite: §aNightVision Enabled");
        provider.add("armor.gtceu.nano_muscle_suite.nightvision.disabled", " NanoMuscle™ Suite: §cNightVision Disabled");
        provider.add("armor.gtceu.nano_muscle_suite.boosted_jump.enabled", "NanoMuscle™ Suite: §aJump Boost Enabled");
        provider.add("armor.gtceu.nano_muscle_suite.boosted_jump.disabled", " NanoMuscle™ Suite: §cJump Boost Disabled");
        provider.add("armor.gtceu.nano_muscle_suite.nightvision.error", "NanoMuscle™ Suite: §cNot enough power!");
        provider.add("armor.gtceu.nano_muscle_suite.charge.enabled", "NanoMuscle™ Suite: §aCharging Enabled");
        provider.add("armor.gtceu.nano_muscle_suite.charge.disabled", " NanoMuscle™ Suite: §cCharging Disabled");
        provider.add("armor.gtceu.nano_muscle_suite.charge.error", "NanoMuscle™ Suite: §cNot enough power for charging!");

        // Quantum Suit
        provider.add("armor.gtceu.quark_tech_suite.nightvision.enabled", "QuarkTech™ Suite: §aNightVision Enabled");
        provider.add("armor.gtceu.quark_tech_suite.nightvision.disabled", "QuarkTech™ Suite: §cNightVision Disabled");
        provider.add("armor.gtceu.quark_tech_suite.nightvision.error", "QuarkTech™ Suite: §cNot enough power!");
        provider.add("armor.gtceu.quark_tech_suite.charge.enabled", "QuarkTech™ Suite: §aCharging Enabled");
        provider.add("armor.gtceu.quark_tech_suite.charge.disabled", "QuarkTech™ Suite: §cCharging Disabled");
        provider.add("armor.gtceu.quark_tech_suite.charge.error", "QuarkTech™ Suite: §cNot enough power for charging!");

        // Jetpacks
        provider.add("armor.gtceu.jetpack.flight.enabled", "Jetpack: §aFlight Enabled");
        provider.add("armor.gtceu.jetpack.flight.disabled", "Jetpack: Flight Disabled");
        provider.add("armor.gtceu.jetpack.hover.enabled", "Jetpack: §aHover Mode Enabled");
        provider.add("armor.gtceu.jetpack.hover.disabled", "Jetpack: Hover Mode Disabled");
        provider.add("armor.gtceu.jetpack.emergency_hover_mode", "§aEmergency Hover Mode Enabled!");

        // Night Vision Goggles
        provider.add("armor.gtceu.message.nightvision.enabled", "§7NightVision: §aOn");
        provider.add("armor.gtceu.message.nightvision.disabled", "§7NightVision: §cOff");
        provider.add("armor.gtceu.message.nightvision.error", "§cNot enough power!");
    }

    private static void generateTooltipKeys(RegistrateLangProvider provider) {
        provider.add("item.gtceu.liquid_fuel_jetpack.tooltip", "§7Uses Combustion Generator Fuels for Thrust"); //move this to item lang?

        // Armor Abilities
        provider.add("armor.gtceu.tooltip.stepassist", "§7Provides Step-Assist");
        provider.add("armor.gtceu.tooltip.speed", "§7Increases Running Speed");
        provider.add("armor.gtceu.tooltip.jump", "§7Increases Jump Height and Distance");
        provider.add("armor.gtceu.tooltip.falldamage", "§7Nullifies Fall Damage");
        provider.add("armor.gtceu.tooltip.potions", "§7Nullifies Harmful Effects");
        provider.add("armor.gtceu.tooltip.burning", "§7Nullifies Burning");
        provider.add("armor.gtceu.tooltip.freezing", "§7Prevents Freezing");
        provider.add("armor.gtceu.tooltip.breath", "§7Replenishes Underwater Breath Bar");
        provider.add("armor.gtceu.tooltip.autoeat", "§7Replenishes Food Bar by Using Food from Inventory");

        // Energy Tooltips (iaddinfo)
        provider.add("armor.gtceu.energy_share.error", "§cNot Enough Power for Charging Gadgets!");
        provider.add("armor.gtceu.energy_share.enabled", "§aGadget Charging Enabled");
        provider.add("armor.gtceu.energy_share.disabled", "§cGadget Charging Disabled");
        provider.add("armor.gtceu.energy_share.tooltip", "Supply Mode: %s");
        provider.add("armor.gtceu.energy_share.tooltip.info", "To change mode, SHIT + R-CLICK when holding item");
    }

    private static void generateHudKeys(RegistrateLangProvider provider) {
        // HUD Information
        provider.add("armor.gtceu.hud.status.enabled", "§aON"); //change this to generic tooltip?
        provider.add("armor.gtceu.hud.status.disabled", "§cOFF");
        provider.add("armor.gtceu.hud.energy_level", "Energy Level: %s");
        provider.add("armor.gtceu.hud.engine_enabled", "Engine: %s");
        provider.add("armor.gtceu.hud.fuel_level", "Fuel Level: %s");
        provider.add("armor.gtceu.hud.hover_mode", "Hover Mode: %s");
        provider.add("armor.gtceu.hud.supply_mode", "Supply Mode: %s");
        provider.add("armor.gtceu.hud.gravi_engine", "GraviEngine: %s");
    }
}
