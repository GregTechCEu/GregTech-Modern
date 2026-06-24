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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
//        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
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

    @ModifyArg(method = "setVertex", at = @At(value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;setY(IF)V"),
            index = 1)
    private static float gtceu$invertFluidFlowDirection(float originalY) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
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
    private static Direction gtceu$invertFluidCulling(Direction original) {
        if (original.getAxis() == Direction.Axis.Y && InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
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
    private static ModelQuadFacing gtceu$invertFluidQuadFacing(ModelQuadFacing original) {
        if (original.getAxis() == 1 && InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"),
            require = 0)
    private static BlockPos gtceu$invertFluidLightCheckAbove(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.below();
        } else {
            return original.call(pos);
        }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"),
            require = 0)
    private static BlockPos gtceu$invertFluidLightCheckAbove(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.above();
        } else {
            return original.call(pos);
        }
    }
}
