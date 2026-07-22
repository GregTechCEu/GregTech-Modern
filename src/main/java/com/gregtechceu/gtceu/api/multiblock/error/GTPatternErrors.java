package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.GTCEuAPI;

import net.minecraftforge.fml.ModLoader;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.PATTERN_ERRORS;

public class GTPatternErrors {

    public static void register(PatternError.PatternErrorType patternErrorType) {
        PATTERN_ERRORS.register(patternErrorType.id(), patternErrorType);
    }

    public static void init() {
        PATTERN_ERRORS.unfreeze();
        register(PlaceholderError.TYPE);
        register(BlockMatchingError.TYPE);
        register(PartAbilityError.TYPE);
        register(CoilMatchingError.TYPE);
        register(FilterMatchingError.TYPE);
        register(PatternStringError.TYPE);
        register(SinglePredicateError.TYPE);
        register(SimplePatternError.TYPE);

        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(PATTERN_ERRORS, PatternError.PatternErrorType.class));

        PATTERN_ERRORS.freeze();
    }
}
