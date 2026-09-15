package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class GTPatternErrors {

    private static final DeferredRegister<PatternError.PatternErrorType> PATTERN_ERROR_TYPES = DeferredRegister
            .create(GTRegistries.Keys.PATTERN_ERROR_TYPE, GTCEu.MOD_ID);

    private static void register(PatternError.PatternErrorType patternErrorType) {
        PATTERN_ERROR_TYPES.register(patternErrorType.id().getPath(), () -> patternErrorType);
    }

    public static void init(IEventBus modBus) {
        PATTERN_ERROR_TYPES.register(modBus);

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
