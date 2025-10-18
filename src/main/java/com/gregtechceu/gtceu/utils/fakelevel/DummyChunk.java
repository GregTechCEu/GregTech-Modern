package com.gregtechceu.gtceu.utils.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DummyChunk extends LevelChunk {

    public DummyChunk(SchemaLevel level, ChunkPos pos) {
        super(level, pos);
    }

    private SchemaLevel getGuidebookLevel() {
        return (SchemaLevel) getLevel();
    }

    public @Nullable BlockState setBlockState(@NotNull BlockPos pos, @NotNull BlockState state, boolean isMoving) {
        getGuidebookLevel().prepareLighting(pos);

        var result = super.setBlockState(pos, state, isMoving);
        if (!state.isAir()) {
            getGuidebookLevel().addFilledBlock(pos);
        } else {
            getGuidebookLevel().removeFilledBlock(pos);
        }
        return result;
    }

    public @NotNull FullChunkStatus getFullStatus() {
        return FullChunkStatus.FULL;
    }
}
