package com.gregtechceu.gtceu.api.multiblock.error;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.PATTERN_ERRORS;

public class GTPatternErrors {

    public static void init() {
        PATTERN_ERRORS.unfreeze();
        PATTERN_ERRORS.register(PlaceholderError.ID, PlaceholderError.CODEC);
        PATTERN_ERRORS.register(BlockMatchingError.ID, BlockMatchingError.CODEC);
        PATTERN_ERRORS.register(CoilMatchingError.ID, CoilMatchingError.CODEC);
        PATTERN_ERRORS.register(FilterMatchingError.ID, FilterMatchingError.CODEC);
        PATTERN_ERRORS.register(PatternStringError.ID, PatternStringError.CODEC);
        PATTERN_ERRORS.register(SinglePredicateError.ID, SinglePredicateError.CODEC);
        PATTERN_ERRORS.register(SimplePatternError.ID, SimplePatternError.CODEC);
    }
}
