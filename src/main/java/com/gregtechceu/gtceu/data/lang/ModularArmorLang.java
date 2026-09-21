package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class ModularArmorLang {

    public static void init(RegistrateLangProvider provider) {
        provider.add("tooltip.gtceu.configure_modular_armor", "Press [%s] when holding to configure modular item");

        provider.add("gui.gtceu.equipment_foundry.applied_to", "Applied to:");
        provider.add("gui.gtceu.equipment_foundry.module_item", "Module item:");
        provider.add("gui.gtceu.equipment_foundry.supports_tiers", "Module tiers: %s-%s");
        provider.add("gui.gtceu.equipment_foundry.tier", "Viewing stats for tier: %s");
        provider.add("gui.gtceu.equipment_foundry.tier_too_high", "No module item exists for this tier");
        provider.add("gui.gtceu.equipment_foundry.tooltip.tier_switch",
                "Left-click to increase tier by 1\nRight-click to decrease tier by 1");

        provider.add("armor.gtceu.jetpack.flight.enabled", "Jetpack: Flight Enabled");
        provider.add("armor.gtceu.jetpack.flight.disabled", "Jetpack: Flight Disabled");
        provider.add("armor.gtceu.jetpack.hover.enabled", "Jetpack: Hover Mode Enabled");
        provider.add("armor.gtceu.jetpack.hover.disabled", "Jetpack: Hover Mode Disabled");
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
        provider.add("gui.gtceu.module_slots", "Module slots:");

        // Module names/descriptions

        provider.add("module.gtceu.speed", "Running Speed Module (%s)");
        provider.add("module.gtceu.speed.description", "Increases sprint speed by %s%%");
        provider.add("module.gtceu.jump_boost", "Jump Module (%s)");
        provider.add("module.gtceu.jump_boost.description", "Increases jump height by %s blocks");
        provider.add("module.gtceu.damage_block", "Energy Shield Module (%s)");
        provider.add("module.gtceu.damage_block.description",
                "Blocks any amount of damage with EU (%s EU per HP, requires any battery module)");
        provider.add("module.gtceu.attack_speed", "Attack Speed Module (%s)");
        provider.add("module.gtceu.attack_speed.description", "Increases attack speed by %s%%");
        provider.add("module.gtceu.attack_damage", "Attack Speed Module (%s)");
        provider.add("module.gtceu.attack_damage.description", "Increases attack damage by %s%%");
        provider.add("module.gtceu.block_reach", "Reach Module (%s)");
        provider.add("module.gtceu.block_reach.description", "Increases block reach by %s blocks");
        provider.add("module.gtceu.movement_speed", "Movement Speed Module (%s)");
        provider.add("module.gtceu.movement_speed.description", "Increases all movement speed by %s%%");
        provider.add("module.gtceu.sneak_speed", "Sneak Speed Module (%s)");
        provider.add("module.gtceu.sneak_speed.description", "Increases sneaking speed by %s%%");
        provider.add("module.gtceu.step_height", "Step Height Module (%s)");
        provider.add("module.gtceu.step_height.description", "Increases step height by %s blocks");
        provider.add("module.gtceu.swim_speed", "Swim Speed Module (%s)");
        provider.add("module.gtceu.swim_speed.description", "Increases swim speed by %s%%");
        
        provider.add("module.gtceu.battery", "Battery Module (%s)");
        provider.add("module.gtceu.battery.description",
                "Allows the item to store energy or increases its capacity (capacity is equal to the battery's)");
        provider.add("module.gtceu.battery.hud.info", "Storing %s/%s EU");
        provider.add("module.gtceu.battery.hud.helmet", "Helmet battery: %s");
        provider.add("module.gtceu.battery.hud.chestplate", "Chestplate battery: %s");
        provider.add("module.gtceu.battery.hud.leggings", "Leggings battery: %s");
        provider.add("module.gtceu.battery.hud.boots", "Boots battery: %s");

        provider.add("module.gtceu.air_supplier", "Air Supplier Module");
        provider.add("module.gtceu.air_supplier.description", "Replenishes Breath Bar");
        provider.add("module.gtceu.auto_eat", "Auto Eat Module");
        provider.add("module.gtceu.auto_eat.description", "Replenishes Food Bar by Using Food from Inventory");

        
        provider.add("module.gtceu.liquid_fuel_jetpack", "Liquid Fuel Jetpack");
        provider.add("module.gtceu.liquid_fuel_jetpack.description",
                "Makes the chestplate work like a liquid fueled jetpack (requires any fluid storage module)");
        provider.add("module.gtceu.jetpack", "Basic Jetpack");
        provider.add("module.gtceu.jetpack.description", "Makes the chestplate work like a jetpack (requires any battery module)");
        provider.add("module.gtceu.advanced_jetpack", "Advanced Jetpack");
        provider.add("module.gtceu.advanced_jetpack.description",
                "Makes the chestplate work like an advanced jetpack (requires any battery module)");
        provider.add("module.gtceu.sensor", "Explosion Reporting Module (%s)");
        provider.add("module.gtceu.sensor.message", "%s exploded at (%d, %d, %d)");
        provider.add("module.gtceu.sensor.description", "Reports machine explosions (for machines placed by the player)");
        provider.add("module.gtceu.wireless_charging", "Wireless Charging Module (%s)");
        provider.add("module.gtceu.wireless_charging.description",
                "Allows wireless charging in the range of %s blocks (1A %s max), bind to a charger by right-clicking on it");
        provider.add("module.gtceu.wireless_charging.description.interdimensional",
                "%s blocks, 1A %s max, interdimensional if an at least %s field generator is present");
        provider.add("module.gtceu.creative_flight", "Creative Flight Module");
        provider.add("module.gtceu.creative_flight.description", "Allows creative flight (consumes %s EU/t while flying)");

        provider.add("module.gtceu.fluid_storage", "Fluid Storage (%s)");
        provider.add("module.gtceu.fluid_storage.description",
                "Allows the item to store liquids or increases its capacity (capacity is equal to the tank's)");
        provider.add("module.gtceu.fluid_storage.current_stored", "Storing %s/%s mB of %s");

        provider.add("module.gtceu.night_vision", "Night Vision Module");
        provider.add("module.gtceu.night_vision.description", "Provides night vision (consumes %s EU/t while active)");

        provider.add("module.gtceu.ppe", "PPE Module");
        provider.add("module.gtceu.ppe.description",
                "Provides protection from environmental hazards if applied to all armor pieces");

        ///
        provider.add("metaarmor.tooltip.potions", "Nullifies Harmful Effects");
        provider.add("metaarmor.tooltip.burning", "Nullifies Burning");
        provider.add("metaarmor.tooltip.freezing", "Prevents Freezing");


        provider.add("module.gtceu.damage_block.short", "%s EU per HP");
        provider.add("module.gtceu.short_percentage", "... by %s%%");
        provider.add("module.gtceu.wireless_charging.short", "%s blocks, 1A %s max");
        provider.add("module.gtceu.block_reach.short", "... by %s blocks");
        provider.add("gui.gtceu.item_module.enabled", "Enabled:");
        provider.add("gui.gtceu.item_module.jump_boost", "Boost:");
        provider.add("gui.gtceu.item_module.power", "Power:");
        provider.add("gui.gtceu.item_module.charge", "Charge:");
        provider.add("gui.gtceu.item_module.energy_limit", "Energy limit:");
        provider.add("gui.gtceu.item_module.hp", "HP:");
    }
}
