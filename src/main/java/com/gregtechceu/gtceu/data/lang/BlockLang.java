package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.replace;

public class BlockLang {

    public static void init(RegistrateLangProvider provider) {
        initCasingLang(provider);
    }

    private static void initCasingLang(RegistrateLangProvider provider) {

        // Coils
        replace(provider, "block.gtceu.hssg_coil_block", "HSS-G Coil Block");

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
        replace(provider, "block.gtceu.heatproof_machine_casing", "Heat Proof Invar Machine Casing");
        replace(provider, "block.gtceu.frostproof_machine_casing", "Frost Proof Aluminium Machine Casing");
        replace(provider, "block.gtceu.steel_machine_casing", "Solid Steel Machine Casing");
        replace(provider, "block.gtceu.clean_machine_casing", "Clean Stainless Steel Casing");
        replace(provider, "block.gtceu.stable_machine_casing", "Stable Titanium Machine Casing");
        replace(provider, "block.gtceu.robust_machine_casing", "Robust Tungstensteel Machine Casing");
        replace(provider, "block.gtceu.casing_coke_bricks", "Coke Oven Bricks");
        replace(provider, "block.gtceu.inert_machine_casing", "Chemically Inert PTFE Machine Casing");
        replace(provider, "block.gtceu.sturdy_machine_casing", "Sturdy HSS-E Machine Casing");
        replace(provider, "block.gtceu.casing_grate", "Grate Machine Casing");
        replace(provider, "block.gtceu.assembly_line_unit", "Assembly Control Casing");
        replace(provider, "block.gtceu.ptfe_pipe_casing", "PTFE Pipe Casing");
        replace(provider, "block.gtceu.bronze_gearbox", "Bronze Gearbox Casing");
        replace(provider, "block.gtceu.steel_gearbox", "Steel Gearbox Casing");
        replace(provider, "block.gtceu.stainless_steel_gearbox", "Stainless Steel Gearbox Casing");
        replace(provider, "block.gtceu.titanium_gearbox", "Titanium Gearbox Casing");
        replace(provider, "block.gtceu.tungstensteel_gearbox", "Tungstensteel Gearbox Casing");
        replace(provider, "block.gtceu.titanium_turbine_casing", "Titanium Turbine Casing");
        replace(provider, "block.gtceu.stainless_steel_turbine_casing", "Stainless Turbine Casing");
        replace(provider, "block.gtceu.tungstensteel_turbine_casing", "Tungstensteel Turbine Casing");
        replace(provider, "block.gtceu.bronze_pipe_casing", "Bronze Pipe Casing");
        replace(provider, "block.gtceu.steel_pipe_casing", "Steel Pipe Casing");
        replace(provider, "block.gtceu.titanium_pipe_casing", "Titanium Pipe Casing");
        replace(provider, "block.gtceu.tungstensteel_pipe_casing", "Tungstensteel Pipe Casing");

        replace(provider, "block.gtceu.steam_casing_bronze", "Bronze Hull");
        provider.add("block.gtceu.steam_casing_bronze.tooltip", "§7For your first Steam Machines");
        replace(provider, "block.gtceu.steam_casing_bricked_bronze", "Bricked Bronze Hull");
        provider.add("block.gtceu.steam_casing_bricked_bronze.tooltip", "§7For your first Steam Machines");
        replace(provider, "block.gtceu.steam_casing_steel", "Steel Hull");
        provider.add("block.gtceu.steam_casing_steel.tooltip", "§7For improved Steam Machines");
        replace(provider, "block.gtceu.steam_casing_bricked_steel", "Bricked Wrought Iron Hull");
        provider.add("block.gtceu.steam_casing_bricked_steel.tooltip", "§7For improved Steam Machines");

        //GCyM Casings
        replace(provider, "block.gtceu.laser_safe_engraving_casing", "Laser-Safe Engraving Casing");
        replace(provider, "block.gtceu.large_scale_assembler_casing", "Large-Scale Assembler Casing");
        replace(provider, "block.gtceu.reaction_safe_mixing_casing", "Reaction-Safe Mixing Casing");
        replace(provider, "block.gtceu.vibration_safe_casing", "Vibration-Safe Casing");

        // todo multiblock tanks
        //replace(provider, "tile.steam_casing.wood_wall", "Wooden Wall");

        // todo fusion
        replace(provider, "block.gtceu.superconducting_coil", "Superconducting Coil Block");
        replace(provider, "block.gtceu.fusion_coil", "Fusion Coil Block");
        replace(provider, "block.gtceu.fusion_casing", "Fusion Machine Casing");
        replace(provider, "block.gtceu.fusion_casing_mk2", "Fusion Machine Casing MK II");
        replace(provider, "block.gtceu.fusion_casing_mk3", "Fusion Machine Casing MK III");

        provider.add("block.filter_casing.tooltip", "Creates a §aParticle-Free§7 environment");
        provider.add("block.sterilizing_filter_casing.tooltip", "Creates a §aSterilized§7 environment");


        // TODO warning sign blocks
        /*
        replace(provider, "block.gtceu.warning_sign.yellow_stripes", "Yellow Stripes Block");
        replace(provider, "block.gtceu.warning_sign.small_yellow_stripes", "Yellow Stripes Block");
        replace(provider, "block.gtceu.warning_sign.radioactive_hazard", "Radioactive Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.bio_hazard", "Bio Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.explosion_hazard", "Explosion Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.fire_hazard", "Fire Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.acid_hazard", "Acid Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.magic_hazard", "Magic Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.frost_hazard", "Frost Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.noise_hazard", "Noise Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.generic_hazard", "Generic Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.high_voltage_hazard", "High Voltage Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.magnetic_hazard", "Magnetic Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.antimatter_hazard", "Antimatter Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.high_temperature_hazard", "High Temperature Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign.void_hazard", "Void Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.mob_spawner_hazard", "Mob Spawner Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.spatial_storage_hazard", "Spatial Storage Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.laser_hazard", "Laser Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.mob_hazard", "Mob Infestation Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.boss_hazard", "Boss Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.gregification_hazard", "Gregification Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.causality_hazard", "Non-Standard Causality Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.automated_defenses_hazard", "Automated Defenses Hazard Sign Block");
        replace(provider, "block.gtceu.warning_sign_1.high_pressure_hazard", "High Pressure Hazard Sign Block");
         */

        replace(provider, "block.gtceu.asphalt.asphalt", "Asphalt");
        replace(provider, "block.gtceu.smooth_black_granite", "Black Granite");
        replace(provider, "block.gtceu.smooth_red_granite", "Red Granite");
        replace(provider, "block.gtceu.smooth_marble", "Marble");
        replace(provider, "block.gtceu.smooth_basalt", "Basalt");
        replace(provider, "block.gtceu.smooth_concrete_light", "Light Concrete");
        replace(provider, "block.gtceu.smooth_concrete_dark", "Dark Concrete");
        replace(provider, "block.gtceu.black_granite_cobblestone", "Black Granite Cobblestone");
        replace(provider, "block.gtceu.red_granite_cobblestone", "Red Granite Cobblestone");
        replace(provider, "block.gtceu.marble_cobblestone", "Marble Cobblestone");
        replace(provider, "block.gtceu.basalt_cobblestone", "Basalt Cobblestone");
        replace(provider, "block.gtceu.light_concrete_cobblestone", "Light Concrete Cobblestone");
        replace(provider, "block.gtceu.dark_concrete_cobblestone", "Dark Concrete Cobblestone");
        replace(provider, "block.gtceu.mossy_black_granite_cobblestone", "Mossy Black Granite Cobblestone");
        replace(provider, "block.gtceu.mossy_red_granite_cobblestone", "Mossy Red Granite Cobblestone");
        replace(provider, "block.gtceu.mossy_marble_cobblestone", "Mossy Marble Cobblestone");
        replace(provider, "block.gtceu.mossy_basalt_cobblestone", "Mossy Basalt Cobblestone");
        replace(provider, "block.gtceu.mossy_concrete_light_cobblestone", "Mossy Light Concrete Cobblestone");
        replace(provider, "block.gtceu.mossy_concrete_dark_cobblestone", "Mossy Dark Concrete Cobblestone");
        replace(provider, "block.gtceu.polished_black_granite", "Polished Black Granite");
        replace(provider, "block.gtceu.polished_red_granite", "Polished Red Granite");
        replace(provider, "block.gtceu.polished_marble", "Polished Marble");
        replace(provider, "block.gtceu.polished_basalt", "Polished Basalt");
        replace(provider, "block.gtceu.polished_light_concrete", "Polished Light Concrete");
        replace(provider, "block.gtceu.polished_dark_concrete", "Polished Dark Concrete");
        replace(provider, "block.gtceu.black_granite_bricks", "Black Granite Bricks");
        replace(provider, "block.gtceu.red_granite_bricks", "Red Granite Bricks");
        replace(provider, "block.gtceu.marble_bricks", "Marble Bricks");
        replace(provider, "block.gtceu.basalt_bricks", "Basalt Bricks");
        replace(provider, "block.gtceu.concrete_light_bricks", "Light Concrete Bricks");
        replace(provider, "block.gtceu.concrete_dark_bricks", "Dark Concrete Bricks");
        replace(provider, "block.gtceu.cracked_black_granite_bricks", "Cracked Black Granite Bricks");
        replace(provider, "block.gtceu.cracked_red_granite_bricks", "Cracked Red Granite Bricks");
        replace(provider, "block.gtceu.cracked_marble_bricks", "Cracked Marble Bricks");
        replace(provider, "block.gtceu.cracked_basalt_bricks", "Cracked Basalt Bricks");
        replace(provider, "block.gtceu.cracked_concrete_light_bricks", "Cracked Light Concrete Bricks");
        replace(provider, "block.gtceu.cracked_concrete_dark_bricks", "Cracked Dark Concrete Bricks");
        replace(provider, "block.gtceu.mossy_black_granite_bricks", "Mossy Black Granite Bricks");
        replace(provider, "block.gtceu.mossy_red_granite_bricks", "Mossy Red Granite Bricks");
        replace(provider, "block.gtceu.mossy_marble_bricks", "Mossy Marble Bricks");
        replace(provider, "block.gtceu.mossy_basalt_bricks", "Mossy Basalt Bricks");
        replace(provider, "block.gtceu.mossy_concrete_light_bricks", "Mossy Light Concrete Bricks");
        replace(provider, "block.gtceu.mossy_concrete_dark_bricks", "Mossy Dark Concrete Bricks");
        replace(provider, "block.gtceu.chiseled_black_granite", "Chiseled Black Granite");
        replace(provider, "block.gtceu.chiseled_red_granite", "Chiseled Red Granite");
        replace(provider, "block.gtceu.chiseled_marble", "Chiseled Marble");
        replace(provider, "block.gtceu.chiseled_basalt", "Chiseled Basalt");
        replace(provider, "block.gtceu.chiseled_concrete_light", "Chiseled Light Concrete");
        replace(provider, "block.gtceu.chiseled_concrete_dark", "Chiseled Dark Concrete");
        replace(provider, "block.gtceu.black_granite_tile", "Black Granite Tiles");
        replace(provider, "block.gtceu.red_granite_tile", "Red Granite Tiles");
        replace(provider, "block.gtceu.marble_tile", "Marble Tiles");
        replace(provider, "block.gtceu.basalt_tile", "Basalt Tiles");
        replace(provider, "block.gtceu.concrete_light_tile", "Light Concrete Tiles");
        replace(provider, "block.gtceu.concrete_dark_tile", "Dark Concrete Tiles");
        replace(provider, "block.gtceu.black_granite_small_tile", "Small Black Granite Tiles");
        replace(provider, "block.gtceu.red_granite_small_tile", "Small Red Granite Tiles");
        replace(provider, "block.gtceu.marble_small_tile", "Small Marble Tiles");
        replace(provider, "block.gtceu.basalt_small_tile", "Small Basalt Tiles");
        replace(provider, "block.gtceu.concrete_light_small_tile", "Small Light Concrete Tiles");
        replace(provider, "block.gtceu.concrete_dark_small_tile", "Small Dark Concrete Tiles");
        replace(provider, "block.gtceu.small_black_granite_bricks", "Small Black Granite Bricks");
        replace(provider, "block.gtceu.small_red_granite_bricks", "Small Red Granite Bricks");
        replace(provider, "block.gtceu.small_marble_bricks", "Small Marble Bricks");
        replace(provider, "block.gtceu.small_basalt_bricks", "Small Basalt Bricks");
        replace(provider, "block.gtceu.small_concrete_light_bricks", "Small Light Concrete Bricks");
        replace(provider, "block.gtceu.small_concrete_dark_bricks", "Small Dark Concrete Bricks");
        replace(provider, "block.gtceu.windmill_a.black_granite_bricks", "Black Granite Windmill Tiles A");
        replace(provider, "block.gtceu.windmill_a.red_granite_bricks", "Red Granite Windmill Tiles A");
        replace(provider, "block.gtceu.windmill_a.marble_bricks", "Marble Windmill Tiles A");
        replace(provider, "block.gtceu.windmill_a.basalt_bricks", "Basalt Windmill Tiles A");
        replace(provider, "block.gtceu.windmill_a.concrete_light_bricks", "Light Concrete Windmill Tiles A");
        replace(provider, "block.gtceu.windmill_a.concrete_dark_bricks", "Dark Concrete Windmill Tiles A");
        replace(provider, "block.gtceu.windmill_b.black_granite_bricks", "Black Granite Windmill Tiles B");
        replace(provider, "block.gtceu.windmill_b.red_granite_bricks", "Red Granite Windmill Tiles B");
        replace(provider, "block.gtceu.windmill_b.marble_bricks", "Marble Windmill Tiles B");
        replace(provider, "block.gtceu.windmill_b.basalt_bricks", "Basalt Windmill Tiles B");
        replace(provider, "block.gtceu.windmill_b.concrete_light_bricks", "Light Concrete Windmill Tiles B");
        replace(provider, "block.gtceu.windmill_b.concrete_dark_bricks", "Dark Concrete Windmill Tiles B");
        replace(provider, "block.gtceu.square_black_granite_bricks", "Square Black Granite Bricks");
        replace(provider, "block.gtceu.square_red_granite_bricks", "Square Red Granite Bricks");
        replace(provider, "block.gtceu.square_marble_bricks", "Square Marble Bricks");
        replace(provider, "block.gtceu.square_basalt_bricks", "Square Basalt Bricks");
        replace(provider, "block.gtceu.square_concrete_light_bricks", "Square Light Concrete Bricks");
        replace(provider, "block.gtceu.square_concrete_dark_bricks", "Square Dark Concrete Bricks");
    }
}
