package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class GTPatternErrors {

    private static final DeferredRegister<PatternError.PatternErrorType> PATTERN_ERRORS = DeferredRegister
            .create(GTRegistries.Keys.PATTERN_ERROR_TYPE, "gtceu");

    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> PLACEHOLDER_ERROR = register(
            PlaceholderError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> BLOCK_MATCHING_ERROR = register(
            BlockMatchingError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> PART_ABILITY_ERROR = register(
            PartAbilityError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> COIL_MATCHING_ERROR = register(
            CoilMatchingError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> FILTER_MATCHING_ERROR = register(
            FilterMatchingError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> PATTERN_STRING_ERROR = register(
            PatternStringError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> SINGLE_PREDICATE_ERROR = register(
            SinglePredicateError.TYPE);
    public static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> SIMPLE_PATTERN_ERROR = register(
            SimplePatternError.TYPE);

    private static DeferredHolder<PatternError.PatternErrorType, PatternError.PatternErrorType> register(PatternError.PatternErrorType patternErrorType) {
        return PATTERN_ERRORS.register(patternErrorType.id().getPath(), () -> patternErrorType);
    }

    public static void init(IEventBus bus) {
        PATTERN_ERRORS.register(bus);
    }
}
