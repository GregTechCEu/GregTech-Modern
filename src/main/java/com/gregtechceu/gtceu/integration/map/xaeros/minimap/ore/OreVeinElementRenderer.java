package com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GTMapRendering;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;

public class OreVeinElementRenderer extends MinimapElementRenderer<OreVeinElement, OreVeinElementContext> {

    protected static final Identifier STONE = Identifier.withDefaultNamespace("block/stone");

    private OreVeinElementRenderer(OreVeinElementReader elementReader,
                                   OreVeinElementRenderProvider provider,
                                   OreVeinElementContext context) {
        super(elementReader, provider, context);
    }

    @Override
    public void preRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource renderTypeBuffers,
                          MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {}

    @Override
    public boolean renderElement(OreVeinElement element,
                                 boolean highlit,
                                 boolean outOfBounds,
                                 double optionalDepth, float optionalScale, double partialX, double partialY,
                                 MinimapElementRenderInfo renderInfo,
                                 GuiGraphics graphics, MultiBufferSource.BufferSource renderTypeBuffers) {
        GeneratedVeinMetadata vein = element.getVein();
        int iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        Material firstMaterial = vein.definition().value().veinGenerator().getAllMaterials().getFirst();
        int materialARGB = firstMaterial.getMaterialARGB();

        Identifier oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(),
                true);
        if (oreTexture != null) {
            var oreSprite = GTMapRendering.getBlockSprite(oreTexture);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, oreSprite, -iconSize / 2, -iconSize / 2, iconSize,
                    iconSize, materialARGB);
        }
        // FIXME drawing the 2nd layer makes xaero's minimap transparent. so we won't. for now.
        // oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(), "secondary",
        // true);
        // if (oreTexture != null) {
        // int materialSecondaryARGB = firstMaterial.getMaterialSecondaryARGB();
        // colors = DrawUtil.floats(materialSecondaryARGB);
        // var oreSprite = Minecraft.getInstance()
        // .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
        // .apply(oreTexture);
        // graphics.blit(-iconSize / 2, -iconSize / 2, 0, iconSize, iconSize,
        // oreSprite, colors[0], colors[1], colors[2], 1);
        // }

        int borderColor = ConfigHolder.INSTANCE.compat.minimap.getBorderColor(materialARGB | 0xFF000000);
        if ((borderColor & 0xFF000000) != 0) {
            int thickness = iconSize / 16;
            graphics.fill(-iconSize / 2, -iconSize / 2, iconSize, thickness, borderColor);
            graphics.fill(-iconSize / 2, -iconSize / 2 - thickness, iconSize, thickness, borderColor);
            graphics.fill(-iconSize / 2, -iconSize / 2, thickness, iconSize, borderColor);
            graphics.fill(-iconSize / 2 - thickness, -iconSize / 2, thickness, iconSize, borderColor);
        }
        return true;
    }

    @Override
    public void postRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource renderTypeBuffers,
                           MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {}

    @Override
    public boolean shouldRender(MinimapElementRenderLocation location) {
        return GroupingMapRenderer.getInstance().doShowLayer("ore_veins") &&
                location == MinimapElementRenderLocation.IN_MINIMAP;
    }

    public static final class Builder {

        private Builder() {}

        public OreVeinElementRenderer build() {
            return new OreVeinElementRenderer(new OreVeinElementReader(), new OreVeinElementRenderProvider(),
                    new OreVeinElementContext());
        }

        public static OreVeinElementRenderer.Builder begin() {
            return new OreVeinElementRenderer.Builder();
        }
    }
}
