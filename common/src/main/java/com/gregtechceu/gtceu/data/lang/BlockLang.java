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

        replace(provider, "block.gtceu.steam_casing_bronze", "Bronze Hull");
        provider.add("block.gtceu.steam_casing_bronze.tooltip", "§7For your first Steam Machines");
        replace(provider, "block.gtceu.steam_casing_bricked_bronze", "Bricked Bronze Hull");
        provider.add("block.gtceu.steam_casing_bricked_bronze.tooltip", "§7For your first Steam Machines");
        replace(provider, "block.gtceu.steam_casing_steel", "Steel Hull");
        provider.add("block.gtceu.steam_casing_steel.tooltip", "§7For improved Steam Machines");
        replace(provider, "block.gtceu.steam_casing_bricked_steel", "Bricked Wrought Iron Hull");
        provider.add("block.gtceu.steam_casing_bricked_steel.tooltip", "§7For improved Steam Machines");

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
        replace(provider, "block.superconducting_coil", "Superconducting Coil Block");
        replace(provider, "block.fusion_coil", "Fusion Coil Block");
        replace(provider, "block.fusion_casing", "Fusion Machine Casing");
        replace(provider, "block.fusion_casing_mk2", "Fusion Machine Casing MK II");
        replace(provider, "block.fusion_casing_mk3", "Fusion Machine Casing MK III");

        provider.add("block.filter.tooltip", "Creates a §aParticle-Free§7 environment");
        provider.add("block.filter_sterile.tooltip", "Creates a §aSterilized§7 environment");


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

        // todo stone type decorative blocks
        /*
        provider.add("tile.asphalt.asphalt.name", "Asphalt");
        provider.add("tile.stone_smooth.black_granite.name", "Black Granite");
        provider.add("tile.stone_smooth.red_granite.name", "Red Granite");
        provider.add("tile.stone_smooth.marble.name", "Marble");
        provider.add("tile.stone_smooth.basalt.name", "Basalt");
        provider.add("tile.stone_smooth.concrete_light.name", "Light Concrete");
        provider.add("tile.stone_smooth.concrete_dark.name", "Dark Concrete");
        provider.add("tile.stone_cobble.black_granite.name", "Black Granite Cobblestone");
        provider.add("tile.stone_cobble.red_granite.name", "Red Granite Cobblestone");
        provider.add("tile.stone_cobble.marble.name", "Marble Cobblestone");
        provider.add("tile.stone_cobble.basalt.name", "Basalt Cobblestone");
        provider.add("tile.stone_cobble.concrete_light.name", "Light Concrete Cobblestone");
        provider.add("tile.stone_cobble.concrete_dark.name", "Dark Concrete Cobblestone");
        provider.add("tile.stone_cobble_mossy.black_granite.name", "Mossy Black Granite Cobblestone");
        provider.add("tile.stone_cobble_mossy.red_granite.name", "Mossy Red Granite Cobblestone");
        provider.add("tile.stone_cobble_mossy.marble.name", "Mossy Marble Cobblestone");
        provider.add("tile.stone_cobble_mossy.basalt.name", "Mossy Basalt Cobblestone");
        provider.add("tile.stone_cobble_mossy.concrete_light.name", "Mossy Light Concrete Cobblestone");
        provider.add("tile.stone_cobble_mossy.concrete_dark.name", "Mossy Dark Concrete Cobblestone");
        provider.add("tile.stone_polished.black_granite.name", "Polished Black Granite");
        provider.add("tile.stone_polished.red_granite.name", "Polished Red Granite");
        provider.add("tile.stone_polished.marble.name", "Polished Marble");
        provider.add("tile.stone_polished.basalt.name", "Polished Basalt");
        provider.add("tile.stone_polished.concrete_light.name", "Polished Light Concrete");
        provider.add("tile.stone_polished.concrete_dark.name", "Polished Dark Concrete");
        provider.add("tile.stone_bricks.black_granite.name", "Black Granite Bricks");
        provider.add("tile.stone_bricks.red_granite.name", "Red Granite Bricks");
        provider.add("tile.stone_bricks.marble.name", "Marble Bricks");
        provider.add("tile.stone_bricks.basalt.name", "Basalt Bricks");
        provider.add("tile.stone_bricks.concrete_light.name", "Light Concrete Bricks");
        provider.add("tile.stone_bricks.concrete_dark.name", "Dark Concrete Bricks");
        provider.add("tile.stone_bricks_cracked.black_granite.name", "Cracked Black Granite Bricks");
        provider.add("tile.stone_bricks_cracked.red_granite.name", "Cracked Red Granite Bricks");
        provider.add("tile.stone_bricks_cracked.marble.name", "Cracked Marble Bricks");
        provider.add("tile.stone_bricks_cracked.basalt.name", "Cracked Basalt Bricks");
        provider.add("tile.stone_bricks_cracked.concrete_light.name", "Cracked Light Concrete Bricks");
        provider.add("tile.stone_bricks_cracked.concrete_dark.name", "Cracked Dark Concrete Bricks");
        provider.add("tile.stone_bricks_mossy.black_granite.name", "Mossy Black Granite Bricks");
        provider.add("tile.stone_bricks_mossy.red_granite.name", "Mossy Red Granite Bricks");
        provider.add("tile.stone_bricks_mossy.marble.name", "Mossy Marble Bricks");
        provider.add("tile.stone_bricks_mossy.basalt.name", "Mossy Basalt Bricks");
        provider.add("tile.stone_bricks_mossy.concrete_light.name", "Mossy Light Concrete Bricks");
        provider.add("tile.stone_bricks_mossy.concrete_dark.name", "Mossy Dark Concrete Bricks");
        provider.add("tile.stone_chiseled.black_granite.name", "Chiseled Black Granite");
        provider.add("tile.stone_chiseled.red_granite.name", "Chiseled Red Granite");
        provider.add("tile.stone_chiseled.marble.name", "Chiseled Marble");
        provider.add("tile.stone_chiseled.basalt.name", "Chiseled Basalt");
        provider.add("tile.stone_chiseled.concrete_light.name", "Chiseled Light Concrete");
        provider.add("tile.stone_chiseled.concrete_dark.name", "Chiseled Dark Concrete");
        provider.add("tile.stone_tiled.black_granite.name", "Black Granite Tiles");
        provider.add("tile.stone_tiled.red_granite.name", "Red Granite Tiles");
        provider.add("tile.stone_tiled.marble.name", "Marble Tiles");
        provider.add("tile.stone_tiled.basalt.name", "Basalt Tiles");
        provider.add("tile.stone_tiled.concrete_light.name", "Light Concrete Tiles");
        provider.add("tile.stone_tiled.concrete_dark.name", "Dark Concrete Tiles");
        provider.add("tile.stone_tiled_small.black_granite.name", "Small Black Granite Tiles");
        provider.add("tile.stone_tiled_small.red_granite.name", "Small Red Granite Tiles");
        provider.add("tile.stone_tiled_small.marble.name", "Small Marble Tiles");
        provider.add("tile.stone_tiled_small.basalt.name", "Small Basalt Tiles");
        provider.add("tile.stone_tiled_small.concrete_light.name", "Small Light Concrete Tiles");
        provider.add("tile.stone_tiled_small.concrete_dark.name", "Small Dark Concrete Tiles");
        provider.add("tile.stone_bricks_small.black_granite.name", "Small Black Granite Bricks");
        provider.add("tile.stone_bricks_small.red_granite.name", "Small Red Granite Bricks");
        provider.add("tile.stone_bricks_small.marble.name", "Small Marble Bricks");
        provider.add("tile.stone_bricks_small.basalt.name", "Small Basalt Bricks");
        provider.add("tile.stone_bricks_small.concrete_light.name", "Small Light Concrete Bricks");
        provider.add("tile.stone_bricks_small.concrete_dark.name", "Small Dark Concrete Bricks");
        provider.add("tile.stone_bricks_windmill_a.black_granite.name", "Black Granite Windmill Tiles A");
        provider.add("tile.stone_bricks_windmill_a.red_granite.name", "Red Granite Windmill Tiles A");
        provider.add("tile.stone_bricks_windmill_a.marble.name", "Marble Windmill Tiles A");
        provider.add("tile.stone_bricks_windmill_a.basalt.name", "Basalt Windmill Tiles A");
        provider.add("tile.stone_bricks_windmill_a.concrete_light.name", "Light Concrete Windmill Tiles A");
        provider.add("tile.stone_bricks_windmill_a.concrete_dark.name", "Dark Concrete Windmill Tiles A");
        provider.add("tile.stone_bricks_windmill_b.black_granite.name", "Black Granite Windmill Tiles B");
        provider.add("tile.stone_bricks_windmill_b.red_granite.name", "Red Granite Windmill Tiles B");
        provider.add("tile.stone_bricks_windmill_b.marble.name", "Marble Windmill Tiles B");
        provider.add("tile.stone_bricks_windmill_b.basalt.name", "Basalt Windmill Tiles B");
        provider.add("tile.stone_bricks_windmill_b.concrete_light.name", "Light Concrete Windmill Tiles B");
        provider.add("tile.stone_bricks_windmill_b.concrete_dark.name", "Dark Concrete Windmill Tiles B");
        provider.add("tile.stone_bricks_square.black_granite.name", "Square Black Granite Bricks");
        provider.add("tile.stone_bricks_square.red_granite.name", "Square Red Granite Bricks");
        provider.add("tile.stone_bricks_square.marble.name", "Square Marble Bricks");
        provider.add("tile.stone_bricks_square.basalt.name", "Square Basalt Bricks");
        provider.add("tile.stone_bricks_square.concrete_light.name", "Square Light Concrete Bricks");
        provider.add("tile.stone_bricks_square.concrete_dark.name", "Square Dark Concrete Bricks");
         */
        provider.add("tile.stone_bricks_square.concrete_dark.name", "Square Dark Concrete Bricks");
        provider.add("tile.wire_coil.tooltip_extended_info", "Hold SHIFT to show Coil Bonus Info");
        provider.add("tile.wire_coil.tooltip_heat", "§cBase Heat Capacity: §f%d K");
        provider.add("tile.wire_coil.tooltip_smelter", "§8Multi Smelter:");
        provider.add("tile.wire_coil.tooltip_parallel_smelter", "  §5Max Parallel: §f%s");
        provider.add("tile.wire_coil.tooltip_energy_smelter", "  §aEnergy Usage: §f%s EU/t §8per recipe");
        provider.add("tile.wire_coil.tooltip_pyro", "§8Pyrolyse Oven:");
        provider.add("tile.wire_coil.tooltip_speed_pyro", "  §bProcessing Speed: §f%s%%");
        provider.add("tile.wire_coil.tooltip_cracking", "§8Cracking Unit:");
        provider.add(" tile.wire_coil.tooltip_energy_cracking", "  §aEnergy Usage: §f%s%%");
    }
}
