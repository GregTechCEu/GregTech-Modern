package com.gregtechceu.gtceu.core;

import com.mojang.blaze3d.pipeline.RenderTarget;

import java.util.function.IntSupplier;

public interface PostPassExt {

    default void gtceu$copyDepthFrom(RenderTarget source) {}
}
