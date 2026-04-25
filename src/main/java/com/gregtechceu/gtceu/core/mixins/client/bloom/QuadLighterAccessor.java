package com.gregtechceu.gtceu.core.mixins.client.bloom;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.lighting.QuadLighter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(QuadLighter.class)
public interface QuadLighterAccessor {

    @Invoker
    int callCalculateLightmap(float[] position, byte[] normal);

    @Accessor
    BlockAndTintGetter getLevel();
}
