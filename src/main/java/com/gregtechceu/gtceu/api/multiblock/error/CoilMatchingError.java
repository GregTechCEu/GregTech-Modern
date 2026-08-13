package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.Codec;

public class CoilMatchingError extends MismatchError<ICoilType> {

    public static final Codec<CoilMatchingError> CODEC = makeCodec(ICoilType.CODEC, CoilMatchingError::new);

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("coil_matching_error"), CODEC);

    public CoilMatchingError(BlockPos pos, ICoilType expected, ICoilType actual) {
        super(pos, expected, actual);
    }

    @Override
    protected String stringify(ICoilType value) {
        return value.getMaterial().getName();
    }

    @Override
    protected String lang() {
        return "gtceu.pattern_error.mismatch_coils";
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
