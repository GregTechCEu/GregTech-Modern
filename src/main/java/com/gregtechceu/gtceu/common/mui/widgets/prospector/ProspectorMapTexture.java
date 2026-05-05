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
import org.joml.Quaternionf;

import java.io.IOException;
import java.lang.reflect.Array;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ProspectorMapTexture<T> extends AbstractTexture implements IDrawable {

    private static final UITexture ARROW = GuiTextures.PLAY;
    private static final Quaternionf rotationQuat = new Quaternionf();

    private final ProspectorMapHandler<T> mapHandler;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    public final T[][][] data;

    @Getter
    private boolean darkMode = true;

    public ProspectorMapTexture(ProspectorMapHandler<T> mapHandler) {
        this.mapHandler = mapHandler;

        ProspectorMode<T> mode = mapHandler.getMode();
        int diameter = mapHandler.getChunkRadius() * 2 - 1;

        this.imageWidth = this.imageHeight = diameter * 16;
        // noinspection unchecked
        this.data = (T[][][]) Array.newInstance(mode.getItemClass(),
                diameter * mode.cellSize, diameter * mode.cellSize, 0);
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

                boolean drewColor = false;
                // draw items
                for (T item : items) {
                    if (mapHandler.getSelected() == null || mapHandler.getSelected().equals(mode.getUniqueId(item))) {
                        int color = mode.getItemColor(item);
                        image.setPixelRGBA(x, z, GradientUtil.argbToAbgr(color) | 0xFF000000);

                        drewColor = true;
                        break;
                    }
                }
                if (!drewColor) {
                    // draw background color
                    image.setPixelRGBA(x, z, (darkMode ? 0xFF666666 : 0xFFFFFFFF));
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

        Player player = this.mapHandler.getPlayer();
        ChunkPos playerChunkPos = player.chunkPosition();
        int chunkRadius = this.mapHandler.getChunkRadius();

        float playerRotationDeg = (player.getVisualRotationYInDegrees() + 180f) / 2f;
        int playerXGui = player.getBlockX() - (playerChunkPos.x - chunkRadius + 1) * 16;
        int playerYGui = player.getBlockZ() - (playerChunkPos.z - chunkRadius + 1) * 16;

        // these are the same colors the F3 debug overlay uses for X and Z
        // draw red vertical line
        GuiDraw.drawRect(context.getGraphics(), x + playerXGui, y, 1, imageHeight, 0xFFFF0000);
        // draw bluish horizontal line
        GuiDraw.drawRect(context.getGraphics(), x, y + playerYGui,  imageWidth, 1, 0xFF7F7FFF);

        PoseStack poseStack = context.graphicsPose();

        poseStack.pushPose();
        poseStack.mulPose(rotationQuat.rotationZ(Mth.DEG_TO_RAD * playerRotationDeg));
        ARROW.draw(context, x + playerXGui - 20, y + playerYGui - 20, 40, 40);
        poseStack.popPose();
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {}

    public void setDarkMode(boolean darkMode) {
        if (this.darkMode != darkMode) {
            this.darkMode = darkMode;
            loadToImage();
        }
    }
}
