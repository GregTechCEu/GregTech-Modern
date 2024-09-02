package com.gregtechceu.gtceu.integration.map.layer;

import com.gregtechceu.gtceu.integration.map.ButtonState;
import com.gregtechceu.gtceu.integration.map.WaypointManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

public abstract class MapRenderLayer {

    protected final String key;

    public MapRenderLayer(String key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return true;//ButtonState.isEnabled(key);
    }

    /**
     * Render the overlay.
     * <br>
     * Starting GL state:
     * <br>
     * 1 unit = 1 block, positioned such that drawing at (x, z) draws over the entire block (x, z)
     * 
     * @param cameraX The X position of the center block of the view
     * @param cameraZ The Z position of the center block of the view
     * @param scale   The scale of the view, such that going from blocks -> pixels requires scaling at
     *                <code>1/scale</code>
     */
    public abstract void render(PoseStack poseStack, GuiGraphics graphics, double cameraX, double cameraZ, float scale);

    /**
     * Update what parts of the overlay should be visible.
     *
     * @param dimension     The ID of the dimension currently being viewed.
     * @param visibleBounds Contains the X and Z coordinates of the top left block of the view, followed by the width
     *                      and height, in blocks
     */
    public abstract void updateVisibleArea(ResourceKey<Level> dimension, int[] visibleBounds);

    /**
     * Update what part of the overlay the mouse is over.
     * Data required for {@link #getTooltip()}, {@link #onActionKey()}, etc. should be cached here.
     * 
     * @param mouseX  The mouse's X position. Does not need to be adjusted for gui scale.
     * @param mouseY  The mouse's Y position. Does not need to be adjusted for gui scale.
     * @param cameraX (as in {@link #render(PoseStack, GuiGraphics, double, double, float)})
     * @param cameraZ (as in {@link #render(PoseStack, GuiGraphics, double, double, float)})
     * @param scale   (as in {@link #render(PoseStack, GuiGraphics, double, double, float)})
     */
    public void updateHovered(int mouseX, int mouseY, double cameraX, double cameraZ, double scale) {}

    /**
     * @return A list of strings that contains the lines to be rendered in the tooltip.
     */
    public List<Component> getTooltip() {
        return null;
    }

    /**
     * @return true if the keypress should be consumed
     */
    public boolean onActionKey() {
        return false;
    }

    /**
     * Generally, only one of this and {@link #onDoubleClick()} should be overridden
     * 
     * @return true if the click should be consumed
     */
    public boolean onClick() {
        return false;
    }

    /**
     * Generally, only one of this and {@link #onClick()} should be overridden
     * 
     * @return true if the click should be consumed
     */
    public boolean onDoubleClick() {
        return false;
    }

    /**
     * Toggle this layer's waypoint.
     * 
     * @param name The name of the waypoint.
     * @param dim  The ID of the dimension the waypoint should be in, <code>null</code> for the player's current
     *             dimension
     * @return true if the waypoint was created or moved, false if it was deleted.
     */
    @SuppressWarnings("SameParameterValue")
    protected final boolean toggleWaypoint(String name, int color, ResourceKey<Level> dim, int x, int y, int z,
                                           ResourceLocation texture) {
        return WaypointManager.toggleWaypoint(key, name, color, dim, x, y, z, texture);
    }
}
