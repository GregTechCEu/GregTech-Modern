package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.screen.event.RichTooltipEvent;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.math.NumberFormat;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiConsumer;

import static brachy.modularui.drawable.UITexture.GUI_TEXTURE_ID_CONVERTER;
import static net.minecraft.util.Mth.HALF_PI;
import static net.minecraft.util.Mth.TWO_PI;

public class GuiDraw {

    private static final TextRenderer textRenderer = new TextRenderer();

    public static void drawRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h, int color) {
        Matrix4f pose = new Matrix4f();
        var builder = new GuiShapeBuilder(GuiShapeRenderState.Topology.QUADS);
        drawRectRaw(builder, pose, x0, y0, x0 + w, y0 + h, color);
        builder.submit(graphics);
    }

    public static void drawHorizontalGradientRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                  int colorLeft, int colorRight) {
        drawRect(graphics, x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight);
    }

    public static void drawVerticalGradientRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                int colorTop, int colorBottom) {
        drawRect(graphics, x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom);
    }

    public static void drawRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                int colorTL, int colorTR, int colorBL, int colorBR) {
        Matrix4f pose = new Matrix4f();
        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.QUADS);

        float x1 = x0 + w, y1 = y0 + h;
        buffer.addVertex(pose, x0, y0, 0.0f)
                .setColor(Color.getRed(colorTL), Color.getGreen(colorTL), Color.getBlue(colorTL), Color.getAlpha(colorTL));
        buffer.addVertex(pose, x0, y1, 0.0f)
                .setColor(Color.getRed(colorBL), Color.getGreen(colorBL), Color.getBlue(colorBL), Color.getAlpha(colorBL));
        buffer.addVertex(pose, x1, y1, 0.0f)
                .setColor(Color.getRed(colorBR), Color.getGreen(colorBR), Color.getBlue(colorBR), Color.getAlpha(colorBR));
        buffer.addVertex(pose, x1, y0, 0.0f)
                .setColor(Color.getRed(colorTR), Color.getGreen(colorTR), Color.getBlue(colorTR), Color.getAlpha(colorTR));
        buffer.submit(graphics);
    }

    public static void drawRectRaw(VertexConsumer buffer, Matrix4f pose, float x0, float y0, float x1, float y1,
                                   int color) {
        int r = Color.getRed(color);
        int g = Color.getGreen(color);
        int b = Color.getBlue(color);
        int a = Color.getAlpha(color);
        drawRectRaw(buffer, pose, x0, y0, x1, y1, r, g, b, a);
    }

    public static void drawRectRaw(VertexConsumer buffer, Matrix4f pose, float x0, float y0, float x1, float y1,
                                   int r, int g, int b, int a) {
        buffer.addVertex(pose, x0, y0, 0.0f).setColor(r, g, b, a);
        buffer.addVertex(pose, x0, y1, 0.0f).setColor(r, g, b, a);
        buffer.addVertex(pose, x1, y1, 0.0f).setColor(r, g, b, a);
        buffer.addVertex(pose, x1, y0, 0.0f).setColor(r, g, b, a);
    }

    public static void drawCircle(GuiGraphicsExtractor graphics, float x0, float y0, float diameter, int color, int segments) {
        drawEllipse(graphics, x0, y0, diameter, diameter, color, color, segments);
    }

    public static void drawCircle(GuiGraphicsExtractor graphics, float x0, float y0, float diameter,
                                  int centerColor, int outerColor, int segments) {
        drawEllipse(graphics, x0, y0, diameter, diameter, centerColor, outerColor, segments);
    }

    public static void drawEllipse(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                   int color, int segments) {
        drawEllipse(graphics, x0, y0, w, h, color, color, segments);
    }

    public static void drawEllipse(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                   int centerColor, int outerColor, int segments) {
        if (segments < 3) throw new IllegalArgumentException("An ellipse needs at least three segments");
        Matrix4f pose = new Matrix4f();
        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_FAN);

        float x_2 = x0 + w / 2f, y_2 = y0 + h / 2f;
        // start at center
        buffer.addVertex(pose, x_2, y_2, 0.0f)
                .setColor(Color.getRed(centerColor), Color.getGreen(centerColor), Color.getBlue(centerColor),
                        Color.getAlpha(centerColor));
        int a = Color.getAlpha(outerColor), r = Color.getRed(outerColor), g = Color.getGreen(outerColor),
                b = Color.getBlue(outerColor);
        float incr = TWO_PI / segments;
        for (int i = 0; i <= segments; i++) {
            float angle = incr * i;
            float x = Mth.sin(angle) * (w / 2) + x_2;
            float y = Mth.cos(angle) * (h / 2) + y_2;
            buffer.addVertex(x, y, 0.0f).setColor(r, g, b, a);
        }
        buffer.submit(graphics);
    }

    public static void drawRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h, int color,
                                       int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, color, color, color, color, cornerRadius, segments);
    }

    public static void drawVerticalGradientRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                       int colorTop, int colorBottom, int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, colorTop, colorTop, colorBottom, colorBottom, cornerRadius, segments);
    }

    public static void drawHorizontalGradientRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                                         int colorLeft, int colorRight, int cornerRadius, int segments) {
        drawRoundedRect(graphics, x0, y0, w, h, colorLeft, colorRight, colorLeft, colorRight, cornerRadius, segments);
    }

    public static void drawRoundedRect(GuiGraphicsExtractor graphics, float x0, float y0, float w, float h,
                                       int colorTL, int colorTR, int colorBL, int colorBR,
                                       int cornerRadius, int segments) {
        if (segments < 1) throw new IllegalArgumentException("A rounded corner needs at least one segment");
        cornerRadius = Math.max(0, Math.min(cornerRadius, (int) (Math.min(w, h) / 2)));
        Matrix4f pose = new Matrix4f();
        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_FAN);

        float x1 = x0 + w, y1 = y0 + h;
        int color = Color.average(colorBL, colorBR, colorTR, colorTL);
        // start at center
        buffer.addVertex(pose, x0 + w / 2f, y0 + h / 2f, 0.0f).setColor(color);
        // left side
        buffer.addVertex(pose, x0, y0 + cornerRadius, 0.0f).setColor(colorTL);
        buffer.addVertex(pose, x0, y1 - cornerRadius, 0.0f)
                .setColor(colorBL);
        // bottom left corner
        for (int i = 1; i <= segments; i++) {
            float x = x0 + cornerRadius - Mth.cos(HALF_PI / segments * i) * cornerRadius;
            float y = y1 - cornerRadius + Mth.sin(HALF_PI / segments * i) * cornerRadius;
            buffer.addVertex(x, y, 0.0f).setColor(colorBL);
        }
        // bottom side
        buffer.addVertex(pose, x1 - cornerRadius, y1, 0.0f).setColor(colorBR);
        // bottom right corner
        for (int i = 1; i <= segments; i++) {
            float x = x1 - cornerRadius + Mth.sin(HALF_PI / segments * i) * cornerRadius;
            float y = y1 - cornerRadius + Mth.cos(HALF_PI / segments * i) * cornerRadius;
            buffer.addVertex(pose, x, y, 0.0f).setColor(colorBR);
        }
        // right side
        buffer.addVertex(pose, x1, y0 + cornerRadius, 0.0f).setColor(colorTR);
        // top right corner
        for (int i = 1; i <= segments; i++) {
            float x = x1 - cornerRadius + Mth.cos(HALF_PI / segments * i) * cornerRadius;
            float y = y0 + cornerRadius - Mth.sin(HALF_PI / segments * i) * cornerRadius;
            buffer.addVertex(pose, x, y, 0.0f).setColor(colorTR);
        }
        // top side
        buffer.addVertex(pose, x0 + cornerRadius, y0, 0.0f).setColor(colorTL);
        // top left corner
        for (int i = 1; i <= segments; i++) {
            float x = x0 + cornerRadius - Mth.sin(HALF_PI / segments * i) * cornerRadius;
            float y = y0 + cornerRadius - Mth.cos(HALF_PI / segments * i) * cornerRadius;
            buffer.addVertex(pose, x, y, 0.0f).setColor(colorTL);
        }
        buffer.addVertex(pose, x0, y0 + cornerRadius, 0.0f).setColor(colorTL);
        buffer.submit(graphics);
    }

    /** Resolves the atlas and UVs together, without changing global texture bindings. */
    private record TextureRegion(Identifier texture, float u0, float v0, float u1, float v1) {}

    private static TextureRegion resolveTexture(Identifier location, float u0, float v0, float u1, float v1) {
        TextureAtlasSprite sprite = GuiSpriteManager.getInstance()
                .getSprite(GUI_TEXTURE_ID_CONVERTER.fileToId(location));
        if (!sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            return new TextureRegion(sprite.atlasLocation(), sprite.getU(u0), sprite.getV(v0),
                    sprite.getU(u1), sprite.getV(v1));
        }
        return new TextureRegion(location, u0, v0, u1, v1);
    }

    public static boolean isGuiAtlasSprite(Identifier location) {
        TextureAtlasSprite sprite = GuiSpriteManager.getInstance()
                .getSprite(GUI_TEXTURE_ID_CONVERTER.fileToId(location));
        return !sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, Identifier location,
                                    float x0, float y0, float x1, float y1,
                                    float u0, float v0, float u1, float v1) {
        drawTexture(graphics, location, x0, y0, x1, y1, u0, v0, u1, v1, true);
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, Identifier location,
                                    float x0, float y0, float x1, float y1,
                                    float u0, float v0, float u1, float v1, boolean blend) {
        TextureRegion region = resolveTexture(location, u0, v0, u1, v1);
        GuiTextureRenderState.submit(graphics, region.texture(), x0, y0, x1, y1,
                region.u0(), region.v0(), region.u1(), region.v1(), blend);
    }

    /** Draws a texture using pixel UV coordinates. */
    public static void drawTexture(GuiGraphicsExtractor graphics, Identifier location,
                                    float x, float y, float w, float h,
                                    int u, int v, int textureWidth, int textureHeight) {
        drawTexture(graphics, location, x, y, x + w, y + h,
                (float) u / textureWidth, (float) v / textureHeight,
                (u + w) / textureWidth, (v + h) / textureHeight);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor graphics, Identifier location,
                                         float x, float y, float w, float h,
                                         int u, int v, int tileWidth, int tileHeight,
                                         int textureWidth, int textureHeight, float z) {
        drawTiledTexture(graphics, location, x, y, w, h,
                (float) u / textureWidth, (float) v / textureHeight,
                (float) (u + tileWidth) / textureWidth, (float) (v + tileHeight) / textureHeight,
                tileWidth, tileHeight, z);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor graphics, Identifier location,
                                         float x, float y, float w, float h,
                                         float u0, float v0, float u1, float v1,
                                         int tileWidth, int tileHeight, float z) {
        drawTiledTexture(graphics, location, x, y, w, h, u0, v0, u1, v1, tileWidth, tileHeight, true);
    }

    /** Tiles a region, clipping the final row and column in both position and UV space. */
    public static void drawTiledTexture(GuiGraphicsExtractor graphics, Identifier location,
                                         float x, float y, float w, float h,
                                         float u0, float v0, float u1, float v1,
                                         int tileWidth, int tileHeight, boolean blend) {
        if (tileWidth <= 0 || tileHeight <= 0) throw new IllegalArgumentException("Tile dimensions must be positive");
        if (w <= 0 || h <= 0) return;
        TextureRegion region = resolveTexture(location, u0, v0, u1, v1);
        int columns = (int) Math.ceil(w / tileWidth);
        int rows = (int) Math.ceil(h / tileHeight);
        for (int row = 0; row < rows; row++) {
            float dy = row * (float) tileHeight;
            float dh = Math.min(tileHeight, h - dy);
            float endV = region.v0() + (region.v1() - region.v0()) * dh / tileHeight;
            for (int column = 0; column < columns; column++) {
                float dx = column * (float) tileWidth;
                float dw = Math.min(tileWidth, w - dx);
                float endU = region.u0() + (region.u1() - region.u0()) * dw / tileWidth;
                GuiTextureRenderState.submit(graphics, region.texture(), x + dx, y + dy, x + dx + dw, y + dy + dh,
                        region.u0(), region.v0(), endU, endV, blend);
            }
        }
    }

    /** Extracts an entity preview now; the entity itself is never retained for deferred rendering. */
    public static <T extends Entity> void drawEntity(GuiGraphicsExtractor graphics, T entity,
                                                      float x, float y, float w, float h, float z,
                                                      @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                      @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        extractEntityPreview(graphics, entity, x, y, w, h, null, null, preDraw, postDraw);
    }

    public static <T extends Entity> void drawEntityLookingAtMouse(GuiGraphicsExtractor graphics, T entity,
                                                                    float x, float y, float w, float h, float z,
                                                                    int mouseX, int mouseY,
                                                                    @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                                    @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        if (w <= 0 || h <= 0) return;
        float xAngle = (float) Math.atan((x + w / 2 - mouseX) / h);
        float yAngle = (float) Math.atan((y + h / 2 - mouseY) / h);
        drawEntityLookingAtAngle(graphics, entity, x, y, w, h, z, xAngle, yAngle, preDraw, postDraw);
    }

    public static <T extends Entity> void drawEntityLookingAtAngle(GuiGraphicsExtractor graphics, T entity,
                                                                    float x, float y, float w, float h, float z,
                                                                    float xAngle, float yAngle,
                                                                    @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                                    @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        extractEntityPreview(graphics, entity, x, y, w, h, xAngle, yAngle, preDraw, postDraw);
    }

    private static <T extends Entity> void extractEntityPreview(GuiGraphicsExtractor graphics, T entity,
                                                                float x, float y, float w, float h,
                                                                @Nullable Float xAngle, @Nullable Float yAngle,
                                                                @Nullable BiConsumer<GuiGraphicsExtractor, T> preDraw,
                                                                @Nullable BiConsumer<GuiGraphicsExtractor, T> postDraw) {
        if (w <= 0 || h <= 0) return;
        float oldYRot = entity.getYRot(), oldYRotO = entity.yRotO;
        float oldXRot = entity.getXRot(), oldXRotO = entity.xRotO;
        LivingEntity living = entity instanceof LivingEntity l ? l : null;
        float oldBody = living == null ? 0 : living.yBodyRot;
        float oldBodyO = living == null ? 0 : living.yBodyRotO;
        float oldHead = living == null ? 0 : living.yHeadRot;
        float oldHeadO = living == null ? 0 : living.yHeadRotO;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x, y);
            if (xAngle != null && yAngle != null) {
                entity.setYRot(entity.yRotO = 180 + xAngle * 40);
                entity.setXRot(entity.xRotO = -yAngle * 20);
                if (living != null) {
                    living.yBodyRot = living.yBodyRotO = 180 + xAngle * 20;
                    living.yHeadRot = living.yHeadRotO = entity.getYRot();
                }
            }
            try {
                if (preDraw != null) preDraw.accept(graphics, entity);
                var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                var state = renderer.createRenderState(entity, 1);
                state.shadowPieces.clear();
                state.outlineColor = 0;
                state.lightCoords = 15728880;
                float scale = Math.min(w / Math.max(0.001f, state.boundingBoxWidth),
                        h / Math.max(0.001f, state.boundingBoxHeight));
                Quaternionf camera = yAngle == null ? null : new Quaternionf().rotateX(yAngle * 20 * Mth.DEG_TO_RAD);
                Quaternionf rotation = new Quaternionf().rotateZ(Mth.PI);
                if (camera != null) rotation.mul(camera);
                int right = (int) Math.ceil(w), bottom = (int) Math.ceil(h);
                var bounds = new net.minecraft.client.gui.navigation.ScreenRectangle(0, 0, right, bottom)
                        .transformMaxBounds(graphics.pose());
                var scissor = graphics.peekScissorStack();
                if (scissor != null) bounds = bounds.intersection(scissor);
                if (bounds != null) {
                    graphics.submitPictureInPictureRenderState(new GuiEntityPreviewState(state,
                            new Vector3f((w - right) / (2 * scale),
                                    state.boundingBoxHeight / 2 + (h - bottom) / (2 * scale), 0),
                            rotation, camera, graphics.pose(), 0, 0, right, bottom, scale, scissor, bounds));
                }
            } finally {
                if (postDraw != null) postDraw.accept(graphics, entity);
            }
        } finally {
            if (xAngle != null) {
                entity.setYRot(oldYRot);
                entity.yRotO = oldYRotO;
                entity.setXRot(oldXRot);
                entity.xRotO = oldXRotO;
                if (living != null) {
                    living.yBodyRot = oldBody;
                    living.yBodyRotO = oldBodyO;
                    living.yHeadRot = oldHead;
                    living.yHeadRotO = oldHeadO;
                }
            }
            graphics.pose().popMatrix();
        }
    }

    public static void drawItem(GuiGraphicsExtractor graphics, ItemStack item, int x, int y, float width, float height, int z) {
        if (item.isEmpty()) return;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x, y);
            graphics.pose().scale(width / 16f, height / 16f);
            graphics.item(item, 0, 0);
            graphics.itemDecorations(Minecraft.getInstance().font, item, 0, 0);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void drawFluidTexture(GuiGraphicsExtractor graphics, FluidStack content,
                                        float x0, float y0, float width, float height, float z) {
        if (content == null || content.isEmpty()) {
            return;
        }
        Fluid fluid = content.getFluid();
        Identifier fluidStill = IClientFluidTypeExtensions.of(fluid).getStillTexture(content);
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                .getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getSprite(fluidStill);
        int fluidColor = IClientFluidTypeExtensions.of(fluid).getTintColor(content);
        int previousTint = GuiTint.get();
        try {
            GuiTint.set(net.minecraft.util.ARGB.multiply(previousTint, fluidColor));
            drawTiledTexture(graphics, sprite.atlasLocation(), x0, y0, width, height,
                    sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                    sprite.contents().width(), sprite.contents().height(), z);
        } finally {
            GuiTint.set(previousTint);
        }
    }

    public static void drawStandardSlotAmountText(GuiContext context, int amount, String format, Area area,
                                                  float z) {
        drawAmountText(context.getMuiContext(), amount, format, 0, 0, area.width, area.height, Alignment.BottomRight, z);
    }

    public static void drawAmountText(ModularGuiContext context, int amount, String format,
                                      int x, int y, int width, int height, Alignment alignment, float z) {
        if (amount <= 1) return;
        String amountText = NumberFormat.AMOUNT_TEXT.format(amount);
        if (format != null) {
            amountText = format + amountText;
        }
        drawScaledAlignedTextInBox(context, amountText, x, y, width, height, alignment, 1f, z);
    }

    public static void drawScaledAlignedTextInBox(ModularGuiContext context, String amountText,
                                                  int x, int y, int width, int height, Alignment alignment) {
        drawScaledAlignedTextInBox(context, amountText, x, y, width, height, alignment, 1f, 0.0f);
    }

    public static void drawScaledAlignedTextInBox(ModularGuiContext context, String amountText,
                                                  int x, int y, int width, int height,
                                                  Alignment alignment, float maxScale, float z) {
        if (amountText == null || amountText.isEmpty()) return;
        // render the amount overlay
        textRenderer.setShadow(true);
        textRenderer.setScale(1f);
        textRenderer.setColor(Color.WHITE.main);
        textRenderer.setAlignment(alignment, width, height);
        textRenderer.setPos(x, y);
        textRenderer.setHardWrapOnBorder(false);
        if (amountText.length() > 2 && width > 16) { // we know that numbers below 100 will always fit in standard slots
            // simulate and calculate scale with width
            textRenderer.setSimulate(true);
            textRenderer.draw(context.getGraphics(), amountText);
            textRenderer.setSimulate(false);
            textRenderer.setScale(Math.min(maxScale, width / textRenderer.getLastWidth()));
        }
        context.getGraphics().nextStratum();
        textRenderer.draw(context.getGraphics(), amountText);
        textRenderer.setHardWrapOnBorder(true);
    }

    public static void drawSprite(GuiGraphicsExtractor graphics, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        GuiTextureRenderState.submit(graphics, sprite.atlasLocation(), x0, y0, x0 + w, y0 + h,
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), true);
    }

    public static void drawTiledSprite(GuiGraphicsExtractor graphics, TextureAtlasSprite sprite, float x0, float y0, float w, float h) {
        drawTiledTexture(graphics, sprite.atlasLocation(), x0, y0, w, h, sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1(), sprite.contents().width(), sprite.contents().height(), 0);
    }

    public static void drawOutlineCenter(GuiGraphicsExtractor graphics, int x, int y, int offset, int color) {
        drawOutlineCenter(graphics, x, y, offset, color, 1);
    }

    public static void drawOutlineCenter(GuiGraphicsExtractor graphics, int x, int y, int offset, int color, int border) {
        drawOutline(graphics, x - offset, y - offset, x + offset, y + offset, color, border);
    }

    public static void drawOutline(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color) {
        drawOutline(graphics, left, top, right, bottom, color, 1);
    }

    /**
     * Draw rectangle outline with given border
     */
    public static void drawOutline(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom,
                                   int color, int border) {
        graphics.fill(left, top, left + border, bottom, color);
        graphics.fill(right - border, top, right, bottom, color);
        graphics.fill(left + border, top, right - border, top + border, color);
        graphics.fill(left + border, bottom - border, right - border, bottom, color);
    }

    private static void drawBorderLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                       float border, int color, boolean outside) {
        if (outside) {
            left -= border;
            top -= border;
            right += border;
            bottom += border;
        }
        float x0 = left, y0 = top, x1 = right, y1 = bottom, d = border;

        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_STRIP);
        var pose = new Matrix4f();
        pc(buffer, pose, x0, y0, color);
        pc(buffer, pose, x1 - d, y0 + d, color);
        pc(buffer, pose, x1, y0, color);
        pc(buffer, pose, x1 - d, y1 - d, color);
        pc(buffer, pose, x1, y1, color);
        pc(buffer, pose, x0 + d, y1 - d, color);
        pc(buffer, pose, x0, y1, color);
        pc(buffer, pose, x0 + d, y0 + d, color);
        pc(buffer, pose, x0, y0, color);
        pc(buffer, pose, x1 - d, y0 + d, color);
        buffer.submit(graphics);
    }

    public static void drawBorderOutsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                             int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, 1, color, true);
    }

    public static void drawBorderOutsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                             float border, int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, border, color, true);
    }

    public static void drawBorderInsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                            int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, 1, color, false);
    }

    public static void drawBorderInsideLTRB(GuiGraphicsExtractor graphics, float left, float top, float right, float bottom,
                                            float border, int color) {
        drawBorderLTRB(graphics, left, top, right, bottom, border, color, false);
    }

    private static void drawBorderXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float border,
                                       int color, boolean outside) {
        drawBorderLTRB(graphics, x, y, x + w, y + h, border, color, outside);
    }

    public static void drawBorderOutsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float border,
                                             int color) {
        drawBorderXYWH(graphics, x, y, w, h, border, color, true);
    }

    public static void drawBorderOutsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
        drawBorderXYWH(graphics, x, y, w, h, 1, color, true);
    }

    public static void drawBorderInsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, float border,
                                            int color) {
        drawBorderXYWH(graphics, x, y, w, h, border, color, false);
    }

    public static void drawBorderInsideXYWH(GuiGraphicsExtractor graphics, float x, float y, float w, float h, int color) {
        drawBorderXYWH(graphics, x, y, w, h, 1, color, false);
    }

    private static void pc(VertexConsumer buffer, Matrix4f pose, float x, float y, int c) {
        buffer.addVertex(pose, x, y, 0).setColor(c);
    }

    /**
     * Draws a rectangular shadow
     *
     * @param x      left of solid shadow part
     * @param y      top of solid shadow part
     * @param w      width of solid shadow part
     * @param h      height of solid shadow part
     * @param oX     shadow gradient size in x
     * @param oY     shadow gradient size in y
     * @param opaque solid shadow color
     * @param shadow gradient end color
     */
    public static void drawDropShadow(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int oX, int oY,
                                      int opaque, int shadow) {
        Matrix4f pose = new Matrix4f();
        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.QUADS);

        float x1 = x + w, y1 = y + h;

        /* Draw opaque part */
        buffer.addVertex(pose, x1, y, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x, y, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x, y1, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x1, y1, 0).setColor(r1, g1, b1, a1);

        /* Draw top shadow */
        buffer.addVertex(pose, x1 + oX, y - oY, 0).setColor(r2, g2, b2, a2);
        buffer.addVertex(pose, x - oX, y - oY, 0).setColor(r2, g2, b2, a2);
        buffer.addVertex(pose, x, y, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x1, y, 0).setColor(r1, g1, b1, a1);

        /* Draw bottom shadow */
        buffer.addVertex(pose, x1, y1, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x, y1, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x - oX, y1 + oY, 0).setColor(r2, g2, b2, a2);
        buffer.addVertex(pose, x1 + oX, y1 + oY, 0).setColor(r2, g2, b2, a2);

        /* Draw left shadow */
        buffer.addVertex(pose, x, y, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x - oX, y - oY, 0).setColor(r2, g2, b2, a2);
        buffer.addVertex(pose, x - oX, y1 + oY, 0).setColor(r2, g2, b2, a2);
        buffer.addVertex(pose, x, y1, 0).setColor(r1, g1, b1, a1);

        /* Draw right shadow */
        buffer.addVertex(pose, x1 + oX, y - oY, 0).setColor(r2, g2, b2, a2);
        buffer.addVertex(pose, x1, y, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x1, y1, 0).setColor(r1, g1, b1, a1);
        buffer.addVertex(pose, x1 + oX, y1 + oY, 0).setColor(r2, g2, b2, a2);
        buffer.submit(graphics);
    }

    public static void drawDropCircleShadow(GuiGraphicsExtractor graphics, int x, int y, int radius, int segments,
                                            int opaque, int shadow) {
        if (segments < 3) throw new IllegalArgumentException("A circle needs at least three segments");
        Matrix4f pose = new Matrix4f();

        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_FAN);
        buffer.addVertex(pose, x, y, 0).setColor(r1, g1, b1, a1);

        Vector3f pos = new Vector3f();
        for (int i = 0; i <= segments; i++) {
            float a = i / (float) segments * TWO_PI - HALF_PI;
            circleVertex(buffer, pose, pos, x, Mth.cos(a), y, Mth.sin(a), radius).setColor(r2, g2, b2, a2);
        }
        buffer.submit(graphics);
    }

    public static void drawDropCircleShadow(GuiGraphicsExtractor graphics, int x, int y, int radius, int offset, int segments,
                                            int opaque, int shadow) {
        if (offset >= radius) {
            drawDropCircleShadow(graphics, x, y, radius, segments, opaque, shadow);
            return;
        }
        if (segments < 3) throw new IllegalArgumentException("A circle needs at least three segments");
        Matrix4f pose = new Matrix4f();

        float a1 = Color.getAlphaF(opaque);
        float r1 = Color.getRedF(opaque);
        float g1 = Color.getGreenF(opaque);
        float b1 = Color.getBlueF(opaque);
        float a2 = Color.getAlphaF(shadow);
        float r2 = Color.getRedF(shadow);
        float g2 = Color.getGreenF(shadow);
        float b2 = Color.getBlueF(shadow);

        var buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_FAN);
        /* Draw opaque base */
        buffer.addVertex(pose, x, y, 0).setColor(r1, g1, b1, a1);

        Vector3f pos = new Vector3f();
        for (int i = 0; i <= segments; i++) {
            float a = i / (float) segments * TWO_PI - HALF_PI;
            circleVertex(buffer, pose, pos, x, Mth.cos(a), y, Mth.sin(a), offset).setColor(r1, g1, b1, a1);
        }

        /* Draw outer shadow */
        buffer.submit(graphics);
        buffer = new GuiShapeBuilder(GuiShapeRenderState.Topology.QUADS);

        for (int i = 0; i < segments; i++) {
            float alpha1 = i / (float) segments * TWO_PI - HALF_PI;
            float alpha2 = (i + 1) / (float) segments * TWO_PI - HALF_PI;

            float cosA1 = Mth.cos(alpha1);
            float cosA2 = Mth.cos(alpha2);
            float sinA1 = Mth.sin(alpha1);
            float sinA2 = Mth.sin(alpha2);

            circleVertex(buffer, pose, pos, x, cosA2, y, sinA2, offset).setColor(r1, g1, b1, a1);
            circleVertex(buffer, pose, pos, x, cosA1, y, sinA1, offset).setColor(r1, g1, b1, a1);
            circleVertex(buffer, pose, pos, x, cosA1, y, sinA1, radius).setColor(r2, g2, b2, a2);
            circleVertex(buffer, pose, pos, x, cosA2, y, sinA2, radius).setColor(r2, g2, b2, a2);
        }
        buffer.submit(graphics);
    }

    private static VertexConsumer circleVertex(VertexConsumer buffer, Matrix4f pose, Vector3f pos,
                                               float x, float xOffset, float y, float yOffset, float mul) {
        pos.x = x - xOffset * mul;
        pos.y = y + yOffset * mul;
        pose.transformPosition(pos);
        return buffer.addVertex(pos.x, pos.y, pos.z);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawBorder(GuiGraphicsExtractor graphics, float x, float y, float width, float height, int color,
                                  float border) {
        drawBorderLTRB(graphics, x, y, x + width, y + height, border, color, false);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawText(GuiGraphicsExtractor graphics, String text, float x, float y,
                                float scale, int color, boolean shadow) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(Minecraft.getInstance().font, text, 0, 0, color, shadow);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawText(GuiGraphicsExtractor graphics, Component text, float x, float y, float scale,
                                int color, boolean shadow) {
        drawText(graphics, text.getVisualOrderText(), x, y, scale, color, shadow);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawText(GuiGraphicsExtractor graphics, FormattedCharSequence text, float x, float y, float scale,
                                int color, boolean shadow) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(Minecraft.getInstance().font, text, 0, 0, color, shadow);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void drawTooltipBackground(GuiContext context, ItemStack stack, List<ClientTooltipComponent> lines,
                                             int x, int y, int textWidth, int height, @Nullable IRichTextBuilder<?> tooltip) {
        GuiGraphicsExtractor graphics = context.getGraphics();

        // TODO theme color
        int backgroundTop = 0xF0100010;
        int backgroundBottom = backgroundTop;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        RichTooltipEvent.Color colorEvent = new RichTooltipEvent.Color(stack, graphics, x, y, context.getFont(),
                backgroundTop, borderColorStart, borderColorEnd, lines, tooltip);

        NeoForge.EVENT_BUS.post(colorEvent);
        backgroundTop = colorEvent.getBackgroundStart();
        backgroundBottom = colorEvent.getBackgroundEnd();
        borderColorStart = colorEvent.getBorderStart();
        borderColorEnd = colorEvent.getBorderEnd();

        // top background border
        drawVerticalGradientRect(graphics, x - 3, y - 4, textWidth + 6, 1, backgroundTop, backgroundTop);
        // bottom background border
        drawVerticalGradientRect(graphics, x - 3, y + height + 3, textWidth + 6, 1, backgroundBottom, backgroundBottom);
        // center background
        drawVerticalGradientRect(graphics, x - 3, y - 3, textWidth + 6, height + 6, backgroundTop, backgroundBottom);
        // left background border
        drawVerticalGradientRect(graphics, x - 4, y - 3, 1, height + 6, backgroundTop, backgroundBottom);
        // right background border
        drawVerticalGradientRect(graphics, x + textWidth + 3, y - 3, 1, height + 6, backgroundTop, backgroundBottom);

        // left accent border
        drawVerticalGradientRect(graphics, x - 3, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // right accent border
        drawVerticalGradientRect(graphics, x + textWidth + 2, y - 2, 1, height + 4, borderColorStart, borderColorEnd);
        // top accent border
        drawVerticalGradientRect(graphics, x - 3, y - 3, textWidth + 6, 1, borderColorStart, borderColorStart);
        // bottom accent border
        drawVerticalGradientRect(graphics, x - 3, y + height + 2, textWidth + 6, 1, borderColorEnd, borderColorEnd);
    }
}
