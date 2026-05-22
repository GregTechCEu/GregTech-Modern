package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class ArmorLang {

    public static void init(GTLangProvider provider) {
        generateArmorMessageKeys(provider);
        generateTooltipKeys(provider);
        generateHudKeys(provider);

        provider.add("item.gtceu.armor.helmet", "%s Helmet");
        provider.add("item.gtceu.armor.chestplate", "%s Chestplate");
        provider.add("item.gtceu.armor.leggings", "%s Leggings");
        provider.add("item.gtceu.armor.boots", "%s Boots");
    }

    private static void generateArmorMessageKeys(GTLangProvider provider) {
        // NanoMuscle Suit
        provider.add("armor.gtceu.nano_muscle_suite.nightvision.enabled", "NanoMuscle™ Suite: §aNightVision Enabled");
        provider.add("armor.gtceu.nano_muscle_suite.nightvision.disabled",
                " NanoMuscle™ Suite: §cNightVision Disabled");
        provider.add("armor.gtceu.nano_muscle_suite.step_assist.disabled", "NanoMuscle™ Suite: StepAssist Disabled");
        provider.add("armor.gtceu.nano_muscle_suite.step_assist.enabled", "NanoMuscle™ Suite: StepAssist Enabled");
        provider.add("armor.gtceu.nano_muscle_suite.nightvision.error", "NanoMuscle™ Suite: §cNot enough power!");
        provider.add("armor.gtceu.nano_muscle_suite.charge.enabled", "NanoMuscle™ Suite: §aCharging Enabled");
        provider.add("armor.gtceu.nano_muscle_suite.charge.disabled", " NanoMuscle™ Suite: §cCharging Disabled");
        provider.add("armor.gtceu.nano_muscle_suite.charge.error",
                "NanoMuscle™ Suite: §cNot enough power for charging!");

        // Quantum Suit
        provider.add("armor.gtceu.quark_tech_suite.nightvision.enabled", "QuarkTech™ Suite: §aNightVision Enabled");
        provider.add("armor.gtceu.quark_tech_suite.nightvision.disabled", "QuarkTech™ Suite: §cNightVision Disabled");
        provider.add("armor.gtceu.quark_tech_suite.nightvision.error", "QuarkTech™ Suite: §cNot enough power!");
        provider.add("armor.gtceu.quark_tech_suite.charge.enabled", "QuarkTech™ Suite: §aCharging Enabled");
        provider.add("armor.gtceu.quark_tech_suite.charge.disabled", "QuarkTech™ Suite: §cCharging Disabled");
        provider.add("armor.gtceu.quark_tech_suite.charge.error", "QuarkTech™ Suite: §cNot enough power for charging!");
        provider.add("armor.gtceu.quark_tech_suite.step_assist.disabled", "QuarkTech™ Suite: StepAssist Disabled");
        provider.add("armor.gtceu.quark_tech_suite.step_assist.enabled", "QuarkTech™ Suite: StepAssist Enabled");
        provider.add("armor.gtceu.quark_tech_suite.boosted_jump.enabled", "QuarkTech™ Suite: Jump Boost Enabled");
        provider.add("armor.gtceu.quark_tech_suite.boosted_jump.disabled", "QuarkTech™ Suite: Jump Boost Disabled");

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

    private static void generateTooltipKeys(GTLangProvider provider) {
        // move this to item lang?
        provider.add("item.gtceu.liquid_fuel_jetpack.tooltip", "§7Uses Combustion Generator Fuels for Thrust");

        // Armor Abilities
        provider.add("armor.gtceu.tooltip.step_assist.enabled", "§bStep-Assist: §aOn");
        provider.add("armor.gtceu.tooltip.step_assist.disabled", "§bStep-Assist: §cOff");
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

    private static void generateHudKeys(GTLangProvider provider) {
        // HUD Information
        provider.add("armor.gtceu.hud.status.enabled", "§aON"); // change this to generic tooltip?
        provider.add("armor.gtceu.hud.status.disabled", "§cOFF");
        provider.add("armor.gtceu.hud.energy_level", "Energy Level: %s");
        provider.add("armor.gtceu.hud.engine_enabled", "Engine: %s");
        provider.add("armor.gtceu.hud.fuel_level", "Fuel Level: %s");
        provider.add("armor.gtceu.hud.hover_mode", "Hover Mode: %s");
        provider.add("armor.gtceu.hud.supply_mode", "Supply Mode: %s");
        provider.add("armor.gtceu.hud.gravi_engine", "GraviEngine: %s");
    }
}
