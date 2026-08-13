package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.core.BlockPos;

import lombok.Getter;

public abstract class MismatchError<T> extends PatternError {

    @Getter
    private final T expected;
    @Getter
    private final T actual;

    public MismatchError(BlockPos pos, T expected, T actual) {
        super(pos);
        this.expected = expected;
        this.actual = actual;
    }
}
