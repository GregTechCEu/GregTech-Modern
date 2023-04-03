package com.gregtechceu.gtceu.data.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote LangHandler
 */
public class LangHandler {

    public static void init(RegistrateLangProvider provider) {
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
        // CreativeModeTabs
        provider.add(GTCreativeModeTabs.MATERIAL_FLUID, toEnglishName(GTCreativeModeTabs.MATERIAL_FLUID.getGroupId()));
        provider.add(GTCreativeModeTabs.MATERIAL_ITEM, toEnglishName(GTCreativeModeTabs.MATERIAL_ITEM.getGroupId()));
        provider.add(GTCreativeModeTabs.MATERIAL_BLOCK, toEnglishName(GTCreativeModeTabs.MATERIAL_BLOCK.getGroupId()));
        provider.add(GTCreativeModeTabs.MATERIAL_PIPE, toEnglishName(GTCreativeModeTabs.MATERIAL_PIPE.getGroupId()));
        provider.add(GTCreativeModeTabs.DECORATION, toEnglishName(GTCreativeModeTabs.DECORATION.getGroupId()));
        provider.add(GTCreativeModeTabs.TOOL, toEnglishName(GTCreativeModeTabs.TOOL.getGroupId()));
        provider.add(GTCreativeModeTabs.MACHINE, toEnglishName(GTCreativeModeTabs.MACHINE.getGroupId()));
        provider.add(GTCreativeModeTabs.ITEM, toEnglishName(GTCreativeModeTabs.ITEM.getGroupId()));
        // Casings
        //casingLang(provider, GTBlocks.CASING.get()); todo
        //casingLang(provider, GTBlocks.WIRE_COIL.get());
        //casingLang(provider, GTBlocks.ACTIVE_CASING.get());
        //casingLang(provider, GTBlocks.BOILER_FIREBOX_CASING.get());
        //casingLang(provider, GTBlocks.HULL_CASING.get()); todo
        provider.add("gtceu.gui.editor.tips.citation", "Number of citations");
        provider.add("gtceu.gui.editor.group.recipe_type", "cap");
        provider.add("ldlib.gui.editor.register.project.rtui", "RecipeType UI Project");
        provider.add("ldlib.gui.editor.register.menu.recipe_type_tab", "recipe type");
        provider.add("recipe.condition.rpm.tooltip", "RPM: %d");
        provider.add("recipe.condition.thunder.tooltip", "Thunder Level: %d");
        provider.add("recipe.condition.rain.tooltip", "Rain Level: %d");
        provider.add("recipe.condition.dimension.tooltip", "Dimension: %s");
        provider.add("recipe.condition.biome.tooltip", "Biome: %s");
        provider.add("recipe.condition.pos_y.tooltip", "Y Level: %d <= Y <= %d");
        provider.add("recipe.condition.steam_vent.tooltip", "Clean steam vent");
        provider.add("recipe.condition.rock_breaker.tooltip", "Fluid blocks around");
        provider.add("death.attack.heat", "%s was boiled alive");
        provider.add("death.attack.frost", "%s explored cryogenics");
        provider.add("death.attack.chemical", "%s had a chemical accident");
        provider.add("death.attack.electric", "%s was electrocuted");
        provider.add("death.attack.radiation", "%s glows with joy now");
        provider.add("death.attack.turbine", "%s put their head into a turbine");
        provider.add("death.attack.explosion", "%s exploded");
        provider.add("death.attack.explosion.player", "%s exploded with help of %s");
        provider.add("death.attack.heat.player", "%s was boiled alive by %s");
        provider.add("death.attack.pickaxe", "%s got mined by %s");
        provider.add("death.attack.shovel", "%s got dug up by %s");
        provider.add("death.attack.axe", "%s has been chopped by %s");
        provider.add("death.attack.hoe", "%s had their head tilled by %s");
        provider.add("death.attack.hammer", "%s was squashed by %s");
        provider.add("death.attack.mallet", "%s got hammered to death by %s");
        provider.add("death.attack.mining_hammer", "%s was mistaken for Ore by %s");
        provider.add("death.attack.spade", "%s got excavated by %s");
        provider.add("death.attack.wrench", "%s gave %s a whack with the Wrench!");
        provider.add("death.attack.file", "%s has been filed D for 'Dead' by %s");
        provider.add("death.attack.crowbar", "%s lost half a life to %s");
        provider.add("death.attack.screwdriver", "%s has screwed with %s for the last time!");
        provider.add("death.attack.mortar", "%s was ground to dust by %s");
        provider.add("death.attack.wire_cutter", "%s has cut the cable for the Life Support Machine of %s");
        provider.add("death.attack.scythe", "%s had their soul taken by %s");
        provider.add("death.attack.knife", "%s was gently poked by %s");
        provider.add("death.attack.butchery_knife", "%s was butchered by %s");
        provider.add("death.attack.drill_lv", "%s was drilled with 32V by %s");
        provider.add("death.attack.drill_mv", "%s was drilled with 128V by %s");
        provider.add("death.attack.drill_hv", "%s was drilled with 512V by %s");
        provider.add("death.attack.drill_ev", "%s was drilled with 2048V by %s");
        provider.add("death.attack.drill_iv", "%s was drilled with 8192V by %s");
        provider.add("death.attack.chainsaw_lv", "%s was massacred by %s");
        provider.add("death.attack.wrench_lv", "%s's pipes were loosened by %s");
        provider.add("death.attack.wrench_hv", "%s's pipes were loosened by %s");
        provider.add("death.attack.wrench_iv", "%s had a Monkey Wrench thrown into their plans by %s");
        provider.add("death.attack.buzzsaw", "%s got buzzed by %s");
        provider.add("death.attack.screwdriver_lv", "%s had their screws removed by %s");
        provider.add("enchantment.disjunction", "Disjunction");
        provider.add("gtceu.multiblock.steam_grinder.description", "A Multiblock Macerator at the Steam Age. Requires at least 14 Bronze Casings to form. Cannot use normal Input/Output busses, nor Fluid Hatches other than the Steam Hatch.");
        provider.add("gtceu.multiblock.steam.low_steam", "Not enough Steam to run!");
        provider.add("gtceu.multiblock.steam.steam_stored", "Steam: %s / %s mb");
        provider.add("gtceu.machine.steam.steam_hatch.tooltip", "§eAccepted Fluid: §fSteam");
        provider.add("gtceu.machine.steam_bus.tooltip", "Does not work with non-steam multiblocks");
        provider.add("gtceu.multiblock.steam_oven.description", "A Multi Smelter at the Steam Age. Requires at least 6 Bronze Casings to form. Cannot use normal Input/Output busses, nor Fluid Hatches other than the Steam Hatch. Steam Hatch must be on the bottom layer, no more than one.");
        provider.add("gtceu.multiblock.require_steam_parts", "Requires Steam Hatches and Buses!");
        provider.add("gtceu.multiblock.steam_.duration_modifier", "Takes §f1.5x §7base duration to process, not affected by number of items.");
        provider.add("gtceu.top.working_disabled", "Working Disabled");
        provider.add("gtceu.top.energy_consumption", "Using");
        provider.add("gtceu.top.energy_production", "Producing");
        provider.add("gtceu.top.transform_up", "§cStep Up§r");
        provider.add("gtceu.top.transform_down", "§aStep Down§r");
        provider.add("gtceu.top.transform_input", "§6Input:§r");
        provider.add("gtceu.top.transform_output", "§9Output:§r");
        provider.add("gtceu.top.convert_eu", "Converting §eEU§r -> §cFE§r");
        provider.add("gtceu.top.convert_fe", "Converting §cFE§r -> §eEU§r");
        provider.add("gtceu.top.fuel_min_consume", "Needs");
        provider.add("gtceu.top.fuel_none", "No fuel");
        provider.add("gtceu.top.invalid_structure", "Structure Incomplete");
        provider.add("gtceu.top.valid_structure", "Structure Formed");
        provider.add("gtceu.top.obstructed_structure", "Structure Obstructed");
        provider.add("gtceu.top.maintenance_fixed", "Maintenance Fine");
        provider.add("gtceu.top.maintenance_broken", "Needs Maintenance");
        provider.add("gtceu.top.maintenance.wrench", "Pipe is loose");
        provider.add("gtceu.top.maintenance.screwdriver", "Screws are loose");
        provider.add("gtceu.top.maintenance.soft_mallet", "Something is stuck");
        provider.add("gtceu.top.maintenance.hard_hammer", "Plating is dented");
        provider.add("gtceu.top.maintenance.wire_cutter", "Wires burned out");
        provider.add("gtceu.top.maintenance.crowbar", "That doesn't belong there");
        provider.add("gtceu.top.primitive_pump_production", "Production:");
        provider.add("gtceu.top.filter.label", "Filter:");
        provider.add("gtceu.top.link_cover.color", "Color:");
        provider.add("gtceu.top.mode.export", "Exporting");
        provider.add("gtceu.top.mode.import", "Importing");
        provider.add("gtceu.top.unit.items", "Items");
        provider.add("gtceu.top.unit.fluid_milibuckets", "L");
        provider.add("gtceu.top.unit.fluid_buckets", "kL");
        provider.add("gtceu.multiblock.title", "Multiblock Pattern");
        provider.add("gtceu.multiblock.primitive_blast_furnace.bronze.description", "The Primitive Blast Furnace (PBF) is a multiblock structure used for cooking steel in the early game. Although not very fast, it will provide you with steel for your first setups.");
        provider.add("gtceu.multiblock.coke_oven.description", "The Coke Oven is a multiblock structure used for getting coke and creosote in the early game. It doesn't require fuel and has an internal tank of 32 buckets for creosote. Its inventory can be accessed via its Coke Oven Hatch.");
        provider.add("gtceu.multiblock.vacuum_freezer.description", "The Vacuum Freezer is a multiblock structure mainly used for freezing Hot Ingots into regular Ingots. However, it can also freeze other substances, such as Water.");
        provider.add("gtceu.multiblock.implosion_compressor.description", "The Implosion Compressor is a multiblock structure that uses explosives to turn gem dusts into their corresponding gems.");
        provider.add("gtceu.multiblock.pyrolyse_oven.description", "The Pyrolyse Oven is a multiblock structure used for turning Logs into Charcoal and Creosote Oil, or Ash and Heavy Oil.");
        provider.add("gtceu.multiblock.cracker.description", "The Oil Cracking Unit is a multiblock structure used for turning Light and Heavy Fuel into their Cracked variants.");
        provider.add("gtceu.multiblock.large_combustion_engine.description", "The Large Combustion Engine is a multiblock structure that acts as a Combustion Generator for EV power.");
        provider.add("gtceu.multiblock.extreme_combustion_engine.description", "The Extreme Combustion Engine is a multiblock structure that acts as a Combustion Generator for IV power.");
        provider.add("gtceu.multiblock.distillation_tower.description", "The Distillation Tower is a multiblock structure used for distilling the various types of Oil and some of their byproducts. Each layer must have exactly one output hatch, starting from the second one. The bottom layer can output items and insert fluids in any position.");
        provider.add("gtceu.multiblock.electric_blast_furnace.description", "The Electric Blast Furnace (EBF) is a multiblock structure used for smelting alloys, cooking metals and refining ores. It is required for obtaining high-tier alloys and metals, such as aluminium, stainless steel, titanium, and naquadah alloy.");
        provider.add("gtceu.multiblock.multi_furnace.description", "The Multi Smelter is a multiblock structure used for smelting massive amounts of items at once. Different tiers of coils provide a speed boost and energy efficiency gain. 32 is the base value of items smelted per operation, and can be multiplied by using higher level coils.");
        provider.add("gtceu.multiblock.large_boiler.description", "Large Boilers are multiblocks that generate steam from an energy source and water. Said energy source is either any Solid Fuel with a Burn Time, or a Diesel/Semi-Fluid Fuel. Can be throttled back in increments of 5%% to reduce Steam output and Fuel consumption.");
        provider.add("gtceu.multiblock.large_turbine.description", "Large Turbines are multiblocks that generate power from steam, gases, and plasma by having them spin the turbine's rotor. Energy output is based on rotor efficiency and current speed of turbine. Gearbox casings are used in the center of the structure.");
        provider.add("gtceu.multiblock.assembly_line.description", "The Assembly Line is a large multiblock structure consisting of 5 to 16 \"slices\". In theory, it's large Assembling Machine, used for creating advanced crafting components.");
        provider.add("gtceu.multiblock.fusion_reactor.luv.description", "The Fusion Reactor Mark 1 is a large multiblock structure used for fusing elements into heavier ones. It can only use LuV, ZPM, and UV Energy Hatches. For every Hatch it has, its buffer increases by 10M EU, and has a maximum of 160M.");
        provider.add("gtceu.multiblock.fusion_reactor.zpm.description", "The Fusion Reactor Mark 2 is a large multiblock structure used for fusing elements into heavier ones. It can only use ZPM and UV Energy Hatches. For every Hatch it has, its buffer increases by 20M EU, and has a maximum of 320M.");
        provider.add("gtceu.multiblock.fusion_reactor.uv.description", "The Fusion Reactor Mark 3 is a large multiblock structure used for fusing elements into heavier ones. It can only use UV Energy Hatches. For every Hatch it has, its buffer increases by 40M EU, and has a maximum of 640M.");
        provider.add("gtceu.multiblock.fusion_reactor.energy", "EU: %d / %d");
        provider.add("gtceu.multiblock.fusion_reactor.heat", "Heat: %d");
        provider.add("gtceu.multiblock.large_chemical_reactor.description", "The Large Chemical Reactor performs chemical reactions at 100%% energy efficiency. Overclocks multiply both speed and energy by 4. The multiblock requires exactly 1 Cupronickel Coil Block, which must be placed adjacent to the PTFE Pipe casing located in the center.");
        provider.add("gtceu.multiblock.primitive_water_pump.description", "The Primitive Water Pump is a pre-Steam Era multiblock that collects water once per second, depending on the Biome it is in. It can use a Pump, ULV, or LV Output Hatch, increasing the amount of water per tier. Follows the formula: Biome Coefficient * Hatch Multiplier.");
        multilineLang(provider, "gtceu.multiblock.primitive_water_pump.extra1", "Biome Coefficient:\n  Ocean, River: 1000 L/s\n  Swamp: 800 L/s\n  Jungle: 350 L/s\n  Snowy: 300 L/s\n  Plains, Forest: 250 L/s\n  Taiga: 175 L/s\n  Beach: 170 L/s\n  Other: 100 L/s");
        multilineLang(provider, "gtceu.multiblock.primitive_water_pump.extra2", "Hatch Multipliers:\n  Pump Hatch: 1x\n  ULV Output Hatch: 2x\n  LV Output Hatch: 4x\n\nWhile raining in the Pump's Biome, the total water production will be increased by 50%%.");
        provider.add("gtceu.multiblock.processing_array.description", "The Processing Array combines up to 16 single block machine(s) in a single multiblock, effectively easing automation.");
        provider.add("gtceu.multiblock.advanced_processing_array.description", "The Processing Array combines up to 64 single block machine(s) in a single multiblock, effectively easing automation.");
        provider.add("item.invalid.name", "Invalid item");
        provider.add("fluid.empty", "Empty");
        provider.add("gtceu.tooltip.hold_shift", "Hold SHIFT for more info");
        provider.add("gtceu.tooltip.hold_ctrl", "Hold CTRL for more info");
        provider.add("gtceu.tooltip.fluid_pipe_hold_shift", "Hold SHIFT to show Fluid Containment Info");
        provider.add("gtceu.tooltip.tool_fluid_hold_shift", "Hold SHIFT to show Fluid Containment and Tool Info");
        provider.add("metaitem.generic.fluid_container.tooltip", "%d/%dL %s");
        provider.add("metaitem.generic.electric_item.tooltip", "%d/%d EU - Tier %s");
        provider.add("metaitem.generic.electric_item.stored", "%d/%d EU(%s)");
        provider.add("metaitem.electric.discharge_mode.enabled", "§eDischarge Mode Enabled");
        provider.add("metaitem.electric.discharge_mode.disabled", "§eDischarge Mode Disabled");
        provider.add("metaitem.electric.discharge_mode.tooltip", "Use while sneaking to toggle discharge mode");
        provider.add("metaitem.dust.tooltip.purify", "Throw into Cauldron to get clean Dust");
        provider.add("metaitem.crushed.tooltip.purify", "Throw into Cauldron to get Purified Ore");
        provider.add("metaitem.int_circuit.configuration", "Configuration: %d");
        provider.add("metaitem.credit.copper.tooltip", "0.125 Credits");
        provider.add("metaitem.credit.cupronickel.tooltip", "1 Credit");
        provider.add("metaitem.credit.silver.tooltip", "8 Credits");
        provider.add("metaitem.credit.gold.tooltip", "64 Credits");
        provider.add("metaitem.credit.platinum.tooltip", "512 Credits");
        provider.add("metaitem.credit.osmium.tooltip", "4096 Credits");
        provider.add("metaitem.credit.naquadah.tooltip", "32768 Credits");
        provider.add("metaitem.credit.neutronium.tooltip", "262144 Credits");
        provider.add("metaitem.coin.gold.ancient.tooltip", "Found in ancient Ruins");
        provider.add("metaitem.coin.doge.tooltip", "wow much coin how money so crypto plz mine v rich very currency wow");
        provider.add("metaitem.coin.chocolate.tooltip", "Wrapped in Gold");
        provider.add("metaitem.shape.empty.tooltip", "Raw Plate to make Molds and Extrude Shapes");
        provider.add("metaitem.nano_saber.tooltip", "Ryujin no ken wo kurae!");
        provider.add("metaitem.shape.mold.plate.tooltip", "Mold for making Plates");
        provider.add("metaitem.shape.mold.casing.tooltip", "Mold for making Item Casings");
        provider.add("metaitem.shape.mold.gear.tooltip", "Mold for making Gears");
        provider.add("metaitem.shape.mold.credit.tooltip", "Secure Mold for making Coins (Don't lose it!)");
        provider.add("metaitem.shape.mold.bottle.tooltip", "Mold for making Bottles");
        provider.add("metaitem.shape.mold.ingot.tooltip", "Mold for making Ingots");
        provider.add("metaitem.shape.mold.ball.tooltip", "Mold for making Balls");
        provider.add("metaitem.shape.mold.block.tooltip", "Mold for making Blocks");
        provider.add("metaitem.shape.mold.nugget.tooltip", "Mold for making Nuggets");
        provider.add("metaitem.shape.mold.cylinder.tooltip", "Mold for shaping Cylinders");
        provider.add("metaitem.shape.mold.anvil.tooltip", "Mold for shaping Anvils");
        provider.add("metaitem.shape.mold.name.tooltip", "Mold for naming Items in the Forming Press (rename Mold with Anvil)");
        provider.add("metaitem.shape.mold.gear.small.tooltip", "Mold for making small Gears");
        provider.add("metaitem.shape.mold.rotor.tooltip", "Mold for making Rotors");
        provider.add("metaitem.shape.extruder.plate.tooltip", "Extruder Shape for making Plates");
        provider.add("metaitem.shape.extruder.rod.tooltip", "Extruder Shape for making Rods");
        provider.add("metaitem.shape.extruder.bolt.tooltip", "Extruder Shape for making Bolts");
        provider.add("metaitem.shape.extruder.ring.tooltip", "Extruder Shape for making Rings");
        provider.add("metaitem.shape.extruder.cell.tooltip", "Extruder Shape for making Cells");
        provider.add("metaitem.shape.extruder.ingot.tooltip", "Extruder Shape for, wait, can't we just use a Furnace?");
        provider.add("metaitem.shape.extruder.wire.tooltip", "Extruder Shape for making Wires");
        provider.add("metaitem.shape.extruder.casing.tooltip", "Extruder Shape for making Item Casings");
        provider.add("metaitem.shape.extruder.pipe.tiny.tooltip", "Extruder Shape for making tiny Pipes");
        provider.add("metaitem.shape.extruder.pipe.small.tooltip", "Extruder Shape for making small Pipes");
        provider.add("metaitem.shape.extruder.pipe.normal.tooltip", "Extruder Shape for making Pipes");
        provider.add("metaitem.shape.extruder.pipe.large.tooltip", "Extruder Shape for making large Pipes");
        provider.add("metaitem.shape.extruder.pipe.huge.tooltip", "Extruder Shape for making full Block Pipes");
        provider.add("metaitem.shape.extruder.block.tooltip", "Extruder Shape for making Blocks");
        provider.add("metaitem.shape.extruder.sword.tooltip", "Extruder Shape for making Swords");
        provider.add("metaitem.shape.extruder.pickaxe.tooltip", "Extruder Shape for making Pickaxes");
        provider.add("metaitem.shape.extruder.shovel.tooltip", "Extruder Shape for making Shovels");
        provider.add("metaitem.shape.extruder.axe.tooltip", "Extruder Shape for making Axes");
        provider.add("metaitem.shape.extruder.hoe.tooltip", "Extruder Shape for making Hoes");
        provider.add("metaitem.shape.extruder.hammer.tooltip", "Extruder Shape for making Hammers");
        provider.add("metaitem.shape.extruder.file.tooltip", "Extruder Shape for making Files");
        provider.add("metaitem.shape.extruder.saw.tooltip", "Extruder Shape for making Saws");
        provider.add("metaitem.shape.extruder.gear.tooltip", "Extruder Shape for making Gears");
        provider.add("metaitem.shape.extruder.bottle.tooltip", "Extruder Shape for making Bottles");
        provider.add("metaitem.shape.extruder.gear_small.tooltip", "Extruder Shape for making Small Gears");
        provider.add("metaitem.shape.extruder.foil.tooltip", "Extruder Shape for making Foils from Non-Metals");
        provider.add("metaitem.shape.extruder.rod_long.tooltip", "Extruder Shape for making Long Rods");
        provider.add("metaitem.shape.extruder.rotor.tooltip", "Extruder Shape for making Rotors");
        provider.add("metaitem.spray.empty.tooltip", "Can be filled with sprays of various colors");
        provider.add("fluid_cell.empty", "Empty");
        provider.add("metaitem.tool.matchbox.tooltip", "This is not a Car");
        provider.add("metaitem.tool.lighter.platinum.tooltip", "A known Prank Master is engraved on it");
        provider.add("metaitem.battery.hull.lv.tooltip", "An empty §7LV §7Battery Hull");
        provider.add("metaitem.battery.hull.mv.tooltip", "An empty §bMV §7Battery Hull");
        provider.add("metaitem.battery.hull.hv.tooltip", "An empty §6HV §7Battery Hull");
        provider.add("metaitem.battery.hull.ev.tooltip", "An empty §5EV §7Battery Hull");
        provider.add("metaitem.battery.hull.iv.tooltip", "An empty §1IV §7Battery Hull");
        provider.add("metaitem.battery.hull.luv.tooltip", "An empty §dLuV §7Battery Hull");
        provider.add("metaitem.battery.hull.zpm.tooltip", "An empty §fZPM §7Battery Hull");
        provider.add("metaitem.battery.hull.uv.tooltip", "An empty §3UV §7Battery Hull");
        provider.add("metaitem.battery.charge_time", "§aHolds %s%s of Power (%s)");
        provider.add("metaitem.battery.charge_detailed", "%d/%d EU - Tier %s §7(§%c%d%s remaining§7)");
        provider.add("metaitem.battery.charge_unit.second", "sec");
        provider.add("metaitem.battery.charge_unit.minute", "min");
        provider.add("metaitem.battery.charge_unit.hour", "hr");
        provider.add("metaitem.battery.re.ulv.tantalum.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.lv.cadmium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.lv.lithium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.lv.sodium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.mv.cadmium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.mv.lithium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.mv.sodium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.hv.cadmium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.hv.lithium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.re.hv.sodium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.ev.vanadium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.iv.vanadium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.luv.vanadium.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.zpm.naquadria.tooltip", "Reusable Battery");
        provider.add("metaitem.battery.uv.naquadria.tooltip", "Reusable Battery");
        provider.add("metaitem.energy_crystal.tooltip", "Reusable Battery");
        provider.add("metaitem.lapotron_crystal.tooltip", "Reusable Battery");
        provider.add("metaitem.energy.lapotronic_orb.tooltip", "Reusable Battery");
        provider.add("metaitem.energy.lapotronic_orb_cluster.tooltip", "Reusable Battery");
        provider.add("metaitem.energy.module.tooltip", "Reusable Battery");
        provider.add("metaitem.energy.cluster.tooltip", "Reusable Battery");
        provider.add("metaitem.max.battery.tooltip", "Fill this to win Minecraft");
        provider.add("metaitem.electric.pump.tooltip", "Transfers §fFluids§7 at specific rates as §fCover§7.");
        provider.add("metaitem.fluid.regulator.tooltip", "Limits §fFluids§7 to specific quantities as §fCover§7.");
        provider.add("metaitem.conveyor.module.tooltip", "Transfers §fItems§7 at specific rates as §fCover§7.");
        provider.add("metaitem.robot.arm.tooltip", "Limits §fItems§7 to specific quantities as §fCover§7.");
        provider.add("metaitem.tool.datastick.tooltip", "A Low Capacity Data Storage");
        provider.add("metaitem.tool.dataorb.tooltip", "A High Capacity Data Storage");
        provider.add("metaitem.circuit.integrated.tooltip", "Use to open configuration GUI");
        provider.add("metaitem.circuit.integrated.gui", "Programmed Circuit Configuration");
        multilineLang(provider, "metaitem.circuit.integrated.jei_description", "JEI is only showing recipes for the given configuration.\n\nYou can select a configuration in the Programmed Circuit configuration tab.");
        provider.add("item.glass.lens", "Glass Lens (White)");
        provider.add("metaitem.boule.silicon.tooltip", "Raw Circuit");
        provider.add("metaitem.boule.glowstone.tooltip", "Raw Circuit");
        provider.add("metaitem.boule.naquadah.tooltip", "Raw Circuit");
        provider.add("metaitem.boule.neutronium.tooltip", "Raw Circuit");
        provider.add("metaitem.wafer.silicon.tooltip", "Raw Circuit");
        provider.add("metaitem.wafer.glowstone.tooltip", "Raw Circuit");
        provider.add("metaitem.wafer.naquadah.tooltip", "Raw Circuit");
        provider.add("metaitem.wafer.neutronium.tooltip", "Raw Circuit");
        provider.add("metaitem.board.coated.tooltip", "A Coated Board");
        provider.add("metaitem.board.phenolic.tooltip", "A Good Board");
        provider.add("metaitem.board.plastic.tooltip", "A Good Board");
        provider.add("metaitem.board.epoxy.tooltip", "An Advanced Board");
        provider.add("metaitem.board.fiber_reinforced.tooltip", "An Extreme Board");
        provider.add("metaitem.board.multilayer.fiber_reinforced.tooltip", "An Elite Board");
        provider.add("metaitem.board.wetware.tooltip", "The Board that keeps life");
        provider.add("metaitem.circuit_board.basic.tooltip", "A Basic Circuit Board");
        provider.add("metaitem.circuit_board.good.tooltip", "A Good Circuit Board");
        provider.add("metaitem.circuit_board.plastic.tooltip", "A Good Circuit Board");
        provider.add("metaitem.circuit_board.advanced.tooltip", "An Advanced Circuit Board");
        provider.add("metaitem.circuit_board.extreme.tooltip", "A More Advanced Circuit Board");
        provider.add("metaitem.circuit_board.elite.tooltip", "An Elite Circuit Board");
        provider.add("metaitem.circuit_board.wetware.tooltip", "The Board that keeps life");
        multilineLang(provider, "metaitem.circuit.vacuum_tube.tooltip", "Technically a Diode\n§cULV-tier");
        provider.add("metaitem.component.diode.tooltip", "Basic Electronic Component");
        provider.add("metaitem.component.resistor.tooltip", "Basic Electronic Component");
        provider.add("metaitem.component.transistor.tooltip", "Basic Electronic Component");
        provider.add("metaitem.component.capacitor.tooltip", "Basic Electronic Component");
        provider.add("metaitem.component.inductor.tooltip", "A Small Coil");
        provider.add("metaitem.component.smd.diode.tooltip", "Electronic Component");
        provider.add("metaitem.component.smd.capacitor.tooltip", "Electronic Component");
        provider.add("metaitem.component.smd.transistor.tooltip", "Electronic Component");
        provider.add("metaitem.component.smd.resistor.tooltip", "Electronic Component");
        provider.add("metaitem.component.smd.inductor.tooltip", "Electronic Component");
        provider.add("metaitem.component.advanced_smd.diode.tooltip", "Advanced Electronic Component");
        provider.add("metaitem.component.advanced_smd.capacitor.tooltip", "Advanced Electronic Component");
        provider.add("metaitem.component.advanced_smd.transistor.tooltip", "Advanced Electronic Component");
        provider.add("metaitem.component.advanced_smd.resistor.tooltip", "Advanced Electronic Component");
        provider.add("metaitem.component.advanced_smd.inductor.tooltip", "Advanced Electronic Component");
        provider.add("metaitem.wafer.highly_advanced_system_on_chip.tooltip", "Raw Highly Advanced Circuit");
        provider.add("metaitem.wafer.advanced_system_on_chip.tooltip", "Raw Advanced Circuit");
        provider.add("metaitem.wafer.integrated_logic_circuit.tooltip", "Raw Integrated Circuit");
        provider.add("metaitem.wafer.central_processing_unit.tooltip", "Raw Processing Unit");
        provider.add("metaitem.wafer.high_power_integrated_circuit.tooltip", "Raw High Power Circuit");
        provider.add("metaitem.wafer.ultra_high_power_integrated_circuit.tooltip", "Raw Ultra High Power Circuit");
        provider.add("metaitem.wafer.nand_memory_chip.tooltip", "Raw Logic Gate");
        provider.add("metaitem.wafer.ultra_low_power_integrated_circuit.tooltip", "Raw Ultra Low Power Circuit");
        provider.add("metaitem.wafer.low_power_integrated_circuit.tooltip", "Raw Low Power Circuit");
        provider.add("metaitem.wafer.power_integrated_circuit.tooltip", "Raw Power Circuit");
        provider.add("metaitem.wafer.nano_central_processing_unit.tooltip", "Raw Nano Circuit");
        provider.add("metaitem.wafer.nor_memory_chip.tooltip", "Raw Logic Gate");
        provider.add("metaitem.wafer.qbit_central_processing_unit.tooltip", "Raw Qubit Circuit");
        provider.add("metaitem.wafer.random_access_memory.tooltip", "Raw Memory");
        provider.add("metaitem.wafer.system_on_chip.tooltip", "Raw Basic Circuit");
        provider.add("metaitem.wafer.simple_system_on_chip.tooltip", "Raw Simple Circuit");
        provider.add("metaitem.engraved.crystal_chip.tooltip", "Needed for Circuits");
        provider.add("metaitem.crystal.raw.tooltip", "Raw Crystal Processor");
        provider.add("metaitem.crystal.raw_chip.tooltip", "Raw Crystal Processor Parts");
        provider.add("metaitem.crystal.central_processing_unit.tooltip", "Crystal Processing Unit");
        provider.add("metaitem.crystal.system_on_chip.tooltip", "Crystal System on Chip");
        provider.add("metaitem.plate.advanced_system_on_chip.tooltip", "Advanced System on Chip");
        provider.add("metaitem.plate.highly_advanced_system_on_chip.tooltip", "Highly Advanced System on Chip");
        provider.add("metaitem.plate.integrated_logic_circuit.tooltip", "Integrated Logic Circuit");
        provider.add("metaitem.plate.central_processing_unit.tooltip", "Central Processing Unit");
        provider.add("metaitem.plate.high_power_integrated_circuit.tooltip", "High Power IC");
        provider.add("metaitem.plate.ultra_high_power_integrated_circuit.tooltip", "Ultra High Power IC");
        provider.add("metaitem.plate.nand_memory_chip.tooltip", "NAND Logic Gate");
        provider.add("metaitem.plate.nano_central_processing_unit.tooltip", "Nano Central Processing Unit");
        provider.add("metaitem.plate.nor_memory_chip.tooltip", "NOR Logic Gate");
        provider.add("metaitem.plate.ultra_low_power_integrated_circuit.tooltip", "Ultra Low Power IC");
        provider.add("metaitem.plate.low_power_integrated_circuit.tooltip", "Low Power IC");
        provider.add("metaitem.plate.power_integrated_circuit.tooltip", "Power IC");
        provider.add("metaitem.plate.qbit_central_processing_unit.tooltip", "Qubit Central Processing Unit");
        provider.add("metaitem.plate.random_access_memory.tooltip", "Random Access Memory");
        provider.add("metaitem.plate.system_on_chip.tooltip", "System on Chip");
        provider.add("metaitem.plate.simple_system_on_chip.tooltip", "Simple System on Chip");
        multilineLang(provider, "metaitem.circuit.electronic.tooltip", "Your First Circuit\n§cLV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.good_electronic.tooltip", "Your Second Circuit\n§cMV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.basic_integrated.tooltip", "Smaller and more powerful\n§6LV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.good_integrated.tooltip", "Smaller and more powerful\n§6MV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.advanced_integrated.tooltip", "Smaller and more powerful\n§6HV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.nand_chip.tooltip", "A Superior Simple Circuit\n§6ULV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.microprocessor.tooltip", "A Superior Basic Circuit\n§eLV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.processor.tooltip", "Amazing Computation Speed!\n§eMV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.assembly.tooltip", "Amazing Computation Speed!\n§eHV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.workstation.tooltip", "Amazing Computation Speed!\n§eEV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.mainframe.tooltip", "Amazing Computation Speed!\n§eIV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.nano_processor.tooltip", "Smaller than ever\n§bHV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.nano_assembly.tooltip", "Smaller than ever\n§bEV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.nano_computer.tooltip", "Smaller than ever\n§bIV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.nano_mainframe.tooltip", "Smaller than ever\n§bLuV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.quantum_processor.tooltip", "Quantum Computing comes to life!\n§aEV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.quantum_assembly.tooltip", "Quantum Computing comes to life!\n§aIV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.quantum_computer.tooltip", "Quantum Computing comes to life!\n§aLuV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.quantum_mainframe.tooltip", "Quantum Computing comes to life!\n§aZPM-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.crystal_processor.tooltip", "Taking Advantage of Crystal Engraving\n§9IV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.crystal_assembly.tooltip", "Taking Advantage of Crystal Engraving\n§9LuV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.crystal_computer.tooltip", "Taking Advantage of Crystal Engraving\n§9ZPM-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.crystal_mainframe.tooltip", "Taking Advantage of Crystal Engraving\n§9UV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.wetware_processor.tooltip", "You have a feeling like it's watching you\n§4LuV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.wetware_assembly.tooltip", "Can run Minecraft\n§4ZPM-tier Circuit");
        multilineLang(provider, "metaitem.circuit.wetware_computer.tooltip", "Ultimate fusion of Flesh and Machine\n§4UV-Tier Circuit");
        multilineLang(provider, "metaitem.circuit.wetware_mainframe.tooltip", "The best Man has ever seen\n§4UHV-Tier Circuit");
        provider.add("metaitem.stem_cells.tooltip", "Raw Intelligence");
        provider.add("metaitem.processor.neuro.tooltip", "Neuro CPU");
        provider.add("metaitem.petri_dish.tooltip", "For cultivating Cells");
        provider.add("metaitem.neutron_reflector.tooltip", "Indestructible");
        provider.add("metaitem.duct_tape.tooltip", "If you can't fix it with this, use more of it!");
        provider.add("metaitem.quantumeye.tooltip", "Improved Ender Eye");
        provider.add("metaitem.quantumstar.tooltip", "Improved Nether Star");
        provider.add("metaitem.gravistar.tooltip", "Ultimate Nether Star");
        multilineLang(provider, "metaitem.item_filter.tooltip", "Filters §fItem§7 I/O as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "metaitem.ore_dictionary_filter.tooltip", "Filters §fItem§7 I/O with §fOre Dictionary§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        multilineLang(provider, "metaitem.fluid_filter.tooltip", "Filters §fFluid§7 I/O as §fCover§7.\nCan be used as an §fElectric Pump§7 and §fFluid Regulator§7 upgrade.");
        multilineLang(provider, "metaitem.smart_item_filter.tooltip", "Filters §fItem§7 I/O with §fMachine Recipes§7 as §fCover§7.\nCan be used as a §fConveyor Module§7 and §fRobotic Arm§7 upgrade.");
        provider.add("metaitem.cover.controller.tooltip", "Turns Machines §fON/OFF§7 as §fCover§7.");
        provider.add("metaitem.cover.activity.detector.tooltip", "Gives out §fActivity Status§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.activity.detector_advanced.tooltip", "Gives out §fMachine Progress§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.fluid.detector.tooltip", "Gives out §fFluid Amount§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.fluid.detector.advanced.tooltip", "Gives §fRS-Latch§7 controlled §fFluid Storage Status§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.item.detector.tooltip", "Gives out §fItem Amount§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.item.detector.advanced.tooltip", "Gives §fRS-Latch§7 controlled §fItem Storage Status§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.energy.detector.tooltip", "Gives out §fEnergy Amount§7 as Redstone as §fCover§7.");
        provider.add("metaitem.cover.energy.detector.advanced.tooltip", "Gives §fRS-Latch§7 controlled §fEnergy Status§7 as Redstone as §fCover§7.");
        multilineLang(provider, "metaitem.cover.fluid.voiding.tooltip", "Voids §fFluids§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "metaitem.cover.fluid.voiding.advanced.tooltip", "Voids §fFluids§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "metaitem.cover.item.voiding.tooltip", "Voids §fItems§7 as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        multilineLang(provider, "metaitem.cover.item.voiding.advanced.tooltip", "Voids §fItems§7 with amount control as §fCover§7.\nActivate with §fSoft Mallet§7 after placement.");
        provider.add("metaitem.cover.facade.tooltip", "Decorative Outfit §fCover§7.");
        provider.add("metaitem.cover.screen.tooltip", "Displays §fData§7 as §fCover§7.");
        provider.add("metaitem.cover.crafting.tooltip", "§fAdvanced Workbench§7 on a Machine as §fCover§7.");
        provider.add("metaitem.cover.shutter.tooltip", "§fBlocks Transfer§7 through attached Side as §fCover§7.");
        multilineLang(provider, "metaitem.cover.solar.panel.tooltip", "May the Sun be with you.\nProduces §fEnergy§7 from the §eSun§7 as §fCover§7.");
        provider.add("metaitem.cover.infinite_water.tooltip.1", "Fills attached containers with §9Water§7 as §fCover§7.");
        provider.add("metaitem.cover.ender_fluid_link.tooltip", "Transports §fFluids§7 with a §fWireless §dEnder§f Connection§7 as §fCover§7.");
        provider.add("metaitem.gelled_toluene.tooltip", "Raw Explosive");
        provider.add("metaitem.bottle.purple.drink.tooltip", "How about Lemonade. Or some Ice Tea? I got Purple Drink!");
        multilineLang(provider, "metaitem.tool_parts_box.tooltip", "Contains some tool parts\nRight click to open");
        multilineLang(provider, "metaitem.foam_sprayer.tooltip", "Sprays Construction Foam\nUse on a frame to foam connected frames\nFoam can be colored");
        provider.add("metaitem.brick.fireclay.tooltip", "Heat resistant");
        provider.add("item.gt.tool.replace_tool_head", "Craft with a new Tool Head to replace it");
        provider.add("item.gt.tool.usable_as", "Usable as: §f%s");
        provider.add("item.gt.tool.behavior.silk_ice", "§bIce Cutter: §fSilk Harvests Ice");
        provider.add("item.gt.tool.behavior.torch_place", "§eSpelunker: §fPlaces Torches on Right-Click");
        provider.add("item.gt.tool.behavior.tree_felling", "§4Lumberjack: §fTree Felling");
        provider.add("item.gt.tool.behavior.shield_disable", "§cBrute: §fDisables Shields");
        provider.add("item.gt.tool.behavior.relocate_mining", "§2Magnetic: §fRelocates Mined Blocks");
        provider.add("item.gt.tool.behavior.aoe_mining", "§5Area-of-Effect: §f%sx%sx%s");
        provider.add("item.gt.tool.behavior.ground_tilling", "§eFarmer: §fTills Ground");
        provider.add("item.gt.tool.behavior.grass_path", "§eLandscaper: §fCreates Grass Paths");
        provider.add("item.gt.tool.behavior.rail_rotation", "§eRailroad Engineer: §fRotates Rails");
        provider.add("item.gt.tool.behavior.crop_harvesting", "§aHarvester: §fHarvests Crops");
        provider.add("item.gt.tool.behavior.plunger", "§9Plumber: §fDrains Fluids");
        provider.add("item.gt.tool.behavior.block_rotation", "§2Mechanic: §fRotates Blocks");
        provider.add("item.gt.tool.behavior.damage_boost", "§4Damage Boost: §fExtra damage against %s");
        provider.add("item.gt.tool.sword.name", "%s Sword");
        provider.add("item.gt.tool.pickaxe.name", "%s Pickaxe");
        provider.add("item.gt.tool.shovel.name", "%s Shovel");
        provider.add("item.gt.tool.axe.name", "%s Axe");
        provider.add("item.gt.tool.hoe.name", "%s Hoe");
        provider.add("item.gt.tool.saw.name", "%s Saw");
        provider.add("item.gt.tool.hammer.name", "%s Hammer");
        provider.add("item.gt.tool.hammer.tooltip", "§8Crushes Blocks when harvesting them");
        provider.add("item.gt.tool.mallet.name", "%s Soft Mallet");
        provider.add("item.gt.tool.mallet.tooltip", "§8Stops/Starts Machinery");
        provider.add("item.gt.tool.wrench.name", "%s Wrench");
        provider.add("item.gt.tool.wrench.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gt.tool.file.name", "%s File");
        provider.add("item.gt.tool.crowbar.name", "%s Crowbar");
        provider.add("item.gt.tool.crowbar.tooltip", "§8Dismounts Covers");
        provider.add("item.gt.tool.screwdriver.name", "%s Screwdriver");
        provider.add("item.gt.tool.screwdriver.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gt.tool.mortar.name", "%s Mortar");
        provider.add("item.gt.tool.wire_cutter.name", "%s Wire Cutter");
        provider.add("item.gt.tool.knife.name", "%s Knife");
        provider.add("item.gt.tool.butchery_knife.name", "%s Butchery Knife");
        provider.add("item.gt.tool.butchery_knife.tooltip", "§8Has a slow Attack Rate");
        provider.add("item.gt.tool.scythe.name", "%s Scythe");
        provider.add("item.gt.tool.scythe.tooltip", "§8Because a Scythe doesn't make Sense");
        provider.add("item.gt.tool.rolling_pin.name", "%s Rolling Pin");
        provider.add("item.gt.tool.drill_lv.name", "%s Drill (LV)");
        provider.add("item.gt.tool.drill_mv.name", "%s Drill (MV)");
        provider.add("item.gt.tool.drill_hv.name", "%s Drill (HV)");
        provider.add("item.gt.tool.drill_ev.name", "%s Drill (EV)");
        provider.add("item.gt.tool.drill_iv.name", "%s Drill (IV)");
        provider.add("item.gt.tool.mining_hammer.name", "%s Mining Hammer");
        provider.add("item.gt.tool.mining_hammer.tooltip", "§8Mines a large area at once (unless you're crouching)");
        provider.add("item.gt.tool.spade.name", "%s Spade");
        provider.add("item.gt.tool.spade.tooltip", "§8Mines a large area at once (unless you're crouching)");
        provider.add("item.gt.tool.chainsaw_lv.name", "%s Chainsaw (LV)");
        provider.add("item.gt.tool.chainsaw_mv.name", "%s Chainsaw (MV)");
        provider.add("item.gt.tool.chainsaw_hv.name", "%s Chainsaw (HV)");
        provider.add("item.gt.tool.wrench_lv.name", "%s Wrench (LV)");
        provider.add("item.gt.tool.wrench_lv.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gt.tool.wrench_hv.name", "%s Wrench (HV)");
        provider.add("item.gt.tool.wrench_hv.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gt.tool.wrench_iv.name", "%s Wrench (IV)");
        provider.add("item.gt.tool.wrench_iv.tooltip", "§8Hold left click to dismantle Machines");
        provider.add("item.gt.tool.buzzsaw.name", "%s Buzzsaw (LV)");
        provider.add("item.gt.tool.buzzsaw.tooltip", "§8Not suitable for harvesting Blocks");
        provider.add("item.gt.tool.screwdriver_lv.name", "%s Screwdriver (LV)");
        provider.add("item.gt.tool.screwdriver_lv.tooltip", "§8Adjusts Covers and Machines");
        provider.add("item.gt.tool.plunger.name", "%s Plunger");
        provider.add("item.gt.tool.plunger.tooltip", "§8Removes Fluids from Machines");
        provider.add("item.gt.tool.tooltip.crafting_uses", "§a%s Crafting Uses");
        provider.add("item.gt.tool.tooltip.general_uses", "§b%s Durability");
        provider.add("item.gt.tool.tooltip.attack_damage", "§c%s Attack Damage");
        provider.add("item.gt.tool.tooltip.attack_speed", "§9%s Attack Speed");
        provider.add("item.gt.tool.tooltip.mining_speed", "§d%s Mining Speed");
        provider.add("item.gt.tool.tooltip.harvest_level", "§eHarvest Level %s");
        provider.add("item.gt.tool.tooltip.harvest_level_extra", "§eHarvest Level %s §f(%s§f)");
        multiLang(provider, "item.gt.tool.harvest_level", "§8Wood", "§7Stone", "§aIron", "§bDiamond", "§dUltimet", "§9Duranium", "§cNeutronium");
        provider.add("item.gt.tool.tooltip.repair_info", "Hold SHIFT to show Repair Info");
        provider.add("item.gt.tool.tooltip.repair_material", "Repair with: §a%s");
        provider.add("item.gt.tool.aoe.rows", "Rows");
        provider.add("item.gt.tool.aoe.columns", "Columns");
        provider.add("item.gt.tool.aoe.layers", "Layers");
        provider.add("metaitem.turbine_rotor.tooltip", "Turbine Rotors for your power station");
        provider.add("metaitem.clipboard.tooltip", "Can be written on (without any writing Instrument). Right-click on Wall to place, and Shift-Right-Click to remove");
        provider.add("metaitem.behavior.mode_switch.tooltip", "Use while sneaking to switch mode");
        provider.add("metaitem.behavior.mode_switch.mode_switched", "§eMode Set to: %s");
        provider.add("metaitem.behavior.mode_switch.current_mode", "Mode: %s");
        provider.add("metaitem.tool.tooltip.primary_material", "§fMaterial: §e%s");
        provider.add("metaitem.tool.tooltip.durability", "§fDurability: §a%d / %d");
        provider.add("metaitem.tool.tooltip.rotor.efficiency", "Turbine Efficiency: §9%d%%");
        provider.add("metaitem.tool.tooltip.rotor.power", "Turbine Power: §9%d%%");
        provider.add("metaitem.voltage_coil.ulv.tooltip", "Primitive Coil");
        provider.add("metaitem.voltage_coil.lv.tooltip", "Basic Coil");
        provider.add("metaitem.voltage_coil.mv.tooltip", "Good Coil");
        provider.add("metaitem.voltage_coil.hv.tooltip", "Advanced Coil");
        provider.add("metaitem.voltage_coil.ev.tooltip", "Extreme Coil");
        provider.add("metaitem.voltage_coil.iv.tooltip", "Elite Coil");
        provider.add("metaitem.voltage_coil.luv.tooltip", "Master Coil");
        provider.add("metaitem.voltage_coil.zpm.tooltip", "Super Coil");
        provider.add("metaitem.voltage_coil.uv.tooltip", "Ultimate Coil");
        provider.add("metaitem.voltage_coil.uhv.tooltip", "Ultra Coil");
        provider.add("metaitem.voltage_coil.uev.tooltip", "Unreal Coil");
        provider.add("metaitem.voltage_coil.uiv.tooltip", "Insane Coil");
        provider.add("metaitem.voltage_coil.uxv.tooltip", "Epic Coil");
        provider.add("metaitem.voltage_coil.opv.tooltip", "Legendary Coil");
        provider.add("metaitem.voltage_coil.max.tooltip", "Maximum Coil");
        provider.add("metaitem.liquid_fuel_jetpack.tooltip", "Uses Combustion Generator Fuels for Thrust");
        provider.add("metaarmor.nms.nightvision.enabled", "NanoMuscle™ Suite: NightVision Enabled");
        provider.add("metaarmor.nms.nightvision.disabled", "NanoMuscle™ Suite: NightVision Disabled");
        provider.add("metaarmor.nms.nightvision.error", "NanoMuscle™ Suite: §cNot enough power!");
        provider.add("metaarmor.qts.nightvision.enabled", "QuarkTech™ Suite: NightVision Enabled");
        provider.add("metaarmor.qts.nightvision.disabled", "QuarkTech™ Suite: NightVision Disabled");
        provider.add("metaarmor.qts.nightvision.error", "QuarkTech™ Suite: §cNot enough power!");
        provider.add("metaarmor.jetpack.hover.enable", "Jetpack: Hover Mode Enabled");
        provider.add("metaarmor.jetpack.hover.disable", "Jetpack: Hover Mode Disabled");
        provider.add("metaarmor.jetpack.emergency_hover_mode", "Emergency Hover Mode Enabled!");
        provider.add("metaarmor.nms.share.enable", "NanoMuscle™ Suite: Charging Enabled");
        provider.add("metaarmor.nms.share.disable", "NanoMuscle™ Suite: Charging Disabled");
        provider.add("metaarmor.nms.share.error", "NanoMuscle™ Suite: §cNot enough power for charging!");
        provider.add("metaarmor.qts.share.enable", "QuarkTech™ Suite: Charging Enabled");
        provider.add("metaarmor.qts.share.disable", "QuarkTech™ Suite: Charging Disabled");
        provider.add("metaarmor.qts.share.error", "QuarkTech™ Suite: §cNot enough power for charging!");
        provider.add("metaarmor.message.nightvision.enabled", "§bNightVision: §aOn");
        provider.add("metaarmor.message.nightvision.disabled", "§bNightVision: §cOff");
        provider.add("metaarmor.message.nightvision.error", "§cNot enough power!");
        provider.add("metaarmor.tooltip.stepassist", "Provides Step-Assist");
        provider.add("metaarmor.tooltip.speed", "Increases Running Speed");
        provider.add("metaarmor.tooltip.jump", "Increases Jump Height and Distance");
        provider.add("metaarmor.tooltip.falldamage", "Nullifies Fall Damage");
        provider.add("metaarmor.tooltip.potions", "Nullifies Harmful Effects");
        provider.add("metaarmor.tooltip.burning", "Nullifies Burning");
        provider.add("metaarmor.tooltip.breath", "Replenishes Underwater Breath Bar");
        provider.add("metaarmor.tooltip.autoeat", "Replenishes Food Bar by Using Food from Inventory");
        provider.add("metaarmor.hud.status.enabled", "§aON");
        provider.add("metaarmor.hud.status.disabled", "§cOFF");
        provider.add("metaarmor.hud.energy_lvl", "Energy Level: %s");
        provider.add("metaarmor.hud.fuel_lvl", "Fuel Level: %s");
        provider.add("metaarmor.hud.hover_mode", "Hover Mode: %s");
        provider.add("mataarmor.hud.supply_mode", "Supply Mode: %s");
        provider.add("metaarmor.hud.gravi_engine", "GraviEngine: %s");
        provider.add("metaarmor.energy_share.error", "Energy Supply: §cNot enough power for gadgets charging!");
        provider.add("metaarmor.energy_share.enable", "Energy Supply: Gadgets charging enabled");
        provider.add("metaarmor.energy_share.disable", "Energy Supply: Gadgets charging disabled");
        provider.add("metaarmor.energy_share.tooltip", "Supply mode: %s");
        provider.add("metaarmor.energy_share.tooltip.guide", "To change mode shift-right click when holding item");
        provider.add("metaitem.record.sus.tooltip", "§7Leonz - Among Us Drip");
        provider.add("metaitem.nan.certificate.tooltip", "Challenge Accepted!");
        provider.add("metaitem.blacklight.tooltip", "Long-Wave §dUltraviolet§7 light source");
        provider.add("gui.widget.incrementButton.default_tooltip", "Hold Shift, Ctrl or both to change the amount");
        provider.add("gui.widget.recipeProgressWidget.default_tooltip", "Show Recipes");
        multilineLang(provider, "gtceu.recipe_memory_widget.tooltip", "§7Left click to automatically input this recipe into the crafting grid\n§7Shift click to lock/unlock this recipe");
        provider.add("cover.filter.blacklist.disabled", "Whitelist");
        provider.add("cover.filter.blacklist.enabled", "Blacklist");
        provider.add("cover.ore_dictionary_filter.title", "Ore Dictionary Filter");
        multilineLang(provider, "cover.ore_dictionary_filter.info", "§bAccepts complex expressions\n& = AND\n| = OR\n^ = XOR\n! = NOT\n( ) for priority\n* for wildcard\n§bExample:\n§6dust*Gold | (plate* & !*Double*)\nWill match all gold dusts of all sizes or all plates, but not double plates");
        provider.add("cover.ore_dictionary_filter.test_slot.info", "Insert a item to test if it matches the filter expression");
        provider.add("cover.ore_dictionary_filter.matches", "Item matches");
        provider.add("cover.ore_dictionary_filter.matches_not", "Item does not match");
        provider.add("cover.fluid_filter.title", "Fluid Filter");
        multilineLang(provider, "cover.fluid_filter.config_amount", "Scroll wheel up increases amount, down decreases.\nShift[§6x10§r],Ctrl[§ex100§r],Shift+Ctrl[§ax1000§r]\nRight click increases amount, left click decreases.\nHold shift to double/halve.\nMiddle click to clear");
        provider.add("cover.fluid_filter.mode.filter_fill", "Filter Fill");
        provider.add("cover.fluid_filter.mode.filter_drain", "Filter Drain");
        provider.add("cover.fluid_filter.mode.filter_both", "Filter Fill & Drain");
        provider.add("cover.item_filter.title", "Item Filter");
        provider.add("cover.filter.mode.filter_insert", "Filter Insert");
        provider.add("cover.filter.mode.filter_extract", "Filter Extract");
        provider.add("cover.filter.mode.filter_both", "Filter Insert/Extract");
        provider.add("cover.item_filter.ignore_damage.enabled", "Ignore Damage");
        provider.add("cover.item_filter.ignore_damage.disabled", "Respect Damage");
        provider.add("cover.item_filter.ignore_nbt.enabled", "Ignore NBT");
        provider.add("cover.item_filter.ignore_nbt.disabled", "Respect NBT");
        provider.add("cover.voiding.voiding_mode.void_any", "Void Matching");
        provider.add("cover.voiding.voiding_mode.void_overflow", "Void Overflow");
        multilineLang(provider, "cover.voiding.voiding_mode.description", "§eVoid Matching§r will void anything matching the filter. \n§eVoid Overflow§r will void anything matching the filter, up to the specified amount.");
        provider.add("cover.fluid.voiding.title", "Fluid Voiding Settings");
        provider.add("cover.fluid.voiding.advanced.title", "Advanced Fluid Voiding Settings");
        provider.add("cover.item.voiding.title", "Item Voiding Settings");
        provider.add("cover.item.voiding.advanced.title", "Advanced Item Voiding Settings");
        provider.add("cover.voiding.label.disabled", "Disabled");
        provider.add("cover.voiding.label.enabled", "Enabled");
        provider.add("cover.voiding.tooltip", "§cWARNING!§7 Setting this to \"Enabled\" means that fluids or items WILL be voided.");
        provider.add("cover.voiding.message.disabled", "Voiding Cover Disabled");
        provider.add("cover.voiding.message.enabled", "Voiding Cover Enabled");
        provider.add("cover.smart_item_filter.title", "Smart Item Filter");
        provider.add("cover.smart_item_filter.filtering_mode.electrolyzer", "Electrolyzer");
        provider.add("cover.smart_item_filter.filtering_mode.centrifuge", "Centrifuge");
        provider.add("cover.smart_item_filter.filtering_mode.sifter", "Sifter");
        multilineLang(provider, "cover.smart_item_filter.filtering_mode.description", "Select Machine this Smart Filter will use for filtering.\nIt will automatically pick right portions of items for robotic arm.");
        provider.add("cover.conveyor.title", "Conveyor Cover Settings (%s)");
        provider.add("cover.conveyor.transfer_rate", "§7items/sec");
        provider.add("cover.conveyor.mode.export", "Mode: Export");
        provider.add("cover.conveyor.mode.import", "Mode: Import");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_enhanced", "Distribution Mode\n§bEnhanced Round Robin§r\n§7Splits items equally to all inventories");
        multilineLang(provider, "cover.conveyor.distribution.round_robin", "Distribution Mode\n§bRound Robin§r with Priority\n§7Tries to split items equally to inventories");
        multilineLang(provider, "cover.conveyor.distribution.first_insert", "Distribution Mode\n§bFirst Insert§r\n§7Will insert into the first inventory it finds");
        multilineLang(provider, "cover.conveyor.blocks_input.enabled", "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§aEnabled");
        multilineLang(provider, "cover.conveyor.blocks_input.disabled", "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§cDisabled");
        provider.add("cover.universal.manual_import_export.mode.disabled", "Manual I/O: Disabled");
        provider.add("cover.universal.manual_import_export.mode.filtered", "Manual I/O: Filtered");
        provider.add("cover.universal.manual_import_export.mode.unfiltered", "Manual I/O: Unfiltered");
        multilineLang(provider, "cover.universal.manual_import_export.mode.description", "§eDisabled§r - Items/fluids will only move as specified by the cover and its filter. \n§eAllow Filtered§r - Items/fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any). \n§eAllow Unfiltered§r - Items/fluids can be moved independently of the cover mode. Filter applies to the items inserted or extracted by this cover");
        provider.add("cover.conveyor.item_filter.title", "Item Filter");
        multiLang(provider, "cover.conveyor.ore_dictionary.title", "Ore Dictionary Name", "(use * for wildcard)");
        provider.add("cover.robotic_arm.title", "Robotic Arm Settings (%s)");
        provider.add("cover.robotic_arm.transfer_mode.transfer_any", "Transfer Any");
        provider.add("cover.robotic_arm.transfer_mode.transfer_exact", "Supply Exact");
        provider.add("cover.robotic_arm.transfer_mode.keep_exact", "Keep Exact");
        multilineLang(provider, "cover.robotic_arm.transfer_mode.description", "§eTransfer Any§r - in this mode, cover will transfer as many items matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply items in portions specified in item filter slots (or variable under this button for ore dictionary filter). If amount of items is less than portion size, items won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of items in the destination inventory, supplying additional amount of items if required.\n§7Tip: left/right click on filter slots to change item amount,  use shift clicking to change amount faster.");
        provider.add("cover.pump.title", "Pump Cover Settings (%s)");
        provider.add("cover.pump.transfer_rate", "%s");
        provider.add("cover.pump.mode.export", "Mode: Export");
        provider.add("cover.pump.mode.import", "Mode: Import");
        provider.add("cover.pump.fluid_filter.title", "Fluid Filter");
        provider.add("cover.bucket.mode.bucket", "kL/s");
        provider.add("cover.bucket.mode.milli_bucket", "L/s");
        provider.add("cover.fluid_regulator.title", "Fluid Regulator Settings (%s)");
        multilineLang(provider, "cover.fluid_regulator.transfer_mode.description", "§eTransfer Any§r - in this mode, cover will transfer as many fluids matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply fluids in portions specified in the window underneath this button. If amount of fluids is less than portion size, fluids won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of fluids in the destination inventory, supplying additional amount of fluids if required.\n§7Tip: shift click will multiply increase/decrease amounts by 10 and ctrl click will multiply by 100.");
        provider.add("cover.fluid_regulator.supply_exact", "Supply Exact: %s");
        provider.add("cover.fluid_regulator.keep_exact", "Keep Exact: %s");
        provider.add("cover.machine_controller.title", "Machine Controller Settings");
        provider.add("cover.machine_controller.normal", "Normal");
        provider.add("cover.machine_controller.inverted", "Inverted");
        multilineLang(provider, "cover.machine_controller.inverted.description", "§eNormal§r - in this mode, the cover will require a signal weaker than the set redstone level to run\n§eInverted§r - in this mode, the cover will require a signal stronger than the set redstone level to run");
        provider.add("cover.machine_controller.redstone", "Min Redstone Strength: %d");
        provider.add("cover.machine_controller.mode.machine", "Control Machine");
        provider.add("cover.machine_controller.mode.cover_up", "Control Cover (Top)");
        provider.add("cover.machine_controller.mode.cover_down", "Control Cover (Bottom)");
        provider.add("cover.machine_controller.mode.cover_south", "Control Cover (South)");
        provider.add("cover.machine_controller.mode.cover_north", "Control Cover (North)");
        provider.add("cover.machine_controller.mode.cover_east", "Control Cover (East)");
        provider.add("cover.machine_controller.mode.cover_west", "Control Cover (West)");
        provider.add("cover.ender_fluid_link.title", "Ender Fluid Link");
        provider.add("cover.ender_fluid_link.iomode.enabled", "I/O Enabled");
        provider.add("cover.ender_fluid_link.iomode.disabled", "I/O Disabled");
        multilineLang(provider, "cover.ender_fluid_link.private.tooltip.disabled", "Switch to private tank mode\nPrivate mode uses the player who originally placed the cover");
        provider.add("cover.ender_fluid_link.private.tooltip.enabled", "Switch to public tank mode");
        multilineLang(provider, "cover.ender_fluid_link.incomplete_hex", "Inputted color is incomplete!\nIt will be applied once complete (all 8 hex digits)\nClosing the gui will lose edits!");
        provider.add("cover.advanced_energy_detector.label", "Advanced Energy Detector");
        provider.add("cover.advanced_energy_detector.min", "Minimum EU");
        provider.add("cover.advanced_energy_detector.max", "Maximum EU");
        multilineLang(provider, "cover.advanced_energy_detector.invert_tooltip", "Toggle to invert the redstone logic\nBy default, redstone is emitted when less than the minimum EU, and stops emitting when greater than the max EU");
        provider.add("cover.advanced_energy_detector.invert_label", "Inverted:");
        provider.add("cover.advanced_energy_detector.normal", "Normal");
        provider.add("cover.advanced_energy_detector.inverted", "Inverted");
        provider.add("cover.advanced_fluid_detector.label", "Advanced Fluid Detector");
        multilineLang(provider, "cover.advanced_fluid_detector.invert_tooltip", "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum L of fluid, and starts emitting when greater than the min L of fluid up to the set maximum");
        provider.add("cover.advanced_fluid_detector.max", "Maximum Fluid:");
        provider.add("cover.advanced_fluid_detector.min", "Minimum Fluid:");
        provider.add("cover.advanced_item_detector.label", "Advanced Item Detector");
        multilineLang(provider, "cover.advanced_item_detector.invert_tooltip", "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum amount of items, and starts emitting when greater than the min amount of items up to the set maximum");
        provider.add("cover.advanced_item_detector.max", "Maximum Items:");
        provider.add("cover.advanced_item_detector.min", "Minimum Items:");
        provider.add("item.nether_quartz.oreNetherrack", "Nether Quartz Ore");
        provider.add("item.gunpowder.dustTiny", "Tiny Pile of Gunpowder");
        provider.add("item.gunpowder.dustSmall", "Small Pile of Gunpowder");
        provider.add("item.paper.dustTiny", "Tiny Pile of Chad");
        provider.add("item.paper.dustSmall", "Small Pile of Chad");
        provider.add("item.paper.dust", "Chad");
        provider.add("item.rare_earth.dustTiny", "Tiny Pile of Rare Earth");
        provider.add("item.rare_earth.dustSmall", "Small Pile of Rare Earth");
        provider.add("item.rare_earth.dust", "Rare Earth");
        provider.add("item.ash.dustTiny", "Tiny Pile of Ashes");
        provider.add("item.ash.dustSmall", "Small Pile of Ashes");
        provider.add("item.ash.dust", "Ashes");
        provider.add("item.bone.dustTiny", "Tiny Pile of Bone Meal");
        provider.add("item.bone.dustSmall", "Small Pile of Bone Meal");
        provider.add("item.bone.dust", "Bone Meal");
        provider.add("item.cassiterite_sand.crushedRefined", "Refined Cassiterite Sand");
        provider.add("item.cassiterite_sand.crushedPurified", "Purified Cassiterite Sand");
        provider.add("item.cassiterite_sand.crushed", "Ground Cassiterite Sand");
        provider.add("item.cassiterite_sand.dustTiny", "Tiny Pile of Cassiterite Sand");
        provider.add("item.cassiterite_sand.dustSmall", "Small Pile of Cassiterite Sand");
        provider.add("item.cassiterite_sand.dustImpure", "Impure Pile of Cassiterite Sand");
        provider.add("item.cassiterite_sand.dustPure", "Purified Pile of Cassiterite Sand");
        provider.add("item.cassiterite_sand.dust", "Cassiterite Sand");
        provider.add("item.dark_ash.dustTiny", "Tiny Pile of Dark Ashes");
        provider.add("item.dark_ash.dustSmall", "Small Pile of Dark Ashes");
        provider.add("item.dark_ash.dust", "Dark Ashes");
        provider.add("item.ice.dustTiny", "Tiny Pile of Crushed Ice");
        provider.add("item.ice.dustSmall", "Small Pile of Crushed Ice");
        provider.add("item.ice.dust", "Crushed Ice");
        provider.add("item.sugar.gem", "Sugar Cube");
        provider.add("item.sugar.gemChipped", "Small Sugar Cubes");
        provider.add("item.sugar.gemFlawed", "Tiny Sugar Cube");
        provider.add("item.rock_salt.dustTiny", "Tiny Pile of Rock Salt");
        provider.add("item.rock_salt.dustSmall", "Small Pile of Rock Salt");
        provider.add("item.rock_salt.dustImpure", "Impure Pile of Rock Salt");
        provider.add("item.rock_salt.dustPure", "Purified Pile of Rock Salt");
        provider.add("item.rock_salt.dust", "Rock Salt");
        provider.add("item.salt.dustTiny", "Tiny Pile of Salt");
        provider.add("item.salt.dustSmall", "Small Pile of Salt");
        provider.add("item.salt.dustImpure", "Impure Pile of Salt");
        provider.add("item.salt.dustPure", "Purified Pile of Salt");
        provider.add("item.salt.dust", "Salt");
        provider.add("item.wood.dustTiny", "Tiny Pile of Wood Pulp");
        provider.add("item.wood.dustSmall", "Small Pile of Wood Pulp");
        provider.add("item.wood.dust", "Wood Pulp");
        provider.add("item.wood.plate", "Wood Plank");
        provider.add("item.wood.rodLong", "Long Wood Stick");
        provider.add("item.wood.bolt", "Short Wood Stick");
        provider.add("item.treated_wood.dustTiny", "Tiny Pile of Treated Wood Pulp");
        provider.add("item.treated_wood.dustSmall", "Small Pile of Treated Wood Pulp");
        provider.add("item.treated_wood.dust", "Treated Wood Pulp");
        provider.add("item.treated_wood.plate", "Treated Wood Plank");
        provider.add("item.treated_wood.rod", "Treated Wood Stick");
        provider.add("item.treated_wood.rodLong", "Long Treated Wood Stick");
        provider.add("item.treated_wood.bolt", "Short Treated Wood Stick");
        provider.add("item.glass.gem", "Glass Crystal");
        provider.add("item.glass.gemChipped", "Chipped Glass Crystal");
        provider.add("item.glass.gemFlawed", "Flawed Glass Crystal");
        provider.add("item.glass.gemFlawless", "Flawless Glass Crystal");
        provider.add("item.glass.gemExquisite", "Exquisite Glass Crystal");
        provider.add("item.glass.plate", "Glass Pane");
        provider.add("item.blaze.dustTiny", "Tiny Pile of Blaze Powder");
        provider.add("item.blaze.dustSmall", "Small Pile of Blaze Powder");
        provider.add("item.sugar.dustTiny", "Tiny Pile of Sugar");
        provider.add("item.sugar.dustSmall", "Small Pile of Sugar");
        provider.add("item.basaltic_mineral_sand.dustTiny", "Tiny Pile of Basaltic Mineral Sand");
        provider.add("item.basaltic_mineral_sand.dustSmall", "Small Pile of Basaltic Mineral Sand");
        provider.add("item.basaltic_mineral_sand.dust", "Basaltic Mineral Sand");
        provider.add("item.granitic_mineral_sand.dustTiny", "Tiny Pile of Granitic Mineral Sand");
        provider.add("item.granitic_mineral_sand.dustSmall", "Small Pile of Granitic Mineral Sand");
        provider.add("item.granitic_mineral_sand.dust", "Granitic Mineral Sand");
        provider.add("item.garnet_sand.dustTiny", "Tiny Pile of Garnet Sand");
        provider.add("item.garnet_sand.dustSmall", "Small Pile of Garnet Sand");
        provider.add("item.garnet_sand.dust", "Garnet Sand");
        provider.add("item.quartz_sand.dustTiny", "Tiny Pile of Quartz Sand");
        provider.add("item.quartz_sand.dustSmall", "Small Pile of Quartz Sand");
        provider.add("item.quartz_sand.dust", "Quartz Sand");
        provider.add("item.glauconite_sand.dustTiny", "Tiny Pile of Glauconite Sand");
        provider.add("item.glauconite_sand.dustSmall", "Small Pile of Glauconite Sand");
        provider.add("item.glauconite_sand.dust", "Glauconite Sand");
        provider.add("item.bentonite.crushedRefined", "Refined Bentonite");
        provider.add("item.bentonite.crushedPurified", "Purified Bentonite");
        provider.add("item.bentonite.crushed", "Ground Bentonite");
        provider.add("item.bentonite.dustTiny", "Tiny Pile of Bentonite");
        provider.add("item.bentonite.dustSmall", "Small Pile of Bentonite");
        provider.add("item.bentonite.dustImpure", "Impure Pile of Bentonite");
        provider.add("item.bentonite.dustPure", "Purified Pile of Bentonite");
        provider.add("item.bentonite.dust", "Bentonite");
        provider.add("item.fullers_earth.dustTiny", "Tiny Pile of Fullers Earth");
        provider.add("item.fullers_earth.dustSmall", "Small Pile of Fullers Earth");
        provider.add("item.fullers_earth.dust", "Fullers Earth");
        provider.add("item.pitchblende.crushedRefined", "Refined Pitchblende");
        provider.add("item.pitchblende.crushedPurified", "Purified Pitchblende");
        provider.add("item.pitchblende.crushed", "Ground Pitchblende");
        provider.add("item.pitchblende.dustTiny", "Tiny Pile of Pitchblende");
        provider.add("item.pitchblende.dustSmall", "Small Pile of Pitchblende");
        provider.add("item.pitchblende.dustImpure", "Impure Pile of Pitchblende");
        provider.add("item.pitchblende.dustPure", "Purified Pile of Pitchblende");
        provider.add("item.pitchblende.dust", "Pitchblende");
        provider.add("item.talc.crushedRefined", "Refined Talc");
        provider.add("item.talc.crushedPurified", "Purified Talc");
        provider.add("item.talc.crushed", "Ground Talc");
        provider.add("item.talc.dustTiny", "Tiny Pile of Talc");
        provider.add("item.talc.dustSmall", "Small Pile of Talc");
        provider.add("item.talc.dustImpure", "Impure Pile of Talc");
        provider.add("item.talc.dustPure", "Purified Pile of Talc");
        provider.add("item.talc.dust", "Talc");
        provider.add("item.wheat.dustTiny", "Tiny Pile of Flour");
        provider.add("item.wheat.dustSmall", "Small Pile of Flour");
        provider.add("item.wheat.dust", "Flour");
        provider.add("item.meat.dustTiny", "Tiny Pile of Mince Meat");
        provider.add("item.meat.dustSmall", "Small Pile of Mince Meat");
        provider.add("item.meat.dust", "Mince Meat");
        provider.add("item.borosilicate_glass.ingot", "Borosilicate Glass Bar");
        provider.add("item.borosilicate_glass.wireFine", "Borosilicate Glass Fibers");
        provider.add("item.platinum_group_sludge.dustTiny", "Tiny Clump of Platinum Group Sludge");
        provider.add("item.platinum_group_sludge.dustSmall", "Small Clump of Platinum Group Sludge");
        provider.add("item.platinum_group_sludge.dust", "Platinum Group Sludge");
        provider.add("item.platinum_raw.dustTiny", "Tiny Pile of Raw Platinum Powder");
        provider.add("item.platinum_raw.dustSmall", "Small Pile of Raw Platinum Powder");
        provider.add("item.platinum_raw.dust", "Raw Platinum Powder");
        provider.add("item.palladium_raw.dustTiny", "Tiny Pile of Raw Palladium Powder");
        provider.add("item.palladium_raw.dustSmall", "Small Pile of Raw Palladium Powder");
        provider.add("item.palladium_raw.dust", "Raw Palladium Powder");
        provider.add("item.inert_metal_mixture.dustTiny", "Tiny Pile of Inert Metal Mixture");
        provider.add("item.inert_metal_mixture.dustSmall", "Small Pile of Inert Metal Mixture");
        provider.add("item.inert_metal_mixture.dust", "Inert Metal Mixture");
        provider.add("item.rarest_metal_mixture.dustTiny", "Tiny Pile of Rarest Metal Mixture");
        provider.add("item.rarest_metal_mixture.dustSmall", "Small Pile of Rarest Metal Mixture");
        provider.add("item.rarest_metal_mixture.dust", "Rarest Metal Mixture");
        provider.add("item.platinum_sludge_residue.dustTiny", "Tiny Pile of Platinum Sludge Residue");
        provider.add("item.platinum_sludge_residue.dustSmall", "Small Pile of Platinum Sludge Residue");
        provider.add("item.platinum_sludge_residue.dust", "Platinum Sludge Residue");
        provider.add("item.iridium_metal_residue.dustTiny", "Tiny Pile of Iridium Metal Residue");
        provider.add("item.iridium_metal_residue.dustSmall", "Small Pile of Iridium Metal Residue");
        provider.add("item.iridium_metal_residue.dust", "Iridium Metal Residue");
        provider.add("behaviour.hoe", "Can till dirt");
        provider.add("behaviour.soft_hammer", "Activates and Deactivates Machines");
        provider.add("behaviour.soft_hammer.enabled", "Working Enabled");
        provider.add("behaviour.soft_hammer.disabled", "Working Disabled");
        provider.add("behaviour.lighter.tooltip", "Can light things on fire");
        provider.add("behaviour.lighter.fluid.tooltip", "Can light things on fire with Butane or Propane");
        provider.add("behaviour.lighter.uses", "Remaining uses: %d");
        provider.add("behavior.toggle_energy_consumer.tooltip", "Use to toggle mode");
        provider.add("enchantment.damage.disjunction", "Disjunction");
        provider.add("enchantment.gtceu.disjunction.desc", "Applies Weakness and Slowness to Ender-related mobs.");
        provider.add("enchantment.hard_hammer", "Hammering");
        provider.add("enchantment.gtceu.hard_hammer.desc", "Breaks blocks as if they were mined with a GregTech Hammer.");
        provider.add("tile.gt.seal.name", "Sealed Block");
        provider.add("tile.gt.foam.name", "Foam");
        provider.add("tile.gt.reinforced_foam.name", "Reinforced Foam");
        provider.add("tile.gt.petrified_foam.name", "Petrified Foam");
        provider.add("tile.gt.reinforced_stone.name", "Reinforced Stone");
        provider.add("tile.rubber_log.name", "Rubber Wood");
        provider.add("tile.rubber_leaves.name", "Rubber Tree Leaves");
        provider.add("tile.rubber_sapling.name", "Rubber Tree Sapling");
        provider.add("tile.planks.rubber.name", "Rubber Wood Planks");
        provider.add("tile.planks.treated.name", "Treated Wood Planks");
        provider.add("tile.brittle_charcoal.name", "Brittle Charcoal");
        multilineLang(provider, "tile.brittle_charcoal.tooltip", "Produced by the Charcoal Pile Igniter.\nMine this to get Charcoal.");
        provider.add("metaitem.prospector.mode.ores", "§aOre Prospection Mode");
        provider.add("metaitem.prospector.mode.fluid", "§bFluid Prospection Mode");
        provider.add("metaitem.prospector.tooltip.ores", "Scans Ores in a %s Chunk Radius");
        provider.add("metaitem.prospector.tooltip.fluids", "Scans Ores and Fluids in a %s Chunk Radius");
        provider.add("behavior.prospector.not_enough_energy", "Not Enough Energy!");
        provider.add("metaitem.tricorder_scanner.tooltip", "Tricorder");
        provider.add("metaitem.debug_scanner.tooltip", "Tricorder");
        provider.add("behavior.tricorder.position", "----- X: %s Y: %s Z: %s D: %s -----");
        provider.add("behavior.tricorder.block_hardness", "Hardness: %s Blast Resistance: %s");
        provider.add("behavior.tricorder.block_name", "Name: %s MetaData: %s");
        provider.add("behavior.tricorder.state", "%s: %s");
        provider.add("behavior.tricorder.divider=========================", "");
        provider.add("behavior.tricorder.tank", "Tank %s: %s L / %s L %s");
        provider.add("behavior.tricorder.tanks_empty", "All Tanks Empty");
        provider.add("behavior.tricorder.muffled", "Muffled.");
        provider.add("behavior.tricorder.machine_disabled", "Disabled.");
        provider.add("behavior.tricorder.machine_power_loss", "Shut down due to power loss.");
        provider.add("behavior.tricorder.machine_progress", "Progress/Load: %s / %s");
        provider.add("behavior.tricorder.energy_container_in", "Max IN: %s (%s) EU at %s A");
        provider.add("behavior.tricorder.energy_container_out", "Max OUT: %s (%s) EU at %s A");
        provider.add("behavior.tricorder.energy_container_storage", "Energy: %s EU / %s EU");
        provider.add("behavior.tricorder.bedrock_fluid.amount", "Fluid In Deposit: %s %s - %s%%");
        provider.add("behavior.tricorder.bedrock_fluid.amount_unknown", "Fluid In Deposit: %s%%");
        provider.add("behavior.tricorder.bedrock_fluid.nothing", "Fluid In Deposit: §6Nothing§r");
        provider.add("behavior.tricorder.eut_per_sec", "Last Second %s EU/t");
        provider.add("behavior.tricorder.amp_per_sec", "Last Second %s A");
        provider.add("behavior.tricorder.workable_progress", "Progress: %s s / %s s");
        provider.add("behavior.tricorder.workable_stored_energy", "Stored Energy: %s EU / %s EU");
        provider.add("behavior.tricorder.workable_consumption", "Probably Uses: %s EU/t at %s A");
        provider.add("behavior.tricorder.workable_production", "Probably Produces: %s EU/t at %s A");
        provider.add("behavior.tricorder.multiblock_energy_input", "Max Energy Income: %s EU/t Tier: %s");
        provider.add("behavior.tricorder.multiblock_energy_output", "Max Energy Output: %s EU/t Tier: %s");
        provider.add("behavior.tricorder.multiblock_maintenance", "Problems: %s");
        provider.add("behavior.tricorder.multiblock_parallel", "Multi Processing: %s");
        provider.add("behavior.tricorder.debug_machine", "Meta-ID: %s");
        provider.add("behavior.tricorder.debug_machine_valid", " valid");
        provider.add("behavior.tricorder.debug_machine_invalid", " invalid!");
        provider.add("behavior.tricorder.debug_machine_invalid_null=invalid! MetaTileEntity =", " null!");
        provider.add("behavior.tricorder.debug_cpu_load", "Average CPU load of ~%sns over %s ticks with worst time of %sns.");
        provider.add("behavior.tricorder.debug_cpu_load_seconds", "This is %s seconds.");
        provider.add("behavior.tricorder.debug_lag_count", "Caused %s Lag Spike Warnings (anything taking longer than %sms) on the Server.");
        provider.add("behavior.item_magnet.enabled", "§aMagnetic Field Enabled");
        provider.add("behavior.item_magnet.disabled", "§cMagnetic Field Disabled");
        provider.add("metaitem.terminal.tooltip", "Sharp tools make good work");
        provider.add("metaitem.terminal.tooltip.creative", "§bCreative Mode");
        provider.add("metaitem.terminal.tooltip.hardware", "§aHardware: %d");
        provider.add("metaitem.plugin.tooltips.1", "Plugins can be added to the screen for more functionality.");
        provider.add("metaitem.plugin.proxy.tooltips.1", "(Please adjust to proxy mode in the screen)");
        provider.add("metaitem.cover.digital.tooltip", "Connects machines over §fPower Cables§7 to the §fCentral Monitor§7 as §fCover§7.");
        provider.add("tile.casing.ulv", "ULV Machine Casing");
        provider.add("tile.casing.lv", "LV Machine Casing");
        provider.add("tile.casing.mv", "MV Machine Casing");
        provider.add("tile.casing.hv", "HV Machine Casing");
        provider.add("tile.casing.ev", "EV Machine Casing");
        provider.add("tile.casing.iv", "IV Machine Casing");
        provider.add("tile.wire_coil.tooltip_extended_info", "Hold SHIFT to show Coil Bonus Info");
        provider.add("tile.wire_coil.tooltip_heat", "§cBase Heat Capacity: §f%d K");
        provider.add("tile.wire_coil.tooltip_smelter", "§8Multi Smelter:");
        provider.add("tile.wire_coil.tooltip_parallel_smelter", "  §5Max Parallel: §f%s");
        provider.add("tile.wire_coil.tooltip_energy_smelter", "  §aEnergy Usage: §f%s EU/t §8per recipe");
        provider.add("tile.wire_coil.tooltip_pyro", "§8Pyrolyse Oven:");
        provider.add("tile.wire_coil.tooltip_speed_pyro", "  §bProcessing Speed: §f%s%%");
        provider.add("tile.wire_coil.tooltip_cracking", "§8Cracking Unit:");
        provider.add("tile.wire_coil.tooltip_energy_cracking", "  §aEnergy Usage: §f%s%%");
        provider.add("tile.wire_coil.cupronickel.name", "Cupronickel Coil Block");
        provider.add("tile.wire_coil.kanthal.name", "Kanthal Coil Block");
        provider.add("tile.wire_coil.nichrome.name", "Nichrome Coil Block");
        provider.add("tile.wire_coil.tungstensteel.name", "Tungstensteel Coil Block");
        provider.add("tile.wire_coil.hss_g.name", "HSS-G Coil Block");
        provider.add("tile.wire_coil.naquadah.name", "Naquadah Coil Block");
        provider.add("tile.wire_coil.trinium.name", "Trinium Coil Block");
        provider.add("tile.wire_coil.tritanium.name", "Tritanium Coil Block");
        provider.add("tile.warning_sign.yellow_stripes.name", "Yellow Stripes Block");
        provider.add("tile.warning_sign.small_yellow_stripes.name", "Yellow Stripes Block");
        provider.add("tile.warning_sign.radioactive_hazard.name", "Radioactive Hazard Sign Block");
        provider.add("tile.warning_sign.bio_hazard.name", "Bio Hazard Sign Block");
        provider.add("tile.warning_sign.explosion_hazard.name", "Explosion Hazard Sign Block");
        provider.add("tile.warning_sign.fire_hazard.name", "Fire Hazard Sign Block");
        provider.add("tile.warning_sign.acid_hazard.name", "Acid Hazard Sign Block");
        provider.add("tile.warning_sign.magic_hazard.name", "Magic Hazard Sign Block");
        provider.add("tile.warning_sign.frost_hazard.name", "Frost Hazard Sign Block");
        provider.add("tile.warning_sign.noise_hazard.name", "Noise Hazard Sign Block");
        provider.add("tile.warning_sign.generic_hazard.name", "Generic Hazard Sign Block");
        provider.add("tile.warning_sign.high_voltage_hazard.name", "High Voltage Hazard Sign Block");
        provider.add("tile.warning_sign.magnetic_hazard.name", "Magnetic Hazard Sign Block");
        provider.add("tile.warning_sign.antimatter_hazard.name", "Antimatter Hazard Sign Block");
        provider.add("tile.warning_sign.high_temperature_hazard.name", "High Temperature Hazard Sign Block");
        provider.add("tile.warning_sign.void_hazard.name", "Void Hazard Sign Block");
        provider.add("tile.warning_sign_1.mob_spawner_hazard.name", "Mob Spawner Hazard Sign Block");
        provider.add("tile.warning_sign_1.spatial_storage_hazard.name", "Spatial Storage Hazard Sign Block");
        provider.add("tile.warning_sign_1.laser_hazard.name", "Laser Hazard Sign Block");
        provider.add("tile.warning_sign_1.mob_hazard.name", "Mob Infestation Hazard Sign Block");
        provider.add("tile.warning_sign_1.boss_hazard.name", "Boss Hazard Sign Block");
        provider.add("tile.warning_sign_1.gregification_hazard.name", "Gregification Hazard Sign Block");
        provider.add("tile.warning_sign_1.causality_hazard.name", "Non-Standard Causality Hazard Sign Block");
        provider.add("tile.warning_sign_1.automated_defenses_hazard.name", "Automated Defenses Hazard Sign Block");
        provider.add("tile.warning_sign_1.high_pressure_hazard.name", "High Pressure Hazard Sign Block");
        provider.add("tile.boiler_casing.bronze_pipe.name", "Bronze Pipe Casing");
        provider.add("tile.boiler_casing.steel_pipe.name", "Steel Pipe Casing");
        provider.add("tile.boiler_casing.titanium_pipe.name", "Titanium Pipe Casing");
        provider.add("tile.boiler_casing.tungstensteel_pipe.name", "Tungstensteel Pipe Casing");
        provider.add("tile.boiler_casing.polytetrafluoroethylene_pipe.name", "PTFE Pipe Casing");
        provider.add("tile.boiler_casing.bronze_firebox.name", "Bronze Firebox Casing");
        provider.add("tile.boiler_casing.steel_firebox.name", "Steel Firebox Casing");
        provider.add("tile.boiler_casing.titanium_firebox.name", "Titanium Firebox Casing");
        provider.add("tile.boiler_casing.tungstensteel_firebox.name", "Tungstensteel Firebox Casing");
        provider.add("tile.metal_casing.primitive_bricks.name", "Firebricks");
        provider.add("tile.metal_casing.coke_bricks.name", "Coke Oven Bricks");
        provider.add("tile.metal_casing.bronze_bricks.name", "Bronze Machine Casing");
        provider.add("tile.metal_casing.invar_heatproof.name", "Heat Proof Invar Machine Casing");
        provider.add("tile.metal_casing.aluminium_frostproof.name", "Frost Proof Aluminium Machine Casing");
        provider.add("tile.metal_casing.steel_solid.name", "Solid Steel Machine Casing");
        provider.add("tile.metal_casing.stainless_clean.name", "Clean Stainless Steel Casing");
        provider.add("tile.metal_casing.titanium_stable.name", "Stable Titanium Machine Casing");
        provider.add("tile.metal_casing.tungstensteel_robust.name", "Robust Tungstensteel Machine Casing");
        provider.add("tile.metal_casing.ptfe_inert.name", "Chemically Inert PTFE Machine Casing");
        provider.add("tile.metal_casing.hsse_sturdy.name", "Sturdy HSS-E Machine Casing");
        provider.add("tile.machine_casing.ultra_low_voltage.name", "ULV Machine Casing");
        provider.add("tile.machine_casing.low_voltage.name", "LV Machine Casing");
        provider.add("tile.machine_casing.medium_voltage.name", "MV Machine Casing");
        provider.add("tile.machine_casing.high_voltage.name", "HV Machine Casing");
        provider.add("tile.machine_casing.extreme_voltage.name", "EV Machine Casing");
        provider.add("tile.machine_casing.insane_voltage.name", "IV Machine Casing");
        provider.add("tile.machine_casing.ludicrous_voltage.name", "LuV Machine Casing");
        provider.add("tile.machine_casing.zpm_voltage.name", "ZPM Machine Casing");
        provider.add("tile.machine_casing.ultimate_voltage.name", "UV Machine Casing");
        provider.add("tile.machine_casing.ultra_high_voltage.name", "UHV Machine Casing");
        provider.add("tile.machine_casing.ultra_excessive_voltage.name", "UEV Machine Casing");
        provider.add("tile.machine_casing.ultra_immense_voltage.name", "UIV Machine Casing");
        provider.add("tile.machine_casing.ultra_extreme_voltage.name", "UXV Machine Casing");
        provider.add("tile.machine_casing.overpowered_voltage.name", "OpV Machine Casing");
        provider.add("tile.machine_casing.maximum_voltage.name", "MAX Machine Casing");
        provider.add("tile.steam_casing.bronze_hull.name", "Bronze Hull");
        provider.add("tile.steam_casing.bronze_bricks_hull.name", "Bricked Bronze Hull");
        provider.add("tile.steam_casing.steel_hull.name", "Steel Hull");
        provider.add("tile.steam_casing.steel_bricks_hull.name", "Bricked Wrought Iron Hull");
        provider.add("tile.steam_casing.bronze.tooltip", "For your first Steam Machines");
        provider.add("tile.steam_casing.steel.tooltip", "For improved Steam Machines");
        provider.add("tile.steam_casing.pump_deck.name", "Pump Deck");
        provider.add("tile.steam_casing.wood_wall.name", "Wooden Wall");
        provider.add("tile.turbine_casing.bronze_gearbox.name", "Bronze Gearbox Casing");
        provider.add("tile.turbine_casing.steel_gearbox.name", "Steel Gearbox Casing");
        provider.add("tile.turbine_casing.stainless_steel_gearbox.name", "Stainless Steel Gearbox Casing");
        provider.add("tile.turbine_casing.titanium_gearbox.name", "Titanium Gearbox Casing");
        provider.add("tile.turbine_casing.tungstensteel_gearbox.name", "Tungstensteel Gearbox Casing");
        provider.add("tile.turbine_casing.steel_turbine_casing.name", "Steel Turbine Casing");
        provider.add("tile.turbine_casing.titanium_turbine_casing.name", "Titanium Turbine Casing");
        provider.add("tile.turbine_casing.stainless_turbine_casing.name", "Stainless Turbine Casing");
        provider.add("tile.turbine_casing.tungstensteel_turbine_casing.name", "Tungstensteel Turbine Casing");
        provider.add("tile.multiblock_casing.engine_intake.name", "Engine Intake Casing");
        provider.add("tile.multiblock_casing.extreme_engine_intake.name", "Extreme Engine Intake Casing");
        provider.add("tile.multiblock_casing.grate.name", "Grate Machine Casing");
        provider.add("tile.multiblock_casing.assembly_control.name", "Assembly Control Casing");
        provider.add("tile.multiblock_casing.assembly_line.name", "Assembly Line Casing");
        provider.add("tile.fusion_casing.superconductor_coil.name", "Superconducting Coil Block");
        provider.add("tile.fusion_casing.fusion_coil.name", "Fusion Coil Block");
        provider.add("tile.fusion_casing.fusion_casing.name", "Fusion Machine Casing");
        provider.add("tile.fusion_casing.fusion_casing_mk2.name", "Fusion Machine Casing MK II");
        provider.add("tile.fusion_casing.fusion_casing_mk3.name", "Fusion Machine Casing MK III");
        provider.add("tile.transparent_casing.tempered_glass.name", "Tempered Glass");
        provider.add("tile.transparent_casing.fusion_glass.name", "Fusion Glass");
        provider.add("tile.transparent_casing.laminated_glass.name", "Laminated Glass");
        provider.add("tile.transparent_casing.cleanroom_glass.name", "Cleanroom Glass");
        provider.add("gtceu.machine.drum.enable_output", "Will drain Fluid to downward adjacent Tanks");
        provider.add("gtceu.machine.drum.disable_output", "Will not drain Fluid");
        provider.add("gtceu.machine.locked_safe.malfunctioning", "§cMalfunctioning!");
        provider.add("gtceu.machine.locked_safe.requirements", "§7Replacements required:");

        multilineLang(provider, "gtceu.machine.workbench.tooltip", "Better than Forestry\nHas Item Storage, Tool Storage, pulls from adjacent Inventories, and saves Recipes.");
        provider.add("gtceu.machine.workbench.tab.workbench", "Crafting");
        provider.add("gtceu.machine.workbench.tab.item_list", "Storage");
        multilineLang(provider, "gtceu.machine.workbench.storage_note", "(Available items from connected\ninventories usable for crafting)");
        provider.add("gtceu.item_list.item_stored", "§7Stored: %d");
        provider.add("gtceu.machine.workbench.tab.crafting", "Crafting");
        provider.add("gtceu.machine.workbench.tab.container", "Container");


        provider.add("gtceu.multiblock.tank.tooltip", "Fill and drain through the controller or tank valves.");
        provider.add("gtceu.tank_valve.tooltip", "Use to fill and drain multiblock tanks. Auto outputs when facing down.");

        provider.add("tile.cleanroom_casing.plascrete.name", "Plascrete");
        provider.add("tile.cleanroom_casing.filter_casing.name", "Filter Casing");
        provider.add("tile.cleanroom_casing.filter_casing_sterile.name", "Sterilizing Filter Casing");
        provider.add("tile.cleanroom_casing.filter.tooltip", "Creates a §aParticle-Free§7 environment");
        provider.add("tile.cleanroom_casing.filter_sterile.tooltip", "Creates a §aSterilized§7 environment");
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

        provider.add("gtceu.machine.steam_boiler_coal_bronze.tooltip", "An early way to get Steam Power");

        provider.add("gtceu.machine.steam_boiler_coal_steel.tooltip", "Faster than the Small Steam Solid Fuel Boiler");

        provider.add("gtceu.machine.steam_boiler_solar_bronze.tooltip", "Steam Power by Sun");

        provider.add("gtceu.machine.steam_boiler_solar_steel.tooltip", "Steam Power by Sun");

        provider.add("gtceu.machine.steam_boiler_lava_bronze.tooltip", "A Boiler running off Liquids");

        provider.add("gtceu.machine.steam_boiler_lava_steel.tooltip", "Faster than Small Steam Liquid Boiler");
        provider.add("gtceu.machine.steam_boiler.heat_amount", "Heat Capacity: %s %%");

        provider.add("gtceu.machine.steam_extractor_bronze.tooltip", "Extracting your first Rubber");

        provider.add("gtceu.machine.steam_extractor_steel.tooltip", "Extracting your first Rubber");

        provider.add("gtceu.machine.steam_macerator_bronze.tooltip", "Macerating your Ores");

        provider.add("gtceu.machine.steam_macerator_steel.tooltip", "Macerating your Ores");

        provider.add("gtceu.machine.steam_compressor_bronze.tooltip", "Compressing Items");

        provider.add("gtceu.machine.steam_compressor_steel.tooltip", "Compressing Items");

        provider.add("gtceu.machine.steam_hammer_bronze.tooltip", "Forge Hammer");

        provider.add("gtceu.machine.steam_hammer_steel.tooltip", "Forge Hammer");

        provider.add("gtceu.machine.steam_furnace_bronze.tooltip", "Smelting things with compressed Steam");

        provider.add("gtceu.machine.steam_furnace_steel.tooltip", "Smelting things with compressed Steam");

        provider.add("gtceu.machine.steam_alloy_smelter_bronze.tooltip", "Combination Smelter");

        provider.add("gtceu.machine.steam_alloy_smelter_steel.tooltip", "Combination Smelter");

        provider.add("gtceu.machine.steam_rock_breaker_bronze.tooltip", "Place Water and Lava horizontally adjacent");

        provider.add("gtceu.machine.steam_rock_breaker_steel.tooltip", "Place Water and Lava horizontally adjacent");

        provider.add("gtceu.machine.steam_miner.tooltip", "Mines ores below the Miner!");
        provider.add("gtceu.machine.combustion_generator.tooltip", "Requires flammable Liquids");


        provider.add("gtceu.machine.steam_turbine.tooltip", "Converts Steam into EU");


        provider.add("gtceu.machine.gas_turbine.tooltip", "Requires flammable Gases");


        multilineLang(provider, "gtceu.machine.magic_energy_absorber.tooltip", "Max Voltage OUT: §a2048 §7(§5EV§7)\nCollects energy from end crystals on naturally generated obsidian pillars in the End within 64 blocks.\nEach ender crystal adds 32 EU/t to output\nPlace Dragon Egg on top to amplify output\n\nWhen amplified:\nEach ender crystal adds 128 EU/t");


        provider.add("gtceu.machine.block_breaker.tooltip", "Mines block on front face and collects its drops");
        provider.add("gtceu.machine.block_breaker.speed_bonus", "§eSpeed Bonus: §f%d%%");

        provider.add("gtceu.machine.electric_furnace.lv.tooltip", "Not like using a Commodore 64");

        provider.add("gtceu.machine.electric_furnace.mv.tooltip", "Not like using a Commodore 64");

        provider.add("gtceu.machine.electric_furnace.hv.tooltip", "Not like using a Commodore 64");

        provider.add("gtceu.machine.electric_furnace.ev.tooltip", "Not like using a Commodore 64");

        provider.add("gtceu.machine.electric_furnace.iv.tooltip", "Electron Excitement Processor");

        provider.add("gtceu.machine.electric_furnace.luv.tooltip", "Electron Excitement Processor");

        provider.add("gtceu.machine.electric_furnace.zpm.tooltip", "Electron Excitement Processor");

        provider.add("gtceu.machine.electric_furnace.uv.tooltip", "Atom Stimulator");

        provider.add("gtceu.machine.electric_furnace.uhv.tooltip", "Atom Stimulator");

        provider.add("gtceu.machine.electric_furnace.uev.tooltip", "Atom Stimulator");

        provider.add("gtceu.machine.electric_furnace.uiv.tooltip", "Atom Stimulator");

        provider.add("gtceu.machine.electric_furnace.uxv.tooltip", "Atom Stimulator");

        provider.add("gtceu.machine.electric_furnace.opv.tooltip", "Atom Stimulator");

        provider.add("gtceu.machine.macerator.lv.tooltip", "Shredding your Ores");

        provider.add("gtceu.machine.macerator.mv.tooltip", "Shredding your Ores");

        provider.add("gtceu.machine.macerator.hv.tooltip", "Shredding your Ores with Byproducts");

        provider.add("gtceu.machine.macerator.ev.tooltip", "Shredding your Ores with Byproducts");

        provider.add("gtceu.machine.macerator.iv.tooltip", "Blend-O-Matic 9001");

        provider.add("gtceu.machine.macerator.luv.tooltip", "Blend-O-Matic 9002");

        provider.add("gtceu.machine.macerator.zpm.tooltip", "Blend-O-Matic 9003");

        provider.add("gtceu.machine.macerator.uv.tooltip", "Shape Eliminator");

        provider.add("gtceu.machine.macerator.uhv.tooltip", "Shape Eliminator");

        provider.add("gtceu.machine.macerator.uev.tooltip", "Shape Eliminator");

        provider.add("gtceu.machine.macerator.uiv.tooltip", "Shape Eliminator");

        provider.add("gtceu.machine.macerator.uxv.tooltip", "Shape Eliminator");

        provider.add("gtceu.machine.macerator.opv.tooltip", "Shape Eliminator");

        provider.add("gtceu.machine.alloy_smelter.lv.tooltip", "HighTech combination Smelter");

        provider.add("gtceu.machine.alloy_smelter.mv.tooltip", "HighTech combination Smelter");

        provider.add("gtceu.machine.alloy_smelter.hv.tooltip", "HighTech combination Smelter");

        provider.add("gtceu.machine.alloy_smelter.ev.tooltip", "HighTech combination Smelter");

        provider.add("gtceu.machine.alloy_smelter.iv.tooltip", "Alloy Integrator");

        provider.add("gtceu.machine.alloy_smelter.luv.tooltip", "Alloy Integrator");

        provider.add("gtceu.machine.alloy_smelter.zpm.tooltip", "Alloy Integrator");

        provider.add("gtceu.machine.alloy_smelter.uv.tooltip", "Metal Amalgamator");

        provider.add("gtceu.machine.alloy_smelter.uhv.tooltip", "Metal Amalgamator");

        provider.add("gtceu.machine.alloy_smelter.uev.tooltip", "Metal Amalgamator");

        provider.add("gtceu.machine.alloy_smelter.uiv.tooltip", "Metal Amalgamator");

        provider.add("gtceu.machine.alloy_smelter.uxv.tooltip", "Metal Amalgamator");

        provider.add("gtceu.machine.alloy_smelter.opv.tooltip", "Metal Amalgamator");

        provider.add("gtceu.machine.arc_furnace.lv.tooltip", "Who needs a Blast Furnace?");

        provider.add("gtceu.machine.arc_furnace.mv.tooltip", "Who needs a Blast Furnace?");

        provider.add("gtceu.machine.arc_furnace.hv.tooltip", "Who needs a Blast Furnace?");

        provider.add("gtceu.machine.arc_furnace.ev.tooltip", "Who needs a Blast Furnace?");

        provider.add("gtceu.machine.arc_furnace.iv.tooltip", "Discharge Heater");

        provider.add("gtceu.machine.arc_furnace.luv.tooltip", "Discharge Heater");

        provider.add("gtceu.machine.arc_furnace.zpm.tooltip", "Discharge Heater");

        provider.add("gtceu.machine.arc_furnace.uv.tooltip", "Short Circuit Heater");

        provider.add("gtceu.machine.arc_furnace.uhv.tooltip", "Short Circuit Heater");

        provider.add("gtceu.machine.arc_furnace.uev.tooltip", "Short Circuit Heater");

        provider.add("gtceu.machine.arc_furnace.uiv.tooltip", "Short Circuit Heater");

        provider.add("gtceu.machine.arc_furnace.uxv.tooltip", "Short Circuit Heater");

        provider.add("gtceu.machine.arc_furnace.opv.tooltip", "Short Circuit Heater");

        provider.add("gtceu.machine.assembler.lv.tooltip", "Avengers, Assemble!");

        provider.add("gtceu.machine.assembler.mv.tooltip", "Avengers, Assemble!");

        provider.add("gtceu.machine.assembler.hv.tooltip", "Avengers, Assemble!");

        provider.add("gtceu.machine.assembler.ev.tooltip", "Avengers, Assemble!");

        provider.add("gtceu.machine.assembler.iv.tooltip", "NOT a Crafting Table");

        provider.add("gtceu.machine.assembler.luv.tooltip", "NOT a Crafting Table");

        provider.add("gtceu.machine.assembler.zpm.tooltip", "NOT a Crafting Table");

        provider.add("gtceu.machine.assembler.uv.tooltip", "Assembly Constructor");

        provider.add("gtceu.machine.assembler.uhv.tooltip", "Assembly Constructor");

        provider.add("gtceu.machine.assembler.uev.tooltip", "Assembly Constructor");

        provider.add("gtceu.machine.assembler.uiv.tooltip", "Assembly Constructor");

        provider.add("gtceu.machine.assembler.uxv.tooltip", "Assembly Constructor");

        provider.add("gtceu.machine.assembler.opv.tooltip", "Assembly Constructor");

        provider.add("gtceu.machine.autoclave.lv.tooltip", "Crystallizing your Dusts");

        provider.add("gtceu.machine.autoclave.mv.tooltip", "Crystallizing your Dusts");

        provider.add("gtceu.machine.autoclave.hv.tooltip", "Crystallizing your Dusts");

        provider.add("gtceu.machine.autoclave.ev.tooltip", "Crystallizing your Dusts");

        provider.add("gtceu.machine.autoclave.iv.tooltip", "Pressure Cooker");

        provider.add("gtceu.machine.autoclave.luv.tooltip", "Pressure Cooker");

        provider.add("gtceu.machine.autoclave.zpm.tooltip", "Pressure Cooker");

        provider.add("gtceu.machine.autoclave.uv.tooltip", "Encumbrance Unit");

        provider.add("gtceu.machine.autoclave.uhv.tooltip", "Encumbrance Unit");

        provider.add("gtceu.machine.autoclave.uev.tooltip", "Encumbrance Unit");

        provider.add("gtceu.machine.autoclave.uiv.tooltip", "Encumbrance Unit");

        provider.add("gtceu.machine.autoclave.uxv.tooltip", "Encumbrance Unit");

        provider.add("gtceu.machine.autoclave.opv.tooltip", "Encumbrance Unit");

        provider.add("gtceu.machine.bender.lv.tooltip", "Boo, he's bad! We want BENDER!!!");

        provider.add("gtceu.machine.bender.mv.tooltip", "Boo, he's bad! We want BENDER!!!");

        provider.add("gtceu.machine.bender.hv.tooltip", "Boo, he's bad! We want BENDER!!!");

        provider.add("gtceu.machine.bender.ev.tooltip", "Boo, he's bad! We want BENDER!!!");

        provider.add("gtceu.machine.bender.iv.tooltip", "Shape Distorter");

        provider.add("gtceu.machine.bender.luv.tooltip", "Shape Distorter");

        provider.add("gtceu.machine.bender.zpm.tooltip", "Shape Distorter");

        provider.add("gtceu.machine.bender.uv.tooltip", "Matter Deformer");

        provider.add("gtceu.machine.bender.uhv.tooltip", "Matter Deformer");

        provider.add("gtceu.machine.bender.uev.tooltip", "Matter Deformer");

        provider.add("gtceu.machine.bender.uiv.tooltip", "Matter Deformer");

        provider.add("gtceu.machine.bender.uxv.tooltip", "Matter Deformer");

        provider.add("gtceu.machine.bender.opv.tooltip", "Matter Deformer");

        provider.add("gtceu.machine.brewery.lv.tooltip", "Compact and efficient potion brewing");

        provider.add("gtceu.machine.brewery.mv.tooltip", "Compact and efficient potion brewing");

        provider.add("gtceu.machine.brewery.hv.tooltip", "Compact and efficient potion brewing");

        provider.add("gtceu.machine.brewery.ev.tooltip", "Compact and efficient potion brewing");

        provider.add("gtceu.machine.brewery.iv.tooltip", "Brewing your Drinks");

        provider.add("gtceu.machine.brewery.luv.tooltip", "Brewing your Drinks");

        provider.add("gtceu.machine.brewery.zpm.tooltip", "Brewing your Drinks");

        provider.add("gtceu.machine.brewery.uv.tooltip", "Brew Rusher");

        provider.add("gtceu.machine.brewery.uhv.tooltip", "Brew Rusher");

        provider.add("gtceu.machine.brewery.uev.tooltip", "Brew Rusher");

        provider.add("gtceu.machine.brewery.uiv.tooltip", "Brew Rusher");

        provider.add("gtceu.machine.brewery.uxv.tooltip", "Brew Rusher");

        provider.add("gtceu.machine.brewery.opv.tooltip", "Brew Rusher");
        provider.add("gtceu.machine.canner.jei_description", "You can fill and empty any fluid containers with the Fluid Canner (e.g. Buckets or Fluid Cells)");

        provider.add("gtceu.machine.canner.lv.tooltip", "Puts things into and out of Containers");

        provider.add("gtceu.machine.canner.mv.tooltip", "Puts things into and out of Containers");

        provider.add("gtceu.machine.canner.hv.tooltip", "Puts things into and out of Containers");

        provider.add("gtceu.machine.canner.ev.tooltip", "Puts things into and out of Containers");

        provider.add("gtceu.machine.canner.iv.tooltip", "Can Operator");

        provider.add("gtceu.machine.canner.luv.tooltip", "Can Operator");

        provider.add("gtceu.machine.canner.zpm.tooltip", "Can Operator");

        provider.add("gtceu.machine.canner.uv.tooltip", "Can Actuator");

        provider.add("gtceu.machine.canner.uhv.tooltip", "Can Actuator");

        provider.add("gtceu.machine.canner.uev.tooltip", "Can Actuator");

        provider.add("gtceu.machine.canner.uiv.tooltip", "Can Actuator");

        provider.add("gtceu.machine.canner.uxv.tooltip", "Can Actuator");

        provider.add("gtceu.machine.canner.opv.tooltip", "Can Actuator");

        provider.add("gtceu.machine.centrifuge.lv.tooltip", "Separating Molecules");

        provider.add("gtceu.machine.centrifuge.mv.tooltip", "Separating Molecules");

        provider.add("gtceu.machine.centrifuge.hv.tooltip", "Separating Molecules");

        provider.add("gtceu.machine.centrifuge.ev.tooltip", "Molecular Separator");

        provider.add("gtceu.machine.centrifuge.iv.tooltip", "Molecular Cyclone");

        provider.add("gtceu.machine.centrifuge.luv.tooltip", "Molecular Cyclone");

        provider.add("gtceu.machine.centrifuge.zpm.tooltip", "Molecular Cyclone");

        provider.add("gtceu.machine.centrifuge.uv.tooltip", "Molecular Tornado");

        provider.add("gtceu.machine.centrifuge.uhv.tooltip", "Molecular Tornado");

        provider.add("gtceu.machine.centrifuge.uev.tooltip", "Molecular Tornado");

        provider.add("gtceu.machine.centrifuge.uiv.tooltip", "Molecular Tornado");

        provider.add("gtceu.machine.centrifuge.uxv.tooltip", "Molecular Tornado");

        provider.add("gtceu.machine.centrifuge.opv.tooltip", "Molecular Tornado");

        provider.add("gtceu.machine.chemical_bath.lv.tooltip", "Bathing Ores in Chemicals to separate them");

        provider.add("gtceu.machine.chemical_bath.mv.tooltip", "Bathing Ores in Chemicals to separate them");

        provider.add("gtceu.machine.chemical_bath.hv.tooltip", "Bathing Ores in Chemicals to separate them");

        provider.add("gtceu.machine.chemical_bath.ev.tooltip", "Bathing Ores in Chemicals to separate them");

        provider.add("gtceu.machine.chemical_bath.iv.tooltip", "Chemical Soaker");

        provider.add("gtceu.machine.chemical_bath.luv.tooltip", "Chemical Soaker");

        provider.add("gtceu.machine.chemical_bath.zpm.tooltip", "Chemical Soaker");

        provider.add("gtceu.machine.chemical_bath.uv.tooltip", "Chemical Dunktron");

        provider.add("gtceu.machine.chemical_bath.uhv.tooltip", "Chemical Dunktron");

        provider.add("gtceu.machine.chemical_bath.uev.tooltip", "Chemical Dunktron");

        provider.add("gtceu.machine.chemical_bath.uiv.tooltip", "Chemical Dunktron");

        provider.add("gtceu.machine.chemical_bath.uxv.tooltip", "Chemical Dunktron");

        provider.add("gtceu.machine.chemical_bath.opv.tooltip", "Chemical Dunktron");

        provider.add("gtceu.machine.chemical_reactor.lv.tooltip", "Letting Chemicals react with each other");

        provider.add("gtceu.machine.chemical_reactor.mv.tooltip", "Letting Chemicals react with each other");

        provider.add("gtceu.machine.chemical_reactor.hv.tooltip", "Letting Chemicals react with each other");

        provider.add("gtceu.machine.chemical_reactor.ev.tooltip", "Letting Chemicals react with each other");

        provider.add("gtceu.machine.chemical_reactor.iv.tooltip", "Chemical Performer");

        provider.add("gtceu.machine.chemical_reactor.luv.tooltip", "Chemical Performer");

        provider.add("gtceu.machine.chemical_reactor.zpm.tooltip", "Chemical Performer");

        provider.add("gtceu.machine.chemical_reactor.uv.tooltip", "Reaction Catalyzer");

        provider.add("gtceu.machine.chemical_reactor.uhv.tooltip", "Reaction Catalyzer");

        provider.add("gtceu.machine.chemical_reactor.uev.tooltip", "Reaction Catalyzer");

        provider.add("gtceu.machine.chemical_reactor.uiv.tooltip", "Reaction Catalyzer");

        provider.add("gtceu.machine.chemical_reactor.uxv.tooltip", "Reaction Catalyzer");

        provider.add("gtceu.machine.chemical_reactor.opv.tooltip", "Reaction Catalyzer");

        provider.add("gtceu.machine.compressor.lv.tooltip", "Compress-O-Matic C77");

        provider.add("gtceu.machine.compressor.mv.tooltip", "Compress-O-Matic C77");

        provider.add("gtceu.machine.compressor.hv.tooltip", "Compress-O-Matic C77");

        provider.add("gtceu.machine.compressor.ev.tooltip", "Compress-O-Matic C77");

        provider.add("gtceu.machine.compressor.iv.tooltip", "Singularity Condenser");

        provider.add("gtceu.machine.compressor.luv.tooltip", "Singularity Condenser");

        provider.add("gtceu.machine.compressor.zpm.tooltip", "Singularity Condenser");

        provider.add("gtceu.machine.compressor.uv.tooltip", "Matter Constrictor");

        provider.add("gtceu.machine.compressor.uhv.tooltip", "Matter Constrictor");

        provider.add("gtceu.machine.compressor.uev.tooltip", "Matter Constrictor");

        provider.add("gtceu.machine.compressor.uiv.tooltip", "Matter Constrictor");

        provider.add("gtceu.machine.compressor.uxv.tooltip", "Matter Constrictor");

        provider.add("gtceu.machine.compressor.opv.tooltip", "Matter Constrictor");

        provider.add("gtceu.machine.cutter.lv.tooltip", "Slice'N Dice");

        provider.add("gtceu.machine.cutter.mv.tooltip", "Slice'N Dice");

        provider.add("gtceu.machine.cutter.hv.tooltip", "Slice'N Dice");

        provider.add("gtceu.machine.cutter.ev.tooltip", "Slice'N Dice");

        provider.add("gtceu.machine.cutter.iv.tooltip", "Matter Cleaver");

        provider.add("gtceu.machine.cutter.luv.tooltip", "Matter Cleaver");

        provider.add("gtceu.machine.cutter.zpm.tooltip", "Matter Cleaver");

        provider.add("gtceu.machine.cutter.uv.tooltip", "Object Divider");

        provider.add("gtceu.machine.cutter.uhv.tooltip", "Object Divider");

        provider.add("gtceu.machine.cutter.uev.tooltip", "Object Divider");

        provider.add("gtceu.machine.cutter.uiv.tooltip", "Object Divider");

        provider.add("gtceu.machine.cutter.uxv.tooltip", "Object Divider");

        provider.add("gtceu.machine.cutter.opv.tooltip", "Object Divider");

        provider.add("gtceu.machine.distillery.lv.tooltip", "Extracting most relevant Parts of Fluids");

        provider.add("gtceu.machine.distillery.mv.tooltip", "Extracting most relevant Parts of Fluids");

        provider.add("gtceu.machine.distillery.hv.tooltip", "Extracting most relevant Parts of Fluids");

        provider.add("gtceu.machine.distillery.ev.tooltip", "Extracting most relevant Parts of Fluids");

        provider.add("gtceu.machine.distillery.iv.tooltip", "Condensation Separator");

        provider.add("gtceu.machine.distillery.luv.tooltip", "Condensation Separator");

        provider.add("gtceu.machine.distillery.zpm.tooltip", "Condensation Separator");

        provider.add("gtceu.machine.distillery.uv.tooltip", "Fraction Splitter");

        provider.add("gtceu.machine.distillery.uhv.tooltip", "Fraction Splitter");

        provider.add("gtceu.machine.distillery.uev.tooltip", "Fraction Splitter");

        provider.add("gtceu.machine.distillery.uiv.tooltip", "Fraction Splitter");

        provider.add("gtceu.machine.distillery.uxv.tooltip", "Fraction Splitter");

        provider.add("gtceu.machine.distillery.opv.tooltip", "Fraction Splitter");

        provider.add("gtceu.machine.electrolyzer.lv.tooltip", "Electrolyzing Molecules");

        provider.add("gtceu.machine.electrolyzer.mv.tooltip", "Electrolyzing Molecules");

        provider.add("gtceu.machine.electrolyzer.hv.tooltip", "Electrolyzing Molecules");

        provider.add("gtceu.machine.electrolyzer.ev.tooltip", "Electrolyzing Molecules");

        provider.add("gtceu.machine.electrolyzer.iv.tooltip", "Molecular Disintegrator E-4906");

        provider.add("gtceu.machine.electrolyzer.luv.tooltip", "Molecular Disintegrator E-4907");

        provider.add("gtceu.machine.electrolyzer.zpm.tooltip", "Molecular Disintegrator E-4908");

        provider.add("gtceu.machine.electrolyzer.uv.tooltip", "Atomic Ionizer");

        provider.add("gtceu.machine.electrolyzer.uhv.tooltip", "Atomic Ionizer");

        provider.add("gtceu.machine.electrolyzer.uev.tooltip", "Atomic Ionizer");

        provider.add("gtceu.machine.electrolyzer.uiv.tooltip", "Atomic Ionizer");

        provider.add("gtceu.machine.electrolyzer.uxv.tooltip", "Atomic Ionizer");

        provider.add("gtceu.machine.electrolyzer.opv.tooltip", "Atomic Ionizer");

        provider.add("gtceu.machine.electromagnetic_separator.lv.tooltip", "Separating the magnetic Ores from the rest");

        provider.add("gtceu.machine.electromagnetic_separator.mv.tooltip", "Separating the magnetic Ores from the rest");

        provider.add("gtceu.machine.electromagnetic_separator.hv.tooltip", "Separating the magnetic Ores from the rest");

        provider.add("gtceu.machine.electromagnetic_separator.ev.tooltip", "Separating the magnetic Ores from the rest");

        provider.add("gtceu.machine.electromagnetic_separator.iv.tooltip", "EM Categorizer");

        provider.add("gtceu.machine.electromagnetic_separator.luv.tooltip", "EM Categorizer");

        provider.add("gtceu.machine.electromagnetic_separator.zpm.tooltip", "EM Categorizer");

        provider.add("gtceu.machine.electromagnetic_separator.uv.tooltip", "EMF Dispeller");

        provider.add("gtceu.machine.electromagnetic_separator.uhv.tooltip", "EMF Dispeller");

        provider.add("gtceu.machine.electromagnetic_separator.uev.tooltip", "EMF Dispeller");

        provider.add("gtceu.machine.electromagnetic_separator.uiv.tooltip", "EMF Dispeller");

        provider.add("gtceu.machine.electromagnetic_separator.uxv.tooltip", "EMF Dispeller");

        provider.add("gtceu.machine.electromagnetic_separator.opv.tooltip", "EMF Dispeller");

        provider.add("gtceu.machine.extractor.lv.tooltip", "Dejuicer-Device of Doom - D123");

        provider.add("gtceu.machine.extractor.mv.tooltip", "Dejuicer-Device of Doom - D123");

        provider.add("gtceu.machine.extractor.hv.tooltip", "Dejuicer-Device of Doom - D123");

        provider.add("gtceu.machine.extractor.ev.tooltip", "Dejuicer-Device of Doom - D123");

        provider.add("gtceu.machine.extractor.iv.tooltip", "Vacuum Extractinator");

        provider.add("gtceu.machine.extractor.luv.tooltip", "Vacuum Extractinator");

        provider.add("gtceu.machine.extractor.zpm.tooltip", "Vacuum Extractinator");

        provider.add("gtceu.machine.extractor.uv.tooltip", "Liquefying Sucker");

        provider.add("gtceu.machine.extractor.uhv.tooltip", "Liquefying Sucker");

        provider.add("gtceu.machine.extractor.uev.tooltip", "Liquefying Sucker");

        provider.add("gtceu.machine.extractor.uiv.tooltip", "Liquefying Sucker");

        provider.add("gtceu.machine.extractor.uxv.tooltip", "Liquefying Sucker");

        provider.add("gtceu.machine.extractor.opv.tooltip", "Liquefying Sucker");

        provider.add("gtceu.machine.extruder.lv.tooltip", "Universal Machine for Metal Working");

        provider.add("gtceu.machine.extruder.mv.tooltip", "Universal Machine for Metal Working");

        provider.add("gtceu.machine.extruder.hv.tooltip", "Universal Machine for Metal Working");

        provider.add("gtceu.machine.extruder.ev.tooltip", "Universal Machine for Metal Working");

        provider.add("gtceu.machine.extruder.iv.tooltip", "Material Displacer");

        provider.add("gtceu.machine.extruder.luv.tooltip", "Material Displacer");

        provider.add("gtceu.machine.extruder.zpm.tooltip", "Material Displacer");

        provider.add("gtceu.machine.extruder.uv.tooltip", "Shape Driver");

        provider.add("gtceu.machine.extruder.uhv.tooltip", "Shape Driver");

        provider.add("gtceu.machine.extruder.uev.tooltip", "Shape Driver");

        provider.add("gtceu.machine.extruder.uiv.tooltip", "Shape Driver");

        provider.add("gtceu.machine.extruder.uxv.tooltip", "Shape Driver");

        provider.add("gtceu.machine.extruder.opv.tooltip", "Shape Driver");

        provider.add("gtceu.machine.fermenter.lv.tooltip", "Fermenting Fluids");

        provider.add("gtceu.machine.fermenter.mv.tooltip", "Fermenting Fluids");

        provider.add("gtceu.machine.fermenter.hv.tooltip", "Fermenting Fluids");

        provider.add("gtceu.machine.fermenter.ev.tooltip", "Fermenting Fluids");

        provider.add("gtceu.machine.fermenter.iv.tooltip", "Fermentation Hastener");

        provider.add("gtceu.machine.fermenter.luv.tooltip", "Fermentation Hastener");

        provider.add("gtceu.machine.fermenter.zpm.tooltip", "Fermentation Hastener");

        provider.add("gtceu.machine.fermenter.uv.tooltip", "Respiration Controller");

        provider.add("gtceu.machine.fermenter.uhv.tooltip", "Respiration Controller");

        provider.add("gtceu.machine.fermenter.uev.tooltip", "Respiration Controller");

        provider.add("gtceu.machine.fermenter.uiv.tooltip", "Respiration Controller");

        provider.add("gtceu.machine.fermenter.uxv.tooltip", "Respiration Controller");

        provider.add("gtceu.machine.fermenter.opv.tooltip", "Respiration Controller");

        provider.add("gtceu.machine.fluid_heater.lv.tooltip", "Heating up your Fluids");

        provider.add("gtceu.machine.fluid_heater.mv.tooltip", "Heating up your Fluids");

        provider.add("gtceu.machine.fluid_heater.hv.tooltip", "Heating up your Fluids");

        provider.add("gtceu.machine.fluid_heater.ev.tooltip", "Heating up your Fluids");

        provider.add("gtceu.machine.fluid_heater.iv.tooltip", "Heat Infuser");

        provider.add("gtceu.machine.fluid_heater.luv.tooltip", "Heat Infuser");

        provider.add("gtceu.machine.fluid_heater.zpm.tooltip", "Heat Infuser");

        provider.add("gtceu.machine.fluid_heater.uv.tooltip", "Thermal Imbuer");

        provider.add("gtceu.machine.fluid_heater.uhv.tooltip", "Thermal Imbuer");

        provider.add("gtceu.machine.fluid_heater.uev.tooltip", "Thermal Imbuer");

        provider.add("gtceu.machine.fluid_heater.uiv.tooltip", "Thermal Imbuer");

        provider.add("gtceu.machine.fluid_heater.uxv.tooltip", "Thermal Imbuer");

        provider.add("gtceu.machine.fluid_heater.opv.tooltip", "Thermal Imbuer");

        provider.add("gtceu.machine.fluid_solidifier.lv.tooltip", "Cools Fluids down to form Solids");

        provider.add("gtceu.machine.fluid_solidifier.mv.tooltip", "Cools Fluids down to form Solids");

        provider.add("gtceu.machine.fluid_solidifier.hv.tooltip", "Cools Fluids down to form Solids");

        provider.add("gtceu.machine.fluid_solidifier.ev.tooltip", "Cools Fluids down to form Solids");

        provider.add("gtceu.machine.fluid_solidifier.iv.tooltip", "Not an Ice Machine");

        provider.add("gtceu.machine.fluid_solidifier.luv.tooltip", "Not an Ice Machine");

        provider.add("gtceu.machine.fluid_solidifier.zpm.tooltip", "Not an Ice Machine");

        provider.add("gtceu.machine.fluid_solidifier.uv.tooltip", "Fluid Petrificator");

        provider.add("gtceu.machine.fluid_solidifier.uhv.tooltip", "Fluid Petrificator");

        provider.add("gtceu.machine.fluid_solidifier.uev.tooltip", "Fluid Petrificator");

        provider.add("gtceu.machine.fluid_solidifier.uiv.tooltip", "Fluid Petrificator");

        provider.add("gtceu.machine.fluid_solidifier.uxv.tooltip", "Fluid Petrificator");

        provider.add("gtceu.machine.fluid_solidifier.opv.tooltip", "Fluid Petrificator");

        provider.add("gtceu.machine.forge_hammer.lv.tooltip", "Stop, Hammertime!");

        provider.add("gtceu.machine.forge_hammer.mv.tooltip", "Stop, Hammertime!");

        provider.add("gtceu.machine.forge_hammer.hv.tooltip", "Stop, Hammertime!");

        provider.add("gtceu.machine.forge_hammer.ev.tooltip", "Stop, Hammertime!");

        provider.add("gtceu.machine.forge_hammer.iv.tooltip", "Plate Forger");

        provider.add("gtceu.machine.forge_hammer.luv.tooltip", "Plate Forger");

        provider.add("gtceu.machine.forge_hammer.zpm.tooltip", "Plate Forger");

        provider.add("gtceu.machine.forge_hammer.uv.tooltip", "Impact Modulator");

        provider.add("gtceu.machine.forge_hammer.uhv.tooltip", "Impact Modulator");

        provider.add("gtceu.machine.forge_hammer.uev.tooltip", "Impact Modulator");

        provider.add("gtceu.machine.forge_hammer.uiv.tooltip", "Impact Modulator");

        provider.add("gtceu.machine.forge_hammer.uxv.tooltip", "Impact Modulator");

        provider.add("gtceu.machine.forge_hammer.opv.tooltip", "Impact Modulator");

        provider.add("gtceu.machine.forming_press.lv.tooltip", "Imprinting Images into things");

        provider.add("gtceu.machine.forming_press.mv.tooltip", "Imprinting Images into things");

        provider.add("gtceu.machine.forming_press.hv.tooltip", "Imprinting Images into things");

        provider.add("gtceu.machine.forming_press.ev.tooltip", "Imprinting Images into things");

        provider.add("gtceu.machine.forming_press.iv.tooltip", "Object Layerer");

        provider.add("gtceu.machine.forming_press.luv.tooltip", "Object Layerer");

        provider.add("gtceu.machine.forming_press.zpm.tooltip", "Object Layerer");

        provider.add("gtceu.machine.forming_press.uv.tooltip", "Surface Shifter");

        provider.add("gtceu.machine.forming_press.uhv.tooltip", "Surface Shifter");

        provider.add("gtceu.machine.forming_press.uev.tooltip", "Surface Shifter");

        provider.add("gtceu.machine.forming_press.uiv.tooltip", "Surface Shifter");

        provider.add("gtceu.machine.forming_press.uxv.tooltip", "Surface Shifter");

        provider.add("gtceu.machine.forming_press.opv.tooltip", "Surface Shifter");

        provider.add("gtceu.machine.lathe.lv.tooltip", "Produces Rods more efficiently");

        provider.add("gtceu.machine.lathe.mv.tooltip", "Produces Rods more efficiently");

        provider.add("gtceu.machine.lathe.hv.tooltip", "Produces Rods more efficiently");

        provider.add("gtceu.machine.lathe.ev.tooltip", "Produces Rods more efficiently");

        provider.add("gtceu.machine.lathe.iv.tooltip", "Turn-O-Matic L-5906");

        provider.add("gtceu.machine.lathe.luv.tooltip", "Turn-O-Matic L-5907");

        provider.add("gtceu.machine.lathe.zpm.tooltip", "Turn-O-Matic L-5908");

        provider.add("gtceu.machine.lathe.uv.tooltip", "Rotation Grinder");

        provider.add("gtceu.machine.lathe.uhv.tooltip", "Rotation Grinder");

        provider.add("gtceu.machine.lathe.uev.tooltip", "Rotation Grinder");

        provider.add("gtceu.machine.lathe.uiv.tooltip", "Rotation Grinder");

        provider.add("gtceu.machine.lathe.uxv.tooltip", "Rotation Grinder");

        provider.add("gtceu.machine.lathe.opv.tooltip", "Rotation Grinder");

        provider.add("gtceu.machine.mixer.lv.tooltip", "Will it Blend?");

        provider.add("gtceu.machine.mixer.mv.tooltip", "Will it Blend?");

        provider.add("gtceu.machine.mixer.hv.tooltip", "Will it Blend?");

        provider.add("gtceu.machine.mixer.ev.tooltip", "Will it Blend?");

        provider.add("gtceu.machine.mixer.iv.tooltip", "Matter Organizer");

        provider.add("gtceu.machine.mixer.luv.tooltip", "Matter Organizer");

        provider.add("gtceu.machine.mixer.zpm.tooltip", "Matter Organizer");

        provider.add("gtceu.machine.mixer.uv.tooltip", "Material Homogenizer");

        provider.add("gtceu.machine.mixer.uhv.tooltip", "Material Homogenizer");

        provider.add("gtceu.machine.mixer.uev.tooltip", "Material Homogenizer");

        provider.add("gtceu.machine.mixer.uiv.tooltip", "Material Homogenizer");

        provider.add("gtceu.machine.mixer.uxv.tooltip", "Material Homogenizer");

        provider.add("gtceu.machine.mixer.opv.tooltip", "Material Homogenizer");

        provider.add("gtceu.machine.ore_washer.lv.tooltip", "Getting more Byproducts from your Ores");

        provider.add("gtceu.machine.ore_washer.mv.tooltip", "Getting more Byproducts from your Ores");

        provider.add("gtceu.machine.ore_washer.hv.tooltip", "Getting more Byproducts from your Ores");

        provider.add("gtceu.machine.ore_washer.ev.tooltip", "Getting more Byproducts from your Ores");

        provider.add("gtceu.machine.ore_washer.iv.tooltip", "Repurposed Laundry-Washer I-360");

        provider.add("gtceu.machine.ore_washer.luv.tooltip", "Repurposed Laundry-Washer I-361");

        provider.add("gtceu.machine.ore_washer.zpm.tooltip", "Repurposed Laundry-Washer I-362");

        provider.add("gtceu.machine.ore_washer.uv.tooltip", "Miniature Car Wash");

        provider.add("gtceu.machine.ore_washer.uhv.tooltip", "Miniature Car Wash");

        provider.add("gtceu.machine.ore_washer.uev.tooltip", "Miniature Car Wash");

        provider.add("gtceu.machine.ore_washer.uiv.tooltip", "Miniature Car Wash");

        provider.add("gtceu.machine.ore_washer.uxv.tooltip", "Miniature Car Wash");

        provider.add("gtceu.machine.ore_washer.opv.tooltip", "Miniature Car Wash");

        provider.add("gtceu.machine.packer.lv.tooltip", "Puts things into and Grabs things out of Boxes");

        provider.add("gtceu.machine.packer.mv.tooltip", "Puts things into and Grabs things out of Boxes");

        provider.add("gtceu.machine.packer.hv.tooltip", "Puts things into and Grabs things out of Boxes");

        provider.add("gtceu.machine.packer.ev.tooltip", "Puts things into and Grabs things out of Boxes");

        provider.add("gtceu.machine.packer.iv.tooltip", "Boxinator");

        provider.add("gtceu.machine.packer.luv.tooltip", "Boxinator");

        provider.add("gtceu.machine.packer.zpm.tooltip", "Boxinator");

        provider.add("gtceu.machine.packer.uv.tooltip", "Amazon Warehouse");

        provider.add("gtceu.machine.packer.uhv.tooltip", "Amazon Warehouse");

        provider.add("gtceu.machine.packer.uev.tooltip", "Amazon Warehouse");

        provider.add("gtceu.machine.packer.uiv.tooltip", "Amazon Warehouse");

        provider.add("gtceu.machine.packer.uxv.tooltip", "Amazon Warehouse");

        provider.add("gtceu.machine.packer.opv.tooltip", "Amazon Warehouse");

        provider.add("gtceu.machine.polarizer.lv.tooltip", "Bipolarising your Magnets");

        provider.add("gtceu.machine.polarizer.mv.tooltip", "Bipolarising your Magnets");

        provider.add("gtceu.machine.polarizer.hv.tooltip", "Bipolarising your Magnets");

        provider.add("gtceu.machine.polarizer.ev.tooltip", "Bipolarising your Magnets");

        provider.add("gtceu.machine.polarizer.iv.tooltip", "Magnetism Inducer");

        provider.add("gtceu.machine.polarizer.luv.tooltip", "Magnetism Inducer");

        provider.add("gtceu.machine.polarizer.zpm.tooltip", "Magnetism Inducer");

        provider.add("gtceu.machine.polarizer.uv.tooltip", "Magnetic Field Rearranger");

        provider.add("gtceu.machine.polarizer.uhv.tooltip", "Magnetic Field Rearranger");

        provider.add("gtceu.machine.polarizer.uev.tooltip", "Magnetic Field Rearranger");

        provider.add("gtceu.machine.polarizer.uiv.tooltip", "Magnetic Field Rearranger");

        provider.add("gtceu.machine.polarizer.uxv.tooltip", "Magnetic Field Rearranger");

        provider.add("gtceu.machine.polarizer.opv.tooltip", "Magnetic Field Rearranger");

        provider.add("gtceu.machine.laser_engraver.lv.tooltip", "Don't look directly at the Laser");

        provider.add("gtceu.machine.laser_engraver.mv.tooltip", "Don't look directly at the Laser");

        provider.add("gtceu.machine.laser_engraver.hv.tooltip", "Don't look directly at the Laser");

        provider.add("gtceu.machine.laser_engraver.ev.tooltip", "Don't look directly at the Laser");

        provider.add("gtceu.machine.laser_engraver.iv.tooltip", "With the Power of 2.04 MW");

        provider.add("gtceu.machine.laser_engraver.luv.tooltip", "With the Power of 8.16 MW");

        provider.add("gtceu.machine.laser_engraver.zpm.tooltip", "With the Power of 32.64 MW");

        provider.add("gtceu.machine.laser_engraver.uv.tooltip", "Exact Photon Cannon");

        provider.add("gtceu.machine.laser_engraver.uhv.tooltip", "Exact Photon Cannon");

        provider.add("gtceu.machine.laser_engraver.uev.tooltip", "Exact Photon Cannon");

        provider.add("gtceu.machine.laser_engraver.uiv.tooltip", "Exact Photon Cannon");

        provider.add("gtceu.machine.laser_engraver.uxv.tooltip", "Exact Photon Cannon");

        provider.add("gtceu.machine.laser_engraver.opv.tooltip", "Exact Photon Cannon");

        provider.add("gtceu.machine.sifter.lv.tooltip", "Stay calm and keep sifting");

        provider.add("gtceu.machine.sifter.mv.tooltip", "Stay calm and keep sifting");

        provider.add("gtceu.machine.sifter.hv.tooltip", "Stay calm and keep sifting");

        provider.add("gtceu.machine.sifter.ev.tooltip", "Stay calm and keep sifting");

        provider.add("gtceu.machine.sifter.iv.tooltip", "Sponsored by TFC");

        provider.add("gtceu.machine.sifter.luv.tooltip", "Sponsored by TFC");

        provider.add("gtceu.machine.sifter.zpm.tooltip", "Sponsored by TFC");

        provider.add("gtceu.machine.sifter.uv.tooltip", "Pulsation Filter");

        provider.add("gtceu.machine.sifter.uhv.tooltip", "Pulsation Filter");

        provider.add("gtceu.machine.sifter.uev.tooltip", "Pulsation Filter");

        provider.add("gtceu.machine.sifter.uiv.tooltip", "Pulsation Filter");

        provider.add("gtceu.machine.sifter.uxv.tooltip", "Pulsation Filter");

        provider.add("gtceu.machine.sifter.opv.tooltip", "Pulsation Filter");

        provider.add("gtceu.machine.thermal_centrifuge.lv.tooltip", "Separating Ores more precisely");

        provider.add("gtceu.machine.thermal_centrifuge.mv.tooltip", "Separating Ores more precisely");

        provider.add("gtceu.machine.thermal_centrifuge.hv.tooltip", "Separating Ores more precisely");

        provider.add("gtceu.machine.thermal_centrifuge.ev.tooltip", "Separating Ores more precisely");

        provider.add("gtceu.machine.thermal_centrifuge.iv.tooltip", "Blaze Sweatshop T-6350");

        provider.add("gtceu.machine.thermal_centrifuge.luv.tooltip", "Blaze Sweatshop T-6351");

        provider.add("gtceu.machine.thermal_centrifuge.zpm.tooltip", "Blaze Sweatshop T-6352");

        provider.add("gtceu.machine.thermal_centrifuge.uv.tooltip", "Fire Cyclone");

        provider.add("gtceu.machine.thermal_centrifuge.uhv.tooltip", "Fire Cyclone");

        provider.add("gtceu.machine.thermal_centrifuge.uev.tooltip", "Fire Cyclone");

        provider.add("gtceu.machine.thermal_centrifuge.uiv.tooltip", "Fire Cyclone");

        provider.add("gtceu.machine.thermal_centrifuge.uxv.tooltip", "Fire Cyclone");

        provider.add("gtceu.machine.thermal_centrifuge.opv.tooltip", "Fire Cyclone");

        provider.add("gtceu.machine.wiremill.lv.tooltip", "Produces Wires more efficiently");

        provider.add("gtceu.machine.wiremill.mv.tooltip", "Produces Wires more efficiently");

        provider.add("gtceu.machine.wiremill.hv.tooltip", "Produces Wires more efficiently");

        provider.add("gtceu.machine.wiremill.ev.tooltip", "Produces Wires more efficiently");

        provider.add("gtceu.machine.wiremill.iv.tooltip", "Ingot Elongator");

        provider.add("gtceu.machine.wiremill.luv.tooltip", "Ingot Elongator");

        provider.add("gtceu.machine.wiremill.zpm.tooltip", "Ingot Elongator");

        provider.add("gtceu.machine.wiremill.uv.tooltip", "Wire Transfigurator");

        provider.add("gtceu.machine.wiremill.uhv.tooltip", "Wire Transfigurator");

        provider.add("gtceu.machine.wiremill.uev.tooltip", "Wire Transfigurator");

        provider.add("gtceu.machine.wiremill.uiv.tooltip", "Wire Transfigurator");

        provider.add("gtceu.machine.wiremill.uxv.tooltip", "Wire Transfigurator");

        provider.add("gtceu.machine.wiremill.opv.tooltip", "Wire Transfigurator");

        provider.add("gtceu.machine.circuit_assembler.lv.tooltip", "Pick-n-Place all over the place");

        provider.add("gtceu.machine.circuit_assembler.mv.tooltip", "Pick-n-Place all over the place");

        provider.add("gtceu.machine.circuit_assembler.hv.tooltip", "Pick-n-Place all over the place");

        provider.add("gtceu.machine.circuit_assembler.ev.tooltip", "Pick-n-Place all over the place");

        provider.add("gtceu.machine.circuit_assembler.iv.tooltip", "Electronics Manufacturer");

        provider.add("gtceu.machine.circuit_assembler.luv.tooltip", "Electronics Manufacturer");

        provider.add("gtceu.machine.circuit_assembler.zpm.tooltip", "Electronics Manufacturer");

        provider.add("gtceu.machine.circuit_assembler.uv.tooltip", "Computation Factory");

        provider.add("gtceu.machine.circuit_assembler.uhv.tooltip", "Computation Factory");

        provider.add("gtceu.machine.circuit_assembler.uev.tooltip", "Computation Factory");

        provider.add("gtceu.machine.circuit_assembler.uiv.tooltip", "Computation Factory");

        provider.add("gtceu.machine.circuit_assembler.uxv.tooltip", "Computation Factory");

        provider.add("gtceu.machine.circuit_assembler.opv.tooltip", "Computation Factory");

        provider.add("gtceu.machine.mass_fabricator.lv.tooltip", "UUM Matter * Fabrication Squared");

        provider.add("gtceu.machine.mass_fabricator.mv.tooltip", "UUM Matter * Fabrication Squared");

        provider.add("gtceu.machine.mass_fabricator.hv.tooltip", "UUM Matter * Fabrication Squared");

        provider.add("gtceu.machine.mass_fabricator.ev.tooltip", "UUM Matter * Fabrication Squared");

        provider.add("gtceu.machine.mass_fabricator.iv.tooltip", "Genesis Factory");

        provider.add("gtceu.machine.mass_fabricator.luv.tooltip", "Genesis Factory");

        provider.add("gtceu.machine.mass_fabricator.zpm.tooltip", "Genesis Factory");

        provider.add("gtceu.machine.mass_fabricator.uv.tooltip", "Existence Initiator");

        provider.add("gtceu.machine.mass_fabricator.uhv.tooltip", "Existence Initiator");

        provider.add("gtceu.machine.mass_fabricator.uev.tooltip", "Existence Initiator");

        provider.add("gtceu.machine.mass_fabricator.uiv.tooltip", "Existence Initiator");

        provider.add("gtceu.machine.mass_fabricator.uxv.tooltip", "Existence Initiator");

        provider.add("gtceu.machine.mass_fabricator.opv.tooltip", "Existence Initiator");

        provider.add("gtceu.machine.replicator.lv.tooltip", "Producing the Purest of Elements");

        provider.add("gtceu.machine.replicator.mv.tooltip", "Producing the Purest of Elements");

        provider.add("gtceu.machine.replicator.hv.tooltip", "Producing the Purest of Elements");

        provider.add("gtceu.machine.replicator.ev.tooltip", "Producing the Purest of Elements");

        provider.add("gtceu.machine.replicator.iv.tooltip", "Matter Paster");

        provider.add("gtceu.machine.replicator.luv.tooltip", "Matter Paster");

        provider.add("gtceu.machine.replicator.zpm.tooltip", "Matter Paster");

        provider.add("gtceu.machine.replicator.uv.tooltip", "Elemental Composer");

        provider.add("gtceu.machine.replicator.uhv.tooltip", "Elemental Composer");

        provider.add("gtceu.machine.replicator.uev.tooltip", "Elemental Composer");

        provider.add("gtceu.machine.replicator.uiv.tooltip", "Elemental Composer");

        provider.add("gtceu.machine.replicator.uxv.tooltip", "Elemental Composer");

        provider.add("gtceu.machine.replicator.opv.tooltip", "Elemental Composer");

        provider.add("gtceu.machine.scanner.lv.tooltip", "Scans Materials and other things");

        provider.add("gtceu.machine.scanner.mv.tooltip", "Scans Materials and other things");

        provider.add("gtceu.machine.scanner.hv.tooltip", "Scans Materials and other things");

        provider.add("gtceu.machine.scanner.ev.tooltip", "Scans Materials and other things");

        provider.add("gtceu.machine.scanner.iv.tooltip", "Anomaly Detector");

        provider.add("gtceu.machine.scanner.luv.tooltip", "Anomaly Detector");

        provider.add("gtceu.machine.scanner.zpm.tooltip", "Anomaly Detector");

        provider.add("gtceu.machine.scanner.uv.tooltip", "Electron Microscope");
        provider.add("gtceu.machine.scanner.uhv.tooltip", "Electron Microscope");
        provider.add("gtceu.machine.scanner.uev.tooltip", "Electron Microscope");
        provider.add("gtceu.machine.scanner.uiv.tooltip", "Electron Microscope");
        provider.add("gtceu.machine.scanner.uxv.tooltip", "Electron Microscope");
        provider.add("gtceu.machine.scanner.opv.tooltip", "Electron Microscope");
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
        provider.add("gtceu.machine.transformer_adjustable.description", "Transforms Energy between voltage tiers, now with more Amps!");
        provider.add("gtceu.machine.transformer_adjustable.tooltip_tool_usage", "Starts as §f4A§7, use Screwdriver to change");
        provider.add("gtceu.machine.transformer_adjustable.message_adjust", "Adjusted Hi-Amp to %d EU %dA, Lo-Amp to %d EU %dA");
        provider.add("gtceu.machine.diode.message", "Max Amperage throughput: %s");
        provider.add("gtceu.machine.diode.tooltip_tool_usage", "Hit with a Soft Mallet to change Amperage flow.");
        provider.add("gtceu.machine.diode.tooltip_general", "Allows Energy Flow in one direction and limits Amperage");
        provider.add("gtceu.machine.diode.tooltip_starts_at", "Starts as §f1A§7, use Soft Mallet to change");
        provider.add("gtceu.machine.energy_converter.description", "Converts Energy between EU and FE");
        provider.add("gtceu.machine.energy_converter.tooltip_tool_usage", "Starts as §fFE Converter§7, use Soft Mallet to change");
        provider.add("gtceu.machine.energy_converter.tooltip_conversion_fe", "§cForge Conversion: §f%d FE -> %dA %d EU (%s§f)");
        provider.add("gtceu.machine.energy_converter.message_conversion_fe", "Converting Forge Energy, In: %d FE, Out: %dA %d EU");
        provider.add("gtceu.machine.energy_converter.tooltip_conversion_eu", "§aEU Conversion: §f%dA %d EU (%s§f) -> %d FE");
        provider.add("gtceu.machine.energy_converter.message_conversion_eu", "Converting EU, In: %dA %d EU, Out: %d FE");
        provider.add("gtceu.machine.pump.tooltip", "The best way to empty Oceans!");
        provider.add("gtceu.machine.pump.tooltip_buckets", "§f%d §7ticks per Bucket");
        provider.add("gtceu.machine.item_collector.gui.collect_range", "Collect in %s blocks");
        provider.add("gtceu.machine.item_collector.tooltip", "Collects Items around itself when given a Redstone signal");
        provider.add("gtceu.machine.quantum_chest.tooltip", "Better than Storage Drawers");
        provider.add("gtceu.machine.quantum_chest.items_stored", "Item Amount:");
        provider.add("gtceu.machine.quantum_tank.tooltip", "Compact place to store all your fluids");
        provider.add("gtceu.machine.buffer.tooltip", "A Small Buffer to store Items and Fluids");
        provider.add("tile.hermetic_casing.hermetic_casing_lv.name", "Hermetic Casing I");
        provider.add("tile.hermetic_casing.hermetic_casing_mv.name", "Hermetic Casing II");
        provider.add("tile.hermetic_casing.hermetic_casing_hv.name", "Hermetic Casing III");
        provider.add("tile.hermetic_casing.hermetic_casing_ev.name", "Hermetic Casing IV");
        provider.add("tile.hermetic_casing.hermetic_casing_iv.name", "Hermetic Casing V");
        provider.add("tile.hermetic_casing.hermetic_casing_luv.name", "Hermetic Casing VI");
        provider.add("tile.hermetic_casing.hermetic_casing_zpm.name", "Hermetic Casing VII");
        provider.add("tile.hermetic_casing.hermetic_casing_uv.name", "Hermetic Casing VIII");
        provider.add("tile.hermetic_casing.hermetic_casing_uhv.name", "Hermetic Casing IX");
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
        provider.add("gtceu.advancement.root_steam.name", "Steam Age");
        provider.add("gtceu.advancement.root_steam.desc", "Welcome to GregTech! Everything begins with your first copper ingots.");
        provider.add("gtceu.advancement.steam.1_first_tools.name", "First Tools");
        provider.add("gtceu.advancement.steam.1_first_tools.desc", "Craft a Hammer.");
        provider.add("gtceu.advancement.steam.2_more_tools.name", "More Tools");
        provider.add("gtceu.advancement.steam.2_more_tools.desc", "Craft a Wrench.");
        provider.add("gtceu.advancement.steam.3_bronze_dust.name", "Bronze");
        provider.add("gtceu.advancement.steam.3_bronze_dust.desc", "Craft bronze dust.");
        provider.add("gtceu.advancement.steam.4_bronze_boiler.name", "First Steam");
        provider.add("gtceu.advancement.steam.4_bronze_boiler.desc", "Craft a Bronze Boiler.");
        provider.add("gtceu.advancement.steam.5_bronze_forge_hammer.name", "Cheaper than a Macerator");
        provider.add("gtceu.advancement.steam.5_bronze_forge_hammer.desc", "Craft a Steam Forge Hammer.");
        provider.add("gtceu.advancement.steam.6_bronze_alloy_smelter.name", "Alloy Smelter");
        provider.add("gtceu.advancement.steam.6_bronze_alloy_smelter.desc", "Craft a Steam Alloy Smelter.");
        provider.add("gtceu.advancement.steam.7_bronze_extractor.name", "Extract");
        provider.add("gtceu.advancement.steam.7_bronze_extractor.desc", "Craft a Steam Extractor.");
        provider.add("gtceu.advancement.steam.8_bronze_solar_boiler.name", "Simply Eco");
        provider.add("gtceu.advancement.steam.8_bronze_solar_boiler.desc", "Craft a Solar Boiler.");
        provider.add("gtceu.advancement.steam.9_coke_oven.name", "Coke Oven");
        provider.add("gtceu.advancement.steam.9_coke_oven.desc", "Craft a Coke Oven.");
        provider.add("gtceu.advancement.steam.10_vacuum_tube.name", "Tubes");
        provider.add("gtceu.advancement.steam.10_vacuum_tube.desc", "Craft a Vacuum Tube.");
        provider.add("gtceu.advancement.steam.11_rubber.name", "Rubber");
        provider.add("gtceu.advancement.steam.11_rubber.desc", "Make Rubber in an alloy smelter using Sulfur and Raw Rubber Pulp, obtained from Rubber Trees.");
        provider.add("gtceu.advancement.steam.12_electronic_circuit.name", "Basic Circuit");
        provider.add("gtceu.advancement.steam.12_electronic_circuit.desc", "Craft an Electronic Circuit.");
        provider.add("gtceu.advancement.steam.13_steel.name", "Steel");
        provider.add("gtceu.advancement.steam.13_steel.desc", "Produce Steel in a Primitive Blast Furnace.");
        provider.add("gtceu.advancement.steam.14_magnetic_iron.name", "Magnetic Iron");
        provider.add("gtceu.advancement.steam.14_magnetic_iron.desc", "Craft a Magnetic Iron Rod with 4 Redstone.");
        provider.add("gtceu.advancement.steam.15_lv_motor.name", "Low Voltage Motor");
        provider.add("gtceu.advancement.steam.15_lv_motor.desc", "Craft a Low Voltage Motor.");
        provider.add("gtceu.advancement.steam.16_steel_boiler.name", "High Pressure");
        provider.add("gtceu.advancement.steam.16_steel_boiler.desc", "Craft a High Pressure Boiler.");
        provider.add("gtceu.advancement.steam.81_crafting_station.name", "Crafting Station");
        provider.add("gtceu.advancement.steam.81_crafting_station.desc", "Craft a Crafting Station to make complex crafts much more manageable.");
        provider.add("gtceu.advancement.steam.83_hp_solar_boiler.name", "Solar OP Pls Nerf");
        provider.add("gtceu.advancement.steam.83_hp_solar_boiler.desc", "Craft a High Pressure Solar Boiler.");
        provider.add("gtceu.advancement.steam.85_steam_vent_death.name", "Get out of the way!");
        provider.add("gtceu.advancement.steam.85_steam_vent_death.desc", "Die to a venting Steam Machine.");
        provider.add("gtceu.advancement.steam.87_fluid_pipe_death_heat.name", "Boiling Hot!");
        provider.add("gtceu.advancement.steam.87_fluid_pipe_death_heat.desc", "Die to a Fluid Pipe full of Hot Fluid.");
        provider.add("gtceu.advancement.steam.90_primitive_pump.name", "Primitive Water Pump");
        provider.add("gtceu.advancement.steam.90_primitive_pump.desc", "Craft a Primitive Water Pump for early water gathering.");
        provider.add("gtceu.advancement.root_lv.name", "Low Voltage");
        provider.add("gtceu.advancement.root_lv.desc", "Craft a Basic Steam Turbine");
        provider.add("gtceu.advancement.low_voltage.17_lv_pump.name", "Pump");
        provider.add("gtceu.advancement.low_voltage.17_lv_pump.desc", "Craft an LV Pump.");
        provider.add("gtceu.advancement.low_voltage.18_shutter_cover.name", "Close it!");
        provider.add("gtceu.advancement.low_voltage.18_shutter_cover.desc", "Get a Shutter Cover.");
        provider.add("gtceu.advancement.low_voltage.19_lv_pump_block.name", "Slurp");
        provider.add("gtceu.advancement.low_voltage.19_lv_pump_block.desc", "Craft a Basic Pump.");
        provider.add("gtceu.advancement.low_voltage.20_lv_conveyor.name", "Transport");
        provider.add("gtceu.advancement.low_voltage.20_lv_conveyor.desc", "Craft an LV Conveyor.");
        provider.add("gtceu.advancement.low_voltage.21_machine_controller_cover.name", "Manipulation");
        provider.add("gtceu.advancement.low_voltage.21_machine_controller_cover.desc", "Get a Machine Controller.");
        provider.add("gtceu.advancement.low_voltage.22_lv_robot_arm.name", "Complex Machines");
        provider.add("gtceu.advancement.low_voltage.22_lv_robot_arm.desc", "Craft an LV Robot Arm.");
        provider.add("gtceu.advancement.low_voltage.23_lv_assembler.name", "Avengers, Assemble!");
        provider.add("gtceu.advancement.low_voltage.23_lv_assembler.desc", "Craft an LV Assembler.");
        provider.add("gtceu.advancement.low_voltage.24_smart_filter.name", "Filter and Regulate");
        provider.add("gtceu.advancement.low_voltage.24_smart_filter.desc", "Get a Smart Filter.");
        provider.add("gtceu.advancement.low_voltage.25_large_boiler.name", "Extreme Pressure");
        provider.add("gtceu.advancement.low_voltage.25_large_boiler.desc", "Set up a Large Boiler.");
        provider.add("gtceu.advancement.low_voltage.26_arc_furnace.name", "Recycling");
        provider.add("gtceu.advancement.low_voltage.26_arc_furnace.desc", "Craft an Arc Furnace.");
        provider.add("gtceu.advancement.low_voltage.27_electric_blast_furnace.name", "Electric Blast Furnace");
        provider.add("gtceu.advancement.low_voltage.27_electric_blast_furnace.desc", "Craft an Electric Blast Furnace.");
        provider.add("gtceu.advancement.low_voltage.28_lv_energy_hatch.name", "You Need Two Of Them");
        provider.add("gtceu.advancement.low_voltage.28_lv_energy_hatch.desc", "Craft an LV Energy Hatch.");
        provider.add("gtceu.advancement.low_voltage.29_lv_battery_buffer.name", "Batteries");
        provider.add("gtceu.advancement.low_voltage.29_lv_battery_buffer.desc", "Craft an LV 4A Battery Buffer.");
        provider.add("gtceu.advancement.low_voltage.30_good_electronic_circuit.name", "Better Circuits");
        provider.add("gtceu.advancement.low_voltage.30_good_electronic_circuit.desc", "Get Good Circuits.");
        provider.add("gtceu.advancement.low_voltage.86_electrocution_death.name", "Shoulda Covered your Wires!");
        provider.add("gtceu.advancement.low_voltage.86_electrocution_death.desc", "Die to an Uninsulated Wire.");
        provider.add("gtceu.advancement.low_voltage.88_first_cover_place.name", "The First of Many");
        provider.add("gtceu.advancement.low_voltage.88_first_cover_place.desc", "Place your first Machine Cover.");
        provider.add("gtceu.advancement.root_mv.name", "Medium Voltage");
        provider.add("gtceu.advancement.root_mv.desc", "Produce an Aluminium Ingot.");
        provider.add("gtceu.advancement.medium_voltage.31_mv_energy_hatch.name", "Upgrade Your EBF");
        provider.add("gtceu.advancement.medium_voltage.31_mv_energy_hatch.desc", "Craft an MV Energy Hatch.");
        provider.add("gtceu.advancement.medium_voltage.32_electric_drill.name", "Drill Time");
        provider.add("gtceu.advancement.medium_voltage.32_electric_drill.desc", "Craft a Drill.");
        provider.add("gtceu.advancement.medium_voltage.33_chainsaw.name", "Brrrr...");
        provider.add("gtceu.advancement.medium_voltage.33_chainsaw.desc", "Craft a Chainsaw.");
        provider.add("gtceu.advancement.medium_voltage.34_silicon_boule.name", "Monocrystalline Silicon Boule");
        provider.add("gtceu.advancement.medium_voltage.34_silicon_boule.desc", "Produce a Monocrystalline Silicon Boule.");
        provider.add("gtceu.advancement.medium_voltage.35_logic_circuit_wafer.name", "Logic Circuit Wafer");
        provider.add("gtceu.advancement.medium_voltage.35_logic_circuit_wafer.desc", "Produce a Logic Circuit Wafer.");
        provider.add("gtceu.advancement.medium_voltage.36_integrated_logic_circuit.name", "Integrated Logic Circuit");
        provider.add("gtceu.advancement.medium_voltage.36_integrated_logic_circuit.desc", "Produce an Integrated Logic Circuit.");
        provider.add("gtceu.advancement.medium_voltage.37_advanced_integrated_logic_circuit.name", "Step Forward");
        provider.add("gtceu.advancement.medium_voltage.37_advanced_integrated_logic_circuit.desc", "Obtain Advanced Circuits.");
        provider.add("gtceu.advancement.medium_voltage.38_super_chest.name", "New Storage");
        provider.add("gtceu.advancement.medium_voltage.38_super_chest.desc", "Craft a Super Chest I.");
        provider.add("gtceu.advancement.medium_voltage.39_super_tank.name", "Where is the Ocean?");
        provider.add("gtceu.advancement.medium_voltage.39_super_tank.desc", "Build a Super Tank I.");
        provider.add("gtceu.advancement.root_hv.name", "High Voltage");
        provider.add("gtceu.advancement.root_hv.desc", "Produce a Stainless Steel Ingot.");
        provider.add("gtceu.advancement.high_voltage.40_workstation.name", "Workstations");
        provider.add("gtceu.advancement.high_voltage.40_workstation.desc", "Get Workstations.");
        provider.add("gtceu.advancement.high_voltage.41_vacuum_freezer.name", "Vacuum Freezer");
        provider.add("gtceu.advancement.high_voltage.41_vacuum_freezer.desc", "Set up a Vacuum Freezer.");
        provider.add("gtceu.advancement.high_voltage.42_kanthal_coil.name", "Upgrade your Coils to Level II");
        provider.add("gtceu.advancement.high_voltage.42_kanthal_coil.desc", "Craft a Kanthal Heating Coil.");
        provider.add("gtceu.advancement.high_voltage.43_multi_smelter.name", "High Power Smelter");
        provider.add("gtceu.advancement.high_voltage.43_multi_smelter.desc", "Set up a Multi Smelter.");
        provider.add("gtceu.advancement.high_voltage.44_distillation_tower.name", "Oil Plant");
        provider.add("gtceu.advancement.high_voltage.44_distillation_tower.desc", "Start up a Distillation Tower.");
        provider.add("gtceu.advancement.high_voltage.45_large_steam_turbine.name", "So Much Steam");
        provider.add("gtceu.advancement.high_voltage.45_large_steam_turbine.desc", "Start up a Large Steam Turbine.");
        provider.add("gtceu.advancement.high_voltage.46_hv_macerator.name", "Universal Macerator");
        provider.add("gtceu.advancement.high_voltage.46_hv_macerator.desc", "Craft an HV Macerator for ore byproducts.");
        provider.add("gtceu.advancement.high_voltage.82_large_chemical_reactor.name", "Large Chemical Reactor");
        provider.add("gtceu.advancement.high_voltage.82_large_chemical_reactor.desc", "Set up a Large Chemical Reactor for more efficient chemistry.");
        provider.add("gtceu.advancement.high_voltage.84_rotor_holder_open.name", "A Painful Way to Go Out");
        provider.add("gtceu.advancement.high_voltage.84_rotor_holder_open.desc", "Die by opening a spinning Rotor Holder.");
        provider.add("gtceu.advancement.high_voltage.89_fluid_pipe_death_cold.name", "Freezing Cold!");
        provider.add("gtceu.advancement.high_voltage.89_fluid_pipe_death_cold.desc", "Die to a Fluid Pipe full of Cold Fluid.");
        provider.add("gtceu.advancement.root_ev.name", "Extreme Voltage");
        provider.add("gtceu.advancement.root_ev.desc", "Cool down a Hot Titanium Ingot.");
        provider.add("gtceu.advancement.extreme_voltage.47_nichrome_coil.name", "Upgrade your Coils to Level III");
        provider.add("gtceu.advancement.extreme_voltage.47_nichrome_coil.desc", "Craft a Nichrome Heating Coil.");
        provider.add("gtceu.advancement.extreme_voltage.48_osmium.name", "Osmium");
        provider.add("gtceu.advancement.extreme_voltage.48_osmium.desc", "Cool down a Hot Osmium Ingot.");
        provider.add("gtceu.advancement.extreme_voltage.49_nano_cpu_wafer.name", "Nano CPU Wafer");
        provider.add("gtceu.advancement.extreme_voltage.49_nano_cpu_wafer.desc", "Produce a Nano CPU Wafer.");
        provider.add("gtceu.advancement.extreme_voltage.50_nano_processor.name", "Nano Processor");
        provider.add("gtceu.advancement.extreme_voltage.50_nano_processor.desc", "Get Nano Processors.");
        provider.add("gtceu.advancement.extreme_voltage.51_large_combustion_engine.name", "Large Combustion Engine");
        provider.add("gtceu.advancement.extreme_voltage.51_large_combustion_engine.desc", "Set up a Large Combustion Engine, supply it with Lubricant, and boost it with Oxygen.");
        provider.add("gtceu.advancement.extreme_voltage.52_soc_wafer.name", "SoC Wafer");
        provider.add("gtceu.advancement.extreme_voltage.52_soc_wafer.desc", "Produce an SoC Wafer to make cheaper Basic and Good Circuits.");
        provider.add("gtceu.advancement.root_iv.name", "Insane Voltage");
        provider.add("gtceu.advancement.root_iv.desc", "Cool down a Hot Tungstensteel Ingot.");
        provider.add("gtceu.advancement.insane_voltage.53_plutonium_239.name", "Plutonium 239");
        provider.add("gtceu.advancement.insane_voltage.53_plutonium_239.desc", "Obtain Plutonium 239 for a source of radon.");
        provider.add("gtceu.advancement.insane_voltage.54_indium.name", "Indium");
        provider.add("gtceu.advancement.insane_voltage.54_indium.desc", "Obtain Indium from Sphalerite and Galena.");
        provider.add("gtceu.advancement.insane_voltage.55_qbit_cpu_wafer.name", "QBit CPU Wafer");
        provider.add("gtceu.advancement.insane_voltage.55_qbit_cpu_wafer.desc", "Produce a QBit CPU Wafer.");
        provider.add("gtceu.advancement.insane_voltage.56_quantum_processor.name", "Quantum Processor");
        provider.add("gtceu.advancement.insane_voltage.56_quantum_processor.desc", "Get Quantum Processors.");
        provider.add("gtceu.advancement.insane_voltage.57_tungstensteel_coil.name", "Upgrade your Coils to Level IV");
        provider.add("gtceu.advancement.insane_voltage.57_tungstensteel_coil.desc", "Craft a Tungstensteel Heating Coil.");
        provider.add("gtceu.advancement.insane_voltage.58_hss_g_coil.name", "Upgrade your Coils to Level V");
        provider.add("gtceu.advancement.insane_voltage.58_hss_g_coil.desc", "Craft an HSS-G Heating Coil.");
        provider.add("gtceu.advancement.root_luv.name", "Ludicrous Voltage");
        provider.add("gtceu.advancement.root_luv.desc", "Set up an Assembly Line.");
        provider.add("gtceu.advancement.ludicrous_voltage.59_superconducting_coil.name", "Conducting");
        provider.add("gtceu.advancement.ludicrous_voltage.59_superconducting_coil.desc", "Craft a Superconducting Coil.");
        provider.add("gtceu.advancement.ludicrous_voltage.60_fusion.name", "Fusion Reactor");
        provider.add("gtceu.advancement.ludicrous_voltage.60_fusion.desc", "Set up a Fusion Reactor Mark 1.");
        provider.add("gtceu.advancement.ludicrous_voltage.61_europium.name", "Advancement in Technology");
        provider.add("gtceu.advancement.ludicrous_voltage.61_europium.desc", "Produce Europium.");
        provider.add("gtceu.advancement.ludicrous_voltage.62_raw_crystal_chip.name", "Raw Crystal Chip");
        provider.add("gtceu.advancement.ludicrous_voltage.62_raw_crystal_chip.desc", "Produce a Raw Crystal Chip.");
        provider.add("gtceu.advancement.ludicrous_voltage.63_crystal_processing_unit.name", "Crystal Processing Unit");
        provider.add("gtceu.advancement.ludicrous_voltage.63_crystal_processing_unit.desc", "Produce a Crystal Processing Unit.");
        provider.add("gtceu.advancement.ludicrous_voltage.64_crystal_processor.name", "Crystal Processor");
        provider.add("gtceu.advancement.ludicrous_voltage.64_crystal_processor.desc", "Get Crystal Processors.");
        provider.add("gtceu.advancement.ludicrous_voltage.65_naquadah.name", "Stargate Material");
        provider.add("gtceu.advancement.ludicrous_voltage.65_naquadah.desc", "Cool down a Hot Naquadah Ingot.");
        provider.add("gtceu.advancement.ludicrous_voltage.66_naquadah_coil.name", "Upgrade your Coils to Level VI");
        provider.add("gtceu.advancement.ludicrous_voltage.66_naquadah_coil.desc", "Craft a Naquadah Heating Coil.");
        provider.add("gtceu.advancement.ludicrous_voltage.67_asoc_wafer.name", "ASoC Wafer");
        provider.add("gtceu.advancement.ludicrous_voltage.67_asoc_wafer.desc", "Produce an ASoC Wafer to make cheaper Extreme and Advanced Circuits.");
        provider.add("gtceu.advancement.ludicrous_voltage.68_large_plasma_turbine.name", "Large Plasma Turbine");
        provider.add("gtceu.advancement.ludicrous_voltage.68_large_plasma_turbine.desc", "Craft a Plasma Turbine to turn Plasma into Usable Fluid.");
        provider.add("gtceu.advancement.root_zpm.name", "Zero Point Module");
        provider.add("gtceu.advancement.root_zpm.desc", "Set up a Fusion Reactor Mark 2.");
        provider.add("gtceu.advancement.zero_point_module.69_americium.name", "Going for the Limit");
        provider.add("gtceu.advancement.zero_point_module.69_americium.desc", "Produce Americium.");
        provider.add("gtceu.advancement.zero_point_module.70_stem_cells.name", "Stem Cells");
        provider.add("gtceu.advancement.zero_point_module.70_stem_cells.desc", "Produce Stem Cells.");
        provider.add("gtceu.advancement.zero_point_module.71_neuro_processing_unit.name", "Neuro Processing Unit");
        provider.add("gtceu.advancement.zero_point_module.71_neuro_processing_unit.desc", "Produce a Neuro Processing Unit.");
        provider.add("gtceu.advancement.zero_point_module.72_wetware_processor.name", "Wetware Processor");
        provider.add("gtceu.advancement.zero_point_module.72_wetware_processor.desc", "Get Wetware Processors.");
        provider.add("gtceu.advancement.zero_point_module.73_trinium_coil.name", "Over 9000!");
        provider.add("gtceu.advancement.zero_point_module.73_trinium_coil.desc", "Craft a Trinium Heating Coil.");
        provider.add("gtceu.advancement.root_uv.name", "Ultimate Voltage");
        provider.add("gtceu.advancement.root_uv.desc", "Produce Tritanium.");
        provider.add("gtceu.advancement.ultimate_voltage.74_wetware_mainframe.name", "Wetware Mainframe");
        provider.add("gtceu.advancement.ultimate_voltage.74_wetware_mainframe.desc", "Get a Wetware Mainframe.");
        provider.add("gtceu.advancement.ultimate_voltage.75_fusion_reactor_3.name", "A Sun Down on Earth");
        provider.add("gtceu.advancement.ultimate_voltage.75_fusion_reactor_3.desc", "Set up a Fusion Reactor Mark 3.");
        provider.add("gtceu.advancement.ultimate_voltage.76_neutronium.name", "As Dense As Possible");
        provider.add("gtceu.advancement.ultimate_voltage.76_neutronium.desc", "Produce Neutronium.");
        provider.add("gtceu.advancement.ultimate_voltage.77_ultimate_battery.name", "What Now?");
        provider.add("gtceu.advancement.ultimate_voltage.77_ultimate_battery.desc", "Craft an Ultimate Battery.");
        provider.add("gtceu.advancement.ultimate_voltage.78_hasoc_wafer.name", "HASoC Wafer");
        provider.add("gtceu.advancement.ultimate_voltage.78_hasoc_wafer.desc", "Produce an HASoC Wafer to make cheaper Master Circuits.");
        provider.add("gtceu.advancement.ultimate_voltage.79_tritanium_coil.name", "The Final Coil");
        provider.add("gtceu.advancement.ultimate_voltage.79_tritanium_coil.desc", "Craft a Tritanium Heating Coil.");
        provider.add("behaviour.softhammer", "Activates and Deactivates Machines");
        provider.add("behaviour.hammer", "Turns on and off Muffling for Machines (by hitting them)");
        provider.add("behaviour.wrench", "Rotates Blocks on Rightclick");
        provider.add("behaviour.boor.by", "by %s");
        provider.add("behaviour.paintspray.solvent.tooltip", "Can remove color from things");
        provider.add("behaviour.paintspray.white.tooltip", "Can paint things in White");
        provider.add("behaviour.paintspray.orange.tooltip", "Can paint things in Orange");
        provider.add("behaviour.paintspray.magenta.tooltip", "Can paint things in Magenta");
        provider.add("behaviour.paintspray.lightBlue.tooltip", "Can paint things in Light Blue");
        provider.add("behaviour.paintspray.yellow.tooltip", "Can paint things in Yellow");
        provider.add("behaviour.paintspray.lime.tooltip", "Can paint things in Lime");
        provider.add("behaviour.paintspray.pink.tooltip", "Can paint things in Pink");
        provider.add("behaviour.paintspray.gray.tooltip", "Can paint things in Gray");
        provider.add("behaviour.paintspray.silver.tooltip", "Can paint things in Light Gray");
        provider.add("behaviour.paintspray.cyan.tooltip", "Can paint things in Cyan");
        provider.add("behaviour.paintspray.purple.tooltip", "Can paint things in Purple");
        provider.add("behaviour.paintspray.blue.tooltip", "Can paint things in Blue");
        provider.add("behaviour.paintspray.brown.tooltip", "Can paint things in Brown");
        provider.add("behaviour.paintspray.green.tooltip", "Can paint things in Green");
        provider.add("behaviour.paintspray.red.tooltip", "Can paint things in Red");
        provider.add("behaviour.paintspray.black.tooltip", "Can paint things in Black");
        provider.add("behaviour.paintspray.uses", "Remaining Uses: %d");
        provider.add("behaviour.prospecting", "Usable for Prospecting");


        provider.add("gtceu.machine.electric_blast_furnace.tooltip.1", "For every §f900K§7 above the recipe temperature, a multiplicative §f95%%§7 energy multiplier is applied pre-overclocking.");
        provider.add("gtceu.machine.electric_blast_furnace.tooltip.2", "For every §f1800K§7 above the recipe temperature, one overclock becomes §f100%% efficient§7 (perfect overclock).");
        provider.add("gtceu.machine.electric_blast_furnace.tooltip.3", "For every voltage tier above §bMV§7, temperature is increased by §f100K§7.");


        provider.add("gtceu.machine.pyrolyse_oven.tooltip.1", "§6Cupronickel §7coils are §f25%%§7 slower. Every coil after §bKanthal§7 increases speed by §f50%%§7.");


        provider.add("gtceu.machine.cracker.tooltip.1", "Every coil after §6Cupronickel§7 reduces energy usage by §f10%%§7.");


        provider.add("gtceu.machine.coke_oven_hatch.tooltip", "Allows automation access for the Coke Oven.");


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
        provider.add("metaitem.cover.digital.mode.proxy.disabled", "Click to enable Proxy Mode");
        provider.add("metaitem.cover.digital.mode.proxy.enabled", "Proxy Mode enabled");
        provider.add("metaitem.cover.digital.mode.machine.disabled", "Click to enable Machine Mode");
        provider.add("metaitem.cover.digital.mode.machine.enabled", "Machine Mode enabled");
        provider.add("metaitem.cover.digital.mode.energy.disabled", "Click to enable Energy Mode");
        provider.add("metaitem.cover.digital.mode.energy.enabled", "Energy Mode enabled");
        provider.add("metaitem.cover.digital.mode.item.disabled", "Click to enable Item Mode");
        provider.add("metaitem.cover.digital.mode.item.enabled", "Item Mode enabled");
        provider.add("metaitem.cover.digital.mode.fluid.disabled", "Click to enable Fluid Mode");
        provider.add("metaitem.cover.digital.mode.fluid.enabled", "Fluid Mode enabled");

        provider.add("gtceu.machine.primitive_water_pump.tooltip", "Endervoir at Home");
        provider.add("gtceu.machine.primitive_blast_furnace.bronze.tooltip", "Making your first Steel");
        provider.add("gtceu.machine.electric_blast_furnace.tooltip", "Where's the electric smoker?");
        provider.add("gtceu.machine.vacuum_freezer.tooltip", "Aluminium Ice Box");
        provider.add("gtceu.machine.implosion_compressor.tooltip", "The only Machine you want to go Boom");
        provider.add("gtceu.machine.pyrolyse_oven.tooltip", "Electric Coke Oven");
        provider.add("gtceu.machine.distillation_tower.tooltip", "Fluid Refinery");
        provider.add("gtceu.machine.multi_furnace.tooltip", "Just like the Oven at Home");
        provider.add("gtceu.machine.large_combustion_engine.tooltip", "Fuel Ignition Chamber");
        provider.add("gtceu.machine.extreme_combustion_engine.tooltip", "Extreme Chemical Energy Releaser");
        provider.add("gtceu.machine.cracker.tooltip", "Makes Oil useful");
        provider.add("gtceu.machine.large_turbine.steam.tooltip", "Do not put your Head in it");
        provider.add("gtceu.machine.large_turbine.gas.tooltip", "Not a Jet Engine");
        provider.add("gtceu.machine.large_turbine.plasma.tooltip", "Plasma Energy Siphon");
        provider.add("gtceu.machine.large_boiler.bronze.tooltip", "We need more Steam!");
        provider.add("gtceu.machine.large_boiler.steel.tooltip", "Charcoal Incinerator");
        provider.add("gtceu.machine.large_boiler.titanium.tooltip", "Where's the Magic Super Fuel?");
        provider.add("gtceu.machine.large_boiler.tungstensteel.tooltip", "How do you even fuel this thing?");
        provider.add("gtceu.machine.coke_oven.tooltip", "Making better fuels for Steel and Power");
        provider.add("gtceu.machine.assembly_line.tooltip", "Not a multiblock Assembling Machine!");
        provider.add("gtceu.machine.fusion_reactor.luv.tooltip", "Atomic Alloy Smelter");
        provider.add("gtceu.machine.fusion_reactor.zpm.tooltip", "A SUN DOWN ON EARTH");
        provider.add("gtceu.machine.fusion_reactor.uv.tooltip", "A WHITE DWARF DOWN ON YOUR BASE");
        provider.add("gtceu.machine.large_chemical_reactor.tooltip", "Black Box Reactor");
        provider.add("gtceu.machine.steam_oven.tooltip", "Not to be confused with Multi-Smelter");
        provider.add("gtceu.machine.steam_grinder.tooltip", "A multiblock Macerator without the Byproducts");
        provider.add("gtceu.machine.large_miner.ev.tooltip", "Digging Ore instead of You");
        provider.add("gtceu.machine.large_miner.iv.tooltip", "Biome Excavator");
        provider.add("gtceu.machine.large_miner.luv.tooltip", "Terrestrial Harvester");
        provider.add("gtceu.machine.central_monitor.tooltip", "But can it run Doom?");
        provider.add("gtceu.machine.processing_array.tooltip", "When a few Machines just doesn't cut it");
        provider.add("gtceu.machine.advanced_processing_array.tooltip", "Parallelize the World");
        provider.add("gtceu.machine.fluid_drilling_rig.mv.tooltip", "Oil Extraction Pump");
        provider.add("gtceu.machine.fluid_drilling_rig.hv.tooltip", "Does not perform Fracking");
        provider.add("gtceu.machine.fluid_drilling_rig.ev.tooltip", "Well Drainer");
        provider.add("gtceu.machine.cleanroom.tooltip", "Keeping those pesky particles out");
        provider.add("gtceu.machine.charcoal_pile.tooltip", "Underground fuel bakery");
        provider.add("gtceu.machine.item_bus.import.tooltip", "Item Input for Multiblocks");
        provider.add("gtceu.universal.disabled", "Multiblock Sharing §4Disabled");
        provider.add("gtceu.universal.enabled", "Multiblock Sharing §aEnabled");


        provider.add("gtceu.machine.item_bus.export.tooltip", "Item Output for Multiblocks");


        provider.add("gtceu.bus.collapse_true", "Bus will collapse Items");
        provider.add("gtceu.bus.collapse_false", "Bus will not collapse Items");
        provider.add("gtceu.bus.collapse.error", "Bus must be attached to multiblock first");
        provider.add("gtceu.machine.fluid_hatch.import.tooltip", "Fluid Input for Multiblocks");


        provider.add("gtceu.machine.fluid_hatch.export.tooltip", "Fluid Output for Multiblocks");


        provider.add("gtceu.machine.energy_hatch.input.tooltip", "Energy Input for Multiblocks");


        provider.add("gtceu.machine.energy_hatch.input_hi_amp.tooltip", "Multiple Ampere Energy Input for Multiblocks");


        provider.add("gtceu.machine.energy_hatch.output.tooltip", "Energy Output for Multiblocks");


        provider.add("gtceu.machine.energy_hatch.output_hi_amp.tooltip", "Multiple Ampere Energy Output for Multiblocks");


        multiLang(provider, "gtceu.machine.rotor_holder.tooltip", "Rotor Holder for Multiblocks", "Holds Rotor in place so it will not fly away");


        provider.add("gtceu.machine.maintenance_hatch.tooltip", "For maintaining Multiblocks");

        multilineLang(provider, "gtceu.machine.maintenance_hatch_configurable.tooltip", "For finer control over Multiblocks\nStarts with no Maintenance problems!");

        provider.add("gtceu.machine.maintenance_hatch_full_auto.tooltip", "For automatically maintaining Multiblocks");

        multiLang(provider, "gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip", "For automatically maintaining Multiblocks with Cleaning!", "Cleans as:");
        provider.add("gtceu.machine.maintenance_hatch_tool_slot.tooltip", "Click slot with empty hand when required tools are in inventory to solve problems");
        provider.add("gtceu.machine.maintenance_hatch_tape_slot.tooltip", "Insert Tape to prevent problems");
        provider.add("gtceu.maintenance.configurable_duration", "Duration: %fx");
        provider.add("gtceu.maintenance.configurable_duration.unchanged_description", "Recipes will run at normal speed. Change configuration to update.");
        provider.add("gtceu.maintenance.configurable_duration.changed_description", "Recipes will run with %fx duration, applied before overclocking.");
        provider.add("gtceu.maintenance.configurable_time", "Time: %fx");
        provider.add("gtceu.maintenance.configurable_time.unchanged_description", "Maintenance problems will occur at normal rate. Change configuration to update.");
        provider.add("gtceu.maintenance.configurable_time.changed_description", "Maintenance problems will occur at %fx the normal rate.");
        multiLang(provider, "gtceu.machine.muffler_hatch.tooltip", "Recovers waste from machines", "DO NOT OBSTRUCT THE OUTPUT!");
        provider.add("gtceu.muffler.recovery_tooltip", "§bRecovery Chance: §f%d%%");
        provider.add("gtceu.machine.pump_hatch.tooltip", "Primitive Fluid Output for Water Pump");
        provider.add("gtceu.machine.machine_hatch.locked", "Machine Interface Locked");
        provider.add("gtceu.machine.machine_hatch.tooltip", "Specialized Access Bus that only holds valid items");
        provider.add("gtceu.machine.machine_hatch.processing_array", "When in the §eProcessing Array§7, only holds machines that work in the §eProcessing Array");
        provider.add("gtceu.machine.passthrough_hatch_item.tooltip", "Sends Items from one Side to the other");
        provider.add("gtceu.machine.passthrough_hatch_fluid.tooltip", "Sends Fluids from one Side to the other");
        provider.add("gtceu.machine.fluid_tank.max_multiblock", "Max Multiblock Size: %dx%dx%d");
        provider.add("gtceu.machine.fluid_tank.fluid", "Contains %s L of %s");
        provider.add("gtceu.universal.tooltip.voltage_in", "§aVoltage IN: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.max_voltage_in", "§aMax Voltage IN: §f%d (%s§f)");
        provider.add("gtceu.universal.tooltip.voltage_out", "§aVoltage OUT: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.max_voltage_out", "§aMax Voltage OUT: §f%d (%s§f)");
        provider.add("gtceu.universal.tooltip.voltage_in_out", "§aVoltage IN/OUT: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.max_voltage_in_out", "§aMax Voltage IN/OUT: §f%d EU/t (%s§f)");
        provider.add("gtceu.universal.tooltip.amperage_in", "§eAmperage IN: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_in_till", "§eAmperage IN up to: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_out", "§eAmperage OUT: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_out_till", "§eAmperage OUT up to: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_in_out", "§eAmperage IN/OUT: §f%dA");
        provider.add("gtceu.universal.tooltip.amperage_in_out_till", "§eAmperage IN/OUT up to: §f%dA");
        provider.add("gtceu.universal.tooltip.energy_storage_capacity", "§cEnergy Capacity: §r%d EU");
        provider.add("gtceu.universal.tooltip.energy_tier_range", "§aAllowed Voltage Tiers: §f%s §f- %s");
        provider.add("gtceu.universal.tooltip.item_storage_capacity", "§6Item Slots: §f%d");
        provider.add("gtceu.universal.tooltip.item_storage_total", "§6Item Capacity: §f%d items");
        provider.add("gtceu.universal.tooltip.item_stored", "§dItem Stored: §f%s, %d items");
        provider.add("gtceu.universal.tooltip.item_transfer_rate", "§bTransfer Rate: §f%d items/s");
        provider.add("gtceu.universal.tooltip.item_transfer_rate_stacks", "§bTransfer Rate: §f%d stacks/s");
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity", "§9Fluid Capacity: §f%d L");
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity_mult", "§9Fluid Capacity: §f%d §7Tanks, §f%d L §7each");
        provider.add("gtceu.universal.tooltip.fluid_stored", "§dFluid Stored: §f%s, %d L");
        provider.add("gtceu.universal.tooltip.fluid_transfer_rate", "§bTransfer Rate: §f%d L/t");
        provider.add("gtceu.universal.tooltip.parallel", "§dMax Parallel: §f%d");
        provider.add("gtceu.universal.tooltip.working_area", "§bWorking Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.working_area_max", "§bMax Working Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.working_area_chunks_max", "§bMax Working Area: §f%dx%d Chunks");
        provider.add("gtceu.universal.tooltip.uses_per_tick", "Uses §f%d EU/t §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_tick_steam", "Uses §f%d L/t §7of Steam while working");
        provider.add("gtceu.universal.tooltip.uses_per_hour_lubricant", "Uses §f%d L/hr §7of Lubricant while working");
        provider.add("gtceu.universal.tooltip.uses_per_second", "Uses §f%d EU/s §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_op", "Uses §f%d EU/operation");
        provider.add("gtceu.universal.tooltip.base_production_eut", "§eBase Production: §f%d EU/t");
        provider.add("gtceu.universal.tooltip.base_production_fluid", "§eBase Production: §f%d L/t");
        provider.add("gtceu.universal.tooltip.produces_fluid", "§eProduces: §f%d L/t");
        provider.add("gtceu.universal.tooltip.terrain_resist", "This Machine will not explode when exposed to the Elements");
        provider.add("gtceu.universal.tooltip.requires_redstone", "§4Requires Redstone power");
        provider.add("gtceu.recipe.total", "Total: %d EU");
        provider.add("gtceu.recipe.eu", "Usage: %d EU/t (%s§r)");
        provider.add("gtceu.recipe.eu_inverted", "Generation: %d EU/t");
        provider.add("gtceu.recipe.duration", "Duration: %.2f secs");
        provider.add("gtceu.recipe.amperage", "Amperage: %d");
        provider.add("gtceu.recipe.not_consumed", "Does not get consumed in the process");
        provider.add("gtceu.recipe.chance", "Chance: %s%% +%s%%/tier");
        provider.add("gtceu.recipe.temperature", "Temperature: %dK");
        provider.add("gtceu.recipe.explosive", "Explosive: %s");
        provider.add("gtceu.recipe.eu_to_start", "Energy To Start: %sEU");
        provider.add("gtceu.recipe.dimensions", "Dimensions: %s");
        provider.add("gtceu.recipe.cleanroom", "Requires %s");
        provider.add("gtceu.recipe.cleanroom.display_name", "Cleanroom");
        provider.add("gtceu.recipe.cleanroom_sterile.display_name", "Sterile Cleanroom");
        provider.add("gtceu.fluid.click_to_fill", "§7Click with a Fluid Container to §bfill §7the tank (Shift-click for a full stack).");
        provider.add("gtceu.fluid.click_combined", "§7Click with a Fluid Container to §cempty §7or §bfill §7the tank (Shift-click for a full stack).");
        provider.add("gtceu.fluid.click_to_empty", "§7Click with a Fluid Container to §cempty §7the tank (Shift-click for a full stack).");
        provider.add("gtceu.tool_action.show_tooltips", "Hold SHIFT to show Tool Info");
        provider.add("gtceu.tool_action.screwdriver.auto_output_covers", "§8Use Screwdriver to Allow Input from Output Side or access Covers");
        provider.add("gtceu.tool_action.screwdriver.toggle_mode_covers", "§8Use Screwdriver to toggle Modes or access Covers");
        provider.add("gtceu.tool_action.screwdriver.access_covers", "§8Use Screwdriver to access Covers");
        provider.add("gtceu.tool_action.screwdriver.auto_collapse", "§8Use Screwdriver to toggle Item collapsing");
        provider.add("gtceu.tool_action.screwdriver.auto_output", "§8Use Screwdriver to toggle Auto-Output");
        provider.add("gtceu.tool_action.screwdriver.toggle_mode", "§8Use Screwdriver to toggle Modes");
        provider.add("gtceu.tool_action.wrench.set_facing", "§8Use Wrench to set Facing");
        provider.add("gtceu.tool_action.wrench.connect", "§8Use Wrench to set Connections, sneak to block Connections");
        provider.add("gtceu.tool_action.wire_cutter.connect", "§8Use Wire Cutters to set Connections");
        provider.add("gtceu.tool_action.soft_mallet.reset", "§8Use Soft Mallet to toggle Working");
        provider.add("gtceu.tool_action.soft_mallet.toggle_mode", "§8Use Soft Mallet to toggle Modes");
        provider.add("gtceu.tool_action.hammer", "§8Use Hard Hammer to muffle Sounds");
        provider.add("gtceu.tool_action.crowbar", "§8Use Crowbar to remove Covers");
        provider.add("gtceu.tool_action.tape", "§8Use Tape to fix Maintenance Problems");
        provider.add("gtceu.fluid.generic", "%s");
        provider.add("gtceu.fluid.plasma", "%s Plasma");
        provider.add("gtceu.fluid.empty", "Empty");
        provider.add("gtceu.fluid.amount", "§9Amount: %d/%d L");
        provider.add("gtceu.fluid.temperature", "§cTemperature: %d K");
        provider.add("gtceu.fluid.temperature.cryogenic", "§bCryogenic! Handle with care!");
        provider.add("gtceu.fluid.state_gas", "§aState: Gaseous");
        provider.add("gtceu.fluid.state_liquid", "§aState: Liquid");
        provider.add("gtceu.fluid.state_plasma", "§aState: Plasma");
        provider.add("gtceu.fluid.type_acid.tooltip", "§6Acidic! Handle with care!");
        provider.add("gtceu.gui.fuel_amount", "Fuel Amount:");
        provider.add("gtceu.gui.fluid_amount", "Fluid Amount:");
        provider.add("gtceu.gui.toggle_view.disabled", "Toggle View (Fluids)");
        provider.add("gtceu.gui.toggle_view.enabled", "Toggle View (Items)");
        multilineLang(provider, "gtceu.gui.overclock.enabled", "Overclocking Enabled.\nClick to Disable");
        multilineLang(provider, "gtceu.gui.overclock.disabled", "Overclocking Disabled.\nClick to Enable");
        multilineLang(provider, "gtceu.gui.overclock.description", "Overclock Button\n§7Recipes can overclock up to the set tier");
        provider.add("gtceu.gui.overclock.off", "X");
        provider.add("gtceu.gui.sort", "Sort");
        provider.add("gtceu.gui.fluid_auto_output.tooltip.enabled", "Fluid Auto-Output Enabled");
        provider.add("gtceu.gui.fluid_auto_output.tooltip.disabled", "Fluid Auto-Output Disabled");
        provider.add("gtceu.gui.fluid_auto_input.tooltip.enabled", "Fluid Auto-Output Enabled");
        provider.add("gtceu.gui.fluid_auto_input.tooltip.disabled", "Fluid Auto-Output Disabled");
        provider.add("gtceu.gui.item_auto_output.tooltip.enabled", "Item Auto-Output Enabled");
        provider.add("gtceu.gui.item_auto_output.tooltip.disabled", "Item Auto-Output Disabled");
        provider.add("gtceu.gui.item_auto_input.tooltip.enabled", "Item Auto-Output Enabled");
        provider.add("gtceu.gui.item_auto_input.tooltip.disabled", "Item Auto-Output Disabled");
        multilineLang(provider, "gtceu.gui.charger_slot.tooltip", "§fCharger Slot§r\n§7Draws power from %s batteries§r\n§7Charges %s tools and batteries");
        multilineLang(provider, "gtceu.gui.configurator_slot.tooltip", "§fConfigurator Slot§r\n§7Place a §6Programmed Circuit§7 in this slot to\n§7change its configured value.\n§7Hold §6Shift§7 when clicking buttons to change by §65.\n§aA Programmed Circuit in this slot is also valid for recipe inputs.§r");
        provider.add("gtceu.gui.fluid_lock.tooltip.enabled", "Fluid Locking Enabled");
        provider.add("gtceu.gui.fluid_lock.tooltip.disabled", "Fluid Locking Disabled");
        provider.add("gtceu.gui.fluid_voiding_partial.tooltip.enabled", "Fluid Voiding Enabled");
        provider.add("gtceu.gui.fluid_voiding_partial.tooltip.disabled", "Fluid Voiding Disabled");
        provider.add("gtceu.gui.item_lock.tooltip.enabled", "Item Locking Enabled");
        provider.add("gtceu.gui.item_lock.tooltip.disabled", "Item Locking Disabled");
        provider.add("gtceu.gui.item_voiding_partial.tooltip.enabled", "Item Voiding Enabled");
        provider.add("gtceu.gui.item_voiding_partial.tooltip.disabled", "Item Voiding Disabled");
        multilineLang(provider, "gtceu.gui.silktouch.enabled", "Silk Touch Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.silktouch.disabled", "Silk Touch Disabled: Click to Enable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.chunkmode.enabled", "Chunk Mode Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.chunkmode.disabled", "Chunk Mode Disabled: Click to Enable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.multiblock_item_voiding", "Voiding Mode\n§7Voiding §6Items");
        multilineLang(provider, "gtceu.gui.multiblock_fluid_voiding", "Voiding Mode\n§7Voiding §9Fluids");
        multilineLang(provider, "gtceu.gui.multiblock_item_fluid_voiding", "Voiding Mode\n§7Voiding §6Items §7and §9Fluids");
        multilineLang(provider, "gtceu.gui.multiblock_no_voiding", "Voiding Mode\n§7Voiding Nothing");
        provider.add("ore.spawnlocation.name", "Ore Spawn Information");
        multiLang(provider, "gtceu.jei.ore.surface_rock", "Surface Rocks with this material denote vein spawn locations.", "They can be broken for 3 Tiny Piles of the dust, with Fortune giving a bonus.");
        provider.add("gtceu.jei.ore.biome_weighting_title", "§dModified Biome Total Weights:");
        provider.add("gtceu.jei.ore.biome_weighting", "§d%s Weight: §3%d");
        provider.add("gtceu.jei.ore.biome_weighting_no_spawn", "§d%s Weight: §cCannot Spawn");
        provider.add("gtceu.jei.ore.ore_weight", "Weight in vein: %d%%");
        multiLang(provider, "gtceu.jei.ore.primary", "Top Ore", "Spawns in the top %d layers of the vein");
        multiLang(provider, "gtceu.jei.ore.secondary", "Bottom Ore", "Spawns in the bottom %d layers of the vein");
        multiLang(provider, "gtceu.jei.ore.between", "Between Ore", "Spawns in the middle %d layers of the vein, with other ores");
        multiLang(provider, "gtceu.jei.ore.sporadic", "Sporadic Ore", "Spawns anywhere in the vein");
        provider.add("fluid.spawnlocation.name", "Fluid Vein Information");
        provider.add("gtceu.jei.fluid.vein_weight", "Vein Weight: %d");
        provider.add("gtceu.jei.fluid.min_yield", "Minimum Yield: %d");
        provider.add("gtceu.jei.fluid.max_yield", "Maximum Yield: %d");
        provider.add("gtceu.jei.fluid.depletion_chance", "Depletion Chance: %d%%");
        provider.add("gtceu.jei.fluid.depletion_amount", "Depletion Amount: %d");
        provider.add("gtceu.jei.fluid.depleted_rate", "Depleted Yield: %d");
        provider.add("gtceu.jei.fluid.dimension", "Dimensions:");
        provider.add("gtceu.jei.fluid.weight_hover", "The Weight of the vein. Hover over the fluid to see any possible biome modifications");
        provider.add("gtceu.jei.fluid.min_hover", "The minimum yield that any fluid vein of this fluid can have");
        provider.add("gtceu.jei.fluid.max_hover", "The maximum yield that any fluid vein of this fluid can have");
        provider.add("gtceu.jei.fluid.dep_chance_hover", "The percentage chance for the vein to be depleted upon harvest");
        provider.add("gtceu.jei.fluid.dep_amount_hover", "The amount the vein will be depleted by");
        provider.add("gtceu.jei.fluid.dep_yield_hover", "The maximum yield of the vein when it is fully depleted");
        provider.add("gtceu.jei.materials.average_mass", "Average mass: %d");
        provider.add("gtceu.jei.materials.average_protons", "Average protons: %d");
        provider.add("gtceu.jei.materials.average_neutrons", "Average neutrons: %d");
        provider.add("gtceu.item_filter.empty_item", "Empty (No Item)");
        provider.add("gtceu.item_filter.footer", "§eClick with item to override");
        provider.add("gtceu.cable.voltage", "Max Voltage: §a%d §a(%s§a)");
        provider.add("gtceu.cable.amperage", "Max Amperage: §e%d");
        provider.add("gtceu.cable.loss_per_block", "Loss/Meter/Ampere: §c%d§7 EU-Volt");
        provider.add("gtceu.cable.superconductor", "§d%s Superconductor");
        provider.add("gtceu.fluid_pipe.capacity", "§9Capacity: §f%d L");
        provider.add("gtceu.fluid_pipe.max_temperature", "§cTemperature Limit: §f%d K");
        provider.add("gtceu.fluid_pipe.channels", "§eChannels: §f%d");
        provider.add("gtceu.fluid_pipe.gas_proof", "§6Can handle Gases");
        provider.add("gtceu.fluid_pipe.acid_proof", "§6Can handle Acids");
        provider.add("gtceu.fluid_pipe.cryo_proof", "§6Can handle Cryogenics");
        provider.add("gtceu.fluid_pipe.plasma_proof", "§6Can handle all Plasmas");
        provider.add("gtceu.fluid_pipe.not_gas_proof", "§4Gases may leak!");
        provider.add("gtceu.item_pipe.priority", "§9Priority: §f%d");
        provider.add("gtceu.multiblock.work_paused", "Work Paused.");
        provider.add("gtceu.multiblock.running", "Running perfectly.");
        provider.add("gtceu.multiblock.idling", "Idling.");
        provider.add("gtceu.multiblock.not_enough_energy", "WARNING: Machine needs more energy.");
        provider.add("gtceu.multiblock.progress", "Progress: %s%%");
        provider.add("gtceu.multiblock.invalid_structure", "Invalid structure.");
        provider.add("gtceu.multiblock.invalid_structure.tooltip", "This block is a controller of the multiblock structure. For building help, see structure template in JEI.");
        provider.add("gtceu.multiblock.validation_failed", "Invalid amount of inputs/outputs.");
        provider.add("gtceu.multiblock.max_energy_per_tick", "Max EU/t: §a%s (%s§r)");
        provider.add("gtceu.multiblock.generation_eu", "Outputting: §a%s EU/t");
        provider.add("gtceu.multiblock.universal.no_problems", "No Maintenance Problems!");
        provider.add("gtceu.multiblock.universal.has_problems", "Has Maintenance Problems!");
        provider.add("gtceu.multiblock.universal.has_problems_header", "Fix the following issues in a Maintenance Hatch:");
        provider.add("gtceu.multiblock.universal.problem.wrench", "%s§7Pipe is loose. (§aWrench§7)");
        provider.add("gtceu.multiblock.universal.problem.screwdriver", "%s§7Screws are loose. (§aScrewdriver§7)");
        provider.add("gtceu.multiblock.universal.problem.soft_mallet", "%s§7Something is stuck. (§aSoft Mallet§7)");
        provider.add("gtceu.multiblock.universal.problem.hard_hammer", "%s§7Plating is dented. (§aHard Hammer§7)");
        provider.add("gtceu.multiblock.universal.problem.wire_cutter", "%s§7Wires burned out. (§aWire Cutter§7)");
        provider.add("gtceu.multiblock.universal.problem.crowbar", "%s§7That doesn't belong there. (§aCrowbar§7)");
        provider.add("gtceu.multiblock.universal.muffler_obstructed", "Muffler Hatch is Obstructed!");
        provider.add("gtceu.multiblock.universal.muffler_obstructed.tooltip", "Muffler Hatch must have a block of airspace in front of it.");
        provider.add("gtceu.multiblock.universal.distinct", "Distinct Buses:");
        provider.add("gtceu.multiblock.universal.distinct.no", "No");
        provider.add("gtceu.multiblock.universal.distinct.yes", "Yes");
        provider.add("gtceu.multiblock.universal.distinct.info", "If enabled, each Item Input Bus will be treated as fully distinct from each other for recipe lookup. Useful for things like Programmed Circuits, Extruder Shapes, etc.");
        provider.add("gtceu.multiblock.parallel", "Performing up to %d Recipes in Parallel");
        provider.add("gtceu.multiblock.multiple_recipemaps.header", "Machine Mode:");
        provider.add("gtceu.multiblock.multiple_recipemaps.tooltip", "Screwdriver the controller to change which machine mode to use.");
        provider.add("gtceu.multiblock.multiple_recipemaps_recipes.tooltip", "Machine Modes: §e%s§r");
        provider.add("gtceu.multiblock.multiple_recipemaps.switch_message", "The machine must be off to switch modes!");
        provider.add("gtceu.multiblock.preview.zoom", "Use mousewheel or right-click + drag to zoom");
        provider.add("gtceu.multiblock.preview.rotate", "Click and drag to rotate");
        provider.add("gtceu.multiblock.preview.select", "Right-click to check candidates");
        provider.add("gtceu.multiblock.pattern.error", "Expected components (%s) at (%s).");
        provider.add("gtceu.multiblock.pattern.error.limited_exact", "§cExactly: %d§r");
        provider.add("gtceu.multiblock.pattern.error.limited_within", "§cBetween %d and %d§r");
        multiLang(provider, "gtceu.multiblock.pattern.error.limited", "§cMaximum: %d§r", "§cMinimum: %d§r", "§cMaximum: %d per layer§r", "§cMinimum: %d per layer§r");
        provider.add("gtceu.multiblock.pattern.error.coils", "§cAll heating coils must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.filters", "§cAll filters must be the same§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_1", "§6Must have a clear 1x1x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_3", "§6Must have a clear 3x3x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.single", "§6Only this block can be used§r");
        provider.add("gtceu.multiblock.pattern.location_end", "§cVery End§r");
        provider.add("gtceu.multiblock.pattern.replaceable_air", "Replaceable by Air");
        provider.add("gtceu.multiblock.blast_furnace.max_temperature", "Heat Capacity: %s");
        provider.add("gtceu.multiblock.multi_furnace.heating_coil_level", "Heating Coil Level: %s");
        provider.add("gtceu.multiblock.multi_furnace.heating_coil_discount", "Heating Coil EU Boost: %sx");
        provider.add("gtceu.multiblock.distillation_tower.distilling_fluid", "Distilling %s");
        provider.add("gtceu.multiblock.large_combustion_engine.lubricant_amount", "Lubricant Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.oxygen_amount", "Oxygen Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.liquid_oxygen_amount", "Liquid Oxygen Amount: %sL");
        provider.add("gtceu.multiblock.large_combustion_engine.oxygen_boosted", "§bOxygen boosted.");
        provider.add("gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted", "§bLiquid Oxygen boosted.");
        provider.add("gtceu.multiblock.large_combustion_engine.boost_disallowed", "§bUpgrade the Dynamo Hatch to enable Oxygen Boosting.");
        provider.add("gtceu.multiblock.large_combustion_engine.supply_oxygen_to_boost", "Supply Oxygen to boost.");
        provider.add("gtceu.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost", "Supply Liquid Oxygen to boost.");
        provider.add("gtceu.multiblock.large_combustion_engine.obstructed", "Engine Intakes Obstructed.");
        provider.add("gtceu.multiblock.turbine.fuel_amount", "Fuel Amount: %sL (%s)");
        provider.add("gtceu.multiblock.turbine.rotor_speed", "Rotor Speed: %s/%s RPM");
        provider.add("gtceu.multiblock.turbine.rotor_durability", "Rotor Durability: %s%%");
        provider.add("gtceu.multiblock.turbine.efficiency", "Turbine Efficiency: %s%%");
        provider.add("gtceu.multiblock.turbine.energy_per_tick", "Energy Output: %s/%s EU/t");
        provider.add("gtceu.multiblock.turbine.energy_per_tick_maxed", "Energy Output: %s EU/t");
        provider.add("gtceu.multiblock.turbine.obstructed", "Turbine Face Obstructed");
        provider.add("gtceu.multiblock.turbine.efficiency_tooltip", "Each Rotor Holder above %s§7 adds §f10%% efficiency§7.");
        provider.add("gtceu.multiblock.large_boiler.max_temperature", "Max Temperature: %dK, Steam Production: %dmB/t");
        provider.add("gtceu.multiblock.large_boiler.efficiency", "Efficiency: %s");
        provider.add("gtceu.multiblock.large_boiler.temperature", "Temperature: %sK / %sK");
        provider.add("gtceu.multiblock.large_boiler.steam_output", "Steam Output: %s L/t");
        provider.add("gtceu.multiblock.large_boiler.throttle", "Throttle: %d");
        provider.add("gtceu.multiblock.large_boiler.throttle.tooltip", "Boiler can output less Steam and consume less fuel (efficiency is not lost, does not affect heat-up time)");
        provider.add("gtceu.multiblock.large_boiler.throttle_modify", "Modify Throttle:");
        provider.add("gtceu.multiblock.large_boiler.rate_tooltip", "§7Produces §f%d L §7of Steam with §f1 Coal");
        provider.add("gtceu.multiblock.large_boiler.heat_time_tooltip", "§7Takes §f%d seconds §7to boiling up");
        provider.add("gtceu.multiblock.large_boiler.explosion_tooltip", "Will explode if provided Fuel with no Water");
        provider.add("gtceu.multiblock.large_miner.done", "Done!");
        provider.add("gtceu.multiblock.large_miner.working", "Working...");
        provider.add("gtceu.multiblock.large_miner.invfull", "Inventory Full!");
        provider.add("gtceu.multiblock.large_miner.needspower", "Needs Power!");
        provider.add("gtceu.multiblock.large_miner.vent", "Venting Blocked!");
        provider.add("gtceu.multiblock.large_miner.steam", "Needs Steam!");
        provider.add("gtceu.multiblock.large_miner.radius", "Radius: §a%d§r Blocks");
        provider.add("gtceu.multiblock.large_miner.errorradius", "§cCannot change radius while working!");
        provider.add("gtceu.multiblock.large_miner.needsfluid", "Needs Drilling Fluid");
        provider.add("gtceu.multiblock.pyrolyse_oven.speed", "Processing Speed: %s%%");
        provider.add("gtceu.multiblock.cracking_unit.energy", "Energy Usage: %s%%");
        provider.add("gtceu.command.usage", "Usage: /gtceu <worldgen/hand/recipecheck>");
        provider.add("gtceu.command.worldgen.usage", "Usage: /gtceu worldgen <reload>");
        provider.add("gtceu.command.worldgen.reload.usage", "Usage: /gtceu worldgen reload");
        provider.add("gtceu.command.worldgen.reload.success", "Worldgen successfully reloaded from config.");
        provider.add("gtceu.command.worldgen.reload.failed", "Worldgen reload failed. Check console for errors.");
        provider.add("gtceu.command.hand.groovy", "Consider using §6/gs hand");
        provider.add("gtceu.command.hand.usage", "Usage: /gtceu hand");
        provider.add("gtceu.command.hand.item_id", "Item: %s (Metadata: %d)");
        provider.add("gtceu.command.hand.electric", "Electric Info: %d / %d EU - Tier: %d; Is Battery: %s");
        provider.add("gtceu.command.hand.fluid", "Fluid Info: %d / %d L; Can Fill: %s; Can Drain: %s");
        provider.add("gtceu.command.hand.fluid2", "Fluid Id:");
        provider.add("gtceu.command.hand.material", "Material Id:");
        provider.add("gtceu.command.hand.ore_prefix", "Ore prefix:");
        provider.add("gtceu.command.hand.meta_item", "MetaItem Id:");
        provider.add("gtceu.command.hand.ore_dict_entries", "§3Ore dictionary entries:");
        provider.add("gtceu.command.hand.tool_stats", "Tool Stats Class: %s");
        provider.add("gtceu.command.hand.not_a_player", "This command is only usable by a player.");
        provider.add("gtceu.command.hand.no_item", "You must hold something in main hand or off hand before executing this command.");
        provider.add("gtceu.command.recipecheck.usage", "Usage: /gtceu recipecheck");
        provider.add("gtceu.command.recipecheck.begin", "Starting recipe conflict check...");
        provider.add("gtceu.command.recipecheck.end", "Recipe conflict check found %d possible conflicts. Check the server log for more info");
        provider.add("gtceu.command.recipecheck.end_no_conflicts", "No recipe conflicts found!");
        provider.add("gtceu.command.copy.copied_and_click", "copied to clipboard. Click to copy again");
        provider.add("gtceu.command.copy.click_to_copy", "Click to copy");
        provider.add("gtceu.command.copy.copied_start", "Copied [");
        provider.add("gtceu.command.copy.copied_end", "] to the clipboard");
        provider.add("gtceu.chat.cape", "§5Congrats: you just unlocked a new cape! See the Cape Selector terminal app to use it.§r");
        provider.add("gtceu.universal.clear_nbt_recipe.tooltip", "§cThis will destroy all contents!");
        provider.add("gtceu.cover.energy_detector.message_electricity_storage_normal", "Monitoring Normal Electricity Storage");
        provider.add("gtceu.cover.energy_detector.message_electricity_storage_inverted", "Monitoring Inverted Electricity Storage");
        provider.add("gtceu.cover.fluid_detector.message_fluid_storage_normal", "Monitoring Normal Fluid Storage");
        provider.add("gtceu.cover.fluid_detector.message_fluid_storage_inverted", "Monitoring Inverted Fluid Storage");
        provider.add("gtceu.cover.item_detector.message_item_storage_normal", "Monitoring Normal Item Storage");
        provider.add("gtceu.cover.item_detector.message_item_storage_inverted", "Monitoring Inverted Item Storage");
        provider.add("gtceu.cover.activity_detector.message_activity_normal", "Monitoring Normal Activity Status");
        provider.add("gtceu.cover.activity_detector.message_activity_inverted", "Monitoring Inverted Activity Status");
        provider.add("gtceu.cover.activity_detector_advanced.message_activity_normal", "Monitoring Normal Progress Status");
        provider.add("gtceu.cover.activity_detector_advanced.message_activity_inverted", "Monitoring Inverted Progress Status");
        provider.add("gtceu.creative.chest.item", "Item");
        provider.add("gtceu.creative.chest.ipc", "Items per Cycle");
        provider.add("gtceu.creative.chest.tpc", "Ticks per Cycle");
        provider.add("gtceu.creative.tank.fluid", "Fluid");
        provider.add("gtceu.creative.tank.mbpc", "mB per Cycle");
        provider.add("gtceu.creative.tank.tpc", "Ticks per Cycle");
        provider.add("gtceu.creative.energy.amperage", "Amperage");
        provider.add("gtceu.creative.energy.voltage", "Voltage");
        provider.add("gtceu.creative.activity.on", "Active");
        provider.add("gtceu.creative.activity.off", "Not active");
        provider.add("gtceu.terminal.app_name.items", "Item Guides");
        provider.add("gtceu.terminal.app_name.machines", "Machine Guides");
        provider.add("gtceu.terminal.app_name.multiblocks", "Multiblock Guides");
        provider.add("gtceu.terminal.app_name.tutorials", "Tutorials");
        provider.add("gtceu.terminal.app_name.settings", "System Settings");
        provider.add("gtceu.terminal.app_name.guide_editor", "Guide Editor");
        provider.add("gtceu.terminal.app_name.recipe_chart", "Recipe Chart");
        provider.add("gtceu.terminal.app_name.ore_prospector", "Ore Prospector");
        provider.add("gtceu.terminal.app_name.fluid_prospector", "Fluid Prospector");
        provider.add("gtceu.terminal.app_name.pong", "Pong");
        provider.add("gtceu.terminal.app_name.minesweeper", "Minesweeper");
        provider.add("gtceu.terminal.app_name.maze", "Theseus's Escape");
        provider.add("gtceu.terminal.app_name.console", "GT Console");
        provider.add("gtceu.terminal.app_name.battery", "Battery Manager");
        provider.add("gtceu.terminal.app_name.hardware", "Hardware Manager");
        provider.add("gtceu.terminal.app_name.store", "App Store");
        provider.add("gtceu.terminal.app_name.multiblock_ar", "Multiblock Helper");
        provider.add("gtceu.terminal.app_name.world_prospector", "World Prospector");
        provider.add("gtceu.terminal.app_name.vtank_viewer", "Virtual Tank Viewer");
        provider.add("gtceu.terminal.app_name.cape_selector", "Cape Selector");
        provider.add("gtceu.terminal.app_name.teleport", "Teleporter");
        provider.add("terminal.app_name.description", "No description.");
        provider.add("terminal.app_name.tier", "Tier %d");
        provider.add("terminal.app_name.maximize.unsupported", "This App does not support maximize.");
        provider.add("terminal.items.description", "A guide book about items.");
        provider.add("terminal.machines.description", "A guide book about gt machines.");
        provider.add("terminal.multiblocks.description", "A guide book about multi-blocks.");
        provider.add("terminal.tutorials.description", "Introduces all kinds of things, CT integration, tips, tutorials and more.");
        provider.add("terminal.pong.description", "A classic pong game, if you're really that bored of waiting for that tungstensteel.");
        provider.add("terminal.minesweeper.description", "A classic minesweeper game, if you're in class.");
        provider.add("terminal.maze.description", "A GTOS exclusive game finding you racing through an endless labyrinth to survive the Minotaur!");
        provider.add("terminal.cape_selector.description", "An app that allows you to equip GT capes that you've unlocked through advancements.");
        provider.add("terminal.teleport.description", "Open portals to any coordinate in any dimension. Be careful not to teleport into a wall!");
        provider.add("texture.modify_gui_texture.missing", "Missing Texture");
        provider.add("texture.url_texture.fail", "Load Failed");
        provider.add("terminal.hw.battery", "Battery");
        provider.add("terminal.hw.device", "Device");
        provider.add("terminal.os.shutdown_confirm", "Confirm shutdown? (Press ESC again to see ok)");
        provider.add("terminal.os.hw_demand", "Missing mounting hardware:");
        provider.add("terminal.system_call.null", "NULL");
        provider.add("terminal.system_call.call_menu", "Call Menu");
        provider.add("terminal.system_call.full_screen", "Full Screen");
        provider.add("terminal.system_call.minimize_focus_app", "Back to Desktop");
        provider.add("terminal.system_call.close_focus_app", "Close App");
        provider.add("terminal.system_call.shutdown", "Shutdown");
        provider.add("terminal.system_call.open_app", "Open App");
        provider.add("terminal.menu.close", "Close App");
        provider.add("terminal.menu.minimize", "Minimize App");
        provider.add("terminal.menu.maximize", "Full Screen/Recover");
        provider.add("terminal.component.new_page", "New Page");
        provider.add("terminal.component.page_name", "Page Name");
        provider.add("terminal.component.load_file", "Load File");
        provider.add("terminal.component.load_file.error", "An error occurred while loading the file.");
        provider.add("terminal.component.save_file", "Save File");
        provider.add("terminal.component.save_file.error", "An error occurred while saving the file.");
        provider.add("terminal.component.confirm", "Are you sure?");
        provider.add("terminal.component.error", "ERROR");
        provider.add("terminal.component.warning", "WARNING");
        provider.add("terminal.component.searching", "searching");
        provider.add("terminal.component.reload", "reload resource");
        provider.add("terminal.dialog.notice", "NOTICE");
        provider.add("terminal.dialog.error_path", "error file path:");
        provider.add("terminal.dialog.no_file_selected", "No file selected.");
        provider.add("terminal.dialog.folder", "Open Folder");
        provider.add("terminal.guide_editor.description", "An interactive guide page editor that allows you to create guide pages easily.");
        provider.add("terminal.guide_editor.up", "Up");
        provider.add("terminal.guide_editor.down", "Down");
        provider.add("terminal.guide_editor.remove", "Remove");
        provider.add("terminal.guide_editor.add_stream", "Add to stream");
        provider.add("terminal.guide_editor.add_fixed", "Add to fixed");
        provider.add("terminal.guide_editor.update", "Update");
        provider.add("terminal.guide_editor.add_slot", "Add Slot");
        provider.add("terminal.guide_editor.default", "Default Value");
        provider.add("terminal.guide_editor.page_config", "Page Config");
        provider.add("terminal.guide_editor.widgets_box", "Widgets Box");
        provider.add("terminal.guide_editor.widget_config", "Widget Config");
        provider.add("terminal.guide_editor.error_type", "Item types do not match and will still be saved, but will not be seen in app.");
        provider.add("terminal.settings.description", "The system settings for GTOS.");
        provider.add("terminal.settings.theme", "Theme");
        provider.add("terminal.settings.theme.color", "Theme Color Settings");
        provider.add("terminal.settings.theme.wallpaper", "Wallpaper Settings");
        provider.add("terminal.settings.theme.wallpaper.resource", "resource");
        provider.add("terminal.settings.theme.wallpaper.url", "url");
        provider.add("terminal.settings.theme.wallpaper.color", "color");
        provider.add("terminal.settings.theme.wallpaper.file", "file");
        provider.add("terminal.settings.theme.select", "Select");
        provider.add("terminal.settings.theme.image", "Image File");
        provider.add("terminal.settings.home", "Home Button");
        provider.add("terminal.settings.home.double", "Double");
        provider.add("terminal.settings.home.action", "Click Action");
        provider.add("terminal.settings.home.args", "Args");
        provider.add("terminal.settings.home.click", "click");
        provider.add("terminal.settings.home.double_click", "double Click");
        provider.add("terminal.settings.os", "Os settings");
        provider.add("terminal.settings.os.double_check", "Double Check");
        provider.add("terminal.settings.os.double_check.desc", "Should Double Check when shutdown.");
        provider.add("terminal.recipe_chart.description", "A tool for analyzing recipe chains, with which you can easily build a recipe graph and even calculate the ingredients needed like JEC.");
        provider.add("terminal.recipe_chart.limit", "Page limit.");
        provider.add("terminal.recipe_chart.delete", "Delete Page");
        provider.add("terminal.recipe_chart.add_slot", "Add Root Slot");
        provider.add("terminal.recipe_chart.demand", "Demand");
        provider.add("terminal.recipe_chart.calculator", "Calculator");
        provider.add("terminal.recipe_chart.add", "Set the item from inventory");
        provider.add("terminal.recipe_chart.drag", "Drag ingredients here.");
        provider.add("terminal.recipe_chart.visible", "Visible");
        provider.add("terminal.recipe_chart.jei", "JEI Focus");
        provider.add("terminal.recipe_chart.tier", "Tier:");
        provider.add("terminal.recipe_chart.ratio", "Weight");
        multiLang(provider, "terminal.recipe_chart.tier", IntStream.of(4)
                .map(i -> i + 5)
                .mapToObj(Integer::toString)
                .map(i -> "cache of " + i + " pages")
                .toArray(String[]::new));
        provider.add("terminal.prospector.vis_mode", "Switch color mode");
        provider.add("terminal.prospector.list", "All Resources");
        provider.add("terminal.prospector.ore", "Ore Data");
        provider.add("terminal.prospector.fluid", "Fluid Deposit Data");
        provider.add("terminal.prospector.fluid.info", "%s %s - %s%%");
        provider.add("terminal.ore_prospector.description", "Hate the scanner toy? Don't want to run around looking for ores? Come and look at it.");
        multiLang(provider, "terminal.ore_prospector.tier", IntStream.of(6)
                .map(i -> i + 1)
                .mapToObj(Integer::toString)
                .map(i -> "radius size " + i)
                .toArray(String[]::new));
        provider.add("terminal.fluid_prospector.description", "You know, there's gold in bedrocks.");
        multiLang(provider, "terminal.fluid_prospector.tier", IntStream.of(6)
                .map(i -> i + 1)
                .mapToObj(Integer::toString)
                .map(i -> "radius size " + i)
                .toArray(String[]::new));
        provider.add("terminal.console.description", "A tool to help you free your inventory, it's time to say goodbye to wrench, screwdriver, hammer, and crowbar.");
        provider.add("terminal.console.notice", "Please shift-right-click a machine when opening the terminal.");
        provider.add("terminal.console.front", "Set as front");
        provider.add("terminal.console.items", "set as items output");
        provider.add("terminal.console.fluids", "set as fluids output");
        provider.add("terminal.console.auto_output", "allow auto output");
        provider.add("terminal.console.input", "allow input from output");
        provider.add("terminal.console.cover_rs", "signal: %s");
        provider.add("terminal.console.cover_gui", "Gui");
        provider.add("terminal.console.gui", "Open machine gui");
        provider.add("terminal.console.maintenance", "Fix all problems");
        provider.add("terminal.console.venting", "set as venting");
        provider.add("terminal.console.controllable", "set working enable");
        provider.add("terminal.hardware.description", "Hardware Manager, masterpieces of elegance and precision. How can a tablet be without hardware?");
        provider.add("terminal.hardware.select", "Mounting hardware");
        provider.add("terminal.hardware.remove", "Remove hardware");
        provider.add("terminal.hardware.remove.full", "There are no empty slots in inventory");
        provider.add("terminal.hardware.tip.remove", "§4right-click remove the hardware");
        provider.add("terminal.battery.description", "Battery manager, visually analyze your app's energy consumption.");
        provider.add("terminal.battery.hover", "%s: Usage %d eu/s");
        provider.add("terminal.battery.low_energy", "Low power! Plz recharge the terminal.");
        provider.add("terminal.store.description", "App store, check out what you got? Install and upgrade the app.");
        provider.add("terminal.store.match", "Detected required item in inventory, install/upgrade App?");
        provider.add("terminal.store.miss", "Requires %s (%d).");
        provider.add("terminal.ar.open", "Open AR");
        provider.add("terminal.multiblock_ar.description", "Remember the §cFreedom Wrench§r?  Unfortunately, it it's gone. It doesn't matter, we have a new technology now. This app can also help you build your multi-block machine.");
        multiLang(provider, "terminal.multiblock_ar.tier", "AR Camera", "3D Builder");
        provider.add("terminal.multiblock_ar.unlock", "Unlock this mode after the upgrade");
        provider.add("terminal.multiblock_ar.builder.hover", "3D Builder");
        provider.add("terminal.multiblock_ar.builder.auto", "Automatic Build");
        provider.add("terminal.multiblock_ar.builder.place", "Place block");
        provider.add("terminal.multiblock_ar.builder.debug", "Debug");
        multilineLang(provider, "terminal.world_prospector.description", "\"I wish I had X-ray vision.\"\n\"Sir, I'm sorry, but we don't sell superpowers. You should trust the science.\"");
        multiLang(provider, "terminal.world_prospector.tier", "Radius 15m (1 slot)", "Radius 30m (2 slot)", "Radius 60m (4 slot)");
        provider.add("terminal.world_prospector.radius", "Radius %sm");
        provider.add("terminal.world_prospector.reference", "Select a reference");
        provider.add("terminal.world_prospector.color", "Select box color");
        provider.add("terminal.vtank_viewer.description", "Never lose any fluids to changing ender link frequencies again! Here's a scrollable list of every virtual tank that you have access to in your world.");
        provider.add("terminal.vtank_viewer.title", "Virtual Tank Viewer");
        provider.add("terminal.vtank_viewer.refresh", "Refresh tank index");
        provider.add("terminal.teleporter.dimension", "Dimension:");
        provider.add("terminal.teleporter.spawn_portal", "Engage");
        provider.add("terminal.maze.title", "Theseus's Escape");
        provider.add("terminal.maze.play", "Play");
        provider.add("terminal.maze.continue", "Continue");
        provider.add("terminal.maze.pause", "Game Paused");
        multiLang(provider, "terminal.maze.death", "Oh no! You were eaten by the Minotaur!", "You got through %s mazes before losing.", "Try again?");
        provider.add("terminal.maze.death.3", "Try again?");
        provider.add("terminal.maze.retry", "Retry");
        provider.add("terminal.minesweeper.time", "%s seconds elapsed");
        provider.add("terminal.minesweeper.lose", "You lost. Game will restart in %s seconds.");
        multiLang(provider, "terminal.minesweeper.win", "You won in %s seconds!", "Game will restart in %s");
        provider.add("terminal.cape_selector.empty", "It looks like you haven't unlocked any capes yet!");
        provider.add("terminal.cape_selector.select", "Click on an unlocked cape to select it!");
        provider.add("terminal.cape_selector.tip", "You can get these from high-level advancements.");
        provider.add("metaitem.cover.digital.title.mode", "Mode:");
        provider.add("metaitem.cover.digital.title.spin", "Spin:");
        multiLang(provider, "metaitem.cover.digital.wireless.tooltip",
                "§fWirelessly§7 connects machines to the §fCentral Monitor§7 as §fCover§7.",
                "§fRight Click§7 on the §fCentral Monitor§7 to remotely bind to it.",
                "§fSneak Right Click§7 to remove the current binding.",
                "§aBinding: §f%s");
        provider.add("monitor.gui.title.back", "Back");
        provider.add("monitor.gui.title.scale", "Scale:");
        provider.add("monitor.gui.title.argb", "ARGB:");
        provider.add("monitor.gui.title.slot", "Slot:");
        provider.add("monitor.gui.title.plugin", "Plugin:");
        provider.add("monitor.gui.title.config", "Config");
        provider.add("fluid.tile.lava", "Lava");
        provider.add("fluid.tile.water", "Water");
        provider.add("gtceu.key.armor_mode_switch", "Armor Mode Switch");
        provider.add("gtceu.key.armor_hover", "Armor Hover Toggle");
        provider.add("gtceu.key.armor_charging", "Armor Charging to Inventory Toggle");
        provider.add("gtceu.key.tool_aoe_change", "Tool AoE Mode Switch");
        provider.add("gtceu.debug.f3_h.enabled", "GregTech has modified the debug info! For Developers: enable the misc:debug config option in the GregTech config file to see more");
        provider.add("config.jade.plugin_gtceu.controllable_provider", "[GTCEu] Controllable");
        provider.add("config.jade.plugin_gtceu.workable_provider", "[GTCEu] Workable");
        provider.add("config.jade.plugin_gtceu.electric_container_provider", "[GTCEu] Electric Container");
        provider.add("config.jade.plugin_gtceu.recipe_logic_provider", "[GTCEu] Recipe Logic");
    }

    /**
     * Returns the sub-key consisting of the given key plus the given index.<br>
     * E.g.,<br>
     * <pre>
     * <code>getSubKey("terminal.fluid_prospector.tier", 0)</code></pre>
     * returns the <code>String</code>:
     * <pre>
     * <code>
     * "terminal.fluid_prospector.tier.0"</code></pre>
     *
     * @param key   Base key of the sub-key.
     * @param index Index of the sub-key.
     * @return Sub-key consisting of key and index.
     */
    private static String getSubKey(String key, int index) {
        return key + "." + index;
    }

    /**
     * Registers multiple values under the same key with a given provider.<br><br>
     * For example, a cumbersome way to add translations would be the following:<br>
     * <pre>
     * <code>provider.add("terminal.fluid_prospector.tier.0", "radius size 1");
     * provider.add("terminal.fluid_prospector.tier.1", "radius size 2");
     * provider.add("terminal.fluid_prospector.tier.2", "radius size 3");</code></pre>
     * Instead, <code>multiLang</code> can be used for the same result:
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code></pre>
     * In situations requiring a large number of generated translations, the following could be used instead, which generates translations for 100 tiers:
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", IntStream.of(100)
     *                 .map(i -> i + 1)
     *                 .mapToObj(Integer::toString)
     *                 .map(i -> "radius size " + i)
     *                 .toArray(String[]::new));</code></pre>
     *
     * @param provider The provider to add to.
     * @param key      Base key of the key-value-pairs. The real key for each translation will be appended by ".0" for the first, ".1" for the second, etc. This ensures that the keys are unique.
     * @param values   All translation values.
     */
    private static void multiLang(RegistrateLangProvider provider, String key, String... values) {
        for (var i = 0; i < values.length; i++) {
            provider.add(getSubKey(key, i), values[i]);
        }
    }

    /**
     * Gets all translation components from a multi lang's sub-keys.<br>
     * E.g., given a multi lang:
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code></pre>
     * The following code can be used to print out the translations:
     * <pre>
     * <code>for (var component : getMultiLang("terminal.fluid_prospector.tier")) {
     *     System.out.println(component.getString());
     * }</code></pre>
     * Result:
     * <pre>
     * <code>radius size 1
     * radius size 2
     * radius size 3</code></pre>
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys
     */
    public static MutableComponent[] getMultiLang(String key) {
        var outputKeys = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (LocalizationUtils.exist(next)) {
            outputKeys.add(next);
            next = getSubKey(key, ++i);
        }
        return outputKeys.stream().map(Component::translatable).toArray(MutableComponent[]::new);
    }

    /**
     * Gets all translation components from a multi lang's sub-keys. Supports additional arguments for the translation components.<br>
     * E.g., given a multi lang:
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code></pre>
     * The following code can be used to print out the translations:
     * <pre>
     * <code>for (var component : getMultiLang("terminal.fluid_prospector.tier")) {
     *     System.out.println(component.getString());
     * }</code></pre>
     * Result:
     * <pre>
     * <code>radius size 1
     * radius size 2
     * radius size 3</code></pre>
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys.
     */
    public static MutableComponent[] getMultiLang(String key, Object... args) {
        var outputKeys = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (LocalizationUtils.exist(next)) {
            outputKeys.add(next);
            next = getSubKey(key, ++i);
        }
        return outputKeys.stream().map(k -> Component.translatable(k, args)).toArray(MutableComponent[]::new);
    }

    /**
     * Gets a single translation from a multi lang.
     *
     * @param key   Base key of the multi lang. E.g. "gtceu.gui.overclock.enabled".
     * @param index Index of the single translation. E.g. 3 would return "gtceu.gui.overclock.enabled.3".
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiLang(String key, int index) {
        return Component.translatable(getSubKey(key, index));
    }

    /**
     * Gets a single translation from a multi lang. Supports additional arguments for the translation component.
     *
     * @param key   Base key of the multi lang. E.g. "gtceu.gui.overclock.enabled".
     * @param index Index of the single translation. E.g. 3 would return "gtceu.gui.overclock.enabled.3".
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiLang(String key, int index, Object... args) {
        return Component.translatable(getSubKey(key, index), args);
    }

    /**
     * Adds one key-value-pair to the given lang provider per line in the given multiline (a multiline is a String containing newline characters).<br>
     * Example:
     * <pre>
     * <code>multilineLang(provider, "gtceu.gui.overclock.enabled", "Overclocking Enabled.\nClick to Disable");</code></pre>
     * This results in the following translations:<br>
     * <pre>
     * <code>"gtceu.gui.overclock.enabled.0": "Overclocking Enabled.",
     * "gtceu.gui.overclock.enabled.1": "Click to Disable",</code></pre>
     *
     * @param provider  The provider to add to.
     * @param key       Base key of the key-value-pair. The real key for each line will be appended by ".0" for the first line, ".1" for the second, etc. This ensures that the keys are unique.
     * @param multiline The multiline string. It is a multiline because it contains at least one newline character '\n'.
     */
    private static void multilineLang(RegistrateLangProvider provider, String key, String multiline) {
        var lines = multiline.split("\n");
        multiLang(provider, key, lines);
    }
}
