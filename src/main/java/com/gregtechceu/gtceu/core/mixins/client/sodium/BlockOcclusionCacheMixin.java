package com.gregtechceu.gtceu.core.mixins.client.sodium;

import com.gregtechceu.gtceu.core.util.extensions.BlockOcclusionCacheAccess;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.Direction;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockOcclusionCache.class, remap = false)
public class BlockOcclusionCacheMixin implements BlockOcclusionCacheAccess {

    @Unique
    boolean gtceu$drawingUpsideDownFluid = false;

    @ModifyExpressionValue(method = "shouldDrawFullBlockFluidSide",
            at = @At(value = "FIELD", target = "Lnet/minecraft/core/Direction;UP:Lnet/minecraft/core/Direction;",
                    opcode = Opcodes.GETSTATIC, remap = true))
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (gtceu$drawingUpsideDownFluid) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @Override
    public void gtceu$drawingUpsideDownFluid(boolean drawingUpsideDownFluid) {
        this.gtceu$drawingUpsideDownFluid = drawingUpsideDownFluid;
    }
}
