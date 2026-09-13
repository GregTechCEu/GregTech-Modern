package com.gregtechceu.gtceu.api.item.module;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

public record ItemModuleType(Codec<? extends ItemModule> codec, Supplier<? extends ItemModule> defaultInstance) {
}
