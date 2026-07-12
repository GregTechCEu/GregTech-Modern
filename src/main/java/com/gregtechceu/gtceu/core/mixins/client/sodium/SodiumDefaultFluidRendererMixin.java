package com.gregtechceu.gtceu.core.mixins.client.sodium;

import com.gregtechceu.gtceu.client.renderer.fluid.InvertedFluidRenderer;

import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
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

    @Inject(method = "render", at = @At("RETURN"), expect = -1)
    private void gtceu$resetInvertedState(CallbackInfo ci) {
        gtceu$drawingUpsideDownFluid = false;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer;setVertex(Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;IFFFFF)V"),
            index = 3, expect = 16)
    private float gtceu$invertFluidFlowDirection(float originalY) {
        if (gtceu$drawingUpsideDownFluid) {
            return 1.0f - originalY;
        } else {
            return originalY;
        }
    }

    @Definition(id = "shouldRenderBackwardUpFace", method = "Lnet/minecraft/world/level/material/FluidState;shouldRenderBackwardUpFace(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z")
    @Definition(id = "scratchPos", field = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer;scratchPos:Lnet/minecraft/core/BlockPos$MutableBlockPos;")
    @Definition(id = "set", method = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;")
    @Expression("?.shouldRenderBackwardUpFace(?, @(this.scratchPos.set(?, ? + 1, ?)))")
    @ModifyArg(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"), index = 2)
    private int gtceu$invertFluidBackwardUpFaceCheckDirection(int posY) {
        if (gtceu$drawingUpsideDownFluid) {
            // `posY - 2` because the original function already did `posY + 1`
            return posY - 2;
        } else {
            return posY;
        }
    }

    @Definition(id = "UP", field = "Lnet/minecraft/core/Direction;UP:Lnet/minecraft/core/Direction;", remap = true)
    @Definition(id = "DOWN", field = "Lnet/minecraft/core/Direction;DOWN:Lnet/minecraft/core/Direction;", remap = true)
    @Expression({ "UP", "DOWN" })
    @ModifyExpressionValue(method = { "render", "fluidCornerHeight", "isSideExposed" },
            at = @At("MIXINEXTRAS:EXPRESSION"), expect = 9)
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (gtceu$drawingUpsideDownFluid) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @Definition(id = "POS_Y", field = "Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;POS_Y")
    @Definition(id = "NEG_Y", field = "Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;NEG_Y")
    @Expression({ "POS_Y", "NEG_Y" })
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"), expect = 5)
    private ModelQuadFacing gtceu$invertFluidFaceOrientation(ModelQuadFacing original) {
        if (gtceu$drawingUpsideDownFluid) {
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
