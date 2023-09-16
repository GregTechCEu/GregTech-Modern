package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.utils.vec3i.Chunk3DPosUtils;
import com.gregtechceu.gtceu.utils.vec3i.Vec3iRangeIterator;
import com.gregtechceu.gtceu.utils.vec3i.Vec3iUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.BulkSectionAccess;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ChunkedPosIterator implements Iterator<ChunkedPosIterator.Pos> {
    public record Pos(BulkSectionAccess access, Vec3i pos) {
    }


    private final Vec3i min;
    private final Vec3i max;

    private final Vec3iRangeIterator chunkIterator;
    private final Level level;

    private Vec3iRangeIterator blocksIterator = null;
    private BulkSectionAccess access = null;

    public ChunkedPosIterator(Level level, Vec3i min, Vec3i max) {
        this.level = level;
        this.min = min;
        this.max = max;

        this.chunkIterator = new Vec3iRangeIterator(
                Chunk3DPosUtils.getChunkAtBlock(min),
                Chunk3DPosUtils.getChunkAtBlock(max)
        );
    }

    @Override
    public boolean hasNext() {
        final var hasNext = (blocksIterator != null && blocksIterator.hasNext()) || chunkIterator.hasNext();

        // next() isn't called after the last element, so the current access needs to be closed here instead.
        if (!hasNext && access != null) {
            access.close();
        }

        return hasNext;
    }

    @Override
    public ChunkedPosIterator.Pos next() {
        if (blocksIterator == null || !blocksIterator.hasNext()) {
            nextChunk();
        }

        return new ChunkedPosIterator.Pos(access, blocksIterator.next());
    }

    private void nextChunk() {
        if (access != null)
            access.close();

        final var chunk = chunkIterator.next();
        final var minBlock = Vec3iUtils.max(Chunk3DPosUtils.getChunkMinBlock(chunk), min);
        final var maxBlock = Vec3iUtils.min(Chunk3DPosUtils.getChunkMaxBlock(chunk), max);

        this.blocksIterator = new Vec3iRangeIterator(minBlock, maxBlock);
        this.access = new BulkSectionAccess(level);
    }
}
