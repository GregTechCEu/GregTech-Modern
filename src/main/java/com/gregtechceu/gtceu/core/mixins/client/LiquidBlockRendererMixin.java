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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = LiquidBlockRenderer.class, remap = false)
public class LiquidBlockRendererMixin {

    @ModifyArg(method = "tesselate",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;vertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFFFFFI)V"),
               index = 2,
               require = 16)
    private float gtceu$invertFluidFlowDirection(float originalY,
                                                 @Local(ordinal = 14) float sectionY) {
        // sadly we can't cache this in the vanilla fluid renderer because it's a singleton instance
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return sectionY + (1.0f - (originalY - sectionY));
        } else {
            return originalY;
        }
    }

    @ModifyArgs(method = "isFaceOccludedByState",
                at = @At(value = "INVOKE",
                         target = "Lnet/minecraft/world/phys/shapes/Shapes;box(DDDDDD)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private static void gtceu$invertExposedSideCheckDirection(Args args) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            // set minY to original maxY
            double maxY = args.get(4);
            args.set(1, maxY);
            // set maxY to 1
            args.set(4, 1.0D);
        }
    }

    // this direction replacement injection is split into 2 because we don't want to replace the UP shade query.
    // this one handles everything up to (and including) the first level.getShade call
    @ModifyExpressionValue(method = "tesselate",
                           at = {
                                   @At(value = "FIELD",
                                       target = "Lnet/minecraft/core/Direction;UP:Lnet/minecraft/core/Direction;",
                                       opcode = Opcodes.GETSTATIC),
                                   @At(value = "FIELD",
                                       target = "Lnet/minecraft/core/Direction;DOWN:Lnet/minecraft/core/Direction;",
                                       opcode = Opcodes.GETSTATIC)
                           },
                           slice = @Slice(to = @At(value = "INVOKE",
                                                   target = "Lnet/minecraft/world/level/BlockAndTintGetter;getShade(Lnet/minecraft/core/Direction;Z)F",
                                                   ordinal = 0)),
                           require = 5)
    private Direction gtceu$invertFluidCulling(Direction original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    // and this handles the one instance of Direction.UP after those.
    @Definition(id = "isFaceOccludedByNeighbor",
                method = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;isFaceOccludedByNeighbor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;FLnet/minecraft/world/level/block/state/BlockState;)Z")
    @Definition(id = "UP", field = "Lnet/minecraft/core/Direction;UP:Lnet/minecraft/core/Direction;")
    @Expression("isFaceOccludedByNeighbor(?, ?, UP, ?, ?)")
    @ModifyArg(method = "tesselate", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Direction gtceu$invertFluidCulling2(Direction original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return original.getOpposite();
        } else {
            return original;
        }
    }

    @Definition(id = "vertex",
                method = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;vertex(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFFFFFI)V")
    @Definition(id = "sectionX", local = @Local(type = float.class, ordinal = 13))
    @Definition(id = "sectionZ", local = @Local(type = float.class, ordinal = 15))
    @Expression({
            "this.vertex(?, sectionX, ?, sectionZ, ?, ?, ?, ?, ?, ?, ?)",
            "this.vertex(?, sectionX + 1.0, ?, sectionZ + 1.0, ?, ?, ?, ?, ?, ?, ?)",
    })
    @ModifyArgs(method = "tesselate",
                at = @At("MIXINEXTRAS:EXPRESSION"),
                slice = @Slice(
                               from = @At(value = "INVOKE",
                                          target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;getU0()F"),
                               to = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/renderer/block/LiquidBlockRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I",
                                        ordinal = 2)),
                require = 2,
                allow = 2)
    private void gtceu$invertBottomQuadsOrder(Args args,
                                              @Local(ordinal = 13) float localXOffset,
                                              @Local(ordinal = 15) float localZOffset) {
        // swap the 1st and 3rd vertex's position to invert the quad's orientation
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            float x = args.get(1);
            float z = args.get(3);
            if (x == localXOffset && z == localZOffset) {
                args.set(1, x + 1.0F);
                args.set(3, z + 1.0F);
            } else {
                args.set(1, x - 1.0F);
                args.set(3, z - 1.0F);
            }
        }
    }

    @WrapOperation(method = { "tesselate", "getHeight*", "getLightColor" },
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"),
                   require = 3)
    private BlockPos gtceu$invertFluidLightCheckAbove(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.below();
        } else {
            return original.call(pos);
        }
    }

    @WrapOperation(method = "tesselate",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"))
    private BlockPos gtceu$invertFluidLightCheckBelow(BlockPos pos, Operation<BlockPos> original) {
        if (InvertedFluidRenderer.INVERTED_FLUID_RENDERING.isActive()) {
            return pos.above();
        } else {
            return original.call(pos);
        }
    }
}
