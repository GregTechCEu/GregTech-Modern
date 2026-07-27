package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;

public class MultiblockLang {

    public static void init(GTLangProvider provider) {
        provider.add("gtceu.pattern_error.mismatch_coils", "Mismatched coils: %s vs %s at (%d, %d, %d)");
        provider.add("gtceu.pattern_error.mismatch_filters", "Mismatched filters: %s vs %s at (%d, %d, %d)");
        provider.add("gtceu.predicate_error.active_transformer.missing_io", "Missing hatches: IN - %s | OUT - %s");
        provider.add("gtceu.predicate_error.cleanroom.too_small", "Cleanroom must be at least 5x5x4.");
        provider.add("gtceu.predicate_error.cleanroom.not_centered", "Cleanroom controller must be centered.");
        provider.add("gtceu.predicate_error.distillery.unexpected_hatch",
                "Fluid hatch at unexpected position (%d, %d, %d).");
        provider.add("gtceu.predicate_error.distillery.missing_outputs", "Missing fluid output hatches.");
        provider.add("gtceu.predicate_error.power_substation.missing_batteries", "Missing batteries.");
        provider.add("gtceu.predicate_error.databank.missing_maintenance", "Missing maintenance hatch.");
        provider.add("gtceu.predicate_error.object_holder.direction",
                "Object holder must face opposite the controller.");
        provider.add("gtceu.predicate_error.research.missing_computation", "Missing computation hatch.");
        provider.add("gtceu.predicate_error.research.missing_object_holder", "Missing object holder hatch.");
        provider.add("gtceu.predicate_error.charcoal.walls", "Incorrect wall block at (%d, %d, %d).");
        provider.add("gtceu.predicate_error.charcoal.logs", "Must be completely filled with logs.");
        provider.add("gtceu.predicate_error.steam.missing_steam_hatch", "Missing steam hatch");
        provider.add("gtceu.pattern_predicate.blocks", "Error at X: %d, Y: %d, Z: %d");

        // Multiblock Preview
        provider.add("gtceu.multiblock.preview.zoom", "Use mousewheel or right-click + drag to zoom");
        provider.add("gtceu.multiblock.preview.rotate", "Click and drag to rotate");
        provider.add("gtceu.multiblock.preview.select", "Right-click to check candidates");
        provider.add("gtceu.multiblock.pattern.error", "Expected components (%s) at (%s).");
        provider.add("gtceu.multiblock.pattern.error.limited_exact", "§cExactly: %d§r");
        provider.add("gtceu.multiblock.pattern.error.limited_within", "§cBetween %d and %d§r");
        provider.addMultiLang("gtceu.multiblock.pattern.error.limited", "§cMaximum: %d§r", "§cMinimum: %d§r",
                "§cMaximum: %d per layer§r", "§cMinimum: %d per layer§r");
        provider.add("gtceu.multiblock.pattern.error.coils", "§cAll heating coils must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.filters", "§cAll filters must be the same§r");
        provider.add("gtceu.multiblock.pattern.error.batteries", "§cAll batteries must be the same§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_1", "§6Must have a clear 1x1x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.clear_amount_3", "§6Must have a clear 3x3x1 space in front§r");
        provider.add("gtceu.multiblock.pattern.single", "§6Only this block can be used§r");
        provider.add("gtceu.multiblock.pattern.location_end", "§cVery End§r");
        provider.add("gtceu.multiblock.pattern.replaceable_air", "Replaceable by Air");
    }
}
