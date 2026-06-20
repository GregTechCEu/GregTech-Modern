package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Collections;
import java.util.Objects;

public class CoilMatchingError extends PatternError {

    public static Codec<CoilMatchingError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            ICoilType.CODEC.fieldOf("coilType1").forGetter(CoilMatchingError::getCoilType1),
            ICoilType.CODEC.fieldOf("coilType2").forGetter(CoilMatchingError::getCoilType2))
            .apply(instance, CoilMatchingError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("coil_matching_error"), CODEC);

    @Getter
    ICoilType coilType1, coilType2;

    public CoilMatchingError(BlockPos pos, ICoilType type1, ICoilType type2) {
        super(pos, Collections.emptyList());
        coilType1 = type1;
        coilType2 = type2;
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            Objects.requireNonNull(pos);
            Component comp = Component.translatable("gtceu.pattern_error.mismatch_coils",
                    coilType1.getMaterial().getName(), coilType2.getMaterial().getName(),
                    pos.getX(), pos.getY(), pos.getZ());
            parent.child(Text.of(comp).asWidget());
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
