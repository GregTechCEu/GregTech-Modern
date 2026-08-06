package com.gregtechceu.gtceu.core.mixins.client.bloom;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.lighting.ForgeModelBlockRenderer;
import net.minecraftforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ForgeModelBlockRenderer.class, remap = false)
public class ForgeModelBlockRendererMixin {

    @Definition(id = "setup",
                method = "Lnet/minecraftforge/client/model/lighting/QuadLighter;setup(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    // we don't really care about the args, but it's better to define them than not; less likely to break this way
    @Definition(id = "level", local = @Local(type = BlockAndTintGetter.class, argsOnly = true))
    @Definition(id = "pos", local = @Local(type = BlockPos.class, argsOnly = true))
    @Definition(id = "state", local = @Local(type = BlockState.class, argsOnly = true))
    @Expression("@(?).setup(level, pos, state)")
    @ModifyExpressionValue(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static QuadLighter gtceu$setQuadLighterRenderType(QuadLighter lighter,
                                                              @Local(argsOnly = true) RenderType renderType) {
        lighter.gtceu$setRenderType(renderType);
        return lighter;
    }
}
