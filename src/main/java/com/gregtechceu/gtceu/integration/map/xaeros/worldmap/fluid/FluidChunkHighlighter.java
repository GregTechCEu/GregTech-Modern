package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import xaero.map.highlight.ChunkHighlighter;

import java.util.List;

public class FluidChunkHighlighter extends ChunkHighlighter {

    private static final int FILL_OPACITY = 25;
    private static final int BORDER_OPACITY = 50;

    public FluidChunkHighlighter() {
        super(false);
    }

    private boolean isEnabled() {
        return GroupingMapRenderer.getInstance().doShowLayer("bedrock_fluids");
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return isEnabled();
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!isEnabled()) return null;

        var dimensionMap = XaerosRenderer.fluidElements.row(dimension);
        ProspectorMode.FluidInfo vein = dimensionMap.get(new ChunkPos(chunkX, chunkZ));
        if (vein == null) return null;

        var topFluid = dimensionMap.get(new ChunkPos(chunkX, chunkZ - 1));
        var rightFluid = dimensionMap.get(new ChunkPos(chunkX + 1, chunkZ));
        var bottomFluid = dimensionMap.get(new ChunkPos(chunkX, chunkZ + 1));
        var leftFluid = dimensionMap.get(new ChunkPos(chunkX - 1, chunkZ));

        int color = IClientFluidTypeExtensions.of(vein.fluid()).getTintColor();
        Material material = ChemicalHelper.getMaterial(vein.fluid());
        if (!material.isNull()) {
            color = material.getMaterialARGB();
        }
        color = (color & 0xFF) << 24 | (color >> 8 & 0xFF) << 16 | (color >> 16 & 0xFF) << 8;

        int centerColor = color | 255 * FILL_OPACITY / 100;
        int sideColor = color | 255 * BORDER_OPACITY / 100;

        this.resultStore[0] = centerColor;
        this.resultStore[1] = topFluid == null ? sideColor : centerColor;
        this.resultStore[2] = rightFluid == null ? sideColor : centerColor;
        this.resultStore[3] = bottomFluid == null ? sideColor : centerColor;
        this.resultStore[4] = leftFluid == null ? sideColor : centerColor;
        return this.resultStore;
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!isEnabled()) return 0;

        long accumulator = FILL_OPACITY;

        for (int x = regionX << 5; x < (regionX + 1) << 5; x++) {
            for (int z = regionZ << 5; z < (regionZ + 1) << 5; z++) {
                ProspectorMode.FluidInfo fluid = XaerosRenderer.fluidElements.get(dimension, new ChunkPos(x, z));
                if (fluid == null) {
                    accumulator *= 37L;
                    accumulator = accumulator * 37L + x;
                    accumulator = accumulator * 37L + z;
                    continue;
                }

                accumulator += BuiltInRegistries.FLUID.getId(fluid.fluid());
                accumulator *= 37L;
                accumulator += fluid.yield();
                accumulator *= 37L;
                accumulator = accumulator * 37L + x;
                accumulator = accumulator * 37L + z;
            }
        }
        accumulator = accumulator * 37L + (long) BORDER_OPACITY;

        return (int) (accumulator >> 32) * 37 + (int) (accumulator);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return isEnabled() && XaerosRenderer.fluidElements.get(dimension, new ChunkPos(chunkX, chunkZ)) != null;
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int x, int z) {
        return null;
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int x, int z) {
        if (!isEnabled()) {
            return null;
        }
        var fluid = XaerosRenderer.fluidElements.get(dimension, new ChunkPos(x, z));
        if (fluid == null) {
            return null;
        }
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
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX,
                                                 int blockZ, int width) {}
}
