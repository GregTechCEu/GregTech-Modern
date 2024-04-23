package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    @Accessor
    double getXLast();
    @Accessor("yLast1")
    double getYLast();
    @Accessor
    double getZLast();
}
