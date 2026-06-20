package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import net.minecraftforge.fml.ModLoader;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.PATTERN_ERRORS;

public class GTPatternErrors {

    @SuppressWarnings("unchecked")
    public static void init() {
        PATTERN_ERRORS.unfreeze();
        PATTERN_ERRORS.register(PlaceholderError.ID, PlaceholderError.CODEC);
        PATTERN_ERRORS.register(BlockMatchingError.ID, BlockMatchingError.CODEC);
        PATTERN_ERRORS.register(PartAbilityError.ID, PartAbilityError.CODEC);
        PATTERN_ERRORS.register(CoilMatchingError.ID, CoilMatchingError.CODEC);
        PATTERN_ERRORS.register(FilterMatchingError.ID, FilterMatchingError.CODEC);
        PATTERN_ERRORS.register(PatternStringError.ID, PatternStringError.CODEC);
        PATTERN_ERRORS.register(SinglePredicateError.ID, SinglePredicateError.CODEC);
        PATTERN_ERRORS.register(SimplePatternError.ID, SimplePatternError.CODEC);

        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(PATTERN_ERRORS, (Class<Codec<? extends PatternError>>)(Class<?>)Codec.class));

        GTRegistries.COVERS.freeze();
    }
}
