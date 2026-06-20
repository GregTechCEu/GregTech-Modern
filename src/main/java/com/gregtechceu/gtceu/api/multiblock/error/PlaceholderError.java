package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderError extends PatternError {

    public static Codec<PlaceholderError> CODEC = Codec.unit(() -> Predicates.PLACEHOLDER);

    public static final PatternErrorType TYPE = new PatternErrorType(GTCEu.id("placeholder_error"), CODEC);

    public PlaceholderError(@Nullable BlockPos pos, List<List<BlockInfo>> candidates) {
        super(pos, candidates);
    }

    @Override
    public PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> parent.child(Text.str("Placeholder error").asWidget());
    }

    @Override
    public PatternErrorType type() {
        return TYPE;
    }
}
