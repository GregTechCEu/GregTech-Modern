package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.replace;

public class BlockLang {

    public static void init(RegistrateLangProvider provider) {
        initCasingLang(provider);
    }

    private static void initCasingLang(RegistrateLangProvider provider) {

        // Coils
        replace(provider, "block.gtceu.wire_coil_cupronickel", "Cupronickel Coil Block");
        replace(provider, "block.gtceu.wire_coil_kanthal", "Kanthal Coil Block");
        replace(provider, "block.gtceu.wire_coil_nichrome", "Nichrome Coil Block");
        replace(provider, "block.gtceu.wire_coil_tungstensteel", "Tungstensteel Coil Block");
        replace(provider, "block.gtceu.wire_coil_hss_g", "HSS-G Coil Block");
        replace(provider, "block.gtceu.wire_coil_naquadah", "Naquadah Coil Block");
        replace(provider, "block.gtceu.wire_coil_trinium", "Trinium Coil Block");
        replace(provider, "block.gtceu.wire_coil_tritanium", "Tritanium Coil Block");

        replace(provider, "block.gtceu.wire_coil.tooltip_extended_info", "§7Hold SHIFT to show Coil Bonus Info");
        replace(provider, "block.gtceu.wire_coil.tooltip_heat", "§cBase Heat Capacity: §f%d K");
        replace(provider, "block.gtceu.wire_coil.tooltip_smelter", "§8Multi Smelter:");
        replace(provider, "block.gtceu.wire_coil.tooltip_parallel_smelter", "  §5Max Parallel: §f%s");
        replace(provider, "block.gtceu.wire_coil.tooltip_energy_smelter", "  §aEnergy Usage: §f%s EU/t §8per recipe");
        replace(provider, "block.gtceu.wire_coil.tooltip_pyro", "§8Pyrolyse Oven:");
        replace(provider, "block.gtceu.wire_coil.tooltip_speed_pyro", "  §bProcessing Speed: §f%s%%");
        replace(provider, "block.gtceu.wire_coil.tooltip_cracking", "§8Cracking Unit:");
        replace(provider, "block.gtceu.wire_coil.tooltip_energy_cracking", "  §aEnergy Usage: §f%s%%");

        // Casings
        replace(provider, "block.gtceu.casing_bronze_bricks", "Bronze Machine Casing");
        replace(provider, "block.gtceu.casing_primitive_bricks", "Firebricks");
        replace(provider, "block.gtceu.casing_invar_heatproof", "Heat Proof Invar Machine Casing");
        replace(provider, "block.gtceu.casing_aluminium_frostproof", "Frost Proof Aluminium Machine Casing");
        replace(provider, "block.gtceu.casing_steel_solid", "Solid Steel Machine Casing");
        replace(provider, "block.gtceu.casing_stainless_clean", "Clean Stainless Steel Casing");
        replace(provider, "block.gtceu.casing_titanium_stable", "Stable Titanium Machine Casing");
        replace(provider, "block.gtceu.casing_tungstensteel_robust", "Robust Tungstensteel Machine Casing");
        replace(provider, "block.gtceu.casing_coke_bricks", "Coke Oven Bricks");
        replace(provider, "block.gtceu.casing_ptfe_inert", "Chemically Inert PTFE Machine Casing");
        replace(provider, "block.gtceu.casing_hsse_sturdy", "Sturdy HSS-E Machine Casing");
        replace(provider, "block.gtceu.casing_grate", "Grate Machine Casing");
        replace(provider, "block.gtceu.casing_assembly_control", "Assembly Control Casing");
        replace(provider, "block.gtceu.casing_polytetrafluoroethylene_pipe", "PTFE Pipe Casing");
        replace(provider, "block.gtceu.casing_laminated_glass", "Laminated Glass");
        replace(provider, "block.gtceu.casing_bronze_gearbox", "Bronze Gearbox Casing");
        replace(provider, "block.gtceu.casing_steel_gearbox", "Steel Gearbox Casing");
        replace(provider, "block.gtceu.casing_stainless_steel_gearbox", "Stainless Steel Gearbox Casing");
        replace(provider, "block.gtceu.casing_titanium_gearbox", "Titanium Gearbox Casing");
        replace(provider, "block.gtceu.casing_tungstensteel_gearbox", "Tungstensteel Gearbox Casing");
        replace(provider, "block.gtceu.casing_steel_turbine", "Steel Turbine Casing");
        replace(provider, "block.gtceu.casing_titanium_turbine", "Titanium Turbine Casing");
        replace(provider, "block.gtceu.casing_stainless_turbine", "Stainless Turbine Casing");
        replace(provider, "block.gtceu.casing_tungstensteel_turbine", "Tungstensteel Turbine Casing");
        replace(provider, "block.gtceu.casing_bronze_pipe", "Bronze Pipe Casing");
        replace(provider, "block.gtceu.casing_steel_pipe", "Steel Pipe Casing");
        replace(provider, "block.gtceu.casing_titanium_pipe", "Titanium Pipe Casing");
        replace(provider, "block.gtceu.casing_tungstensteel_pipe", "Tungstensteel Pipe Casing");
        replace(provider, "block.gtceu.casing_pump_deck", "Pump Deck");

        // todo these tooltips dont work
        replace(provider, "block.gtceu.steam_casing_bronze", "Bronze Hull");
        provider.add("block.gtceu.steam_casing_bronze.tooltip", "For your first Steam Machines");
        replace(provider, "block.gtceu.steam_casing_bricked_bronze", "Bricked Bronze Hull");
        provider.add("block.gtceu.steam_casing_bricked_bronze.tooltip", "For your first Steam Machines");
        replace(provider, "block.gtceu.steam_casing_steel", "Steel Hull");
        provider.add("block.gtceu.steam_casing_steel.tooltip", "For improved Steam Machines");
        replace(provider, "block.gtceu.steam_casing_bricked_steel", "Bricked Wrought Iron Hull");
        provider.add("block.gtceu.steam_casing_bricked_steel.tooltip", "For improved Steam Machines");

        replace(provider, "block.gtceu.active_casing_engine_intake", "Engine Intake Casing");
        replace(provider, "block.gtceu.active_casing_extreme_engine_intake", "Extreme Engine Intake Casing");
        replace(provider, "block.gtceu.active_casing_assembly_line", "Assembly Line Casing");

        replace(provider, "block.gtceu.bronze_firebox", "Bronze Firebox Casing");
        replace(provider, "block.gtceu.steel_firebox", "Steel Firebox Casing");
        replace(provider, "block.gtceu.titanium_firebox", "Titanium Firebox Casing");
        replace(provider, "block.gtceu.tungstensteel_firebox", "Tungstensteel Firebox Casing");

        // todo multiblock tanks
        //replace(provider, "tile.steam_casing.wood_wall.name", "Wooden Wall");

        // todo fusion
        //replace(provider, "tile.fusion_casing.superconductor_coil.name", "Superconducting Coil Block");
        //replace(provider, "tile.fusion_casing.fusion_coil.name", "Fusion Coil Block");
        //replace(provider, "tile.fusion_casing.fusion_casing.name", "Fusion Machine Casing");
        //replace(provider, "tile.fusion_casing.fusion_casing_mk2.name", "Fusion Machine Casing MK II");
        //replace(provider, "tile.fusion_casing.fusion_casing_mk3.name", "Fusion Machine Casing MK III");

        // todo tiered glass
        //replace(provider, "tile.transparent_casing.tempered_glass.name", "Tempered Glass");
        //replace(provider, "tile.transparent_casing.fusion_glass.name", "Fusion Glass");
        //replace(provider, "tile.transparent_casing.cleanroom_glass.name", "Cleanroom Glass");

        // TODO warning sign blocks
        /*
        replace(provider, "tile.warning_sign.yellow_stripes.name", "Yellow Stripes Block");
        replace(provider, "tile.warning_sign.small_yellow_stripes.name", "Yellow Stripes Block");
        replace(provider, "tile.warning_sign.radioactive_hazard.name", "Radioactive Hazard Sign Block");
        replace(provider, "tile.warning_sign.bio_hazard.name", "Bio Hazard Sign Block");
        replace(provider, "tile.warning_sign.explosion_hazard.name", "Explosion Hazard Sign Block");
        replace(provider, "tile.warning_sign.fire_hazard.name", "Fire Hazard Sign Block");
        replace(provider, "tile.warning_sign.acid_hazard.name", "Acid Hazard Sign Block");
        replace(provider, "tile.warning_sign.magic_hazard.name", "Magic Hazard Sign Block");
        replace(provider, "tile.warning_sign.frost_hazard.name", "Frost Hazard Sign Block");
        replace(provider, "tile.warning_sign.noise_hazard.name", "Noise Hazard Sign Block");
        replace(provider, "tile.warning_sign.generic_hazard.name", "Generic Hazard Sign Block");
        replace(provider, "tile.warning_sign.high_voltage_hazard.name", "High Voltage Hazard Sign Block");
        replace(provider, "tile.warning_sign.magnetic_hazard.name", "Magnetic Hazard Sign Block");
        replace(provider, "tile.warning_sign.antimatter_hazard.name", "Antimatter Hazard Sign Block");
        replace(provider, "tile.warning_sign.high_temperature_hazard.name", "High Temperature Hazard Sign Block");
        replace(provider, "tile.warning_sign.void_hazard.name", "Void Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.mob_spawner_hazard.name", "Mob Spawner Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.spatial_storage_hazard.name", "Spatial Storage Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.laser_hazard.name", "Laser Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.mob_hazard.name", "Mob Infestation Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.boss_hazard.name", "Boss Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.gregification_hazard.name", "Gregification Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.causality_hazard.name", "Non-Standard Causality Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.automated_defenses_hazard.name", "Automated Defenses Hazard Sign Block");
        replace(provider, "tile.warning_sign_1.high_pressure_hazard.name", "High Pressure Hazard Sign Block");
         */
    }
}
