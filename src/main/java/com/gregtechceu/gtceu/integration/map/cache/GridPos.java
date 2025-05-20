package com.gregtechceu.gtceu.integration.map.cache;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class GridPos {

    public int x;
    public int z;

    public GridPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public GridPos(ChunkPos chunk) {
        this.x = Math.floorDiv(chunk.x, ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
        this.z = Math.floorDiv(chunk.z, ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
    }

    public GridPos(BlockPos block) {
        this.x = Math.floorDiv((block.getX() >> 4), ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
        this.z = Math.floorDiv((block.getZ() >> 4), ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
    }

    public ChunkPos getChunk(int x, int z) {
        return new ChunkPos(this.x * 3 + x, this.z * 3 + z);
    }

    public BlockPos getBlock(int x, int y, int z) {
        return new BlockPos(this.x * 3 * 16 + x, y, this.z * 3 * 16 + z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridPos gridPos = (GridPos) o;
        return x == gridPos.x && z == gridPos.z;
    }

    @Override
    public int hashCode() {
        return GTMath.hashInts(x, z);
    }

    @Override
    public String toString() {
        return "<" + x + ", " + z + ">";
    }

    public static int chunkToGridCoords(int c) {
        return Math.floorDiv(c, 3);
    }

    public static int blockToGridCoords(int c) {
        return Math.floorDiv((c >> 4), ConfigHolder.INSTANCE.worldgen.oreVeins.oreVeinGridSize);
    }

    public static GridPos fromChunkCoords(int cx, int cz) {
        return new GridPos(chunkToGridCoords(cx), chunkToGridCoords(cz));
    }
}
