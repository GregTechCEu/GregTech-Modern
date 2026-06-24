package com.gregtechceu.gtceu.core.mixins.client.embeddium;

import com.gregtechceu.gtceu.client.renderer.fluid.InvertedFluidRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.embeddedt.embeddium.impl.model.quad.properties.ModelQuadFacing;
import org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidRenderer.class, remap = false)
public class EmbeddiumFluidRendererMixin {

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
            target = "Lorg/embeddedt/embeddium/impl/render/chunk/compile/pipeline/FluidRenderer;setVertex(Lorg/embeddedt/embeddium/impl/model/quad/ModelQuadViewMutable;IFFFFF)V"),
            index = 4)
    private float gtceu$invertFluidFlowDirection(float originalY) {
        if (gtceu$drawingUpsideDownFluid) {
            return 1.0f - originalY;
        } else {
            return originalY;
        }
    }

    @ModifyVariable(method = "writeQuad", at = @At(value = "HEAD"), index = 6, argsOnly = true)
    private boolean gtceu$invertFluidVertexOrder(boolean flip) {
        // reverse vertex order if we're drawing an upside-down fluid
        if (gtceu$drawingUpsideDownFluid) {
            return !flip;
        } else {
            return flip;
        }
    }

    @Definition(id = "UP", field = "Lnet/minecraft/core/Direction;UP", remap = true)
    @Definition(id = "DOWN", field = "Lnet/minecraft/core/Direction;DOWN", remap = true)
    @Expression(value = "UP", id = "up")
    @Expression(value = "DOWN", id = "down")
    @ModifyExpressionValue(method = "*", at = {
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "up"),
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "down"),
    }, expect = 10)
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (original.getAxis() == Direction.Axis.Y && gtceu$drawingUpsideDownFluid) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @Definition(id = "POS_Y", field = "Lorg/embeddedt/embeddium/impl/model/quad/properties/ModelQuadFacing;POS_Y")
    @Definition(id = "NEG_Y", field = "Lorg/embeddedt/embeddium/impl/model/quad/properties/ModelQuadFacing;NEG_Y")
    @Expression(value = "POS_Y", id = "pos_y")
    @Expression(value = "NEG_Y", id = "neg_y")
    @ModifyExpressionValue(method = "*", at = {
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "pos_y"),
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "neg_y")
    }, expect = 3)
    private ModelQuadFacing gtceu$invertFluidQuadFacing(ModelQuadFacing original) {
        if ((original == ModelQuadFacing.POS_Y || original == ModelQuadFacing.NEG_Y) &&
                gtceu$drawingUpsideDownFluid) {
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
