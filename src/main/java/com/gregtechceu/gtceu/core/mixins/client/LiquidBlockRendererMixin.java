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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {

    @Definition(id = "UP", field = "Lnet/minecraft/core/Direction;UP")
    @Definition(id = "DOWN", field = "Lnet/minecraft/core/Direction;DOWN")
    @Expression(value = "DOWN", id = "down")
    @Expression(value = "UP", id = "up")
    @ModifyExpressionValue(method = "tesselate", at = {
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "up"),
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "down"),
    })
    private static Direction gtceu$invertFluidCulling(Direction original) {
        if (original.getAxis() == Direction.Axis.Y && InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @WrapOperation(method = { "tesselate", "getLightColor" }, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos gtceu$invertFluidLightCheckAbove(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.below();
        } else {
            return original.call(pos);
        }
    }

    @WrapOperation(method = "tesselate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"))
    private static BlockPos gtceu$invertFluidLightCheckBelow(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.above();
        } else {
            return original.call(pos);
        }
    }
}
