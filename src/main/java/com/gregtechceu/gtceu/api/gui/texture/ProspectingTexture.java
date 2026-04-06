package com.gregtechceu.gtceu.api.gui.texture;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.IOException;
import java.lang.reflect.Array;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ProspectingTexture<T> extends AbstractTexture {

    private static final ResourceTexture ARROW = GuiTextures.UP.copy().setColor(ColorPattern.RED.color);

    @Getter
    private @Nullable String selected = null;
    private boolean darkMode;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    public final T[][][] data;
    private final int playerXGui;
    private final int playerYGui;
    private final float direction;
    private final int playerChunkX;
    private final int playerChunkZ;
    private final ProspectorMode<T> mode;
    private final int chunkRadius;

    public ProspectingTexture(int playerChunkX, int playerChunkZ, int playerBlockX, int playerBlockZ, float direction,
                              ProspectorMode<T> mode, int chunkRadius, boolean darkMode) {
        this.darkMode = darkMode;
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        // noinspection unchecked
        this.data = (T[][][]) Array.newInstance(mode.getItemClass(), (chunkRadius * 2 - 1) * mode.cellSize,
                (chunkRadius * 2 - 1) * mode.cellSize, 0);
        this.imageWidth = (chunkRadius * 2 - 1) * 16;
        this.imageHeight = (chunkRadius * 2 - 1) * 16;
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
        this.direction = (direction + 180) % 360;
        this.playerXGui = playerBlockX - (playerChunkX - this.chunkRadius + 1) * 16 + (playerBlockX > 0 ? 1 : 0);
        this.playerYGui = playerBlockZ - (playerChunkZ - this.chunkRadius + 1) * 16 + (playerBlockX > 0 ? 1 : 0);
    }

    public void updateTexture(PacketProspecting<T> packet) {
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

        int currentColumn = (this.chunkRadius - 1) + ox;
        int currentRow = (this.chunkRadius - 1) + oy;
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
        NativeImage image = new NativeImage(this.imageWidth, this.imageHeight, false);

        for (int x = 0; x < this.imageWidth; x++) {
            for (int y = 0; y < this.imageHeight; y++) {
                T[] items = this.data[x * mode.cellSize / 16][y * mode.cellSize / 16];
                // draw background
                image.setPixelRGBA(x, y, (darkMode ? 0xFF666666 : 0xFFFFFFFF));
                // draw items
                for (T item : items) {
                    if (selected != null && !selected.equals(mode.getUniqueId(item))) continue;
                    int color = mode.getItemColor(item);
                    // this is actually ARGB, even though the method name says RGBA and the parameter says ABGR.
                    image.setPixelRGBA(x, y, FastColor.ABGR32.opaque(color));
                    break;
                }
                // draw grid
                if (x % 16 == 0 || y % 16 == 0) {
                    image.blendPixel(x, y, 0xFF000000);
                    image.setPixelRGBA(x, y, ColorUtils.averageColor(image.getPixelRGBA(x, y), 0xFF000000));
                }
            }
        }
        return image;
    }

    public void load() {
        doLoad(getImage());
    }

    private void doLoad(NativeImage image) {
        TextureUtil.prepareImage(this.getId(), image.getWidth(), image.getHeight());
        // the last parameter is actually autoClose, it's named wrong.
        image.upload(0, 0, 0, true);
    }

    public void draw(GuiGraphics graphics, int x, int y) {
        // getId() generates a new texture ID if it's NOT_ASSIGNED, so we shouldn't use that.
        if (this.id == NOT_ASSIGNED) return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.getId());

        Matrix4f pose = graphics.pose().last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        bufferbuilder.vertex(pose, x, y + imageHeight, 0).uv(0, 1).color(0xFFFFFFFF).endVertex();
        bufferbuilder.vertex(pose, x + imageWidth, y + imageHeight, 0).uv(1, 1).color(0xFFFFFFFF).endVertex();
        bufferbuilder.vertex(pose, x + imageWidth, y, 0).uv(1, 0).color(0xFFFFFFFF).endVertex();
        bufferbuilder.vertex(pose, x, y, 0).uv(0, 0).color(0xFFFFFFFF).endVertex();
        tesselator.end();

        // draw special grid (e.g. fluid)
        for (int cx = 0; cx < chunkRadius * 2 - 1; cx++) {
            for (int cz = 0; cz < chunkRadius * 2 - 1; cz++) {
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

    @Override
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
