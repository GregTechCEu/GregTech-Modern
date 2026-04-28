package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {

    @Invoker
    void callFlushIfUnmanaged();
}
