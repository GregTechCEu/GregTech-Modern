package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.Text;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderError extends PatternError {

    public static Codec<PlaceholderError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            Codec.list(Codec.list(BlockInfo.CODEC)).fieldOf("candidates").forGetter(PatternError::getCandidates))
            .apply(instance, PlaceholderError::new));

    public static ResourceLocation ID = GTCEu.id("placeholder_error");

    public PlaceholderError(@Nullable BlockPos pos, List<List<BlockInfo>> candidates) {
        super(pos, candidates);
    }

    @Override
    public @NotNull PatternErrorUI getPatternErrorUIModifier() {
        return (parent) -> { parent.child(Text.str("Placeholder error").asWidget()); };
    }

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }
}
