package com.gregtechceu.gtceu.core.mixins.client.bloom;

import com.gregtechceu.gtceu.core.util.extensions.QuadLighterExt;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.lighting.LightPipelineAwareModelBlockRenderer;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = LightPipelineAwareModelBlockRenderer.class, remap = false)
public class LightPipelineAwareModelBlockRendererMixin {

    @Definition(id = "setup",
                method = "Lnet/neoforged/neoforge/client/model/lighting/QuadLighter;setup(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    // we don't really care about the args, but it's better to define them than not; less likely to break this way
    @Definition(id = "level", local = @Local(type = BlockAndTintGetter.class, argsOnly = true))
    @Definition(id = "pos", local = @Local(type = BlockPos.class, argsOnly = true))
    @Definition(id = "state", local = @Local(type = BlockState.class, argsOnly = true))
    @Expression("@(?).setup(level, pos, state)")
    @ModifyExpressionValue(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static QuadLighter gtceu$setQuadLighterRenderType(QuadLighter lighter,
                                                              @Local(argsOnly = true) RenderType renderType) {
        ((QuadLighterExt) lighter).gtceu$setRenderType(renderType);
        return lighter;
    }
}
