package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/3/19
 * @implNote LangHandler
 */
public class LangHandler {

    public static void init(RegistrateLangProvider provider) {

        AdvancementLang.init(provider);
        BlockLang.init(provider);
        IntegrationLang.init(provider);
        ItemLang.init(provider);
        MachineLang.init(provider);
        ToolLang.init(provider);
        ConfigurationLang.init(provider);
        CompassLang.init(provider);

        provider.add("gtceu.gui.editor.tips.citation", "Number of citations");
        provider.add("gtceu.gui.editor.group.recipe_type", "cap");
        provider.add("ldlib.gui.editor.register.editor.gtceu.rtui", "RecipeType UI Project");
        provider.add("ldlib.gui.editor.register.editor.gtceu.mui", "Machine UI Project");
        provider.add("ldlib.gui.editor.register.editor.gtceu.template_tab", "templates");
        //capabilities
        provider.add("recipe.capability.eu.name", "GTCEu Energy");
        provider.add("recipe.capability.fluid.name", "Fluid");
        provider.add("recipe.capability.item.name", "Item");
        provider.add("recipe.capability.su.name", "Create Stress");

        provider.add("recipe.condition.rpm.tooltip", "RPM: %d");
        provider.add("recipe.condition.thunder.tooltip", "Thunder Level: %d");
        provider.add("recipe.condition.rain.tooltip", "Rain Level: %d");
        provider.add("recipe.condition.dimension.tooltip", "Dimension: %s");
        provider.add("recipe.condition.biome.tooltip", "Biome: %s");
        provider.add("recipe.condition.pos_y.tooltip", "Y Level: %d <= Y <= %d");
        provider.add("recipe.condition.steam_vent.tooltip", "Clean steam vent");
        provider.add("recipe.condition.rock_breaker.tooltip", "Fluid blocks around");
        provider.add("recipe.condition.eu_to_start.tooltip", "EU to start: %d");

        provider.add("gtceu.io.import", "Import");
        provider.add("gtceu.io.export", "Export");
        provider.add("gtceu.io.both", "Both");
        provider.add("gtceu.io.none", "None");

        provider.add("enchantment.disjunction", "Disjunction");
        provider.add("gtceu.multiblock.steam_grinder.description", "A Multiblock Macerator at the Steam Age. Requires at least 14 Bronze Casings to form. Cannot use normal Input/Output busses, nor Fluid Hatches other than the Steam Hatch.");
        provider.add("gtceu.multiblock.steam.low_steam", "Not enough Steam to run!");
        provider.add("gtceu.multiblock.steam.steam_stored", "Steam: %s / %s mb");
        provider.add("gtceu.machine.steam.steam_hatch.tooltip", "§eAccepted Fluid: §fSteam");
        provider.add("gtceu.machine.steam_bus.tooltip", "Does not work with non-steam multiblocks");
        provider.add("gtceu.multiblock.steam_oven.description", "A Multi Smelter at the Steam Age. Requires at least 6 Bronze Casings to form. Cannot use normal Input/Output busses, nor Fluid Hatches other than the Steam Hatch. Steam Hatch must be on the bottom layer, no more than one.");
        provider.add("gtceu.multiblock.require_steam_parts", "Requires Steam Hatches and Buses!");
        provider.add("gtceu.multiblock.steam_.duration_modifier", "Takes §f1.5x §7base duration to process, not affected by number of items.");

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
        provider.add("gtceu.multiblock.fusion_reactor.luv.description", "The Fusion Reactor MK 1 is a large multiblock structure used for fusing elements into heavier ones. It can only use LuV, ZPM, and UV Energy Hatches. For every Hatch it has, its buffer increases by 10M EU, and has a maximum of 160M.");
        provider.add("gtceu.multiblock.fusion_reactor.zpm.description", "The Fusion Reactor MK 2 is a large multiblock structure used for fusing elements into heavier ones. It can only use ZPM and UV Energy Hatches. For every Hatch it has, its buffer increases by 20M EU, and has a maximum of 320M.");
        provider.add("gtceu.multiblock.fusion_reactor.uv.description", "The Fusion Reactor MK 3 is a large multiblock structure used for fusing elements into heavier ones. It can only use UV Energy Hatches. For every Hatch it has, its buffer increases by 40M EU, and has a maximum of 640M.");
        provider.add("gtceu.multiblock.fusion_reactor.energy", "EU: %d / %d");
        provider.add("gtceu.multiblock.fusion_reactor.heat", "Heat: %d");
        provider.add("gtceu.multiblock.large_chemical_reactor.description", "The Large Chemical Reactor performs chemical reactions at 100%% energy efficiency. Overclocks multiply both speed and energy by 4. The multiblock requires exactly 1 Cupronickel Coil Block, which must be placed adjacent to the PTFE Pipe casing located in the center.");
        provider.add("gtceu.multiblock.primitive_water_pump.description", "The Primitive Water Pump is a pre-Steam Era multiblock that collects water once per second, depending on the Biome it is in. It can use a Pump, ULV, or LV Output Hatch, increasing the amount of water per tier. Follows the formula: Biome Coefficient * Hatch Multiplier.");
        multilineLang(provider, "gtceu.multiblock.primitive_water_pump.extra1", "Biome Coefficient:\n  Ocean, River: 1000 L/s\n  Swamp: 800 L/s\n  Jungle: 350 L/s\n  Snowy: 300 L/s\n  Plains, Forest: 250 L/s\n  Taiga: 175 L/s\n  Beach: 170 L/s\n  Other: 100 L/s");
        multilineLang(provider, "gtceu.multiblock.primitive_water_pump.extra2", "Hatch Multipliers:\n  Pump Hatch: 1x\n  ULV Output Hatch: 2x\n  LV Output Hatch: 4x\n\nWhile raining in the Pump's Biome, the total water production will be increased by 50%%.");
        provider.add("gtceu.multiblock.processing_array.description", "The Processing Array combines up to 16 single block machine(s) in a single multiblock, effectively easing automation.");
        provider.add("gtceu.multiblock.advanced_processing_array.description", "The Processing Array combines up to 64 single block machine(s) in a single multiblock, effectively easing automation.");
        provider.add("gtceu.multiblock.parallelizable.tooltip", "Can parallelize with Parallel Control Hatches.");
        provider.add("gtceu.parallel_hatch_mk5", "Allows to run up to 4 recipes in parallel.");
        provider.add("gtceu.parallel_hatch_mk6", "Allows to run up to 16 recipes in parallel.");
        provider.add("gtceu.parallel_hatch_mk7", "Allows to run up to 64 recipes in parallel.");
        provider.add("gtceu.parallel_hatch_mk8", "Allows to run up to 256 recipes in parallel.");

        provider.add("item.invalid.name", "Invalid item");
        provider.add("fluid.empty", "Empty");
        provider.add("gtceu.tooltip.hold_shift", "§7Hold SHIFT for more info");
        provider.add("gtceu.tooltip.hold_ctrl", "§7Hold CTRL for more info");
        provider.add("gtceu.tooltip.fluid_pipe_hold_shift", "§7Hold SHIFT to show Fluid Containment Info");
        provider.add("gtceu.tooltip.tool_fluid_hold_shift", "§7Hold SHIFT to show Fluid Containment and Tool Info");
        provider.add("metaitem.generic.fluid_container.tooltip", "%d/%dL %s");
        provider.add("metaitem.generic.electric_item.tooltip", "%d/%d EU - Tier %s");
        provider.add("metaitem.generic.electric_item.stored", "%d/%d EU (%s)");
        provider.add("metaitem.electric.discharge_mode.enabled", "§eDischarge Mode Enabled");
        provider.add("metaitem.electric.discharge_mode.disabled", "§eDischarge Mode Disabled");
        provider.add("metaitem.electric.discharge_mode.tooltip", "Use while sneaking to toggle discharge mode");
        provider.add("metaitem.dust.tooltip.purify", "Throw into Cauldron to get clean Dust");
        provider.add("metaitem.crushed.tooltip.purify", "Throw into Cauldron to get Purified Ore");
        provider.add("metaitem.int_circuit.configuration", "Configuration: %d");


        provider.add("gtceu.tool.class.sword", "Sword");
        provider.add("gtceu.tool.class.pickaxe", "Pickaxe");
        provider.add("gtceu.tool.class.shovel", "Shovel");
        provider.add("gtceu.tool.class.axe", "Axe");
        provider.add("gtceu.tool.class.hoe", "Hoe");
        provider.add("gtceu.tool.class.mining_hammer", "Mining Hammer");
        provider.add("gtceu.tool.class.spade", "Spade");
        provider.add("gtceu.tool.class.saw", "Saw");
        provider.add("gtceu.tool.class.hammer", "Hammer");
        provider.add("gtceu.tool.class.mallet", "Soft Mallet");
        provider.add("gtceu.tool.class.wrench", "Wrench");
        provider.add("gtceu.tool.class.file", "File");
        provider.add("gtceu.tool.class.crowbar", "Crowbar");
        provider.add("gtceu.tool.class.screwdriver", "Screwdriver");
        provider.add("gtceu.tool.class.mortar", "Mortar");
        provider.add("gtceu.tool.class.wire_cutter", "Wire Cutter");
        provider.add("gtceu.tool.class.knife", "Knife");
        provider.add("gtceu.tool.class.butchery_knife", "Butchery Knife");
        provider.add("gtceu.tool.class.scythe", "Scythe");
        provider.add("gtceu.tool.class.rolling_pin", "Rolling Pin");
        provider.add("gtceu.tool.class.plunger", "Plunger");
        provider.add("gtceu.tool.class.shears", "Shears");
        provider.add("gtceu.tool.class.drill", "Drill");

        provider.add("item.gtceu.tool.replace_tool_head", "Craft with a new Tool Head to replace it");
        provider.add("item.gtceu.tool.usable_as", "§8Usable as: §f%s");
        provider.add("item.gtceu.tool.behavior.silk_ice", "§bIce Cutter: §fSilk Harvests Ice");
        provider.add("item.gtceu.tool.behavior.torch_place", "§eSpelunker: §fPlaces Torches on Right-Click");
        provider.add("item.gtceu.tool.behavior.tree_felling", "§4Lumberjack: §fTree Felling");
        provider.add("item.gtceu.tool.behavior.strip_log", "§5Artisan: §fStrips Logs");
        provider.add("item.gtceu.tool.behavior.scrape", "§bPolisher: §fRemoves Oxidation");
        provider.add("item.gtceu.tool.behavior.remove_wax", "§6Cleaner: §fRemoves Wax");
        provider.add("item.gtceu.tool.behavior.shield_disable", "§cBrute: §fDisables Shields");
        provider.add("item.gtceu.tool.behavior.relocate_mining", "§2Magnetic: §fRelocates Mined Blocks");
        provider.add("item.gtceu.tool.behavior.aoe_mining", "§5Area-of-Effect: §f%sx%sx%s");
        provider.add("item.gtceu.tool.behavior.ground_tilling", "§eFarmer: §fTills Ground");
        provider.add("item.gtceu.tool.behavior.grass_path", "§eLandscaper: §fCreates Grass Paths");
        provider.add("item.gtceu.tool.behavior.rail_rotation", "§eRailroad Engineer: §fRotates Rails");
        provider.add("item.gtceu.tool.behavior.crop_harvesting", "§aHarvester: §fHarvests Crops");
        provider.add("item.gtceu.tool.behavior.plunger", "§9Plumber: §fDrains Fluids");
        provider.add("item.gtceu.tool.behavior.block_rotation", "§2Mechanic: §fRotates Blocks");
        provider.add("item.gtceu.tool.behavior.damage_boost", "§4Damage Boost: §fExtra damage against %s");
        replace(provider, "item.gtceu.tool.sword", "%s Sword");
        replace(provider, "item.gtceu.tool.pickaxe", "%s Pickaxe");
        replace(provider, "item.gtceu.tool.shovel", "%s Shovel");
        replace(provider, "item.gtceu.tool.axe", "%s Axe");
        replace(provider, "item.gtceu.tool.hoe", "%s Hoe");
        replace(provider, "item.gtceu.tool.saw", "%s Saw");
        replace(provider, "item.gtceu.tool.hammer", "%s Hammer");
        provider.add("item.gtceu.tool.hammer.tooltip", "§8Crushes Blocks when harvesting them");
        replace(provider, "item.gtceu.tool.mallet", "%s Soft Mallet");
        provider.add("item.gtceu.tool.mallet.tooltip", "§8Stops/Starts Machinery");
        replace(provider, "item.gtceu.tool.wrench", "%s Wrench");
        provider.add("item.gtceu.tool.wrench.tooltip", "§8Hold left click to dismantle Machines");
        replace(provider, "item.gtceu.tool.file", "%s File");
        replace(provider, "item.gtceu.tool.crowbar", "%s Crowbar");
        provider.add("item.gtceu.tool.crowbar.tooltip", "§8Dismounts Covers");
        replace(provider, "item.gtceu.tool.screwdriver", "%s Screwdriver");
        provider.add("item.gtceu.tool.screwdriver.tooltip", "§8Adjusts Covers and Machines");
        replace(provider, "item.gtceu.tool.mortar", "%s Mortar");
        replace(provider, "item.gtceu.tool.wire_cutter", "%s Wire Cutter");
        replace(provider, "item.gtceu.tool.knife", "%s Knife");
        replace(provider, "item.gtceu.tool.butchery_knife", "%s Butchery Knife");
        provider.add("item.gtceu.tool.butchery_knife.tooltip", "§8Has a slow Attack Rate");
        replace(provider, "item.gtceu.tool.scythe", "%s Scythe");
        provider.add("item.gtceu.tool.scythe.tooltip", "§8Because a Scythe doesn't make Sense");
        replace(provider, "item.gtceu.tool.rolling_pin", "%s Rolling Pin");
        replace(provider, "item.gtceu.tool.lv_drill", "%s Drill (LV)");
        replace(provider, "item.gtceu.tool.mv_drill", "%s Drill (MV)");
        replace(provider, "item.gtceu.tool.hv_drill", "%s Drill (HV)");
        replace(provider, "item.gtceu.tool.ev_drill", "%s Drill (EV)");
        replace(provider, "item.gtceu.tool.iv_drill", "%s Drill (IV)");
        replace(provider, "item.gtceu.tool.mining_hammer", "%s Mining Hammer");
        provider.add("item.gtceu.tool.mining_hammer.tooltip", "§8Mines a large area at once (unless you're crouching)");
        replace(provider, "item.gtceu.tool.spade", "%s Spade");
        provider.add("item.gtceu.tool.spade.tooltip", "§8Mines a large area at once (unless you're crouching)");
        replace(provider, "item.gtceu.tool.lv_chainsaw", "%s Chainsaw (LV)");
        replace(provider, "item.gtceu.tool.mv_chainsaw", "%s Chainsaw (MV)");
        replace(provider, "item.gtceu.tool.hv_chainsaw", "%s Chainsaw (HV)");
        replace(provider, "item.gtceu.tool.lv_wrench", "%s Wrench (LV)");
        provider.add("item.gtceu.tool.lv_wrench.tooltip", "§8Hold left click to dismantle Machines");
        replace(provider, "item.gtceu.tool.hv_wrench", "%s Wrench (HV)");
        provider.add("item.gtceu.tool.hv_wrench.tooltip", "§8Hold left click to dismantle Machines");
        replace(provider, "item.gtceu.tool.iv_wrench", "%s Wrench (IV)");
        provider.add("item.gtceu.tool.iv_wrench.tooltip", "§8Hold left click to dismantle Machines");
        replace(provider, "item.gtceu.tool.buzzsaw", "%s Buzzsaw (LV)");
        provider.add("item.gtceu.tool.buzzsaw.tooltip", "§8Not suitable for harvesting Blocks");
        replace(provider, "item.gtceu.tool.lv_screwdriver", "%s Screwdriver (LV)");
        provider.add("item.gtceu.tool.lv_screwdriver.tooltip", "§8Adjusts Covers and Machines");
        replace(provider, "item.gtceu.tool.plunger", "%s Plunger");
        provider.add("item.gtceu.tool.plunger.tooltip", "§8Removes Fluids from Machines");
        replace(provider, "item.gtceu.tool.shears", "%s Shears");
        provider.add("item.gtceu.tool.tooltip.crafting_uses", "%s §aCrafting Uses");
        provider.add("item.gtceu.tool.tooltip.general_uses", "%s §bDurability");
        provider.add("item.gtceu.tool.tooltip.attack_damage", "%s §cAttack Damage");
        provider.add("item.gtceu.tool.tooltip.attack_speed", "%s §9Attack Speed");
        provider.add("item.gtceu.tool.tooltip.mining_speed", "%s §dMining Speed");
        provider.add("item.gtceu.tool.tooltip.harvest_level", "§eHarvest Level %s");
        provider.add("item.gtceu.tool.tooltip.harvest_level_extra", "§eHarvest Level %s §f(%s§f)");
        multiLang(provider, "item.gtceu.tool.harvest_level", "§8Wood", "§7Stone", "§aIron", "§bDiamond", "§dNetherite", "§9Duranium", "§cNeutronium");
        provider.add("item.gtceu.tool.tooltip.repair_info", "§8Hold SHIFT to show Repair Info");
        provider.add("item.gtceu.tool.tooltip.repair_material", "§8Repair with: §f§a%s");
        provider.add("item.gtceu.tool.aoe.rows", "Rows");
        provider.add("item.gtceu.tool.aoe.columns", "Columns");
        provider.add("item.gtceu.tool.aoe.layers", "Layers");
        provider.add("item.gtceu.turbine_rotor.tooltip", "Turbine Rotors for your power station");
        provider.add("metaitem.clipboard.tooltip", "Can be written on (without any writing Instrument). Right-click on Wall to place, and Shift-Right-Click to remove");
        provider.add("metaitem.behavior.mode_switch.tooltip", "Use while sneaking to switch mode");
        provider.add("metaitem.behavior.mode_switch.mode_switched", "§eMode Set to: %s");
        provider.add("metaitem.behavior.mode_switch.current_mode", "Mode: %s");
        provider.add("metaitem.tool.tooltip.primary_material", "§fMaterial: §e%s");
        provider.add("metaitem.tool.tooltip.durability", "§fDurability: §a%d / %d");
        provider.add("metaitem.tool.tooltip.rotor.efficiency", "Turbine Efficiency: §9%d%%");
        provider.add("metaitem.tool.tooltip.rotor.power", "Turbine Power: §9%d%%");
        provider.add("item.gtceu.ulv_voltage_coil.tooltip", "Primitive Coil");
        provider.add("item.gtceu.lv_voltage_coil.tooltip", "Basic Coil");
        provider.add("item.gtceu.mv_voltage_coil.tooltip", "Good Coil");
        provider.add("item.gtceu.hv_voltage_coil.tooltip", "Advanced Coil");
        provider.add("item.gtceu.ev_voltage_coil.tooltip", "Extreme Coil");
        provider.add("item.gtceu.iv_voltage_coil.tooltip", "Elite Coil");
        provider.add("item.gtceu.luv_voltage_coil.tooltip", "Master Coil");
        provider.add("item.gtceu.zpm_voltage_coil.tooltip", "Super Coil");
        provider.add("item.gtceu.uv_voltage_coil.tooltip", "Ultimate Coil");
        provider.add("item.gtceu.uhv_voltage_coil.tooltip", "Ultra Coil");
        provider.add("item.gtceu.uev_voltage_coil.tooltip", "Unreal Coil");
        provider.add("item.gtceu.uiv_voltage_coil.tooltip", "Insane Coil");
        provider.add("item.gtceu.uxv_voltage_coil.tooltip", "Epic Coil");
        provider.add("item.gtceu.opv_voltage_coil.tooltip", "Legendary Coil");
        provider.add("item.gtceu.max_voltage_coil.tooltip", "Maximum Coil");
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
        provider.add("item.gtceu.nan_certificate.tooltip", "Challenge Accepted!");
        provider.add("item.gtceu.blacklight.tooltip", "Long-Wave §dUltraviolet§7 light source");
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
        provider.add("cover.conveyor.mode", "Mode: %s");
        provider.add("cover.conveyor.mode.export", "Mode: Export");
        provider.add("cover.conveyor.mode.import", "Mode: Import");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_global", "Distribution Mode: §bRound Robin\n§7Splits items equally across connected inventories");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_prio", "Distribution Mode: §bRound Robin with Priority\n§7Tries to split items across connected inventories and considers higher priorities first.\n§7Restrictive item pipes lower the priority of a path.");
        multilineLang(provider, "cover.conveyor.distribution.insert_first", "Distribution Mode: §bPriority\n§7Will insert into the first inventory with the highest priority it can find.\n§7Restrictive item pipes lower the priority of a path.");
        multilineLang(provider, "cover.conveyor.blocks_input.enabled", "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§aEnabled");
        multilineLang(provider, "cover.conveyor.blocks_input.disabled", "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§cDisabled");
        provider.add("cover.universal.manual_import_export.mode.disabled", "Manual I/O: §bDisabled\n§7Items / Fluids will only move as specified by the cover and its filter.");
        provider.add("cover.universal.manual_import_export.mode.filtered", "Manual I/O: §bFiltered\n§7Items / Fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any)");
        provider.add("cover.universal.manual_import_export.mode.unfiltered", "Manual I/O: §bUnfiltered\n§7Items / Fluids can be moved independently of the cover mode. The filter only applies to what is inserted or extracted by this cover itself.");
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
        provider.add("cover.bucket.mode.bucket", "kL");
        provider.add("cover.bucket.mode.milli_bucket", "L");
        provider.add("cover.fluid_regulator.title", "Fluid Regulator Settings (%s)");
        multilineLang(provider, "cover.fluid_regulator.transfer_mode.description", "§eTransfer Any§r - in this mode, cover will transfer as many fluids matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply fluids in portions specified in the window underneath this button. If amount of fluids is less than portion size, fluids won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of fluids in the destination inventory, supplying additional amount of fluids if required.\n§7Tip: shift click will multiply increase/decrease amounts by 10 and ctrl click will multiply by 100.");
        provider.add("cover.fluid_regulator.supply_exact", "Supply Exact: %s");
        provider.add("cover.fluid_regulator.keep_exact", "Keep Exact: %s");
        provider.add("cover.machine_controller.title", "Machine Controller Settings");
        provider.add("cover.machine_controller.normal", "Normal");
        provider.add("cover.machine_controller.inverted", "Inverted");
        multilineLang(provider, "cover.machine_controller.invert.enabled", "§eInverted§r - in this mode, the cover will require a signal stronger than the set redstone level to run");
        multilineLang(provider, "cover.machine_controller.invert.disabled", "§eNormal§r - in this mode, the cover will require a signal weaker than the set redstone level to run");
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
        provider.add("cover.detector_base.message_normal_state", "Monitoring Status: Normal");
        provider.add("cover.detector_base.message_inverted_state", "Monitoring Status: Inverted");

