package com.gregtechceu.gtceu.api.multiblock.error;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderError extends PatternError {

    public static Codec<PlaceholderError> CODEC = Codec.unit(() -> PatternError.PLACEHOLDER);
    public static ResourceLocation ID = GTCEu.id("placeholder_error");

    @Override
    public Codec<? extends PatternError> codec() {
        return CODEC;
    }

    public PlaceholderError(@Nullable BlockPos pos, List<List<ItemStack>> candidates) {
        super(pos, candidates);
    }
}
