package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class SimplePatternError extends PatternError {

    public static Codec<SimplePatternError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            Codec.list(Codec.list(ItemStack.CODEC)).fieldOf("candidates").forGetter(PatternError::getCandidates))
            .apply(instance, SimplePatternError::new));
    public static ResourceLocation ID = GTCEu.id("simple_pattern_error");

    public SimplePatternError(BlockPos pos, List<List<ItemStack>> candidates) {
        super(pos, candidates);
    }

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }
}
