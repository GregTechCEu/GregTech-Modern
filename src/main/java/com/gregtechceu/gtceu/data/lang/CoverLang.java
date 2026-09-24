package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class CoverLang {

    public static void init(GTLangProvider provider) {
        generateCoverKeys(provider);
    }

    private static void generateCoverKeys(GTLangProvider provider) {
        // todo uses
        provider.add("cover.gtceu.filter.blacklist.disabled", "Whitelist");
        provider.add("cover.gtceu.filter.blacklist.enabled", "Blacklist");

        // Filter IO
        provider.add("cover.gtceu.filter.mode.filter_insert", "Filter Insert");
        provider.add("cover.gtceu.filter.mode.filter_extract", "Filter Extract");
        provider.add("cover.gtceu.filter.mode.filter_both", "Filter Insert/Extract");

        // Tag filter
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
        provider.add("cover.gtceu.tag_filter.matches", "Item matches");
        provider.add("cover.gtceu.tag_filter.matches_not", "Item does not match");

        // Smart filter
        provider.add("cover.gtceu.smart_item_filter.filtering_mode.electrolyzer", "Electrolyzer");
        provider.add("cover.gtceu.smart_item_filter.filtering_mode.centrifuge", "Centrifuge");
        provider.add("cover.gtceu.smart_item_filter.filtering_mode.sifter", "Sifter");
        provider.addMultiline("cover.gtceu.smart_item_filter.filtering_mode.description",
                "Select Machine this Smart Filter will use for filtering.\nIt will automatically pick right portions of items for robotic arm.");

        // Voiding
        provider.add("cover.gtceu.voiding.voiding_mode.void_any", "Void Matching");
        provider.add("cover.gtceu.voiding.voiding_mode.void_overflow", "Void Overflow");
        provider.addMultiline("cover.gtceu.voiding.voiding_mode.description",
                "§eVoid Matching§r will void anything matching the filter. \n§eVoid Overflow§r will void anything matching the filter, up to the specified amount.");
        provider.add("cover.gtceu.voiding.warning_tooltip",
                "§cWARNING!§7 Setting this to \"Enabled\" means that fluids or items WILL be voided.");
        provider.add("cover.gtceu.voiding.message.disabled", "Voiding Cover Disabled");
        provider.add("cover.gtceu.voiding.message.enabled", "Voiding Cover Enabled");

        // Conveyors
        provider.addMultiline("cover.gtceu.conveyor.distribution_mode.round_robin_global",
                "Distribution Mode: §bRound Robin\n§7Splits items equally across connected inventories");
        provider.addMultiline("cover.gtceu.conveyor.distribution_mode.round_robin_prio",
                """
                        Distribution Mode: §bRound Robin with Restriction
                        §7Tries to split items equally across connected inventories.
                        §7Will not send items down Restrictive item pipes unless no other paths are available.""");
        provider.addMultiline("cover.gtceu.conveyor.distribution_mode.insert_first", """
                Distribution Mode: §bPriority
                §7Will insert into the first inventory with the highest priority it can find.
                §7Restrictive item pipes lower the priority of a path.
                """);

        // Common cover key entries
        provider.add("cover.gtceu.manual_io_mode.disabled",
                "Manual I/O: §bDisabled\n§7Items / Fluids will only move as specified by the cover and its filter.");
        provider.add("cover.gtceu.manual_io_mode.filtered",
                "Manual I/O: §bFiltered\n§7Items / Fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any)");
        provider.add("cover.gtceu.manual_io_mode.unfiltered",
                "Manual I/O: §bUnfiltered\n§7Items / Fluids can be moved independently of the cover mode. The filter only applies to what is inserted or extracted by this cover itself.");
        provider.addMultiline("cover.gtceu.manual_io_mode.description",
                """
                        §eDisabled§r - Items/fluids will only move as specified by the cover and its filter.
                        §eAllow Filtered§r - Items/fluids can be extracted and inserted independently of the cover mode, as long as its filter matches (if any).
                        §eAllow Unfiltered§r - Items/fluids can be moved independently of the cover mode. Filter applies to the items inserted or extracted by this cover
                        """);

        // Robot arm
        provider.add("cover.gtceu.robot_arm_transfer_mode.transfer_any", "Transfer Any");
        provider.add("cover.gtceu.robot_arm_transfer_mode.transfer_exact", "Supply Exact");
        provider.add("cover.gtceu.robot_arm_transfer_mode.keep_exact", "Keep Exact");
        provider.addMultiline("cover.gtceu.robot_arm_transfer_mode.description",
                """
                        §eTransfer Any§r - in this mode, cover will transfer as many items matching its filter as possible.
                        §eSupply Exact§r - in this mode, cover will supply items in portions specified in item filter slots (or variable under this button for tag filter). If amount of items is less than portion size, items won't be moved.
                        §eKeep Exact§r - in this mode, cover will keep specified amount of items in the destination inventory, supplying additional amount of items if required.
                        §7Tip: left/right click on filter slots to change item amount,  use shift clicking to change amount faster.");
                        """);

        // Fluid regulator
        provider.addMultiline("cover.gtceu.fluid_regulator.transfer_mode.description",
                """
                        §eTransfer Any§r - in this mode, cover will transfer as many fluids matching its filter as possible.
                        §eSupply Exact§r - in this mode, cover will supply fluids in portions specified in the window underneath this button. If amount of fluids is less than portion size, fluids won't be moved.
                        §eKeep Exact§r - in this mode, cover will keep specified amount of fluids in the destination inventory, supplying additional amount of fluids if required.
                        §7Tip: shift click will multiply increase/decrease amounts by 10 and ctrl click will multiply by 100.
                        """);
        provider.add("cover.gtceu.fluid_regulator.supply_exact", "Supply Exact: %s");
        provider.add("cover.gtceu.fluid_regulator.keep_exact", "Keep Exact: %s");

        // Machine controller
        provider.add("cover.gtceu.machine_controller.normal", "Normal");
        provider.add("cover.gtceu.machine_controller.inverted", "Inverted");
        provider.addMultiline("cover.gtceu.machine_controller.invert.enabled",
                "§eInverted§r - in this mode, the cover will require a signal stronger than the set redstone level to run");
        provider.addMultiline("cover.gtceu.machine_controller.invert.disabled",
                "§eNormal§r - in this mode, the cover will require a signal weaker than the set redstone level to run");
        provider.add("cover.gtceu.machine_controller.redstone", "Min Redstone Strength: %d");
        provider.add("cover.gtceu.machine_controller.suspend_powerfail", "Prevent Power Failing:");
        provider.add("cover.gtceu.machine_controller.mode.machine", "Control Machine");
        provider.add("cover.gtceu.machine_controller.mode.cover_up", "Control Cover (Top)");
        provider.add("cover.gtceu.machine_controller.mode.cover_down", "Control Cover (Bottom)");
        provider.add("cover.gtceu.machine_controller.mode.cover_south", "Control Cover (South)");
        provider.add("cover.gtceu.machine_controller.mode.cover_north", "Control Cover (North)");
        provider.add("cover.gtceu.machine_controller.mode.cover_east", "Control Cover (East)");
        provider.add("cover.gtceu.machine_controller.mode.cover_west", "Control Cover (West)");
        provider.add("cover.gtceu.machine_controller.mode.null", "Control Nothing");

        // Ender fluid
        provider.add("cover.gtceu.ender_link.iomode.enabled", "I/O Enabled");
        provider.add("cover.gtceu.ender_link.iomode.disabled", "I/O Disabled");
        provider.add("cover.gtceu.ender_link.tooltip.channel_description", "Set channel description with input text");
        provider.add("cover.gtceu.ender_link.tooltip.channel_name", "Set channel name with input text");
        provider.add("cover.gtceu.ender_link.tooltip.list_button", "Show channel list");
        provider.add("cover.gtceu.ender_link.tooltip.clear_button", "Clear channel description");
        provider.addMultiline("cover.gtceu.ender_link.private.tooltip.disabled",
                "Switch to private mode\nPrivate mode uses the player who originally placed the cover");
        provider.add("cover.gtceu.ender_link.private.tooltip.enabled", "Switch to public mode");
        provider.addMultiline("cover.gtceu.ender_link.incomplete_hex",
                "Inputted color is incomplete!\nIt will be applied once complete (all 8 hex digits)\nClosing the gui will lose edits!");

        // Detector
        provider.add("cover.gtceu.detector_cover.message_normal_state", "Monitoring Status: Normal");
        provider.add("cover.gtceu.detector_cover.message_inverted_state", "Monitoring Status: Inverted");

        var detectorLatchDescription = """
                Change the redstone behavior of this Cover.
                §eContinuous§7 - Default; values less than the minimum output 0; values higher than the maximum output 15; values between min and max output between 0 and 15
                §eLatched§7 - output 15 until above max, then output 0 until below min""";

        provider.addMultiline("cover.gtceu.advanced_detector.latch.enabled",
                "Behavior: Latched\n\n" + detectorLatchDescription);
        provider.addMultiline("cover.gtceu.advanced_detector.latch.disabled",
                "Behavior: Continuous\n\n" + detectorLatchDescription);

        // Advanced energy detector
        provider.add("cover.gtceu.advanced_energy_detector.min", "Min");
        provider.add("cover.gtceu.advanced_energy_detector.max", "Max");

        var advancedEnergyDetectorInvertDescription = """
                        Toggle to invert the redstone logic
                        By default, redstone is emitted when less than the minimum EU, and stops emitting when greater than the max EU
                """;

        provider.addMultiline("cover.gtceu.advanced_energy_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedEnergyDetectorInvertDescription);
        provider.addMultiline("cover.gtceu.advanced_energy_detector.invert.disabled",
                "Output: Normal\n\n" + advancedEnergyDetectorInvertDescription);
        var advancedEnergyDetectorModeDescription = "Change between using discrete EU values or percentages for comparing min/max against an attached energy storage.";
        provider.addMultiline("cover.gtceu.advanced_energy_detector.use_percent.enabled",
                "Mode: Percentage\n\n" + advancedEnergyDetectorModeDescription);
        provider.addMultiline("cover.gtceu.advanced_energy_detector.use_percent.disabled",
                "Mode: Discrete EU\n\n" + advancedEnergyDetectorModeDescription);

        // Advanced fluid detector
        var advancedFluidDetectorInvertDescription = """
                Toggle to invert the redstone logic
                By default, redstone stops emitting when less than the minimum mB of fluid, and starts emitting when greater than the min mB of fluid up to the set maximum.
                """;

        provider.addMultiline("cover.gtceu.advanced_fluid_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedFluidDetectorInvertDescription);
        provider.addMultiline("cover.gtceu.advanced_fluid_detector.invert.disabled",
                "Output: Normal\n\n" + advancedFluidDetectorInvertDescription);
        provider.add("cover.gtceu.advanced_fluid_detector.max", "Max Fluid (mB)");
        provider.add("cover.gtceu.advanced_fluid_detector.min", "Min Fluid (mB)");

        // Advanced item detector
        var advancedItemDetectorInvertDescription = """
                Toggle to invert the redstone logic
                By default, redstone stops emitting when less than the minimum amount of items, and starts emitting when greater than the min amount of items up to the set maximum.
                """;

        provider.addMultiline("cover.gtceu.advanced_item_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedItemDetectorInvertDescription);
        provider.addMultiline("cover.gtceu.advanced_item_detector.invert.disabled",
                "Output: Normal\n\n" + advancedItemDetectorInvertDescription);
        provider.add("cover.gtceu.advanced_item_detector.max", "Max Items");
        provider.add("cover.gtceu.advanced_item_detector.min", "Min Items");

        // Shutter
        provider.add("cover.gtceu.shutter.enabled", "Closed shutter");
        provider.add("cover.gtceu.shutter.disabled", "Opened shutter");
    }
}
