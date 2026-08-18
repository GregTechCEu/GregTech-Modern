package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraftforge.fml.ModLoader;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.PATTERN_ERROR_TYPES;

public class GTPatternErrors {

    public static void register(PatternError.PatternErrorType patternErrorType) {
        GTRegistries.register(PATTERN_ERROR_TYPES, patternErrorType.id(), patternErrorType);
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
