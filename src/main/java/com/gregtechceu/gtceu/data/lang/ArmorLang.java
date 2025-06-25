package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;

public class ArmorLang {

    public static void init(RegistrateLangProvider provider) {
        generateArmorKeys(provider);
        generateTooltipKeys(provider);
        generateHudKeys(provider);
    }

    private static void generateArmorKeys(RegistrateLangProvider provider) {
        // NanoMuscle Suit
        provider.add("armor.gtceu.nanomusclesuite.nightvision.enabled", "NanoMuscle™ Suite: §aNightVision Enabled");
        provider.add("armor.gtceu.nanomusclesuite.nightvision.disabled", " NanoMuscle™ Suite: §cNightVision Disabled");
        provider.add("armor.gtceu.nanomusclesuite.boosted_jump.enabled", "NanoMuscle™ Suite: §aJump Boost Enabled");
        provider.add("armor.gtceu.nanomusclesuite.boosted_jump.disabled", " NanoMuscle™ Suite: §cJump Boost Disabled");
        provider.add("armor.gtceu.nanomusclesuite.nightvision.error", "NanoMuscle™ Suite: §cNot enough power!");
        provider.add("armor.gtceu.nanomusclesuite.charge.enabled", "NanoMuscle™ Suite: §aCharging Enabled");
        provider.add("armor.gtceu.nanomusclesuite.charge.disable", " NanoMuscle™ Suite: §cCharging Disabled");
        provider.add("armor.gtceu.nanomusclesuite.charge.error", "NanoMuscle™ Suite: §cNot enough power for charging!");

        // Quantum Suit
        provider.add("armor.gtceu.quarktechsuite.nightvision.enabled", "QuarkTech™ Suite: §aNightVision Enabled");
        provider.add("armor.gtceu.quarktechsuite.nightvision.disabled", "QuarkTech™ Suite: §cNightVision Disabled");
        provider.add("armor.gtceu.quarktechsuite.nightvision.error", "QuarkTech™ Suite: §cNot enough power!");
        provider.add("armor.gtceu.quarktechsuite.charge.enabled", "QuarkTech™ Suite: §aCharging Enabled");
        provider.add("armor.gtceu.quarktechsuite.charge.disable", "QuarkTech™ Suite: §cCharging Disabled");
        provider.add("armor.gtceu.quarktechsuite.charge.error", "QuarkTech™ Suite: §cNot enough power for charging!");

        // Jetpacks
        provider.add("armor.gtceu.jetpack.flight.enable", "Jetpack: Flight Enabled");
        provider.add("armor.gtceu.jetpack.flight.disable", "Jetpack: Flight Disabled");
        provider.add("armor.gtceu.jetpack.hover.enable", "Jetpack: Hover Mode Enabled");
        provider.add("armor.gtceu.jetpack.hover.disable", "Jetpack: Hover Mode Disabled");
        provider.add("armor.gtceu.jetpack.emergency_hover_mode", "§aEmergency Hover Mode Enabled!");

        // Night Vision
        provider.add("armor.gtceu.message.nightvision.enabled", "NightVision: §aOn");
        provider.add("armor.gtceu.message.nightvision.disabled", "NightVision: §cOff");
        provider.add("armor.gtceu.message.nightvision.error", "Not enough power!");
    }

    private static void generateTooltipKeys(RegistrateLangProvider provider) {
        provider.add("item.liquid_fuel_jetpack.tooltip", "Uses Combustion Generator Fuels for Thrust");

        // Armor Abilities
        provider.add("armor.gtceu.tooltip.stepassist", "Provides Step-Assist");
        provider.add("armor.gtceu.tooltip.speed", "Increases Running Speed");
        provider.add("armor.gtceu.tooltip.jump", "Increases Jump Height and Distance");
        provider.add("armor.gtceu.tooltip.falldamage", "Nullifies Fall Damage");
        provider.add("armor.gtceu.tooltip.potions", "Nullifies Harmful Effects");
        provider.add("armor.gtceu.tooltip.burning", "Nullifies Burning");
        provider.add("armor.gtceu.tooltip.freezing", "Prevents Freezing");
        provider.add("armor.gtceu.tooltip.breath", "Replenishes Underwater Breath Bar");
        provider.add("armor.gtceu.tooltip.autoeat", "Replenishes Food Bar by Using Food from Inventory");

        // Energy Tooltips (iaddinfo)
        provider.add("armor.gtceu.energy_share.error", "Energy Supply: §cNot enough power for gadgets charging!");
        provider.add("armor.gtceu.energy_share.enable", "§aEnergy Supply: Gadgets charging enabled");
        provider.add("armor.gtceu.energy_share.disable", "Energy Supply: Gadgets charging disabled");
        provider.add("armor.gtceu.energy_share.tooltip", "Supply mode: %s");
        provider.add("armor.gtceu.energy_share.tooltip.guide",
                "To change mode shift-right click when holding item");
    }

    private static void generateHudKeys(RegistrateLangProvider provider) {
        // HUD Information
        provider.add("armor.gtceu.hud.status.enabled", "§aON");
        provider.add("armor.gtceu.hud.status.disabled", "§cOFF");
        provider.add("armor.gtceu.hud.energy_lvl", "Energy Level: %s");
        provider.add("armor.gtceu.hud.engine_enabled", "§aEngine Enabled: %s");
        provider.add("armor.gtceu.hud.fuel_lvl", "Fuel Level: %s");
        provider.add("armor.gtceu.hud.hover_mode", "Hover Mode: %s");
        provider.add("mataarmor.hud.supply_mode", "Supply Mode: %s");
        provider.add("armor.gtceu.hud.gravi_engine", "GraviEngine: %s");
    }
}
