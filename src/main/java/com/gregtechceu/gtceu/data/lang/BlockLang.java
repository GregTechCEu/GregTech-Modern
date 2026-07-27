package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class BlockLang {

    public static void init(GTLangProvider provider) {
        generateBlockTooltips(provider);
        generatePipeKeys(provider);

        provider.add("block.gtceu.surface_rock", "%s Surface Rock");
    }

    private static void generateBlockTooltips(GTLangProvider provider) {

        provider.add("block.gtceu.explosive.breaking_tooltip",
                "Primes explosion when mined, sneak mine to pick back up");
        provider.add("block.gtceu.explosive.lighting_tooltip", "Cannot be lit with Redstone");
        provider.add("block.gtceu.powderbarrel.drops_tooltip",
                "Slightly larger than TNT, drops all destroyed Blocks as Items");
        provider.add("block.gtceu.itnt.drops_tooltip", "Much larger than TNT, drops all destroyed Blocks as Items");

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
        provider.add("tooltip.gtceu.fluid_pipe.throughput", "§bTransfer Rate: §f%d mB/t");
        provider.add("tooltip.gtceu.fluid_pipe.channels", "§eChannels: §f%d");
        provider.add("tooltip.gtceu.fluid_pipe.max_temperature", "§cTemperature Limit: §f%s");
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
