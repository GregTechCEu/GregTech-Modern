package com.gregtechceu.gtceu.api.multiblock.error;

import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.MapCodec;

public record PatternErrorType(ResourceLocation id, MapCodec<? extends PatternError> codec) {}
