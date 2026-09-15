package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ModularArmorLang {

    public static void init(RegistrateLangProvider provider) {
        provider.add("tooltip.gtceu.configure_modular_armor", "Press [%s] when holding to configure modular item");

        provider.add("metaitem.liquid_fuel_jetpack.tooltip", "Uses Combustion Generator Fuels for Thrust");
        provider.add("metaarmor.nms.nightvision.enabled", "NanoMuscle™ Suite: NightVision Enabled");
        provider.add("metaarmor.nms.nightvision.disabled", "NanoMuscle™ Suite: NightVision Disabled");
        provider.add("metaarmor.nms.nightvision.error", "NanoMuscle™ Suite: §cNot enough power!");
        provider.add("metaarmor.qts.nightvision.enabled", "QuarkTech™ Suite: NightVision Enabled");
        provider.add("metaarmor.qts.nightvision.disabled", "QuarkTech™ Suite: NightVision Disabled");
        provider.add("metaarmor.qts.nightvision.error", "QuarkTech™ Suite: §cNot enough power!");
        provider.add("metaarmor.nms.step_assist.disabled", "NanoMuscle™ Suite: StepAssist Disabled");
        provider.add("metaarmor.nms.step_assist.enabled", "NanoMuscle™ Suite: StepAssist Enabled");
        provider.add("metaarmor.qts.step_assist.disabled", "QuarkTech™ Suite: StepAssist Disabled");
        provider.add("metaarmor.qts.step_assist.enabled", "QuarkTech™ Suite: StepAssist Enabled");
        provider.add("metaarmor.qts.boosted_jump.enabled", "QuarkTech™ Suite: Jump Boost Enabled");
        provider.add("metaarmor.qts.boosted_jump.disabled", "QuarkTech™ Suite: Jump Boost Disabled");
        provider.add("metaarmor.jetpack.flight.enable", "Jetpack: Flight Enabled");
        provider.add("metaarmor.jetpack.flight.disable", "Jetpack: Flight Disabled");
        provider.add("metaarmor.jetpack.hover.enable", "Jetpack: Hover Mode Enabled");
        provider.add("metaarmor.jetpack.hover.disable", "Jetpack: Hover Mode Disabled");
        provider.add("metaarmor.jetpack.emergency_hover_mode", "Emergency Hover Mode Enabled!");
        provider.add("metaarmor.nms.share.enable", "NanoMuscle™ Suite: Charging Enabled");
        provider.add("metaarmor.nms.share.disable", "NanoMuscle™ Suite: Charging Disabled");
        provider.add("metaarmor.nms.share.error", "NanoMuscle™ Suite: §cNot enough power for charging!");
        provider.add("metaarmor.qts.share.enable", "QuarkTech™ Suite: Charging Enabled");
        provider.add("metaarmor.qts.share.disable", "QuarkTech™ Suite: Charging Disabled");
        provider.add("metaarmor.qts.share.error", "QuarkTech™ Suite: §cNot enough power for charging!");
        provider.add("metaarmor.message.nightvision.enabled", "§bNightVision: §aOn");
        provider.add("metaarmor.message.nightvision.disabled", "§bNightVision: §cOff");
        provider.add("metaarmor.message.nightvision.error", "§cNot enough power!");
        provider.add("metaarmor.message.step_assist.enabled", "§bStep-Assist: §aOn");
        provider.add("metaarmor.message.step_assist.disabled", "§bStep-Assist: §cOff");
        provider.add("metaarmor.tooltip.stepassist", "Provides Step-Assist");
        provider.add("metaarmor.tooltip.speed", "Increases Running Speed");
        provider.add("metaarmor.tooltip.jump", "Increases Jump Height and Distance");
        provider.add("metaarmor.tooltip.falldamage", "Nullifies Fall Damage");
        provider.add("metaarmor.tooltip.potions", "Nullifies Harmful Effects");
        provider.add("metaarmor.tooltip.burning", "Nullifies Burning");
        provider.add("metaarmor.tooltip.freezing", "Prevents Freezing");
        provider.add("metaarmor.tooltip.breath", "Replenishes Underwater Breath Bar");
        provider.add("metaarmor.tooltip.autoeat", "Replenishes Food Bar by Using Food from Inventory");
        provider.add("metaarmor.tooltip.modifier.speed", "Running Speed Module (%s)");
        provider.add("metaarmor.tooltip.modifier.jump", "Jump Module (%s)");
        provider.add("metaarmor.tooltip.modifier.damage_block", "Energy Shield Module (%s)");
        provider.add("metaarmor.tooltip.modifier.attack_speed", "Attack Speed Module (%s)");
        provider.add("metaarmor.tooltip.modifier.attack_damage", "Attack Damage Module (%s)");
        provider.add("metaarmor.tooltip.modifier.battery", "Battery Module (%s)");
        provider.add("metaarmor.tooltip.modifier.block_reach", "Reach Module (%s)");
        provider.add("metaarmor.tooltip.modifier.jetpack", "Jetpack Module (%s)");
        provider.add("metaarmor.tooltip.modifier.movement_speed", "Movement Speed Module (%s)");
        provider.add("metaarmor.tooltip.modifier.sneak_speed", "Sneak Speed Module (%s)");
        provider.add("metaarmor.tooltip.modifier.step_height", "Step Height Module (%s)");
        provider.add("metaarmor.tooltip.modifier.swim_speed", "Swim Speed Module (%s)");
        provider.add("metaarmor.tooltip.modifier.sensor", "Explosion Reporting Module (%s)");
        provider.add("metaarmor.tooltip.modifier.wireless_charging", "Wireless Charging Module (%s)");
        provider.add("metaarmor.tooltip.modifier.creative_flight", "Creative Flight Module");
        provider.add("metaarmor.tooltip.modifier.battery.hud.info", "Storing %s/%s EU");
        provider.add("metaarmor.tooltip.modifier.battery.hud.helmet", "Helmet battery: %s");
        provider.add("metaarmor.tooltip.modifier.battery.hud.chestplate", "Chestplate battery: %s");
        provider.add("metaarmor.tooltip.modifier.battery.hud.leggings", "Leggings battery: %s");
        provider.add("metaarmor.tooltip.modifier.battery.hud.boots", "Boots battery: %s");
        provider.add("metaarmor.tooltip.modifier.fluid_storage", "Additional fluid storage (%s)");
        provider.add("metaarmor.tooltip.modifier.fluid_storage.tooltip", "Storing %s/%s mB of %s");
        provider.add("metaarmor.tooltip.modifier", " - %s: %s");
        provider.add("metaarmor.tooltip.modifier.empty", "Empty");
        provider.add("metaarmor.tooltip.modifier_slot.universal", "Universal");
        provider.add("metaarmor.tooltip.modifier_slot.tiered", "Tiered (%s)");
        provider.add("metaarmor.tooltip.modifiers", "Module slots:");
        provider.add("gtceu.modules", "Modules:");
        provider.add("gtceu.machine.exploded", "%s exploded at (%d, %d, %d)");
        provider.add("metaarmor.hud.status.enabled", "§aON");
        provider.add("metaarmor.hud.status.disabled", "§cOFF");
        provider.add("metaarmor.hud.energy_lvl", "Energy Level: %s");
        provider.add("metaarmor.hud.engine_enabled", "Engine Enabled: %s");
        provider.add("metaarmor.hud.fuel_lvl", "Fuel Level: %s");
        provider.add("metaarmor.hud.hover_mode", "Hover Mode: %s");
        provider.add("mataarmor.hud.supply_mode", "Supply Mode: %s");
        provider.add("metaarmor.hud.gravi_engine", "GraviEngine: %s");
        provider.add("metaarmor.energy_share.error", "Energy Supply: §cNot enough power for gadgets charging!");
        provider.add("metaarmor.energy_share.enable", "Energy Supply: Gadgets charging enabled");
        provider.add("metaarmor.energy_share.disable", "Energy Supply: Gadgets charging disabled");
        provider.add("metaarmor.energy_share.tooltip", "Supply mode: %s");
        provider.add("metaarmor.energy_share.tooltip.guide",
                "To change mode shift-right click when holding item");
        provider.add("gtceu.module.speed", "Increases sprint speed by %s%%");
        provider.add("gtceu.module.jump", "Increases jump height by %s blocks");
        provider.add("gtceu.module.damage_block",
                "Blocks any amount of damage with EU (%s EU per HP, requires any battery module)");
        provider.add("gtceu.module.attack_speed", "Increases attack speed by %s%%");
        provider.add("gtceu.module.attack_damage", "Increases attack damage by %s%%");
        provider.add("gtceu.module.battery",
                "Allows the item to store energy or increases its capacity (capacity is equal to the battery's)");
        provider.add("gtceu.module.block_reach", "Increases block reach by %s blocks");
        provider.add("gtceu.module.liquid_fuel_jetpack",
                "Makes the chestplate work like a liquid fueled jetpack (requires any fluid storage module)");
        provider.add("gtceu.module.jetpack", "Makes the chestplate work like a jetpack (requires any battery module)");
        provider.add("gtceu.module.advanced_jetpack",
                "Makes the chestplate work like an advanced jetpack (requires any battery module)");
        provider.add("gtceu.module.movement_speed", "Increases all movement speed by %s%%");
        provider.add("gtceu.module.sneak_speed", "Increases sneaking speed by %s%%");
        provider.add("gtceu.module.step_height", "Increases step height by %s blocks");
        provider.add("gtceu.module.swim_speed", "Increases swim speed by %s%%");
        provider.add("gtceu.module.sensor", "Reports machine explosions (for machines placed by the player)");
        provider.add("gtceu.module.wireless_charging",
                "Allows wireless charging in the range of %s blocks (1A %s max), bind to a charger by right-clicking on it");
        provider.add("gtceu.module.wireless_charging.interdimensional",
                "%s blocks, 1A %s max, interdimensional if an at least %s field generator is present");
        provider.add("gtceu.module.creative_flight", "Allows creative flight (consumes %s EU/t while flying)");
        provider.add("gtceu.module.fluid_storage",
                "Allows the item to store liquids or increases its capacity (capacity is equal to the tank's)");
        provider.add("gtceu.module.nightvision", "Provides night vision (consumes %s EU/t while active)");
        provider.add("gtceu.module.ppe",
                "Provides protection from environmental hazards if applied to all armor pieces");
        provider.add("gtceu.module.damage_block.short", "%s EU per HP");
        provider.add("gtceu.module.short_percentage", "... by %s%%");
        provider.add("gtceu.module.wireless_charging.short", "%s blocks, 1A %s max");
        provider.add("gtceu.module.block_reach.short", "... by %s blocks");
        provider.add("gtceu.module.gui.enabled", "Enabled:");
        provider.add("gtceu.module.gui.jump_boost", "Boost:");
        provider.add("gtceu.module.gui.power", "Power:");
        provider.add("gtceu.module.gui.charge", "Charge:");
        provider.add("gtceu.module.gui.energy_limit", "Energy limit:");
        provider.add("gtceu.module.gui.hp", "HP:");
        provider.add("gtceu.module.gui.select_an_item", "Select an item");
        provider.add("gtceu.module.gui.invalid_item", "This item does not accept modules");
    }
}
