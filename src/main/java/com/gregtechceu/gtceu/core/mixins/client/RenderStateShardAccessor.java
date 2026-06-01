package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.renderer.RenderStateShard;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {

    @Accessor("name")
    String getName();
}
