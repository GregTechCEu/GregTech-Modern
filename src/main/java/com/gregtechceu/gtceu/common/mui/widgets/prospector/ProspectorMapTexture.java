package com.gregtechceu.gtceu.common.mui.widgets.prospector;

import com.gregtechceu.gtceu.api.mui.prospector.ProspectingUpdatePacket;
import com.gregtechceu.gtceu.api.mui.prospector.ProspectorMode;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ProspectorMapTexture<T> extends AbstractTexture implements IDrawable {

    private final ProspectorMapHandler<T> mapHandler;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    public final T[][][] data;

    @Getter
    private @Nullable String selected = null;
    @Getter
    private boolean darkMode = false;

    public ProspectorMapTexture(ProspectorMapHandler<T> mapHandler, ChunkPos playerChunkPos) {
        this.mapHandler = mapHandler;

        int diameter = mapHandler.getChunkRadius() * 2 - 1;
        ProspectorMode<T> mode = mapHandler.getMode();

        this.imageWidth = this.imageHeight = diameter * 16;
        // noinspection unchecked
        this.data = (T[][][]) Array.newInstance(mode.getItemClass(),
                diameter * mode.cellSize, diameter * mode.cellSize, 0);
    }

    public void toggleDarkMode() {
        this.darkMode = !this.darkMode;
    }

    public void updateTexture(ProspectingUpdatePacket<T> packet) {
        int ox = packet.chunkX - mapHandler.getPlayerChunkPos().x;
        int oz = packet.chunkZ - mapHandler.getPlayerChunkPos().z;

        int currentColumn = (mapHandler.getChunkRadius() - 1) + ox;
        int currentRow = (mapHandler.getChunkRadius() - 1) + oz;
        if (currentRow < 0) {
            return;
        }

        ProspectorMode<T> mode = mapHandler.getMode();
        for (int x = 0; x < mode.cellSize; x++) {
            System.arraycopy(packet.data[x], 0, data[x + currentColumn * mode.cellSize], currentRow * mode.cellSize,
                    mode.cellSize);
        }

        loadToImage();
    }

    private NativeImage getImage() {
        ProspectorMode<T> mode = mapHandler.getMode();
        NativeImage image = new NativeImage(this.imageWidth, this.imageHeight, false);

        for (int x = 0; x < this.imageWidth; x++) {
            for (int z = 0; z < this.imageHeight; z++) {
                T[] items = this.data[x * mode.cellSize / 16][z * mode.cellSize / 16];
                // draw background color
                image.setPixelRGBA(x, z, (darkMode ? 0xFF666666 : 0xFFFFFFFF));

                // draw items
                for (T item : items) {
                    if (selected != null && !selected.equals(mode.getUniqueId(item))) continue;

                    int color = mode.getItemColor(item);
                    image.setPixelRGBA(x, z, GradientUtil.argbToAbgr(color) | 0xFF000000);
                    break;
                }
                // draw grid
                if (x % 16 == 0 || z % 16 == 0) {
                    image.blendPixel(x, z, 0xFF000000);
                }
            }
        }

        return image;
    }

    public void loadToImage() {
        NativeImage image = getImage();
        TextureUtil.prepareImage(this.getId(), image.getWidth(), image.getHeight());
        // the last parameter is actually autoClose, it's named wrong.
        image.upload(0, 0, 0, true);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        // getId() generates a new texture ID if it's NOT_ASSIGNED, so we shouldn't use that.
        if (this.id == NOT_ASSIGNED) return;

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, this.getId());
        GuiDraw.drawTexture(context.getLastGraphicsPose(), x, y, x + width, y + height, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
        RenderSystem.disableBlend();

        // draw special grid (e.g. fluid)
        final int diameter = mapHandler.getChunkRadius() * 2 - 1;
        for (int cx = 0; cx < diameter; cx++) {
            for (int cz = 0; cz < diameter; cz++) {
                if (this.data[cx][cz] != null && this.data[cx][cz].length > 0) {
                    var items = this.data[cx][cz];
                    mapHandler.getMode().drawSpecialGrid(context, items, x + cx * 16 + 1, y + cz * 16 + 1, 16, 16);
                }
            }
        }

        // player rotation & red lines are drawn separately
        /*

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
         */
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {}

    public void setDarkMode(boolean darkMode) {
        if (this.darkMode != darkMode) {
            this.darkMode = darkMode;
            loadToImage();
        }
    }

    public void setSelected(String uniqueID) {
        if (!Objects.equals(this.selected, uniqueID)) {
            this.selected = uniqueID;
            loadToImage();
        }
    }
}
