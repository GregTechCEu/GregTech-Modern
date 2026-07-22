package com.gregtechceu.gtceu.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class MultiblockLang {

    public static void init(RegistrateLangProvider provider) {
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
    }
}