        provider.add("cover.advanced_energy_detector.label", "Advanced Energy Detector");
        provider.add("cover.advanced_energy_detector.min", "Min");
        provider.add("cover.advanced_energy_detector.max", "Max");

        var advancedEnergyDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when less than the minimum EU, and stops emitting when greater than the max EU";
        multilineLang(provider, "cover.advanced_energy_detector.invert.enabled", "Output: Inverted\n\n" + advancedEnergyDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_energy_detector.invert.disabled", "Output: Normal\n\n" + advancedEnergyDetectorInvertDescription);
        var advancedEnergyDetectorModeDescription = "Change between using discrete EU values or percentages for comparing min/max against an attached energy storage.";
        multilineLang(provider, "cover.advanced_energy_detector.use_percent.enabled", "Mode: Percentage\n\n" + advancedEnergyDetectorModeDescription);
        multilineLang(provider, "cover.advanced_energy_detector.use_percent.disabled", "Mode: Discrete EU\n\n" + advancedEnergyDetectorModeDescription);

        provider.add("cover.advanced_fluid_detector.label", "Advanced Fluid Detector");
        var advancedFluidDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum L of fluid, and starts emitting when greater than the min L of fluid up to the set maximum";
        multilineLang(provider, "cover.advanced_fluid_detector.invert.enabled", "Output: Inverted\n\n" + advancedFluidDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_fluid_detector.invert.disabled", "Output: Normal\n\n" + advancedFluidDetectorInvertDescription);
        provider.add("cover.advanced_fluid_detector.max", "Max Fluid (L)");
        provider.add("cover.advanced_fluid_detector.min", "Min Fluid (L)");

        provider.add("cover.advanced_item_detector.label", "Advanced Item Detector");
        var advancedItemDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum amount of items, and starts emitting when greater than the min amount of items up to the set maximum";
        multilineLang(provider, "cover.advanced_item_detector.invert.enabled", "Output: Inverted\n\n" + advancedItemDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_item_detector.invert.disabled", "Output: Normal\n\n" + advancedItemDetectorInvertDescription);
        provider.add("cover.advanced_item_detector.max", "Max Items");
        provider.add("cover.advanced_item_detector.min", "Min Items");

        replace(provider, "item.gtceu.bucket", "%s Bucket");
        replace(provider, GTMaterials.FullersEarth.getUnlocalizedName(), "Fuller's Earth");
        replace(provider, GTMaterials.Cooperite.getUnlocalizedName(), "Sheldonite"); //greg's humor is now on 1.19...
        replace(provider, GTMaterials.HSSG.getUnlocalizedName(), "HSS-G");
        replace(provider, GTMaterials.HSSE.getUnlocalizedName(), "HSS-E");
        replace(provider, GTMaterials.HSSS.getUnlocalizedName(), "HSS-S");
        replace(provider, GTMaterials.UUMatter.getUnlocalizedName(), "UU-Matter");
        replace(provider, GTMaterials.PCBCoolant.getUnlocalizedName(), "PCB Coolant");
        replace(provider, GTMaterials.TungstenSteel.getUnlocalizedName(), "Tungstensteel");
        replace(provider, GTMaterials.OilHeavy.getUnlocalizedName(), "Heavy Oil");
        replace(provider, "block.gtceu.oil_heavy", "Heavy Oil");
        replace(provider, GTMaterials.OilLight.getUnlocalizedName(), "Light Oil");
        replace(provider, "block.gtceu.oil_light", "Light Oil");
        replace(provider, GTMaterials.RawOil.getUnlocalizedName(), "Raw Oil");
        replace(provider, "block.gtceu.oil_medium", "Raw Oil");

        replace(provider, GTMaterials.HydroCrackedButadiene.getUnlocalizedName(), "Hydro-Cracked Butadiene");
        replace(provider, GTMaterials.HydroCrackedButane.getUnlocalizedName(), "Hydro-Cracked Butane");
        replace(provider, GTMaterials.HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        replace(provider, GTMaterials.HydroCrackedButene.getUnlocalizedName(), "Hydro-Cracked Butene");
        replace(provider, GTMaterials.HydroCrackedEthane.getUnlocalizedName(), "Hydro-Cracked Ethane");
        replace(provider, GTMaterials.HydroCrackedEthylene.getUnlocalizedName(), "Hydro-Cracked Ethylene");
        replace(provider, GTMaterials.HydroCrackedPropane.getUnlocalizedName(), "Hydro-Cracked Propane");
        replace(provider, GTMaterials.HydroCrackedPropene.getUnlocalizedName(), "Hydro-Cracked Propene");
        replace(provider, GTMaterials.SteamCrackedButadiene.getUnlocalizedName(), "Steam-Cracked Butadiene");
        replace(provider, GTMaterials.SteamCrackedButane.getUnlocalizedName(), "Steam-Cracked Butane");
        replace(provider, GTMaterials.SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        replace(provider, GTMaterials.SteamCrackedButene.getUnlocalizedName(), "Steam-Cracked Butene");
        replace(provider, GTMaterials.SteamCrackedEthane.getUnlocalizedName(), "Steam-Cracked Ethane");
        replace(provider, GTMaterials.SteamCrackedEthylene.getUnlocalizedName(), "Steam-Cracked Ethylene");
        replace(provider, GTMaterials.SteamCrackedPropane.getUnlocalizedName(), "Steam-Cracked Propane");
        replace(provider, GTMaterials.SteamCrackedPropene.getUnlocalizedName(), "Steam-Cracked Propene");
        replace(provider, GTMaterials.LightlyHydroCrackedGas.getUnlocalizedName(), "Lightly Hydro-Cracked Gas");
        replace(provider, GTMaterials.LightlyHydroCrackedHeavyFuel.getUnlocalizedName(), "Lightly Hydro-Cracked Heavy Fuel");
        replace(provider, GTMaterials.LightlyHydroCrackedLightFuel.getUnlocalizedName(), "Lightly Hydro-Cracked Light Fuel");
        replace(provider, GTMaterials.LightlyHydroCrackedNaphtha.getUnlocalizedName(), "Lightly Hydro-Cracked Naphtha");
        replace(provider, GTMaterials.LightlySteamCrackedGas.getUnlocalizedName(), "Lightly Steam-Cracked Gas");
        replace(provider, GTMaterials.LightlySteamCrackedHeavyFuel.getUnlocalizedName(), "Lightly Steam-Cracked Heavy Fuel");
        replace(provider, GTMaterials.LightlySteamCrackedLightFuel.getUnlocalizedName(), "Lightly Steam-Cracked Light Fuel");
        replace(provider, GTMaterials.LightlySteamCrackedNaphtha.getUnlocalizedName(), "Lightly Steam-Cracked Naphtha");
        replace(provider, GTMaterials.SeverelyHydroCrackedGas.getUnlocalizedName(), "Severely Hydro-Cracked Gas");
        replace(provider, GTMaterials.SeverelyHydroCrackedHeavyFuel.getUnlocalizedName(), "Severely Hydro-Cracked Heavy Fuel");
        replace(provider, GTMaterials.SeverelyHydroCrackedLightFuel.getUnlocalizedName(), "Severely Hydro-Cracked Light Fuel");
        replace(provider, GTMaterials.SeverelyHydroCrackedNaphtha.getUnlocalizedName(), "Severely Hydro-Cracked Naphtha");
        replace(provider, GTMaterials.SeverelySteamCrackedGas.getUnlocalizedName(), "Severely Steam-Cracked Gas");
        replace(provider, GTMaterials.SeverelySteamCrackedHeavyFuel.getUnlocalizedName(), "Severely Steam-Cracked Heavy Fuel");
        replace(provider, GTMaterials.SeverelySteamCrackedLightFuel.getUnlocalizedName(), "Severely Steam-Cracked Light Fuel");
        replace(provider, GTMaterials.SeverelySteamCrackedNaphtha.getUnlocalizedName(), "Severely Steam-Cracked Naphtha");

        replace(provider, GTMaterials.Zeron100.getUnlocalizedName(), "Zeron-100");
        replace(provider, GTMaterials.IncoloyMA956.getUnlocalizedName(), "Incoloy MA-956");
        replace(provider, GTMaterials.Stellite100.getUnlocalizedName(), "Stellite-100");
        replace(provider, GTMaterials.HastelloyC276.getUnlocalizedName(), "Hastelloy C-276");

        replace(provider, GTBlocks.BATTERY_EMPTY_TIER_I.get().getDescriptionId(), "Empty Tier I Capacitor");
        replace(provider, GTBlocks.BATTERY_LAPOTRONIC_EV.get().getDescriptionId(), "EV Lapotronic Capacitor");
        replace(provider, GTBlocks.BATTERY_LAPOTRONIC_IV.get().getDescriptionId(), "IV Lapotronic Capacitor");
        replace(provider, GTBlocks.BATTERY_EMPTY_TIER_II.get().getDescriptionId(), "Empty Tier II Capacitor");
        replace(provider, GTBlocks.BATTERY_LAPOTRONIC_LuV.get().getDescriptionId(), "LuV Lapotronic Capacitor");
        replace(provider, GTBlocks.BATTERY_LAPOTRONIC_ZPM.get().getDescriptionId(), "ZPM Lapotronic Capacitor");
        replace(provider, GTBlocks.BATTERY_EMPTY_TIER_III.get().getDescriptionId(), "Empty Tier III Capacitor");
        replace(provider, GTBlocks.BATTERY_LAPOTRONIC_UV.get().getDescriptionId(), "UV Lapotronic Capacitor");
        replace(provider, GTBlocks.BATTERY_ULTIMATE_UHV.get().getDescriptionId(), "UHV Ultimate Capacitor");

        provider.add("item.netherrack_nether_quartz", "Nether Quartz Ore");
        provider.add("block.surface_rock", "%s Surface Rock");

        provider.add("item.gtceu.tiny_gunpowder_dust", "Tiny Pile of Gunpowder");
        provider.add("item.gtceu.small_gunpowder_dust", "Small Pile of Gunpowder");
        provider.add("item.gtceu.tiny_paper_dust", "Tiny Pile of Chad");
        provider.add("item.gtceu.small_paper_dust", "Small Pile of Chad");
        provider.add("item.gtceu.paper_dust", "Chad");
        provider.add("item.gtceu.tiny_rare_earth_dust", "Tiny Pile of Rare Earth");
        provider.add("item.gtceu.small_rare_earth_dust", "Small Pile of Rare Earth");
        provider.add("item.gtceu.rare_earth_dust", "Rare Earth");
        provider.add("item.gtceu.tiny_ash_dust", "Tiny Pile of Ashes");
        provider.add("item.gtceu.small_ash_dust", "Small Pile of Ashes");
        provider.add("item.gtceu.ash_dust", "Ashes");
        provider.add("item.gtceu.tiny_bone_dust", "Tiny Pile of Bone Meal");
        provider.add("item.gtceu.small_bone_dust", "Small Pile of Bone Meal");
        provider.add("item.gtceu.bone_dust", "Bone Meal");
        provider.add("item.gtceu.refined_cassiterite_sand_ore", "Refined Cassiterite Sand");
        provider.add("item.gtceu.purified_cassiterite_sand_ore", "Purified Cassiterite Sand");
        provider.add("item.gtceu.crushed_cassiterite_sand_ore", "Ground Cassiterite Sand");
        provider.add("item.gtceu.tiny_cassiterite_sand_dust", "Tiny Pile of Cassiterite Sand");
        provider.add("item.gtceu.small_cassiterite_sand_dust", "Small Pile of Cassiterite Sand");
        provider.add("item.gtceu.impure_cassiterite_sand_dust", "Impure Pile of Cassiterite Sand");
        provider.add("item.gtceu.pure_cassiterite_sand_dust", "Purified Pile of Cassiterite Sand");
        provider.add("item.gtceu.cassiterite_sand_dust", "Cassiterite Sand");
        provider.add("item.gtceu.tiny_dark_ash_dust", "Tiny Pile of Dark Ashes");
        provider.add("item.gtceu.small_dark_ash_dust", "Small Pile of Dark Ashes");
        provider.add("item.gtceu.dark_ash_dust", "Dark Ashes");
        provider.add("item.gtceu.tiny_ice_dust", "Tiny Pile of Crushed Ice");
        provider.add("item.gtceu.small_ice_dust", "Small Pile of Crushed Ice");
        provider.add("item.gtceu.ice_dust", "Crushed Ice");
        provider.add("item.gtceu.sugar_gem", "Sugar Cube");
        provider.add("item.gtceu.chipped_sugar_gem", "Small Sugar Cubes");
        provider.add("item.gtceu.flawed_sugar_gem", "Tiny Sugar Cube");
        provider.add("item.gtceu.tiny_rock_salt_dust", "Tiny Pile of Rock Salt");
        provider.add("item.gtceu.small_rock_salt_dust", "Small Pile of Rock Salt");
        provider.add("item.gtceu.impure_rock_salt_dust", "Impure Pile of Rock Salt");
        provider.add("item.gtceu.pure_rock_salt_dust", "Purified Pile of Rock Salt");
        provider.add("item.gtceu.rock_salt_dust", "Rock Salt");
        provider.add("item.gtceu.tiny_salt_dust", "Tiny Pile of Salt");
        provider.add("item.gtceu.small_salt_dust", "Small Pile of Salt");
        provider.add("item.gtceu.impure_salt_dust", "Impure Pile of Salt");
        provider.add("item.gtceu.pure_salt_dust", "Purified Pile of Salt");
        provider.add("item.gtceu.salt_dust", "Salt");
        provider.add("item.gtceu.tiny_wood_dust", "Tiny Pile of Wood Pulp");
        provider.add("item.gtceu.small_wood_dust", "Small Pile of Wood Pulp");
        provider.add("item.gtceu.wood_dust", "Wood Pulp");
        provider.add("item.gtceu.wood_plate", "Wood Plank");
        provider.add("item.gtceu.long_wood_rod", "Long Wood Stick");
        provider.add("item.gtceu.wood_bolt", "Short Wood Stick");
        provider.add("item.gtceu.tiny_treated_wood_dust", "Tiny Pile of Treated Wood Pulp");
        provider.add("item.gtceu.small_treated_wood_dust", "Small Pile of Treated Wood Pulp");
        provider.add("item.gtceu.treated_wood_dust", "Treated Wood Pulp");
        provider.add("item.gtceu.treated_wood_plate", "Treated Wood Plank");
        provider.add("item.gtceu.treated_wood_rod", "Treated Wood Stick");
        provider.add("item.gtceu.long_treated_wood_rod", "Long Treated Wood Stick");
        provider.add("item.gtceu.treated_wood_bolt", "Short Treated Wood Stick");
        provider.add("item.gtceu.glass_gem", "Glass Crystal");
        provider.add("item.gtceu.chipped_glass_gem", "Chipped Glass Crystal");
        provider.add("item.gtceu.flawed_glass_gem", "Flawed Glass Crystal");
        provider.add("item.gtceu.flawless_glass_gem", "Flawless Glass Crystal");
        provider.add("item.gtceu.exquisite_glass_gem", "Exquisite Glass Crystal");
        provider.add("item.gtceu.glass_plate", "Glass Pane");
        provider.add("item.gtceu.tiny_blaze_dust", "Tiny Pile of Blaze Powder");
        provider.add("item.gtceu.small_blaze_dust", "Small Pile of Blaze Powder");
        provider.add("item.gtceu.tiny_sugar_dust", "Tiny Pile of Sugar");
        provider.add("item.gtceu.small_sugar_dust", "Small Pile of Sugar");
        provider.add("item.gtceu.tiny_basaltic_mineral_sand_dust", "Tiny Pile of Basaltic Mineral Sand");
        provider.add("item.gtceu.small_basaltic_mineral_sand_dust", "Small Pile of Basaltic Mineral Sand");
        provider.add("item.gtceu.basaltic_mineral_sand_dust", "Basaltic Mineral Sand");
        provider.add("item.gtceu.tiny_granitic_mineral_sand_dust", "Tiny Pile of Granitic Mineral Sand");
        provider.add("item.gtceu.small_granitic_mineral_sand_dust", "Small Pile of Granitic Mineral Sand");
        provider.add("item.gtceu.granitic_mineral_sand_dust", "Granitic Mineral Sand");
        provider.add("item.gtceu.tiny_garnet_sand_dust", "Tiny Pile of Garnet Sand");
        provider.add("item.gtceu.small_garnet_sand_dust", "Small Pile of Garnet Sand");
        provider.add("item.gtceu.garnet_sand_dust", "Garnet Sand");
        provider.add("item.gtceu.tiny_quartz_sand_dust", "Tiny Pile of Quartz Sand");
        provider.add("item.gtceu.small_quartz_sand_dust", "Small Pile of Quartz Sand");
        provider.add("item.gtceu.quartz_sand_dust", "Quartz Sand");
        provider.add("item.gtceu.tiny_glauconite_sand_dust", "Tiny Pile of Glauconite Sand");
        provider.add("item.gtceu.small_glauconite_sand_dust", "Small Pile of Glauconite Sand");
        provider.add("item.gtceu.glauconite_sand_dust", "Glauconite Sand");
        provider.add("item.gtceu.refined_bentonite_ore", "Refined Bentonite");
        provider.add("item.gtceu.purified_bentonite_ore", "Purified Bentonite");
        provider.add("item.gtceu.crushed_bentonite_ore", "Ground Bentonite");
        provider.add("item.gtceu.tiny_bentonite_dust", "Tiny Pile of Bentonite");
        provider.add("item.gtceu.small_bentonite_dust", "Small Pile of Bentonite");
        provider.add("item.gtceu.impure_bentonite_dust", "Impure Pile of Bentonite");
        provider.add("item.gtceu.pure_bentonite_dust", "Purified Pile of Bentonite");
        provider.add("item.gtceu.bentonite_dust", "Bentonite");
        provider.add("item.gtceu.tiny_fullers_earth_dust", "Tiny Pile of Fullers Earth");
        provider.add("item.gtceu.small_fullers_earth_dust", "Small Pile of Fullers Earth");
        provider.add("item.gtceu.fullers_earth_dust", "Fullers Earth");
        provider.add("item.gtceu.refined_pitchblende_ore", "Refined Pitchblende");
        provider.add("item.gtceu.purified_pitchblende_ore", "Purified Pitchblende");
        provider.add("item.gtceu.crushed_pitchblende_ore", "Ground Pitchblende");
        provider.add("item.gtceu.tiny_pitchblende_dust", "Tiny Pile of Pitchblende");
        provider.add("item.gtceu.small_pitchblende_dust", "Small Pile of Pitchblende");
        provider.add("item.gtceu.impure_pitchblende_dust", "Impure Pile of Pitchblende");
        provider.add("item.gtceu.pure_pitchblende_dust", "Purified Pile of Pitchblende");
        provider.add("item.gtceu.pitchblende_dust", "Pitchblende");
        provider.add("item.gtceu.refined_talc_ore", "Refined Talc");
        provider.add("item.gtceu.purified_talc_ore", "Purified Talc");
        provider.add("item.gtceu.crushed_talc_ore_ore", "Ground Talc");
        provider.add("item.gtceu.tiny_talc_dust", "Tiny Pile of Talc");
        provider.add("item.gtceu.small_talc_dust", "Small Pile of Talc");
        provider.add("item.gtceu.impure_talc_dust", "Impure Pile of Talc");
        provider.add("item.gtceu.pure_talc_dust", "Purified Pile of Talc");
        provider.add("item.gtceu.talc_dust", "Talc");
        provider.add("item.gtceu.tiny_wheat_dust", "Tiny Pile of Flour");
        provider.add("item.gtceu.small_wheat_dust", "Small Pile of Flour");
        provider.add("item.gtceu.wheat_dust", "Flour");
        provider.add("item.gtceu.tiny_meat_dust", "Tiny Pile of Mince Meat");
        provider.add("item.gtceu.small_meat_dust", "Small Pile of Mince Meat");
        provider.add("item.gtceu.meat_dust", "Mince Meat");
        provider.add("item.gtceu.borosilicate_glass_ingot", "Borosilicate Glass Bar");
        provider.add("item.gtceu.fine_borosilicate_glass_wire", "Borosilicate Glass Fibers");
        provider.add("item.gtceu.tiny_platinum_group_sludge_dust", "Tiny Clump of Platinum Group Sludge");
        provider.add("item.gtceu.small_platinum_group_sludge_dust", "Small Clump of Platinum Group Sludge");
        provider.add("item.gtceu.platinum_group_sludge_dust", "Platinum Group Sludge");
        provider.add("item.gtceu.tiny_platinum_raw_dust", "Tiny Pile of Raw Platinum Powder");
        provider.add("item.gtceu.small_platinum_raw_dust", "Small Pile of Raw Platinum Powder");
        provider.add("item.gtceu.platinum_raw_dust", "Raw Platinum Powder");
        provider.add("item.gtceu.tiny_palladium_raw_dust", "Tiny Pile of Raw Palladium Powder");
        provider.add("item.gtceu.small_palladium_raw_dust", "Small Pile of Raw Palladium Powder");
        provider.add("item.gtceu.palladium_raw_dust", "Raw Palladium Powder");
        provider.add("item.gtceu.tiny_inert_metal_mixture_dust", "Tiny Pile of Inert Metal Mixture");
        provider.add("item.gtceu.small_inert_metal_mixture_dust", "Small Pile of Inert Metal Mixture");
        provider.add("item.gtceu.inert_metal_mixture_dust", "Inert Metal Mixture");
        provider.add("item.gtceu.tiny_rarest_metal_mixture_dust", "Tiny Pile of Rarest Metal Mixture");
        provider.add("item.gtceu.small_rarest_metal_mixture_dust", "Small Pile of Rarest Metal Mixture");
        provider.add("item.gtceu.rarest_metal_mixture_dust", "Rarest Metal Mixture");
        provider.add("item.gtceu.tiny_platinum_sludge_residue_dust", "Tiny Pile of Platinum Sludge Residue");
        provider.add("item.gtceu.small_platinum_sludge_residue_dust", "Small Pile of Platinum Sludge Residue");
        provider.add("item.gtceu.platinum_sludge_residue_dust", "Platinum Sludge Residue");
        provider.add("item.gtceu.tiny_iridium_metal_residue_dust", "Tiny Pile of Iridium Metal Residue");
        provider.add("item.gtceu.small_iridium_metal_residue_dust", "Small Pile of Iridium Metal Residue");
        provider.add("item.gtceu.iridium_metal_residue_dust", "Iridium Metal Residue");

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
        provider.add("tile.gtceu.seal.name", "Sealed Block");
        provider.add("tile.gtceu.foam.name", "Foam");
        provider.add("tile.gtceu.reinforced_foam.name", "Reinforced Foam");
        provider.add("tile.gtceu.petrified_foam.name", "Petrified Foam");
        provider.add("tile.gtceu.reinforced_stone.name", "Reinforced Stone");
        provider.add("tile.brittle_charcoal.name", "Brittle Charcoal");
        multilineLang(provider, "tile.brittle_charcoal.tooltip", "Produced by the Charcoal Pile Igniter.\nMine this to get Charcoal.");
        provider.add("metaitem.prospector.mode.ores", "§aOre Prospection Mode§r");
        provider.add("metaitem.prospector.mode.fluid", "§bFluid Prospection Mode§r");
        provider.add("metaitem.prospector.mode.bedrock_ore", "§bBedrock Ore Prospection Mode§r");
        provider.add("metaitem.prospector.tooltip.radius", "Scans range in a %s Chunk Radius");
        provider.add("metaitem.prospector.tooltip.modes", "Available Modes:");
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

        provider.add("behaviour.softhammer", "Activates and Deactivates Machines");
        provider.add("behaviour.hammer", "Turns on and off Muffling for Machines (by hitting them)");
        provider.add("behaviour.wrench", "Rotates Blocks on Rightclick");
        provider.add("behaviour.boor.by", "by %s");
        provider.add("behaviour.paintspray.solvent.tooltip", "Can remove color from things");
        provider.add("behaviour.paintspray.white.tooltip", "Can paint things in White");
        provider.add("behaviour.paintspray.orange.tooltip", "Can paint things in Orange");
        provider.add("behaviour.paintspray.magenta.tooltip", "Can paint things in Magenta");
        provider.add("behaviour.paintspray.light_blue.tooltip", "Can paint things in Light Blue");
        provider.add("behaviour.paintspray.yellow.tooltip", "Can paint things in Yellow");
        provider.add("behaviour.paintspray.lime.tooltip", "Can paint things in Lime");
        provider.add("behaviour.paintspray.pink.tooltip", "Can paint things in Pink");
        provider.add("behaviour.paintspray.gray.tooltip", "Can paint things in Gray");
        provider.add("behaviour.paintspray.light_gray.tooltip", "Can paint things in Light Gray");
        provider.add("behaviour.paintspray.cyan.tooltip", "Can paint things in Cyan");
        provider.add("behaviour.paintspray.purple.tooltip", "Can paint things in Purple");
        provider.add("behaviour.paintspray.blue.tooltip", "Can paint things in Blue");
        provider.add("behaviour.paintspray.brown.tooltip", "Can paint things in Brown");
        provider.add("behaviour.paintspray.green.tooltip", "Can paint things in Green");
        provider.add("behaviour.paintspray.red.tooltip", "Can paint things in Red");
        provider.add("behaviour.paintspray.black.tooltip", "Can paint things in Black");
        provider.add("behaviour.paintspray.uses", "Remaining Uses: %d");
        provider.add("behaviour.prospecting", "Usable for Prospecting");


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
        provider.add("gtceu.machine.mv_fluid_drilling_rig.tooltip", "Oil Extraction Pump");
        provider.add("gtceu.machine.hv_fluid_drilling_rig.tooltip", "Does not perform Fracking");
        provider.add("gtceu.machine.ev_fluid_drilling_rig.tooltip", "Well Drainer");
        provider.add("gtceu.machine.cleanroom.tooltip", "Keeping those pesky particles out");
        provider.add("gtceu.machine.charcoal_pile.tooltip", "Underground fuel bakery");
        provider.add("gtceu.machine.available_recipe_map_1.tooltip", "Available Recipe Maps: %s");
        provider.add("gtceu.machine.available_recipe_map_2.tooltip", "Available Recipe Maps: %s, %s");
        provider.add("gtceu.machine.available_recipe_map_3.tooltip", "Available Recipe Maps: %s, %s, %s");
        provider.add("gtceu.machine.available_recipe_map_4.tooltip", "Available Recipe Maps: %s, %s, %s, %s");

        multiLang(provider, "gtceu.machine.power_substation.tooltip",
                "The heart of a centralized power grid",
                "§fCapacitors§7 do not need to be all the same tier.",
                "Allows up to §f%d Capacitor Layers§7.",
                "Loses energy equal to §f1%%§7 of total capacity every §f24 hours§7.",
                "Capped at §f%d kEU/t§7 passive loss per Capacitor Block.",
                "Can use",
                " Laser Hatches§7."
        );

        multiLang(provider, "gtceu.machine.active_transformer.tooltip",
                "Transformers: Lasers in Disguise",
                "Can combine any number of Energy §fInputs§7 into any number of Energy §fOutputs§7.",
                "Can transmit power at incredible distance with",
                "Lasers§7."
        );

        multiLang(provider, "gtceu.machine.laser_hatch.source.tooltip",
                "Transmitting power at distance",
                "§cLaser cables must be in a straight line!§7"
        );

        multiLang(provider, "gtceu.machine.laser_hatch.target.tooltip",
                "Receiving power from distance",
                "§cLaser cables must be in a straight line!§7"
        );

        multiLang(provider, "gtceu.machine.endpoint.tooltip",
                "Connect with §fLong Distance Pipe§7 blocks to create a pipeline.",
                "Pipelines must have exactly §f1 Input§7 and §f1 Output§7 endpoint.",
                "Only pipeline endpoints need to be §fchunk-loaded§7.");
        provider.add("gtceu.machine.endpoint.tooltip.min_length", "§bMinimum Endpoint Distance: §f%d Blocks");

        provider.add("gtceu.universal.disabled", "Multiblock Sharing §4Disabled");
        provider.add("gtceu.universal.enabled", "Multiblock Sharing §aEnabled");

        provider.add("gtceu.bus.collapse_true", "Bus will collapse Items");
        provider.add("gtceu.bus.collapse_false", "Bus will not collapse Items");
        provider.add("gtceu.bus.collapse.error", "Bus must be attached to multiblock first");

        provider.add("gtceu.machine.item_bus.import.tooltip", "Item Input for Multiblocks");
        provider.add("gtceu.machine.item_bus.export.tooltip", "Item Output for Multiblocks");
        provider.add("gtceu.machine.fluid_hatch.import.tooltip", "Fluid Input for Multiblocks");
        provider.add("gtceu.machine.fluid_hatch.export.tooltip", "Fluid Output for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.input.tooltip", "Energy Input for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.input_hi_amp.tooltip", "Multiple Ampere Energy Input for Multiblocks");
        provider.add("gtceu.machine.substation_hatch.input.tooltip", "Energy Input for the Power Substation");
        provider.add("gtceu.machine.energy_hatch.output.tooltip", "Energy Output for Multiblocks");
        provider.add("gtceu.machine.energy_hatch.output_hi_amp.tooltip", "Multiple Ampere Energy Output for Multiblocks");
        provider.add("gtceu.machine.substation_hatch.output.tooltip", "Energy Output for the Power Substation");
        provider.add("gtceu.machine.me.item_export.tooltip", "Stores items directly into an ME network.");
        provider.add("gtceu.machine.me.fluid_export.tooltip", "Stores fluids directly into an ME network.");
        provider.add("gtceu.machine.me.fluid_import.tooltip", "Fetches fluids from an ME network automatically.");
        provider.add("gtceu.machine.me.item_import.tooltip", "Fetches items from an ME network automatically.");
        provider.add("gtceu.machine.me.export.tooltip", "Has infinite capacity before connecting to ME network.");

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
        provider.add("gtceu.maintenance.configurable_duration.modify", "Modify Duration:");
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
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity", "§9Fluid Capacity: §f%d mB");
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity_mult", "§9Fluid Capacity: §f%d §7Tanks, §f%d L §7each");
        provider.add("gtceu.universal.tooltip.fluid_stored", "§dFluid Stored: §f%s, %d mB");
        provider.add("gtceu.universal.tooltip.fluid_transfer_rate", "§bTransfer Rate: §f%d mB/t");
        provider.add("gtceu.universal.tooltip.parallel", "§dMax Parallel: §f%d");
        provider.add("gtceu.universal.tooltip.working_area", "§bWorking Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.chunk_mode", "Chunk Mode: ");
        provider.add("gtceu.universal.tooltip.silk_touch", "Silk Touch: ");
        provider.add("gtceu.universal.tooltip.working_area_chunks", "§bWorking Area: §f%dx%d Chunks");
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
        provider.add("gtceu.recipe.total", "Total: %,d EU");
        provider.add("gtceu.recipe.eu", "Usage: %,d EU/t (%s§r)");
        provider.add("gtceu.recipe.eu_inverted", "Generation: %,d EU/t");
        provider.add("gtceu.recipe.duration", "Duration: %,.2f secs");
        provider.add("gtceu.recipe.amperage", "Amperage: %,d");
        provider.add("gtceu.recipe.not_consumed", "Does not get consumed in the process");
        provider.add("gtceu.recipe.chance", "Chance: %s +%s/tier");
        provider.add("gtceu.recipe.temperature", "Temperature: %,dK");
        provider.add("gtceu.recipe.temperature_and_coil", "Temp.: %,dK (%s)");
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
        provider.add("gtceu.fluid.liquid_generic", "Liquid %s");
        provider.add("gtceu.fluid.generic", "%s");
        provider.add("gtceu.fluid.gas_generic", "%s Gas");
        provider.add("gtceu.fluid.gas_vapor", "%s Vapor");
        provider.add("gtceu.fluid.plasma", "%s Plasma");
        provider.add("gtceu.fluid.molten", "Molten %s");
        provider.add("gtceu.fluid.empty", "Empty");
        provider.add("gtceu.fluid.amount", "§9Amount: %d/%d mB");
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
        provider.add("gtceu.fluid_pipe.capacity", "§9Capacity: §f%d mB");
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
        provider.add("gtceu.multiblock.waiting", "WARNING: Machine is waiting.");
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
        provider.add("gtceu.multiblock.pattern.error.batteries", "§cAll batteries must be the same§r");
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
        provider.add("gtceu.multiblock.power_substation.stored", "Stored: %s EU");
        provider.add("gtceu.multiblock.power_substation.capacity", "Capacity: %s EU");
        provider.add("gtceu.multiblock.power_substation.passive_drain", "Passive Drain: %s EU/t");
        provider.add("gtceu.multiblock.power_substation.average_io", "Avg. I/O: %s EU/t");
        provider.add("gtceu.multiblock.power_substation.average_io_hover", "The average change in energy of the Power Substation's internal energy bank");
        provider.add("gtceu.multiblock.power_substation.time_to_fill", "Time to fill: %s");
        provider.add("gtceu.multiblock.power_substation.time_to_drain", "Time to drain: %s");
        provider.add("gtceu.multiblock.power_substation.time_seconds", "%s Seconds");
        provider.add("gtceu.multiblock.power_substation.time_minutes", "%s Minutes");
        provider.add("gtceu.multiblock.power_substation.time_hours", "%s Hours");
        provider.add("gtceu.multiblock.power_substation.time_days", "%s Days");
        provider.add("gtceu.multiblock.power_substation.time_years", "%s Years");
        provider.add("gtceu.multiblock.power_substation.time_forever", "Forever");
        provider.add("gtceu.multiblock.power_substation.under_one_hour_left", "Less than 1 hour until fully drained!");
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
        provider.add("gtceu.creative.energy.sink", "Sink");
        provider.add("gtceu.creative.energy.source", "Source");
        provider.add("gtceu.creative.activity.on", "Active");
        provider.add("gtceu.creative.activity.off", "Not active");
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
        // gui
        provider.add("gtceu.recipe_type.show_recipes", "Show Recipes");
        provider.add("gtceu.recipe_logic.insufficient_fuel", "Insufficient Fuel");
        provider.add("gtceu.recipe_logic.insufficient_in", "Insufficient Inputs");
        provider.add("gtceu.recipe_logic.insufficient_out", "Insufficient Outputs");
        provider.add("gtceu.recipe_logic.condition_fails", "Condition Fails");
        provider.add("gtceu.gui.cover_setting.title", "Cover Settings");
        provider.add("gtceu.gui.output_setting.title", "Output Settings");
        provider.add("gtceu.gui.circuit.title", "Circuit Settings");
        multiLang(provider, "gtceu.gui.output_setting.tooltips", "left-click to tune the item auto output", "right-click to tune the fluid auto output.");
        provider.add("gtceu.gui.item_auto_output.allow_input.enabled", "allow items input from the output side");
        provider.add("gtceu.gui.item_auto_output.allow_input.disabled", "disable items input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.allow_input.enabled", "allow fluids input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.allow_input.disabled", "disable fluids input from the output side");
        provider.add("gtceu.gui.auto_output.name", "auto");
        provider.add("gtceu.gui.overclock.title", "Overclock Tier");
        provider.add("gtceu.gui.overclock.range", "Available Tiers [%s, %s]");

        provider.add("gtceu.gui.machinemode.title", "Active Machine Mode");

        provider.add("gtceu.gui.content.chance_0", "§cNot Consumed§r");
        provider.add("gtceu.gui.content.chance_0_short", "§cNC§r");
        provider.add("gtceu.gui.content.chance_1", "Chance: %s%%");
        provider.add("gtceu.gui.content.tier_boost", "Tier Chance: +%s%%/tier");

        provider.add("gtceu.gui.content.per_tick", "§aConsumed/Produced Per Tick§r");
        provider.add("gtceu.gui.content.tips.per_tick_short", "§a/tick§r");
        provider.add("gtceu.gui.content.tips.per_second_short", "§a/second§r");

        provider.add("gtceu.gui.content.units.per_tick", "/t");
        provider.add("gtceu.gui.content.units.per_second", "/s");

        provider.add("gtceu.gui.me_network.online", "Network Status: §2Online§r");
        provider.add("gtceu.gui.me_network.offline", "Network Status: §4Offline§r");
        provider.add("gtceu.gui.config_slot", "§fConfig Slot§r");
        provider.add("gtceu.gui.config_slot.set", "§7Click to §bset/select§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.scroll", "§7Scroll wheel to §achange§7 config amount.§r");
        provider.add("gtceu.gui.config_slot.remove", "§7Right click to §4clear§7 config slot.§r");

        provider.add("gtceu.machine.parallel_hatch.display", "Adjust the maximum parallel of the multiblock");
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
    protected static String getSubKey(String key, int index) {
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
    protected static void multiLang(RegistrateLangProvider provider, String key, String... values) {
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
    public static List<MutableComponent> getMultiLang(String key) {
        var outputKeys = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (LocalizationUtils.exist(next)) {
            outputKeys.add(next);
            next = getSubKey(key, ++i);
        }
        return outputKeys.stream().map(Component::translatable).collect(Collectors.toList());
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
    public static List<MutableComponent> getMultiLang(String key, Object... args) {
        var outputKeys = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (LocalizationUtils.exist(next)) {
            outputKeys.add(next);
            next = getSubKey(key, ++i);
        }
        return outputKeys.stream().map(k -> Component.translatable(k, args)).collect(Collectors.toList());
    }

    /**
     * See {@link #getMultiLang(String)}. If no multiline key is available, get single instead.
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys.
     */
    public static List<MutableComponent> getSingleOrMultiLang(String key) {
        List<MutableComponent> multiLang = getMultiLang(key);

        if (!multiLang.isEmpty()) {
            return multiLang;
        }


        return List.of(Component.translatable(key));
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
    protected static void multilineLang(RegistrateLangProvider provider, String key, String multiline) {
        var lines = multiline.split("\n");
        multiLang(provider, key, lines);
    }

    /**
     * Replace a value in a language provider's mappings
     *
     * @param provider the provider whose mappings should be modified
     * @param key the key for the value
     * @param value the value to use in place of the old one
     */
    public static void replace(@NotNull RegistrateLangProvider provider, @NotNull String key, @NotNull String value) {
        try {
            // the regular lang mappings
            Field field = LanguageProvider.class.getDeclaredField("data");
            field.setAccessible(true);
            // noinspection unchecked
            Map<String, String> map = (Map<String, String>) field.get(provider);
            map.put(key, value);

            // upside-down lang mappings
            Field upsideDownField = RegistrateLangProvider.class.getDeclaredField("upsideDown");
            upsideDownField.setAccessible(true);
            // noinspection unchecked
            map = (Map<String, String>) field.get(upsideDownField.get(provider));

            Method toUpsideDown = RegistrateLangProvider.class.getDeclaredMethod("toUpsideDown", String.class);
            toUpsideDown.setAccessible(true);

            map.put(key, (String) toUpsideDown.invoke(provider, value));
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error replacing entry in datagen.", e);
        }
    }
}
