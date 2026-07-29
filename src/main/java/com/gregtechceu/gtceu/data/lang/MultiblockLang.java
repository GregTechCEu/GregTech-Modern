package com.gregtechceu.gtceu.data.lang;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTLangProvider;
import com.tterrag.registrate.providers.RegistrateLangProvider;

public class MultiblockLang {

    public static void init(GTLangProvider provider) {

        provider.addMultiline("multiblock.gtceu.dimensions", "Dimensions: \n" +
                "  §c§lWidth§r: %s, §a§lHeight§r: %s, §9§lDepth§r: %s ");
        
        provider.add("multiblock.gtceu.part_sharing.disabled", "Multiblock Sharing §4Disabled");
        provider.add("multiblock.gtceu.part_sharing.enabled", "Multiblock Sharing §aEnabled");
        
        // Pattern/Predicate Info

        provider.add("multiblock.gtceu.predicate.count.exact", "§cExactly: %d§r");
        provider.add("multiblock.gtceu.predicate.count.range", "§cBetween %d and %d§r");
        provider.addMultiLang("multiblock.gtceu.predicate.count.limit", "§cMaximum: %d§r", "§cMinimum: %d§r",
                "§cMaximum: %d per layer§r", "§cMinimum: %d per layer§r");

        provider.add("multiblock.gtceu.pattern.clear_amount_1", "§6Must have a clear 1x1x1 space in front§r");
        provider.add("multiblock.gtceu.pattern.clear_amount_3", "§6Must have a clear 3x3x1 space in front§r");
        provider.add("multiblock.gtceu.pattern.location_end", "§cVery End§r");
        provider.add("multiblock.gtceu.pattern.replaceable_air", "Replaceable by Air");
        provider.add("multiblock.gtceu.predicate.single", "§6Only this block can be used§r");

        // Pattern Errors

        provider.add("multiblock.gtceu.pattern_error.block_match_error", "Error at X: %d, Y: %d, Z: %d");
        provider.add("multiblock.gtceu.pattern_error.mismatch_coils", "Mismatched coils: %s vs %s at (%d, %d, %d)");
        provider.add("multiblock.gtceu.pattern_error.mismatch_filters", "Mismatched filters: %s vs %s at (%d, %d, %d)");

        
        provider.add("multiblock.gtceu.pattern_error.active_transformer.missing_io", "Missing hatches: IN - %s | OUT - %s");
        provider.add("multiblock.gtceu.pattern_error.cleanroom.too_small", "Cleanroom must be at least 5x5x4.");
        provider.add("multiblock.gtceu.pattern_error.cleanroom.not_centered", "Cleanroom controller must be centered.");
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
        provider.add("multiblock.gtceu.pattern_error.steam.missing_steam_hatch", "Missing steam hatch");
    }
}
