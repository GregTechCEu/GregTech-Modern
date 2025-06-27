package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;
import com.gregtechceu.gtceu.common.data.GTBlocks;

public class BlockLang {

    public static void init(GTLangProvider provider) {
        generateBlockKeys(provider);
        generateBlockTooltips(provider);
        generatePipeKeys(provider);
    }

    private static void generateBlockKeys(GTLangProvider provider) {
        // Coils
        provider.add("block.gtceu.hssg_coil_block", "HSS-G Coil Block");
        provider.add("block.gtceu.rtm_alloy_coil_block", "RTM Alloy Coil Block");

        // Substation Capacitors
        provider.add(GTBlocks.BATTERY_EMPTY_TIER_I.get().getDescriptionId(), "Empty Tier I Capacitor");
        provider.add(GTBlocks.BATTERY_EMPTY_TIER_II.get().getDescriptionId(), "Empty Tier II Capacitor");
        provider.add(GTBlocks.BATTERY_EMPTY_TIER_III.get().getDescriptionId(), "Empty Tier III Capacitor");

        provider.add(GTBlocks.BATTERY_LAPOTRONIC_EV.get().getDescriptionId(), "EV Lapotronic Capacitor");
        provider.add(GTBlocks.BATTERY_LAPOTRONIC_IV.get().getDescriptionId(), "IV Lapotronic Capacitor");
        provider.add(GTBlocks.BATTERY_LAPOTRONIC_LuV.get().getDescriptionId(), "LuV Lapotronic Capacitor");
        provider.add(GTBlocks.BATTERY_LAPOTRONIC_ZPM.get().getDescriptionId(), "ZPM Lapotronic Capacitor");
        provider.add(GTBlocks.BATTERY_LAPOTRONIC_UV.get().getDescriptionId(), "UV Lapotronic Capacitor");
        provider.add(GTBlocks.BATTERY_ULTIMATE_UHV.get().getDescriptionId(), "UHV Ultimate Capacitor");

        // Casings
        provider.add("block.gtceu.bronze_brick_casing", "Bricked Bronze Casing");
        provider.add("block.gtceu.steel_brick_casing", "Bricked Wrought Iron Casing");
        provider.add("block.gtceu.heatproof_machine_casing", "Heat Proof Invar Machine Casing");
        provider.add("block.gtceu.frostproof_machine_casing", "Frost Proof Aluminium Machine Casing");
        provider.add("block.gtceu.steel_machine_casing", "Solid Steel Machine Casing");
        provider.add("block.gtceu.clean_machine_casing", "Clean Stainless Steel Casing");
        provider.add("block.gtceu.stable_machine_casing", "Stable Titanium Machine Casing");
        provider.add("block.gtceu.robust_machine_casing", "Robust Tungstensteel Machine Casing");
        provider.add("block.gtceu.casing_coke_bricks", "Coke Oven Bricks");
        provider.add("block.gtceu.inert_machine_casing", "Chemically Inert PTFE Machine Casing");
        provider.add("block.gtceu.sturdy_machine_casing", "Sturdy HSS-E Machine Casing");
        provider.add("block.gtceu.casing_grate", "Grate Machine Casing");
        provider.add("block.gtceu.assembly_line_unit", "Assembly Control Casing");
        provider.add("block.gtceu.ptfe_pipe_casing", "PTFE Pipe Casing");
        provider.add("block.gtceu.palladium_substation", "Palladium Substation Casing");

        // Gearboxes
        provider.add("block.gtceu.bronze_gearbox", "Bronze Gearbox Casing");
        provider.add("block.gtceu.steel_gearbox", "Steel Gearbox Casing");
        provider.add("block.gtceu.stainless_steel_gearbox", "Stainless Steel Gearbox Casing");
        provider.add("block.gtceu.titanium_gearbox", "Titanium Gearbox Casing");
        provider.add("block.gtceu.tungstensteel_gearbox", "Tungstensteel Gearbox Casing");

        // Turbine Casing
        provider.add("block.gtceu.steel_turbine_casing", "Magnalium Turbine Casing");
        provider.add("block.gtceu.titanium_turbine_casing", "Titanium Turbine Casing");
        provider.add("block.gtceu.stainless_steel_turbine_casing", "Stainless Turbine Casing");
        provider.add("block.gtceu.tungstensteel_turbine_casing", "Tungstensteel Turbine Casing");

        // Pipe Casing
        provider.add("block.gtceu.bronze_pipe_casing", "Bronze Pipe Casing");
        provider.add("block.gtceu.steel_pipe_casing", "Steel Pipe Casing");
        provider.add("block.gtceu.titanium_pipe_casing", "Titanium Pipe Casing");
        provider.add("block.gtceu.tungstensteel_pipe_casing", "Tungstensteel Pipe Casing");

        // Bricked Casings
        provider.add("block.gtceu.steam_casing_bronze", "Bronze Hull");
        provider.add("block.gtceu.steam_casing_bricked_bronze", "Bricked Bronze Hull");
        provider.add("block.gtceu.steam_casing_steel", "Steel Hull");
        provider.add("block.gtceu.steam_casing_bricked_steel", "Bricked Wrought Iron Hull");

        // GCYM Casings
        provider.add("block.gtceu.laser_safe_engraving_casing", "Laser-Safe Engraving Casing");
        provider.add("block.gtceu.large_scale_assembler_casing", "Large-Scale Assembler Casing");
        provider.add("block.gtceu.reaction_safe_mixing_casing", "Reaction-Safe Mixing Casing");
        provider.add("block.gtceu.vibration_safe_casing", "Vibration-Safe Casing");

        // Fusion Casings
        provider.add("block.gtceu.superconducting_coil", "Superconducting Coil Block");
        provider.add("block.gtceu.fusion_coil", "Fusion Coil Block");
        provider.add("block.gtceu.fusion_casing", "Fusion Machine Casing");
        provider.add("block.gtceu.fusion_casing_mk2", "Fusion Machine Casing MK II");
        provider.add("block.gtceu.fusion_casing_mk3", "Fusion Machine Casing MK III");

        provider.add("block.gtceu.explosive.breaking_tooltip",
                "Primes explosion when mined, sneak mine to pick back up");
        provider.add("block.gtceu.explosive.lighting_tooltip", "Cannot be lit with Redstone");
        provider.add("block.gtceu.powderbarrel.drops_tooltip",
                "Slightly larger than TNT, drops all destroyed Blocks as Items");
        provider.add("block.gtceu.itnt.drops_tooltip", "Much larger than TNT, drops all destroyed Blocks as Items");

        // Decor Stuff
        provider.add("block.gtceu.yellow_stripes_block_a", "Yellow Stripes Block");
        provider.add("block.gtceu.yellow_stripes_block_b", "Yellow Stripes Block");
        provider.add("block.gtceu.yellow_stripes_block_c", "Yellow Stripes Block");
        provider.add("block.gtceu.yellow_stripes_block_d", "Yellow Stripes Block");

        // Extra stone blocks
        provider.add("block.gtceu.seal", "Sealed Block");

        // Surface Rocks
        provider.add("block.surface_rock", "%s Surface Rock");
    }

