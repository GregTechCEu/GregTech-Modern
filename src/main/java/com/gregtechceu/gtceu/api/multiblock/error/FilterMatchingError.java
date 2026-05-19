package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.block.IFilterType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class FilterMatchingError extends PatternError {

    IFilterType type1, type2;

    public FilterMatchingError(BlockPos pos, IFilterType type1, IFilterType type2) {
        super(pos, Collections.emptyList());
        this.type1 = type1;
        this.type2 = type2;
    }

    @Override
    public List<Component> getErrorInfo() {
        return Collections.singletonList(Component.translatable("gtceu.pattern_error.mismatch_coils", type1.getCleanroomType().getName(), type2.getCleanroomType().getName(),
                pos.getX(), pos.getY(), pos.getZ()));
    }
}
