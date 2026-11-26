package com.gregtechceu.gtceu.common.mui.widgets.prospector;

import com.gregtechceu.gtceu.api.item.component.prospector.ProspectingUpdatePacket;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.utils.GradientUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Array;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ProspectorMapTexture<T> extends AbstractTexture implements IDrawable {

    private static final UITexture ARROW = GuiTextures.PLAY.withColorOverride(0xFFFF0000);
    private static final Quaternionf rotationQuat = new Quaternionf();

    private final ProspectorMapHandler<T> mapHandler;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    public final T[][][] data;
    private final T[] emptyCell;

    public ProspectorMapTexture(ProspectorMapHandler<T> mapHandler) {
        this.mapHandler = mapHandler;

        ProspectorMode<T> mode = mapHandler.getMode();
        int diameter = mapHandler.getChunkRadius() * 2 - 1;

        this.imageWidth = this.imageHeight = diameter * 16 + 1;
        // noinspection unchecked
        this.data = (T[][][]) Array.newInstance(mode.getItemClass(),
                diameter * mode.cellSize, diameter * mode.cellSize, 0);
        // noinspection unchecked
        this.emptyCell = (T[]) Array.newInstance(mode.getItemClass(), 0);
    }

    private T[] applySelectionFilter(T @Nullable [] items) {
        if (items == null || items.length == 0) return this.emptyCell;

        String selected = mapHandler.getSelected();
        if (selected == null) return items;

        ProspectorMode<T> mode = mapHandler.getMode();
        int matches = 0;
        for (T item : items) {
            if (selected.equals(mode.getUniqueId(item))) matches++;
        }
        if (matches == items.length) return items;
        if (matches == 0) return this.emptyCell;

        // noinspection unchecked
        T[] filtered = (T[]) Array.newInstance(mode.getItemClass(), matches);
        int i = 0;
        for (T item : items) {
            if (selected.equals(mode.getUniqueId(item))) filtered[i++] = item;
        }
        return filtered;
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
                // the last row/column is the closing grid line, past the end of the data
                boolean edge = x == this.imageWidth - 1 || z == this.imageHeight - 1;

                boolean drewColor = false;
                // draw items
                if (!edge) {
                    T[] items = this.data[x * mode.cellSize / 16][z * mode.cellSize / 16];
                    for (T item : items) {
                        if (mapHandler.getSelected() == null ||
                                mapHandler.getSelected().equals(mode.getUniqueId(item))) {
                            int color = mode.getItemColor(item);
                            image.setPixelRGBA(x, z, GradientUtil.argbToAbgr(color) | 0xFF000000);

                            drewColor = true;
                            break;
                        }
                    }
                }
                if (!drewColor) {
                    // draw background color
                    image.setPixelRGBA(x, z, (mapHandler.isDarkMode() ? 0xFF666666 : 0xFFFFFFFF));
                }
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
        final ProspectorMode<T> mode = mapHandler.getMode();
        final int diameter = mapHandler.getChunkRadius() * 2 - 1;
        for (int cx = 0; cx < diameter; cx++) {
            for (int cz = 0; cz < diameter; cz++) {
                T[] items = applySelectionFilter(this.data[cx * mode.cellSize][cz * mode.cellSize]);
                if (items.length == 0) continue;

                mode.drawSpecialGrid(context, items, x + cx * 16 + 1, y + cz * 16 + 1, 15, 15);
            }
        }

        Player player = this.mapHandler.getPlayer();
        // the map is anchored to the chunk the player was in when it was opened, not to the one they're in now
        ChunkPos mapChunkPos = this.mapHandler.getPlayerChunkPos();
        int chunkRadius = this.mapHandler.getChunkRadius();

        float playerRotationDeg = ((player.getVisualRotationYInDegrees() % 360.0f) + 180f) - 90.0f;
        int playerXGui = player.getBlockX() - (mapChunkPos.x - chunkRadius + 1) * 16;
        int playerYGui = player.getBlockZ() - (mapChunkPos.z - chunkRadius + 1) * 16;
        if (playerXGui < 0 || playerXGui >= diameter * 16 || playerYGui < 0 || playerYGui >= diameter * 16) {
            return;
        }

        PoseStack poseStack = context.graphicsPose();

        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        poseStack.pushPose();
        poseStack.translate(x + playerXGui, y + playerYGui, 0f);
        poseStack.mulPose(rotationQuat.rotationZ(Mth.DEG_TO_RAD * playerRotationDeg));
        poseStack.translate(-5.f, -5.f, 0.0f);

        ARROW.draw(context, 0, 0, 10, 10, widgetTheme);
        poseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {}
}
