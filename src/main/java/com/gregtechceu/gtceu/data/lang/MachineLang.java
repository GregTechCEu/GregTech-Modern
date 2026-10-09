package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class MachineLang {

    public static void init(GTLangProvider provider) {
        generateGeneralMachineLang(provider);
        generateMachineTooltips(provider);
        generateCustomMachineLang(provider);

        generateCustomMultiblockLang(provider);
        generateCustomMultiblockPartLang(provider);

        generateMultiblockKeys(provider);
        generateCentralMonitorGuiLang(provider);
    }

    private static void generateMachineTooltips(GTLangProvider provider) {}

    private static void generateGeneralMachineLang(GTLangProvider provider) {
        // General Machine Behavior
        provider.add("machine.gtceu.basic.input_from_output_side.allow", "Allow Input from Output Side: ");
        provider.add("machine.gtceu.basic.input_from_output_side.disallow",
                "Disallow Input from Output Side: ");
        provider.add("machine.gtceu.muffle.on", "Sound Muffling: Enabled");
        provider.add("machine.gtceu.muffle.off", "Sound Muffling: Disabled");
        provider.add("machine.gtceu.perfect_oc", "Does not lose energy efficiency when overclocked.");

        provider.add("gui.gtceu.auto_output.fluid.tooltip.enabled", "Fluid Auto-Output Enabled");
        provider.add("gui.gtceu.auto_output.fluid.tooltip.disabled", "Fluid Auto-Output Disabled");
        provider.add("gui.gtceu.auto_input.fluid.tooltip.enabled", "Fluid Auto-Input Enabled");
        provider.add("gui.gtceu.auto_input.fluid.tooltip.disabled", "Fluid Auto-Input Disabled");

        provider.add("gui.gtceu.auto_output.item.tooltip.enabled", "Item Auto-Output Enabled");
        provider.add("gui.gtceu.auto_output.item.tooltip.disabled", "Item Auto-Output Disabled");
        provider.add("gui.gtceu.auto_input.item.tooltip.enabled", "Item Auto-Input Enabled");
        provider.add("gui.gtceu.auto_input.item.tooltip.disabled", "Item Auto-Input Disabled");

        provider.addMultiline("gui.gtceu.charger_slot.tooltip",
                "§fCharger Slot§r\n§7Draws power from %s batteries§r\n§7Charges %s tools and batteries");
        provider.addMultiline("gui.gtceu.configurator_slot.tooltip",
                "§fConfigurator Slot§r\n§7Place a §6Programmed Circuit§7 in this slot to\n§7change its configured value.\n§7Hold §6Shift§7 when clicking buttons to change by §65.\n§aA Programmed Circuit in this slot is also valid for recipe inputs.§r");

        provider.add("gui.gtceu.fluid_locking.enabled", "Fluid Locking Enabled");
        provider.add("gui.gtceu.fluid_locking.disabled", "Fluid Locking Disabled");

        provider.add("gui.gtceu.fluid_voiding.enabled", "Fluid Voiding Enabled");
        provider.add("gui.gtceu.fluid_voiding.disabled", "Fluid Voiding Disabled");

        provider.add("gui.gtceu.item_locking.enabled", "Item Locking Enabled");
        provider.add("gui.gtceu.item_locking.disabled", "Item Locking Disabled");

        provider.add("gui.gtceu.item_voiding.enabled", "Item Voiding Enabled");
        provider.add("gui.gtceu.item_voiding.disabled", "Item Voiding Disabled");

        // EIO style gui

        provider.add("gui.gtceu.cover_settings", "Cover Settings");
        provider.add("gui.gtceu.output_settings", "Output Settings");
        provider.add("gui.gtceu.circuit_settings", "Circuit Settings");
        provider.addMultiLang("gui.gtceu.output_settings_tune.tooltips", "left-click to tune the item auto output",
                "right-click to tune the fluid auto output.");
        provider.add("gui.gtceu.auto_output.item.allow_input.enabled",
                "Allow items input from the output side.");
        provider.add("gui.gtceu.auto_output.item.allow_input.disabled",
                "Disable items input from the output side.");
        provider.add("gui.gtceu.auto_output.item.enabled", "Item Auto Output: §aEnabled");
        provider.add("gui.gtceu.auto_output.item.disabled", "Item Auto Output: §cDisabled");
        provider.addMultiLang("gui.gtceu.auto_output.item.unselected",
                """
                        Item Auto Output
                        §7Select a side of the machine to configure its output.
                        """);
        provider.addMultiLang("gui.gtceu.auto_output.item.other_direction",
                """
                        Item Auto Output: §6Other Direction
                        §7The machine's item output is set to another direction.
                        §7Click to move the output to the currently selected side.
                        """);
        provider.add("gui.gtceu.auto_output.fluid.allow_input.enabled",
                "Allow fluids input from the output side.");
        provider.add("gui.gtceu.auto_output.fluid.allow_input.disabled",
                "Disable fluids input from the output side.");
        provider.add("gui.gftceu.auto_output.fluid.enabled", "Fluid Auto Output: §aEnabled");
        provider.add("gui.gtceu.auto_output.fluid.disabled", "Fluid Auto Output: §cDisabled");
        provider.addMultiLang("gui.gtceu.auto_output.fluid.unselected",
                """
                        Fluid Auto Output
                        §7Select a side of the machine to configure its output.
                        """);
        provider.addMultiLang("gui.gtceu.auto_output.fluid.other_direction",
                """
                        Fluid Auto Output: §6Other Direction
                        §7The machine's fluid output is set to another direction.
                        §7Click to move the output to the currently selected side.
                        """);
        provider.add("gui.gtceu.directional_setting.title", "Directional Setting");
        provider.add("gui.gtceu.directional_setting.tab_tooltip", "Change Directional Setting");
    }

    /**
     * For Tooltips that are NOT in the form {@code mod.machine.tier_name.tooltip}
     */
    private static void generateCustomMachineLang(GTLangProvider provider) {
        // Steam Boilers
        provider.add("gui.gtceu.machine.boiler.heating_up", "§cHeating up§r%s");
        provider.add("gui.gtceu.machine.boiler.cooling_down", "§9Cooling down§r%s");
        provider.add("gui.gtceu.machine.boiler.production", "§aProducing %s§a mB/t");
        provider.add("gui.gtceu.machine.boiler.heat", "Heat Capacity: %s %%");

        // Machine Hull
        provider.add("machine.gtceu.hull.tooltip",
                "§7You just need §5I§dm§4a§cg§ei§an§ba§3t§7i§1o§5n§7 to use this");

        // Transformer
        provider.add("machine.gtceu.transformer.tooltip", "§7Transforms Energy between voltage tiers");
        provider.add("machine.gtceu.transformer.tooltip_tool_usage",
                "§7Starts as §fTransform Down§7, use Screwdriver to change");
        provider.add("machine.gtceu.transformer.tooltip_transform_down",
                "§aTransform Down: §f%dA %s EU (%s§f) -> %dA %s EU (%s§f)");
        provider.add("machine.gtceu.transformer.message_transform_down",
                "Transforming Down, In: %s EU %dA, Out: %s EU %dA");
        provider.add("machine.gtceu.transformer.tooltip_transform_up",
                "§cTransform Up: §f%dA %s EU (%s§f) -> %dA %s EU (%s§f)");
        provider.add("machine.gtceu.transformer.message_transform_up",
                "Transforming Up, In: %s EU %dA, Out: %s EU %dA");

        // Diode
        provider.add("machine.gtceu.diode.message", "Max Amperage throughput: %s");
        provider.add("machine.gtceu.diode.tooltip_tool_usage",
                "Hit with a Soft Mallet to change Amperage flow.");
        provider.add("machine.gtceu.diode.tooltip_general",
                "Allows Energy Flow in one direction and limits Amperage");
        provider.add("machine.gtceu.diode.tooltip_starts_at", "Starts as §f1A§7, use Soft Mallet to change");

        // Energy Converter
        provider.add("machine.gtceu.energy_converter.description", "Converts Energy between EU and FE");
        provider.add("machine.gtceu.energy_converter.tooltip_tool_usage",
                "Starts as §fFE Converter§7, use Soft Mallet to change");
        provider.add("machine.gtceu.energy_converter.tooltip_conversion_native",
                "§cNative Conversion: §f%d FE -> %dA %d EU (%s§f)");
        provider.add("machine.gtceu.energy_converter.message_conversion_native",
                "Converting Native Energy, In: %d FE, Out: %dA %d EU");
        provider.add("machine.gtceu.energy_converter.tooltip_conversion_eu",
                "§aEU Conversion: §f%dA %d EU (%s§f) -> %d Native");
        provider.add("machine.gtceu.energy_converter.message_conversion_eu",
                "Converting EU, In: %dA %d EU, Out: %d Native");

        // Pump
        provider.add("machine.gtceu.pump.tooltip", "§7The best way to empty Oceans!");
        provider.add("machine.gtceu.pump.tooltip_buckets", "§f%d §7ticks per Bucket");

        // Item Collector
        provider.add("machine.gtceu.item_collector.tooltip", "Collects Items around itself");
        provider.add("machine.gtceu.item_collector.gui.collect_range",
                "Collect within an area of %sx%s blocks");

        // Fisher
        provider.add("machine.gtceu.fisher.tooltip", "Costs string to fish. Consumes one string each time.");
        provider.add("machine.gtceu.fisher.speed", "Catches something every %d ticks");
        provider.add("machine.gtceu.fisher.requirement",
                "Requires a %dx%d centered square of water directly below.");

        // World Accelerator
        provider.add("machine.gtceu.world_accelerator.description",
                "Tick accelerates nearby blocks in one of 2 modes: §fTile Entity§7 or §fRandom Tick§7. Use Screwdriver to change mode.");
        provider.add("machine.gtceu.world_accelerator.working_area", "§bWorking Area:");
        provider.add("machine.gtceu.world_accelerator.working_area_tile",
                "  Block Entity Mode:§f Adjacent Blocks");
        provider.add("machine.gtceu.world_accelerator.working_area_random", "  Random Tick Mode:§f %dx%d");
        provider.add("machine.gtceu.world_accelerator.mode_tile", "Block Entity Mode");
        provider.add("machine.gtceu.world_accelerator.mode_entity", "Random Tick Mode");

        // Forming Press
        provider.add("gui.gtceu.machine.forming_press.naming.press", "§oNamed Press");
        provider.add("gui.gtceu.machine.forming_press.naming.to_name", "§oItem to Name");
        provider.add("gui.gtceu.machine.forming_press.naming.named", "§oNamed Item");

        // Scanner
        provider.add("gui.gtceu.machine.scanner.copy_stick_from", "§oStick to Copy");
        provider.add("gui.gtceu.machine.scanner.copy_stick_empty", "§oEmpty Stick");
        provider.add("gui.gtceu.machine.scanner.copy_stick_to", "§oCopy of Stick");

        /// Miners (& Large Miners)

        provider.addMultiline("gui.gtceu.machine.miner.silk_touch.enabled",
                "Silk Touch Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        provider.addMultiline("gui.gtceu.machine.miner.silk_touch.disabled",
                "Silk Touch Disabled: Click to Enable.\n§7Switching requires an idle machine.");
        provider.addMultiline("gui.gtceu.machine.miner.chunk_mode.enabled",
                "Chunk Mode Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        provider.addMultiline("gui.gtceu.machine.miner.chunk_mode.disabled",
                "Chunk Mode Disabled: Click to Enable.\n§7Switching requires an idle machine.");

        provider.add("machine.gtceu.miner.working_area", "§bWorking Area: §f%dx%d");
        provider.add("machine.gtceu.miner.chunk_mode", "Chunk Mode");
        provider.add("machine.gtceu.miner.silk_touch", "Silk Touch Mode");
        provider.add("machine.gtceu.miner.working_area_chunks", "§bWorking Area: §f%dx%d Chunks");
        provider.add("machine.gtceu.miner.working_area_max", "§bMax Working Area: §f%dx%d");
        provider.add("machine.gtceu.miner.working_area_chunks_max", "§bMax Working Area: §f%dx%d Chunks");
        provider.add("machine.gtceu.miner.tooltip", "§7Mines ores below the Miner! Starts as §f%sx%s §7area");

        provider.add("machine.gtceu.miner.per_block", "§7takes §f%ds §7per Block");

        provider.add("gui.gtceu.machine.miner.multi.modes", "Has Silk Touch and Chunk Aligned Modes.");
        provider.add("gui.gtceu.machine.miner.multi.production",
                "Produces §f3x§7 more crushed ore than a §fMacerator§7.");
        provider.add("gui.gtceu.machine.miner.fluid_usage", "Uses §f%d mB/t §7of §f%s§7, doubled per overclock.");
        provider.add("gui.gtceu.machine.miner.multi.description",
                "A multiblock mining machine that covers a large area and produces huge quantity of ore.");
        provider.add("gui.gtceu.machine.miner.x", "sX: %d, mX: %d");
        provider.add("gui.gtceu.machine.miner.y", "sY: %d, mY: %d");
        provider.add("gui.gtceu.machine.miner.z", "sZ: %d, mZ: %d");
        provider.add("gui.gtceu.machine.miner.progress", "Progress: %d/%d");
        provider.add("gui.gtceu.machine.large_miner.done", "Done!");
        provider.add("gui.gtceu.machine.large_miner.working", "Working...");
        provider.add("gui.gtceu.machine.large_miner.invfull", "Inventory Full!");
        provider.add("gui.gtceu.machine.large_miner.needspower", "Needs Power!");
        provider.add("gui.gtceu.machine.large_miner.vent", "Venting Blocked!");
        provider.add("gui.gtceu.machine.large_miner.steam", "Needs Steam!");
        provider.add("gui.gtceu.machine.large_miner.errorradius", "§cCannot change radius while working!");
        provider.add("gui.gtceu.machine.large_miner.needsfluid", "Needs Drilling Fluid");

        // Buffer
        provider.add("machine.gtceu.buffer.tooltip", "A Small Buffer to store Items and Fluids");

        /// Block Breaker

        provider.add("machine.gtceu.block_breaker.tooltip",
                "§7Mines block on front face and collects its drops");
        provider.add("machine.gtceu.block_breaker.speed_bonus", "§eSpeed Bonus: §f%d%%");

        /// Creative Machines

        provider.add("gui.gtceu.machine.creative_tooltip.1", "§7You just need");
        provider.add("gui.gtceu.machine.creative_tooltip.2", " Creative Mode");
        provider.add("gui.gtceu.machine.creative_tooltip.3", "§7 to use this");

        provider.add("machine.gtceu.quantum_chest.tooltip", "§7Better than Storage Drawers");
        provider.add("machine.gtceu.quantum_chest.items_stored", "Item Amount:");
        provider.add("machine.gtceu.quantum_tank.tooltip", "§7Compact place to store all your fluids");

        provider.add("gui.gtceu.machine.creative_chest.item", "Item");
        provider.add("gui.gtceu.machine.creative_chest.ipc", "Items per Cycle");
        provider.add("gui.gtceu.machine.creative_chest.tpc", "Ticks per Cycle");
        provider.add("gui.gtceu.machine.creative_tank.fluid", "Fluid");
        provider.add("gui.gtceu.machine.creative_tank.mbpc", "mB per Cycle");
        provider.add("gui.gtceu.machine.creative_tank.tpc", "Ticks per Cycle");
        provider.add("gui.gtceu.machine.creative_energy.amperage", "Amperage");
        provider.add("gui.gtceu.machine.creative_energy.voltage", "Voltage");
        provider.add("gui.gtceu.machine.creative_energy.sink", "Sink");
        provider.add("gui.gtceu.machine.creative_energy.source", "Source");
        provider.add("gui.gtceu.machine.creative_computation.average", "Average Requested CWUt");
        provider.add("gui.gtceu.machine.creative.activity.on", "Active");
        provider.add("gui.gtceu.machine.creative.activity.off", "Not active");

        /// Fisher
        provider.addMultiline("gui.gtceu.machine.fisher.fisher_mode",
                "Toggle junk items\nOff costs 2 string per operation");

        /// Drums
        provider.add("machine.gtceu.drum.enable_output", "Will drain Fluid to downward adjacent Tanks");
        provider.add("machine.gtceu.drum.disable_output", "Will not drain Fluid");

        // Long Distance Pipeline
        provider.addMultiLang("machine.gtceu.long_distance_pipe.tooltip",
                "Connect with §fLong Distance Pipe§7 blocks to create a pipeline.",
                "Pipelines must have exactly §f1 Input§7 and §f1 Output§7 endpoint.",
                "Only pipeline endpoints need to be §fchunk-loaded§7.");
        provider.add("gui.gtceu.machine.ldpl.no_network", "No network found");
        provider.add("gui.gtceu.machine.ldpl.input_endpoint", "Input Endpoint");
        provider.add("gui.gtceu.machine.ldpl.output_endpoint", "Output Endpoint");
        provider.add("gui.gtceu.machine.ldpl.network_header", "Network:");
        provider.add("gui.gtceu.machine.ldpl.pipe_count", " - Pipes: %s");
        provider.add("gui.gtceu.machine.ldpl.input_pos", " - Input: %s");
        provider.add("gui.gtceu.machine.ldpl.output_pos", " - Output: %s");
        provider.add("machine.gtceu.long_distance_pipe.tooltip.min_length", "§bMinimum Endpoint Distance: §f%d Blocks");
    }

    private static void generateCustomMultiblockLang(GTLangProvider provider) {
        // Primitive Water Pump
        provider.add("machine.gtceu.primitive_pump.tooltip", "Endervoir at Home");
        provider.add("gtceu.multiblock.primitive_water_pump.description",
                "The Primitive Water Pump is a pre-Steam Era multiblock that collects water once per second, depending on the Biome it is in. It can use a Pump, ULV, or LV Output Hatch, increasing the amount of water per tier. Follows the formula: Biome Coefficient * Hatch Multiplier.");
        provider.addMultiline("gtceu.multiblock.primitive_water_pump.extra1",
                "Biome Coefficient:\n  Ocean, River: 1000 mB/s\n  Swamp: 800 mB/s\n  Jungle: 350 mB/s\n  Snowy: 300 mB/s\n  Plains, Forest: 250 mB/s\n  Taiga: 175 mB/s\n  Beach: 170 mB/s\n  Other: 100 mB/s");
        provider.addMultiline("gtceu.multiblock.primitive_water_pump.extra2",
                "Hatch Multipliers:\n  Pump Hatch: 1x\n  ULV Output Hatch: 2x\n  LV Output Hatch: 4x\n\nWhile raining in the Pump's Biome, the total water production will be increased by 50%%.");

        // Electric Blast Furnace
        provider.add("gtceu.multiblock.blast_furnace.max_temperature", "Heat Capacity: %s");

        // General Steam Multiblocks
        provider.add("gtceu.multiblock.steam.low_steam", "Not enough Steam to run!");
        provider.add("gtceu.multiblock.steam.steam_stored", "Steam: %s / %s mb");
        provider.add("gtceu.multiblock.steam.duration_modifier",
                "Takes §f1.5x §7base duration to process, not affected by number of items.");
        provider.add("machine.gtceu.steam.steam_hatch.tooltip", "§eAccepted Fluid: §fSteam");
        provider.add("machine.gtceu.steam_bus.tooltip", "Does not work with non-steam multiblocks");
        provider.add("gtceu.multiblock.require_steam_parts", "Requires Steam Hatches and Buses!");

        // Large Boiler
        provider.add("gtceu.multiblock.large_boiler.max_temperature",
                "Max Temperature: %dK, Steam Production: %dmB/t");
        provider.add("gtceu.multiblock.large_boiler.efficiency", "Efficiency: %s");
        provider.add("gtceu.multiblock.large_boiler.temperature", "Temperature: %sK / %sK");
        provider.add("gtceu.multiblock.large_boiler.steam_output", "Steam Output: %s mB/t");
        provider.add("gtceu.multiblock.large_boiler.throttle", "Throttle: %d");
        provider.add("gtceu.multiblock.large_boiler.throttle.tooltip",
                "Boiler can output less Steam and consume less fuel (efficiency is not lost, does not affect heat-up time)");
        provider.add("gtceu.multiblock.large_boiler.throttle_modify", "Modify Throttle:");
        provider.add("gtceu.multiblock.large_boiler.rate_tooltip",
                "§7Produces §f%d L §7of Steam with §f1 Coal");
        provider.add("gtceu.multiblock.large_boiler.heat_time_tooltip", "§7Takes §f%d seconds §7to boiling up");
        provider.add("gtceu.multiblock.large_boiler.explosion_tooltip",
                "Will explode if provided Fuel with no Water");

        // Multi Smelter
        provider.add("machine.gtceu.multi_smelter.tooltip", "Just like the Oven at Home");
        provider.add("gtceu.multiblock.multi_furnace.description",
                "The Multi Smelter is a multiblock structure used for smelting massive amounts of items at once. Different tiers of coils provide a speed boost and energy efficiency gain. 32 is the base value of items smelted per operation, and can be multiplied by using higher level coils.");
        provider.add("gtceu.multiblock.multi_furnace.heating_coil_level", "Heating Coil Level: %s");
        provider.add("gtceu.multiblock.multi_furnace.heating_coil_discount", "Heating Coil EU Boost: %sx");

        // Large Chemical Reactor
        provider.add("gtceu.multiblock.large_chemical_reactor.description",
                "The Large Chemical Reactor performs chemical reactions at 100%% energy efficiency. Overclocks multiply both speed and energy by 4. The multiblock requires exactly 1 Cupronickel Coil Block, which must be placed adjacent to the PTFE Pipe casing located in the center.");

        // Vacuum Freezer
        provider.add("gtceu.multiblock.vacuum_freezer.description",
                "The Vacuum Freezer is a multiblock structure mainly used for freezing Hot Ingots into regular Ingots. However, it can also freeze other substances, such as Water.");

        // Pyrolyse Oven
        provider.add("gtceu.multiblock.pyrolyse_oven.speed", "Processing Speed: %s%%");

        // Cracker
        provider.add("gtceu.multiblock.cracking_unit.energy", "Energy Usage: %s%%");

        // Combustion Engine
        provider.add("machine.gtceu.large_combustion_engine.tooltip", "Fuel Ignition Chamber");
        provider.add("machine.gtceu.extreme_combustion_engine.tooltip", "Extreme Chemical Energy Releaser");
        provider.add("gtceu.machine.large_combustion_engine.tooltip.boost_regular",
                "Supply §f20 mB/s§7 of §bOxygen§7 to produce up to §f%s EU/t§7 at §f2x§7 fuel consumption.");
        provider.add("gtceu.machine.large_combustion_engine.tooltip.boost_extreme",
                "Supply §f80 mB/s§7 of §9Liquid Oxygen§7 to produce up to §f%s EU/t§7 at §f2x§7 fuel consumption.");
        provider.add("gtceu.multiblock.large_combustion_engine.description",
                "The Large Combustion Engine is a multiblock structure that acts as a Combustion Generator for EV power.");
        provider.add("gtceu.multiblock.extreme_combustion_engine.description",
                "The Extreme Combustion Engine is a multiblock structure that acts as a Combustion Generator for IV power.");
        provider.add("gtceu.multiblock.large_combustion_engine.lubricant_amount", "Lubricant Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.oxygen_amount", "Oxygen Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.liquid_oxygen_amount",
                "Liquid Oxygen Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.oxygen_boosted", "§bOxygen boosted.");
        provider.add("gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted",
                "§bLiquid Oxygen boosted.");
        provider.add("gtceu.multiblock.large_combustion_engine.obstructed", "Engine Intakes Obstructed.");

        // Turbines

        provider.add("machine.gtceu.steam_large_turbine.tooltip", "Do not put your Head in it");
        provider.add("machine.gtceu.gas_large_turbine.tooltip", "Not a Jet Engine");
        provider.add("machine.gtceu.plasma_large_turbine.tooltip", "Plasma Energy Siphon");
        provider.add("gtceu.multiblock.turbine.fuel_amount", "Fuel Amount: %sL (%s)");
        provider.add("gtceu.multiblock.turbine.rotor_speed", "Rotor Speed: %s/%s RPM");
        provider.add("gtceu.multiblock.turbine.rotor_durability", "Rotor Durability: %s%%");
        provider.add("gtceu.multiblock.turbine.efficiency", "Turbine Efficiency: %s%%");
        provider.add("gtceu.multiblock.turbine.energy_per_tick", "Energy Output: %s/%s EU/t");
        provider.add("gtceu.multiblock.turbine.energy_per_tick_maxed", "Energy Output: %s EU/t");
        provider.add("gtceu.multiblock.turbine.obstructed", "Turbine Face Obstructed");
        provider.add("gtceu.multiblock.turbine.efficiency_tooltip",
                "Each Rotor Holder above %s§7 adds §f10%% efficiency and multiplies EU/t by 2§7.");
        provider.add("gtceu.multiblock.turbine.fuel_needed", "Consumes %s per %s ticks");
        provider.add("gtceu.multiblock.large_turbine.description",
                "Large Turbines are multiblocks that generate power from steam, gases, and plasma by having them spin the turbine's rotor. Energy output is based on rotor efficiency and current speed of turbine. Gearbox casings are used in the center of the structure.");

        // Fusion Reactor
        provider.add("gtceu.machine.fusion_reactor.capacity", "§7Maximum Energy Storage: §e%sM EU");
        provider.add("gtceu.machine.fusion_reactor.overclocking",
                "Overclocks double energy and halve duration.");
        provider.add("gtceu.multiblock.fusion_reactor.energy", "EU: %d / %d");
        provider.add("gtceu.multiblock.fusion_reactor.heat", "Heat: %d");
        provider.add("gtceu.machine.luv_fusion_reactor.description",
                "The Fusion Reactor MK 1 is a large multiblock structure used for fusing elements into heavier ones. It can only use LuV, ZPM, and UV Energy Hatches. For every Hatch it has, its buffer increases by 10M EU, and has a maximum of 160M.");
        provider.add("gtceu.machine.zpm_fusion_reactor.description",
                "The Fusion Reactor MK 2 is a large multiblock structure used for fusing elements into heavier ones. It can only use ZPM and UV Energy Hatches. For every Hatch it has, its buffer increases by 20M EU, and has a maximum of 320M.");
        provider.add("gtceu.multiblock.uv_fusion_reactor.description",
                "The Fusion Reactor MK 3 is a large multiblock structure used for fusing elements into heavier ones. It can only use UV Energy Hatches. For every Hatch it has, its buffer increases by 40M EU, and has a maximum of 640M.");

        // Fluid Drilling Rig
        provider.add("gtceu.machine.fluid_drilling_rig.description",
                "§7Drills fluids from veins under bedrock.");
        provider.add("gtceu.machine.fluid_drilling_rig.production",
                "§eProduction Multiplier: §f%dx, %fx overclocked");
        provider.add("gtceu.machine.fluid_drilling_rig.depletion", "§bDepletion Rate: §f%s%%");
        provider.add("gtceu.multiblock.fluid_rig.drilled_fluid", "Fluid: %s");
        provider.add("gtceu.multiblock.fluid_rig.no_fluid_in_area", "None in Area.");
        provider.add("gtceu.multiblock.fluid_rig.fluid_amount", "Pumping Rate: %s");
        provider.add("gtceu.multiblock.fluid_rig.vein_depletion", "Vein Size: %s");
        provider.add("gtceu.multiblock.fluid_rig.vein_depleted", "Vein Depleted.");

        // Bedrock Miner
        provider.add("gtceu.machine.bedrock_ore_miner.description", "§7Drills ores from veins under bedrock.");
        provider.add("gtceu.machine.bedrock_ore_miner.production",
                "§eProduction Multiplier: §f%dx, %fx overclocked");
        provider.add("gtceu.machine.bedrock_ore_miner.depletion", "§bDepletion Rate: §f%s%%");
        provider.add("gtceu.multiblock.ore_rig.drilled_ores_list", "Ores:");
        provider.add("gtceu.multiblock.ore_rig.drilled_ore_entry", " - %s");
        provider.add("gtceu.multiblock.ore_rig.ore_amount", "Drilling Rate: %s");

        // Cleanroom
        provider.add("gtceu.machine.cleanroom.tooltip.hold_ctrl",
                "Hold CTRL to show additional Structure Information");
        provider.add("gtceu.machine.cleanroom.tooltip.ae2.channels",
                "Send up to §f8 AE2 Channels §7through §fHulls§7 in the walls.");
        provider.add("gtceu.machine.cleanroom.tooltip.ae2.no_channels",
                "Send §aAE2 Networks§7 through §fHulls§7 in the walls.");
        provider.add("gui.gtceu.machine.cleanroom.clean_state", "Status: §aCLEAN");
        provider.add("gui.gtceu.machine.cleanroom.dirty_state", "Status: §4CONTAMINATED");
        provider.add("gui.gtceu.machine.cleanroom.clean_amount", "Cleanliness: §a%s%%");

        // Power Substation
        provider.add("gui.gtceu.machine.power_substation.stored", "§7Stored: %s §7EU");
        provider.add("gui.gtceu.machine.power_substation.capacity", "§7Capacity: %s §7EU");
        provider.add("gui.gtceu.machine.power_substation.passive_drain", "§7Passive Drain: %s §7EU/t");
        provider.add("gui.gtceu.machine.power_substation.average_in", "§7Avg. Input: %s §7EU/t");
        provider.add("gui.gtceu.machine.power_substation.average_in_hover",
                "The average inserted EU into the Power Substation's internal energy bank");
        provider.add("gui.gtceu.machine.power_substation.average_out", "§7Avg. Output: %s §7EU/t");
        provider.add("gui.gtceu.machine.power_substation.average_out_hover",
                "The average extracted EU out of the Power Substation's internal energy bank");
        provider.add("gui.gtceu.machine.power_substation.time_to_fill", "§7Time to fill: %s");
        provider.add("gui.gtceu.machine.power_substation.time_to_drain", "§7Time to drain: %s");
        provider.add("gui.gtceu.machine.power_substation.time_forever", "Forever");

        // Active Transformer
        provider.addMultiLang("gtceu.machine.active_transformer.tooltip",
                "§7Transformers: Lasers in Disguise",
                "§7Can combine any number of Energy §fInputs§7 into any number of Energy §fOutputs§7.",
                "§7Can transmit power at incredible distance with",
                " Lasers§7.");
        provider.add("gui.gtceu.machine.active_transformer.average_in", "§bAvg. Input: §f%s EU/t");
        provider.add("gui.gtceu.machine.active_transformer.average_out", "§bAvg. Output: §f%s EU/t");
        provider.add("gui.gtceu.machine.active_transformer.max_input", "§aMax Input: §f%s EU/t");
        provider.add("gui.gtceu.machine.active_transformer.max_output", "§cMax Output: §f%s EU/t");
        provider.add("gui.gtceu.machine.active_transformer.danger_enabled", "§c§bDANGER: Explosive");

        // Research Station
        provider.add("gtceu.multiblock.research_station.description",
                "The Research Station is a multiblock structure used for researching much more complex Assembly Line Research Data. Any Research requiring a Data Orb or Data Module must be scanned in the Research Station. Requires Compute Work Units (CWU/t) to research recipes, which is supplied by High Performance Computing Arrays (HPCAs).");
        provider.add("gtceu.machine.research_station.researching", "Researching.");

        // Data Bank
        provider.add("gtceu.multiblock.data_bank.providing", "Providing data.");

        // HPCA
        provider.add("gtceu.multiblock.hpca.computation", "Providing: %s");
        provider.add("gtceu.multiblock.hpca.energy", "Using: %s / %s EU/t (%s)");
        provider.add("gtceu.multiblock.hpca.temperature", "Temperature: %s");
        provider.add("gtceu.multiblock.hpca.hover_for_info", "Hover for details");
        provider.add("gtceu.multiblock.hpca.error_damaged", "Damaged component in structure!");
        provider.add("gtceu.multiblock.hpca.error_temperature",
                "Temperature above 100C, components may be damaged!");
        provider.add("gtceu.multiblock.hpca.warning_temperature",
                "Temperature above 50C, components may be damaged at 100C!");
        provider.add("gtceu.multiblock.hpca.warning_temperature_active_cool", "Fully utilizing active coolers");
        provider.add("gtceu.multiblock.hpca.warning_structure_header", "Structure Warnings:");
        provider.add("gtceu.multiblock.hpca.warning_multiple_bridges",
                "- Multiple bridges in structure (provides no additional benefit)");
        provider.add("gtceu.multiblock.hpca.warning_no_computation", "- No computation providers");
        provider.add("gtceu.multiblock.hpca.warning_low_cooling", "- Not enough cooling");
        provider.add("gtceu.multiblock.hpca.info_max_computation", "Max CWU/t: %s");
        provider.add("gtceu.multiblock.hpca.info_max_cooling_demand", "Cooling Demand: %s");
        provider.add("gtceu.multiblock.hpca.info_max_cooling_available", "Cooling Available: %s");
        provider.add("gtceu.multiblock.hpca.info_max_coolant_required", "Coolant Needed: %s");
        provider.add("gtceu.multiblock.hpca.info_coolant_name", "PCB Coolant");
        provider.add("gtceu.multiblock.hpca.info_bridging_enabled", "Bridging Enabled");
        provider.add("gtceu.multiblock.hpca.info_bridging_disabled", "Bridging Disabled");

        // Multiblock Tanks
        provider.add("gtceu.machine.multiblock.tank.tooltip",
                "Fill and drain through the controller or tank valves.");
        provider.add("gtceu.machine.tank_valve.tooltip",
                "Use to fill and drain multiblock tanks. Auto outputs when facing down.");
        provider.add("gtceu.machine.fluid_tank.max_multiblock", "Max Multiblock Size: %dx%dx%d");
    }

    private static void generateCustomMultiblockPartLang(GTLangProvider provider) {
        // Maintenance Hatch
        provider.add("gtceu.machine.maintenance_hatch.tooltip", "For maintaining Multiblocks");
        provider.addMultiline("gtceu.machine.maintenance_hatch_configurable.tooltip",
                "For finer control over Multiblocks\nStarts with no Maintenance problems!");
        provider.add("gtceu.machine.maintenance_hatch_full_auto.tooltip",
                "For automatically maintaining Multiblocks");
        provider.addMultiLang("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip",
                "For automatically maintaining Multiblocks with Cleaning!", "Cleans as:");
        provider.add("gtceu.machine.maintenance_hatch_tool_slot.tooltip",
                "Click slot with empty hand when required tools are in inventory to solve problems");
        provider.add("gtceu.machine.maintenance_hatch_tape_slot.tooltip", "Insert Tape to prevent problems");
        provider.add("gtceu.maintenance.configurable_duration", "Duration: %fx");
        provider.add("gtceu.maintenance.configurable_duration.unchanged_description",
                "Recipes will run at normal speed. Change configuration to update.");
        provider.add("gtceu.maintenance.configurable_duration.changed_description",
                "Recipes will run with %fx duration, applied before overclocking.");
        provider.add("gtceu.maintenance.configurable_duration.modify", "Modify Duration:");
        provider.add("gtceu.maintenance.configurable_time", "Time: %fx");
        provider.add("gtceu.maintenance.configurable_time.unchanged_description",
                "Maintenance problems will occur at normal rate. Change configuration to update.");
        provider.add("gtceu.maintenance.configurable_time.changed_description",
                "Maintenance problems will occur at %fx the normal rate.");

        // Muffler Hatch
        provider.addMultiLang("gtceu.machine.muffler_hatch.tooltip", "Recovers waste from machines",
                "DO NOT OBSTRUCT THE OUTPUT!");
        provider.add("gtceu.muffler.recovery_tooltip", "§bRecovery Chance: §f%d%%");

        // Parallel Hatch
        provider.add("gtceu.machine.parallel_hatch.display", "Adjust the maximum parallel of the multiblock");
        provider.add("gtceu.multiblock.parallelizable.tooltip", "Can parallelize with Parallel Control Hatches.");
        provider.add("machine.gtceu.parallel_hatch.tooltip", "Allows to run up to %s recipes in parallel.");

        // Item/Fluid Hatches
        provider.add("gtceu.machine.item_bus.import.tooltip", "Item Input for Multiblocks");
        provider.add("gtceu.machine.item_bus.export.tooltip", "Item Output for Multiblocks");
        provider.add("gtceu.machine.fluid_hatch.import.tooltip", "Fluid Input for Multiblocks");
        provider.add("gtceu.machine.fluid_hatch.export.tooltip", "Fluid Output for Multiblocks");

        /// ME linked machines

        provider.add("gui.gtceu.machine.me_part.network.online", "Network Status: §2Online§r");
        provider.add("gui.gtceu.machine.me_part.network.offline", "Network Status: §4Offline§r");
        provider.add("gui.gtceu.machine.me_output.waiting_list", "Sending Queue:");
        provider.add("gui.gtceu.machine.me_input.config_slot", "§fConfig Slot§r");
        provider.add("gui.gtceu.machine.me_input.config_slot.set", "§7Click to §bset/select§7 config slot.§r");
        provider.add("gui.gtceu.machine.me_input.config_slot.scroll", "§7Scroll wheel to §achange§7 config amount.§r");
        provider.add("gui.gtceu.machine.me_input.config_slot.remove", "§7Right click to §4clear§7 config slot.§r");
        provider.add("gui.gtceu.machine.me_input.config_slot.set_only", "§7Click to §bset§7 config slot.§r");
        provider.add("gui.gtceu.machine.me_input.config_slot.auto_pull_managed", "§4Disabled:§7 Managed by Auto-Pull");
        provider.add("gui.gtceu.machine.me_input.auto_pull", "Click to toggle automatic item pulling from ME");

        // Pattern Buffer/Proxy
        provider.add("gui.gtceu.machine.pattern_buffer.proxy_bind",
                "§fBinding to a Pattern Buffer at %s %s %s");

        provider.add("gui.gtceu.machine.pattern_buffer.shared_inventory.title", "Shared Item Inventory");
        provider.add("gui.gtceu.machine.pattern_buffer.shared_inventory.desc.0",
                "Shares inserted items with all patterns within buffer!");
        provider.add("gui.gtceu.machine.pattern_buffer.shared_inventory.desc.1",
                "Allows powerful automation by storing catalysts");
        provider.add("gui.gtceu.machine.pattern_buffer.shared_tank.title", "Shared Tank Inventory");
        provider.add("gui.gtceu.machine.pattern_buffer.shared_tank.desc.0",
                "Shares inserted fluids/gasses/etc. with all patterns within buffer!");
        provider.add("gui.gtceu.machine.pattern_buffer.rename", "Rename Pattern Buffer");
        provider.add("gui.gtceu.machine.pattern_buffer.refund", "Return Stored Contents to AE2");

        // ME Hatches
        provider.add("gtceu.machine.me.item_export.tooltip", "Stores items directly into an ME network.");
        provider.add("gtceu.machine.me.fluid_export.tooltip", "Stores fluids directly into an ME network.");
        provider.add("gtceu.machine.me.fluid_import.tooltip",
                "Fetches fluids from an ME network automatically.");
        provider.add("gtceu.machine.me.item_import.tooltip", "Fetches items from an ME network automatically.");
        provider.add("gtceu.machine.me.export.tooltip",
                "Has infinite capacity before connecting to ME network.");

        // ME Stocking
        provider.addMultiLang("gtceu.machine.me.stocking_item.tooltip", "Retrieves items directly from the ME network",
                "Auto-Pull from ME mode will automatically stock the first 16 items in the ME system, updated every 5 seconds.");
        provider.addMultiLang("gtceu.machine.me.stocking_fluid.tooltip",
                "Retrieves fluids directly from the ME network",
                "Auto-Pull from ME mode will automatically stock the first 16 fluids in the ME system, updated every 5 seconds.");
        provider.add("gtceu.machine.me_import_item_hatch.configs.tooltip",
                "Keeps 16 item types in stock");
        provider.add("gtceu.machine.me_import_fluid_hatch.configs.tooltip",
                "Keeps 16 fluid types in stock");

        provider.add("gui.gtceu.machine.me_stocking.auto_pull.enabled",
                "Auto-Pull Enabled");
        provider.add("gui.gtceu.machine.me_stocking.auto_pull.disabled",
                "Auto-Pull Disabled");

        // ME Adv Stocking
        provider.add("gui.gtceu.machine.me.stocking_adv.min_item_count_title",
                "Min. Item Count");
        provider.add("gui.gtceu.machine.me.stocking_adv.min_fluid_count_title",
                "Min. Fluid Count");
        provider.add("gui.gtceu.machine.me.stocking_adv.min_item_count",
                "Minimum Item Stack Size for Automated Pulling");
        provider.add("gui.gtceu.machine.me.stocking_adv.min_fluid_count",
                "Minimum Fluid Stack Size for Automated Pulling");
        provider.add("gui.gtceu.machine.me.stocking_adv.ticks_per_cycle_title",
                "Ticks Per Cycle");
        provider.add("gui.gtceu.machine.me.stocking_adv.ticks_per_cycle",
                "Delay between item list updates");
        provider.add("gui.gtceu.machine.me.stocking_adv.title",
                "Configure Automatic Stocking");

        // ME Data Stick Behavior
        provider.add("gtceu.machine.me.copy_paste.tooltip",
                "Sneak right-click with Data Stick to copy settings, right-click to apply");
        provider.add("gtceu.machine.me.import_copy_settings",
                "Saved settings to Data Stick");
        provider.add("gtceu.machine.me.import_paste_settings",
                "Applied settings from Data Stick");
        provider.add("gtceu.machine.me.item_import.data_stick.name",
                "§oME Input Bus Configuration Data");
        provider.add("gtceu.machine.me.fluid_import.data_stick.name",
                "§oME Input Hatch Configuration Data");

        // Dual Hatch
        provider.add("gtceu.machine.dual_hatch.import.tooltip", "Item and Fluid Input for Multiblocks");
        provider.add("gtceu.machine.dual_hatch.export.tooltip", "Item and Fluid Output for Multiblocks");

        // Energy Hatch
        provider.add("gtceu.machine.energy_hatch.input.tooltip", "Energy Input for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.input_hi_amp.tooltip",
                "Multiple Ampere Energy Input for Multiblocks");
        provider.add("gtceu.machine.substation_hatch.input.tooltip", "Energy Input for the Power Substation");
        provider.add("gtceu.machine.energy_hatch.output.tooltip", "Energy Output for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.output_hi_amp.tooltip",
                "Multiple Ampere Energy Output for Multiblocks");
        provider.add("gtceu.machine.substation_hatch.output.tooltip", "Energy Output for the Power Substation");

        // Rotor Holder
        provider.addMultiLang("gtceu.machine.rotor_holder.tooltip", "Rotor Holder for Multiblocks",
                "Holds Rotor in place so it will not fly away");

        // Data Hatches
        provider.addMultiLang("gtceu.machine.data_access_hatch.tooltip", "Data Access for Multiblocks",
                "Adds §a%s§7 slots for Data Items");
        provider.add("gtceu.machine.data_receiver_hatch.tooltip", "Research Data Input for Multiblocks");
        provider.add("gtceu.machine.data_transmitter_hatch.tooltip", "Research Data Output for Multiblocks");

        // Computation Hatches
        provider.add("gtceu.machine.computation_transmitter_hatch.tooltip",
                "Computation Data Output for Multiblocks");
        provider.add("gtceu.machine.computation_receiver_hatch.tooltip",
                "Computation Data Input for Multiblocks");

        // HPCA Parts
        provider.add("machine.gtceu.hpca_component.tooltip.upkeep_eut", "§eUpkeep Energy: §f%d EU/t");
        provider.add("machine.gtceu.hpca_component.tooltip.max_eut", "§6Max Energy: §f%d EU/t");
        provider.add("machine.gtceu.hpca_component.tooltip.cooler_passive", "§bCooler Type: §fPassive");
        provider.add("machine.gtceu.hpca_component.tooltip.cooler_active", "§bCooler Type: §fActive");
        provider.add("machine.gtceu.hpca_component.tooltip.cooler_cooling", "§aProvides: §f%d Cooling");
        provider.add("machine.gtceu.hpca_component.tooltip.cooler_active_coolant",
                "§cRequires up to: §f%d mB/t %s");
        provider.add("machine.gtceu.hpca_component.tooltip.computation_cwut", "§9Computation: §f%d CWU/t");
        provider.add("machine.gtceu.hpca_component.tooltip.computation_cooling", "§cRequires up to: §f%d Cooling");
        provider.add("machine.gtceu.hpca_component.tooltip.bridge",
                "Allows §fHPCA§7 to connect to §fNetwork Switches§7");
        provider.add("machine.gtceu.hpca_component.tooltip.damaged", "Can be damaged by HPCA overheating!");

        provider.add("gtceu.machine.hpca.empty_component.tooltip", "Just for filling space");
        provider.add("gtceu.machine.hpca.heat_sink_component.tooltip", "Free cooling! Is anything free?");
        provider.add("gtceu.machine.hpca.active_cooler_component.tooltip", "Less free, more effective cooling");
        provider.add("gtceu.machine.hpca.computation_component.tooltip", "Baby's first computation");
        provider.add("gtceu.machine.hpca.advanced_computation_component.tooltip", "Computation big leagues");
        provider.add("gtceu.machine.hpca.bridge_component.tooltip",
                "So that's where the \"Array\" in HPCA comes from");

        provider.add("gtceu.machine.hpca.computation_component.damaged.name",
                "Damaged HPCA Computation Component");
        provider.add("gtceu.machine.hpca.computation_component.damaged.tooltip", "Free recyclable materials");
        provider.add("gtceu.machine.hpca.advanced_computation_component.damaged.name",
                "Damaged HPCA Advanced Computation Component");
        provider.add("gtceu.machine.hpca.advanced_computation_component.damaged.tooltip",
                "It only cost an arm and a leg");

        // Passthrough Hatch
        provider.add("gtceu.machine.passthrough_hatch_item.tooltip", "Sends Items from one Side to the other");
        provider.add("gtceu.machine.passthrough_hatch_fluid.tooltip",
                "Sends Fluids from one Side to the other");

        // Laser Hatches
        provider.add("gtceu.machine.laser_hatch.source.tooltip", "§7Transmitting power at distance");
        provider.add("gtceu.machine.laser_hatch.target.tooltip", "§7Receiving power from distance");
        provider.add("gtceu.machine.laser_hatch.both.tooltip", "§cLaser Cables must be in a straight line!§7");
        provider.addMultiLang("gtceu.machine.laser_source_hatch.tooltip",
                "Transmitting power at distance",
                "§cLaser cables must be in a straight line!§7");

        provider.addMultiLang("gtceu.machine.laser_target_hatch.tooltip",
                "Receiving power from distance",
                "§cLaser cables must be in a straight line!§7");
    }

    private static void generateCentralMonitorGuiLang(GTLangProvider provider) {
        provider.add("gtceu.multiblock.central_monitor.low_power", "Low Power");
        provider.add("gtceu.multiblock.central_monitor.height", "Screen Height:");
        provider.add("gtceu.multiblock.central_monitor.width", "Screen Width: %d");
        provider.add("gtceu.multiblock.central_monitor.height_modify", "Modify Height: %d");
        provider.add("gtceu.machine.central_monitor.tooltip", "But can it run Doom?");

        provider.add("tooltip.gtceu.wireless_transmitter_bind",
                "Binding to a transmitter cover at %s %s %s facing %s in %s");
        provider.add("tooltip.gtceu.computer_monitor_config", "Storing computer monitor cover configuration data");
        provider.add("tooltip.gtceu.computer_monitor_data", "Storing data: %s");
        provider.add("gtceu.display_source.computer_monitor_cover", "Computer Monitor Cover");
        provider.add("gtceu.display_target.computer_monitor_cover", "Computer Monitor Cover");
        provider.addMultiLang("placeholder.gtceu.energy",
                "Returns the amount of energy stored.",
                "Usage:",
                "  {energy} -> the amount of energy stored");
        provider.addMultiLang("placeholder.gtceu.energyCapacity",
                "Returns the max amount of energy that can be stored",
                "Usage:",
                "{energyCapacity} -> the energy capacity");
        provider.addMultiLang("placeholder.gtceu.itemCount",
                "Returns the amount of items (can be filtered).",
                "Usage:",
                "  {itemCount} -> total item amount",
                "  {itemCount <item_id>} -> amount of items with ids equal to item_id",
                "  {itemCount filter <slot_id>} -> amount of items matching filter in specified slot of this cover");
        provider.addMultiLang("placeholder.gtceu.calc",
                "Returns the result of a math function or operation.",
                "Usage:",
                "  {calc <any_string>} -> any_string",
                "  {calc <round|floor|ceil|sqrt|~> <arg>} -> the result of the specified operation",
                "  {calc <first_arg> <+|-|*|/|//|>>|<<|%> <second_arg>} -> the result of the specified operation");
        provider.addMultiLang("placeholder.gtceu.if",
                "Returns one of the arguments depending on the condition. The condition is considered true if it is not an empty string and is not equal to 0.",
                "Usage:",
                "  {if <condition> <returned_if_true> [returned_if_false]}");
        provider.addMultiLang("placeholder.gtceu.obf",
                "Returns the text from the first argument, obfuscated.",
                "Usage:",
                "  {obf <text>} -> obfuscated text");
        provider.addMultiLang("placeholder.gtceu.underline",
                "Returns the text from the first argument, underlined",
                "Usage:",
                "  {underline <text>} -> underlined text");
        provider.addMultiLang("placeholder.gtceu.strike",
                "Returns the text from the first text, displaying it as if it was crossed out",
                "Usage:",
                "  {strike <text>} -> crossed-out text");
        provider.addMultiLang("placeholder.gtceu.color",
                "Returns the text from the second argument, colored with the color from the first argument. All default minecraft chat colors can be used.",
                "Usage:",
                "  {color <color> <text>} -> colored text");
        provider.addMultiLang("placeholder.gtceu.tick",
                "Returns the amount of ticks passed from when this cover was placed.",
                "Usage:",
                "  {tick} -> the amount of ticks");
        provider.addMultiLang("placeholder.gtceu.block", "Returns the block symbol (█).",
                "Usage:",
                "  {block} -> '█'");
        provider.addMultiLang("placeholder.gtceu.repeat",
                "Returns the text from the second arguments, repeated the amount of times specified in the first argument.",
                "Usage:",
                "  {repeat <amount> <text>} -> text repeated the specified amount of times");
        provider.addMultiLang("placeholder.gtceu.random",
                "Returns a random number in the specified interval (inclusive).",
                "Usage:",
                "  {random <min> <max>} -> a random number between min and max (inclusive)");
        provider.addMultiLang("placeholder.gtceu.select",
                "Returns the argument at the specified index (starting from 0)",
                "Usage:",
                "  {select <index> [arg1] [arg2] [arg3] ... -> argument at the specified index");
        provider.addMultiLang("placeholder.gtceu.redstone",
                "Returns the redstone signal strength or sets the redstone output strength",
                "Usage:",
                "  {redstone get <up|down|north|south|east|west>} -> redstone signal strength (0-15) at the specified side",
                "  {redstone get link <slot_index> <freq_slot_index>} -> redstone signal strength of a Create redstone link frequency specified by a linked controller in slot #slot_index. freq_slot_index is the index of the frequency inside the controller (from left to right, 0-6)",
                "  {redstone set <power>} -> empty string, sets the redstone output strength from this cover's side",
                "  {redstone set link <slot_index> <freq_slot_index> <power>} -> empty string, broadcasts the specified redstone power on the specified Create redstone link frequency");
        provider.addMultiLang("placeholder.gtceu.fluidCount",
                "Returns the amount of fluids (can be filtered).",
                "Usage:",
                "  {fluidCount [fluidId]} -> the amount of all fluids, or the fluid with fluidId if specified");
        provider.addMultiLang("placeholder.gtceu.displayTarget",
                "Returns the specified line that was transmitted to this cover using a display link.",
                "Usage:",
                "  {displayTarget <line_number>} -> the text on the specified line (line number is 1-100)");
        provider.addMultiLang("placeholder.gtceu.previousText",
                "Returns the text that was previously displayed by this cover at the specified line (before line-wrapping).",
                "Usage:",
                "  {previousText <line>} -> the text previously displayed on the specified line (index starts at 1)");
        provider.addMultiLang("placeholder.gtceu.ae2itemCount",
                "Same as itemCount, but counts items in the ME network of the block this cover is attached to.",
                "Note that counting by filter or all items may cause lag!",
                "Usage:",
                "  {itemCount} -> total item amount",
                "  {itemCount <item_id>} -> amount of items with ids equal to item_id",
                "  {itemCount filter <slot_id>} -> amount of items matching filter in specified slot of this cover");
        provider.addMultiLang("placeholder.gtceu.ae2fluidCount",
                "Same as fluidCount, but counts items in the ME network of the block this cover is attached to.",
                "Note that counting all fluids may cause lag!",
                "Usage:",
                "  {fluidCount [fluidId]} -> the amount of all fluids, or the fluid with fluidId if specified");
        provider.addMultiLang("placeholder.gtceu.progress",
                "Returns the progress of the currently running recipe of the block this cover is attached to.",
                "Note that progress is an integer between 0 and {maxProgress}",
                "Usage:",
                "  {progress} -> the progress of the currently running recipe");
        provider.addMultiLang("placeholder.gtceu.maxProgress",
                "Returns the maximum progress of the currently running recipe of the block this cover is attached to.",
                "Example: 'Progress: {calc {calc {progress} / {maxProgress}} * 100}%'",
                "Usage:",
                "  {maxProgress} -> the max progress of the currently running recipe");
        provider.addMultiLang("placeholder.gtceu.maintenance",
                "Returns a 1 if there are maintenance problems in the block the cover is attached to, 0 otherwise.",
                "Example: 'Maintenance status: {if {maintenance} FIXING\\ REQUIRED OK}'",
                "Usage:",
                "  {maintenance} -> whether there are maintenance problems");
        provider.addMultiLang("placeholder.gtceu.active",
                "Returns a 1 if the block the cover is attached to is currently running a recipe, 0 otherwise.",
                "Usage:",
                "  {active} -> whether there's a currently running recipe");
        provider.addMultiLang("placeholder.gtceu.voltage",
                "Returns the voltage in the wire/cable the cover is on.",
                "Usage:",
                "  {voltage} -> the voltage in the wire/cable");
        provider.addMultiLang("placeholder.gtceu.amperage",
                "Returns the amperage in the wire/cable the cover is on.",
                "Usage:",
                "  {amperage} -> the amperate in the wire/cable");
        provider.addMultiLang("placeholder.gtceu.ae2energy",
                "Returns the energy currently stored in the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2energy} -> the energy in the ME network (in AE units)");
        provider.addMultiLang("placeholder.gtceu.ae2maxPower",
                "Returns the energy capacity of the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2maxPower} -> the energy capacity of the ME network");
        provider.addMultiLang("placeholder.gtceu.ae2powerUsage",
                "Returns the energy consumption of the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2powerUsage} -> the energy consumption of the ME network");
        provider.addMultiLang("placeholder.gtceu.ae2spatial",
                "Returns information about spatial I/O in the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2spatial power} -> the amount of power required to initiate spatial I/O",
                "  {ae2spatial efficiency} -> the efficiency of the Spatial Containment Structure (SPS)",
                "  {ae2spatial size<X|Y|Z>} -> the size of the SPS along the specified axis (example: 'Size: {sizeX}x{sizeY}x{sizeZ}')");
        provider.addMultiLang("placeholder.gtceu.ae2crafting",
                "Returns information about auto-crafting in the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2crafting get amount} -> the amount of crafting CPUs in the ME network",
                "  {ae2crafting get <index> storage} -> the amount of crafting storage the specified CPU has",
                "  {ae2crafting get <index> threads} -> the amount of co-processors the specified CPU has",
                "  {ae2crafting get <index> name} -> the name of the specified crafting CPU",
                "  {ae2crafting get <index> selectionMode} -> the selection mode of the specified crafting CPU (used for manual, automatic or both requests)",
                "  {ae2crafting get <index> amount} -> the amount of the item that was requested, or 0 if the CPU is idle",
                "  {ae2crafting get <index> item} -> the display name of the item that was requested, or 0 if the CPU is idle",
                "  {ae2crafting get <index> progress} -> the crafting job progress, or 0 if the CPU is idle",
                "  {ae2crafting get <index> time} -> the amount of time elapsed from the start of the craft (in nanoseconds), or 0 if the CPU is idle");
        provider.addMultiLang("placeholder.gtceu.count",
                "Returns how many of the provided arguments are equal to the first (compared as strings, so \"0\" != \"0.0\")",
                "Usage:",
                "  {count <arg1> [arg2] [arg3] [arg4] ...} -> the amount of arguments that are equal to the first");
        provider.addMultiLang("placeholder.gtceu.data",
                "Stores or retrieves some data from a data item (data stick/orb/module) in one of the slots.",
                "If you leave the <index> argument empty, it will be replaced with the value p (p is an integer from 0 to (capacity - 1) that is stored in the data item nbt).",
                "If the slot argument is equal to 0, this placeholder will manipulate the data stick that is currently targeted by this text module inside a data hatch.",
                "Usage:",
                "  {data get <slot> <index>} -> the data stored in the item in the specified slot",
                "  {data set <slot> <index> <value>} -> sets the data stored in the item in the specified slot, returns an empty string",
                "  {data getp <slot>} -> p",
                "  {data setp <slot> <value>} -> sets p, returns an empty string",
                "  {data inc <slot>} -> increments p by 1, if p becomes more than or equal to capacity, sets p to 0",
                "  {data dec <slot>} -> decrements p by 1, if p becomes less than 0, sets p to (capacity - 1)");
        provider.addMultiLang("placeholder.gtceu.combine",
                "Combines all of it's arguments into a single string (by escaping all spaces between the arguments)",
                "Example: {combine abc def ghi jkl mno} -> \"abc\\ def\\ ghi\\ jkl\\ mno\"",
                "Usage:",
                "  {combine [arg1] [arg2] [arg3] ...} -> a string that will be treated as a single argument in further placeholders");
        provider.addMultiLang("placeholder.gtceu.nbt",
                "Returns the nbt data of the item in the specified slot",
                "Usage:",
                "  {nbt <slot> [key1] [key2] [key3] ...} -> item_nbt[key1][key2][key3][...]");
        provider.addMultiLang("placeholder.gtceu.toChars",
                "Returns the characters of the provided string with spaces between them",
                "Example: {toChars example} -> 'e x a m p l e'",
                "Usage:",
                "  {toChars <arg>} -> characters");
        provider.addMultiLang("placeholder.gtceu.toAscii",
                "Returns the ASCII code of the provided character",
                "Usage:",
                "  {toAscii <character>} -> ASCII code of the character");
        provider.addMultiLang("placeholder.gtceu.fromAscii",
                "Returns the character represented by the provided ASCII code",
                "Usage:",
                "  {fromAscii <char_code>} -> a character");
        provider.addMultiLang("gui.gtceu.cover.computer_monitor.placeholder_reference",
                "All placeholders:",
                "(hover for more info)");
        provider.addMultiLang("placeholder.gtceu.subList",
                "Returns arguments from with indexes from l (inclusive) to r (exclusive) (starting from 0)",
                "Usage:",
                "  {subList <left> <right> [arg0] [arg1] ...} -> all arguments with indexes from l to r separated by spaces");
        provider.addMultiLang("placeholder.gtceu.cmp",
                "Returns a 1 or 0 based on the expression in it's arguments",
                "Usage:",
                "  {cmp <a> <operator> <b>} -> 1 or 0, operator is one of >, <, >=, <=, ==, !=");
        provider.addMultiLang("placeholder.gtceu.bf",
                "Usage:",
                "  {bf <data_item_slot_index> <code>} -> empty string");
        provider.addMultiLang("placeholder.gtceu.cmd",
                "Executes Minecraft commands and returns their output.",
                "Requires a data item bound to a player, bind any data item to yourself by right-clicking with it.",
                "Usage:",
                "  {cmd <slot_index> <command>} -> command output");
        provider.addMultiLang("placeholder.gtceu.tm",
                "Returns the ™ symbol",
                "Usage:",
                "  {tm} -> the ™ symbol");
        provider.addMultiLang("placeholder.gtceu.formatInt",
                "Returns a string representation of the provided integer",
                "Example: {formatInt 1236457} -> 1.24M",
                "Usage:",
                "  {formatInt <arg>} -> string representation of the int");
        provider.addMultiLang("placeholder.gtceu.click",
                "Returns whether the targeted advanced monitor was clicked before the current tick",
                "Usage:",
                "  {click} -> \"1\" if the targeted advanced monitor was clicked, \"0\" otherwise",
                "  {click x} -> the x position of the last click (between 0 and 1)",
                "  {click y} -> the y position of the last click (between 0 and 1)");
        provider.addMultiLang("placeholder.gtceu.ender",
                "Interacts with ender link covers",
                "Can interact with private channels if provided with a data item bound to a player",
                "Usage:",
                "  {ender item <channel> [player_data_item_slot]} -> item count",
                "  {ender itemPull <channel> [player_data_item_slot]} -> pull 1 item from the ender link's buffer",
                "  {ender itemPush <channel> [player_data_item_slot]} -> push 1 item to the ender link's buffer",
                "  {ender itemId <channel> [player_data_item_slot]} -> the id of the item in the ender link's buffer (ex. \"26 minecraft:dirt\")",
                "  {ender fluid <channel> [player_data_item_slot]} -> fluid count",
                "  {ender redstone <channel> [player_data_item_slot] -> redstone signal level",
                "  {ender redstone <channel> <player_data_item_slot> <signal> -> sets the redstone signal outputed to the ender redstone link, returns empty string",
                "The player_data_item_slot argument may be left empty (not 0, empty string)");
        provider.addMultiLang("placeholder.gtceu.eval",
                "Returns the result of evaluating the provided string which may placeholders",
                "Usage:",
                "  {eval abcdefg} -> abcdefg",
                "  {eval \"repeating a: {repeat 5 \\\"a \\\"}\" -> repeating a: a a a a a ",
                "  {eval \\\"\"{some random text}\"\\\" -> {some random text}",
                "  {eval \"text \"\\\"\"{something with spaces}\"\\\"\" more text\" -> text {something with spaces} more text");
        provider.addMultiLang("placeholder.gtceu.module",
                "Renders the module in the specified slot onto the central monitor (does not work in a cover)",
                "Usage:",
                "  {module <slot> <x> <y>} -> empty string");
        provider.addMultiLang("placeholder.gtceu.setImage",
                "Sets the image URL in an image module in the specified slot",
                "Usage:",
                "  {setImage <slot> <url>} -> empty string");
        provider.addMultiLang("placeholder.gtceu.rect",
                "Draws a rectangle at the specified position with the specified coordinates and size",
                "Usage:",
                "  {rect <x> <y> <width> <height> <colorARGB>} -> empty string",
                "  {rect 0.5 0.25 2 1 0xFFFFFFFF} -> draws a white rectangle at (0.5, 0.25) with the size (2, 1)");
        provider.addMultiLang("placeholder.gtceu.quad",
                "Draws a quad (must specify parameters for all 4 vertices)",
                "Usage:",
                "  {quad <x1> <y1> <x2> <y2> <x3> <y3> <x4> <y4> <color1> <color2> <color3> <color4>} -> empty string");
        provider.addMultiLang("placeholder.gtceu.item",
                "Returns the amount and id of the item in a specified slot",
                "Usage:",
                "  {item <slot>} -> \"31 minecraft:diamond\" (for example)");
        provider.addMultiLang("placeholder.gtceu.bufferText",
                "Returns the text from a buffer accessible by ComputerCraft",
                "Usage:",
                "  {bufferText <line>} -> text from the buffer on the specified line (line is 1-100)");
        provider.addMultiLang("placeholder.gtceu.blockNbt",
                "Returns the NBT of the block entity",
                "Usage:",
                "  {blockNbt} -> full block entity nbt",
                "  {blockNbt [key1] [key2] ...} -> part of the nbt");
        provider.addMultiLang("placeholder.gtceu.targetSlot",
                "Returns the index of the targeted data hatch slot",
                "Usage:",
                "  {targetSlot} -> <slot> (from 1 to 4/9/16)");
        provider.addMultiLang("placeholder.gtceu.setTargetSlot",
                "Sets the index of the targeted data hatch slot.",
                "The change will take effect immediately after this placeholder executes.",
                "(Further placeholders will reference the new target)",
                "Usage:",
                "  {setTargetSlot <slot>} -> empty string");

        provider.add("gui.gtceu.cover.computer_monitor.update_interval", "Update interval (in ticks)");
        provider.add("gui.gtceu.cover.computer_monitor.edit_blank_placeholders", "Edit blank placeholders");
        provider.add("gui.gtceu.cover.computer_monitor.edit_displayed_text", "Edit displayed text");
        provider.add("gui.gtceu.central_monitor.text_scale", "Text scale");
        provider.add("gui.gtceu.central_monitor.group", "Group: %s");
        provider.add("gui.gtceu.central_monitor.group_default_name", "Group #%d");
        provider.add("gui.gtceu.central_monitor.none", "none");
        provider.add("gtceu.central_monitor.size", "Size: (%d+1+%d)x(%d+1+%d)");
        provider.add("gui.gtceu.cover.computer_monitor.error.invalid_number", "Invalid number '%s'!");
        provider.add("gui.gtceu.cover.computer_monitor.error.wrong_number_of_args", "Expected %d args, got %d!");
        provider.add("gui.gtceu.cover.computer_monitor.error.not_enough_args", "Expected at least %d args, got %d!");
        provider.add("gui.gtceu.cover.computer_monitor.error.no_cover", "No cover!");
        provider.add("gui.gtceu.cover.computer_monitor.error.exception", "Unexpected exception occurred: %s");
        provider.add("gui.gtceu.cover.computer_monitor.error.not_in_range",
                "Expected %s to be between %d and %d (inclusive), got %d");
        provider.add("gui.gtceu.cover.computer_monitor.error.invalid_args", "Invalid arguments!");
        provider.add("gui.gtceu.cover.computer_monitor.error.missing_item", "Missing %s in slot %d!");
        provider.add("gui.gtceu.cover.computer_monitor.error.bf_invalid_num",
                "Invalid number at index %d when processing symbol number %d");
        provider.add("gui.gtceu.cover.computer_monitor.error.bf_invalid", "Invalid character at %d");
        provider.add("gui.gtceu.cover.computer_monitor.error.no_target",
                "No target selected for the monitor group");
        provider.addMultiLang("gui.gtceu.cover.computer_monitor.main_textbox_tooltip",
                "Input string to display on line %d here.",
                "It can have placeholders, for example: 'Energy: {energy}/{energyCapacity} EU'",
                "Placeholders can also be inside other placeholders.");
        provider.addMultiLang("gui.gtceu.cover.computer_monitor.slot_tooltip",
                "A slot for items that some placeholders can reference",
                "Slot number: %d");
        provider.addMultiLang("gui.gtceu.cover.computer_monitor.second_page_textbox_tooltip",
                "Input placeholder to be used in place of %s '{}' here.",
                "For example, you can have a string 'Energy: {}/{} EU' and 'energy' and 'energyCapacity' in these text boxes.");
        provider.add("gui.gtceu.cover.computer_monitor.error.no_placeholder", "No such placeholder: '%s'!");
        provider.add("gui.gtceu.cover.computer_monitor.error.unclosed_bracket", "Unclosed bracket!");
        provider.add("gui.gtceu.cover.computer_monitor.error.unexpected_bracket", "Unexpected closing bracket!");
        provider.add("gui.gtceu.cover.computer_monitor.error.no_ae", "Cover holder does not have an AE2 network!");
        provider.add("gui.gtceu.cover.computer_monitor.error.not_supported",
                "This feature is not supported by this block/cover!");
        provider.add("gui.gtceu.central_monitor.create_group", "Create group");
        provider.add("gui.gtceu.central_monitor.remove_from_group", "Remove from group");
        provider.add("gui.gtceu.central_monitor.set_target", "Set target");
        provider.add("gui.gtceu.central_monitor.currently_editing", "Currently editing: %s");
        provider.addMultiLang("tooltip.gtceu.central_monitor.info",
                "In order to use monitors, you have to split them into groups first. A group may only have 1 module in it.",
                "Select them by left-clicking, then click 'Create group'.",
                "Then in the settings page for the group you can insert a module, you can configure it in the same page.",
                "To delete a group, select all of it's components and click 'Remove from group'.",
                "You can quickly select all components of a group by clicking on it's name. Click again to unselect.",
                "Some modules may display things depending on the block they target, to set a target for a group select any component of that group and right-click on the target component.",
                "You may wish to select a target that is not in the multiblock, you have to use the wireless transmitter cover for that.",
                "Place the cover on the target block, right-click it with a data stick and put that data stick into a data access hatch in the multiblock.",
                "Then select the data access hatch as the target, and set the slot index of your data stick in the number field that appeared.");
        provider.add("tooltip.gtceu.player_bind", "Bound to player: %s");
    }

    public static void generateMultiblockKeys(GTLangProvider provider) {
        // Recipe Logic
        provider.add("gtceu.multiblock.work_paused", "Work Paused.");
        provider.add("gtceu.multiblock.running", "Running perfectly.");
        provider.add("gtceu.multiblock.idling", "Idling.");

        // Researching
        provider.add("gtceu.multiblock.research_station.researching", "§6Researching.");

        // Energy Requirement
        provider.add("gtceu.multiblock.not_enough_energy", "WARNING: Machine needs more energy.");
        provider.add("gtceu.multiblock.not_enough_energy_output", "WARNING: Energy Dynamo Tier Too Low!");

        // Recipe Progress
        provider.add("gtceu.multiblock.waiting", "WARNING: Machine is waiting.");
        provider.add("gtceu.multiblock.progress_percent", "Progress: %s%%");
        provider.add("gtceu.multiblock.progress", "Progress: %ss / %ss (%s%%)");

        // Recipe Outputs
        provider.add("gtceu.multiblock.output_line.0", "%s x §e%s§r (%ss/ea)");
        provider.add("gtceu.multiblock.output_line.1", "%s x §e%s§r (%s/s)");
        provider.add("gtceu.multiblock.output_line.2", "%s ≈ §e%s§r (%ss/ea)");
        provider.add("gtceu.multiblock.output_line.3", "%s ≈ §e%s§r (%s/s)");

        // Structure Validation
        provider.add("gtceu.multiblock.invalid_structure", "Invalid structure.");
        provider.add("gtceu.multiblock.invalid_structure.tooltip",
                "This block is a controller of the multiblock structure. For building help, see structure template in JEI.");
        provider.add("gtceu.multiblock.validation_failed", "Invalid amount of inputs/outputs.");

        // Pattern Preview
        provider.add("gtceu.multiblock.title", "Multiblock Pattern");

        // Hatch Limitation - Mostly for GCYM Multiblocks that are Hardcoded into GTCEU
        provider.add("gtceu.multiblock.exact_hatch_1.tooltip", "§fAccepts Exactly §6One §fEnergy Hatch.");

        // Recipe Tier
        provider.add("gtceu.multiblock.max_recipe_tier", "Max Recipe Tier: %s");
        provider.add("gtceu.multiblock.max_recipe_tier_hover", "The maximum tier of recipes that can be run");

        // Energy Consumption/Generation
        provider.add("gtceu.multiblock.max_energy_per_tick", "Max EU/t: §a%s (%s§r)");
        provider.add("gtceu.multiblock.max_energy_per_tick_hover",
                "The maximum EU/t available for running recipes or overclocking");
        provider.add("gtceu.multiblock.max_energy_per_tick_amps", "Max EU/t: %s (%sA %s)");
        provider.add("gtceu.multiblock.energy_consumption", "Energy Usage: %s EU/t (%s)");
        provider.add("gtceu.multiblock.generation_eu", "Outputting: §a%s EU/t");

        // Maintenence
        provider.add("gtceu.multiblock.universal.no_problems", "No Maintenance Problems!");
        provider.add("gtceu.multiblock.universal.has_problems", "Has Maintenance Problems!");
        provider.add("gtceu.multiblock.universal.has_problems_header",
                "Fix the following issues in a Maintenance Hatch:");
        provider.add("gtceu.multiblock.universal.problem.wrench", "§7Pipe is loose. (§aWrench§7)");
        provider.add("gtceu.multiblock.universal.problem.screwdriver", "§7Screws are loose. (§aScrewdriver§7)");
        provider.add("gtceu.multiblock.universal.problem.soft_mallet", "§7Something is stuck. (§aSoft Mallet§7)");
        provider.add("gtceu.multiblock.universal.problem.hard_hammer", "§7Plating is dented. (§aHard Hammer§7)");
        provider.add("gtceu.multiblock.universal.problem.wire_cutter", "§7Wires burned out. (§aWire Cutter§7)");
        provider.add("gtceu.multiblock.universal.problem.crowbar", "§7That doesn't belong there. (§aCrowbar§7)");

        // Muffler
        provider.add("gtceu.multiblock.universal.muffler_obstructed", "Muffler Hatch is Obstructed!");
        provider.add("gtceu.multiblock.universal.muffler_obstructed.tooltip",
                "Muffler Hatch must have a block of airspace in front of it.");

        // Rotor
        provider.add("gtceu.multiblock.universal.rotor_obstructed", "Rotor is Obstructed!");

        // Distinctness
        provider.add("gtceu.multiblock.universal.distinct", "Distinct Buses:");
        provider.add("gtceu.multiblock.universal.distinct.info",
                "If enabled, each Item Input Bus will be treated as fully distinct from each other for recipe lookup. Useful for things like Programmed Circuits, Extruder Shapes, etc.");

        // Parallel
        provider.add("gtceu.multiblock.parallel", "Performing up to %d Recipes in Parallel");
        provider.add("gtceu.multiblock.total_runs", "Performing %d Recipes at once");
        provider.add("gtceu.multiblock.parallel.exact", "- %dx from Parallels");
        provider.add("gtceu.multiblock.batch_enabled", "- %dx from Batching");
        provider.add("gtceu.multiblock.subtick_parallels", "- %dx from Overclocking");

        // Batching
        provider.add("gtceu.machine.batching.enabled", "Batching Enabled");
        provider.add("gtceu.machine.batching.disabled", "Batching Disabled");

        // Active RecipeMap
        provider.add("gtceu.multiblock.multiple_recipemaps.header", "Machine Mode:");
        provider.add("gtceu.multiblock.multiple_recipemaps.tooltip",
                "Screwdriver the controller to change which machine mode to use.");
        provider.add("gtceu.multiblock.multiple_recipemaps_recipes.tooltip", "Machine Modes: §e%s§r");
        provider.add("gtceu.multiblock.multiple_recipemaps.switch_message",
                "The machine must be off to switch modes!");
        provider.add("gui.gtceu.active_machine_mode.title", "Active Machine Mode");
        provider.add("gui.gtceu.active_machine_mode", "Active Machine Mode: %s");
        provider.add("gui.gtceu.active_machine_mode.tab_tooltip", "Change active Machine Mode");
        provider.add("gui.gtceu.available_recipe_types_1.tooltip", "Available Recipe Types: %s");
        provider.add("gui.gtceu.available_recipe_types_2.tooltip", "Available Recipe Types: %s, %s");
        provider.add("gui.gtceu.available_recipe_types_3.tooltip", "Available Recipe Types: %s, %s, %s");
        provider.add("gui.gtceu.available_recipe_types_4.tooltip", "Available Recipe Types: %s, %s, %s, %s");

        // Computation
        provider.add("gtceu.multiblock.computation.max", "Max CWU/t: %s");
        provider.add("gtceu.multiblock.computation.usage", "Using: %s");
        provider.add("gtceu.multiblock.computation.non_bridging", "Non-bridging connection found");
        provider.add("gtceu.multiblock.computation.non_bridging.detailed",
                "A Reception Hatch is linked to a machine which cannot bridge");
        provider.add("gtceu.multiblock.computation.not_enough_computation", "Machine needs more computation!");

        // XEI Categories
        provider.add("gtceu.auto_decomp.rotor", "Turbine Rotor");
        provider.add("gtceu.auto_decomp.tool", "Non-electric tool");
    }
}
