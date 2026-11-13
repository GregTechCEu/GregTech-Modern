package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public abstract class PatternError {

    public static final Codec<PatternError> CODEC = GTRegistries.PATTERN_ERROR_TYPES.byNameCodec()
            .dispatch(PatternError::type, PatternErrorType::codec);

    public static final BlockPos CONTROLLER = BlockPos.of(0x112233445566788L);

    @Getter
    @Setter
    protected BlockPos pos;

    public PatternError(BlockPos pos) {
        this.pos = pos;
    }

    protected PatternError() {
        this.pos = CONTROLLER;
    }

    public abstract PatternErrorType type();

    public abstract PatternErrorUI getPatternErrorUIModifier();
}
