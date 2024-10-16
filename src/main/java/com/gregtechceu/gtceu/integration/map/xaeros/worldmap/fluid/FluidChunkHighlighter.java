package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.fluid;

import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import xaero.map.WorldMap;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;

public class FluidChunkHighlighter extends ChunkHighlighter {

    public FluidChunkHighlighter() {
        super(false);
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return true;
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!GroupingMapRenderer.getInstance().doShowLayer("bedrock_fluids")) return null;

        ProspectorMode.FluidInfo fluid = XaerosRenderer.fluidElements.get(new ChunkPos(chunkX, chunkZ));
        if (fluid == null) return null;

        var topFluid = XaerosRenderer.fluidElements.get(new ChunkPos(chunkX, chunkZ - 1));
        var rightFluid = XaerosRenderer.fluidElements.get(new ChunkPos(chunkX + 1, chunkZ));
        var bottomFluid = XaerosRenderer.fluidElements.get(new ChunkPos(chunkX, chunkZ + 1));
        var leftFluid = XaerosRenderer.fluidElements.get(new ChunkPos(chunkX - 1, chunkZ));

        int color = IClientFluidTypeExtensions.of(fluid.fluid()).getTintColor();
        int fluidColorFormatted = (color & 255) << 24 | (color >> 8 & 255) << 16 | (color >> 16 & 255) << 8;
        int fillOpacity = 25;
        int borderOpacity = 50;
        int centerColor = fluidColorFormatted | 255 * fillOpacity / 100;
        int sideColor = fluidColorFormatted | 255 * borderOpacity / 100;

        this.resultStore[0] = centerColor;
        this.resultStore[1] = topFluid == null ? sideColor : centerColor;
        this.resultStore[2] = rightFluid == null ? sideColor : centerColor;
        this.resultStore[3] = bottomFluid == null ? sideColor : centerColor;
        this.resultStore[4] = leftFluid == null ? sideColor : centerColor;
        return this.resultStore;
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!GroupingMapRenderer.getInstance().doShowLayer("bedrock_fluids")) return 0;
        if (!regionHasHighlights(dimension, regionX, regionZ)) return 0;

        ProspectorMode.FluidInfo fluid = XaerosRenderer.fluidElements.get(new ChunkPos(regionX << 5, regionZ << 5));
        if (fluid == null) return 0;

        long accumulator = WorldMap.settings.claimsBorderOpacity;
        accumulator += fluid.left();
        accumulator *= 37L;
        accumulator += fluid.yield();
        accumulator *= 37L;
        accumulator = accumulator * 37L + (long) WorldMap.settings.claimsFillOpacity;
        accumulator = accumulator * 37L + regionX;
        accumulator = accumulator * 37L + regionZ;

        return (int) (accumulator >> 32) * 37 + (int) (accumulator);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaerosRenderer.fluidElements.get(new ChunkPos(chunkX, chunkZ)) != null;
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int x, int z) {
        var fluid = XaerosRenderer.fluidElements.get(new ChunkPos(x, z));
        return FluidRenderLayer.getTooltip(fluid).stream().reduce(Component.empty(), (c1, c2) -> {
            if (c1.getString().isEmpty()) {
                return c2;
            }
            if (c2.getString().isEmpty()) {
                return c1;
            }
            return ((MutableComponent) c1).append("\n").append(c2);
        });
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int x, int z) {
        return null;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX, int blockZ, int width) {}
}
