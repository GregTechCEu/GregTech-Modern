package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class PatternError {

    public static final Codec<PatternError> CODEC = GTRegistries.PATTERN_ERRORS.codec()
            .dispatch(PatternError::type, PatternErrorType::codec);

    @Getter
    @Setter
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
