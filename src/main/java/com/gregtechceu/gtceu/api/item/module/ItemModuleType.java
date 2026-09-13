package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;

import java.util.function.Function;

public record ItemModuleType<T extends ItemModule>(Codec<T> codec, Function<ItemStack, T> defaultInstance) {}
