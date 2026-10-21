package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ModularArmorLang {

    public static void init(RegistrateLangProvider provider) {
        provider.add("gui.gtceu.item_module.jump_height_value", "+%s blocks");
        provider.add("gui.gtceu.item_module.hover_mode", "Hover Mode");
        provider.add("gtceu.key.modular_item_gui", "Armor Configuration");
        provider.add("gui.gtceu.armor_config.title", "Armor Configuration");
        provider.add("gui.gtceu.armor_config.select", "Select an equipped armor piece");
        provider.add("gui.gtceu.armor_config.no_modules", "No modules installed");
        provider.add("gui.gtceu.armor_config.helmet", "Helmet");
        provider.add("gui.gtceu.armor_config.chestplate", "Chestplate");
        provider.add("gui.gtceu.armor_config.charge_inventory", "Charge Inventory");
        provider.add("gui.gtceu.armor_config.leggings", "Leggings");
        provider.add("gui.gtceu.armor_config.boots", "Boots");

        provider.add("item.gtceu.netherite_plated", "Netherite-Plated %s");
        provider.add("tooltip.gtceu.netherite_plated", "Netherite Plating: +2 Module Slots");
        provider.add("tooltip.gtceu.configure_modular_armor", "Press [%s] to configure equipped armor");

        provider.add("gui.gtceu.equipment_foundry.applied_to", "Applied to:");
        provider.add("gui.gtceu.equipment_foundry.module_item", "Module item:");
        provider.add("gui.gtceu.equipment_foundry.supports_tiers", "Module tiers: %s-%s");
        provider.add("gui.gtceu.equipment_foundry.tier", "Viewing stats for tier: %s");
        provider.add("gui.gtceu.equipment_foundry.tier_too_high", "No module item exists for this tier");
        provider.add("gui.gtceu.equipment_foundry.tooltip.tier_switch",
                "Left-click to increase tier by 1\nRight-click to decrease tier by 1");

        provider.add("armor.gtceu.jetpack.flight.enabled", "Thruster: Flight Enabled");
        provider.add("armor.gtceu.jetpack.flight.disabled", "Thruster: Flight Disabled");
        provider.add("armor.gtceu.jetpack.hover.enabled", "Thruster: Hover Mode Enabled");
        provider.add("armor.gtceu.jetpack.hover.disabled", "Thruster: Hover Mode Disabled");
        provider.add("armor.gtceu.jetpack.emergency_hover", "Emergency Hover Mode Enabled!");

        provider.add("armor.gtceu.night_vision.enabled", "§bNightVision: §aOn");
        provider.add("armor.gtceu.night_vision.disabled", "§bNightVision: §cOff");
        provider.add("armor.gtceu.night_vision.error", "NightVision: §cNot enough power!");

        provider.add("hud.gtceu.armor.status.enabled", "§aON");
        provider.add("hud.gtceu.armor.status.disabled", "§cOFF");
        provider.add("hud.gtceu.armor.energy_lvl", "Energy Level: %s");
        provider.add("hud.gtceu.armor.engine_enabled", "Engine Enabled: %s");
        provider.add("hud.gtceu.armor.fuel_lvl", "Fuel Level: %s");
        provider.add("hud.gtceu.armor.hover_mode", "Hover Mode: %s");

        provider.add("gui.gtceu.item_module.empty_module_slot", "Empty");
        provider.add("gui.gtceu.module_slot.universal", "Universal");
        provider.add("gui.gtceu.module_slot.tiered", "Tiered (%s)");
        provider.add("gui.gtceu.max_module_tier", "Max Module Tier: %s");
        provider.add("gui.gtceu.module_slots", "Module slots:");
        provider.add("gui.gtceu.modules", "Modules:");

        provider.add("tooltip.gtceu.armor.built_in_traits", "Built-in Traits:");
        provider.add("tooltip.gtceu.armor.trait.fall", "Fall protection");
        provider.add("tooltip.gtceu.armor.trait.fire", "Fire immunity");
        provider.add("tooltip.gtceu.armor.trait.freeze", "Freeze protection");
        provider.add("tooltip.gtceu.armor.trait.heat", "Reduces damage from hot ingots by %s%%");
        provider.add("tooltip.gtceu.armor.trait.hazard", "Hazard protection");

        // Module names/descriptions

        provider.add("module.gtceu.speed", "Running Speed Module (%s)");
        provider.add("module.gtceu.speed.description",
                "Increases sprint speed by %1$s%%\nUses %2$s EU/t while sprinting.");
        provider.add("module.gtceu.jump_boost", "Jump Module (%s)");
        provider.add("module.gtceu.jump_boost.description",
                "Increases jump height by %1$s blocks\nUses %2$s EU per jump.");
        provider.add("module.gtceu.damage_block", "Energy Shield Module (%s)");
        provider.add("module.gtceu.damage_block.description",
                "Blocks any amount of damage with EU (%s EU per HP, requires any battery module)");
        provider.add("module.gtceu.attack_speed", "Attack Speed Module (%s)");
        provider.add("module.gtceu.attack_speed.description",
                "Increases attack speed by %1$s%%\nUses %2$s EU per melee hit.");
        provider.add("module.gtceu.attack_damage", "Attack Damage Module (%s)");
        provider.add("module.gtceu.attack_damage.description",
                "Increases attack damage by %1$s%%\nUses %2$s EU per melee hit.");
        provider.add("module.gtceu.block_reach", "Reach Module (%s)");
        provider.add("module.gtceu.block_reach.description",
                "Increases block reach by %1$s blocks\nUses %2$s EU/t while enabled.");
        provider.add("module.gtceu.movement_speed", "Movement Speed Module (%s)");
        provider.add("module.gtceu.movement_speed.description",
                "Increases movement speed by %1$s%%\nUses %2$s EU/t while moving.");
        provider.add("module.gtceu.sneak_speed", "Sneak Speed Module (%s)");
        provider.add("module.gtceu.sneak_speed.description",
                "Sneaking speed: %1$s%% of walking speed before Swift Sneak. Capped at 100%%, including Swift Sneak.\nUses %2$s EU/t while moving crouched or crawling.");
        provider.add("module.gtceu.step_height", "Step Assist Module (%s)");
        provider.add("module.gtceu.step_height.description",
                "Increases step height by %1$s blocks\nUses %2$s EU/t while moving.");
        provider.add("module.gtceu.swim_speed", "Swim Speed Module (%s)");
        provider.add("module.gtceu.swim_speed.description",
                "Increases swim speed by %1$s%%\nUses %2$s EU/t while swimming.");

        provider.add("module.gtceu.battery", "Battery Module (%s)");
        provider.add("module.gtceu.battery.description",
                "Allows the item to store energy or increases its capacity (capacity is equal to the battery's)");
        provider.add("module.gtceu.battery.hud.suit", "Armor Power");
        provider.add("module.gtceu.battery.hud.info", "Storing %s/%s EU");
        provider.add("module.gtceu.battery.hud.helmet", "Helmet battery: %s");
        provider.add("module.gtceu.battery.hud.chestplate", "Chestplate battery: %s");
        provider.add("module.gtceu.battery.hud.leggings", "Leggings battery: %s");
        provider.add("module.gtceu.battery.hud.boots", "Boots battery: %s");

        provider.add("module.gtceu.air_supplier", "Air Supplier Module");
        provider.add("module.gtceu.air_supplier.description",
                "Replenishes the breath bar for %s EU per refill");
        provider.add("module.gtceu.auto_eat", "Auto Eat Module");
        provider.add("module.gtceu.auto_eat.description",
                "Replenishes the food bar using food from inventory for %s EU for each item consumed");

        provider.add("module.gtceu.liquid_fuel_jetpack", "Liquid-Fueled Thruster");
        provider.add("module.gtceu.liquid_fuel_jetpack.description",
                "Provides thrust using liquid fuel (requires a fluid storage module, does not work with Creative Flight Modules)");
        provider.add("module.gtceu.jetpack", "Electric Thruster");
        provider.add("module.gtceu.jetpack.description",
                "Provides thrust for %s EU/t. Sprint during Creative Flight for 1.3x speed (requires a battery module).");
        provider.add("module.gtceu.advanced_jetpack", "Advanced Electric Thruster");
        provider.add("module.gtceu.advanced_jetpack.description",
                "Provides thrust for %1$s EU/t, or %2$s EU/t while sprinting. Sprint during Creative Flight for 2.4x speed (requires a battery module).");
        provider.add("module.gtceu.sensor", "Explosion Reporting Module (%s)");
        provider.add("module.gtceu.sensor.message", "%s exploded at (%d, %d, %d)");
        provider.add("module.gtceu.sensor.description",
                "Reports machine explosions (for machines placed by the player)");
        provider.add("module.gtceu.wireless_charger", "Quantum Charger (%s)");
        provider.add("module.gtceu.wireless_charger.description",
                "Links armor to a Battery Buffer or Power Substation across any distance and dimension (up to %s, configurable 1-16A). Right-click to link to storage.");
        provider.add("module.gtceu.creative_flight", "Creative Flight Module");
        provider.add("module.gtceu.creative_flight.description",
                "Uses %s EU/t to fly, can be used with thrusters for extra movement speed.");

        provider.add("module.gtceu.fluid_storage", "Fluid Storage");
        provider.add("module.gtceu.fluid_storage.description",
                "Allows the item to store liquids or increases its capacity (capacity is equal to the tank's)");
        provider.add("module.gtceu.fluid_storage.current_stored", "Storing %s/%s mB of %s");

        provider.add("module.gtceu.night_vision", "Night Vision Module");
        provider.add("module.gtceu.night_vision.description",
                "Provides night vision for %s EU/t while active.");

        provider.add("module.gtceu.ppe", "PPE Module");
        provider.add("module.gtceu.ppe.description",
                "Provides protection from environmental hazards if applied to all armor pieces");

        provider.add("metaarmor.tooltip.potions", "Nullifies Harmful Effects");
        provider.add("metaarmor.tooltip.burning", "Nullifies Burning");
        provider.add("metaarmor.tooltip.freezing", "Prevents Freezing");

        provider.add("module.gtceu.damage_block.short", "%s EU per HP");
        provider.add("module.gtceu.short_percentage", "... by %s%%");
        provider.add("module.gtceu.wireless_charging.short", "Unlimited range, interdimensional, 1A %s max");
        provider.add("module.gtceu.block_reach.short", "... by %s blocks");
        provider.add("gui.gtceu.item_module.enabled", "Enabled:");
        provider.add("gui.gtceu.item_module.jump_boost", "Boost:");
        provider.add("gui.gtceu.item_module.power", "Power:");
        provider.add("gui.gtceu.item_module.charge", "Charge:");
        provider.add("gui.gtceu.item_module.energy_limit", "Energy limit:");
        provider.add("gui.gtceu.item_module.hp", "HP:");
    }
}
