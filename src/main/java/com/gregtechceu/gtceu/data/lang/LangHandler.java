package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;

public class LangHandler {

    /**
     *
     * Unformatted text must be in light gray - §7
     * Items must be in gold - §6
     * Fluids must be in blue - §9
     * Directions must be in yellow - §e
     * Disabled/Inactive/Errors must be in red - §c
     * Enabled/Active must be in green - §a
     * Potion effects must be in yellow - §e
     * Time must be in red - §c
     * Percentages must be in green - §a
     * Keys must be in all caps
     * Key combos must follow the format KEY1 + KEY2 (for example, SHIFT + R-CLICK)
     *
     * @param provider
     */

    public static void init(RegistrateLangProvider provider) {
        AdvancementLang.init(provider);
        ArmorLang.init(provider);
        BlockLang.init(provider);
        CommandLang.init(provider);
        ConfigurationLang.init(provider);
        CoverLang.init(provider);
        HazardLang.init(provider);
        IntegrationLang.init(provider);
        ItemLang.init(provider);
        MachineLang.init(provider);
        MaterialLang.init(provider);
        RecipeLang.init(provider);
        SubtitleLang.init(provider);
        ToolLang.init(provider);

        generateTooltipKeys(provider);

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

        provider.add("gtceu.multiblock.page_switcher.io.import", "§2Inputs");
        provider.add("gtceu.multiblock.page_switcher.io.export", "§4Outputs");
        provider.add("gtceu.multiblock.page_switcher.io.both", "§5Combined Inputs + Outputs");

        provider.add("enchantment.disjunction", "Disjunction");

        provider.add("fluid.empty", "Empty");
        provider.add("item.generic.fluid_container.tooltip", "%d/%dL %s");
        provider.add("item.electric.tooltip", "%d/%d EU - Tier %s");
        provider.add("item.electric.stored", "%d/%d EU (%s)");
        provider.add("item.electric.discharge_mode.enabled", "§eDischarge Mode Enabled");
        provider.add("item.electric.discharge_mode.disabled", "§eDischarge Mode Disabled");
        provider.add("item.electric.discharge_mode.tooltip", "Use while sneaking to toggle discharge mode");

        provider.add("item.dust.tooltip.purify", "Right click a Cauldron to get clean Dust");
        provider.add("item.crushed.tooltip.purify", "Right click a Cauldron to get Purified Ore");
        provider.add("item.programmed_circuit.configuration", "Configuration: %d");

        provider.add("item.machine_configuration.mode", "§aConfiguration Mode:§r %s");
        provider.add("gtceu.tool.mode.fluid", "§9Fluid§r");
        provider.add("gtceu.tool.mode.item", "§6Item§r");
        provider.add("gtceu.tool.mode.both", "§dBoth (Fluid And Item)§r");
        provider.add("gtceu.multiblock.dimension", "§eDimensions: §r%sx%sx%s");

        provider.add("item.gtceu.tool.replace_tool_head", "Craft with a new Tool Head to replace it");
        provider.add("item.gtceu.tool.usable_as", "§8Usable as: §f%s");

        provider.add("item.gtceu.tool.aoe.rows", "Rows");
        provider.add("item.gtceu.tool.aoe.columns", "Columns");
        provider.add("item.gtceu.tool.aoe.layers", "Layers");

        provider.add("item.gtceu.turbine_rotor.tooltip", "Turbine Rotors for your power station");
        provider.add("item.clipboard.tooltip",
                "Can be written on (without any writing Instrument). Right-click on Wall to place, and Shift-Right-Click to remove");

        provider.add("item.tool.tooltip.primary_material", "§fMaterial: §e%s");
        provider.add("item.tool.tooltip.durability", "§fDurability: §a%d / %d");
        provider.add("item.tool.tooltip.rotor.efficiency", "Turbine Efficiency: §9%d%%");
        provider.add("item.tool.tooltip.rotor.power", "Turbine Power: §9%d%%");

        provider.add("item.record.sus.tooltip", "§7Leonz - Among Us Drip");
        provider.add("item.gtceu.nan_certificate.tooltip", "Challenge Accepted!");
        provider.add("item.gtceu.blacklight.tooltip", "Long-Wave §dUltraviolet§7 light source");
        provider.add("gui.widget.incrementButton.default_tooltip",
                "Hold Shift, Ctrl or both to change the amount");
        provider.add("gui.widget.recipeProgressWidget.default_tooltip", "Show Recipes");
        multilineLang(provider, "gtceu.recipe_memory_widget.tooltip",
                "§7Left click to automatically input this recipe into the crafting grid\n§7Shift click to lock/unlock this recipe");

        replace(provider, "item.gtceu.bucket", "%s Bucket");

        provider.add("item.netherrack_nether_quartz", "Nether Quartz Ore");
        provider.add("block.surface_rock", "%s Surface Rock");

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

        provider.add("behavior.item_magnet.enabled", "§aMagnetic Field Enabled");
        provider.add("behavior.item_magnet.disabled", "§cMagnetic Field Disabled");
        provider.add("behavior.data_item.assemblyline.title", "§nAssembly Line Construction Data:");
        provider.add("behavior.data_item.assemblyline.data", "- §a%s");

        provider.add("item.terminal.tooltip", "Sharp tools make good work");
        provider.add("item.terminal.tooltip.creative", "§bCreative Mode");
        provider.add("item.terminal.tooltip.hardware", "§aHardware: %d");
        provider.add("item.plugin.tooltips.1",
                "Plugins can be added to the screen for more functionality.");
        provider.add("item.plugin.proxy.tooltips.1", "(Please adjust to proxy mode in the screen)");
        provider.add("item.cover.digital.tooltip",
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

        provider.add("item.cover.digital.mode.proxy.disabled", "Click to enable Proxy Mode");
        provider.add("item.cover.digital.mode.proxy.enabled", "Proxy Mode enabled");
        provider.add("item.cover.digital.mode.machine.disabled", "Click to enable Machine Mode");
        provider.add("item.cover.digital.mode.machine.enabled", "Machine Mode enabled");
        provider.add("item.cover.digital.mode.energy.disabled", "Click to enable Energy Mode");
        provider.add("item.cover.digital.mode.energy.enabled", "Energy Mode enabled");
        provider.add("item.cover.digital.mode.item.disabled", "Click to enable Item Mode");
        provider.add("item.cover.digital.mode.item.enabled", "Item Mode enabled");
        provider.add("item.cover.digital.mode.fluid.disabled", "Click to enable Fluid Mode");
        provider.add("item.cover.digital.mode.fluid.enabled", "Fluid Mode enabled");

        // todo: own method (gui/tooltip/???)
        provider.add("gtceu.part_sharing.disabled", "Multiblock Sharing §4Disabled");
        provider.add("gtceu.part_sharing.enabled", "Multiblock Sharing §aEnabled");
        provider.add("gtceu.universal.liters", "%s mB");
        provider.add("gtceu.universal.kiloliters", "%s B");
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
        multilineLang(provider, "gtceu.gui.multiblock_item_voiding", "Voiding Mode\n§7Voiding §6Items");
        multilineLang(provider, "gtceu.gui.multiblock_fluid_voiding", "Voiding Mode\n§7Voiding §9Fluids");
        multilineLang(provider, "gtceu.gui.multiblock_item_fluid_voiding",
                "Voiding Mode\n§7Voiding §6Items §7and §9Fluids");
        multilineLang(provider, "gtceu.gui.multiblock_no_voiding", "Voiding Mode\n§7Voiding Nothing");
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

        multiLang(provider, "item.cover.digital.wireless.tooltip",
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
        provider.add("gtceu.gui.fluid_auto_output.allow_input.enabled",
                "allow fluids input from the output side");
        provider.add("gtceu.gui.fluid_auto_output.allow_input.disabled",
                "disable fluids input from the output side");
        provider.add("gtceu.gui.auto_output.name", "auto");
        provider.add("gtceu.gui.overclock.title", "Overclock Tier");
        provider.add("gtceu.gui.overclock.range", "Available Tiers [%s, %s]");

        provider.add("gtceu.gui.machinemode.title", "Active Machine Mode");
        provider.add("gtceu.gui.machinemode", "Active Machine Mode: %s");
        provider.add("gtceu.machine.available_recipe_map_1.tooltip", "Available Recipe Types: %s");
        provider.add("gtceu.machine.available_recipe_map_2.tooltip", "Available Recipe Types: %s, %s");
        provider.add("gtceu.machine.available_recipe_map_3.tooltip", "Available Recipe Types: %s, %s, %s");
        provider.add("gtceu.machine.available_recipe_map_4.tooltip", "Available Recipe Types: %s, %s, %s, %s");

        provider.add("gtceu.gui.content.chance_nc", "§cNot Consumed§r");
        provider.add("gtceu.gui.content.chance_nc_short", "§cNC§r");
        provider.add("gtceu.gui.content.chance_base", "Base Chance: %s%%");
        provider.add("gtceu.gui.content.chance_base_logic", "Base Chance: %s%% (%s)");
        provider.add("gtceu.gui.content.chance_tier_boost_plus", "Bonus Chance: +%s%%/tier");
        provider.add("gtceu.gui.content.chance_tier_boost_minus", "Bonus Chance: -%s%%/tier");
        provider.add("gtceu.gui.content.chance_boosted", "Chance at Tier: %s%%");
        provider.add("gtceu.gui.content.chance_boosted_logic", "Chance at Tier: %s%% (%s)");
        provider.add("gtceu.gui.content.count_range", "%s-%sx");

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
    }

    public static void generateTooltipKeys(RegistrateLangProvider provider) {
        provider.add("gtceu.tooltip.hold_shift", "§7Hold SHIFT for more info");
        provider.add("gtceu.tooltip.hold_ctrl", "§7Hold CTRL for more info");
        provider.add("gtceu.tooltip.fluid_pipe_hold_shift", "§7Hold SHIFT to show Fluid Containment Info");
        provider.add("gtceu.tooltip.tool_fluid_hold_shift",
                "§7Hold SHIFT to show Fluid Containment and Tool Info");
    }
}
