package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;

public class MultiblockLang {

    public static void init(GTLangProvider provider) {
        provider.addMultiline("multiblock.gtceu.dimensions", "Dimensions: \n" +
                "  §c§lWidth§r: %s, §a§lHeight§r: %s, §9§lDepth§r: %s ");

        provider.add("multiblock.gtceu.part_sharing.disabled", "Multiblock Sharing §4Disabled");
        provider.add("multiblock.gtceu.part_sharing.enabled", "Multiblock Sharing §aEnabled");

        // Pattern/Predicate Info

        provider.add("multiblock.gtceu.predicate.count.exact", "Exactly: %d");
        provider.add("multiblock.gtceu.predicate.count.range", "Between %d and %d");
        provider.add("multiblock.gtceu.predicate.limit_max", "Maximum: %d");
        provider.add("multiblock.gtceu.predicate.limit_min", "Minimum: %d");
        provider.add("multiblock.gtceu.predicate.limit_layer_max", "Maximum: %d per layer");
        provider.add("multiblock.gtceu.predicate.limit_layer_min", "Minimum: %d per layer");
        provider.add("multiblock.gtceu.predicate.single", "Only this block can be used");
        provider.add("multiblock.gtceu.predicate.tier_must_match", "Can only be of tier: %s");

        provider.add("multiblock.gtceu.pattern.clear_amount", "§6Must have a clear %dx%dx%d space in front§r");
        provider.add("multiblock.gtceu.pattern.location_end", "§cVery End§r");
        provider.add("multiblock.gtceu.predicate.replaceable_air", "Replaceable by Air");

        provider.add("multiblock.gtceu.pattern.coils_must_match", "All heating coils must be the same");
        provider.add("multiblock.gtceu.pattern.filters_must_match", "All filters must be the same");

        // Pattern Errors

        provider.add("multiblock.gtceu.pattern_error.block_match_error", "Error at X: %d, Y: %d, Z: %d");
        provider.add("multiblock.gtceu.pattern_error.mismatch_coils", "Mismatched coils: %s vs %s at (%d, %d, %d)");
        provider.add("multiblock.gtceu.pattern_error.mismatch_filters", "Mismatched filters: %s vs %s at (%d, %d, %d)");

        provider.add("multiblock.gtceu.pattern_error.active_transformer.missing_io",
                "Missing hatches: IN - %s | OUT - %s");
        provider.add("multiblock.gtceu.pattern_error.cleanroom.too_small", "Cleanroom must be at least 5x5x4.");
        provider.add(
                "multiblock.gtceu.pattern_error.cleanroom.not_centered",
                "Cleanroom controller must be centered.");
        provider.add("multiblock.gtceu.pattern_error.distillery.unexpected_hatch",
                "Fluid hatch at unexpected position (%d, %d, %d).");
        provider.add("multiblock.gtceu.pattern_error.distillery.missing_outputs", "Missing fluid output hatches.");
        provider.add("multiblock.gtceu.pattern_error.power_substation.missing_batteries", "Missing batteries.");
        provider.add("multiblock.gtceu.pattern_error.databank.missing_maintenance", "Missing maintenance hatch.");
        provider.add("multiblock.gtceu.pattern_error.object_holder.direction",
                "Object holder must face opposite the controller.");
        provider.add("multiblock.gtceu.pattern_error.research.missing_computation", "Missing computation hatch.");
        provider.add("multiblock.gtceu.pattern_error.research.missing_object_holder", "Missing object holder hatch.");
        provider.add("multiblock.gtceu.pattern_error.charcoal.walls", "Incorrect wall block at (%d, %d, %d).");
        provider.add("multiblock.gtceu.pattern_error.charcoal.logs", "Must be completely filled with logs.");
        provider.add("multiblock.gtceu.pattern_error.charcoal.too_small", "Charcoal Pile is too small.");
        provider.add("multiblock.gtceu.pattern_error.steam.missing_steam_hatch", "Missing steam hatch");

        // Autobuilding, Multiblock Preview & Terminal

        provider.add("gui.gtceu.multi_preview.clear_preferences", "Clear Preferences");
        provider.add("gui.gtceu.multi_preview.clear_multiblock", "Clear Multiblock");

        provider.add("gui.gtceu.terminal.bound_to_controller", "Bound to multiblock controller at (%s, %s, %s).");
        provider.add("gui.gtceu.terminal.bound_to_me_network", "Bound to ME network. (%s, %s, %s, %s)");
        provider.add("gui.gtceu.terminal.not_bound_to_me_network", "Terminal is not bound to ME network.");

        provider.add("gui.gtceu.terminal.me_network_invalid", "Cannot access ME network.");

        provider.add("gui.gtceu.autobuild.ae_blocks", "The following blocks are in AE:");
        provider.add("gui.gtceu.autobuild.placed_blocks", "The following blocks were placed:");
        provider.add("gui.gtceu.autobuild.missing_blocks", "The following blocks are missing:");
        provider.add("gui.gtceu.autobuild.unplaced_blocks",
                "Could not place some blocks. Obstructions are highlighted in-world.");
    }
}
