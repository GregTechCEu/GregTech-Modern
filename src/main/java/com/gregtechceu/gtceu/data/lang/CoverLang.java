package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

import static com.gregtechceu.gtceu.data.lang.LangUtil.*;

public class CoverLang {

    public static void init(RegistrateLangProvider provider) {
        generateCoverKeys(provider);
    }

    private static void generateCoverKeys(RegistrateLangProvider provider) {
        // todo uses
        provider.add("cover.filter.blacklist.disabled", "Whitelist");
        provider.add("cover.filter.blacklist.enabled", "Blacklist");

        // filter
        provider.add("cover.gtceu.filter.mode.filter_insert", "Filter Insert");
        provider.add("cover.gtceu.filter.mode.filter_extract", "Filter Extract");
        provider.add("cover.gtceu.filter.mode.filter_both", "Filter Insert/Extract");

        // tag filter
        // todo: use these???
        provider.add("cover.tag_filter.title", "Tag Filter");
        multilineLang(provider, "cover.gtceu.tag_filter.info",
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

        // fluid filter
        provider.add("cover.fluid_filter.title", "Fluid Filter");
        multilineLang(provider, "cover.fluid_filter.config_amount",
                "Scroll wheel up increases amount, down decreases.\nShift[§6x10§r],Ctrl[§ex100§r],Shift+Ctrl[§ax1000§r]\nRight click increases amount, left click decreases.\nHold shift to double/halve.\nMiddle click to clear");
        provider.add("cover.fluid_filter.mode.filter_fill", "Filter Fill");
        provider.add("cover.fluid_filter.mode.filter_drain", "Filter Drain");
        provider.add("cover.fluid_filter.mode.filter_both", "Filter Fill & Drain");

        // item filter
        provider.add("cover.item_filter.title", "Item Filter");
        provider.add("cover.item_filter.ignore_damage.disabled", "Respect Damage");
        provider.add("cover.item_filter.ignore_nbt.enabled", "Ignore NBT");
        provider.add("cover.item_filter.ignore_nbt.disabled", "Respect NBT");
        provider.add("cover.item_filter.ignore_damage.enabled", "Ignore Damage");

        // smart filter
        provider.add("cover.item_smart_filter.title", "Smart Item Filter");
        provider.add("cover.item_smart_filter.filtering_mode.electrolyzer", "Electrolyzer");
        provider.add("cover.item_smart_filter.filtering_mode.centrifuge", "Centrifuge");
        provider.add("cover.item_smart_filter.filtering_mode.sifter", "Sifter");
        multilineLang(provider, "cover.item_smart_filter.filtering_mode.description",
                "Select Machine this Smart Filter will use for filtering.\nIt will automatically pick right portions of items for robotic arm.");

        provider.add("cover.storage.title", "Storage Cover");

        // voiding
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

        // conveyors
        provider.add("cover.conveyor.title", "Conveyor Cover Settings (%s)");
        provider.add("cover.conveyor.transfer_rate", "§7items/sec");
        provider.add("cover.conveyor.mode", "Mode: %s");
        provider.add("cover.conveyor.mode.export", "Mode: Export");
        provider.add("cover.conveyor.mode.import", "Mode: Import");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_global",
                "Distribution Mode: §bRound Robin\n§7Splits items equally across connected inventories");
        multilineLang(provider, "cover.conveyor.distribution.round_robin_prio",
                "Distribution Mode: §bRound Robin with Priority\n§7Tries to split items across connected inventories and considers higher priorities first.\n§7Restrictive item pipes lower the priority of a path.");
        multilineLang(provider, "cover.conveyor.distribution.insert_first",
                "Distribution Mode: §bPriority\n§7Will insert into the first inventory with the highest priority it can find.\n§7Restrictive item pipes lower the priority of a path.");
        multilineLang(provider, "cover.conveyor.blocks_input.enabled",
                "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§aEnabled");
        multilineLang(provider, "cover.conveyor.blocks_input.disabled",
                "If enabled, items will not be inserted when cover is set to pull items from the inventory into pipe.\n§cDisabled");

        // cover universal whatever that is
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

        // robot arm
        provider.add("cover.robotic_arm.title", "Robotic Arm Settings (%s)");
        provider.add("cover.robotic_arm.transfer_mode.transfer_any", "Transfer Any");
        provider.add("cover.robotic_arm.transfer_mode.transfer_exact", "Supply Exact");
        provider.add("cover.robotic_arm.transfer_mode.keep_exact", "Keep Exact");
        multilineLang(provider, "cover.robotic_arm.transfer_mode.description",
                "§eTransfer Any§r - in this mode, cover will transfer as many items matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply items in portions specified in item filter slots (or variable under this button for tag filter). If amount of items is less than portion size, items won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of items in the destination inventory, supplying additional amount of items if required.\n§7Tip: left/right click on filter slots to change item amount,  use shift clicking to change amount faster.");

        // pump
        provider.add("cover.pump.title", "Pump Cover Settings (%s)");
        provider.add("cover.pump.transfer_rate", "%s");
        provider.add("cover.pump.mode.export", "Mode: Export");
        provider.add("cover.pump.mode.import", "Mode: Import");
        provider.add("cover.pump.fluid_filter.title", "Fluid Filter");
        provider.add("cover.bucket.mode.bucket", "B");
        provider.add("cover.bucket.mode.milli_bucket", "mB");

        // fluid regulator
        provider.add("cover.fluid_regulator.title", "Fluid Regulator Settings (%s)");
        multilineLang(provider, "cover.fluid_regulator.transfer_mode.description",
                "§eTransfer Any§r - in this mode, cover will transfer as many fluids matching its filter as possible.\n§eSupply Exact§r - in this mode, cover will supply fluids in portions specified in the window underneath this button. If amount of fluids is less than portion size, fluids won't be moved.\n§eKeep Exact§r - in this mode, cover will keep specified amount of fluids in the destination inventory, supplying additional amount of fluids if required.\n§7Tip: shift click will multiply increase/decrease amounts by 10 and ctrl click will multiply by 100.");
        provider.add("cover.fluid_regulator.supply_exact", "Supply Exact: %s");
        provider.add("cover.fluid_regulator.keep_exact", "Keep Exact: %s");

        // machine controller
        provider.add("cover.machine_controller.title", "Machine Controller Settings");
        provider.add("cover.machine_controller.normal", "Normal");
        provider.add("cover.machine_controller.inverted", "Inverted");
        multilineLang(provider, "cover.machine_controller.invert.enabled",
                "§eInverted§r - in this mode, the cover will require a signal stronger than the set redstone level to run");
        multilineLang(provider, "cover.machine_controller.invert.disabled",
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

        // ender fluid
        provider.add("cover.ender_fluid_link.title", "Ender Fluid Link");
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

        // detector
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

        // advanced energy detector
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

        // advanced fluid cover
        provider.add("cover.advanced_fluid_detector.label", "Advanced Fluid Detector");
        var advancedFluidDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum mB of fluid, and starts emitting when greater than the min mB of fluid up to the set maximum";
        multilineLang(provider, "cover.advanced_fluid_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedFluidDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_fluid_detector.invert.disabled",
                "Output: Normal\n\n" + advancedFluidDetectorInvertDescription);
        provider.add("cover.advanced_fluid_detector.max", "Max Fluid (mB)");
        provider.add("cover.advanced_fluid_detector.min", "Min Fluid (mB)");

        // advanced item detector
        provider.add("cover.advanced_item_detector.label", "Advanced Item Detector");
        var advancedItemDetectorInvertDescription = "Toggle to invert the redstone logic\nBy default, redstone stops emitting when less than the minimum amount of items, and starts emitting when greater than the min amount of items up to the set maximum";
        multilineLang(provider, "cover.advanced_item_detector.invert.enabled",
                "Output: Inverted\n\n" + advancedItemDetectorInvertDescription);
        multilineLang(provider, "cover.advanced_item_detector.invert.disabled",
                "Output: Normal\n\n" + advancedItemDetectorInvertDescription);
        provider.add("cover.advanced_item_detector.max", "Max Items");
        provider.add("cover.advanced_item_detector.min", "Min Items");

        // shutter
        provider.add("cover.shutter.message.enabled", "Closed shutter");
        provider.add("cover.shutter.message.disabled", "Opened shutter");
    }
}
