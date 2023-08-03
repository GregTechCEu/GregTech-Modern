package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.server.level.WorldGenRegion;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {

    @Shadow @Final
    private int writeRadiusCutoff;

    @Redirect(method = "ensureCanWrite(Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/WorldGenRegion;writeRadiusCutoff:I", opcode = Opcodes.GETFIELD))
    public int gtceu$changeWriteRadius(WorldGenRegion instance) {
        if (writeRadiusCutoff == 0) {
            return ConfigHolder.INSTANCE.worldgen.maxFeatureChunkSize;
        }
        return writeRadiusCutoff;
    }
}
