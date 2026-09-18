package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import lombok.Getter;

public abstract class PatternError {

    public static final Codec<PatternError> CODEC = GTRegistries.PATTERN_ERROR_TYPES.byNameCodec()
            .dispatch(PatternError::type, PatternErrorType::codec);

    @Getter
    protected BlockPos pos;

    public PatternError(BlockPos pos) {
        this.pos = pos;
    }

    protected PatternError() {
        this.pos = BlockPos.ZERO;
    }

    public abstract PatternErrorType type();

    public abstract PatternErrorUI getPatternErrorUIModifier();

    public record PatternErrorType(ResourceLocation id, Codec<? extends PatternError> codec) {}
}
