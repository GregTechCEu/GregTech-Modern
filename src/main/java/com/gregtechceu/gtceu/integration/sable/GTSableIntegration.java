package com.gregtechceu.gtceu.integration.sable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import dev.ryanhcode.sable.Sable;

public final class GTSableIntegration {

    private GTSableIntegration() {}

    public static boolean isWithinSubLevel(Level level, BlockPos pos) {
        return Sable.HELPER.getContaining(level, (Vec3i) pos) != null;
    }
}
