package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CoilMatchingError extends MismatchError<ICoilType> {

    public static Codec<CoilMatchingError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            ICoilType.CODEC.fieldOf("coil_type_1").forGetter(CoilMatchingError::getActual),
            ICoilType.CODEC.fieldOf("coil_type_2").forGetter(CoilMatchingError::getExpected))
            .apply(instance, CoilMatchingError::new));

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("coil_matching_error"), CODEC);

    public CoilMatchingError(BlockPos pos, ICoilType expected, ICoilType actual) {
        super(pos, expected, actual);
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> {
            Component comp = Component.translatable("gtceu.pattern_error.mismatch_coils",
                    getExpected().getMaterial().getName(), getActual().getMaterial().getName(),
                    pos.getX(), pos.getY(), pos.getZ());
            parent.child(Text.of(comp).asWidget());
        };
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
