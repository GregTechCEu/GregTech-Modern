package com.gregtechceu.gtceu.integration.map;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.integration.map.journeymap.JourneymapRenderer;
import com.gregtechceu.gtceu.integration.map.layer.Layers;
import com.gregtechceu.gtceu.integration.map.layer.MapRenderLayer;
import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A map renderer designed to work with any map mod.
 */
@OnlyIn(Dist.CLIENT)
public abstract class GenericMapRenderer {

    @Getter
    private static final GenericMapRenderer instance;

    static {
        if (Platform.isModLoaded(GTValues.MODID_JOURNEYMAP)) {
            instance = new JourneymapRenderer();
        } else if (Platform.isModLoaded(GTValues.MODID_XAEROS_MINIMAP)) {
            instance = new XaerosRenderer();
        } else {
            instance = null;
        }
    }

    private static final int VISIBLE_AREA_PADDING = 20;

    protected ResourceKey<Level> dimension;
    protected Screen gui;

    protected int[] visibleBounds = new int[4];

    protected List<MapRenderLayer> layers;
    private double oldMouseX;
    private double oldMouseY;
    private long timeLastClick;

    public GenericMapRenderer() {
        layers = new ArrayList<>();
        Layers.addLayersTo(layers, this);
    }

    public GenericMapRenderer(Screen gui) {
        this();
        this.gui = gui;
    }

    public void updateVisibleArea(ResourceKey<Level> dim, int x, int y, int w, int h) {
        // padding visible area to reduce/eliminate pop-in at map edges
        x -= VISIBLE_AREA_PADDING;
        y -= VISIBLE_AREA_PADDING;
        w += VISIBLE_AREA_PADDING * 2;
        h += VISIBLE_AREA_PADDING * 2;
        if (!dim.equals(dimension) || visibleBounds[0] != x || visibleBounds[1] != y || visibleBounds[2] != w ||
                visibleBounds[3] != h) {
            dimension = dim;
            visibleBounds[0] = x;
            visibleBounds[1] = y;
            visibleBounds[2] = w;
            visibleBounds[3] = h;

            for (MapRenderLayer layer : layers) {
                layer.updateVisibleArea(dimension, visibleBounds);
            }

        }
    }

    /**
     * Update the active overlays' hovered items.
     * 
     * @param mouseX  X position of the mouse, intended to be taken from the second parameter of {@link Screen#render}
     * @param mouseY  Y position of the mouse, see mouseX
     * @param cameraX X position of the center block of the view
     * @param cameraZ Z position of the center block of the view
     * @param scale   Scale of the camera, such that scaling by <code>1/scale</code> results in 1 unit = 1 pixel
     */
    public void updateHovered(int mouseX, int mouseY, double cameraX, double cameraZ, double scale) {
        for (MapRenderLayer layer : layers) {
            if (layer.isEnabled()) {
                layer.updateHovered(mouseX, mouseY, cameraX, cameraZ, scale);
            }
        }
    }

    /**
     * Render all active map overlays.
     * <br>
     * EXPECTED GL STATE:
     * <br>
     * 1 unit = 1 block, positioned such that drawing at (x, z) draws over the entire block (x, z)
     * 
     * @param cameraX X position of the center block of the view
     * @param cameraZ Z position of the center block of the view
     * @param scale   Scale of the camera, such that scaling by <code>1/scale</code> results in 1 unit = 1 pixel
     */
    public void render(GuiGraphics graphics, double cameraX, double cameraZ, float scale) {
        for (MapRenderLayer layer : layers) {
            if (layer.isEnabled()) {
                layer.render(graphics.pose(), graphics, cameraX, cameraZ, scale);
            }
        }
    }

    /**
     * Render tooltips provided by active overlays.
     * <br>
     * Will error if called from a context with no GuiScreen provided - such as a minimap. Only call if one was
     * provided.
     * 
     * @param mouseX X position of the mouse, intended to be taken from the first parameter of {@link Screen#render}
     * @param mouseY Y position of the mouse, see mouseX
     */
    public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        for (MapRenderLayer layer : layers) {
            if (layer.isEnabled()) {
                List<Component> layerTooltip = layer.getTooltip();
                if (layerTooltip != null && !layerTooltip.isEmpty()) {
                    if (!tooltip.isEmpty()) {
                        tooltip.add(0, Component.literal("---"));
                        tooltip.addAll(0, layerTooltip);
                    } else {
                        // layerTooltip might be immutable
                        tooltip = new ArrayList<>(layerTooltip);
                    }
                }
            }
        }
        renderTooltipInternal(tooltip, graphics, mouseX, mouseY);
    }

    /**
     * Call when {@link KeyBind#ACTION} is pressed.
     * 
     * @return Whether to consume the key press
     */
    public boolean onActionKey() {
        for (MapRenderLayer layer : layers) {
            if (layer.isEnabled() && layer.onActionKey()) {
                return true;
            }
        }
        return false;
    }

    public boolean onClick(double mouseX, double mouseY) {
        final long timestamp = System.currentTimeMillis();
        final boolean isDoubleClick = mouseX == oldMouseX && mouseY == oldMouseY && timestamp - timeLastClick < 500;
        oldMouseX = mouseX;
        oldMouseY = mouseY;
        timeLastClick = isDoubleClick ? 0 : timestamp;

        for (MapRenderLayer layer : layers) {
            if (layer.isEnabled()) {
                if (isDoubleClick && layer.onDoubleClick() || !isDoubleClick && layer.onClick()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Override this if specializing the renderer to have consistent tooltip theming
     */
    protected void renderTooltipInternal(List<Component> tooltip, GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    public abstract boolean addMarker(String name, GeneratedVeinMetadata vein);

    public abstract boolean removeMarker(String name);
}
