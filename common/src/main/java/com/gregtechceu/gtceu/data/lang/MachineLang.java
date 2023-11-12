package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.*;

public class MachineLang {

    protected static void init(RegistrateLangProvider provider) {

        replace(provider, "block.gtceu.steam_large_turbine", "Large Steam Turbine");
        replace(provider, "block.gtceu.gas_large_turbine", "Large Gas Turbine");
        replace(provider, "block.gtceu.plasma_large_turbine", "Large Plasma Turbine");

        // Steam machines
        replace(provider, "block.gtceu.steam_solid_boiler.bronze", "Small Steam Solid Boiler");
        replace(provider, "block.gtceu.steam_liquid_boiler.bronze", "Small Steam Liquid Boiler");

        provider.add("gtceu.machine.steam_solid_boiler.bronze.tooltip", "§7An early way to get Steam Power");
        provider.add("gtceu.machine.steam_solid_boiler.steel.tooltip", "§7Faster than the Small Steam Solid Fuel Boiler");
        provider.add("gtceu.machine.steam_solar_boiler.bronze.tooltip", "§7Steam Power by Sun");
        provider.add("gtceu.machine.steam_solar_boiler.steel.tooltip", "§7Steam Power by Sun");
        provider.add("gtceu.machine.steam_liquid_boiler.bronze.tooltip", "§7A Boiler running off Liquids");
        provider.add("gtceu.machine.steam_liquid_boiler.steel.tooltip", "§7Faster than Small Steam Liquid Boiler");
        provider.add("gtceu.machine.steam_boiler.heat_amount", "Heat Capacity: %s %%");

        provider.add("gtceu.machine.steam_extractor.bronze.tooltip", "§7Extracting your first Rubber");
        provider.add("gtceu.machine.steam_extractor.steel.tooltip", "§7Extracting your first Rubber");
        provider.add("gtceu.machine.steam_macerator.bronze.tooltip", "§7Macerating your Ores");
        provider.add("gtceu.machine.steam_macerator.steel.tooltip", "§7Macerating your Ores");
        provider.add("gtceu.machine.steam_compressor.bronze.tooltip", "§7Compressing Items");
        provider.add("gtceu.machine.steam_compressor.steel.tooltip", "§7Compressing Items");
        provider.add("gtceu.machine.steam_forge_hammer.bronze.tooltip", "§7Forge Hammer");
        provider.add("gtceu.machine.steam_forge_hammer.steel.tooltip", "§7Forge Hammer");
        provider.add("gtceu.machine.steam_furnace.bronze.tooltip", "§7Smelting things with compressed Steam");
        provider.add("gtceu.machine.steam_furnace.steel.tooltip", "§7Smelting things with compressed Steam");
        provider.add("gtceu.machine.steam_alloy_smelter.bronze.tooltip", "§7Combination Smelter");
        provider.add("gtceu.machine.steam_alloy_smelter.steel.tooltip", "§7Combination Smelter");
        provider.add("gtceu.machine.steam_rock_crusher.bronze.tooltip", "§7Place Water and Lava horizontally adjacent");
        provider.add("gtceu.machine.steam_rock_crusher.steel.tooltip", "§7Place Water and Lava horizontally adjacent");
        provider.add("gtceu.machine.steam_miner.tooltip", "§7Mines ores below the Miner!");

        provider.add("gtceu.machine.combustion_generator.tooltip", "§7Requires flammable Liquids");
        provider.add("gtceu.machine.steam_turbine.tooltip", "§7Converts Steam into EU");
        provider.add("gtceu.machine.gas_turbine.tooltip", "§7Requires flammable Gases");


        provider.add("gtceu.machine.block_breaker.tooltip", "§7Mines block on front face and collects its drops");
        provider.add("gtceu.machine.block_breaker.speed_bonus", "§eSpeed Bonus: §f%d%%");

        ///////////////////////////////////////////////////
        //               Standard Machines               //
        // Ones done with more unique tooltips are first /
        ///////////////////////////////////////////////////

        provider.add("gtceu.machine.macerator.lv.tooltip", "§7Shredding your Ores");
        provider.add("gtceu.machine.macerator.mv.tooltip", "§7Shredding your Ores");
        provider.add("gtceu.machine.macerator.hv.tooltip", "§7Shredding your Ores with Byproducts");
        provider.add("gtceu.machine.macerator.ev.tooltip", "§7Shredding your Ores with Byproducts");
        provider.add("gtceu.machine.macerator.iv.tooltip", "§7Blend-O-Matic 9001");
        provider.add("gtceu.machine.macerator.luv.tooltip", "§7Blend-O-Matic 9002");
        provider.add("gtceu.machine.macerator.zpm.tooltip", "§7Blend-O-Matic 9003");
        provider.add("gtceu.machine.macerator.uv.tooltip", "§7Shape Eliminator");

        provider.add("gtceu.machine.centrifuge.lv.tooltip", "§7Separating Molecules");
        provider.add("gtceu.machine.centrifuge.mv.tooltip", "§7Separating Molecules");
        provider.add("gtceu.machine.centrifuge.hv.tooltip", "§7Separating Molecules");
        provider.add("gtceu.machine.centrifuge.ev.tooltip", "§7Molecular Separator");
        provider.add("gtceu.machine.centrifuge.iv.tooltip", "§7Molecular Cyclone");
        provider.add("gtceu.machine.centrifuge.luv.tooltip", "§7Molecular Cyclone");
        provider.add("gtceu.machine.centrifuge.zpm.tooltip", "§7Molecular Cyclone");
        provider.add("gtceu.machine.centrifuge.uv.tooltip", "§7Molecular Tornado");

        provider.add("gtceu.machine.laser_engraver.lv.tooltip", "§7Don't look directly at the Laser");
        provider.add("gtceu.machine.laser_engraver.mv.tooltip", "§7Don't look directly at the Laser");
        provider.add("gtceu.machine.laser_engraver.hv.tooltip", "§7Don't look directly at the Laser");
        provider.add("gtceu.machine.laser_engraver.ev.tooltip", "§7Don't look directly at the Laser");
        provider.add("gtceu.machine.laser_engraver.iv.tooltip", "§7With the Power of 2.04 MW");
        provider.add("gtceu.machine.laser_engraver.luv.tooltip", "§7With the Power of 8.16 MW");
        provider.add("gtceu.machine.laser_engraver.zpm.tooltip", "§7With the Power of 32.64 MW");
        provider.add("gtceu.machine.laser_engraver.uv.tooltip", "§7Exact Photon Cannon");

        provider.add("gtceu.machine.thermal_centrifuge.lv.tooltip", "§7Separating Ores more precisely");
        provider.add("gtceu.machine.thermal_centrifuge.mv.tooltip", "§7Separating Ores more precisely");
        provider.add("gtceu.machine.thermal_centrifuge.hv.tooltip", "§7Separating Ores more precisely");
        provider.add("gtceu.machine.thermal_centrifuge.ev.tooltip", "§7Separating Ores more precisely");
        provider.add("gtceu.machine.thermal_centrifuge.iv.tooltip", "§7Blaze Sweatshop T-6350");
        provider.add("gtceu.machine.thermal_centrifuge.luv.tooltip", "§7Blaze Sweatshop T-6351");
        provider.add("gtceu.machine.thermal_centrifuge.zpm.tooltip", "§7Blaze Sweatshop T-6352");
        provider.add("gtceu.machine.thermal_centrifuge.uv.tooltip", "§7Fire Cyclone");

        provider.add("gtceu.machine.electrolyzer.lv.tooltip", "§7Electrolyzing Molecules");
        provider.add("gtceu.machine.electrolyzer.mv.tooltip", "§7Electrolyzing Molecules");
        provider.add("gtceu.machine.electrolyzer.hv.tooltip", "§7Electrolyzing Molecules");
        provider.add("gtceu.machine.electrolyzer.ev.tooltip", "§7Electrolyzing Molecules");
        provider.add("gtceu.machine.electrolyzer.iv.tooltip", "§7Molecular Disintegrator E-4906");
        provider.add("gtceu.machine.electrolyzer.luv.tooltip", "§7Molecular Disintegrator E-4907");
        provider.add("gtceu.machine.electrolyzer.zpm.tooltip", "§7Molecular Disintegrator E-4908");
        provider.add("gtceu.machine.electrolyzer.uv.tooltip", "§7Atomic Ionizer");

        provider.add("gtceu.machine.lathe.lv.tooltip", "§7Produces Rods more efficiently");
        provider.add("gtceu.machine.lathe.mv.tooltip", "§7Produces Rods more efficiently");
        provider.add("gtceu.machine.lathe.hv.tooltip", "§7Produces Rods more efficiently");
        provider.add("gtceu.machine.lathe.ev.tooltip", "§7Produces Rods more efficiently");
        provider.add("gtceu.machine.lathe.iv.tooltip", "§7Turn-O-Matic L-5906");
        provider.add("gtceu.machine.lathe.luv.tooltip", "§7Turn-O-Matic L-5907");
        provider.add("gtceu.machine.lathe.zpm.tooltip", "§7Turn-O-Matic L-5908");
        provider.add("gtceu.machine.lathe.uv.tooltip", "§7Rotation Grinder");

        provider.add("gtceu.machine.ore_washer.lv.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("gtceu.machine.ore_washer.mv.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("gtceu.machine.ore_washer.hv.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("gtceu.machine.ore_washer.ev.tooltip", "§7Getting more Byproducts from your Ores");
        provider.add("gtceu.machine.ore_washer.iv.tooltip", "§7Repurposed Laundry-Washer I-360");
        provider.add("gtceu.machine.ore_washer.luv.tooltip", "§7Repurposed Laundry-Washer I-361");
        provider.add("gtceu.machine.ore_washer.zpm.tooltip", "§7Repurposed Laundry-Washer I-362");
        provider.add("gtceu.machine.ore_washer.uv.tooltip", "§7Miniature Car Wash");

        standardTooltips(provider, "gtceu.machine.electric_furnace",
                "Not like using a Commodore 64",
                "Electron Excitement Processor",
                "Atom Stimulator");

        standardTooltips(provider, "gtceu.machine.alloy_smelter",
                "HighTech combination Smelter",
                "Alloy Integrator",
                "Metal Amalgamator");

        standardTooltips(provider, "gtceu.machine.arc_furnace",
                "Who needs a Blast Furnace?",
                "Discharge Heater",
                "Short Circuit Heater");

        standardTooltips(provider, "gtceu.machine.assembler",
                "Avengers, Assemble!",
                "NOT a Crafting Table",
                "Assembly Constructor");

        standardTooltips(provider, "gtceu.machine.autoclave",
                "Crystallizing your Dusts",
                "Pressure Cooker",
                "Encumbrance Unit");

        standardTooltips(provider, "gtceu.machine.bender",
                "Boo, he's bad! We want BENDER!!!",
                "Shape Distorter",
                "Matter Deformer");

        standardTooltips(provider, "gtceu.machine.brewery",
                "Compact and efficient potion brewing",
                "Brewing your Drinks",
                "Brew Rusher");

        standardTooltips(provider, "gtceu.machine.canner",
                "Puts things into and out of Containers",
                "Can Operator",
                "Can Actuator");

        standardTooltips(provider, "gtceu.machine.chemical_bath",
                "Bathing Ores in Chemicals to separate them",
                "Chemical Soaker",
                "Chemical Dunktron");

        standardTooltips(provider, "gtceu.machine.chemical_reactor",
                "Letting Chemicals react with each other",
                "Chemical Performer",
                "Reaction Catalyzer");

        standardTooltips(provider, "gtceu.machine.compressor",
                "Compress-O-Matic C77",
                "Singularity Condenser",
                "Matter Constrictor");

        standardTooltips(provider, "gtceu.machine.cutter",
                "Slice'N Dice",
                "Matter Cleaver",
                "Object Divider");

        standardTooltips(provider, "gtceu.machine.distillery",
                "Extracting most relevant Parts of Fluids",
                "Condensation Separator",
                "Fraction Splitter");

        standardTooltips(provider, "gtceu.machine.electromagnetic_separator",
                "Separating the magnetic Ores from the rest",
                "EM Categorizer",
                "EMF Dispeller");

        standardTooltips(provider, "gtceu.machine.extractor",
                "Dejuicer-Device of Doom - D123",
                "Vacuum Extractinator",
                "Liquefying Sucker");

        standardTooltips(provider, "gtceu.machine.extruder",
                "Universal Machine for Metal Working",
                "Material Displacer",
                "Shape Driver");

        standardTooltips(provider, "gtceu.machine.fermenter",
                "Fermenting Fluids",
                "Fermentation Hastener",
                "Respiration Controller");

        standardTooltips(provider, "gtceu.machine.fluid_heater",
                "Heating up your Fluids",
                "Heat Infuser",
                "Thermal Imbuer");

        standardTooltips(provider, "gtceu.machine.fluid_solidifier",
                "Cools Fluids down to form Solids",
                "Not an Ice Machine",
                "Fluid Petrificator");

        standardTooltips(provider, "gtceu.machine.forge_hammer",
                "Stop, Hammertime!",
                "Plate Forger",
                "Impact Modulator");

        standardTooltips(provider, "gtceu.machine.forming_press",
                "Imprinting Images into things",
                "Object Layerer",
                "Surface Shifter");

        standardTooltips(provider, "gtceu.machine.mixer",
                "Will it Blend?",
                "Matter Organizer",
                "Material Homogenizer");

        standardTooltips(provider, "gtceu.machine.packer",
                "Puts things into and Grabs things out of Boxes",
                "Boxinator",
                "Amazon Warehouse");

        standardTooltips(provider, "gtceu.machine.polarizer",
                "Bipolarising your Magnets",
                "Magnetism Inducer",
                "Magnetic Field Rearranger");

        standardTooltips(provider, "gtceu.machine.sifter",
                "Stay calm and keep sifting",
                "Sponsored by TFC",
                "Pulsation Filter");

        standardTooltips(provider, "gtceu.machine.wiremill",
                "Produces Wires more efficiently",
                "Ingot Elongator",
                "Wire Transfigurator");

        standardTooltips(provider, "gtceu.machine.circuit_assembler",
                "Pick-n-Place all over the place",
                "Electronics Manufacturer",
                "Computation Factory");

        standardTooltips(provider, "gtceu.machine.mass_fabricator",
                "UUM Matter * Fabrication Squared",
                "Genesis Factory",
                "Existence Initiator");

        standardTooltips(provider, "gtceu.machine.replicator",
                "Producing the Purest of Elements",
                "Matter Paster",
                "Elemental Composer");

        standardTooltips(provider, "gtceu.machine.scanner",
                "Scans Materials and other things",
                "Anomaly Detector",
                "Electron Microscope");

        provider.add("gtceu.creative_tooltip.1", "§7You just need");
        provider.add("gtceu.creative_tooltip.2", " Creative Mode");
        provider.add("gtceu.creative_tooltip.3", "§7 to use this");
        provider.add("gtceu.machine.hull.tooltip", "§7You just need §5I§dm§4a§cg§ei§an§ba§3t§7i§1o§5n§7 to use this");
        provider.add("gtceu.battery_buffer.average_input", "Average input: %s EU/t");
        provider.add("gtceu.battery_buffer.average_output", "Average output: %s EU/t");
        provider.add("gtceu.machine.transformer.description", "Transforms Energy between voltage tiers");
        provider.add("gtceu.machine.transformer.tooltip_tool_usage", "Starts as §fTransform Down§7, use Screwdriver to change");
        provider.add("gtceu.machine.transformer.tooltip_transform_down", "§aTransform Down: §f%dA %d EU (%s§f) -> %dA %d EU (%s§f)");
        provider.add("gtceu.machine.transformer.message_transform_down", "Transforming Down, In: %d EU %dA, Out: %d EU %dA");
        provider.add("gtceu.machine.transformer.tooltip_transform_up", "§cTransform Up: §f%dA %d EU (%s§f) -> %dA %d EU (%s§f)");
        provider.add("gtceu.machine.transformer.message_transform_up", "Transforming Up, In: %d EU %dA, Out: %d EU %dA");
//        provider.add("gtceu.machine.transformer_adjustable.description", "Transforms Energy between voltage tiers, now with more Amps!");
//        provider.add("gtceu.machine.transformer_adjustable.tooltip_tool_usage", "Starts as §f4A§7, use Screwdriver to change");
//        provider.add("gtceu.machine.transformer_adjustable.message_adjust", "Adjusted Hi-Amp to %d EU %dA, Lo-Amp to %d EU %dA");

        provider.add("gtceu.machine.diode.message", "Max Amperage throughput: %s");
        provider.add("gtceu.machine.diode.tooltip_tool_usage", "Hit with a Soft Mallet to change Amperage flow.");
        provider.add("gtceu.machine.diode.tooltip_general", "Allows Energy Flow in one direction and limits Amperage");
        provider.add("gtceu.machine.diode.tooltip_starts_at", "Starts as §f1A§7, use Soft Mallet to change");
        provider.add("gtceu.machine.energy_converter.description", "Converts Energy between EU and FE");
        provider.add("gtceu.machine.energy_converter.tooltip_tool_usage", "Starts as §fFE Converter§7, use Soft Mallet to change");
        provider.add("gtceu.machine.energy_converter.tooltip_conversion_native", "§cNative Conversion: §f%d FE -> %dA %d EU (%s§f)");
        provider.add("gtceu.machine.energy_converter.message_conversion_native", "Converting Native Energy, In: %d FE, Out: %dA %d EU");
        provider.add("gtceu.machine.energy_converter.tooltip_conversion_eu", "§aEU Conversion: §f%dA %d EU (%s§f) -> %d Native");
        provider.add("gtceu.machine.energy_converter.message_conversion_eu", "Converting EU, In: %dA %d EU, Out: %d Native");
        provider.add("gtceu.machine.pump.tooltip", "The best way to empty Oceans!");
        provider.add("gtceu.machine.pump.tooltip_buckets", "§f%d §7ticks per Bucket");
        provider.add("gtceu.machine.item_collector.gui.collect_range", "Collect in %s blocks");
        provider.add("gtceu.machine.item_collector.tooltip", "Collects Items around itself when given a Redstone signal");
        provider.add("gtceu.machine.quantum_chest.tooltip", "Better than Storage Drawers");
        provider.add("gtceu.machine.quantum_chest.items_stored", "Item Amount:");
        provider.add("gtceu.machine.quantum_tank.tooltip", "Compact place to store all your fluids");
        provider.add("gtceu.machine.buffer.tooltip", "A Small Buffer to store Items and Fluids");

        provider.add("gtceu.machine.gas_collector.lv.tooltip", "Collects Gas from the air depending on the dimension");
        provider.add("gtceu.machine.gas_collector.mv.tooltip", "Collects Gas from the air depending on the dimension");
        provider.add("gtceu.machine.gas_collector.hv.tooltip", "Collects Gas from the air depending on the dimension");
        provider.add("gtceu.machine.gas_collector.ev.tooltip", "Collects Gas from the air depending on the dimension");
        provider.add("gtceu.machine.gas_collector.iv.tooltip", "Collects Gas from the atmosphere depending on the dimension");
        provider.add("gtceu.machine.gas_collector.luv.tooltip", "Collects Gas from the atmosphere depending on the dimension");
        provider.add("gtceu.machine.gas_collector.zpm.tooltip", "Collects Gas from the atmosphere depending on the dimension");
        provider.add("gtceu.machine.gas_collector.uv.tooltip", "Collects Gas from the solar system depending on the dimension");
        provider.add("gtceu.machine.gas_collector.uhv.tooltip", "Collects Gas from the solar system depending on the dimension");
        provider.add("gtceu.machine.gas_collector.uev.tooltip", "Collects Gas from the solar system depending on the dimension");
        provider.add("gtceu.machine.gas_collector.uiv.tooltip", "Collects Gas from the solar system depending on the dimension");
        provider.add("gtceu.machine.gas_collector.uxv.tooltip", "Collects Gas from the solar system depending on the dimension");
        provider.add("gtceu.machine.gas_collector.opv.tooltip", "Collects Gas from the universe depending on the dimension");
        provider.add("gtceu.machine.rock_breaker.lv.tooltip", "Place Water and Lava horizontally adjacent");
        provider.add("gtceu.machine.rock_breaker.mv.tooltip", "Place Water and Lava horizontally adjacent");
        provider.add("gtceu.machine.rock_breaker.hv.tooltip", "Place Water and Lava horizontally adjacent");
        provider.add("gtceu.machine.rock_breaker.ev.tooltip", "Place Water and Lava horizontally adjacent");
        provider.add("gtceu.machine.rock_breaker.iv.tooltip", "Cryogenic Magma Solidifier R-8200");
        provider.add("gtceu.machine.rock_breaker.luv.tooltip", "Cryogenic Magma Solidifier R-9200");
        provider.add("gtceu.machine.rock_breaker.zpm.tooltip", "Cryogenic Magma Solidifier R-10200");
        provider.add("gtceu.machine.rock_breaker.uv.tooltip", "Volcanic Formation Chamber");
        provider.add("gtceu.machine.rock_breaker.uhv.tooltip", "Volcanic Formation Chamber");
        provider.add("gtceu.machine.rock_breaker.uev.tooltip", "Volcanic Formation Chamber");
        provider.add("gtceu.machine.rock_breaker.uiv.tooltip", "Volcanic Formation Chamber");
        provider.add("gtceu.machine.rock_breaker.uxv.tooltip", "Volcanic Formation Chamber");
        provider.add("gtceu.machine.rock_breaker.opv.tooltip", "Volcanic Formation Chamber");
        provider.add("gtceu.machine.fisher.tooltip", "Costs string to fish. Consumes one string each time.");
        provider.add("gtceu.machine.fisher.speed", "Catches something every %d ticks");
        provider.add("gtceu.machine.fisher.requirement", "Requires a %dx%d centered square of water directly below.");
        provider.add("gtceu.machine.world_accelerator.description", "Tick accelerates nearby blocks in one of 2 modes: §fTile Entity§7 or §fRandom Tick§7. Use Screwdriver to change mode.");
        provider.add("gtceu.machine.world_accelerator.working_area", "§bWorking Area:");
        provider.add("gtceu.machine.world_accelerator.working_area_tile", "  Tile Entity Mode:§f Adjacent Blocks");
        provider.add("gtceu.machine.world_accelerator.working_area_random", "  Random Tick Mode:§f %dx%d");
        provider.add("gtceu.machine.world_accelerator.mode_tile", "Tile Entity Mode");
        provider.add("gtceu.machine.world_accelerator.mode_entity", "Random Tick Mode");
        provider.add("gtceu.machine.basic.input_from_output_side.allow", "Allow Input from Output Side: ");
        provider.add("gtceu.machine.basic.input_from_output_side.disallow", "Disallow Input from Output Side: ");
        provider.add("gtceu.machine.muffle.on", "Sound Muffling: Enabled");
        provider.add("gtceu.machine.muffle.off", "Sound Muffling: Disabled");
        provider.add("gtceu.machine.perfect_oc", "Does not lose energy efficiency when overclocked.");
        provider.add("gtceu.machine.parallel_limit", "Can run up to §b%d§r§7 Recipes at once.");

        provider.add("gtceu.machine.electric_blast_furnace.tooltip.1", "For every §f900K§7 above the recipe temperature, a multiplicative §f95%%§7 energy multiplier is applied pre-overclocking.");
        provider.add("gtceu.machine.electric_blast_furnace.tooltip.2", "For every §f1800K§7 above the recipe temperature, one overclock becomes §f100%% efficient§7 (perfect overclock).");
        provider.add("gtceu.machine.electric_blast_furnace.tooltip.3", "For every voltage tier above §bMV§7, temperature is increased by §f100K§7.");

        provider.add("gtceu.machine.pyrolyse_oven.tooltip.1", "§6Cupronickel §7coils are §f25%%§7 slower. Every coil after §bKanthal§7 increases speed by §f50%%§7.");

        provider.add("gtceu.machine.cracker.tooltip.1", "Every coil after §6Cupronickel§7 reduces energy usage by §f10%%§7.");

        provider.add("gtceu.machine.coke_oven_hatch.tooltip", "Allows automation access for the Coke Oven.");

        provider.add("gtceu.machine.canner.jei_description", "You can fill and empty any fluid containers with the Fluid Canner (e.g. Buckets or Fluid Cells)");

        provider.add("gtceu.machine.large_combustion_engine.tooltip.boost_regular", "Supply §f20 L/s§7 of Oxygen to produce up to §f%s EU/t§7 at §f2x§7 fuel consumption.");
        provider.add("gtceu.machine.large_combustion_engine.tooltip.boost_extreme", "Supply §f80 L/s§7 of Liquid Oxygen to produce up to §f%s EU/t§7 at §f2x§7 fuel consumption.");

        provider.add("gtceu.machine.fusion_reactor.capacity", "§7Maximum Energy Storage: §e%sM EU");
        provider.add("gtceu.machine.fusion_reactor.overclocking", "Overclocks double energy and halve duration.");

        provider.add("gtceu.machine.miner.tooltip", "Mines ores below the Miner! Starts as §f%sx%s §7area");
        provider.add("gtceu.machine.miner.per_block", "§7takes §f%ds §7per Block");

        provider.add("gtceu.machine.miner.multi.modes", "Has Silk Touch and Chunk Aligned Modes.");
        provider.add("gtceu.machine.miner.multi.production", "Produces §f3x§7 more crushed ore than a §fMacerator§7.");
        provider.add("gtceu.machine.miner.fluid_usage", "Uses §f%d L/t §7of §f%s§7, doubled per overclock.");
        provider.add("gtceu.machine.miner.multi.description", "A multiblock mining machine that covers a large area and produces huge quantity of ore.");
        provider.add("gtceu.machine.miner.startx", "sX: %d");
        provider.add("gtceu.machine.miner.starty", "sY: %d");
        provider.add("gtceu.machine.miner.startz", "sZ: %d");
        provider.add("gtceu.machine.miner.minex", "mX: %d");
        provider.add("gtceu.machine.miner.miney", "mY: %d");
        provider.add("gtceu.machine.miner.minez", "mZ: %d");
        provider.add("gtceu.machine.miner.radius", "Radius: %d");
        provider.add("gtceu.machine.miner.chunkradius", "Chunk Radius: %d");

        provider.add("gtceu.machine.fluid_drilling_rig.description", "Drills fluids from veins under bedrock.");
        provider.add("gtceu.machine.fluid_drilling_rig.production", "§eProduction Multiplier: §f%dx, %fx overclocked");
        provider.add("gtceu.machine.fluid_drilling_rig.depletion", "§bDepletion Rate: §f%s%%");

        provider.add("gtceu.machine.bedrock_ore_miner.description", "Drills ores from veins under bedrock.");
        provider.add("gtceu.machine.bedrock_ore_miner.production", "§eProduction Multiplier: §f%dx, %fx overclocked");
        provider.add("gtceu.machine.bedrock_ore_miner.depletion", "§bDepletion Rate: §f%s%%");

        multiLang(provider, "gtceu.machine.cleanroom.tooltip",
                "Place machines inside to run cleanroom recipes.",
                "Uses §f30 EU/t§7 when dirty, §f4 EU/t§7 when clean.",
                "Overclocking increases cleaning per cycle.",
                "§bSize: §f5x5x5 to 15x15x15",
                "Requires §fFilter Casings §7in the ceiling, excluding the edges.",
                "Accepts up to §f4 Doors§7! Remains clean when the door is open.",
                "Generators, Mufflers, Drills, and Primitive Machines are too dirty for the cleanroom!",
                "Send power through §fHulls §7or §fDiodes §7in the walls.");
        provider.add("gtceu.machine.cleanroom.tooltip.hold_ctrl", "Hold CTRL to show additional Structure Information");
        provider.add("gtceu.machine.cleanroom.tooltip.ae2.channels", "Send up to §f8 AE2 Channels §7through §fHulls§7 in the walls.");
        provider.add("gtceu.machine.cleanroom.tooltip.ae2.no_channels", "Send §aAE2 Networks§7 through §fHulls§7 in the walls.");
        provider.add("gtceu.multiblock.cleanroom.clean_state", "Status: §aCLEAN");
        provider.add("gtceu.multiblock.cleanroom.dirty_state", "Status: §4CONTAMINATED");
        provider.add("gtceu.multiblock.cleanroom.clean_amount", "Cleanliness: §a%s%%");

        multiLang(provider, "gtceu.machine.charcoal_pile.tooltip", "Turns Logs into §aCharcoal§7 when §cignited§7.", "Right Click with fire-starting items to start.", "Pyrolysis occurs in up to a §b9x4x9§7 space beneath.", "Logs must be not be exposed to §eAir§7!");
        multilineLang(provider, "gtceu.multiblock.charcoal_pile.description", "Converts logs into Brittle Charcoal in a 9x4x9 area beneath it.\n\nThe floor of the pit must be made from bricks, and any ground-related block can be used for the walls and roof. No air can be inside the pit.\n\nLarger pits take more time to process logs, but are more efficient.");

        provider.add("gtceu.multiblock.central_monitor.low_power", "Low Power");
        provider.add("gtceu.multiblock.central_monitor.height", "Screen Height:");
        provider.add("gtceu.multiblock.central_monitor.width", "Screen Width: %d");
        provider.add("gtceu.multiblock.central_monitor.height_modify", "Modify Height: %d");
        multiLang(provider, "gtceu.multiblock.central_monitor.tooltip", "This is a machine that monitors machines proxied by the Digital Interface Cover. You can easily monitor the Fluids, Items, Energy, and States of machines proxied in Energy Network.", "You can build the central monitor screen from 3X2 to %dX%d (width X height).", "The default height is 3. You can adjust the screen height in the GUI before the structure is formed.", "Energy consumption: %d EU/s for each screen.");
        multiLang(provider, "gtceu.multiblock.monitor_screen.tooltip", "The GUI can be opened with a right-click of a screwdriver.", "The proxy mode of Digital Interface Cover can delegate machines' capabilities and GUI. (Yes, you can connect pipes directly on the screen.)", "The screen also supports plugins.");
    }

    private static void standardTooltips(RegistrateLangProvider provider, String root, String lowTier, String midTier, String highTier) {
        provider.add("%s.%s.tooltip".formatted(root, "lv"), "§7%s".formatted(lowTier));
        provider.add("%s.%s.tooltip".formatted(root, "mv"), "§7%s".formatted(lowTier));
        provider.add("%s.%s.tooltip".formatted(root, "hv"), "§7%s".formatted(lowTier));
        provider.add("%s.%s.tooltip".formatted(root, "ev"), "§7%s".formatted(lowTier));
        provider.add("%s.%s.tooltip".formatted(root, "iv"), "§7%s".formatted(midTier));
        provider.add("%s.%s.tooltip".formatted(root, "luv"), "§7%s".formatted(midTier));
        provider.add("%s.%s.tooltip".formatted(root, "zpm"), "§7%s".formatted(midTier));
        provider.add("%s.%s.tooltip".formatted(root, "uv"), "§7%s".formatted(highTier));
    }
}
