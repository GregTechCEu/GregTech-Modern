package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class CoverLang {

    public static void init(GTLangProvider provider) {
        generateCoverKeys(provider);
    }

    private static void generateCoverKeys(GTLangProvider provider) {
        // todo uses
        provider.add("cover.filter.blacklist.disabled", "Whitelist");
        provider.add("cover.filter.blacklist.enabled", "Blacklist");

        // Filter IO
        provider.add("cover.gtceu.filter.mode.filter_insert", "Filter Insert");
        provider.add("cover.gtceu.filter.mode.filter_extract", "Filter Extract");
        provider.add("cover.gtceu.filter.mode.filter_both", "Filter Insert/Extract");

        // Tag filter
        // todo: use these???
        provider.add("cover.tag_filter.title", "Tag Filter");
        provider.addMultiline("cover.gtceu.tag_filter.info",
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
        provider.add("cover.gtceu.test_slot.info",
                "Insert a item to test if it matches the filter expression");
        provider.add("cover.tag_filter.matches", "Item matches");
        provider.add("cover.tag_filter.matches_not", "Item does not match");

        // Fluid filter
        provider.add("cover.fluid_filter.title", "Fluid Filter");
        provider.addMultiline("cover.fluid_filter.config_amount",
                "Scroll wheel up increases amount, down decreases.\nShift[§6x10§r],Ctrl[§ex100§r],Shift+Ctrl[§ax1000§r]\nRight click increases amount, left click decreases.\nHold shift to double/halve.\nMiddle click to clear");
        provider.add("cover.fluid_filter.mode.filter_fill", "Filter Fill");
        provider.add("cover.fluid_filter.mode.filter_drain", "Filter Drain");
        provider.add("cover.fluid_filter.mode.filter_both", "Filter Fill & Drain");

        // Item filter
        provider.add("cover.item_filter.title", "Item Filter");
        provider.add("cover.item_filter.ignore_damage.disabled", "Respect Damage");
        provider.add("cover.item_filter.ignore_nbt.enabled", "Ignore NBT");
        provider.add("cover.item_filter.ignore_nbt.disabled", "Respect NBT");
        provider.add("cover.item_filter.ignore_damage.enabled", "Ignore Damage");
        provider.add("gtceu.item_filter.empty_item", "Empty (No Item)");
        provider.add("gtceu.item_filter.footer", "§eClick with item to override");

        // Smart filter
        provider.add("cover.item_smart_filter.title", "Smart Item Filter");
        provider.add("cover.item_smart_filter.filtering_mode.electrolyzer", "Electrolyzer");
        provider.add("cover.item_smart_filter.filtering_mode.centrifuge", "Centrifuge");
        provider.add("cover.item_smart_filter.filtering_mode.sifter", "Sifter");
        provider.addMultiline("cover.item_smart_filter.filtering_mode.description",
                "Select Machine this Smart Filter will use for filtering.\nIt will automatically pick right portions of items for robotic arm.");

        // Storage Cover
        provider.add("cover.storage.title", "Storage Cover");

        // Voiding
        provider.add("cover.voiding.voiding_mode.void_any", "Void Matching");
        provider.add("cover.voiding.voiding_mode.void_overflow", "Void Overflow");
        provider.addMultiline("cover.voiding.voiding_mode.description",
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

        // Conveyors
        provider.add("cover.conveyor.title", "Conveyor Cover Settings (%s)");
        provider.add("cover.conveyor.transfer_rate", "§7items/sec");
        provider.add("cover.conveyor.mode", "Mode: %s");
        provider.add("cover.conveyor.mode.export", "Mode: Export");
        provider.add("cover.conveyor.mode.import", "Mode: Import");
        provider.addMultiline("cover.conveyor.distribution.round_robin_global",
                "Distribution Mode: §bRound Robin\n§7Splits items equally across connected inventories");
        provider.addMultiline("cover.conveyor.distribution.round_robin_prio",
                "Distribution Mode: §bRound Robin with Priority\n§7Tries to split items across connected inventories and considers higher priorities first.\n§7Restrictive item pipes lower the priority of a path.");
        provider.addMultiline("cover.conveyor.distribution.insert_first",
                "Distribution Mode: §bPriority\n§7Will insert into the first inventory with the highest priority it can find.\n§7Restrictive item pipes lower the priority of a path.");
        provider.addMultiline("cover.conveyor.blocks_input.enabled",
                "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§aEnabled");
        provider.addMultiline("cover.conveyor.blocks_input.disabled",
                "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§cDisabled");

        // Common cover key entries
        provider.add("cover.universal.manual_import_export.mode.disabled",
                "Manual I/O: §bDisabled\n§7Items / Fluids will only move as specified by the cover and its filter.");
        provider.add("cover.universal.manual_import_export.mode.filtered",
                "Manual I/O: §bFiltered\n§7Items / Fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any)");
        provider.add("cover.universal.manual_import_export.mode.unfiltered",
                "Manual I/O: §bUnfiltered\n§7Items / Fluids can be moved independently of the cover mode. The filter only applies to what is inserted or extracted by this cover itself.");
        provider.addMultiline("cover.universal.manual_import_export.mode.description",
                "§eDisabled§r - Items/fluids will only move as specified by the cover and its filter. \n§eAllow Filtered§r - Items/fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any). \n§eAllow Unfiltered§r - Items/fluids can be moved independently of the cover mode. Filter applies to the items inserted or extracted by this cover");
        provider.add("cover.conveyor.item_filter.title", "Item Filter");
        provider.addMultiLang("cover.conveyor.tag.title", "Tag Name",
                "(use * for wildcard)");

        // Robot arm
        provider.add("cover.robotic_arm.title", "Robotic Arm Settings (%s)");
        provider.add("cover.robotic_arm.transfer_mode.transfer_any", "Transfer Any");
        provider.add("cover.robotic_arm.transfer_mode.transfer_exact", "Supply Exact");
        provider.add("cover.robotic_arm.transfer_mode.keep_exact", "Keep Exact");
        provider.addMultiline("cover.robotic_arm.transfer_mode.description",
                "§eTransfer Any§r - in this mode, cover will transfer as many items matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply items in portions specified in item filter slots (or variable under this button for tag filter). If amount of items is less than portion size, items won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of items in the destination inventory, supplying additional amount of items if required.\n§7Tip: left/right click on filter slots to change item amount,  use shift clicking to change amount faster.");

        // Pump
        provider.add("cover.pump.title", "Pump Cover Settings (%s)");
        provider.add("cover.pump.transfer_rate", "%s");
        provider.add("cover.pump.mode.export", "Mode: Export");
        provider.add("cover.pump.mode.import", "Mode: Import");
        provider.add("cover.pump.fluid_filter.title", "Fluid Filter");
        provider.add("cover.bucket.mode.bucket", "B");
        provider.add("cover.bucket.mode.milli_bucket", "mB");

        // Fluid regulator
        provider.add("cover.fluid_regulator.title", "Fluid Regulator Settings (%s)");
        provider.addMultiline("cover.fluid_regulator.transfer_mode.description",
                "§eTransfer Any§r - in this mode, cover will transfer as many fluids matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply fluids in portions specified in the window underneath this button. If amount of fluids is less than portion size, fluids won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of fluids in the destination inventory, supplying additional amount of fluids if required.\n§7Tip: shift click will multiply increase/decrease amounts by 10 and ctrl click will multiply by 100.");
        provider.add("cover.fluid_regulator.supply_exact", "Supply Exact: %s");
        provider.add("cover.fluid_regulator.keep_exact", "Keep Exact: %s");

        // Machine controller
        provider.add("cover.machine_controller.title", "Machine Controller Settings");
        provider.add("cover.machine_controller.normal", "Normal");
        provider.add("cover.machine_controller.inverted", "Inverted");
        provider.addMultiline("cover.machine_controller.invert.enabled",
                "§eInverted§r - in this mode, the cover will require a signal stronger than the set redstone level to run");
        provider.addMultiline("cover.machine_controller.invert.disabled",
                "§eNormal§r - in this mode, the cover will require a signal weaker than the set redstone level to run");
        provider.add("cover.machine_controller.redstone", "Min Redstone Strength: %d");
        provider.add("cover.machine_controller.mode.machine", "Control Machine");
        provider.add("cover.machine_controller.mode.cover_up", "Control Cover (Top)");
        provider.add("cover.machine_controller.mode.cover_down", "Control Cover (Bottom)");
        provider.add("cover.machine_controller.mode.cover_south", "Control Cover (South)");
        provider.add("cover.machine_controller.mode.cover_north", "Control Cover (North)");
        provider.add("cover.machine_controller.mode.cover_east", "Control Cover (East)");
        provider.add("cover.machine_controller.mode.cover_west", "Control Cover (West)");
        provider.add("cover.machine_controller.mode.null", "Control Nothing");

        // Ender fluid
        provider.add("cover.ender_fluid_link.title", "Ender Fluid Link");
        provider.add("cover.ender_fluid_link.iomode.enabled", "I/O Enabled");
        provider.add("cover.ender_fluid_link.iomode.disabled", "I/O Disabled");
        provider.add("cover.ender_fluid_link.tooltip.channel_description", "Set channel description with input text");
        provider.add("cover.ender_fluid_link.tooltip.channel_name", "Set channel name with input text");
        provider.add("cover.ender_fluid_link.tooltip.list_button", "Show channel list");
        provider.add("cover.ender_fluid_link.tooltip.clear_button", "Clear channel description");
        provider.addMultiline("cover.ender_fluid_link.private.tooltip.disabled",
                "Switch to private tank mode\nPrivate mode uses the player who originally placed the cover");
        provider.add("cover.ender_fluid_link.private.tooltip.enabled", "Switch to public tank mode");
        provider.addMultiline("cover.ender_fluid_link.incomplete_hex",
                "Inputted color is incomplete!\nIt will be applied once complete (all 8 hex digits)\nClosing the gui will lose edits!");

        // Detector
        provider.add("cover.detector_base.message_normal_state", "Monitoring Status: Normal");
        provider.add("cover.detector_base.message_inverted_state", "Monitoring Status: Inverted");

        var detectorLatchDescription = """
                Change the redstone behavior of this Cover.
                §eContinuous§7 - Default; values less than the minimum output 0; values higher than the maximum output 15; values between min and max output between 0 and 15
                §eLatched§7 - output 15 until above max, then output 0 until below min""";
        provider.addMultiline("cover.advanced_detector.latch.enabled",
                "Behavior: Latched\n\n" + detectorLatchDescription);
        provider.addMultiline("cover.advanced_detector.latch.disabled",
                "Behavior: Continuous\n\n" + detectorLatchDescription);

        // Energy Detector
        provider.add("gtceu.cover.energy_detector.message_electricity_storage_normal",
                "Monitoring Normal Electricity Storage");
        provider.add("gtceu.cover.energy_detector.message_electricity_storage_inverted",
                "Monitoring Inverted Electricity Storage");

        // Advanced energy detector
        provider.add("cover.advanced_energy_detector.label", "Advanced Energy Detector");
        provider.add("cover.advanced_energy_detector.min", "Min");
        provider.add("cover.advanced_energy_detector.max", "Max");

        var advancedEnergyDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when less than the minimum EU, and stops emitting when greater than the max EU";
        provider.addMultiline("cover.advanced_energy_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedEnergyDetectorInvertDescription);
        provider.addMultiline("cover.advanced_energy_detector.invert.disabled",
                "Output: Normal\n\n" + advancedEnergyDetectorInvertDescription);
        var advancedEnergyDetectorModeDescription = "Change between using discrete EU values or percentages for comparing min/max against an attached energy storage.";
        provider.addMultiline("cover.advanced_energy_detector.use_percent.enabled",
                "Mode: Percentage\n\n" + advancedEnergyDetectorModeDescription);
        provider.addMultiline("cover.advanced_energy_detector.use_percent.disabled",
                "Mode: Discrete EU\n\n" + advancedEnergyDetectorModeDescription);

        // Fluid detector
        provider.add("gtceu.cover.fluid_detector.message_fluid_storage_normal",
                "Monitoring Normal Fluid Storage");
        provider.add("gtceu.cover.fluid_detector.message_fluid_storage_inverted",
                "Monitoring Inverted Fluid Storage");

        // Advanced fluid detector
        provider.add("cover.advanced_fluid_detector.label", "Advanced Fluid Detector");
        var advancedFluidDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum mB of fluid, and starts emitting when greater than the min mB of fluid up to the set maximum";
        provider.addMultiline("cover.advanced_fluid_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedFluidDetectorInvertDescription);
        provider.addMultiline("cover.advanced_fluid_detector.invert.disabled",
                "Output: Normal\n\n" + advancedFluidDetectorInvertDescription);
        provider.add("cover.advanced_fluid_detector.max", "Max Fluid (mB)");
        provider.add("cover.advanced_fluid_detector.min", "Min Fluid (mB)");

        // Item detector
        provider.add("gtceu.cover.item_detector.message_item_storage_normal", "Monitoring Normal Item Storage");
        provider.add("gtceu.cover.item_detector.message_item_storage_inverted",
                "Monitoring Inverted Item Storage");

        // Advanced item detector
        provider.add("cover.advanced_item_detector.label", "Advanced Item Detector");
        var advancedItemDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum amount of items, and starts emitting when greater than the min amount of items up to the set maximum";
        provider.addMultiline("cover.advanced_item_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedItemDetectorInvertDescription);
        provider.addMultiline("cover.advanced_item_detector.invert.disabled",
                "Output: Normal\n\n" + advancedItemDetectorInvertDescription);
        provider.add("cover.advanced_item_detector.max", "Max Items");
        provider.add("cover.advanced_item_detector.min", "Min Items");

        // Activity detector
        provider.add("gtceu.cover.activity_detector.message_activity_normal",
                "Monitoring Normal Activity Status");
        provider.add("gtceu.cover.activity_detector.message_activity_inverted",
                "Monitoring Inverted Activity Status");
        provider.add("gtceu.cover.activity_detector_advanced.message_activity_normal",
                "Monitoring Normal Progress Status");
        provider.add("gtceu.cover.activity_detector_advanced.message_activity_inverted",
                "Monitoring Inverted Progress Status");

        // Shutter
        provider.add("cover.shutter.message.enabled", "Closed shutter");
        provider.add("cover.shutter.message.disabled", "Opened shutter");

        // Proxy cover
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

        // Digital wireless cover
        provider.addMultiLang("item.cover.digital.wireless.tooltip",
                "§fWirelessly§7 connects machines to the §fCentral Monitor§7 as §fCover§7.",
                "§fRight Click§7 on the §fCentral Monitor§7 to remotely bind to it.",
                "§fSneak Right Click§7 to remove the current binding.",
                "§aBinding: §f%s");

        // Central Monitor
        provider.add("monitor.gui.title.back", "Back");
        provider.add("monitor.gui.title.scale", "Scale:");
        provider.add("monitor.gui.title.argb", "ARGB:");
        provider.add("monitor.gui.title.slot", "Slot:");
        provider.add("monitor.gui.title.plugin", "Plugin:");
        provider.add("monitor.gui.title.config", "Config");

        provider.add("item.plugin.tooltips.1",
                "Plugins can be added to the screen for more functionality.");
        provider.add("item.plugin.proxy.tooltips.1", "(Please adjust to proxy mode in the screen)");
        provider.add("item.cover.digital.tooltip",
                "Connects machines over §fPower Cables§7 to the §fCentral Monitor§7 as §fCover§7.");
    }
}
