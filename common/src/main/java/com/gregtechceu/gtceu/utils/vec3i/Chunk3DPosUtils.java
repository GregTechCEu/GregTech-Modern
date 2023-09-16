package com.gregtechceu.gtceu.utils.vec3i;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Vec3i;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Chunk3DPosUtils {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_MAX = CHUNK_SIZE - 1;


    public static Vec3i getChunkMinBlock(Vec3i chunkPos) {
        return new Vec3i(
                chunkPos.getX() * CHUNK_SIZE,
                chunkPos.getY() * CHUNK_SIZE,
                chunkPos.getZ() * CHUNK_SIZE
        );
    }

    public static Vec3i getChunkMaxBlock(Vec3i chunkPos) {
        return new Vec3i(
                (chunkPos.getX() * CHUNK_SIZE) + CHUNK_MAX,
                (chunkPos.getY() * CHUNK_SIZE) + CHUNK_MAX,
                (chunkPos.getZ() * CHUNK_SIZE) + CHUNK_MAX
        );
    }

    public static Vec3i getChunkAtBlock(Vec3i pos) {
        var x = (pos.getX() - (pos.getX() % CHUNK_SIZE)) / CHUNK_SIZE;
        var y = (pos.getY() - (pos.getY() % CHUNK_SIZE)) / CHUNK_SIZE;
        var z = (pos.getZ() - (pos.getZ() % CHUNK_SIZE)) / CHUNK_SIZE;

        return new Vec3i(x, y, z);
    }
}
