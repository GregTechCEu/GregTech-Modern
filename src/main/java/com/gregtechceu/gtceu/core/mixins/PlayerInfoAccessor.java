package com.gregtechceu.gtceu.core.mixins;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PlayerInfo.class)
public interface PlayerInfoAccessor {

    @Accessor
    Map<MinecraftProfileTexture.Type, ResourceLocation> getTextureLocations();
}
