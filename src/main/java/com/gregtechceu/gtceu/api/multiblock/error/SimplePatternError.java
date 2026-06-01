package com.gregtechceu.gtceu.api.multiblock.error;

import brachy.modularui.api.drawable.Text;
import com.gregtechceu.gtceu.GTCEu;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class SimplePatternError extends PatternError {

    public static Codec<SimplePatternError> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(PatternError::getPos),
            Codec.list(Codec.list(ItemStack.CODEC)).fieldOf("candidates").forGetter(PatternError::getCandidates)
            )
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
