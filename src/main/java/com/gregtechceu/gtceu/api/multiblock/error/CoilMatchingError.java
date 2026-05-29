package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.block.ICoilType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CoilMatchingError extends PatternError {

    ICoilType coilType1, coilType2;

    public CoilMatchingError(BlockPos pos, ICoilType type1, ICoilType type2) {
        super(pos, Collections.emptyList());
        coilType1 = type1;
        coilType2 = type2;
    }

    @Override
    public List<Component> getErrorInfo() {
        Objects.requireNonNull(pos);
        return Collections.singletonList(Component.translatable("gtceu.pattern_error.mismatch_coils",
                coilType1.getMaterial().getName(), coilType2.getMaterial().getName(),
                pos.getX(), pos.getY(), pos.getZ()));
    }
}
