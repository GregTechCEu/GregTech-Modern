package com.gregtechceu.gtceu.api.gui.texture;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.reflect.Array;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Environment(value= EnvType.CLIENT)
public class ProspectingTexture extends AbstractTexture {
    public static final String SELECTED_ALL = "[all]";
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

    public ProspectingTexture(int playerChunkX, int playerChunkZ, int posX, int posZ, float direction, ProspectorMode mode, int radius, boolean darkMode) {
        this.darkMode = darkMode;
        this.radius = radius;
        this.mode = mode;
        this.data = (Object[][][]) Array.newInstance(mode.getItemClass(), (radius * 2 - 1) * mode.cellSize, (radius * 2 - 1) * mode.cellSize, 0);
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
            System.arraycopy(packet.data[x], 0, data[x + currentColumn * mode.cellSize], currentRow * mode.cellSize, mode.cellSize);
        }
        load();
    }

    private NativeImage getImage() {
        int wh = (this.radius * 2 - 1) * 16;
        NativeImage image = new NativeImage(wh, wh, false);
        for (int i = 0; i < wh; i++) {
            for (int j = 0; j < wh; j++) {
                var items = this.data[i * mode.cellSize / 16][j * mode.cellSize / 16];
                // draw bg
                image.setPixelRGBA(i, j, (darkMode ? ColorPattern.GRAY.color : ColorPattern.WHITE.color));
                //draw items
                for (var item : items) {
                    if (!selected.equals(SELECTED_ALL) && !selected.equals(mode.getUniqueID(item))) continue;
                    var color = mode.getItemColor(item);
                    image.setPixelRGBA(i, j,  NativeImage.combine(255, ColorUtils.blueI(color), ColorUtils.greenI(color), ColorUtils.redI(color)));
                    break;
                }
                // draw grid
                if ((i) % 16 == 0 || (j) % 16 == 0) {
                    image.setPixelRGBA(i, j, ColorUtils.averageColor(image.getPixelRGBA(i, j), 0xff000000));
                }
            }
        }
        return image;
    }

    public void load() {
        doLoad(getImage());
    }

    private void doLoad(NativeImage image) {
        TextureUtil.prepareImage(this.getId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, true);
    }

    public void draw(PoseStack poseStack, int x, int y) {
        if (this.getId() == -1) return;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.getId());
        Matrix4f matrix4f = poseStack.last().pose();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix4f, x, y + imageHeight, 0).uv(0, 1).color(-1).endVertex();
        bufferbuilder.vertex(matrix4f, x + imageWidth, y + imageHeight, 0).uv(1, 1).color(-1).endVertex();
        bufferbuilder.vertex(matrix4f, x + imageWidth, y, 0).uv(1, 0).color(-1).endVertex();
        bufferbuilder.vertex(matrix4f, x, y, 0).uv(0, 0).color(-1).endVertex();
        tessellator.end();

        //draw special grid (e.g. fluid)
        for (int cx = 0; cx < radius * 2 - 1; cx++) {
            for (int cz = 0; cz < radius * 2 - 1; cz++) {
                if (this.data[cx][cz] != null && this.data[cx][cz].length > 0) {
                    var items = this.data[cx][cz];
                    mode.drawSpecialGrid(poseStack, items, x + cx * 16 + 1, y + cz * 16 + 1, 16, 16);
                }
            }
        }

        GuiTextures.UP.copy().setColor(ColorPattern.RED.color).rotate(direction / 2).draw(poseStack, 0, 0, x + playerXGui - 20, y + playerYGui - 20, 40, 40);

        //draw red vertical line
        if (playerXGui % 16 > 7 || playerXGui % 16 == 0) {
            DrawerHelper.drawSolidRect(poseStack, x + playerXGui - 1, y, 1, imageHeight, ColorPattern.RED.color);
        } else {
            DrawerHelper.drawSolidRect(poseStack, x + playerXGui, y, 1, imageHeight, ColorPattern.RED.color);
        }
        //draw red horizontal line
        if (playerYGui % 16 > 7 || playerYGui % 16 == 0) {
            DrawerHelper.drawSolidRect(poseStack, x, y + playerYGui - 1, imageWidth, 1, ColorPattern.RED.color);
        } else {
            DrawerHelper.drawSolidRect(poseStack, x, y + playerYGui, imageWidth, 1, ColorPattern.RED.color);
        }
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {

    }

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
