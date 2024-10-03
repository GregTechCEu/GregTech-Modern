package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultMaterials.class, remap = false)
public class DefaultMaterialsMixin {

    @Inject(method = "forRenderLayer",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderType;translucent()Lnet/minecraft/client/renderer/RenderType;",
                     remap = true),
            cancellable = true)
    private static void gtceu$allowBloomMaterial(RenderType layer, CallbackInfoReturnable<Material> cir) {
        if (layer == BloomEffectUtil.getBloomLayer()) {
            cir.setReturnValue(BloomEffectUtil.EMBEDDIUM_MATERIAL_BLOOM.get().get());
        }
    }
}
