package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.PATTERN_ERRORS;

public class GTPatternErrors {

    public static void register(PatternError.PatternErrorType patternErrorType) {
        GTRegistries.register(PATTERN_ERRORS, patternErrorType.id(), patternErrorType);
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
