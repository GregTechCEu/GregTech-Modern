package com.gregtechceu.gtceu.core.mixins.client.sodium;

import com.gregtechceu.gtceu.client.renderer.fluid.InvertedFluidRenderer;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(value = DefaultFluidRenderer.class, remap = false)
public class SodiumDefaultFluidRendererMixin {

    @Unique
    private boolean gtceu$drawingUpsideDownFluid = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void gtceu$cacheInvertedState(CallbackInfo ci) {
        gtceu$drawingUpsideDownFluid = InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void gtceu$resetInvertedState(CallbackInfo ci) {
        gtceu$drawingUpsideDownFluid = false;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer;setVertex(Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;IFFFFF)V"),
            index = 3)
    private float gtceu$invertFluidFlowDirection(float originalY) {
        if (gtceu$drawingUpsideDownFluid) {
            return 1.0f - originalY;
        } else {
            return originalY;
        }
    }

    @Definition(id = "UP", field = "Lnet/minecraft/core/Direction;UP", remap = true)
    @Definition(id = "DOWN", field = "Lnet/minecraft/core/Direction;DOWN", remap = true)
    @Expression(value = "UP", id = "up")
    @Expression(value = "DOWN", id = "down")
    @ModifyExpressionValue(method = "*", at = {
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "up"),
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "down"),
    }, expect = 9)
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (original.getAxis() == Direction.Axis.Y && gtceu$drawingUpsideDownFluid) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"),
            require = 0)
    private BlockPos gtceu$invertFluidLightCheckAbove(BlockPos pos, Operation<BlockPos> original) {
        if (gtceu$drawingUpsideDownFluid) {
            return pos.below();
        } else {
            return original.call(pos);
        }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"),
            require = 0)
    private BlockPos gtceu$invertFluidLightCheckBelow(BlockPos pos, Operation<BlockPos> original) {
        if (gtceu$drawingUpsideDownFluid) {
            return pos.above();
        } else {
            return original.call(pos);
        }
    }
}
