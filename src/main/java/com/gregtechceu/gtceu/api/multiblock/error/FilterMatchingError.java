package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import net.minecraft.core.BlockPos;

import com.mojang.serialization.Codec;

public class FilterMatchingError extends MismatchError<CleanroomType> {

    public static final Codec<FilterMatchingError> CODEC = makeCodec(CleanroomType.CODEC, FilterMatchingError::new);

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("filter_matching_error"), CODEC);

    public FilterMatchingError(BlockPos pos, CleanroomType expected, CleanroomType actual) {
        super(pos, expected, actual);
        valueToString(CleanroomType::getName);
    }

    @Override
    protected String langKey() {
        return "gtceu.pattern_error.mismatch_filters";
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
