package com.gregtechceu.gtceu.api.gui.texture;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.Getter;

import java.io.IOException;
import java.lang.reflect.Array;

@SuppressWarnings({ "rawtypes", "unchecked" })
@OnlyIn(Dist.CLIENT)
public class ProspectingTexture {

    public static final String SELECTED_ALL = "[all]";
    private static final ResourceTexture ARROW = GuiTextures.UP.copy().setColor(ColorPattern.RED.color);

    @Getter
    private String selected = SELECTED_ALL;
    private boolean darkMode;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    public final Object[][][] data;
    private final int playerXGui;
    private final int playerYGui;
    private final float direction;
    private final int playerChunkX;
    private final int playerChunkZ;
    private final ProspectorMode mode;
    private final int radius;

    public ProspectingTexture(int playerChunkX, int playerChunkZ, int posX, int posZ, float direction,
                              ProspectorMode mode, int radius, boolean darkMode) {
        this.darkMode = darkMode;
        this.radius = radius;
        this.mode = mode;
        this.data = (Object[][][]) Array.newInstance(mode.getItemClass(), (radius * 2 - 1) * mode.cellSize,
                (radius * 2 - 1) * mode.cellSize, 0);
        this.imageWidth = (radius * 2 - 1) * 16;
        this.imageHeight = (radius * 2 - 1) * 16;
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
        this.direction = (direction + 180) % 360;
        this.playerXGui = posX - (playerChunkX - this.radius + 1) * 16 + (posX > 0 ? 1 : 0);
        playerYGui = posZ - (playerChunkZ - this.radius + 1) * 16 + (posX > 0 ? 1 : 0);
    }

    public void updateTexture(PacketProspecting packet) {
        int ox;
        if ((packet.chunkX > 0 && playerChunkX > 0) || (packet.chunkX < 0 && playerChunkX < 0)) {
            ox = Math.abs(Math.abs(packet.chunkX) - Math.abs(playerChunkX));
        } else {
            ox = Math.abs(playerChunkX) + Math.abs(packet.chunkX);
        }
        if (playerChunkX > packet.chunkX) {
            ox = -ox;
        }

        int oy;
        if ((packet.chunkZ > 0 && playerChunkZ > 0) || (packet.chunkZ < 0 && playerChunkZ < 0)) {
            oy = Math.abs(Math.abs(packet.chunkZ) - Math.abs(playerChunkZ));
        } else {
            oy = Math.abs(playerChunkZ) + Math.abs(packet.chunkZ);
        }
        if (playerChunkZ > packet.chunkZ) {
            oy = -oy;
        }

        int currentColumn = (this.radius - 1) + ox;
        int currentRow = (this.radius - 1) + oy;
        if (currentRow < 0) {
            return;
        }

        for (int x = 0; x < mode.cellSize; x++) {
            System.arraycopy(packet.data[x], 0, data[x + currentColumn * mode.cellSize], currentRow * mode.cellSize,
                    mode.cellSize);
        }
        load();
    }

    private NativeImage getImage() {
        int wh = (this.radius * 2 - 1) * 16;
        NativeImage image = new NativeImage(wh, wh, false);
        for (int i = 0; i < wh; i++) {
            for (int j = 0; j < wh; j++) {
                Object[] items = this.data[i * mode.cellSize / 16][j * mode.cellSize / 16];
                // draw bg
                image.setPixelABGR(i, j, (darkMode ? ColorPattern.GRAY.color : ColorPattern.WHITE.color));
                // draw items
                for (Object item : items) {
                    if (!selected.equals(SELECTED_ALL) && !selected.equals(mode.getUniqueID(item))) continue;
                    int color = mode.getItemColor(item);
                    image.setPixelABGR(i, j,
                            combine(255, ColorUtils.blueI(color), ColorUtils.greenI(color), ColorUtils.redI(color)));
                    break;
                }
                // draw grid
                if ((i) % 16 == 0 || (j) % 16 == 0) {
                    image.setPixelABGR(i, j, ColorUtils.averageColor(image.getPixel(i, j), 0xff000000));
                }
            }
        }
        return image;
    }

    /**
     * The resulting color of this operation is stored as least to most significant bits.
     */
    public static int combine(int alpha, int blue, int green, int red) {
        return (alpha & 0xFF) << 24 | (blue & 0xFF) << 16 | (green & 0xFF) << 8 | (red & 0xFF) << 0;
    }

    public void load() {
        doLoad(getImage());
    }

    private void doLoad(NativeImage image) {
        image.close();
    }

    public void draw(GuiGraphics graphics, int x, int y) {
        try (NativeImage image = getImage()) {
            for (int i = 0; i < imageWidth; i++) {
                for (int j = 0; j < imageHeight; j++) {
                    DrawerHelper.drawSolidRect(graphics, x + i, y + j, 1, 1, image.getPixel(i, j));
                }
            }
        }

        // draw special grid (e.g. fluid)
        for (int cx = 0; cx < radius * 2 - 1; cx++) {
            for (int cz = 0; cz < radius * 2 - 1; cz++) {
                if (this.data[cx][cz] != null && this.data[cx][cz].length > 0) {
                    var items = this.data[cx][cz];
                    mode.drawSpecialGrid(graphics, items, x + cx * 16 + 1, y + cz * 16 + 1, 16, 16);
                }
            }
        }
        TransformTexture arrow = ARROW.rotate(this.direction / 2);
        arrow.draw(graphics, 0, 0, x + playerXGui - 20, y + playerYGui - 20, 40, 40);

        // draw red vertical line
        if (playerXGui % 16 > 7 || playerXGui % 16 == 0) {
            DrawerHelper.drawSolidRect(graphics, x + playerXGui - 1, y, 1, imageHeight, ColorPattern.RED.color);
        } else {
            DrawerHelper.drawSolidRect(graphics, x + playerXGui, y, 1, imageHeight, ColorPattern.RED.color);
        }
        // draw red horizontal line
        if (playerYGui % 16 > 7 || playerYGui % 16 == 0) {
            DrawerHelper.drawSolidRect(graphics, x, y + playerYGui - 1, imageWidth, 1, ColorPattern.RED.color);
        } else {
            DrawerHelper.drawSolidRect(graphics, x, y + playerYGui, imageWidth, 1, ColorPattern.RED.color);
        }
    }

    public void load(ResourceManager resourceManager) throws IOException {}

    public void setDarkMode(boolean darkMode) {
        if (this.darkMode != darkMode) {
            this.darkMode = darkMode;
            load();
        }
    }

    public void setSelected(String uniqueID) {
        if (!this.selected.equals(uniqueID)) {
            this.selected = uniqueID;
            load();
        }
    }
}
