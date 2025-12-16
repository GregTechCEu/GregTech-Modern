package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.multilineLang;
import static com.gregtechceu.gtceu.data.lang.LangHandler.replace;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class ItemLang {

    public static void init(RegistrateLangProvider provider) {
        initGeneratedNames(provider);
        initItemNames(provider);
        initItemTooltips(provider);
    }

    private static void initGeneratedNames(RegistrateLangProvider provider) {
        // RecipeTypes
        for (var recipeType : GTRegistries.RECIPE_TYPES) {
            provider.add(recipeType.registryName.toLanguageKey(), toEnglishName(recipeType.registryName.getPath()));
        }

        // Recipe Categories
        provider.add("gtceu.recipe.category.arc_furnace_recycling", "Arc Scrapping");
        provider.add("gtceu.recipe.category.macerator_recycling", "Part Grinding");
        provider.add("gtceu.recipe.category.extractor_recycling", "Scrap Remelting");
        provider.add("gtceu.recipe.category.ore_crushing", "Ore Grinding");
        provider.add("gtceu.recipe.category.ore_forging", "Ore Crushing");
        provider.add("gtceu.recipe.category.ore_bathing", "Ore Treating");
        provider.add("gtceu.recipe.category.chem_dyes", "Chemical Dyeing");
        provider.add("gtceu.recipe.category.ingot_molding", "Metal Molding");

        // TagPrefix
        for (TagPrefix tagPrefix : TagPrefix.values()) {
            provider.add(tagPrefix.getUnlocalizedName(), tagPrefix.langValue);
        }
        // GTToolType
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            provider.add(toolType.getUnlocalizedName(), toEnglishName(toolType.name));
        }

        provider.add("tagprefix.polymer.plate", "%s Sheet");
        provider.add("tagprefix.polymer.foil", "Thin %s Sheet");
        provider.add("tagprefix.polymer.nugget", "%s Chip");
        provider.add("tagprefix.polymer.dense_plate", "Dense %s Sheet");
        provider.add("tagprefix.polymer.double_plate", "Double %s Sheet");
        provider.add("tagprefix.polymer.tiny_dust", "Tiny Pile of %s Pulp");
        provider.add("tagprefix.polymer.small_dust", "Small Pile of %s Pulp");
        provider.add("tagprefix.polymer.dust", "%s Pulp");
        provider.add("tagprefix.polymer.ingot", "%s Ingot");
    }

    private static void initItemNames(RegistrateLangProvider provider) {
        replace(provider, "item.gtceu.tungsten_steel_fluid_cell", "%s Tungstensteel Cell");
    }

    private static void initItemTooltips(RegistrateLangProvider provider) {
        provider.add("item.gtceu.empty_mold.tooltip", "§7Raw Plate to make Molds and Extrude Shapes");
        provider.add("item.gtceu.nano_saber.tooltip", "§7Ryujin no ken wo kurae!");
        provider.add("item.gtceu.plate_casting_mold.tooltip", "§7Mold for making Plates");
        provider.add("item.gtceu.casing_casting_mold.tooltip", "§7Mold for making Item Casings");
        provider.add("item.gtceu.gear_casting_mold.tooltip", "§7Mold for making Gears");
        provider.add("item.gtceu.bottle_casting_mold.tooltip", "§7Mold for making Bottles");
        provider.add("item.gtceu.ingot_casting_mold.tooltip", "§7Mold for making Ingots");
        provider.add("item.gtceu.ball_casting_mold.tooltip", "§7Mold for making Balls");
        provider.add("item.gtceu.block_casting_mold.tooltip", "§7Mold for making Blocks");
        provider.add("item.gtceu.nugget_casting_mold.tooltip", "§7Mold for making Nuggets");
        provider.add("item.gtceu.cylinder_casting_mold.tooltip", "§7Mold for shaping Cylinders");
        provider.add("item.gtceu.anvil_casting_mold.tooltip", "§7Mold for shaping Anvils");
        provider.add("item.gtceu.name_casting_mold.tooltip",
                "§7Mold for naming Items in the Forming Press (rename Mold with Anvil)");
        provider.add("item.gtceu.gear_casting_mold.small.tooltip", "§7Mold for making small Gears");
        provider.add("item.gtceu.rotor_casting_mold.tooltip", "§7Mold for making Rotors");
        provider.add("item.gtceu.pipe.tiny_casting_mold.tooltip", "§7Mold for making tiny Pipes");
        provider.add("item.gtceu.pipe.small_casting_mold.tooltip", "§7Mold for making small Pipes");
        provider.add("item.gtceu.pipe.normal_casting_mold.tooltip", "§7Mold for making Pipes");
        provider.add("item.gtceu.pipe.large_casting_mold.tooltip", "§7Mold for making large Pipes");
        provider.add("item.gtceu.pipe.huge_casting_mold.tooltip", "§7Mold for making full Block Pipes");
        provider.add("item.gtceu.plate_extruder_mold.tooltip", "§7Extruder Shape for making Plates");
        provider.add("item.gtceu.rod_extruder_mold.tooltip", "§7Extruder Shape for making Rods");
        provider.add("item.gtceu.bolt_extruder_mold.tooltip", "§7Extruder Shape for making Bolts");
        provider.add("item.gtceu.ring_extruder_mold.tooltip", "§7Extruder Shape for making Rings");
        provider.add("item.gtceu.cell_extruder_mold.tooltip", "§7Extruder Shape for making Cells");
        provider.add("item.gtceu.ingot_extruder_mold.tooltip",
                "§7Extruder Shape for, wait, can't we just use a Furnace?");
        provider.add("item.gtceu.wire_extruder_mold.tooltip", "§7Extruder Shape for making Wires");
        provider.add("item.gtceu.casing_extruder_mold.tooltip", "§7Extruder Shape for making Item Casings");
        provider.add("item.gtceu.pipe.tiny_extruder_mold.tooltip", "§7Extruder Shape for making tiny Pipes");
        provider.add("item.gtceu.pipe.small_extruder_mold.tooltip", "§7Extruder Shape for making small Pipes");
        provider.add("item.gtceu.pipe.normal_extruder_mold.tooltip", "§7Extruder Shape for making Pipes");
        provider.add("item.gtceu.pipe.large_extruder_mold.tooltip", "§7Extruder Shape for making large Pipes");
        provider.add("item.gtceu.pipe.huge_extruder_mold.tooltip", "§7Extruder Shape for making full Block Pipes");
        provider.add("item.gtceu.block_extruder_mold.tooltip", "§7Extruder Shape for making Blocks");
        provider.add("item.gtceu.sword_extruder_mold.tooltip", "§7Extruder Shape for making Swords");
        provider.add("item.gtceu.pickaxe_extruder_mold.tooltip", "§7Extruder Shape for making Pickaxes");
        provider.add("item.gtceu.shovel_extruder_mold.tooltip", "§7Extruder Shape for making Shovels");
        provider.add("item.gtceu.axe_extruder_mold.tooltip", "§7Extruder Shape for making Axes");
        provider.add("item.gtceu.hoe_extruder_mold.tooltip", "§7Extruder Shape for making Hoes");
        provider.add("item.gtceu.hammer_extruder_mold.tooltip", "§7Extruder Shape for making Hammers");
        provider.add("item.gtceu.file_extruder_mold.tooltip", "§7Extruder Shape for making Files");
        provider.add("item.gtceu.saw_extruder_mold.tooltip", "§7Extruder Shape for making Saws");
        provider.add("item.gtceu.gear_extruder_mold.tooltip", "§7Extruder Shape for making Gears");
        provider.add("item.gtceu.bottle_extruder_mold.tooltip", "§7Extruder Shape for making Bottles");
        provider.add("item.gtceu.gear_small_extruder_mold.tooltip", "§7Extruder Shape for making Small Gears");
        provider.add("item.gtceu.foil_extruder_mold.tooltip", "§7Extruder Shape for making Foils from Non-Metals");
        provider.add("item.gtceu.rod_long_extruder_mold.tooltip", "§7Extruder Shape for making Long Rods");
        provider.add("item.gtceu.rotor_extruder_mold.tooltip", "§7Extruder Shape for making Rotors");
        provider.add("item.gtceu.empty_spray_can.tooltip", "§7Can be filled with sprays of various colors");
        provider.add("fluid_cell.empty", "Empty");
        provider.add("item.gtceu.tool.matchbox.tooltip", "§7This is not a Car");
        provider.add("item.gtceu.tool.lighter.platinum.tooltip", "§7A known Prank Master is engraved on it");
        provider.add("item.gtceu.lv_battery_hull.tooltip", "§7An empty LV Battery Hull");
        provider.add("item.gtceu.mv_battery_hull.tooltip", "§7An empty §bMV §7Battery Hull");
        provider.add("item.gtceu.hv_battery_hull.tooltip", "§7An empty §6HV §7Battery Hull");
        provider.add("item.gtceu.ev_battery_hull.tooltip", "§7An empty §5EV §7Battery Hull");
        provider.add("item.gtceu.iv_battery_hull.tooltip", "§7An empty §1IV §7Battery Hull");
        provider.add("item.gtceu.luv_battery_hull.tooltip", "§7An empty §dLuV §7Battery Hull");
        provider.add("item.gtceu.zpm_battery_hull.tooltip", "§7An empty §fZPM §7Battery Hull");
        provider.add("item.gtceu.uv_battery_hull.tooltip", "§7An empty §3UV §7Battery Hull");
        provider.add("item.gtceu.battery.charge_time", "§aHolds %s %s of Power (%s)");
        provider.add("item.gtceu.battery.charge_detailed", "%s/%s EU§7 - Tier %s §7(%s/%s %s remaining§7)");
        provider.add("item.gtceu.battery.charge_unit.second", "seconds");
        provider.add("item.gtceu.battery.charge_unit.minute", "minutes");
        provider.add("item.gtceu.battery.charge_unit.hour", "hours");
        provider.add("item.gtceu.ulv_tantalum_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lv_cadmium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lv_lithium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lv_sodium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.mv_cadmium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.mv_lithium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.mv_sodium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.hv_cadmium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.hv_lithium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.hv_sodium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.ev_vanadium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.iv_vanadium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.luv_vanadium_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.zpm_naquadria_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.uv_naquadria_battery.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy_crystal.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotron_crystal.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotronic_energy_orb.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotronic_energy_orb_cluster.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy_module.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy_cluster.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.max_battery.tooltip", "§7Fill this to win Minecraft");
        provider.add("item.gtceu.electric.pump.tooltip", "§7Transfers §fFluids§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.fluid.regulator.tooltip", "§7Limits §fFluids§7 to specific quantities as §fCover§7.");
        provider.add("item.gtceu.conveyor.module.tooltip", "§7Transfers §fItems§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.robot.arm.tooltip", "§7Limits §fItems§7 to specific quantities as §fCover§7.");
        provider.add("item.gtceu.data_stick.tooltip", "§7A Low Capacity Data Storage");
        provider.add("item.gtceu.data_orb.tooltip", "§7A High Capacity Data Storage");
        multilineLang(provider, "item.gtceu.programmed_circuit.tooltip",
                "Use to open configuration GUI\nShift-Right-Click on a machine\nwith a circuit slot to set it to\nthis circuit's value.");
        provider.add("item.gtceu.circuit.integrated.gui", "§7Programmed Circuit Configuration");
        // multilineLang(provider, "item.gtceu.circuit.integrated.jei_description", "JEI is only showing recipes for the
        // given configuration.\n\nYou can select a configuration in the Programmed Circuit configuration tab.");
        provider.add("item.glass_lens", "Glass Lens (White)"); // todo move to material overrides
        provider.add("item.gtceu.silicon_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.phosphorus_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.naquadah_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.neutronium_boule.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.silicon_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.phosphorus_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.naquadah_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.neutronium_wafer.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.resin_circuit_board.tooltip", "§7A Coated Board");
        provider.add("item.gtceu.phenolic_circuit_board.tooltip", "§7A Good Board");
        provider.add("item.gtceu.plastic_circuit_board.tooltip", "§7A Good Board");
        provider.add("item.gtceu.epoxy_circuit_board.tooltip", "§7An Advanced Board");
        provider.add("item.gtceu.fiber_reinforced_circuit_board.tooltip", "§7An Extreme Board");
        provider.add("item.gtceu.multilayer_fiber_reinforced_circuit_board.tooltip", "§7An Elite Board");
        provider.add("item.gtceu.wetware_circuit_board.tooltip", "§7The Board that keeps life");
        provider.add("item.gtceu.resin_printed_circuit_board.tooltip", "§7A Basic Circuit Board");
        provider.add("item.gtceu.phenolic_printed_circuit_board.tooltip", "§7A Good Circuit Board");
        provider.add("item.gtceu.plastic_printed_circuit_board.tooltip", "§7A Good Circuit Board");
        provider.add("item.gtceu.epoxy_printed_circuit_board.tooltip", "§7An Advanced Circuit Board");
        provider.add("item.gtceu.fiber_reinforced_printed_circuit_board.tooltip", "§7A More Advanced Circuit Board");
        provider.add("item.gtceu.multilayer_fiber_reinforced_printed_circuit_board.tooltip",
                "§7An Elite Circuit Board");
        provider.add("item.gtceu.wetware_printed_circuit_board.tooltip", "§7The Board that keeps life");
        multilineLang(provider, "item.gtceu.vacuum_tube.tooltip", "§7Technically a Diode\n§cULV-Tier");
        provider.add("item.gtceu.diode.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.resistor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.transistor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.capacitor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.inductor.tooltip", "§7A Small Coil");
        provider.add("item.gtceu.smd_diode.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_capacitor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_transistor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_resistor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.smd_inductor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.advanced_smd_diode.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_capacitor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_transistor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_resistor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.advanced_smd_inductor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.highly_advanced_soc_wafer.tooltip", "§7Raw Highly Advanced Circuit");
        provider.add("item.gtceu.advanced_soc_wafer.tooltip", "§7Raw Advanced Circuit");
        provider.add("item.gtceu.ilc_wafer.tooltip", "§7Raw Integrated Circuit");
        provider.add("item.gtceu.cpu_wafer.tooltip", "§7Raw Processing Unit");
        provider.add("item.gtceu.hpic_wafer.tooltip", "§7Raw High Power Circuit");
        provider.add("item.gtceu.uhpic_wafer.tooltip", "§7Raw Ultra High Power Circuit");
        provider.add("item.gtceu.nand_memory_wafer.tooltip", "§7Raw Logic Gate");
        provider.add("item.gtceu.ulpic_wafer.tooltip", "§7Raw Ultra Low Power Circuit");
        provider.add("item.gtceu.lpic_wafer.tooltip", "§7Raw Low Power Circuit");
        provider.add("item.gtceu.mpic_wafer.tooltip", "§7Raw Power Circuit");
        provider.add("item.gtceu.nano_cpu_wafer.tooltip", "§7Raw Nano Circuit");
        provider.add("item.gtceu.nor_memory_wafer.tooltip", "§7Raw Logic Gate");
        provider.add("item.gtceu.qbit_cpu_wafer.tooltip", "§7Raw Qubit Circuit");
        provider.add("item.gtceu.ram_wafer.tooltip", "§7Raw Memory");
        provider.add("item.gtceu.soc_wafer.tooltip", "§7Raw Basic Circuit");
        provider.add("item.gtceu.simple_soc_wafer.tooltip", "§7Raw Simple Circuit");
        provider.add("item.gtceu.engraved_crystal_chip.tooltip", "§7Needed for Circuits");
        provider.add("item.gtceu.raw_crystal_chip.tooltip", "§7Raw Crystal Processor");
        provider.add("item.gtceu.raw_crystal_chip_parts.tooltip", "§7Raw Crystal Processor Parts");
        provider.add("item.gtceu.crystal_cpu.tooltip", "§7Crystal Processing Unit");
        provider.add("item.gtceu.crystal_soc.tooltip", "§7Crystal System on Chip");
        provider.add("item.gtceu.advanced_soc.tooltip", "§7Advanced System on Chip");
        provider.add("item.gtceu.highly_advanced_soc.tooltip", "§7Highly Advanced System on Chip");
        provider.add("item.gtceu.ilc_chip.tooltip", "§7Integrated Logic Circuit");
        provider.add("item.gtceu.cpu_chip.tooltip", "§7Central Processing Unit");
        provider.add("item.gtceu.hpic_chip.tooltip", "§7High Power IC");
        provider.add("item.gtceu.uhpic_chip.tooltip", "§7Ultra High Power IC");
        provider.add("item.gtceu.nand_memory_chip.tooltip", "§7NAND Logic Gate");
        provider.add("item.gtceu.nano_cpu_chip.tooltip", "§7Nano Central Processing Unit");
        provider.add("item.gtceu.nor_memory_chip.tooltip", "§7NOR Logic Gate");
        provider.add("item.gtceu.ulpic_chip.tooltip", "§7Ultra Low Power IC");
        provider.add("item.gtceu.lpic_chip.tooltip", "§7Low Power IC");
        provider.add("item.gtceu.mpic_chip.tooltip", "§7Power IC");
        provider.add("item.gtceu.qbit_cpu_chip.tooltip", "§7Qubit Central Processing Unit");
        provider.add("item.gtceu.ram_chip.tooltip", "§7Random Access Memory");
        provider.add("item.gtceu.soc.tooltip", "§7System on Chip");
        provider.add("item.gtceu.simple_soc.tooltip", "§7Simple System on Chip");
        multilineLang(provider, "item.gtceu.basic_electronic_circuit.tooltip",
                "§7Your First Circuit\n§cLV-Tier Circuit");
        multilineLang(provider, "item.gtceu.good_electronic_circuit.tooltip",
                "§7Your Second Circuit\n§cMV-Tier Circuit");
        multilineLang(provider, "item.gtceu.basic_integrated_circuit.tooltip",
                "§7Smaller and more powerful\n§6LV-Tier Circuit");
        multilineLang(provider, "item.gtceu.good_integrated_circuit.tooltip",
                "§7Smaller and more powerful\n§6MV-Tier Circuit");
        multilineLang(provider, "item.gtceu.advanced_integrated_circuit.tooltip",
                "§7Smaller and more powerful\n§6HV-Tier Circuit");
        multilineLang(provider, "item.gtceu.nand_chip.tooltip", "§7A Superior Simple Circuit\n§6ULV-Tier Circuit");
        multilineLang(provider, "item.gtceu.microchip_processor.tooltip",
                "§7A Superior Basic Circuit\n§eLV-Tier Circuit");
        multilineLang(provider, "item.gtceu.micro_processor.tooltip",
                "§7Amazing Computation Speed!\n§eMV-Tier Circuit");
        multilineLang(provider, "item.gtceu.micro_processor_assembly.tooltip",
                "§7Amazing Computation Speed!\n§eHV-Tier Circuit");
        multilineLang(provider, "item.gtceu.micro_processor_computer.tooltip",
                "§7Amazing Computation Speed!\n§eEV-Tier Circuit");
        multilineLang(provider, "item.gtceu.micro_processor_mainframe.tooltip",
                "§7Amazing Computation Speed!\n§eIV-Tier Circuit");
        multilineLang(provider, "item.gtceu.nano_processor.tooltip", "§7Smaller than ever\n§bHV-Tier Circuit");
        multilineLang(provider, "item.gtceu.nano_processor_assembly.tooltip", "§7Smaller than ever\n§bEV-Tier Circuit");
        multilineLang(provider, "item.gtceu.nano_processor_computer.tooltip", "§7Smaller than ever\n§bIV-Tier Circuit");
        multilineLang(provider, "item.gtceu.nano_processor_mainframe.tooltip",
                "§7Smaller than ever\n§bLuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.quantum_processor.tooltip",
                "§7Quantum Computing comes to life!\n§aEV-Tier Circuit");
        multilineLang(provider, "item.gtceu.quantum_processor_assembly.tooltip",
                "§7Quantum Computing comes to life!\n§aIV-Tier Circuit");
        multilineLang(provider, "item.gtceu.quantum_processor_computer.tooltip",
                "§7Quantum Computing comes to life!\n§aLuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.quantum_processor_mainframe.tooltip",
                "§7Quantum Computing comes to life!\n§aZPM-Tier Circuit");
        multilineLang(provider, "item.gtceu.crystal_processor.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9IV-Tier Circuit");
        multilineLang(provider, "item.gtceu.crystal_processor_assembly.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9LuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.crystal_processor_computer.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9ZPM-Tier Circuit");
        multilineLang(provider, "item.gtceu.crystal_processor_mainframe.tooltip",
                "§7Taking Advantage of Crystal Engraving\n§9UV-Tier Circuit");
        multilineLang(provider, "item.gtceu.wetware_processor.tooltip",
                "§7You have a feeling like it's watching you\n§4LuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.wetware_processor_assembly.tooltip",
                "§7Can run Minecraft\n§4ZPM-Tier Circuit");
        multilineLang(provider, "item.gtceu.wetware_processor_computer.tooltip",
                "§7Ultimate fusion of Flesh and Machine\n§4UV-Tier Circuit");
        multilineLang(provider, "item.gtceu.wetware_processor_mainframe.tooltip",
                "§7The best Man has ever seen\n§4UHV-Tier Circuit");
        provider.add("item.gtceu.stem_cells.tooltip", "§7Raw Intelligence");
        provider.add("item.gtceu.neuro_processing_unit.tooltip", "§7Neuro CPU");
        provider.add("item.gtceu.petri_dish.tooltip", "§7For cultivating Cells");
        provider.add("item.gtceu.neutron_reflector.tooltip", "§7Indestructible");
        provider.add("item.gtceu.duct_tape.tooltip", "§7If you can't fix it with this, use more of it!");
        provider.add("item.gtceu.quantum_eye.tooltip", "§7Improved Ender Eye");
        provider.add("item.gtceu.quantum_star.tooltip", "§7Improved Nether Star");
        provider.add("item.gtceu.gravi_star.tooltip", "§7Ultimate Nether Star");
        multilineLang(provider, "item.gtceu.item_filter.tooltip",
                "§7Filters §fItem§7 I/O as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "item.gtceu.item_tag_filter.tooltip",
                "§7Filters §fItem§7 I/O with §fItem Tags§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "item.gtceu.tag_filter.tooltip",
                "§7Filters §fItem§7 I/O with §fTag§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "item.gtceu.fluid_filter.tooltip",
                "§7Filters §fFluid§7 I/O as §fCover§7.\nCan be used as an §fElectric Pump§7 and §fFluid Regulator§7 upgrade.");
        multilineLang(provider, "item.gtceu.fluid_tag_filter.tooltip",
                "§7Filters §fFluid§7 I/O with §fFluid Tags§7 as §fCover§7.\nCan be used as an §fElectric Pump§7 and §fFluid Regulator§7 upgrade.");
        multilineLang(provider, "item.gtceu.item_smart_filter.tooltip",
                "§7Filters §fItem§7 I/O with §fMachine Recipes§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.add("item.gtceu.machine_controller.tooltip", "§7Turns Machines §fON/OFF§7 as §fCover§7.");
        provider.add("item.gtceu.activity_detector_cover.tooltip",
                "§7Gives out §fActivity Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_activity_detector_cover.tooltip",
                "§7Gives out §fMachine Progress§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.fluid_detector_cover.tooltip",
                "§7Gives out §fFluid Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_fluid_detector_cover.tooltip",
                "§7Gives §fRS-Latch§7 controlled §fFluid Storage Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.item_detector_cover.tooltip", "§7Gives out §fItem Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_item_detector_cover.tooltip",
                "§7Gives §fRS-Latch§7 controlled §fItem Storage Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.energy_detector_cover.tooltip",
                "§7Gives out §fEnergy Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.advanced_energy_detector_cover.tooltip",
                "§7Gives §fRS-Latch§7 controlled §fEnergy Status§7 as Redstone as §fCover§7.");
        multilineLang(provider, "item.gtceu.fluid_voiding_cover.tooltip",
                "§7Voids §fFluids§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.advanced_fluid_voiding_cover.tooltip",
                "§7Voids §fFluids§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.item_voiding_cover.tooltip",
                "§7Voids §fItems§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.advanced_item_voiding_cover.tooltip",
                "§7Voids §fItems§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.facade_cover.tooltip",
                "§7Decorative Outfit §fCover§7.\n§7Crafted using an Iron Plate and any block");
        provider.add("item.gtceu.computer_monitor_cover.tooltip", "§7Displays §fData§7 as §fCover§7.");
        provider.add("item.gtceu.shutter_module_cover.tooltip",
                "§fBlocks Transfer§7 through attached Side as §fCover§7.");
        multilineLang(provider, "item.gtceu.solar_panel.tooltip",
                "§7May the Sun be with you.\nProduces §fEnergy§7 from the §eSun§7 as §fCover§7.");
        provider.add("item.gtceu.infinite_water_cover.tooltip",
                "§7Fills attached containers with §9Water§7 as §fCover§7.");
        provider.add("item.gtceu.ender_fluid_link_cover.tooltip",
                "§7Transports §fFluids§7 with a §fWireless §dEnder§f Connection§7 as §fCover§7.");
        provider.add("item.gtceu.gelled_toluene.tooltip", "§7Raw Explosive");
        provider.add("item.gtceu.bottle.purple.drink.tooltip",
                "§7How about Lemonade. Or some Ice Tea? I got Purple Drink!");
        multilineLang(provider, "item.gtceu.foam_sprayer.tooltip",
                "§7Sprays Construction Foam\nUse on a frame to foam connected frames\nFoam can be colored");
        provider.add("item.gtceu.firebrick.tooltip", "§7Heat resistant");
        provider.add("item.gtceu.basic_tape.tooltip",
                "§7Not strong enough for mechanical issues\nCan be used to pick up crates without dropping their items");
        provider.add("item.gtceu.terminal.tooltip",
                "Shift + R-Click on a controller to automatically build the multi-block");

        provider.add("item.gtceu.sus_record.desc", "§7sussy!");
    }
}
