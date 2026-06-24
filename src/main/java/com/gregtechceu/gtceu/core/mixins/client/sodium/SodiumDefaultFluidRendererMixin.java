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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(value = DefaultFluidRenderer.class, remap = false)
public class SodiumDefaultFluidRendererMixin {

// this probably isn't required; I don't see anything actually use the light face.
//    @Inject(method = "updateQuad", at = @At(value = "HEAD"))
//    private static void gtceu$invertQuadLightFace(ModelQuadViewMutable quad, LevelSlice level, BlockPos pos,
//                                                       LightPipeline lighter, Direction dir, ModelQuadFacing facing,
//                                                       float brightness, ColorProvider<FluidState> colorProvider,
//                                                       FluidState fluidState,
//                                                       CallbackInfo ci) {
//        if (gtceu$drawingUpsideDownFluid) {
//            // flip the light face (default is UP)
//            quad.setLightFace(Direction.DOWN);
//        }
//    }
//
//    @Inject(method = "writeQuad", at = @At(value = "RETURN"))
//    private static void gtceu$resetQuadLightFace(ChunkModelBuilder builder, TranslucentGeometryCollector collector,
//                                                 Material material, BlockPos offset, ModelQuadView quad,
//                                                 ModelQuadFacing facing, boolean flip,
//                                                 CallbackInfo ci) {
//        // reset the light face after every write
//        quad.setLightFace(Direction.UP);
//    }

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
            index = 4)
    private float gtceu$invertFluidFlowDirection(float originalY) {
        if (gtceu$drawingUpsideDownFluid) {
            return 1.0f - originalY;
        } else {
            return originalY;
        }
    }

    @ModifyVariable(method = "writeQuad", at = @At(value = "HEAD"), index = 7, argsOnly = true)
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
    }, expect = 9)
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (original.getAxis() == Direction.Axis.Y && gtceu$drawingUpsideDownFluid) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @Definition(id = "POS_Y", field = "Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;POS_Y")
    @Definition(id = "NEG_Y", field = "Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;NEG_Y")
    @Expression(value = "POS_Y", id = "pos_y")
    @Expression(value = "NEG_Y", id = "neg_y")
    @ModifyExpressionValue(method = "*", at = {
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "pos_y"),
            @At(value = "MIXINEXTRAS:EXPRESSION", id = "neg_y")
    }, expect = 5)
    private ModelQuadFacing gtceu$invertFluidQuadFacing(ModelQuadFacing original) {
        if (original.getAxis() == 1 && gtceu$drawingUpsideDownFluid) {
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