    private static void generateBlockTooltips(GTLangProvider provider) {
        // Coil Tooltip
        provider.add("block.gtceu.wire_coil.tooltip.extended_info", "§7Hold SHIFT to show Coil Bonus Info");
        provider.add("block.gtceu.wire_coil.tooltip.heat", "§cBase Heat Capacity: §f%d K");
        provider.add("block.gtceu.wire_coil.tooltip.smelter", "§8Multi Smelter:");
        provider.add("block.gtceu.wire_coil.tooltip.parallel_smelter", "  §5Max Parallel: §f%s");
        provider.add("block.gtceu.wire_coil.tooltip.energy_smelter", "  §aEnergy Usage: §f%s EU/t §8per recipe");
        provider.add("block.gtceu.wire_coil.tooltip.pyro", "§8Pyrolyse Oven:");
        provider.add("block.gtceu.wire_coil.tooltip.speed_pyro", "  §bProcessing Speed: §f%s%%");
        provider.add("block.gtceu.wire_coil.tooltip.cracking", "§8Cracking Unit:");
        provider.add("block.gtceu.wire_coil.tooltip.energy_cracking", "  §aEnergy Usage: §f%s%%");

        // Substation Capacitors
        provider.add("block.gtceu.substation_capacitor.tooltip.empty", "§7For filling space in your Power Substation");
        provider.add("block.gtceu.substation_capacitor.tooltip.filled", "§cEnergy Capacity: §f%d EU");

        // Bricked Casings
        provider.add("block.gtceu.steam_casing_bronze.tooltip", "§7For your first Steam Machines");
        provider.add("block.gtceu.steam_casing_bricked_bronze.tooltip", "§7For your first Steam Machines");
        provider.add("block.gtceu.steam_casing_steel.tooltip", "§7For improved Steam Machines");
        provider.add("block.gtceu.steam_casing_bricked_steel.tooltip", "§7For improved Steam Machines");

        // Filter Casings
        provider.add("block.gtceu.filter_casing.tooltip", "Creates a §aParticle-Free§7 environment");
        provider.add("block.gtceu.sterilizing_filter_casing.tooltip", "Creates a §aSterilized§7 environment");

        // Lamps
        provider.add("block.gtceu.lamp.tooltip.inverted", "Inverted");
        provider.add("block.gtceu.lamp.tooltip.no_bloom", "No Bloom");
        provider.add("block.gtceu.lamp.tooltip.no_light", "No Light");

        // Charcoal
        provider.addMultiline("block.gtceu.brittle_charcoal.tooltip",
                "Produced by the Charcoal Pile Igniter.\nMine this to get Charcoal.");
    }

