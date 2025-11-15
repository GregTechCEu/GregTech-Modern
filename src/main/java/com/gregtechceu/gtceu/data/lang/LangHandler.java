package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.data.LanguageProvider;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LangHandler {

    public static void init(RegistrateLangProvider provider) {
        AdvancementLang.init(provider);
        BlockLang.init(provider);
        IntegrationLang.init(provider);
        ItemLang.init(provider);
        MachineLang.init(provider);
        ToolLang.init(provider);
        ConfigurationLang.init(provider);

        provider.add("gtceu.gui.editor.tips.citation", "Number of citations");
        provider.add("gtceu.gui.editor.group.recipe_type", "cap");
        provider.add("ldlib.gui.editor.register.editor.gtceu.rtui", "RecipeType UI Project");
        provider.add("ldlib.gui.editor.register.editor.gtceu.mui", "Machine UI Project");
        provider.add("ldlib.gui.editor.register.editor.gtceu.template_tab", "templates");
        provider.add("ldlib.gui.editor.group.widget.gtm_container", "GTM Container Widgets");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_item_slot", "GTM Item Slot");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_fluid_slot", "GTM Fluid Slot");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_phantom_item_slot", "GTM Phantom Item Slot");
        provider.add("ldlib.gui.editor.register.widget.container.gtm_phantom_fluid_slot", "GTM Phantom Fluid Slot");

        provider.add("curios.identifier.gtceu_magnet", "GTCEu Magnet");
        // capabilities
        provider.add("recipe.capability.eu.name", "GTCEu Energy");
        provider.add("recipe.capability.fluid.name", "Fluid");
        provider.add("recipe.capability.item.name", "Item");
        multiLang(provider, "gtceu.oc.tooltip", "Min: %s", "Left click to increase the OC",
                "Right click to decrease the OC", "Middle click to reset the OC",
                "Hold Shift to change by Perfect OC");

        provider.add("recipe.condition.thunder.tooltip", "Thunder Level: %d");
        provider.add("recipe.condition.rain.tooltip", "Rain Level: %d");
        provider.add("recipe.condition.dimension.tooltip", "Dimension: %s");
        provider.add("recipe.condition.dimension_marker.tooltip", "Dimension:");
        provider.add("recipe.condition.biome.tooltip", "Biome: %s");
        provider.add("recipe.condition.pos_y.tooltip", "Y Level: %d <= Y <= %d");
        provider.add("recipe.condition.steam_vent.tooltip", "Clean steam vent");
        provider.add("recipe.condition.adjacent_fluid.tooltip", "Fluid blocks around");
        provider.add("recipe.condition.adjacent_block.tooltip", "Blocks around");
        provider.add("recipe.condition.eu_to_start.tooltip", "EU to Start: %d%s");
        provider.add("recipe.condition.daytime.day.tooltip", "Requires day time to work");
        provider.add("recipe.condition.daytime.night.tooltip", "Requires night time to work");
        provider.add("recipe.condition.gamestage.unlocked_stage", "Unlocked at stage: %s");
        provider.add("recipe.condition.gamestage.locked_stage", "Locked at stage: %s");
        provider.add("recipe.condition.quest.completed.tooltip", "Requires %s completed");
        provider.add("recipe.condition.quest.not_completed.tooltip", "Requires %s not completed");

        provider.add("gtceu.io.import", "Import");
        provider.add("gtceu.io.export", "Export");
        provider.add("gtceu.io.both", "Both");
        provider.add("gtceu.io.none", "None");

        provider.add("gtceu.multiblock.page_switcher.io.import", "§2Inputs");
        provider.add("gtceu.multiblock.page_switcher.io.export", "§4Outputs");
        provider.add("gtceu.multiblock.page_switcher.io.both", "§5Combined Inputs + Outputs");

        provider.add("enchantment.disjunction", "Disjunction");

        provider.add("item.invalid.name", "Invalid item");
        provider.add("fluid.empty", "Empty");
        provider.add("gtceu.tooltip.hold_shift", "§7Hold SHIFT for more info");
        provider.add("gtceu.tooltip.hold_ctrl", "§7Hold CTRL for more info");
        provider.add("gtceu.tooltip.fluid_pipe_hold_shift", "§7Hold SHIFT to show Fluid Containment Info");
        provider.add("gtceu.tooltip.tool_fluid_hold_shift",
                "§7Hold SHIFT to show Fluid Containment and Tool Info");
        provider.add("metaitem.generic.fluid_container.tooltip", "%d/%dL %s");
        provider.add("metaitem.generic.electric_item.tooltip", "%d/%d EU - Tier %s");
        provider.add("metaitem.generic.electric_item.stored", "%d/%d EU (%s)");
        provider.add("metaitem.electric.discharge_mode.enabled", "§eDischarge Mode Enabled");
        provider.add("metaitem.electric.discharge_mode.disabled", "§eDischarge Mode Disabled");
        provider.add("metaitem.electric.discharge_mode.tooltip", "Use while sneaking to toggle discharge mode");
        provider.add("metaitem.dust.tooltip.purify", "Right click a Cauldron to get clean Dust");
        provider.add("metaitem.crushed.tooltip.purify", "Right click a Cauldron to get Purified Ore");
        provider.add("metaitem.int_circuit.configuration", "Configuration: %d");

        provider.add("metaitem.machine_configuration.mode", "§aConfiguration Mode:§r %s");
        provider.add("gtceu.mode.fluid", "§9Fluid§r");
        provider.add("gtceu.mode.item", "§6Item§r");
        provider.add("gtceu.mode.both", "§dBoth (Fluid And Item)§r");

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

        provider.add("command.gtceu.medical_condition.get", "Player %s has these medical conditions:");
        provider.add("command.gtceu.medical_condition.get.empty", "Player %s has no medical conditions.");
        provider.add("command.gtceu.medical_condition.get.element", "Condition %s§r: %s minutes %s seconds");
        provider.add("command.gtceu.medical_condition.get.element.permanent",
                "Condition %s§r: %s minutes %s seconds (permanent)");
        provider.add("command.gtceu.dump_data.success", "Dumped %s resources from registry %s to %s");
        provider.add("command.gtceu.place_vein.failure", "Failed to place vein %s at position %s");
        provider.add("command.gtceu.place_vein.success", "Placed vein %s at position %s");
        provider.add("command.gtceu.share_prospection_data.notification", "%s is sharing prospecting data with you!");
        provider.add("command.gtceu.cape.failure.does_not_exist", "Cape %s does not exist");
        provider.add("command.gtceu.cape.give.failed", "No new capes were unlocked");
        provider.add("command.gtceu.cape.give.success.multiple", "Unlocked %s capes for %s players");
        provider.add("command.gtceu.cape.give.success.single", "Unlocked %s capes for %s");
        provider.add("command.gtceu.cape.take.failed", "No capes could be removed");
        provider.add("command.gtceu.cape.take.success.multiple", "Took %s capes from %s players");
        provider.add("command.gtceu.cape.take.success.single", "Took %s capes from %s");
        provider.add("command.gtceu.cape.use.failed",
                "%s can't use cape %s because they don't have it (or it doesn't exist)!");
        provider.add("command.gtceu.cape.use.success", "%s is now using cape %s");
        provider.add("command.gtceu.cape.use.success.none", "%s is no longer using a cape");

        provider.add("gtceu.medical_condition.description", "§l§cHAZARDOUS §7Hold Shift to show details");
        provider.add("gtceu.medical_condition.description_shift", "§l§cHAZARDOUS:");
        provider.add("gtceu.medical_condition.chemical_burns", "§5Chemical burns");
        provider.add("gtceu.medical_condition.poison", "§2Poisonous");
        provider.add("gtceu.medical_condition.weak_poison", "§aWeakly poisonous");
        provider.add("gtceu.medical_condition.irritant", "§6Irritant");
        provider.add("gtceu.medical_condition.nausea", "§3Nauseating");
        provider.add("gtceu.medical_condition.carcinogen", "§eCarcinogenic");
        provider.add("gtceu.medical_condition.asbestosis", "§dAsbestosis");
        provider.add("gtceu.medical_condition.arsenicosis", "§bArsenicosis");
        provider.add("gtceu.medical_condition.silicosis", "§1Silicosis");
        provider.add("gtceu.medical_condition.berylliosis", "§5Berylliosis");
        provider.add("gtceu.medical_condition.methanol_poisoning", "§6Methanol Poisoning");
        provider.add("gtceu.medical_condition.carbon_monoxide_poisoning", "§7Carbon Monoxide Poisoning");
        provider.add("gtceu.medical_condition.none", "§2Not Dangerous");
        provider.add("gtceu.hazard_trigger.description", "Caused by:");
        provider.add("gtceu.hazard_trigger.protection.description", "Protects from:");
        provider.add("gtceu.hazard_trigger.inhalation", "Inhalation");
        provider.add("gtceu.hazard_trigger.any", "Any contact");

        provider.add("gtceu.hazard_trigger.skin_contact", "Skin contact");
        provider.add("gtceu.hazard_trigger.none", "Nothing");
        provider.add("gtceu.medical_condition.antidote.description", "§aAntidote §7Hold Shift to show details");
        provider.add("gtceu.medical_condition.antidote.description_shift", "§aCures these conditions:");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed",
                "Removes %s%% of current conditions' effects");
        provider.add("gtceu.medical_condition.antidote.description.effect_removed.all",
                "Removes all of current conditions' effects");

        provider.add("gtceu.multiblock.dimension", "§eDimensions: §r%sx%sx%s");

        provider.add("item.gtceu.tool.replace_tool_head", "Craft with a new Tool Head to replace it");
        provider.add("item.gtceu.tool.usable_as", "§8Usable as: §f%s");
        provider.add("item.gtceu.tool.behavior.silk_ice", "§bIce Cutter: §fSilk Harvests Ice");
        provider.add("item.gtceu.tool.behavior.torch_place", "§eSpelunker: §fPlaces Torches on Right-Click");
        provider.add("item.gtceu.tool.behavior.tree_felling", "§4Lumberjack: §fTree Felling");
        provider.add("item.gtceu.tool.behavior.strip_log", "§5Artisan: §fStrips Logs");
        provider.add("item.gtceu.tool.behavior.scrape", "§bPolisher: §fRemoves Oxidation");
        provider.add("item.gtceu.tool.behavior.remove_wax", "§6Cleaner: §fRemoves Wax");
        provider.add("item.gtceu.tool.behavior.shield_disable", "§cBrute: §fDisables Shields");
        provider.add("item.gtceu.tool.behavior.relocate_mining", "§2Magnetic: §fRelocates Mined Blocks and Mob Drops");
        provider.add("item.gtceu.tool.behavior.aoe_mining", "§5Area-of-Effect: §f%sx%sx%s");
        provider.add("item.gtceu.tool.behavior.ground_tilling", "§eFarmer: §fTills Ground");
        provider.add("item.gtceu.tool.behavior.grass_path", "§eLandscaper: §fCreates Grass Paths");
        provider.add("item.gtceu.tool.behavior.rail_rotation", "§eRailroad Engineer: §fRotates Rails");
        provider.add("item.gtceu.tool.behavior.crop_harvesting", "§aHarvester: §fHarvests Crops");
        provider.add("item.gtceu.tool.behavior.plunger", "§9Plumber: §fDrains Fluids");
        provider.add("item.gtceu.tool.behavior.block_rotation", "§2Mechanic: §fRotates Blocks");
        provider.add("item.gtceu.tool.behavior.dowse_campfire", "§Firefighter: §fDowses Campfires");
        provider.add("item.gtceu.tool.behavior.damage_boost", "§4Damage Boost: §fExtra damage against %s");
        provider.add("item.gtceu.tool.behavior.prospecting.ore", "Found ore: %s");
        provider.add("item.gtceu.tool.behavior.prospecting.air", "Found an air pocket");
        provider.add("item.gtceu.tool.behavior.prospecting.water", "Found water");
        provider.add("item.gtceu.tool.behavior.prospecting.lava", "Found lava");
        provider.add("item.gtceu.tool.behavior.prospecting.changing", "Detected material change");
        replace(provider, "item.gtceu.tool.sword", "%s Sword");
        replace(provider, "item.gtceu.tool.pickaxe", "%s Pickaxe");
        replace(provider, "item.gtceu.tool.shovel", "%s Shovel");
        replace(provider, "item.gtceu.tool.axe", "%s Axe");
        replace(provider, "item.gtceu.tool.hoe", "%s Hoe");
        replace(provider, "item.gtceu.tool.saw", "%s Saw");
        replace(provider, "item.gtceu.tool.hammer", "%s Hammer");
        provider.add("item.gtceu.tool.hammer.tooltip", "§8Crushes Blocks when harvesting them");
        replace(provider, "item.gtceu.tool.mallet", "%s Soft Mallet");
        multilineLang(provider, "item.gtceu.tool.mallet.tooltip",
                "§8Sneak to Pause Machine After Current Recipe.\n§8Stops/Starts Machines");
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
        replace(provider, "item.gtceu.tool.lv_wirecutter", "%s Wire Cutter (LV)");
        replace(provider, "item.gtceu.tool.hv_wirecutter", "%s Wire Cutter (HV)");
        replace(provider, "item.gtceu.tool.iv_wirecutter", "%s Wire Cutter (IV)");
        replace(provider, "item.gtceu.tool.mining_hammer", "%s Mining Hammer");
        provider.add("item.gtceu.tool.mining_hammer.tooltip",
                "§8Mines a large area at once (unless you're crouching)");
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
        provider.add("item.gtceu.tool.tooltip.max_uses", "%s §eTotal Durability");
        provider.add("item.gtceu.tool.tooltip.general_uses", "%s §bDurability");
        provider.add("item.gtceu.tool.tooltip.attack_damage", "%s §cAttack Damage");
        provider.add("item.gtceu.tool.tooltip.attack_speed", "%s §9Attack Speed");
        provider.add("item.gtceu.tool.tooltip.mining_speed", "%s §dMining Speed");
        provider.add("item.gtceu.tool.tooltip.harvest_level", "§eHarvest Level %s");
        provider.add("item.gtceu.tool.tooltip.harvest_level_extra", "§eHarvest Level %s §f(%s§f)");
        multiLang(provider, "item.gtceu.tool.harvest_level", "§8Wood", "§7Stone", "§aIron", "§bDiamond",
                "§dNetherite",
                "§9Duranium", "§cNeutronium");
        provider.add("item.gtceu.tool.tooltip.repair_info", "§8Hold SHIFT to show Repair Info");
        provider.add("item.gtceu.tool.tooltip.repair_material", "§8Repair with: §f§a%s");
        provider.add("item.gtceu.tool.tooltip.default_enchantments", "§5Default Enchantments:");
        provider.add("item.gtceu.tool.aoe.rows", "Rows");
        provider.add("item.gtceu.tool.aoe.columns", "Columns");
        provider.add("item.gtceu.tool.aoe.layers", "Layers");

        provider.add("item.gtceu.armor.helmet", "%s Helmet");
        provider.add("item.gtceu.armor.chestplate", "%s Chestplate");
        provider.add("item.gtceu.armor.leggings", "%s Leggings");
        provider.add("item.gtceu.armor.boots", "%s Boots");

        provider.add("item.gtceu.turbine_rotor.tooltip", "Turbine Rotors for your power station");
        provider.add("metaitem.clipboard.tooltip",
                "Can be written on (without any writing Instrument). Right-click on Wall to place, and Shift-Right-Click to remove");
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
        provider.add("metaarmor.nms.boosted_jump.enabled", "NanoMuscle™ Suite: Jump Boost Enabled");
        provider.add("metaarmor.nms.boosted_jump.disabled", "NanoMuscle™ Suite: Jump Boost Disabled");
        provider.add("metaarmor.nms.nightvision.error", "NanoMuscle™ Suite: §cNot enough power!");
        provider.add("metaarmor.qts.nightvision.enabled", "QuarkTech™ Suite: NightVision Enabled");
        provider.add("metaarmor.qts.nightvision.disabled", "QuarkTech™ Suite: NightVision Disabled");
        provider.add("metaarmor.qts.nightvision.error", "QuarkTech™ Suite: §cNot enough power!");
        provider.add("metaarmor.jetpack.flight.enable", "Jetpack: Flight Enabled");
        provider.add("metaarmor.jetpack.flight.disable", "Jetpack: Flight Disabled");
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
        provider.add("metaarmor.tooltip.freezing", "Prevents Freezing");
        provider.add("metaarmor.tooltip.breath", "Replenishes Underwater Breath Bar");
        provider.add("metaarmor.tooltip.autoeat", "Replenishes Food Bar by Using Food from Inventory");
        provider.add("metaarmor.hud.status.enabled", "§aON");
        provider.add("metaarmor.hud.status.disabled", "§cOFF");
        provider.add("metaarmor.hud.energy_lvl", "Energy Level: %s");
        provider.add("metaarmor.hud.engine_enabled", "Engine Enabled: %s");
        provider.add("metaarmor.hud.fuel_lvl", "Fuel Level: %s");
        provider.add("metaarmor.hud.hover_mode", "Hover Mode: %s");
        provider.add("mataarmor.hud.supply_mode", "Supply Mode: %s");
        provider.add("metaarmor.hud.gravi_engine", "GraviEngine: %s");
        provider.add("metaarmor.energy_share.error", "Energy Supply: §cNot enough power for gadgets charging!");
        provider.add("metaarmor.energy_share.enable", "Energy Supply: Gadgets charging enabled");
        provider.add("metaarmor.energy_share.disable", "Energy Supply: Gadgets charging disabled");
        provider.add("metaarmor.energy_share.tooltip", "Supply mode: %s");
        provider.add("metaarmor.energy_share.tooltip.guide",
                "To change mode shift-right click when holding item");
        provider.add("metaitem.record.sus.tooltip", "§7Leonz - Among Us Drip");
        provider.add("item.gtceu.nan_certificate.tooltip", "Challenge Accepted!");
        provider.add("item.gtceu.blacklight.tooltip", "Long-Wave §dUltraviolet§7 light source");
        provider.add("gui.widget.incrementButton.default_tooltip",
                "Hold Shift, Ctrl or both to change the amount");
        provider.add("gui.widget.recipeProgressWidget.default_tooltip", "Show Recipes");
        multilineLang(provider, "gtceu.recipe_memory_widget.tooltip",
                "§7Left click to automatically input this recipe into the crafting grid\n§7Shift click to lock/unlock this recipe");
        provider.add("cover.filter.blacklist.disabled", "Whitelist");
        provider.add("cover.filter.blacklist.enabled", "Blacklist");
        provider.add("cover.tag_filter.title", "Tag Filter");
        multilineLang(provider, "cover.tag_filter.info",
                """
                        §bAccepts complex expressions
                        §6a & b§r = AND
                        §6a | b§r = OR
                        §6a ^ b§r = XOR
                        §6!a§r = NOT
                        §6(a)§r for grouping
                        §6*§r for wildcard
                        §6$§r for untagged
                        §bTags come in the form 'namespace:tag/subtype'.
                        The 'forge:' namespace is assumed if one isn't provided.
                        §bExample: §6*dusts/gold | (gtceu:circuits & !*lv)
                        This matches all gold dusts or all circuits except LV ones""");
        provider.add("cover.tag_filter.test_slot.info",
                "Insert a item to test if it matches the filter expression");
        provider.add("cover.tag_filter.matches", "Item matches");
        provider.add("cover.tag_filter.matches_not", "Item does not match");
        provider.add("cover.fluid_filter.title", "Fluid Filter");
        multilineLang(provider, "cover.fluid_filter.config_amount",
                "Scroll wheel up increases amount, down decreases.\nShift[§6x10§r],Ctrl[§ex100§r],Shift+Ctrl[§ax1000§r]\nRight click increases amount, left click decreases.\nHold shift to double/halve.\nMiddle click to clear");
        provider.add("cover.fluid_filter.mode.filter_fill", "Filter Fill");
        provider.add("cover.fluid_filter.mode.filter_drain", "Filter Drain");
        provider.add("cover.fluid_filter.mode.filter_both", "Filter Fill & Drain");
        provider.add("cover.item_filter.title", "Item Filter");
        provider.add("cover.storage.title", "Storage Cover");
        provider.add("cover.filter.mode.filter_insert", "Filter Insert");
        provider.add("cover.filter.mode.filter_extract", "Filter Extract");
        provider.add("cover.filter.mode.filter_both", "Filter Insert/Extract");
        provider.add("cover.item_filter.ignore_damage.enabled", "Ignore Damage");
        provider.add("cover.item_filter.ignore_damage.disabled", "Respect Damage");
        provider.add("cover.item_filter.ignore_nbt.enabled", "Ignore NBT");
        provider.add("cover.item_filter.ignore_nbt.disabled", "Respect NBT");
        provider.add("cover.voiding.voiding_mode.void_any", "Void Matching");
        provider.add("cover.voiding.voiding_mode.void_overflow", "Void Overflow");
        multilineLang(provider, "cover.voiding.voiding_mode.description",
                "§eVoid Matching§r will void anything matching the filter. \n§eVoid Overflow§r will void anything matching the filter, up to the specified amount.");
        provider.add("cover.fluid.voiding.title", "Fluid Voiding Settings");
        provider.add("cover.fluid.voiding.advanced.title", "Advanced Fluid Voiding Settings");
        provider.add("cover.item.voiding.title", "Item Voiding Settings");
        provider.add("cover.item.voiding.advanced.title", "Advanced Item Voiding Settings");
        provider.add("cover.voiding.label.disabled", "Disabled");
        provider.add("cover.voiding.label.enabled", "Enabled");
        provider.add("cover.voiding.tooltip",
                "§cWARNING!§7 Setting this to \"Enabled\" means that fluids or items WILL be voided.");
        provider.add("cover.voiding.message.disabled", "Voiding Cover Disabled");
        provider.add("cover.voiding.message.enabled", "Voiding Cover Enabled");
        provider.add("cover.item_smart_filter.title", "Smart Item Filter");
        provider.add("cover.item_smart_filter.filtering_mode.electrolyzer", "Electrolyzer");
        provider.add("cover.item_smart_filter.filtering_mode.centrifuge", "Centrifuge");
        provider.add("cover.item_smart_filter.filtering_mode.sifter", "Sifter");
        multilineLang(provider, "cover.item_smart_filter.filtering_mode.description",
                "Select Machine this Smart Filter will use for filtering.\nIt will automatically pick right portions of items for robotic arm.");
        provider.add("cover.conveyor.title", "Conveyor Cover Settings (%s)");
        provider.add("cover.conveyor.transfer_rate", "§7items/sec");
        provider.add("cover.conveyor.mode", "Mode: %s");
        provider.add("cover.conveyor.mode.export", "Mode: Export");
        provider.add("cover.conveyor.mode.import", "Mode: Import");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_global",
                "Distribution Mode: §bRound Robin\n§7Splits items equally across connected inventories");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_prio",
                "Distribution Mode: §bRound Robin with Restriction\n§7Tries to split items equally across connected inventories.\n§7Will not send items down Restrictive item pipes unless no other paths are available.");
        multilineLang(provider, "cover.conveyor.distribution.insert_first",
                "Distribution Mode: §bPriority\n§7Will insert into the first inventory with the highest priority it can find.\n§7Restrictive item pipes lower the priority of a path.");
        multilineLang(provider, "cover.conveyor.blocks_input.enabled",
                "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§aEnabled");
        multilineLang(provider, "cover.conveyor.blocks_input.disabled",
                "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§cDisabled");
        provider.add("cover.universal.manual_import_export.mode.disabled",
                "Manual I/O: §bDisabled\n§7Items / Fluids will only move as specified by the cover and its filter.");
        provider.add("cover.universal.manual_import_export.mode.filtered",
                "Manual I/O: §bFiltered\n§7Items / Fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any)");
        provider.add("cover.universal.manual_import_export.mode.unfiltered",
                "Manual I/O: §bUnfiltered\n§7Items / Fluids can be moved independently of the cover mode. The filter only applies to what is inserted or extracted by this cover itself.");
        multilineLang(provider, "cover.universal.manual_import_export.mode.description",
                "§eDisabled§r - Items/fluids will only move as specified by the cover and its filter. \n§eAllow Filtered§r - Items/fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any). \n§eAllow Unfiltered§r - Items/fluids can be moved independently of the cover mode. Filter applies to the items inserted or extracted by this cover");
        provider.add("cover.conveyor.item_filter.title", "Item Filter");
        multiLang(provider, "cover.conveyor.tag.title", "Tag Name",
                "(use * for wildcard)");
        provider.add("cover.robotic_arm.title", "Robotic Arm Settings (%s)");
        provider.add("cover.robotic_arm.transfer_mode.transfer_any", "Transfer Any");
        provider.add("cover.robotic_arm.transfer_mode.transfer_exact", "Supply Exact");
        provider.add("cover.robotic_arm.transfer_mode.keep_exact", "Keep Exact");
        multilineLang(provider, "cover.robotic_arm.transfer_mode.description",
                "§eTransfer Any§r - in this mode, cover will transfer as many items matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply items in portions specified in item filter slots (or variable under this button for tag filter). If amount of items is less than portion size, items won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of items in the destination inventory, supplying additional amount of items if required.\n§7Tip: left/right click on filter slots to change item amount,  use shift clicking to change amount faster.");
        provider.add("cover.pump.title", "Pump Cover Settings (%s)");
        provider.add("cover.pump.transfer_rate", "%s");
        provider.add("cover.pump.mode.export", "Mode: Export");
        provider.add("cover.pump.mode.import", "Mode: Import");
        provider.add("cover.pump.fluid_filter.title", "Fluid Filter");
        provider.add("cover.bucket.mode.bucket", "B");
        provider.add("cover.bucket.mode.milli_bucket", "mB");
        provider.add("cover.fluid_regulator.title", "Fluid Regulator Settings (%s)");
        multilineLang(provider, "cover.fluid_regulator.transfer_mode.description",
                "§eTransfer Any§r - in this mode, cover will transfer as many fluids matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply fluids in portions specified in the window underneath this button. If amount of fluids is less than portion size, fluids won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of fluids in the destination inventory, supplying additional amount of fluids if required.\n§7Tip: shift click will multiply increase/decrease amounts by 10 and ctrl click will multiply by 100.");
        provider.add("cover.fluid_regulator.supply_exact", "Supply Exact: %s");
        provider.add("cover.fluid_regulator.keep_exact", "Keep Exact: %s");
        provider.add("cover.machine_controller.title", "Machine Controller Settings");
        provider.add("cover.machine_controller.normal", "Normal");
        provider.add("cover.machine_controller.inverted", "Inverted");
        multilineLang(provider, "cover.machine_controller.invert.enabled",
                "§eInverted§r - in this mode, the cover will require a signal stronger than the set redstone level to run");
        multilineLang(provider, "cover.machine_controller.invert.disabled",
                "§eNormal§r - in this mode, the cover will require a signal weaker than the set redstone level to run");
        provider.add("cover.machine_controller.redstone", "Min Redstone Strength: %d");
        provider.add("cover.machine_controller.suspend_powerfail", "Prevent Power Failing:");
        provider.add("cover.machine_controller.mode.machine", "Control Machine");
        provider.add("cover.machine_controller.mode.cover_up", "Control Cover (Top)");
        provider.add("cover.machine_controller.mode.cover_down", "Control Cover (Bottom)");
        provider.add("cover.machine_controller.mode.cover_south", "Control Cover (South)");
        provider.add("cover.machine_controller.mode.cover_north", "Control Cover (North)");
        provider.add("cover.machine_controller.mode.cover_east", "Control Cover (East)");
        provider.add("cover.machine_controller.mode.cover_west", "Control Cover (West)");
        provider.add("cover.machine_controller.mode.null", "Control Nothing");
        provider.add("cover.ender_fluid_link.title", "Ender Fluid Link");
        provider.add("cover.ender_item_link.title", "Ender Item Link");
        provider.add("cover.ender_redstone_link.title", "Ender Redstone Link");
        provider.add("cover.ender_fluid_link.iomode.enabled", "I/O Enabled");
        provider.add("cover.ender_fluid_link.iomode.disabled", "I/O Disabled");
        provider.add("cover.ender_fluid_link.tooltip.channel_description", "Set channel description with input text");
        provider.add("cover.ender_fluid_link.tooltip.channel_name", "Set channel name with input text");
        provider.add("cover.ender_fluid_link.tooltip.list_button", "Show channel list");
        provider.add("cover.ender_fluid_link.tooltip.clear_button", "Clear channel description");
        multilineLang(provider, "cover.ender_fluid_link.private.tooltip.disabled",
                "Switch to private tank mode\nPrivate mode uses the player who originally placed the cover");
        provider.add("cover.ender_fluid_link.private.tooltip.enabled", "Switch to public tank mode");
        multilineLang(provider, "cover.ender_fluid_link.incomplete_hex",
                "Inputted color is incomplete!\nIt will be applied once complete (all 8 hex digits)\nClosing the gui will lose edits!");
        provider.add("cover.detector_base.message_normal_state", "Monitoring Status: Normal");
        provider.add("cover.detector_base.message_inverted_state", "Monitoring Status: Inverted");

        var detectorLatchDescription = """
                Change the redstone behavior of this Cover.
                §eContinuous§7 - Default; values less than the minimum output 0; values higher than the maximum output 15; values between min and max output between 0 and 15
                §eLatched§7 - output 15 until above max, then output 0 until below min""";
        multilineLang(provider, "cover.advanced_detector.latch.enabled",
                "Behavior: Latched\n\n" + detectorLatchDescription);
        multilineLang(provider, "cover.advanced_detector.latch.disabled",
                "Behavior: Continuous\n\n" + detectorLatchDescription);

        provider.add("cover.advanced_energy_detector.label", "Advanced Energy Detector");
        provider.add("cover.advanced_energy_detector.min", "Min");
        provider.add("cover.advanced_energy_detector.max", "Max");

        var advancedEnergyDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when less than the minimum EU, and stops emitting when greater than the max EU";
        multilineLang(provider, "cover.advanced_energy_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedEnergyDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_energy_detector.invert.disabled",
                "Output: Normal\n\n" + advancedEnergyDetectorInvertDescription);
        var advancedEnergyDetectorModeDescription = "Change between using discrete EU values or percentages for comparing min/max against an attached energy storage.";
        multilineLang(provider, "cover.advanced_energy_detector.use_percent.enabled",
                "Mode: Percentage\n\n" + advancedEnergyDetectorModeDescription);
        multilineLang(provider, "cover.advanced_energy_detector.use_percent.disabled",
                "Mode: Discrete EU\n\n" + advancedEnergyDetectorModeDescription);

        provider.add("cover.advanced_fluid_detector.label", "Advanced Fluid Detector");
        var advancedFluidDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum mB of fluid, and starts emitting when greater than the min mB of fluid up to the set maximum";
        multilineLang(provider, "cover.advanced_fluid_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedFluidDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_fluid_detector.invert.disabled",
                "Output: Normal\n\n" + advancedFluidDetectorInvertDescription);
        provider.add("cover.advanced_fluid_detector.max", "Max Fluid (mB)");
        provider.add("cover.advanced_fluid_detector.min", "Min Fluid (mB)");

        provider.add("cover.advanced_item_detector.label", "Advanced Item Detector");
        var advancedItemDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum amount of items, and starts emitting when greater than the min amount of items up to the set maximum";
        multilineLang(provider, "cover.advanced_item_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedItemDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_item_detector.invert.disabled",
                "Output: Normal\n\n" + advancedItemDetectorInvertDescription);
        provider.add("cover.advanced_item_detector.max", "Max Items");
        provider.add("cover.advanced_item_detector.min", "Min Items");
        provider.add("cover.shutter.message.enabled", "Closed shutter");
        provider.add("cover.shutter.message.disabled", "Opened shutter");

        replace(provider, "item.gtceu.bucket", "%s Bucket");

        replace(provider, "block.gtceu.oil_heavy", "Heavy Oil");
        replace(provider, "block.gtceu.oil_light", "Light Oil");
        replace(provider, "block.gtceu.oil_medium", "Raw Oil");
        replace(provider, "block.gtceu.oil", "Oil");
        replace(provider, "block.gtceu.creosote", "Creosote");

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
        provider.add("behaviour.soft_hammer.disabled_cycle", "Working Disabled after current cycle");
        provider.add("behaviour.lighter.tooltip.description", "Can light things on fire");
        provider.add("behaviour.lighter.tooltip.usage", "Shift-right click to open/close");
        provider.add("behaviour.lighter.fluid.tooltip", "Can light things on fire with Butane or Propane");
        provider.add("behaviour.lighter.uses", "Remaining uses: %d");
        provider.add("behavior.toggle_energy_consumer.tooltip", "Use to toggle mode");
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
        provider.add("behaviour.meta.machine.config.copy.tooltip", "§7Sneak + R-Click to copy machine configuration");
        provider.add("behaviour.meta.machine.config.paste.tooltip", "§7R-Click to paste machine configuration");
        provider.add("behaviour.setting.allow.input.from.output.tooltip", "%s input from output side is %s");
        provider.add("behaviour.setting.output.direction.tooltip", "%s output direction: %s");
        provider.add("behaviour.setting.item_auto_output.tooltip", "%s auto-output is %s");
        provider.add("behaviour.setting.muffled.tooltip", "Muffling %s");
        provider.add("item.toggle.advanced.info.tooltip", "§8<Sneak to view stored configuration>");
        provider.add("enchantment.damage.disjunction", "Disjunction");
        provider.add("enchantment.gtceu.disjunction.description",
                "Applies Weakness and Slowness to Ender-related mobs.");
        provider.add("enchantment.hard_hammer", "Hammering");
        provider.add("enchantment.gtceu.hard_hammer.description",
                "Breaks blocks as if they were mined with a GregTech Hammer.");
        provider.add("tile.gtceu.seal.name", "Sealed Block");
        provider.add("tile.gtceu.foam.name", "Foam");
        provider.add("tile.gtceu.reinforced_foam.name", "Reinforced Foam");
        provider.add("tile.gtceu.petrified_foam.name", "Petrified Foam");
        provider.add("tile.gtceu.reinforced_stone.name", "Reinforced Stone");
        provider.add("tile.gtceu.brittle_charcoal.name", "Brittle Charcoal");
        multilineLang(provider, "tile.gtceu.brittle_charcoal.tooltip",
                "Produced by the Charcoal Pile Igniter.\nMine this to get Charcoal.");
        provider.add("metaitem.prospector.mode.ores", "§aOre Prospection Mode§r");
        provider.add("metaitem.prospector.mode.fluid", "§bFluid Prospection Mode§r");
        provider.add("metaitem.prospector.mode.bedrock_ore", "§bBedrock Ore Prospection Mode§r");
        provider.add("metaitem.prospector.tooltip.radius", "Scans range in a %s Chunk Radius");
        provider.add("metaitem.prospector.tooltip.modes", "Available Modes:");
        provider.add("behavior.prospector.not_enough_energy", "Not Enough Energy!");
        provider.add("behavior.prospector.added_waypoint", "Created waypoint named %s!");
        provider.add("metaitem.tricorder_scanner.tooltip", "Tricorder");
        provider.add("metaitem.debug_scanner.tooltip", "Tricorder");
        provider.add("behavior.portable_scanner.bedrock_fluid.amount", "Fluid In Deposit: %s %s - %s%%");
        provider.add("behavior.portable_scanner.bedrock_fluid.amount_unknown", "Fluid In Deposit: %s%%");
        provider.add("behavior.portable_scanner.bedrock_fluid.nothing", "Fluid In Deposit: §6Nothing§r");
        provider.add("behavior.portable_scanner.environmental_hazard", "Environmental Hazard In Chunk: %s§r - %s ppm");
        provider.add("behavior.portable_scanner.environmental_hazard.nothing",
                "Environmental Hazard In Chunk: §6Nothing§r");
        provider.add("behavior.portable_scanner.local_hazard", "Local Hazard In Area: %s§r - %s ppm");
        provider.add("behavior.portable_scanner.local_hazard.nothing", "Local Hazard In Area: §6Nothing§r");
        provider.add("behavior.portable_scanner.block_hardness", "Hardness: %s Blast Resistance: %s");
        provider.add("behavior.portable_scanner.block_name", "Name: %s MetaData: %s");
        provider.add("behavior.portable_scanner.debug_cpu_load",
                "Average CPU load of ~%sns over %s ticks with worst time of %sns.");
        provider.add("behavior.portable_scanner.debug_cpu_load_seconds", "This is %s seconds.");
        provider.add("behavior.portable_scanner.debug_lag_count",
                "Caused %s Lag Spike Warnings (anything taking longer than %sms) on the Server.");
        provider.add("behavior.portable_scanner.debug_machine", "Meta-ID: %s");
        provider.add("behavior.portable_scanner.debug_machine_invalid", " invalid!");
        provider.add("behavior.portable_scanner.debug_machine_invalid_null=invalid! MetaTileEntity =",
                " null!");
        provider.add("behavior.portable_scanner.debug_machine_valid", " valid");
        provider.add("behavior.portable_scanner.divider", "=========================");
        provider.add("behavior.portable_scanner.energy_container_in", "Max IN: %s (%s) EU at %s A");
        provider.add("behavior.portable_scanner.energy_container_out", "Max OUT: %s (%s) EU at %s A");
        provider.add("behavior.portable_scanner.energy_container_storage", "Energy: %s EU / %s EU");
        provider.add("behavior.portable_scanner.eu_per_sec", "Average (last second): %s EU/t");
        provider.add("behavior.portable_scanner.amp_per_sec", "Average (last second): %s A");
        provider.add("behavior.portable_scanner.machine_disabled", "Disabled.");
        provider.add("behavior.portable_scanner.machine_front_facing", "Front Facing: %s");
        provider.add("behavior.portable_scanner.machine_ownership", "§2Machine Owner Type: %s§r");
        provider.add("behavior.portable_scanner.guild_name", "§2Guild Name: %s§r");
        provider.add("behavior.portable_scanner.team_name", "§2Team Name: %s§r");
        provider.add("behavior.portable_scanner.player_name", "§2Player Name: %s§r, §7Player Online: %s§r");
        provider.add("behavior.portable_scanner.machine_power_loss", "Shut down due to power loss.");
        provider.add("behavior.portable_scanner.machine_progress", "Progress/Load: %s / %s");
        provider.add("behavior.portable_scanner.machine_upwards_facing", "Upwards Facing: %s");
        provider.add("behavior.portable_scanner.muffled", "Muffled.");
        provider.add("behavior.portable_scanner.multiblock_energy_input",
                "Max Energy Income: %s EU/t Tier: %s");
        provider.add("behavior.portable_scanner.multiblock_energy_output",
                "Max Energy Output: %s EU/t Tier: %s");
        provider.add("behavior.portable_scanner.multiblock_maintenance", "Problems: %s");
        provider.add("behavior.portable_scanner.multiblock_parallel", "Multi Processing: %s");
        provider.add("behavior.portable_scanner.position", "----- X: %s Y: %s Z: %s D: %s -----");
        provider.add("behavior.portable_scanner.state", "%s: %s");
        provider.add("behavior.portable_scanner.tank", "Tank %s: %s mB / %s mB %s");
        provider.add("behavior.portable_scanner.tanks_empty", "All Tanks Empty");
        provider.add("behavior.portable_scanner.workable_consumption", "Probably Uses: %s EU/t at %s A");
        provider.add("behavior.portable_scanner.workable_production", "Probably Produces: %s EU/t at %s A");
        provider.add("behavior.portable_scanner.workable_progress", "Progress: %s s / %s s");
        provider.add("behavior.portable_scanner.workable_stored_energy", "Stored Energy: %s EU / %s EU");
        provider.add("behavior.portable_scanner.mode.caption", "Display mode: %s");
        provider.add("behavior.portable_scanner.mode.show_all_info", "Show all info");
        provider.add("behavior.portable_scanner.mode.show_block_info", "Show block info");
        provider.add("behavior.portable_scanner.mode.show_machine_info", "Show machine info");
        provider.add("behavior.portable_scanner.mode.show_electrical_info", "Show electrical info");
        provider.add("behavior.portable_scanner.mode.show_recipe_info", "Show recipe info");
        provider.add("behavior.portable_scanner.mode.show_environmental_info", "Show environmental info");
        provider.add("behavior.item_magnet.enabled", "§aMagnetic Field Enabled");
        provider.add("behavior.item_magnet.disabled", "§cMagnetic Field Disabled");
        provider.add("behavior.data_item.title", "§n%s Construction Data:");
        provider.add("behavior.data_item.data", "- §a%s");

        provider.add("metaitem.terminal.tooltip", "Sharp tools make good work");
        provider.add("metaitem.terminal.tooltip.creative", "§bCreative Mode");
        provider.add("metaitem.terminal.tooltip.hardware", "§aHardware: %d");
        provider.add("metaitem.plugin.tooltips.1",
                "Plugins can be added to the screen for more functionality.");
        provider.add("metaitem.plugin.proxy.tooltips.1", "(Please adjust to proxy mode in the screen)");
        provider.add("metaitem.cover.digital.tooltip",
                "Connects machines over §fPower Cables§7 to the §fCentral Monitor§7 as §fCover§7.");

        provider.add("gtceu.machine.drum.enable_output", "Will drain Fluid to downward adjacent Tanks");
        provider.add("gtceu.machine.drum.disable_output", "Will not drain Fluid");
        provider.add("gtceu.machine.locked_safe.malfunctioning", "§cMalfunctioning!");
        provider.add("gtceu.machine.locked_safe.requirements", "§7Replacements required:");

        multilineLang(provider, "gtceu.machine.workbench.tooltip",
                "Better than Forestry\nHas Item Storage, Tool Storage, pulls from adjacent Inventories, and saves Recipes.");
        provider.add("gtceu.machine.workbench.tab.workbench", "Crafting");
        provider.add("gtceu.machine.workbench.tab.item_list", "Storage");
        multilineLang(provider, "gtceu.machine.workbench.storage_note",
                "(Available items from connected\ninventories usable for crafting)");
        provider.add("gtceu.item_list.item_stored", "§7Stored: %d");
        provider.add("gtceu.machine.workbench.tab.crafting", "Crafting");
        provider.add("gtceu.machine.workbench.tab.container", "Container");

        provider.add("gtceu.machine.parallel_hatch.display", "Adjust the maximum parallel of the multiblock");
        provider.add("gtceu.machine.basic.input_from_output_side.allow", "Allow Input from Output Side: ");
        provider.add("gtceu.machine.basic.input_from_output_side.disallow",
                "Disallow Input from Output Side: ");
        provider.add("gtceu.machine.muffle.on", "Sound Muffling: Enabled");
        provider.add("gtceu.machine.muffle.off", "Sound Muffling: Disabled");
        provider.add("gtceu.machine.perfect_oc", "Does not lose energy efficiency when overclocked.");
        provider.add("gtceu.machine.parallel_limit", "Can run up to §b%d§r§7 Recipes at once.");

        provider.add("gtceu.machine.multiblock.tank.tooltip",
                "Fill and drain through the controller or tank valves.");
        provider.add("gtceu.machine.tank_valve.tooltip",
                "Use to fill and drain multiblock tanks. Auto outputs when facing down.");

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

        provider.add("gtceu.part_sharing.disabled", "Multiblock Sharing §4Disabled");
        provider.add("gtceu.part_sharing.enabled", "Multiblock Sharing §aEnabled");
        provider.add("gtceu.universal.liters", "%s mB");
        provider.add("gtceu.universal.kiloliters", "%s B");
        provider.add("gtceu.universal.parentheses", "(%s)");
        provider.add("gtceu.universal.spaced_parentheses", "( %s )");
        provider.add("gtceu.universal.padded_parentheses", " (%s) ");
        provider.add("gtceu.universal.padded_spaced_parentheses", " ( %s ) ");
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
        provider.add("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                "§9Fluid Capacity: §f%d §7Tanks, §f%d mB §7each");
        provider.add("gtceu.universal.tooltip.fluid_stored", "§2Fluid Stored: §f%s, %d mB");
        provider.add("gtceu.universal.tooltip.fluid_transfer_rate", "§bTransfer Rate: §f%d mB/t");
        provider.add("gtceu.universal.tooltip.parallel", "§dMax Parallel: §f%d");
        provider.add("gtceu.universal.tooltip.working_area", "§bWorking Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.chunk_mode", "Chunk Mode: ");
        provider.add("gtceu.universal.tooltip.silk_touch", "Silk Touch: ");
        provider.add("gtceu.universal.tooltip.working_area_chunks", "§bWorking Area: §f%dx%d Chunks");
        provider.add("gtceu.universal.tooltip.working_area_max", "§bMax Working Area: §f%dx%d");
        provider.add("gtceu.universal.tooltip.working_area_chunks_max", "§bMax Working Area: §f%dx%d Chunks");
        provider.add("gtceu.universal.tooltip.uses_per_tick", "Uses §f%d EU/t §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_tick_steam", "Uses §f%d mB/t §7of §fSteam §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_hour_lubricant",
                "Uses §f%d mB/hr §7of §6Lubricant §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_second", "Uses §f%d EU/s §7while working");
        provider.add("gtceu.universal.tooltip.uses_per_op", "Uses §f%d EU/operation");
        provider.add("gtceu.universal.tooltip.base_production_eut", "§eBase Production: §f%d EU/t");
        provider.add("gtceu.universal.tooltip.base_production_fluid", "§eBase Production: §f%d mB/t");
        provider.add("gtceu.universal.tooltip.produces_fluid", "§eProduces: §f%d mB/t");
        provider.add("gtceu.universal.tooltip.terrain_resist",
                "This Machine will not explode when exposed to the Elements");
        provider.add("gtceu.universal.tooltip.requires_redstone", "§4Requires Redstone power");
        provider.add("gtceu.universal.tooltip.deprecated",
                "§4§lWARNING:§r§4 DEPRECATED. WILL BE REMOVED IN A FUTURE VERSION.§r");
        provider.add("gtceu.recipe.total", "Total: %s EU");
        provider.add("gtceu.recipe.max_eu", "Max. EU: %s EU");
        provider.add("gtceu.recipe.eu", "Usage: %s A @ %s");
        provider.add("gtceu.recipe.eu_inverted", "Generation: %s A @ %s");
        provider.add("gtceu.recipe.eu.total", "%s EU/t");
        provider.add("gtceu.recipe.duration", "Duration: %s secs");
        provider.add("gtceu.recipe.voltage", "Usage: %s A @ %s");
        provider.add("gtceu.recipe.total_eu", "Total Usage: %s EU/t");
        provider.add("gtceu.recipe.not_consumed", "Does not get consumed in the process");
        provider.add("gtceu.recipe.chance", "Chance: %s +%s/tier");
        provider.add("gtceu.recipe.temperature", "Temp: %sK");
        provider.add("gtceu.recipe.coil.tier", "Coil: %s");
        provider.add("gtceu.recipe.explosive", "Explosive: %s");
        provider.add("gtceu.recipe.eu_to_start", "EU To Start: %sEU%s");
        provider.add("gtceu.recipe.dimensions", "Dimensions: %s");
        provider.add("gtceu.recipe.cleanroom", "Requires %s");
        provider.add("gtceu.recipe.environmental_hazard.reverse", "§cArea must be free of %s");
        provider.add("gtceu.recipe.environmental_hazard", "§cArea must have %s");
        provider.add("gtceu.recipe.cleanroom.display_name", "Cleanroom");
        provider.add("gtceu.recipe.cleanroom_sterile.display_name", "Sterile Cleanroom");
        provider.add("gtceu.recipe.research", "Requires Research");
        provider.add("gtceu.recipe.scan_for_research", "Scan for Assembly Line");
        provider.add("gtceu.recipe.computation_per_tick", "Min. Computation: %s CWU/t");
        provider.add("gtceu.recipe.total_computation", "Computation: %s CWU");
        provider.add("gtceu.recipe.byproduct_tier", "Byproducts from %s§r+");
        provider.add("gtceu.fluid.click_to_fill",
                "§7Click with a Fluid Container to §bfill §7the tank (Shift-click for a full stack).");
        provider.add("gtceu.fluid.click_combined",
                "§7Click with a Fluid Container to §cempty §7or §bfill §7the tank (Shift-click for a full stack).");
        provider.add("gtceu.fluid.click_to_empty",
                "§7Click with a Fluid Container to §cempty §7the tank (Shift-click for a full stack).");
        provider.add("gtceu.tool_action.show_tooltips", "Hold SHIFT to show Tool Info");
        provider.add("gtceu.tool_action.screwdriver.auto_output_covers",
                "§8Use Screwdriver to Allow Input from Output Side or access Covers");
        provider.add("gtceu.tool_action.screwdriver.toggle_mode_covers",
                "§8Use Screwdriver to toggle Modes or access Covers");
        provider.add("gtceu.tool_action.screwdriver.access_covers", "§8Use Screwdriver to access Covers");
        provider.add("gtceu.tool_action.screwdriver.auto_collapse",
                "§8Use Screwdriver to toggle Item collapsing");
        provider.add("gtceu.tool_action.screwdriver.auto_output", "§8Use Screwdriver to toggle Auto-Output");
        provider.add("gtceu.tool_action.screwdriver.toggle_mode", "§8Use Screwdriver to toggle Modes");
        provider.add("gtceu.tool_action.wrench.set_facing", "§8Use Wrench to set Facing");
        provider.add("gtceu.tool_action.wrench.connect",
                "§8Use Wrench to set Connections, sneak to block Connections");
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
        provider.add("gtceu.gui.title_bar.back", "Back");
        provider.add("gtceu.gui.title_bar.page_switcher", "Pages");
        provider.add("gtceu.gui.fuel_amount", "Fuel Amount:");
        provider.add("gtceu.gui.fluid_amount", "Fluid Amount:");
        provider.add("gtceu.gui.toggle_view.disabled", "Toggle View (Fluids)");
        provider.add("gtceu.gui.toggle_view.enabled", "Toggle View (Items)");
        multilineLang(provider, "gtceu.gui.overclock.enabled", "Overclocking Enabled.\nClick to Disable");
        multilineLang(provider, "gtceu.gui.overclock.disabled", "Overclocking Disabled.\nClick to Enable");
        multilineLang(provider, "gtceu.gui.overclock.description",
                "Overclock Button\n§7Recipes can overclock up to the set tier");
        provider.add("gtceu.gui.overclock.off", "X");
        provider.add("gtceu.gui.sort", "Sort");
        provider.add("gtceu.gui.fluid_auto_output.tooltip.enabled", "Fluid Auto-Output Enabled");
        provider.add("gtceu.gui.fluid_auto_output.tooltip.disabled", "Fluid Auto-Output Disabled");
        provider.add("gtceu.gui.fluid_auto_input.tooltip.enabled", "Fluid Auto-Input Enabled");
        provider.add("gtceu.gui.fluid_auto_input.tooltip.disabled", "Fluid Auto-Input Disabled");
        provider.add("gtceu.gui.item_auto_output.tooltip.enabled", "Item Auto-Output Enabled");
        provider.add("gtceu.gui.item_auto_output.tooltip.disabled", "Item Auto-Output Disabled");
        provider.add("gtceu.gui.item_auto_input.tooltip.enabled", "Item Auto-Input Enabled");
        provider.add("gtceu.gui.item_auto_input.tooltip.disabled", "Item Auto-Input Disabled");
        multilineLang(provider, "gtceu.gui.charger_slot.tooltip",
                "§fCharger Slot§r\n§7Draws power from %s batteries§r\n§7Charges %s tools and batteries");
        multilineLang(provider, "gtceu.gui.configurator_slot.tooltip",
                "§fConfigurator Slot§r\n§7Place a §6Programmed Circuit§7 in this slot to\n§7change its configured value.\n§7Hold §6Shift§7 when clicking buttons to change by §65.\n§aA Programmed Circuit in this slot is also valid for recipe inputs.§r");
        provider.add("gtceu.gui.fluid_lock.tooltip.enabled", "Fluid Locking Enabled");
        provider.add("gtceu.gui.fluid_lock.tooltip.disabled", "Fluid Locking Disabled");
        provider.add("gtceu.gui.fluid_voiding_partial.tooltip.enabled", "Fluid Voiding Enabled");
        provider.add("gtceu.gui.fluid_voiding_partial.tooltip.disabled", "Fluid Voiding Disabled");
        provider.add("gtceu.gui.item_lock.tooltip.enabled", "Item Locking Enabled");
        provider.add("gtceu.gui.item_lock.tooltip.disabled", "Item Locking Disabled");
        provider.add("gtceu.gui.item_voiding_partial.tooltip.enabled", "Item Voiding Enabled");
        provider.add("gtceu.gui.item_voiding_partial.tooltip.disabled", "Item Voiding Disabled");
        multilineLang(provider, "gtceu.gui.silktouch.enabled",
                "Silk Touch Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.silktouch.disabled",
                "Silk Touch Disabled: Click to Enable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.chunkmode.enabled",
                "Chunk Mode Enabled: Click to Disable.\n§7Switching requires an idle machine.");
        multilineLang(provider, "gtceu.gui.chunkmode.disabled",
                "Chunk Mode Disabled: Click to Enable.\n§7Switching requires an idle machine.");
        provider.add("gtceu.gui.multiblock.voiding_mode", "Voiding Mode:");
        provider.add("gtceu.gui.item_voiding", "§7Voiding §6Items");
        provider.add("gtceu.gui.fluid_voiding", "§7Voiding §9Fluids");
        provider.add("gtceu.gui.all_voiding",
                "§7Voiding §cAll");
        provider.add("gtceu.gui.no_voiding", "§7Voiding Nothing");
        multilineLang(provider, "gtceu.gui.fisher_mode.tooltip",
                "Toggle junk items\nOff costs 2 string per operation");
        provider.add("ore.spawnlocation.name", "Ore Spawn Information");
        multiLang(provider, "gtceu.jei.ore.surface_rock",
                "Surface Rocks with this material denote vein spawn locations.",
                "They can be broken for 3 Tiny Piles of the dust, with Fortune giving a bonus.");
        provider.add("gtceu.jei.ore.biome_weighting_title", "§dModified Biome Total Weights:");
        provider.add("gtceu.jei.ore.biome_weighting", "§d%s Weight: §3%d");
        provider.add("gtceu.jei.ore.biome_weighting_no_spawn", "§d%s Weight: §cCannot Spawn");
        provider.add("gtceu.jei.ore.ore_weight", "Weight in vein: %d%%");
        multiLang(provider, "gtceu.jei.ore.primary", "Top Ore", "Spawns in the top %d layers of the vein");
        multiLang(provider, "gtceu.jei.ore.secondary", "Bottom Ore",
                "Spawns in the bottom %d layers of the vein");
        multiLang(provider, "gtceu.jei.ore.between", "Between Ore",
                "Spawns in the middle %d layers of the vein, with other ores");
        multiLang(provider, "gtceu.jei.ore.sporadic", "Sporadic Ore", "Spawns anywhere in the vein");
        provider.add("fluid.spawnlocation.name", "Fluid Vein Information");
        provider.add("gtceu.jei.fluid.vein_weight", "Vein Weight: %d");
        provider.add("gtceu.jei.fluid.min_yield", "Minimum Yield: %d");
        provider.add("gtceu.jei.fluid.max_yield", "Maximum Yield: %d");
        provider.add("gtceu.jei.fluid.depletion_chance", "Depletion Chance: %d%%");
        provider.add("gtceu.jei.fluid.depletion_amount", "Depletion Amount: %d");
        provider.add("gtceu.jei.fluid.depleted_rate", "Depleted Yield: %d");
        provider.add("gtceu.jei.fluid.dimension", "Dimensions:");
        provider.add("gtceu.jei.fluid.weight_hover",
                "The Weight of the vein. Hover over the fluid to see any possible biome modifications");
        provider.add("gtceu.jei.fluid.min_hover",
                "The minimum yield that any fluid vein of this fluid can have");
        provider.add("gtceu.jei.fluid.max_hover",
                "The maximum yield that any fluid vein of this fluid can have");
        provider.add("gtceu.jei.fluid.dep_chance_hover",
                "The percentage chance for the vein to be depleted upon harvest");
        provider.add("gtceu.jei.fluid.dep_amount_hover", "The amount the vein will be depleted by");
        provider.add("gtceu.jei.fluid.dep_yield_hover",
                "The maximum yield of the vein when it is fully depleted");
        provider.add("gtceu.jei.materials.average_mass", "Average mass: %d");
        provider.add("gtceu.jei.materials.average_protons", "Average protons: %d");
        provider.add("gtceu.jei.materials.average_neutrons", "Average neutrons: %d");
        provider.add("gtceu.item_filter.empty_item", "Empty (No Item)");
        provider.add("gtceu.item_filter.footer", "§eClick with item to override");
        provider.add("gtceu.cable.voltage", "§aMax Voltage:§r §a%d §a(%s§a)");
        provider.add("gtceu.cable.amperage", "§eMax Amperage:§r §e%d");
        provider.add("gtceu.cable.loss_per_block", "§cLoss/Meter/Ampere:§r §c%d§7 EU-Volt");
        provider.add("gtceu.cable.superconductor", "%s §dSuperconductor");
        provider.add("gtceu.fluid_pipe.capacity", "§9Capacity: §f%d mB");
        provider.add("gtceu.fluid_pipe.max_temperature", "§cTemperature Limit: §f%d K");
        provider.add("gtceu.fluid_pipe.channels", "§eChannels: §f%d");
        provider.add("gtceu.fluid_pipe.gas_proof", "§6Can handle Gases");
        provider.add("gtceu.fluid_pipe.acid_proof", "§6Can handle Acids");
        provider.add("gtceu.fluid_pipe.cryo_proof", "§6Can handle Cryogenics");
        provider.add("gtceu.fluid_pipe.plasma_proof", "§6Can handle all Plasmas");
        provider.add("gtceu.fluid_pipe.not_gas_proof", "§4Gases may leak!");
        provider.add("gtceu.item_pipe.priority", "§9Priority: §f%d");
        provider.add("gtceu.duct_pipe.transfer_rate", "§bAir transfer rate: %s");
        provider.add("gtceu.multiblock.work_paused", "Work Paused.");
        provider.add("gtceu.multiblock.running", "Running perfectly.");
        provider.add("gtceu.multiblock.idling", "§6Idling.");
        provider.add("gtceu.multiblock.research_station.researching", "§6Researching.");
        provider.add("gtceu.multiblock.not_enough_energy", "WARNING: Machine needs more energy.");
        provider.add("gtceu.multiblock.not_enough_energy_output", "WARNING: Energy Dynamo Tier Too Low!");
        provider.add("gtceu.multiblock.waiting", "WARNING: Machine is waiting.");
        provider.add("gtceu.multiblock.total_runs", "Performing %d Recipes at once");
        provider.add("gtceu.multiblock.batch_enabled", "- %dx from Batching");
        provider.add("gtceu.multiblock.subtick_parallels", "- %dx from Overclocking");
        provider.add("gtceu.machine.batch_enabled", "Batching Enabled");
        provider.add("gtceu.machine.batch_disabled", "Batching Disabled");
        provider.add("gtceu.multiblock.progress_percent", "Progress: %s%%");
        provider.add("gtceu.multiblock.progress", "Progress: %ss / %ss (%s%%)");
        provider.add("gtceu.multiblock.output_line.0", "%s x §e%s§r (%ss/ea)");
        provider.add("gtceu.multiblock.output_line.1", "%s x §e%s§r (%s/s)");
        provider.add("gtceu.multiblock.output_line.2", "%s ≈ §e%s§r (%ss/ea)");
        provider.add("gtceu.multiblock.output_line.3", "%s ≈ §e%s§r (%s/s)");
        provider.add("gtceu.multiblock.invalid_structure", "Invalid structure.");
        provider.add("gtceu.multiblock.invalid_structure.tooltip",
                "This block is a controller of the multiblock structure. For building help, see structure template in JEI.");
        provider.add("gtceu.multiblock.validation_failed", "Invalid amount of inputs/outputs.");
        provider.add("gtceu.multiblock.max_recipe_tier", "Max Recipe Tier: %s");
        provider.add("gtceu.multiblock.max_recipe_tier_hover", "The maximum tier of recipes that can be run");
        provider.add("gtceu.multiblock.max_energy_per_tick", "Max EU/t: §a%s (%s§r)");
        provider.add("gtceu.multiblock.max_energy_per_tick_hover",
                "The maximum EU/t available for running recipes or overclocking");
        provider.add("gtceu.multiblock.max_energy_per_tick_amps", "Max EU/t: %s (%sA %s)");
        provider.add("gtceu.multiblock.energy_consumption", "Energy Usage: %s EU/t (%s)");
        provider.add("gtceu.multiblock.generation_eu", "Outputting: §a%s EU/t");
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
        provider.add("gtceu.multiblock.universal.muffler_obstructed", "Muffler Hatch is Obstructed!");
        provider.add("gtceu.multiblock.universal.muffler_obstructed.tooltip",
                "Muffler Hatch must have a block of airspace in front of it.");
        provider.add("gtceu.multiblock.universal.rotor_obstructed", "Rotor is Obstructed!");
        provider.add("gtceu.multiblock.universal.distinct", "Distinct Buses:");
        provider.add("gtceu.multiblock.universal.distinct.no", "No");
        provider.add("gtceu.multiblock.universal.distinct.yes", "Yes");
        provider.add("gtceu.multiblock.universal.distinct.info",
                "If enabled, each Item Input Bus will be treated as fully distinct from each other for recipe lookup. Useful for things like Programmed Circuits, Extruder Shapes, etc.");
        provider.add("gtceu.multiblock.parallel", "Performing up to %d Recipes in Parallel");
        provider.add("gtceu.multiblock.parallel.exact", "- %dx from Parallels");
        provider.add("gtceu.multiblock.multiple_recipemaps.header", "Machine Mode:");
        provider.add("gtceu.multiblock.multiple_recipemaps.tooltip",
                "Screwdriver the controller to change which machine mode to use.");
        provider.add("gtceu.multiblock.multiple_recipemaps_recipes.tooltip", "Machine Modes: §e%s§r");
        provider.add("gtceu.multiblock.multiple_recipemaps.switch_message",
                "The machine must be off to switch modes!");
        provider.add("gtceu.multiblock.preview.zoom", "Use mousewheel or right-click + drag to zoom");
        provider.add("gtceu.multiblock.preview.rotate", "Click and drag to rotate");
        provider.add("gtceu.multiblock.preview.select", "Right-click to check candidates");
        provider.add("gtceu.multiblock.pattern.error", "Expected components (%s) at (%s).");
        provider.add("gtceu.multiblock.pattern.error.limited_exact", "§cExactly: %d§r");
        provider.add("gtceu.multiblock.pattern.error.limited_within", "§cBetween %d and %d§r");
        multiLang(provider, "gtceu.multiblock.pattern.error.limited", "§cMaximum: %d§r", "§cMinimum: %d§r",
                "§cMaximum: %d per layer§r", "§cMinimum: %d per layer§r");
        provider.add("gtceu.multiblock.pattern.error.coils", "§cAll heating coils must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.filters", "§cAll filters must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.batteries", "§cAll batteries must be the same§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_1", "§6Must have a clear 1x1x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_3", "§6Must have a clear 3x3x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.single", "§6Only this block can be used§r");
        provider.add("gtceu.multiblock.pattern.location_end", "§cVery End§r");
        provider.add("gtceu.multiblock.pattern.replaceable_air", "Replaceable by Air");

        provider.add("gtceu.multiblock.computation.max", "Max CWU/t: %s");
        provider.add("gtceu.multiblock.computation.usage", "Using: %s");
        provider.add("gtceu.multiblock.computation.non_bridging", "Non-bridging connection found");
        provider.add("gtceu.multiblock.computation.non_bridging.detailed",
                "A Reception Hatch is linked to a machine which cannot bridge");
        provider.add("gtceu.multiblock.computation.not_enough_computation", "Machine needs more computation!");

        provider.add("gtceu.chat.cape",
                "§5Congrats: you just unlocked a new cape! See the Cape Selector terminal app to use it.§r");
        provider.add("gtceu.universal.clear_nbt_recipe.tooltip", "§cThis will destroy all contents!");
        provider.add("gtceu.cover.energy_detector.message_electricity_storage_normal",
                "Monitoring Normal Electricity Storage");
        provider.add("gtceu.cover.energy_detector.message_electricity_storage_inverted",
                "Monitoring Inverted Electricity Storage");
        provider.add("gtceu.cover.fluid_detector.message_fluid_storage_normal",
                "Monitoring Normal Fluid Storage");
        provider.add("gtceu.cover.fluid_detector.message_fluid_storage_inverted",
                "Monitoring Inverted Fluid Storage");
        provider.add("gtceu.cover.item_detector.message_item_storage_normal", "Monitoring Normal Item Storage");
        provider.add("gtceu.cover.item_detector.message_item_storage_inverted",
                "Monitoring Inverted Item Storage");
        provider.add("gtceu.cover.activity_detector.message_activity_normal",
                "Monitoring Normal Activity Status");
        provider.add("gtceu.cover.activity_detector.message_activity_inverted",
                "Monitoring Inverted Activity Status");
        provider.add("gtceu.cover.activity_detector_advanced.message_activity_normal",
                "Monitoring Normal Progress Status");
        provider.add("gtceu.cover.activity_detector_advanced.message_activity_inverted",
                "Monitoring Inverted Progress Status");

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
        provider.add("gtceu.key.enable_jetpack", "Enable Jetpack");
        provider.add("gtceu.key.enable_boots", "Enable Boosted Jump");
        provider.add("gtceu.key.armor_charging", "Armor Charging to Inventory Toggle");
        provider.add("gtceu.key.tool_aoe_change", "Tool AoE Mode Switch");
        provider.add("gtceu.debug.f3_h.enabled",
                "GregTech has modified the debug info! For Developers: enable the misc:debug config option in the GregTech config file to see more");
        provider.add("config.jade.plugin_gtceu.controllable_provider", "[GTCEu] Controllable");
        provider.add("config.jade.plugin_gtceu.workable_provider", "[GTCEu] Workable");
        provider.add("config.jade.plugin_gtceu.electric_container_provider", "[GTCEu] Electric Container");
        provider.add("config.jade.plugin_gtceu.recipe_logic_provider", "[GTCEu] Recipe Logic");
        provider.add("config.jade.plugin_gtceu.hazard_cleaner_provider", "[GTCEu] Hazard Cleaner");
        provider.add("config.jade.plugin_gtceu.recipe_output_info", "[GTCEu] Recipe Output Info");
        provider.add("config.jade.plugin_gtceu.auto_output_info", "[GTCEu] Auto Output Info");
        provider.add("config.jade.plugin_gtceu.cable_info", "[GTCEu] Cable Info");
        provider.add("config.jade.plugin_gtceu.exhaust_vent_info", "[GTCEu] Exhaust Vent Info");
        provider.add("config.jade.plugin_gtceu.steam_boiler_info", "[GTCEu] Steam Boiler Info");
        provider.add("config.jade.plugin_gtceu.machine_mode", "[GTCEu] Machine Mode");
        provider.add("config.jade.plugin_gtceu.maintenance_info", "[GTCEu] Maintenance Info");
        provider.add("config.jade.plugin_gtceu.multiblock_structure", "[GTCEu] MultiBlock Structure");
        provider.add("config.jade.plugin_gtceu.parallel_info", "[GTCEu] Parallel Info");
        provider.add("config.jade.plugin_gtceu.primitive_pump", "[GTCEu] Primitive Pump Info");
        provider.add("config.jade.plugin_gtceu.data_bank", "[GTCEu] Data Bank Info");
        provider.add("config.jade.plugin_gtceu.transformer", "[GTCEu] Transformer Info");
        provider.add("config.jade.plugin_gtceu.stained_color", "[GTCEu] Stained Block Info");
        provider.add("config.jade.plugin_gtceu.me_pattern_buffer", "[GTCEu] Pattern Buffer Info");
        provider.add("config.jade.plugin_gtceu.me_pattern_buffer_proxy", "[GTCEu] Pattern Buffer Proxy Info");
        provider.add("config.jade.plugin_gtceu.energy_converter_provider", "[GTCEu] Energy Converter Mode");

        // gui
        provider.add("gtceu.button.ore_veins", "Show GT Ore Veins");
        provider.add("gtceu.button.bedrock_fluids", "Show Bedrock Fluid Veins");
        provider.add("gtceu.button.hide_depleted", "Hide Depleted Veins");
        provider.add("gtceu.button.show_depleted", "Show Depleted Veins");
        provider.add("gtceu.recipe_type.show_recipes", "Show Recipes");
        provider.add("gtceu.recipe_logic.insufficient_fuel", "Insufficient Fuel");
        provider.add("gtceu.recipe_logic.insufficient_in", "Insufficient Inputs");
        provider.add("gtceu.recipe_logic.insufficient_out", "Insufficient Outputs");
        provider.add("gtceu.recipe_logic.condition_fails", "Condition Fails");
        provider.add("gtceu.recipe_logic.no_contents", "Recipe has no Contents");
        provider.add("gtceu.recipe_logic.no_capabilities", "Machine has no Capabilities");
        provider.add("gtceu.gui.cover_setting.title", "Cover Settings");
        provider.add("gtceu.gui.output_setting.title", "Output Settings");
        provider.add("gtceu.gui.circuit.title", "Circuit Settings");
        multiLang(provider, "gtceu.gui.output_setting.tooltips", "left-click to tune the item auto output",
                "right-click to tune the fluid auto output.");
        provider.add("gtceu.gui.item_auto_output.allow_input.enabled",
                "allow items input from the output side");
        provider.add("gtceu.gui.item_auto_output.allow_input.disabled",
                "disable items input from the output side");
        provider.add("gtceu.gui.item_auto_output.enabled", "Item Auto Output: §aEnabled");
        provider.add("gtceu.gui.item_auto_output.disabled", "Item Auto Output: §cDisabled");
        multilineLang(provider, "gtceu.gui.item_auto_output.unselected",
                """
                        Item Auto Output
                        §7Select a side of the machine to configure its output.
                        """);
        multilineLang(provider, "gtceu.gui.item_auto_output.other_direction",
                """
                        Item Auto Output: §6Other Direction
                        §7The machine's item output is set to another direction.
                        §7Click to move the output to the currently selected side.
                        """);
        provider.add("gtceu.gui.fluid_auto_output.allow_input.enabled",
                "allow fluids input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.allow_input.disabled",
                "disable fluids input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.enabled", "Fluid Auto Output: §aEnabled");
        provider.add("gtceu.gui.fluid_auto_output.disabled", "Fluid Auto Output: §cDisabled");
        multilineLang(provider, "gtceu.gui.fluid_auto_output.unselected",
                """
                        Fluid Auto Output
                        §7Select a side of the machine to configure its output.
                        """);
        multilineLang(provider, "gtceu.gui.fluid_auto_output.other_direction",
                """
                        Fluid Auto Output: §6Other Direction
                        §7The machine's fluid output is set to another direction.
                        §7Click to move the output to the currently selected side.
                        """);
        provider.add("gtceu.gui.auto_output.name", "auto");
        provider.add("gtceu.gui.overclock.title", "Overclock Tier");
        provider.add("gtceu.gui.overclock.range", "Available Tiers [%s, %s]");

        provider.add("gtceu.gui.machinemode.title", "Active Machine Mode");
        provider.add("gtceu.gui.machinemode", "Active Machine Mode: %s");
        provider.add("gtceu.gui.machinemode.tab_tooltip", "Change active Machine Mode");
        provider.add("gtceu.machine.available_recipe_map_1.tooltip", "Available Recipe Types: %s");
        provider.add("gtceu.machine.available_recipe_map_2.tooltip", "Available Recipe Types: %s, %s");
        provider.add("gtceu.machine.available_recipe_map_3.tooltip", "Available Recipe Types: %s, %s, %s");
        provider.add("gtceu.machine.available_recipe_map_4.tooltip", "Available Recipe Types: %s, %s, %s, %s");

        provider.add("gtceu.gui.content.chance_nc", "§cNot Consumed§r");
        provider.add("gtceu.gui.content.chance_nc_short", "§cNC§r");
        provider.add("gtceu.gui.content.chance_base", "Base Chance: %s%%");
        provider.add("gtceu.gui.content.chance_base_logic", "Base Chance: %s%% (%s)");
        provider.add("gtceu.gui.content.chance_no_boost", "Chance: %s%%");
        provider.add("gtceu.gui.content.chance_no_boost_logic", "Chance: %s%% (%s)");
        provider.add("gtceu.gui.content.chance_tier_boost_plus", "Bonus Chance: +%s%%/tier");
        provider.add("gtceu.gui.content.chance_tier_boost_minus", "Bonus Chance: -%s%%/tier");
        provider.add("gtceu.gui.content.chance_boosted", "Chance at Tier: %s%%");
        provider.add("gtceu.gui.content.chance_boosted_logic", "Chance at Tier: %s%% (%s)");
        provider.add("gtceu.gui.content.count_range", "%s-%sx");
        provider.add("gtceu.gui.content.fluid_range", "%s-%smB");
        provider.add("gtceu.gui.content.range", "%s-%s");
        provider.add("gtceu.gui.content.times_item", "x %s");

        provider.add("gtceu.chance_logic.or", "OR");
        provider.add("gtceu.chance_logic.and", "AND");
        provider.add("gtceu.chance_logic.xor", "XOR");
        provider.add("gtceu.chance_logic.first", "FIRST");
        provider.add("gtceu.chance_logic.none", "NONE");

        provider.add("gtceu.gui.content.per_tick", "§aConsumed/Produced Per Tick§r");
        provider.add("gtceu.gui.content.tips.per_tick_short", "§a/tick§r");
        provider.add("gtceu.gui.content.tips.per_second_short", "§a/second§r");

        provider.add("gtceu.gui.content.units.per_tick", "/t");
        provider.add("gtceu.gui.content.units.per_second", "/s");

        provider.add("gtceu.gui.me_network.online", "Network Status: §2Online§r");
        provider.add("gtceu.gui.me_network.offline", "Network Status: §4Offline§r");
        provider.add("gtceu.gui.waiting_list", "Sending Queue:");
        provider.add("gtceu.gui.config_slot", "§fConfig Slot§r");
        provider.add("gtceu.gui.config_slot.set", "§7Click to §bset/select§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.scroll", "§7Scroll wheel to §achange§7 config amount.§r");
        provider.add("gtceu.gui.config_slot.remove", "§7Right click to §4clear§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.set_only", "§7Click to §bset§7 config slot.§r");
        provider.add("gtceu.gui.config_slot.auto_pull_managed", "§4Disabled:§7 Managed by Auto-Pull");
        provider.add("gtceu.gui.me_bus.auto_pull_button", "Click to toggle automatic item pulling from ME");

        // Decor Stuff
        replace(provider, "block.gtceu.yellow_stripes_block.a", "Yellow Stripes Block");
        replace(provider, "block.gtceu.yellow_stripes_block.b", "Yellow Stripes Block");
        replace(provider, "block.gtceu.yellow_stripes_block.c", "Yellow Stripes Block");
        replace(provider, "block.gtceu.yellow_stripes_block.d", "Yellow Stripes Block");

        // Subtitles
        provider.add("gtceu.subtitle.boiler", "Boiler heating");
        provider.add("gtceu.subtitle.computation", "Computer beeps");
        provider.add("gtceu.subtitle.assembler", "Assembler constructing");
        provider.add("gtceu.subtitle.chainsaw", "Chainsaw revving");
        provider.add("gtceu.subtitle.compressor", "Compressor squeezing");
        provider.add("gtceu.subtitle.centrifuge", "Centrifuge spinning");
        provider.add("gtceu.subtitle.mortar", "Mortar crushing");
        provider.add("gtceu.subtitle.screwdriver", "Screwing");
        provider.add("gtceu.subtitle.saw", "Sawing");
        provider.add("gtceu.subtitle.miner", "Miner excavating");
        provider.add("gtceu.subtitle.turbine", "Turbine whizzing");
        provider.add("gtceu.subtitle.wrench", "Wrench rattling");
        provider.add("gtceu.subtitle.portal_opening", "Portal opens");
        provider.add("gtceu.subtitle.replicator", "Replicator copying");
        provider.add("gtceu.subtitle.arc", "Arcs buzzing");
        provider.add("gtceu.subtitle.combustion", "Combusting");
        provider.add("gtceu.subtitle.portable_scanner", "Scanning");
        provider.add("gtceu.subtitle.macerator", "Macerator crushing");
        provider.add("gtceu.subtitle.jet_engine", "Jet roaring");
        provider.add("gtceu.subtitle.spray_can", "Spraying");
        provider.add("gtceu.subtitle.mixer", "Mixer sloshing");
        provider.add("gtceu.subtitle.fire", "Fire crackling");
        provider.add("gtceu.subtitle.forge_hammer", "Forge Hammer thumping");
        provider.add("gtceu.subtitle.bath", "Bath fizzing");
        provider.add("gtceu.subtitle.soft_hammer", "Soft tap");
        provider.add("gtceu.subtitle.wirecutter", "Wire snipped");
        provider.add("gtceu.subtitle.chemical", "Chemical bubbling");
        provider.add("gtceu.subtitle.file", "File rasping");
        provider.add("gtceu.subtitle.portal_closing", "Portal closes");
        provider.add("gtceu.subtitle.motor", "Motor humming");
        provider.add("gtceu.subtitle.drill", "Drilling");
        provider.add("gtceu.subtitle.cut", "Cutter whirring");
        provider.add("gtceu.subtitle.furnace", "Furnace heating");
        provider.add("gtceu.subtitle.electrolyzer", "Electrolyzer sparking");
        provider.add("gtceu.subtitle.cooling", "Freezer humming");
        provider.add("gtceu.subtitle.plunger", "Plunger popping");
        provider.add("gtceu.subtitle.sus", "Sus...");
        provider.add("gtceu.subtitle.science", "s c i e n c e");
        provider.add("gtceu.subtitle.metal_pipe", "Destruction_Metal_Pole_L_Wave_2_0_0.wav");

        provider.add("effect.gtceu.weak_poison", "Weak Poison");

        provider.add("gtceu.tooltip.potion.header", "§6Contains effects:");
        provider.add("gtceu.tooltip.potion.each", "%s %s §7for§r %s §7ticks with a§r %s%% §7chance of happening§r");

        provider.add("gtceu.direction.tooltip.up", "Up");
        provider.add("gtceu.direction.tooltip.down", "Down");
        provider.add("gtceu.direction.tooltip.left", "Left");
        provider.add("gtceu.direction.tooltip.right", "Right");
        provider.add("gtceu.direction.tooltip.back", "Back");
        provider.add("gtceu.direction.tooltip.front", "Front");

        provider.add("gtceu.tooltip.status.trinary.false", "False");
        provider.add("gtceu.tooltip.status.trinary.true", "True");
        provider.add("gtceu.tooltip.status.trinary.unknown", "Unknown");

        provider.add("gtceu.tooltip.wireless_transmitter_bind",
                "Binding to a transmitter cover at %s %s %s facing %s in %s");
        provider.add("gtceu.tooltip.computer_monitor_config", "Storing computer monitor cover configuration data");
        provider.add("gtceu.tooltip.computer_monitor_data", "Storing data: %s");

        provider.add("gtceu.display_source.computer_monitor_cover", "Computer Monitor Cover");
        provider.add("gtceu.display_target.computer_monitor_cover", "Computer Monitor Cover");
        multiLang(provider, "gtceu.placeholder_info.energy",
                "Returns the amount of energy stored.",
                "Usage:",
                "  {energy} -> the amount of energy stored");
        multiLang(provider, "gtceu.placeholder_info.energyCapacity",
                "Returns the max amount of energy that can be stored",
                "Usage:",
                "{energyCapacity} -> the energy capacity");
        multiLang(provider, "gtceu.placeholder_info.itemCount",
                "Returns the amount of items (can be filtered).",
                "Usage:",
                "  {itemCount} -> total item amount",
                "  {itemCount <item_id>} -> amount of items with ids equal to item_id",
                "  {itemCount filter <slot_id>} -> amount of items matching filter in specified slot of this cover");
        multiLang(provider, "gtceu.placeholder_info.calc",
                "Returns the result of a math function or operation.",
                "Usage:",
                "  {calc <any_string>} -> any_string",
                "  {calc <round|floor|ceil|sqrt|~> <arg>} -> the result of the specified operation",
                "  {calc <first_arg> <+|-|*|/|//|>>|<<|%> <second_arg>} -> the result of the specified operation");
        multiLang(provider, "gtceu.placeholder_info.if",
                "Returns one of the arguments depending on the condition. The condition is considered true if it is not an empty string and is not equal to 0.",
                "Usage:",
                "  {if <condition> <returned_if_true> [returned_if_false]}");
        multiLang(provider, "gtceu.placeholder_info.obf",
                "Returns the text from the first argument, obfuscated.",
                "Usage:",
                "  {obf <text>} -> obfuscated text");
        multiLang(provider, "gtceu.placeholder_info.underline",
                "Returns the text from the first argument, underlined",
                "Usage:",
                "  {underline <text>} -> underlined text");
        multiLang(provider, "gtceu.placeholder_info.strike",
                "Returns the text from the first text, displaying it as if it was crossed out",
                "Usage:",
                "  {strike <text>} -> crossed-out text");
        multiLang(provider, "gtceu.placeholder_info.color",
                "Returns the text from the second argument, colored with the color from the first argument. All default minecraft chat colors can be used.",
                "Usage:",
                "  {color <color> <text>} -> colored text");
        multiLang(provider, "gtceu.placeholder_info.tick",
                "Returns the amount of ticks passed from when this cover was placed.",
                "Usage:",
                "  {tick} -> the amount of ticks");
        multiLang(provider, "gtceu.placeholder_info.block", "Returns the block symbol (█).",
                "Usage:",
                "  {block} -> '█'");
        multiLang(provider, "gtceu.placeholder_info.repeat",
                "Returns the text from the second arguments, repeated the amount of times specified in the first argument.",
                "Usage:",
                "  {repeat <amount> <text>} -> text repeated the specified amount of times");
        multiLang(provider, "gtceu.placeholder_info.random",
                "Returns a random number in the specified interval (inclusive).",
                "Usage:",
                "  {random <min> <max>} -> a random number between min and max (inclusive)");
        multiLang(provider, "gtceu.placeholder_info.select",
                "Returns the argument at the specified index (starting from 0)",
                "Usage:",
                "  {select <index> [arg1] [arg2] [arg3] ... -> argument at the specified index");
        multiLang(provider, "gtceu.placeholder_info.redstone",
                "Returns the redstone signal strength or sets the redstone output strength",
                "Usage:",
                "  {redstone get <up|down|north|south|east|west>} -> redstone signal strength (0-15) at the specified side",
                "  {redstone get link <slot_index> <freq_slot_index>} -> redstone signal strength of a Create redstone link frequency specified by a linked controller in slot #slot_index. freq_slot_index is the index of the frequency inside the controller (from left to right, 0-6)",
                "  {redstone set <power>} -> empty string, sets the redstone output strength from this cover's side",
                "  {redstone set link <slot_index> <freq_slot_index> <power>} -> empty string, broadcasts the specified redstone power on the specified Create redstone link frequency");
        multiLang(provider, "gtceu.placeholder_info.fluidCount",
                "Returns the amount of fluids (can be filtered).",
                "Usage:",
                "  {fluidCount [fluidId]} -> the amount of all fluids, or the fluid with fluidId if specified");
        multiLang(provider, "gtceu.placeholder_info.displayTarget",
                "Returns the specified line that was transmitted to this cover using a display link.",
                "Usage:",
                "  {displayTarget <line_number>} -> the text on the specified line (line number is 1-100)");
        multiLang(provider, "gtceu.placeholder_info.previousText",
                "Returns the text that was previously displayed by this cover at the specified line (before line-wrapping).",
                "Usage:",
                "  {previousText <line>} -> the text previously displayed on the specified line (index starts at 1)");
        multiLang(provider, "gtceu.placeholder_info.ae2itemCount",
                "Same as itemCount, but counts items in the ME network of the block this cover is attached to.",
                "Note that counting by filter or all items may cause lag!",
                "Usage:",
                "  {itemCount} -> total item amount",
                "  {itemCount <item_id>} -> amount of items with ids equal to item_id",
                "  {itemCount filter <slot_id>} -> amount of items matching filter in specified slot of this cover");
        multiLang(provider, "gtceu.placeholder_info.ae2fluidCount",
                "Same as fluidCount, but counts items in the ME network of the block this cover is attached to.",
                "Note that counting all fluids may cause lag!",
                "Usage:",
                "  {fluidCount [fluidId]} -> the amount of all fluids, or the fluid with fluidId if specified");
        multiLang(provider, "gtceu.placeholder_info.progress",
                "Returns the progress of the currently running recipe of the block this cover is attached to.",
                "Note that progress is an integer between 0 and {maxProgress}",
                "Usage:",
                "  {progress} -> the progress of the currently running recipe");
        multiLang(provider, "gtceu.placeholder_info.maxProgress",
                "Returns the maximum progress of the currently running recipe of the block this cover is attached to.",
                "Example: 'Progress: {calc {calc {progress} / {maxProgress}} * 100}%'",
                "Usage:",
                "  {maxProgress} -> the max progress of the currently running recipe");
        multiLang(provider, "gtceu.placeholder_info.maintenance",
                "Returns a 1 if there are maintenance problems in the block the cover is attached to, 0 otherwise.",
                "Example: 'Maintenance status: {if {maintenance} FIXING\\ REQUIRED OK}'",
                "Usage:",
                "  {maintenance} -> whether there are maintenance problems");
        multiLang(provider, "gtceu.placeholder_info.active",
                "Returns a 1 if the block the cover is attached to is currently running a recipe, 0 otherwise.",
                "Usage:",
                "  {active} -> whether there's a currently running recipe");
        multiLang(provider, "gtceu.placeholder_info.voltage",
                "Returns the voltage in the wire/cable the cover is on.",
                "Usage:",
                "  {voltage} -> the voltage in the wire/cable");
        multiLang(provider, "gtceu.placeholder_info.amperage",
                "Returns the amperage in the wire/cable the cover is on.",
                "Usage:",
                "  {amperage} -> the amperate in the wire/cable");
        multiLang(provider, "gtceu.placeholder_info.ae2energy",
                "Returns the energy currently stored in the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2energy} -> the energy in the ME network (in AE units)");
        multiLang(provider, "gtceu.placeholder_info.ae2maxPower",
                "Returns the energy capacity of the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2maxPower} -> the energy capacity of the ME network");
        multiLang(provider, "gtceu.placeholder_info.ae2powerUsage",
                "Returns the energy consumption of the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2powerUsage} -> the energy consumption of the ME network");
        multiLang(provider, "gtceu.placeholder_info.ae2spatial",
                "Returns information about spatial I/O in the ME network of the block this cover is on.",
                "Usage:",
                "  {ae2spatial power} -> the amount of power required to initiate spatial I/O",
                "  {ae2spatial efficiency} -> the efficiency of the Spatial Containment Structure (SPS)",
                "  {ae2spatial size<X|Y|Z>} -> the size of the SPS along the specified axis (example: 'Size: {sizeX}x{sizeY}x{sizeZ}')");
        multiLang(provider, "gtceu.placeholder_info.ae2crafting",
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
        multiLang(provider, "gtceu.placeholder_info.count",
                "Returns how many of the provided arguments are equal to the first (compared as strings, so \"0\" != \"0.0\")",
                "Usage:",
                "  {count <arg1> [arg2] [arg3] [arg4] ...} -> the amount of arguments that are equal to the first");
        multiLang(provider, "gtceu.placeholder_info.data",
                "Stores or retrieves some data from a data item (data stick/orb/module) in one of the slots.",
                "If you leave the <index> argument empty, it will be replaced with the value p (p is an integer from 0 to (capacity - 1) that is stored in the data item nbt).",
                "Usage:",
                "  {data get <slot> <index>} -> the data stored in the item in the specified slot",
                "  {data set <slot> <index> <value>} -> sets the data stored in the item in the specified slot, returns an empty string",
                "  {data getp <slot>} -> p",
                "  {data setp <slot> <value>} -> sets p, returns an empty string",
                "  {data inc <slot>} -> increments p by 1, if p becomes more than or equal to capacity, sets p to 0",
                "  {data dec <slot>} -> decrements p by 1, if p becomes less than 0, sets p to (capacity - 1)");
        multiLang(provider, "gtceu.placeholder_info.combine",
                "Combines all of it's arguments into a single string (by escaping all spaces between the arguments)",
                "Example: {combine abc def ghi jkl mno} -> \"abc\\ def\\ ghi\\ jkl\\ mno\"",
                "Usage:",
                "  {combine [arg1] [arg2] [arg3] ...} -> a string that will be treated as a single argument in further placeholders");
        multiLang(provider, "gtceu.placeholder_info.nbt",
                "Returns the nbt data of the item in the specified slot",
                "Usage:",
                "  {nbt <slot> [key1] [key2] [key3] ...} -> item_nbt[key1][key2][key3][...]");
        multiLang(provider, "gtceu.placeholder_info.toChars",
                "Returns the characters of the provided string with spaces between them",
                "Example: {toChars example} -> 'e x a m p l e'",
                "Usage:",
                "  {toChars <arg>} -> characters");
        multiLang(provider, "gtceu.placeholder_info.toAscii",
                "Returns the ASCII code of the provided character",
                "Usage:",
                "  {toAscii <character>} -> ASCII code of the character");
        multiLang(provider, "gtceu.placeholder_info.fromAscii",
                "Returns the character represented by the provided ASCII code",
                "Usage:",
                "  {fromAscii <char_code>} -> a character");
        multiLang(provider, "gtceu.gui.computer_monitor_cover.placeholder_reference",
                "All placeholders:",
                "(hover for more info)");
        multiLang(provider, "gtceu.placeholder_info.subList",
                "Returns arguments from with indexes from l (inclusive) to r (exclusive) (starting from 0)",
                "Usage:",
                "  {subList <left> <right> [arg0] [arg1] ...} -> all arguments with indexes from l to r separated by spaces");
        multiLang(provider, "gtceu.placeholder_info.cmp",
                "Returns a 1 or 0 based on the expression in it's arguments",
                "Usage:",
                "  {cmp <a> <operator> <b>} -> 1 or 0, operator is one of >, <, >=, <=, ==, !=");
        multiLang(provider, "gtceu.placeholder_info.bf",
                "Usage:",
                "  {bf <data_item_slot_index> <code>} -> empty string");
        multiLang(provider, "gtceu.placeholder_info.cmd",
                "Executes Minecraft commands and returns their output.",
                "Requires a data item bound to a player, bind any data item to yourself by right-clicking with it.",
                "Usage:",
                "  {cmd <slot_index> <command>} -> command output");
        multiLang(provider, "gtceu.placeholder_info.tm",
                "Returns the ™ symbol",
                "Usage:",
                "  {tm} -> the ™ symbol");
        multiLang(provider, "gtceu.placeholder_info.formatInt",
                "Returns a string representation of the provided integer",
                "Example: {formatInt 1236457} -> 1.24M",
                "Usage:",
                "  {formatInt <arg>} -> string representation of the int");
        multiLang(provider, "gtceu.placeholder_info.click",
                "Returns whether the targeted advanced monitor was clicked before the current tick",
                "Usage:",
                "  {click} -> \"1\" if the targeted advanced monitor was clicked, \"0\" otherwise",
                "  {click x} -> the x position of the last click (between 0 and 1)",
                "  {click y} -> the y position of the last click (between 0 and 1)");
        multiLang(provider, "gtceu.placeholder_info.ender",
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
        multiLang(provider, "gtceu.placeholder_info.eval",
                "Returns the result of evaluating the provided string which may placeholders",
                "Usage:",
                "  {eval abcdefg} -> abcdefg",
                "  {eval \"repeating a: {repeat 5 \\\"a \\\"}\" -> repeating a: a a a a a ",
                "  {eval \\\"\"{some random text}\"\\\" -> {some random text}",
                "  {eval \"text \"\\\"\"{something with spaces}\"\\\"\" more text\" -> text {something with spaces} more text");
        multiLang(provider, "gtceu.placeholder_info.bufferText",
                "Returns the text from a buffer accessible by ComputerCraft",
                "Usage:",
                "  {bufferText <line>} -> text from the buffer on the specified line (line is 1-100)");
        provider.add("gtceu.ender_item_link_cover.title", "Ender Item Link");
        provider.add("gtceu.ender_redstone_link_cover.title", "Ender Redstone Link");
        provider.add("gtceu.ender_redstone_link_cover.label", "Redstone power: %d");
        provider.add("gtceu.gui.computer_monitor_cover.update_interval", "Update interval (in ticks)");
        provider.add("gtceu.gui.computer_monitor_cover.edit_blank_placeholders", "Edit blank placeholders");
        provider.add("gtceu.gui.computer_monitor_cover.edit_displayed_text", "Edit displayed text");
        provider.add("gtceu.gui.central_monitor.text_scale", "Text scale");
        provider.add("gtceu.gui.central_monitor.group", "Group: %s");
        provider.add("gtceu.gui.central_monitor.group_default_name", "Group #%d");
        provider.add("gtceu.gui.central_monitor.none", "none");
        provider.add("gtceu.central_monitor.size", "Size: (%d+1+%d)x(%d+1+%d)");
        provider.add("gtceu.computer_monitor_cover.error.invalid_number", "Invalid number '%s'!");
        provider.add("gtceu.computer_monitor_cover.error.wrong_number_of_args", "Expected %d args, got %d!");
        provider.add("gtceu.computer_monitor_cover.error.not_enough_args", "Expected at least %d args, got %d!");
        provider.add("gtceu.computer_monitor_cover.error.no_cover", "No cover!");
        provider.add("gtceu.computer_monitor_cover.error.exception", "Unexpected exception occurred: %s");
        provider.add("gtceu.computer_monitor_cover.error.not_in_range",
                "Expected %s to be between %d and %d (inclusive), got %d");
        provider.add("gtceu.computer_monitor_cover.error.invalid_args", "Invalid arguments!");
        provider.add("gtceu.computer_monitor_cover.error.missing_item", "Missing %s in slot %d!");
        provider.add("gtceu.computer_monitor_cover.error.bf_invalid_num",
                "Invalid number at index %d when processing symbol number %d");
        provider.add("gtceu.computer_monitor_cover.error.bf_invalid", "Invalid character at %d");
        multiLang(provider, "gtceu.gui.computer_monitor_cover.main_textbox_tooltip",
                "Input string to display on line %d here.",
                "It can have placeholders, for example: 'Energy: {energy}/{energyCapacity} EU'",
                "Placeholders can also be inside other placeholders.");
        multiLang(provider, "gtceu.gui.computer_monitor_cover.slot_tooltip",
                "A slot for items that some placeholders can reference",
                "Slot number: %d");
        multiLang(provider, "gtceu.gui.computer_monitor_cover.second_page_textbox_tooltip",
                "Input placeholder to be used in place of %s '{}' here.",
                "For example, you can have a string 'Energy: {}/{} EU' and 'energy' and 'energyCapacity' in these text boxes.");
        provider.add("gtceu.computer_monitor_cover.error.no_placeholder", "No such placeholder: '%s'!");
        provider.add("gtceu.computer_monitor_cover.error.unclosed_bracket", "Unclosed bracket!");
        provider.add("gtceu.computer_monitor_cover.error.unexpected_bracket", "Unexpected closing bracket!");
        provider.add("gtceu.computer_monitor_cover.error.no_ae", "Cover holder does not have an AE2 network!");
        provider.add("gtceu.computer_monitor_cover.error.not_supported",
                "This feature is not supported by this block/cover!");
        provider.add("gtceu.central_monitor.gui.create_group", "Create group");
        provider.add("gtceu.central_monitor.gui.remove_from_group", "Remove from group");
        provider.add("gtceu.central_monitor.gui.set_target", "Set target");
        provider.add("gtceu.central_monitor.gui.currently_editing", "Currently editing: %s");
        multiLang(provider, "gtceu.central_monitor.info_tooltip",
                "In order to use monitors, you have to split them into groups first. A group may only have 1 module in it.",
                "Select them by left-clicking, then click 'Create group'.",
                "Then in the settings page for the group you can insert a module, you can configure it in the same page.",
                "To delete a group, select all of it's components and click 'Remove from group'.",
                "You can quickly select all components of a group by clicking on it's name. Click again to unselect.",
                "Some modules may display things depending on the block they target, to set a target for a group select any component of that group and right-click on the target component.",
                "You may wish to select a target that is not in the multiblock, you have to use the wireless transmitter cover for that.",
                "Place the cover on the target block, right-click it with a data stick and put that data stick into a data access hatch in the multiblock.",
                "Then select the data access hatch as the target, and set the slot index of your data stick in the number field that appeared.");
        provider.add("gtceu.tooltip.player_bind", "Bound to player: %s");
    }

    /**
     * Returns the sub-key consisting of the given key plus the given index.<br>
     * E.g.,<br>
     *
     * <pre>
     * <code>getSubKey("terminal.fluid_prospector.tier", 0)</code>
     * </pre>
     *
     * returns the <code>String</code>:
     *
     * <pre>
     * <code>
     * "terminal.fluid_prospector.tier.0"</code>
     * </pre>
     *
     * @param key   Base key of the sub-key.
     * @param index Index of the sub-key.
     * @return Sub-key consisting of key and index.
     */
    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }

    /**
     * Registers multiple values under the same key with a given provider.<br>
     * <br>
     * For example, a cumbersome way to add translations would be the following:<br>
     *
     * <pre>
     * <code>provider.add("terminal.fluid_prospector.tier.0", "radius size 1");
     * provider.add("terminal.fluid_prospector.tier.1", "radius size 2");
     * provider.add("terminal.fluid_prospector.tier.2", "radius size 3");</code>
     * </pre>
     *
     * Instead, <code>multiLang</code> can be used for the same result:
     *
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code>
     * </pre>
     *
     * In situations requiring a large number of generated translations, the
     * following could be used instead, which
     * generates translations for 100 tiers:
     *
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", IntStream.of(100)
     *                 .map(i -> i + 1)
     *                 .mapToObj(Integer::toString)
     *                 .map(i -> "radius size " + i)
     *                 .toArray(String[]::new));</code>
     * </pre>
     *
     * @param provider The provider to add to.
     * @param key      Base key of the key-value-pairs. The real key for each
     *                 translation will be appended by ".0" for
     *                 the first, ".1" for the second, etc. This ensures that the
     *                 keys are unique.
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
     *
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code>
     * </pre>
     *
     * The following code can be used to print out the translations:
     *
     * <pre>
     * <code>for (var component : getMultiLang("terminal.fluid_prospector.tier")) {
     *     System.out.println(component.getString());
     * }</code>
     * </pre>
     *
     * Result:
     *
     * <pre>
     * <code>radius size 1
     * radius size 2
     * radius size 3</code>
     * </pre>
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys
     */
    public static List<MutableComponent> getMultiLang(String key) {
        var outputKeys = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (Language.getInstance().has(next)) {
            outputKeys.add(next);
            next = getSubKey(key, ++i);
        }
        return outputKeys.stream().map(Component::translatable).collect(Collectors.toList());
    }

    /**
     * Gets all translation components from a multi lang's sub-keys. Supports
     * additional arguments for the translation
     * components.<br>
     * E.g., given a multi lang:
     *
     * <pre>
     * <code>multiLang(provider, "terminal.fluid_prospector.tier", "radius size 1", "radius size 2", "radius size 3");</code>
     * </pre>
     *
     * The following code can be used to print out the translations:
     *
     * <pre>
     * <code>for (var component : getMultiLang("terminal.fluid_prospector.tier")) {
     *     System.out.println(component.getString());
     * }</code>
     * </pre>
     *
     * Result:
     *
     * <pre>
     * <code>radius size 1
     * radius size 2
     * radius size 3</code>
     * </pre>
     *
     * @param key Base key of the multi lang. E.g. "terminal.fluid_prospector.tier".
     * @return Returns all translation components from a multi lang's sub-keys.
     */
    public static List<MutableComponent> getMultiLang(String key, Object... args) {
        var outputKeys = new ArrayList<String>();
        var i = 0;
        var next = getSubKey(key, i);
        while (Language.getInstance().has(next)) {
            outputKeys.add(next);
            next = getSubKey(key, ++i);
        }
        return outputKeys.stream().map(k -> Component.translatable(k, args)).collect(Collectors.toList());
    }

    /**
     * See {@link #getMultiLang(String)}. If no multiline key is available, get
     * single instead.
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
     * @param index Index of the single translation. E.g. 3 would return
     *              "gtceu.gui.overclock.enabled.3".
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiLang(String key, int index) {
        return Component.translatable(getSubKey(key, index));
    }

    /**
     * Gets a single translation from a multi lang. Supports additional arguments
     * for the translation component.
     *
     * @param key   Base key of the multi lang. E.g. "gtceu.gui.overclock.enabled".
     * @param index Index of the single translation. E.g. 3 would return
     *              "gtceu.gui.overclock.enabled.3".
     * @return Returns a single translation from a multi lang.
     */
    public static MutableComponent getFromMultiLang(String key, int index, Object... args) {
        return Component.translatable(getSubKey(key, index), args);
    }

    /**
     * Adds one key-value-pair to the given lang provider per line in the given
     * multiline (a multiline is a String
     * containing newline characters).<br>
     * Example:
     *
     * <pre>
     * <code>multilineLang(provider, "gtceu.gui.overclock.enabled", "Overclocking Enabled.\nClick to Disable");</code>
     * </pre>
     *
     * This results in the following translations:<br>
     *
     * <pre>
     * <code>"gtceu.gui.overclock.enabled.0": "Overclocking Enabled.",
     * "gtceu.gui.overclock.enabled.1": "Click to Disable",</code>
     * </pre>
     *
     * @param provider  The provider to add to.
     * @param key       Base key of the key-value-pair. The real key for each line
     *                  will be appended by ".0" for the
     *                  first line, ".1" for the second, etc. This ensures that the
     *                  keys are unique.
     * @param multiline The multiline string. It is a multiline because it contains
     *                  at least one newline character '\n'.
     */
    protected static void multilineLang(RegistrateLangProvider provider, String key, String multiline) {
        var lines = multiline.split("\n");
        multiLang(provider, key, lines);
    }

    /**
     * Replace a value in a language provider's mappings
     *
     * @param provider the provider whose mappings should be modified
     * @param key      the key for the value
     * @param value    the value to use in place of the old one
     */
    public static void replace(@NotNull RegistrateLangProvider provider, @NotNull String key,
                               @NotNull String value) {
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

            Method toUpsideDown = RegistrateLangProvider.class.getDeclaredMethod("toUpsideDown",
                    String.class);
            toUpsideDown.setAccessible(true);

            map.put(key, (String) toUpsideDown.invoke(provider, value));
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error replacing entry in datagen.", e);
        }
    }
}
