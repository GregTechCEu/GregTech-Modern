package com.gregtechceu.gtceu.core.mixins.embeddium;

import com.gregtechceu.gtceu.client.util.BloomEffectUtil;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin {


    @Shadow @Final private int chunkX;

    @Shadow @Final private int chunkY;

    @Shadow @Final private int chunkZ;

    @Inject(method = "delete", at = @At("HEAD"))
    private void gtceu$resetBloomBuffers(CallbackInfo ci) {
        BlockPos origin = SectionPos.of(this.chunkX, this.chunkY, this.chunkZ).origin();
        BloomEffectUtil.removeBloomChunk(origin);

    }
}
