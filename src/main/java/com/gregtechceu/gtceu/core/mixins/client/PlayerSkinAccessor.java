package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerSkin.class)
public interface PlayerSkinAccessor {

    @Accessor("cape")
    @Mutable
    void gtceu$setCape(ClientAsset.Texture cape);
}
