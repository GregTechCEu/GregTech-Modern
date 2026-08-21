package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.block.ICoilType;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.MapCodec;

public class CoilMatchingError extends MismatchError<ICoilType> {

    public static final MapCodec<CoilMatchingError> CODEC = makeCodec(ICoilType.CODEC, CoilMatchingError::new);

    public CoilMatchingError(BlockPos pos, ICoilType expected, ICoilType actual) {
        super(pos, expected, actual);
        valueToString(ICoilType::getName);
    }

    @Override
    protected String langKey() {
        return "gtceu.pattern_error.mismatch_coils";
    }

    @Override
    public PatternErrorType type() {
        return GTPatternErrors.COIL_MATCHING_ERROR.value();
    }
}
