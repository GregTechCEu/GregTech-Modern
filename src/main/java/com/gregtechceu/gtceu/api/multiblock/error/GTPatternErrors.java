package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class GTPatternErrors {

    public static void register(PatternError.PatternErrorType patternErrorType) {
        GTRegistries.register(GTRegistries.PATTERN_ERROR_TYPES, patternErrorType.id(), patternErrorType);
    }

    public static void init() {
        register(PlaceholderError.TYPE);
        register(BlockMatchingError.TYPE);
        register(PartAbilityError.TYPE);
        register(CoilMatchingError.TYPE);
        register(FilterMatchingError.TYPE);
        register(PatternStringError.TYPE);
        register(SinglePredicateError.TYPE);
        register(SimplePatternError.TYPE);
    }
}
