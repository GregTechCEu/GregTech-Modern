package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

public class GTPatternErrors {

    private static final DeferredRegister<PatternErrorType> PATTERN_ERROR_TYPES = DeferredRegister
            .create(GTRegistries.Keys.PATTERN_ERROR_TYPE, GTCEu.MOD_ID);

    // spotless:off
    public static DeferredHolder<PatternErrorType, PatternErrorType> BLOCK_MATCHING_ERROR = register(GTCEu.id("block_matching_error"), BlockMatchingError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> COIL_MATCHING_ERROR = register(GTCEu.id("coil_matching_error"), CoilMatchingError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> FILTER_MATCHING_ERROR = register(GTCEu.id("filter_matching_error"), FilterMatchingError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> PART_ABILITY_ERROR = register(GTCEu.id("part_ability_error"), PartAbilityError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> PLACEHOLDER_ERROR = register(GTCEu.id("placeholder_error"), PlaceholderError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> PATTERN_STRING_ERROR = register(GTCEu.id("pattern_string_error"), PatternStringError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> SIMPLE_PATTERN_ERROR = register(GTCEu.id("simple_pattern_error"), SimplePatternError.CODEC);
    public static DeferredHolder<PatternErrorType, PatternErrorType> SINGLE_PREDICATE_ERROR = register(GTCEu.id("single_predicate_error"), SinglePredicateError.CODEC);
    //spotless:on

    private static DeferredHolder<PatternErrorType, PatternErrorType> register(ResourceLocation id,
                                                                               MapCodec<? extends PatternError> codec) {
        return PATTERN_ERROR_TYPES.register(id.getPath(), () -> new PatternErrorType(id, codec));
    }

    public static void init(IEventBus modBus) {
        PATTERN_ERROR_TYPES.register(modBus);
    }
}
