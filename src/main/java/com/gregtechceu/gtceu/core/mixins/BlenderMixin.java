package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.IGTBlender;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Blender.class)
public class BlenderMixin implements IGTBlender {

    @Unique
    private WorldGenRegion gtceu$region;

    @Inject(method = "of", at = @At(value = "TAIL"))
    private static void gtceu$captureRegion(WorldGenRegion region, CallbackInfoReturnable<Blender> cir) {
        ((IGTBlender)cir.getReturnValue()).setRegion(region);
    }

    @Override
    public WorldGenRegion getRegion() {
        return gtceu$region;
    }

    @Override
    public void setRegion(@Nullable WorldGenRegion region) {
        gtceu$region = region;
    }
}