    private static void generatePipeKeys(GTLangProvider provider) {
        // Cables
        provider.add("tooltip.gtceu.cable.voltage", "§aMax Voltage:§r §a%d §a(%s§a)");
        provider.add("tooltip.gtceu.cable.amperage", "§eMax Amperage:§r §e%d");
        provider.add("tooltip.gtceu.cable.loss_per_block", "§cLoss/Meter/Ampere:§r §c%d§7 EU-Volt");
        provider.add("tooltip.gtceu.cable.superconductor", "%s §dSuperconductor");

        // Fluid Pipes
        provider.add("tooltip.gtceu.fluid_pipe.capacity", "§9Capacity: §f%d mB");
        provider.add("tooltip.gtceu.fluid_pipe.max_temperature", "§cTemperature Limit: §f%d K");
        provider.add("tooltip.gtceu.fluid_pipe.channels", "§eChannels: §f%d");
        provider.add("tooltip.gtceu.fluid_pipe.gas_proof", "§6Can handle Gases");
        provider.add("tooltip.gtceu.fluid_pipe.not_gas_proof", "§4Gases may leak!");
        provider.add("tooltip.gtceu.fluid_pipe.acid_proof", "§6Can handle Acids");
        provider.add("tooltip.gtceu.fluid_pipe.cryo_proof", "§6Can handle Cryogenics");
        provider.add("tooltip.gtceu.fluid_pipe.plasma_proof", "§6Can handle all Plasmas");

        // Item Pipes
        provider.add("tooltip.gtceu.item_pipe.priority", "§9Priority: §f%d");

        // Duct Pipes
        provider.add("tooltip.gtceu.duct_pipe.transfer_rate", "§bAir transfer rate: %s");

        // Optical/Laser
        provider.add("block.gtceu.normal_laser_pipe.tooltip",
                "§7Transmitting power with §fno loss§7 in straight lines");
        provider.add("block.gtceu.normal_optical_pipe.tooltip", "§7Transmitting §fComputation§7 or §fResearch Data§7");
    }
}
