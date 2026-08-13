package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class FilterMatchingError extends MismatchError<CleanroomType> {

    public static Codec<FilterMatchingError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            CleanroomType.CODEC.fieldOf("filter_type_1").forGetter(FilterMatchingError::getExpected),
            CleanroomType.CODEC.fieldOf("filter_type_2").forGetter(FilterMatchingError::getActual))
            .apply(instance, FilterMatchingError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("filter_matching_error"), CODEC);

    public FilterMatchingError(BlockPos pos, CleanroomType expected, CleanroomType actual) {
        super(pos, expected, actual);
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            Component comp = Component.translatable("gtceu.pattern_error.mismatch_filters",
                    getExpected().getName(), getActual().getName(),
                    pos.getX(), pos.getY(), pos.getZ());
            parent.child(Text.of(comp).asWidget());
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
