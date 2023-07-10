package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangHandler.multilineLang;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

public class ItemLang {

    public static void init(RegistrateLangProvider provider) {
        initGeneratedNames(provider);
        initItemNames(provider);
        initItemTooltips(provider);
    }

    private static void initGeneratedNames(RegistrateLangProvider provider) {
        // Materials
        for (Material material : GTRegistries.MATERIALS) {
            provider.add(material.getUnlocalizedName(), toEnglishName(material.getName()));
        }
        // RecipeTypes
        for (var recipeType : GTRegistries.RECIPE_TYPES) {
            provider.add(recipeType.registryName.toLanguageKey(), toEnglishName(recipeType.registryName.getPath()));
        }
        // TagPrefix
        for (TagPrefix tagPrefix : TagPrefix.values()) {
            provider.add(tagPrefix.getUnlocalizedName(), tagPrefix.langValue);
        }
        // GTToolType
        for (GTToolType toolType : GTToolType.values()) {
            provider.add(toolType.getUnlocalizedName(), toEnglishName(toolType));
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

    }

    private static void initItemTooltips(RegistrateLangProvider provider) {
        provider.add("item.gtceu.credit.copper.tooltip", "§70.125 Credits");
        provider.add("item.gtceu.credit.cupronickel.tooltip", "§71 Credit");
        provider.add("item.gtceu.credit.silver.tooltip", "§78 Credits");
        provider.add("item.gtceu.credit.gold.tooltip", "§764 Credits");
        provider.add("item.gtceu.credit.platinum.tooltip", "§7512 Credits");
        provider.add("item.gtceu.credit.osmium.tooltip", "§74096 Credits");
        provider.add("item.gtceu.credit.naquadah.tooltip", "§732768 Credits");
        provider.add("item.gtceu.credit.neutronium.tooltip", "§7262144 Credits");
        provider.add("item.gtceu.coin.gold.ancient.tooltip", "§7Found in ancient Ruins");
        provider.add("item.gtceu.coin.doge.tooltip", "§7wow much coin how monyey so cwypto pwz minye v wich vewy cuwwency wow");
        provider.add("item.gtceu.coin.chocolate.tooltip", "§7Wrapped in Gold");
        provider.add("item.gtceu.shape.empty.tooltip", "§7Raw Plate to make Molds and Extrude Shapes");
        provider.add("item.gtceu.nano_saber.tooltip", "§7Ryujin no ken wo kurae!");
        provider.add("item.gtceu.shape.mold.plate.tooltip", "§7Mold for making Plates");
        provider.add("item.gtceu.shape.mold.casing.tooltip", "§7Mold for making Item Casings");
        provider.add("item.gtceu.shape.mold.gear.tooltip", "§7Mold for making Gears");
        provider.add("item.gtceu.shape.mold.credit.tooltip", "§7Secure Mold for making Coins (Don't lose it!)");
        provider.add("item.gtceu.shape.mold.bottle.tooltip", "§7Mold for making Bottles");
        provider.add("item.gtceu.shape.mold.ingot.tooltip", "§7Mold for making Ingots");
        provider.add("item.gtceu.shape.mold.ball.tooltip", "§7Mold for making Balls");
        provider.add("item.gtceu.shape.mold.block.tooltip", "§7Mold for making Blocks");
        provider.add("item.gtceu.shape.mold.nugget.tooltip", "§7Mold for making Nuggets");
        provider.add("item.gtceu.shape.mold.cylinder.tooltip", "§7Mold for shaping Cylinders");
        provider.add("item.gtceu.shape.mold.anvil.tooltip", "§7Mold for shaping Anvils");
        provider.add("item.gtceu.shape.mold.name.tooltip", "§7Mold for naming Items in the Forming Press (rename Mold with Anvil)");
        provider.add("item.gtceu.shape.mold.gear.small.tooltip", "§7Mold for making small Gears");
        provider.add("item.gtceu.shape.mold.rotor.tooltip", "§7Mold for making Rotors");
        provider.add("item.gtceu.shape.extruder.plate.tooltip", "§7Extruder Shape for making Plates");
        provider.add("item.gtceu.shape.extruder.rod.tooltip", "§7Extruder Shape for making Rods");
        provider.add("item.gtceu.shape.extruder.bolt.tooltip", "§7Extruder Shape for making Bolts");
        provider.add("item.gtceu.shape.extruder.ring.tooltip", "§7Extruder Shape for making Rings");
        provider.add("item.gtceu.shape.extruder.cell.tooltip", "§7Extruder Shape for making Cells");
        provider.add("item.gtceu.shape.extruder.ingot.tooltip", "§7Extruder Shape for, wait, can't we just use a Furnace?");
        provider.add("item.gtceu.shape.extruder.wire.tooltip", "§7Extruder Shape for making Wires");
        provider.add("item.gtceu.shape.extruder.casing.tooltip", "§7Extruder Shape for making Item Casings");
        provider.add("item.gtceu.shape.extruder.pipe.tiny.tooltip", "§7Extruder Shape for making tiny Pipes");
        provider.add("item.gtceu.shape.extruder.pipe.small.tooltip", "§7Extruder Shape for making small Pipes");
        provider.add("item.gtceu.shape.extruder.pipe.normal.tooltip", "§7Extruder Shape for making Pipes");
        provider.add("item.gtceu.shape.extruder.pipe.large.tooltip", "§7Extruder Shape for making large Pipes");
        provider.add("item.gtceu.shape.extruder.pipe.huge.tooltip", "§7Extruder Shape for making full Block Pipes");
        provider.add("item.gtceu.shape.extruder.block.tooltip", "§7Extruder Shape for making Blocks");
        provider.add("item.gtceu.shape.extruder.sword.tooltip", "§7Extruder Shape for making Swords");
        provider.add("item.gtceu.shape.extruder.pickaxe.tooltip", "§7Extruder Shape for making Pickaxes");
        provider.add("item.gtceu.shape.extruder.shovel.tooltip", "§7Extruder Shape for making Shovels");
        provider.add("item.gtceu.shape.extruder.axe.tooltip", "§7Extruder Shape for making Axes");
        provider.add("item.gtceu.shape.extruder.hoe.tooltip", "§7Extruder Shape for making Hoes");
        provider.add("item.gtceu.shape.extruder.hammer.tooltip", "§7Extruder Shape for making Hammers");
        provider.add("item.gtceu.shape.extruder.file.tooltip", "§7Extruder Shape for making Files");
        provider.add("item.gtceu.shape.extruder.saw.tooltip", "§7Extruder Shape for making Saws");
        provider.add("item.gtceu.shape.extruder.gear.tooltip", "§7Extruder Shape for making Gears");
        provider.add("item.gtceu.shape.extruder.bottle.tooltip", "§7Extruder Shape for making Bottles");
        provider.add("item.gtceu.shape.extruder.gear_small.tooltip", "§7Extruder Shape for making Small Gears");
        provider.add("item.gtceu.shape.extruder.foil.tooltip", "§7Extruder Shape for making Foils from Non-Metals");
        provider.add("item.gtceu.shape.extruder.rod_long.tooltip", "§7Extruder Shape for making Long Rods");
        provider.add("item.gtceu.shape.extruder.rotor.tooltip", "§7Extruder Shape for making Rotors");
        provider.add("item.gtceu.spray.empty.tooltip", "§7Can be filled with sprays of various colors");
        provider.add("fluid_cell.empty", "Empty");
        provider.add("item.gtceu.tool.matchbox.tooltip", "§7This is not a Car");
        provider.add("item.gtceu.tool.lighter.platinum.tooltip", "§7A known Prank Master is engraved on it");
        provider.add("item.gtceu.battery.hull.lv.tooltip", "§7An empty LV Battery Hull");
        provider.add("item.gtceu.battery.hull.mv.tooltip", "§7An empty §bMV §7Battery Hull");
        provider.add("item.gtceu.battery.hull.hv.tooltip", "§7An empty §6HV §7Battery Hull");
        provider.add("item.gtceu.battery.hull.ev.tooltip", "§7An empty §5EV §7Battery Hull");
        provider.add("item.gtceu.battery.hull.iv.tooltip", "§7An empty §1IV §7Battery Hull");
        provider.add("item.gtceu.battery.hull.luv.tooltip", "§7An empty §dLuV §7Battery Hull");
        provider.add("item.gtceu.battery.hull.zpm.tooltip", "§7An empty §fZPM §7Battery Hull");
        provider.add("item.gtceu.battery.hull.uv.tooltip", "§7An empty §3UV §7Battery Hull");
        provider.add("item.gtceu.battery.charge_time", "§aHolds %s%s of Power (%s)");
        provider.add("item.gtceu.battery.charge_detailed", "%d/%d EU - Tier %s §7(§%c%d%s remaining§7)");
        provider.add("item.gtceu.battery.charge_unit.second", "sec");
        provider.add("item.gtceu.battery.charge_unit.minute", "min");
        provider.add("item.gtceu.battery.charge_unit.hour", "hr");
        provider.add("item.gtceu.battery.re.ulv.tantalum.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.lv.cadmium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.lv.lithium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.lv.sodium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.mv.cadmium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.mv.lithium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.mv.sodium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.hv.cadmium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.hv.lithium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.re.hv.sodium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.ev.vanadium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.iv.vanadium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.luv.vanadium.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.zpm.naquadria.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.battery.uv.naquadria.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy_crystal.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.lapotron_crystal.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy.lapotronic_orb.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy.lapotronic_orb_cluster.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy.module.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.energy.cluster.tooltip", "§7Reusable Battery");
        provider.add("item.gtceu.max.battery.tooltip", "§7Fill this to win Minecraft");
        provider.add("item.gtceu.electric.pump.tooltip", "§7Transfers §fFluids§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.fluid.regulator.tooltip", "§7Limits §fFluids§7 to specific quantities as §fCover§7.");
        provider.add("item.gtceu.conveyor.module.tooltip", "§7Transfers §fItems§7 at specific rates as §fCover§7.");
        provider.add("item.gtceu.robot.arm.tooltip", "§7Limits §fItems§7 to specific quantities as §fCover§7.");
        provider.add("item.gtceu.tool.datastick.tooltip", "§7A Low Capacity Data Storage");
        provider.add("item.gtceu.tool.dataorb.tooltip", "§7A High Capacity Data Storage");
        provider.add("item.gtceu.circuit.integrated.tooltip", "§7Use to open configuration GUI");
        provider.add("item.gtceu.circuit.integrated.gui", "§7Programmed Circuit Configuration");
        //multilineLang(provider, "item.gtceu.circuit.integrated.jei_description", "JEI is only showing recipes for the given configuration.\n\nYou can select a configuration in the Programmed Circuit configuration tab.");
        provider.add("item.glass.lens", "Glass Lens (White)"); // todo move to material overrides
        provider.add("item.gtceu.boule.silicon.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.boule.glowstone.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.boule.naquadah.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.boule.neutronium.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.wafer.silicon.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.wafer.glowstone.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.wafer.naquadah.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.wafer.neutronium.tooltip", "§7Raw Circuit");
        provider.add("item.gtceu.board.coated.tooltip", "§7A Coated Board");
        provider.add("item.gtceu.board.phenolic.tooltip", "§7A Good Board");
        provider.add("item.gtceu.board.plastic.tooltip", "§7A Good Board");
        provider.add("item.gtceu.board.epoxy.tooltip", "§7An Advanced Board");
        provider.add("item.gtceu.board.fiber_reinforced.tooltip", "§7An Extreme Board");
        provider.add("item.gtceu.board.multilayer.fiber_reinforced.tooltip", "§7An Elite Board");
        provider.add("item.gtceu.board.wetware.tooltip", "§7The Board that keeps life");
        provider.add("item.gtceu.circuit_board.basic.tooltip", "§7A Basic Circuit Board");
        provider.add("item.gtceu.circuit_board.good.tooltip", "§7A Good Circuit Board");
        provider.add("item.gtceu.circuit_board.plastic.tooltip", "§7A Good Circuit Board");
        provider.add("item.gtceu.circuit_board.advanced.tooltip", "§7An Advanced Circuit Board");
        provider.add("item.gtceu.circuit_board.extreme.tooltip", "§7A More Advanced Circuit Board");
        provider.add("item.gtceu.circuit_board.elite.tooltip", "§7An Elite Circuit Board");
        provider.add("item.gtceu.circuit_board.wetware.tooltip", "§7The Board that keeps life");
        multilineLang(provider, "item.gtceu.circuit.vacuum_tube.tooltip", "§7Technically a Diode\n§cULV-tier");
        provider.add("item.gtceu.component.diode.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.component.resistor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.component.transistor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.component.capacitor.tooltip", "§7Basic Electronic Component");
        provider.add("item.gtceu.component.inductor.tooltip", "§7A Small Coil");
        provider.add("item.gtceu.component.smd.diode.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.component.smd.capacitor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.component.smd.transistor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.component.smd.resistor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.component.smd.inductor.tooltip", "§7Electronic Component");
        provider.add("item.gtceu.component.advanced_smd.diode.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.component.advanced_smd.capacitor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.component.advanced_smd.transistor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.component.advanced_smd.resistor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.component.advanced_smd.inductor.tooltip", "§7Advanced Electronic Component");
        provider.add("item.gtceu.wafer.highly_advanced_system_on_chip.tooltip", "§7Raw Highly Advanced Circuit");
        provider.add("item.gtceu.wafer.advanced_system_on_chip.tooltip", "§7Raw Advanced Circuit");
        provider.add("item.gtceu.wafer.integrated_logic_circuit.tooltip", "§7Raw Integrated Circuit");
        provider.add("item.gtceu.wafer.central_processing_unit.tooltip", "§7Raw Processing Unit");
        provider.add("item.gtceu.wafer.high_power_integrated_circuit.tooltip", "§7Raw High Power Circuit");
        provider.add("item.gtceu.wafer.ultra_high_power_integrated_circuit.tooltip", "§7Raw Ultra High Power Circuit");
        provider.add("item.gtceu.wafer.nand_memory_chip.tooltip", "§7Raw Logic Gate");
        provider.add("item.gtceu.wafer.ultra_low_power_integrated_circuit.tooltip", "§7Raw Ultra Low Power Circuit");
        provider.add("item.gtceu.wafer.low_power_integrated_circuit.tooltip", "§7Raw Low Power Circuit");
        provider.add("item.gtceu.wafer.power_integrated_circuit.tooltip", "§7Raw Power Circuit");
        provider.add("item.gtceu.wafer.nano_central_processing_unit.tooltip", "§7Raw Nano Circuit");
        provider.add("item.gtceu.wafer.nor_memory_chip.tooltip", "§7Raw Logic Gate");
        provider.add("item.gtceu.wafer.qbit_central_processing_unit.tooltip", "§7Raw Qubit Circuit");
        provider.add("item.gtceu.wafer.random_access_memory.tooltip", "§7Raw Memory");
        provider.add("item.gtceu.wafer.system_on_chip.tooltip", "§7Raw Basic Circuit");
        provider.add("item.gtceu.wafer.simple_system_on_chip.tooltip", "§7Raw Simple Circuit");
        provider.add("item.gtceu.engraved.crystal_chip.tooltip", "§7Needed for Circuits");
        provider.add("item.gtceu.crystal.raw.tooltip", "§7Raw Crystal Processor");
        provider.add("item.gtceu.crystal.raw_chip.tooltip", "§7Raw Crystal Processor Parts");
        provider.add("item.gtceu.crystal.central_processing_unit.tooltip", "§7Crystal Processing Unit");
        provider.add("item.gtceu.crystal.system_on_chip.tooltip", "§7Crystal System on Chip");
        provider.add("item.gtceu.plate.advanced_system_on_chip.tooltip", "§7Advanced System on Chip");
        provider.add("item.gtceu.plate.highly_advanced_system_on_chip.tooltip", "§7Highly Advanced System on Chip");
        provider.add("item.gtceu.plate.integrated_logic_circuit.tooltip", "§7Integrated Logic Circuit");
        provider.add("item.gtceu.plate.central_processing_unit.tooltip", "§7Central Processing Unit");
        provider.add("item.gtceu.plate.high_power_integrated_circuit.tooltip", "§7High Power IC");
        provider.add("item.gtceu.plate.ultra_high_power_integrated_circuit.tooltip", "§7Ultra High Power IC");
        provider.add("item.gtceu.plate.nand_memory_chip.tooltip", "§7NAND Logic Gate");
        provider.add("item.gtceu.plate.nano_central_processing_unit.tooltip", "§7Nano Central Processing Unit");
        provider.add("item.gtceu.plate.nor_memory_chip.tooltip", "§7NOR Logic Gate");
        provider.add("item.gtceu.plate.ultra_low_power_integrated_circuit.tooltip", "§7Ultra Low Power IC");
        provider.add("item.gtceu.plate.low_power_integrated_circuit.tooltip", "§7Low Power IC");
        provider.add("item.gtceu.plate.power_integrated_circuit.tooltip", "§7Power IC");
        provider.add("item.gtceu.plate.qbit_central_processing_unit.tooltip", "§7Qubit Central Processing Unit");
        provider.add("item.gtceu.plate.random_access_memory.tooltip", "§7Random Access Memory");
        provider.add("item.gtceu.plate.system_on_chip.tooltip", "§7System on Chip");
        provider.add("item.gtceu.plate.simple_system_on_chip.tooltip", "§7Simple System on Chip");
        multilineLang(provider, "item.gtceu.circuit.electronic.tooltip", "§7Your First Circuit\n§cLV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.good_electronic.tooltip", "§7Your Second Circuit\n§cMV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.basic_integrated.tooltip", "§7Smaller and more powerful\n§6LV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.good_integrated.tooltip", "§7Smaller and more powerful\n§6MV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.advanced_integrated.tooltip", "§7Smaller and more powerful\n§6HV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.nand_chip.tooltip", "§7A Superior Simple Circuit\n§6ULV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.microprocessor.tooltip", "§7A Superior Basic Circuit\n§eLV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.processor.tooltip", "§7Amazing Computation Speed!\n§eMV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.assembly.tooltip", "§7Amazing Computation Speed!\n§eHV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.workstation.tooltip", "§7Amazing Computation Speed!\n§eEV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.mainframe.tooltip", "§7Amazing Computation Speed!\n§eIV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.nano_processor.tooltip", "§7Smaller than ever\n§bHV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.nano_assembly.tooltip", "§7Smaller than ever\n§bEV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.nano_computer.tooltip", "§7Smaller than ever\n§bIV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.nano_mainframe.tooltip", "§7Smaller than ever\n§bLuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.quantum_processor.tooltip", "§7Quantum Computing comes to life!\n§aEV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.quantum_assembly.tooltip", "§7Quantum Computing comes to life!\n§aIV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.quantum_computer.tooltip", "§7Quantum Computing comes to life!\n§aLuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.quantum_mainframe.tooltip", "§7Quantum Computing comes to life!\n§aZPM-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.crystal_processor.tooltip", "§7Taking Advantage of Crystal Engraving\n§9IV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.crystal_assembly.tooltip", "§7Taking Advantage of Crystal Engraving\n§9LuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.crystal_computer.tooltip", "§7Taking Advantage of Crystal Engraving\n§9ZPM-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.crystal_mainframe.tooltip", "§7Taking Advantage of Crystal Engraving\n§9UV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.wetware_processor.tooltip", "§7You have a feeling like it's watching you\n§4LuV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.wetware_assembly.tooltip", "§7Can run Minecraft\n§4ZPM-tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.wetware_computer.tooltip", "§7Ultimate fusion of Flesh and Machine\n§4UV-Tier Circuit");
        multilineLang(provider, "item.gtceu.circuit.wetware_mainframe.tooltip", "§7The best Man has ever seen\n§4UHV-Tier Circuit");
        provider.add("item.gtceu.stem_cells.tooltip", "§7Raw Intelligence");
        provider.add("item.gtceu.processor.neuro.tooltip", "§7Neuro CPU");
        provider.add("item.gtceu.petri_dish.tooltip", "§7For cultivating Cells");
        provider.add("item.gtceu.neutron_reflector.tooltip", "§7Indestructible");
        provider.add("item.gtceu.duct_tape.tooltip", "§7If you can't fix it with this, use more of it!");
        provider.add("item.gtceu.quantumeye.tooltip", "§7Improved Ender Eye");
        provider.add("item.gtceu.quantumstar.tooltip", "§7Improved Nether Star");
        provider.add("item.gtceu.gravistar.tooltip", "§7Ultimate Nether Star");
        multilineLang(provider, "item.gtceu.item_filter.tooltip", "§7Filters §fItem§7 I/O as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "item.gtceu.ore_dictionary_filter.tooltip", "§7Filters §fItem§7 I/O with §fOre Dictionary§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "item.gtceu.fluid_filter.tooltip", "§7Filters §fFluid§7 I/O as §fCover§7.\nCan be used as an §fElectric Pump§7 and §fFluid Regulator§7 upgrade.");
        multilineLang(provider, "item.gtceu.smart_item_filter.tooltip", "§7Filters §fItem§7 I/O with §fMachine Recipes§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.add("item.gtceu.cover.controller.tooltip", "§7Turns Machines §fON/OFF§7 as §fCover§7.");
        provider.add("item.gtceu.cover.activity.detector.tooltip", "§7Gives out §fActivity Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.activity.detector_advanced.tooltip", "§7Gives out §fMachine Progress§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.fluid.detector.tooltip", "§7Gives out §fFluid Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.fluid.detector.advanced.tooltip", "§7Gives §fRS-Latch§7 controlled §fFluid Storage Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.item.detector.tooltip", "§7Gives out §fItem Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.item.detector.advanced.tooltip", "§7Gives §fRS-Latch§7 controlled §fItem Storage Status§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.energy.detector.tooltip", "§7Gives out §fEnergy Amount§7 as Redstone as §fCover§7.");
        provider.add("item.gtceu.cover.energy.detector.advanced.tooltip", "§7Gives §fRS-Latch§7 controlled §fEnergy Status§7 as Redstone as §fCover§7.");
        multilineLang(provider, "item.gtceu.cover.fluid.voiding.tooltip", "§7Voids §fFluids§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.cover.fluid.voiding.advanced.tooltip", "§7Voids §fFluids§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.cover.item.voiding.tooltip", "§7Voids §fItems§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "item.gtceu.cover.item.voiding.advanced.tooltip", "§7Voids §fItems§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        provider.add("item.gtceu.cover.facade.tooltip", "§7Decorative Outfit §fCover§7.");
        provider.add("item.gtceu.cover.screen.tooltip", "§7Displays §fData§7 as §fCover§7.");
        provider.add("item.gtceu.cover.crafting.tooltip", "§fAdvanced Workbench§7 on a Machine as §fCover§7.");
        provider.add("item.gtceu.cover.shutter.tooltip", "§fBlocks Transfer§7 through attached Side as §fCover§7.");
        multilineLang(provider, "item.gtceu.cover.solar.panel.tooltip", "§7May the Sun be with you.\nProduces §fEnergy§7 from the §eSun§7 as §fCover§7.");
        provider.add("item.gtceu.cover.infinite_water.tooltip", "§7Fills attached containers with §9Water§7 as §fCover§7.");
        provider.add("item.gtceu.cover.ender_fluid_link.tooltip", "§7Transports §fFluids§7 with a §fWireless §dEnder§f Connection§7 as §fCover§7.");
        provider.add("item.gtceu.gelled_toluene.tooltip", "§7Raw Explosive");
        provider.add("item.gtceu.bottle.purple.drink.tooltip", "§7How about Lemonade. Or some Ice Tea? I got Purple Drink!");
        multilineLang(provider, "item.gtceu.foam_sprayer.tooltip", "§7Sprays Construction Foam\nUse on a frame to foam connected frames\nFoam can be colored");
        provider.add("item.gtceu.brick.fireclay.tooltip", "§7Heat resistant");
    }
}
