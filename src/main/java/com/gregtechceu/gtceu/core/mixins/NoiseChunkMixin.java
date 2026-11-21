package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.level.levelgen.NoiseChunk;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin {

    @ModifyExpressionValue(method = "<init>",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;oreVeinsEnabled()Z"))
    private static boolean gtceu$disableVanillaLargeOreVeinsIfConfigEnabled(boolean original) {
        return original && !ConfigHolder.getInstance().worldgen.oreVeins.removeVanillaLargeOreVeins;
    }
}
