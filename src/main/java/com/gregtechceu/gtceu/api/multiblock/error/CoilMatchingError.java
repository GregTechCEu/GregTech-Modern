package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;

public class CoilMatchingError extends MismatchError<ICoilType> {

    public static Codec<CoilMatchingError> CODEC = makeCodec(ICoilType.CODEC, CoilMatchingError::new);

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
