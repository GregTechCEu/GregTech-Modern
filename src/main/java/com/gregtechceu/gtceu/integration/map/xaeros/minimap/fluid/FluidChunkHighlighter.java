package com.gregtechceu.gtceu.integration.map.xaeros.minimap.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import xaero.common.minimap.highlight.ChunkHighlighter;
import xaero.common.minimap.info.render.compile.InfoDisplayCompiler;

public class FluidChunkHighlighter extends ChunkHighlighter {

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

        int fillOpacity = 25;
        int borderOpacity = 50;
        int centerColor = color | 255 * fillOpacity / 100;
        int sideColor = color | 255 * borderOpacity / 100;

        this.resultStore[0] = centerColor;
        this.resultStore[1] = topFluid == null ? sideColor : centerColor;
        this.resultStore[2] = rightFluid == null ? sideColor : centerColor;
        this.resultStore[3] = bottomFluid == null ? sideColor : centerColor;
        this.resultStore[4] = leftFluid == null ? sideColor : centerColor;
        return this.resultStore;
    }

    @Override
    public void addChunkHighlightTooltips(InfoDisplayCompiler compiler, ResourceKey<Level> dimension, int chunkX,
                                          int chunkZ, int width) {}

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return isEnabled() && XaerosRenderer.fluidElements.get(dimension, new ChunkPos(chunkX, chunkZ)) != null;
    }
}
