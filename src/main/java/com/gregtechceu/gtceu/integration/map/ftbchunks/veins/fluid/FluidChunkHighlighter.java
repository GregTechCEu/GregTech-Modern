package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.world.level.ChunkPos;

import dev.ftb.mods.ftbchunks.client.map.MapChunk;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftblibrary.math.XZ;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import lombok.Getter;

@Getter
public class FluidChunkHighlighter {

    protected MapDimension dimension;
    protected int chunkX;
    protected int chunkZ;
    protected MapChunk mapChunk;

    public FluidChunkHighlighter(Panel p, MapDimension dimension, ChunkPos chunkPos) {
        this.dimension = dimension;
        this.chunkX = chunkPos.x;
        this.chunkZ = chunkPos.z;
        var pos = XZ.regionFromChunk(chunkPos);
        this.mapChunk = this.dimension.getRegion(pos).getDataBlocking().getChunk(pos);
    }

    public boolean isEnabled() {
        return GroupingMapRenderer.getInstance().doShowLayer("bedrock_fluids");
    }

    public boolean shouldDraw() {
        return isEnabled();
    }

    public void addMouseOverText(TooltipList list) {
        // todo: append tooltips
    }
}
