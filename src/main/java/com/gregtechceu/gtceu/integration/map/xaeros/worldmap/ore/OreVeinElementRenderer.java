package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosWorldMapPlugin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;
import xaero.map.element.MapElementReader;
import xaero.map.element.MapElementRenderProvider;
import xaero.map.element.MapElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;

public class OreVeinElementRenderer extends
                                    MapElementRenderer<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> {

    protected static final ResourceLocation STONE = new ResourceLocation("textures/block/stone.png");

    protected OreVeinElementRenderer(OreVeinElementContext context,
                                     MapElementRenderProvider<OreVeinElement, OreVeinElementContext> provider,
                                     MapElementReader<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> reader) {
        super(context, provider, reader);
    }

    @Override
    public boolean shouldBeDimScaled() {
        return false;
    }

    @Override
    public void beforeRender(int location, Minecraft mc, GuiGraphics guiGraphics,
                             double cameraX, double cameraZ, double mouseX, double mouseZ,
                             float brightness, double scale, double screenSizeBasedScale, TextureManager textureManager,
                             Font fontRenderer,
                             MultiBufferSource.BufferSource renderTypeBuffers,
                             MultiTextureRenderTypeRendererProvider rendererProvider,
                             boolean pre) {}

    @Override
    public void afterRender(int location, Minecraft mc, GuiGraphics guiGraphics,
                            double cameraX, double cameraZ, double mouseX, double mouseZ,
                            float brightness, double scale, double screenSizeBasedScale,
                            TextureManager textureManager, Font fontRenderer,
                            MultiBufferSource.BufferSource renderTypeBuffers,
                            MultiTextureRenderTypeRendererProvider rendererProvider,
                            boolean pre) {}

    @Override
    public void renderElementPre(int location, OreVeinElement w, boolean hovered,
                                 Minecraft mc, GuiGraphics guiGraphics,
                                 double cameraX, double cameraZ, double mouseX, double mouseZ,
                                 float brightness, double scale, double screenSizeBasedScale,
                                 TextureManager textureManager, Font fontRenderer,
                                 MultiBufferSource.BufferSource renderTypeBuffers,
                                 MultiTextureRenderTypeRendererProvider rendererProvider,
                                 float optionalScale, double partialX, double partialY,
                                 boolean cave,
                                 float partialTicks) {}

    @Override
    public boolean renderElement(int location, OreVeinElement element,
                                 boolean hovered,
                                 Minecraft mc, GuiGraphics graphics,
                                 double cameraX, double cameraZ, double mouseX, double mouseZ,
                                 float brightness, double scale, double screenSizeBasedScale,
                                 TextureManager textureManager, Font fontRenderer,
                                 MultiBufferSource.BufferSource renderTypeBuffers,
                                 MultiTextureRenderTypeRendererProvider rendererProvider,
                                 int elementIndex, double optionalDepth, float optionalScale,
                                 double partialX, double partialY,
                                 boolean cave, float partialTicks) {
        GeneratedVeinMetadata vein = element.getVein();
        int iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        Material firstMaterial = vein.definition().veinGenerator().getAllMaterials().get(0);
        int materialARGB = firstMaterial.getMaterialARGB();
        float[] colors = DrawUtil.floats(materialARGB);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        ResourceLocation texture = MaterialIconType.ore.getBlockTexturePath(firstMaterial.getMaterialIconSet(), true);
        if (texture == null) {
            return false;
        }
        texture = texture.withPath(path -> "textures/" + path + ".png");
        graphics.blit(STONE, -iconSize / 2, -iconSize / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);

        RenderSystem.setShaderColor(colors[0], colors[1], colors[2], 1);
        graphics.blit(texture, -iconSize / 2, -iconSize / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);

        RenderSystem.setShaderColor(1, 1, 1, 1);
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
    public boolean shouldRender(int location, boolean pre) {
        return XaerosWorldMapPlugin.getOptionValue("ore_veins");
    }

    public static final class Builder {

        private Builder() {}

        public OreVeinElementRenderer build() {
            return new OreVeinElementRenderer(new OreVeinElementContext(), new OreVeinElementRenderProvider(),
                    new OreVeinElementReader());
        }

        public static OreVeinElementRenderer.Builder begin() {
            return new OreVeinElementRenderer.Builder();
        }
    }
}
