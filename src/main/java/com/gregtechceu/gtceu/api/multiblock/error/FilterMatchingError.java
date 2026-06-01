package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.api.block.IFilterType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;

import java.util.Collections;
import java.util.Objects;

public class FilterMatchingError extends PatternError {

    IFilterType type1, type2;

    public FilterMatchingError(BlockPos pos, IFilterType type1, IFilterType type2) {
        super(pos, Collections.emptyList());
        this.type1 = type1;
        this.type2 = type2;
    }

    @Override
    public PatternErrorUI applyErrorInformation() {
        return (parent) -> {
            Objects.requireNonNull(pos);
            Component comp = Component.translatable("gtceu.pattern_error.mismatch_filters",
                    type1.getCleanroomType().getName(), type2.getCleanroomType().getName(),
                    pos.getX(), pos.getY(), pos.getZ());
            parent.child(Text.of(comp).asWidget());
        };
    }
}
