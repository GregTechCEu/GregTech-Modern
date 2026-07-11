package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collections;
import java.util.Objects;

public class FilterMatchingError extends PatternError {

    public static MapCodec<FilterMatchingError> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            CleanroomType.CODEC.fieldOf("coilType1").forGetter(FilterMatchingError::getFilterType1),
            CleanroomType.CODEC.fieldOf("coilType2").forGetter(FilterMatchingError::getFilterType2))
            .apply(instance, FilterMatchingError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("filter_matching_error"), CODEC);

    @Getter
    CleanroomType filterType1, filterType2;

    public FilterMatchingError(BlockPos pos, CleanroomType type1, CleanroomType type2) {
        super(pos, Collections.emptyList());
        this.filterType1 = type1;
        this.filterType2 = type2;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            Objects.requireNonNull(pos);
            Component comp = Component.translatable("gtceu.pattern_error.mismatch_filters",
                    filterType1.getName(), filterType2.getName(),
                    pos.getX(), pos.getY(), pos.getZ());
            parent.child(Text.of(comp).asWidget());
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
