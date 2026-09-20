package brachy.modularui.core.mixins.client;

import brachy.modularui.client.SchemaPreviewRenderer;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Both block geometry and block entity features must use the preview level's lighting. */
@Mixin(GameRenderer.class)
public abstract class PreviewLightmapMixin {
    @Inject(method = "lightmap", at = @At("HEAD"), cancellable = true)
    private void modularui$previewLightmap(CallbackInfoReturnable<GpuTextureView> cir) {
        var lightmap = SchemaPreviewRenderer.activeLightmap();
        if (lightmap != null) cir.setReturnValue(lightmap);
    }
}
