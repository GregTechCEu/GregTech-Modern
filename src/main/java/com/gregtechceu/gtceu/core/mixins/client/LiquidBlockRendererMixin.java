package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.client.renderer.fluid.InvertedFluidRenderer;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = LiquidBlockRenderer.class, remap = false)
public class LiquidBlockRendererMixin {

    @Definition(id = "UP", field = "Lnet/minecraft/core/Direction;UP")
    @Definition(id = "DOWN", field = "Lnet/minecraft/core/Direction;DOWN")
    @Expression({ "UP", "DOWN" })
    @ModifyExpressionValue(method = "tesselate", at = @At("MIXINEXTRAS:EXPRESSION"), require = 8)
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @WrapOperation(method = { "tesselate", "getHeight*", "getLightColor" },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"),
            require = 3)
    private BlockPos gtceu$invertFluidLightCheckAbove(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.below();
        } else {
            return original.call(pos);
        }
    }

    @WrapOperation(method = "tesselate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"))
    private BlockPos gtceu$invertFluidLightCheckBelow(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.above();
        } else {
            return original.call(pos);
        }
    }
}
