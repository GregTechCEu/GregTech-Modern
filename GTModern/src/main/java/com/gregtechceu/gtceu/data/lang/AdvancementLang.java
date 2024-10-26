package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class AdvancementLang {

    public static void init(RegistrateLangProvider provider) {
        // todo advancements
        /*
         * provider.add("gtceu.advancement.root_steam.name", "Steam Age");
         * provider.add("gtceu.advancement.root_steam.desc",
         * "Welcome to GregTech! Everything begins with your first copper ingots.");
         * provider.add("gtceu.advancement.steam.1_first_tools.name", "First Tools");
         * provider.add("gtceu.advancement.steam.1_first_tools.desc", "Craft a Hammer.");
         * provider.add("gtceu.advancement.steam.2_more_tools.name", "More Tools");
         * provider.add("gtceu.advancement.steam.2_more_tools.desc", "Craft a Wrench.");
         * provider.add("gtceu.advancement.steam.3_bronze_dust.name", "Bronze");
         * provider.add("gtceu.advancement.steam.3_bronze_dust.desc", "Craft bronze dust.");
         * provider.add("gtceu.advancement.steam.4_bronze_boiler.name", "First Steam");
         * provider.add("gtceu.advancement.steam.4_bronze_boiler.desc", "Craft a Bronze Boiler.");
         * provider.add("gtceu.advancement.steam.5_bronze_forge_hammer.name", "Cheaper than a Macerator");
         * provider.add("gtceu.advancement.steam.5_bronze_forge_hammer.desc", "Craft a Steam Forge Hammer.");
         * provider.add("gtceu.advancement.steam.6_bronze_alloy_smelter.name", "Alloy Smelter");
         * provider.add("gtceu.advancement.steam.6_bronze_alloy_smelter.desc", "Craft a Steam Alloy Smelter.");
         * provider.add("gtceu.advancement.steam.7_bronze_extractor.name", "Extract");
         * provider.add("gtceu.advancement.steam.7_bronze_extractor.desc", "Craft a Steam Extractor.");
         * provider.add("gtceu.advancement.steam.8_bronze_solar_boiler.name", "Simply Eco");
         * provider.add("gtceu.advancement.steam.8_bronze_solar_boiler.desc", "Craft a Solar Boiler.");
         * provider.add("gtceu.advancement.steam.9_coke_oven.name", "Coke Oven");
         * provider.add("gtceu.advancement.steam.9_coke_oven.desc", "Craft a Coke Oven.");
         * provider.add("gtceu.advancement.steam.10_vacuum_tube.name", "Tubes");
         * provider.add("gtceu.advancement.steam.10_vacuum_tube.desc", "Craft a Vacuum Tube.");
         * provider.add("gtceu.advancement.steam.11_rubber.name", "Rubber");
         * provider.add("gtceu.advancement.steam.11_rubber.desc",
         * "Make Rubber in an alloy smelter using Sulfur and Raw Rubber Pulp, obtained from Rubber Trees.");
         * provider.add("gtceu.advancement.steam.12_electronic_circuit.name", "Basic Circuit");
         * provider.add("gtceu.advancement.steam.12_electronic_circuit.desc", "Craft an Electronic Circuit.");
         * provider.add("gtceu.advancement.steam.13_steel.name", "Steel");
         * provider.add("gtceu.advancement.steam.13_steel.desc", "Produce Steel in a Primitive Blast Furnace.");
         * provider.add("gtceu.advancement.steam.14_magnetic_iron.name", "Magnetic Iron");
         * provider.add("gtceu.advancement.steam.14_magnetic_iron.desc", "Craft a Magnetic Iron Rod with 4 Redstone.");
         * provider.add("gtceu.advancement.steam.15_lv_motor.name", "Low Voltage Motor");
         * provider.add("gtceu.advancement.steam.15_lv_motor.desc", "Craft a Low Voltage Motor.");
         * provider.add("gtceu.advancement.steam.16_steel_boiler.name", "High Pressure");
         * provider.add("gtceu.advancement.steam.16_steel_boiler.desc", "Craft a High Pressure Boiler.");
         * provider.add("gtceu.advancement.steam.81_crafting_station.name", "Crafting Station");
         * provider.add("gtceu.advancement.steam.81_crafting_station.desc",
         * "Craft a Crafting Station to make complex crafts much more manageable.");
         * provider.add("gtceu.advancement.steam.83_hp_solar_boiler.name", "Solar OP Pls Nerf");
         * provider.add("gtceu.advancement.steam.83_hp_solar_boiler.desc", "Craft a High Pressure Solar Boiler.");
         * provider.add("gtceu.advancement.steam.85_steam_vent_death.name", "Get out of the way!");
         * provider.add("gtceu.advancement.steam.85_steam_vent_death.desc", "Die to a venting Steam Machine.");
         * provider.add("gtceu.advancement.steam.87_fluid_pipe_death_heat.name", "Boiling Hot!");
         * provider.add("gtceu.advancement.steam.87_fluid_pipe_death_heat.desc",
         * "Die to a Fluid Pipe full of Hot Fluid.");
         * provider.add("gtceu.advancement.steam.90_primitive_pump.name", "Primitive Water Pump");
         * provider.add("gtceu.advancement.steam.90_primitive_pump.desc",
         * "Craft a Primitive Water Pump for early water gathering.");
         * provider.add("gtceu.advancement.root_lv.name", "Low Voltage");
         * provider.add("gtceu.advancement.root_lv.desc", "Craft a Basic Steam Turbine");
         * provider.add("gtceu.advancement.low_voltage.17_lv_pump.name", "Pump");
         * provider.add("gtceu.advancement.low_voltage.17_lv_pump.desc", "Craft an LV Pump.");
         * provider.add("gtceu.advancement.low_voltage.18_shutter_cover.name", "Close it!");
         * provider.add("gtceu.advancement.low_voltage.18_shutter_cover.desc", "Get a Shutter Cover.");
         * provider.add("gtceu.advancement.low_voltage.19_lv_pump_block.name", "Slurp");
         * provider.add("gtceu.advancement.low_voltage.19_lv_pump_block.desc", "Craft a Basic Pump.");
         * provider.add("gtceu.advancement.low_voltage.20_lv_conveyor.name", "Transport");
         * provider.add("gtceu.advancement.low_voltage.20_lv_conveyor.desc", "Craft an LV Conveyor.");
         * provider.add("gtceu.advancement.low_voltage.21_machine_controller_cover.name", "Manipulation");
         * provider.add("gtceu.advancement.low_voltage.21_machine_controller_cover.desc", "Get a Machine Controller.");
         * provider.add("gtceu.advancement.low_voltage.22_lv_robot_arm.name", "Complex Machines");
         * provider.add("gtceu.advancement.low_voltage.22_lv_robot_arm.desc", "Craft an LV Robot Arm.");
         * provider.add("gtceu.advancement.low_voltage.23_lv_assembler.name", "Avengers, Assemble!");
         * provider.add("gtceu.advancement.low_voltage.23_lv_assembler.desc", "Craft an LV Assembler.");
         * provider.add("gtceu.advancement.low_voltage.24_smart_filter.name", "Filter and Regulate");
         * provider.add("gtceu.advancement.low_voltage.24_smart_filter.desc", "Get a Smart Filter.");
         * provider.add("gtceu.advancement.low_voltage.25_large_boiler.name", "Extreme Pressure");
         * provider.add("gtceu.advancement.low_voltage.25_large_boiler.desc", "Set up a Large Boiler.");
         * provider.add("gtceu.advancement.low_voltage.26_arc_furnace.name", "Recycling");
         * provider.add("gtceu.advancement.low_voltage.26_arc_furnace.desc", "Craft an Arc Furnace.");
         * provider.add("gtceu.advancement.low_voltage.27_electric_blast_furnace.name", "Electric Blast Furnace");
         * provider.add("gtceu.advancement.low_voltage.27_electric_blast_furnace.desc",
         * "Craft an Electric Blast Furnace.");
         * provider.add("gtceu.advancement.low_voltage.28_lv_energy_hatch.name", "You Need Two Of Them");
         * provider.add("gtceu.advancement.low_voltage.28_lv_energy_hatch.desc", "Craft an LV Energy Hatch.");
         * provider.add("gtceu.advancement.low_voltage.29_lv_battery_buffer.name", "Batteries");
         * provider.add("gtceu.advancement.low_voltage.29_lv_battery_buffer.desc", "Craft an LV 4A Battery Buffer.");
         * provider.add("gtceu.advancement.low_voltage.30_good_electronic_circuit.name", "Better Circuits");
         * provider.add("gtceu.advancement.low_voltage.30_good_electronic_circuit.desc", "Get Good Circuits.");
         * provider.add("gtceu.advancement.low_voltage.86_electrocution_death.name", "Shoulda Covered your Wires!");
         * provider.add("gtceu.advancement.low_voltage.86_electrocution_death.desc", "Die to an Uninsulated Wire.");
         * provider.add("gtceu.advancement.low_voltage.88_first_cover_place.name", "The First of Many");
         * provider.add("gtceu.advancement.low_voltage.88_first_cover_place.desc", "Place your first Machine Cover.");
         * provider.add("gtceu.advancement.root_mv.name", "Medium Voltage");
         * provider.add("gtceu.advancement.root_mv.desc", "Produce an Aluminium Ingot.");
         * provider.add("gtceu.advancement.medium_voltage.31_mv_energy_hatch.name", "Upgrade Your EBF");
         * provider.add("gtceu.advancement.medium_voltage.31_mv_energy_hatch.desc", "Craft an MV Energy Hatch.");
         * provider.add("gtceu.advancement.medium_voltage.32_electric_drill.name", "Drill Time");
         * provider.add("gtceu.advancement.medium_voltage.32_electric_drill.desc", "Craft a Drill.");
         * provider.add("gtceu.advancement.medium_voltage.33_chainsaw.name", "Brrrr...");
         * provider.add("gtceu.advancement.medium_voltage.33_chainsaw.desc", "Craft a Chainsaw.");
         * provider.add("gtceu.advancement.medium_voltage.34_silicon_boule.name", "Monocrystalline Silicon Boule");
         * provider.add("gtceu.advancement.medium_voltage.34_silicon_boule.desc",
         * "Produce a Monocrystalline Silicon Boule.");
         * provider.add("gtceu.advancement.medium_voltage.35_logic_circuit_wafer.name", "Logic Circuit Wafer");
         * provider.add("gtceu.advancement.medium_voltage.35_logic_circuit_wafer.desc",
         * "Produce a Logic Circuit Wafer.");
         * provider.add("gtceu.advancement.medium_voltage.36_integrated_logic_circuit.name",
         * "Integrated Logic Circuit");
         * provider.add("gtceu.advancement.medium_voltage.36_integrated_logic_circuit.desc",
         * "Produce an Integrated Logic Circuit.");
         * provider.add("gtceu.advancement.medium_voltage.37_advanced_integrated_logic_circuit.name", "Step Forward");
         * provider.add("gtceu.advancement.medium_voltage.37_advanced_integrated_logic_circuit.desc",
         * "Obtain Advanced Circuits.");
         * provider.add("gtceu.advancement.medium_voltage.38_super_chest.name", "New Storage");
         * provider.add("gtceu.advancement.medium_voltage.38_super_chest.desc", "Craft a Super Chest I.");
         * provider.add("gtceu.advancement.medium_voltage.39_super_tank.name", "Where is the Ocean?");
         * provider.add("gtceu.advancement.medium_voltage.39_super_tank.desc", "Build a Super Tank I.");
         * provider.add("gtceu.advancement.root_hv.name", "High Voltage");
         * provider.add("gtceu.advancement.root_hv.desc", "Produce a Stainless Steel Ingot.");
         * provider.add("gtceu.advancement.high_voltage.40_workstation.name", "Workstations");
         * provider.add("gtceu.advancement.high_voltage.40_workstation.desc", "Get Workstations.");
         * provider.add("gtceu.advancement.high_voltage.41_vacuum_freezer.name", "Vacuum Freezer");
         * provider.add("gtceu.advancement.high_voltage.41_vacuum_freezer.desc", "Set up a Vacuum Freezer.");
         * provider.add("gtceu.advancement.high_voltage.42_kanthal_coil.name", "Upgrade your Coils to Level II");
         * provider.add("gtceu.advancement.high_voltage.42_kanthal_coil.desc", "Craft a Kanthal Heating Coil.");
         * provider.add("gtceu.advancement.high_voltage.43_multi_smelter.name", "High Power Smelter");
         * provider.add("gtceu.advancement.high_voltage.43_multi_smelter.desc", "Set up a Multi Smelter.");
         * provider.add("gtceu.advancement.high_voltage.44_distillation_tower.name", "Oil Plant");
         * provider.add("gtceu.advancement.high_voltage.44_distillation_tower.desc", "Start up a Distillation Tower.");
         * provider.add("gtceu.advancement.high_voltage.45_large_steam_turbine.name", "So Much Steam");
         * provider.add("gtceu.advancement.high_voltage.45_large_steam_turbine.desc",
         * "Start up a Large Steam Turbine.");
         * provider.add("gtceu.advancement.high_voltage.46_hv_macerator.name", "Universal Macerator");
         * provider.add("gtceu.advancement.high_voltage.46_hv_macerator.desc",
         * "Craft an HV Macerator for ore byproducts.");
         * provider.add("gtceu.advancement.high_voltage.82_large_chemical_reactor.name", "Large Chemical Reactor");
         * provider.add("gtceu.advancement.high_voltage.82_large_chemical_reactor.desc",
         * "Set up a Large Chemical Reactor for more efficient chemistry.");
         * provider.add("gtceu.advancement.high_voltage.84_rotor_holder_open.name", "A Painful Way to Go Out");
         * provider.add("gtceu.advancement.high_voltage.84_rotor_holder_open.desc",
         * "Die by opening a spinning Rotor Holder.");
         * provider.add("gtceu.advancement.high_voltage.89_fluid_pipe_death_cold.name", "Freezing Cold!");
         * provider.add("gtceu.advancement.high_voltage.89_fluid_pipe_death_cold.desc",
         * "Die to a Fluid Pipe full of Cold Fluid.");
         * provider.add("gtceu.advancement.root_ev.name", "Extreme Voltage");
         * provider.add("gtceu.advancement.root_ev.desc", "Cool down a Hot Titanium Ingot.");
         * provider.add("gtceu.advancement.extreme_voltage.47_nichrome_coil.name", "Upgrade your Coils to Level III");
         * provider.add("gtceu.advancement.extreme_voltage.47_nichrome_coil.desc", "Craft a Nichrome Heating Coil.");
         * provider.add("gtceu.advancement.extreme_voltage.48_osmium.name", "Osmium");
         * provider.add("gtceu.advancement.extreme_voltage.48_osmium.desc", "Cool down a Hot Osmium Ingot.");
         * provider.add("gtceu.advancement.extreme_voltage.49_nano_cpu_wafer.name", "Nano CPU Wafer");
         * provider.add("gtceu.advancement.extreme_voltage.49_nano_cpu_wafer.desc", "Produce a Nano CPU Wafer.");
         * provider.add("gtceu.advancement.extreme_voltage.50_nano_processor.name", "Nano Processor");
         * provider.add("gtceu.advancement.extreme_voltage.50_nano_processor.desc", "Get Nano Processors.");
         * provider.add("gtceu.advancement.extreme_voltage.51_large_combustion_engine.name", "Large Combustion Engine");
         * provider.add("gtceu.advancement.extreme_voltage.51_large_combustion_engine.desc",
         * "Set up a Large Combustion Engine, supply it with Lubricant, and boost it with Oxygen.");
         * provider.add("gtceu.advancement.extreme_voltage.52_soc_wafer.name", "SoC Wafer");
         * provider.add("gtceu.advancement.extreme_voltage.52_soc_wafer.desc",
         * "Produce an SoC Wafer to make cheaper Basic and Good Circuits.");
         * provider.add("gtceu.advancement.root_iv.name", "Insane Voltage");
         * provider.add("gtceu.advancement.root_iv.desc", "Cool down a Hot Tungstensteel Ingot.");
         * provider.add("gtceu.advancement.insane_voltage.53_plutonium_239.name", "Plutonium 239");
         * provider.add("gtceu.advancement.insane_voltage.53_plutonium_239.desc",
         * "Obtain Plutonium 239 for a source of radon.");
         * provider.add("gtceu.advancement.insane_voltage.54_indium.name", "Indium");
         * provider.add("gtceu.advancement.insane_voltage.54_indium.desc", "Obtain Indium from Sphalerite and Galena.");
         * provider.add("gtceu.advancement.insane_voltage.55_qbit_cpu_wafer.name", "QBit CPU Wafer");
         * provider.add("gtceu.advancement.insane_voltage.55_qbit_cpu_wafer.desc", "Produce a QBit CPU Wafer.");
         * provider.add("gtceu.advancement.insane_voltage.56_quantum_processor.name", "Quantum Processor");
         * provider.add("gtceu.advancement.insane_voltage.56_quantum_processor.desc", "Get Quantum Processors.");
         * provider.add("gtceu.advancement.insane_voltage.57_tungstensteel_coil.name",
         * "Upgrade your Coils to Level IV");
         * provider.add("gtceu.advancement.insane_voltage.57_tungstensteel_coil.desc",
         * "Craft an RTM Alloy Heating Coil.");
         * provider.add("gtceu.advancement.insane_voltage.58_hss_g_coil.name", "Upgrade your Coils to Level V");
         * provider.add("gtceu.advancement.insane_voltage.58_hss_g_coil.desc", "Craft an HSS-G Heating Coil.");
         * provider.add("gtceu.advancement.root_luv.name", "Ludicrous Voltage");
         * provider.add("gtceu.advancement.root_luv.desc", "Set up an Assembly Line.");
         * provider.add("gtceu.advancement.ludicrous_voltage.59_superconducting_coil.name", "Conducting");
         * provider.add("gtceu.advancement.ludicrous_voltage.59_superconducting_coil.desc",
         * "Craft a Superconducting Coil.");
         * provider.add("gtceu.advancement.ludicrous_voltage.60_fusion.name", "Fusion Reactor");
         * provider.add("gtceu.advancement.ludicrous_voltage.60_fusion.desc", "Set up a Fusion Reactor Mark 1.");
         * provider.add("gtceu.advancement.ludicrous_voltage.61_europium.name", "Advancement in Technology");
         * provider.add("gtceu.advancement.ludicrous_voltage.61_europium.desc", "Produce Europium.");
         * provider.add("gtceu.advancement.ludicrous_voltage.62_raw_crystal_chip.name", "Raw Crystal Chip");
         * provider.add("gtceu.advancement.ludicrous_voltage.62_raw_crystal_chip.desc", "Produce a Raw Crystal Chip.");
         * provider.add("gtceu.advancement.ludicrous_voltage.63_crystal_processing_unit.name",
         * "Crystal Processing Unit");
         * provider.add("gtceu.advancement.ludicrous_voltage.63_crystal_processing_unit.desc",
         * "Produce a Crystal Processing Unit.");
         * provider.add("gtceu.advancement.ludicrous_voltage.64_crystal_processor.name", "Crystal Processor");
         * provider.add("gtceu.advancement.ludicrous_voltage.64_crystal_processor.desc", "Get Crystal Processors.");
         * provider.add("gtceu.advancement.ludicrous_voltage.65_naquadah.name", "Stargate Material");
         * provider.add("gtceu.advancement.ludicrous_voltage.65_naquadah.desc", "Cool down a Hot Naquadah Ingot.");
         * provider.add("gtceu.advancement.ludicrous_voltage.66_naquadah_coil.name", "Upgrade your Coils to Level VI");
         * provider.add("gtceu.advancement.ludicrous_voltage.66_naquadah_coil.desc", "Craft a Naquadah Heating Coil.");
         * provider.add("gtceu.advancement.ludicrous_voltage.67_asoc_wafer.name", "ASoC Wafer");
         * provider.add("gtceu.advancement.ludicrous_voltage.67_asoc_wafer.desc",
         * "Produce an ASoC Wafer to make cheaper Extreme and Advanced Circuits.");
         * provider.add("gtceu.advancement.ludicrous_voltage.68_large_plasma_turbine.name", "Large Plasma Turbine");
         * provider.add("gtceu.advancement.ludicrous_voltage.68_large_plasma_turbine.desc",
         * "Craft a Plasma Turbine to turn Plasma into Usable Fluid.");
         * provider.add("gtceu.advancement.root_zpm.name", "Zero Point Module");
         * provider.add("gtceu.advancement.root_zpm.desc", "Set up a Fusion Reactor Mark 2.");
         * provider.add("gtceu.advancement.zero_point_module.69_americium.name", "Going for the Limit");
         * provider.add("gtceu.advancement.zero_point_module.69_americium.desc", "Produce Americium.");
         * provider.add("gtceu.advancement.zero_point_module.70_stem_cells.name", "Stem Cells");
         * provider.add("gtceu.advancement.zero_point_module.70_stem_cells.desc", "Produce Stem Cells.");
         * provider.add("gtceu.advancement.zero_point_module.71_neuro_processing_unit.name", "Neuro Processing Unit");
         * provider.add("gtceu.advancement.zero_point_module.71_neuro_processing_unit.desc",
         * "Produce a Neuro Processing Unit.");
         * provider.add("gtceu.advancement.zero_point_module.72_wetware_processor.name", "Wetware Processor");
         * provider.add("gtceu.advancement.zero_point_module.72_wetware_processor.desc", "Get Wetware Processors.");
         * provider.add("gtceu.advancement.zero_point_module.73_trinium_coil.name", "Over 9000!");
         * provider.add("gtceu.advancement.zero_point_module.73_trinium_coil.desc", "Craft a Trinium Heating Coil.");
         * provider.add("gtceu.advancement.root_uv.name", "Ultimate Voltage");
         * provider.add("gtceu.advancement.root_uv.desc", "Produce Tritanium.");
         * provider.add("gtceu.advancement.ultimate_voltage.74_wetware_mainframe.name", "Wetware Mainframe");
         * provider.add("gtceu.advancement.ultimate_voltage.74_wetware_mainframe.desc", "Get a Wetware Mainframe.");
         * provider.add("gtceu.advancement.ultimate_voltage.75_fusion_reactor_3.name", "A Sun Down on Earth");
         * provider.add("gtceu.advancement.ultimate_voltage.75_fusion_reactor_3.desc",
         * "Set up a Fusion Reactor Mark 3.");
         * provider.add("gtceu.advancement.ultimate_voltage.76_neutronium.name", "As Dense As Possible");
         * provider.add("gtceu.advancement.ultimate_voltage.76_neutronium.desc", "Produce Neutronium.");
         * provider.add("gtceu.advancement.ultimate_voltage.77_ultimate_battery.name", "What Now?");
         * provider.add("gtceu.advancement.ultimate_voltage.77_ultimate_battery.desc", "Craft an Ultimate Battery.");
         * provider.add("gtceu.advancement.ultimate_voltage.78_hasoc_wafer.name", "HASoC Wafer");
         * provider.add("gtceu.advancement.ultimate_voltage.78_hasoc_wafer.desc",
         * "Produce an HASoC Wafer to make cheaper Master Circuits.");
         * provider.add("gtceu.advancement.ultimate_voltage.79_tritanium_coil.name", "The Final Coil");
         * provider.add("gtceu.advancement.ultimate_voltage.79_tritanium_coil.desc", "Craft a Tritanium Heating Coil.");
         */
    }
}
