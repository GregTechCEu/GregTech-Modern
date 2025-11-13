package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Interpolations;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class UITexture implements IDrawable, IJsonSerializable<UITexture> {

    public static final UITexture DEFAULT = fullImage("gui/options_background", ColorType.DEFAULT);

    private static final ResourceLocation ICONS_LOCATION = GTCEu.id("textures/gui/icons.png");

    // only for usage in GuiTextures
    static UITexture icon(String name, int x, int y, int w, int h) {
        return UITexture.builder()
                .location(ICONS_LOCATION)
                .imageSize(256, 256)
                .subAreaXYWH(x, y, w, h)
                .iconColorType()
                .name(name)
                .build();
    }

    static UITexture icon(String name, int x, int y) {
        return icon(name, x, y, 16, 16);
    }

    private static final String TEXTURES_PREFIX = "textures/";
    private static final String PNG_SUFFIX = ".png";

    @Getter
    public final ResourceLocation location;
    public final float u0, v0, u1, v1;
    @Getter
    @Nullable
    public final ColorType colorType;
    public final boolean nonOpaque;

    /**
     * Creates a drawable texture
     *
     * @param location  location of the texture
     * @param u0        x offset of the image (0-1)
     * @param v0        y offset of the image (0-1)
     * @param u1        x end offset of the image (0-1)
     * @param v1        y end offset of the image (0-1)
     * @param colorType a function to get which color from a widget theme should be used to color this texture.
     */
    public UITexture(ResourceLocation location, float u0, float v0, float u1, float v1, @Nullable ColorType colorType) {
        this(location, u0, v0, u1, v1, colorType, false);
    }

    /**
     * Creates a drawable texture
     *
     * @param location  location of the texture
     * @param u0        x offset of the image (0-1)
     * @param v0        y offset of the image (0-1)
     * @param u1        x end offset of the image (0-1)
     * @param v1        y end offset of the image (0-1)
     * @param colorType a function to get which color from a widget theme should be used to color this texture.
     * @param nonOpaque whether the texture should draw with blend (if true) or not (if false)
     */
    public UITexture(ResourceLocation location, float u0, float v0, float u1, float v1, @Nullable ColorType colorType,
                     boolean nonOpaque) {
        this.colorType = colorType;
        boolean png = !location.getPath().endsWith(".png");
        boolean textures = !location.getPath().startsWith("textures/");
        if (png || textures) {
            String path = location.getPath();
            path = png ? (textures ? TEXTURES_PREFIX + path + PNG_SUFFIX : path + PNG_SUFFIX) : TEXTURES_PREFIX + path;
            location = new ResourceLocation(location.getNamespace(), path);
        }
        this.location = location;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.nonOpaque = nonOpaque;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UITexture fullImage(ResourceLocation location) {
        return new UITexture(location, 0, 0, 1, 1, null);
    }

    public static UITexture fullImage(String location) {
        return fullImage(new ResourceLocation(location), null);
    }

    public static UITexture fullImage(String mod, String location) {
        return fullImage(new ResourceLocation(mod, location), null);
    }

    public static UITexture fullImage(ResourceLocation location, ColorType colorType) {
        return new UITexture(location, 0, 0, 1, 1, colorType);
    }

    public static UITexture fullImage(String location, ColorType colorType) {
        return fullImage(new ResourceLocation(location), colorType);
    }

    public static UITexture fullImage(String mod, String location, ColorType colorType) {
        return fullImage(new ResourceLocation(mod, location), colorType);
    }

    public UITexture getSubArea(Area bounds) {
        return getSubArea(bounds.x, bounds.y, bounds.ex(), bounds.ey());
    }

    /**
     * Returns a texture with a sub area relative to this area texture
     *
     * @param uStart x offset of the image (0-1)
     * @param vStart y offset of the image (0-1)
     * @param uEnd   x end offset of the image (0-1)
     * @param vEnd   y end offset of the image (0-1)
     * @return relative sub area
     */
    public UITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new UITexture(this.location, lerpU(uStart), lerpV(vStart), lerpU(uEnd), lerpV(vEnd), this.colorType);
    }

    protected final float lerpU(float u) {
        return Interpolations.lerp(this.u0, this.u1, u);
    }

    protected final float lerpV(float v) {
        return Interpolations.lerp(this.v0, this.v1, v);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        applyColor(this.colorType != null ? this.colorType.getColor(widgetTheme) :
                ColorType.DEFAULT.getColor(widgetTheme));
        draw(context, (float) x, y, width, height);
    }

    public void draw(GuiContext context, float x, float y, float width, float height) {
        GuiDraw.drawTexture(context.getLastGraphicsPose(), this.location, x, y, x + width, y + height, this.u0, this.v0,
                this.u1, this.v1);
    }

    public void drawSubArea(GuiContext context, float x, float y, float width, float height, float uStart, float vStart,
                            float uEnd,
                            float vEnd, WidgetTheme widgetTheme) {
        applyColor(this.colorType != null ? this.colorType.getColor(widgetTheme) :
                ColorType.DEFAULT.getColor(widgetTheme));
        GuiDraw.drawTexture(context.getLastGraphicsPose(), this.location, x, y, x + width, y + height, lerpU(uStart),
                lerpV(vStart), lerpU(uEnd),
                lerpV(vEnd), this.nonOpaque);
    }

    public static UITexture parseFromJson(JsonObject json) {
        String name = JsonHelper.getString(json, null, "name", "id");
        if (name != null) {
            UITexture drawable = DrawableSerialization.getTexture(name);
            if (drawable != null) return drawable;
        }
        Builder builder = builder();
        builder.location(JsonHelper.getString(json, GTCEu.MOD_ID + ":gui/widgets/error", "location"))
                .imageSize(JsonHelper.getInt(json, defaultImageWidth, "imageWidth", "iw"),
                        JsonHelper.getInt(json, defaultImageHeight, "imageHeight", "ih"));
        boolean mode1 = json.has("x") || json.has("y") || json.has("w") || json.has("h") || json.has("width") ||
                json.has("height");
        boolean mode2 = json.has("u0") || json.has("v0") || json.has("u1") || json.has("u1");
        if (mode1) {
            if (mode2) {
                throw new JsonParseException("Tried to specify x, y, w, h and u0, v0, u1, v1!");
            }
            builder.subAreaXYWH(JsonHelper.getInt(json, 0, "x"),
                    JsonHelper.getInt(json, 0, "y"),
                    JsonHelper.getInt(json, builder.iw, "w", "width"),
                    JsonHelper.getInt(json, builder.ih, "h", "height"));
        } else if (mode2) {
            builder.subAreaUV(JsonHelper.getFloat(json, 0, "u0"),
                    JsonHelper.getFloat(json, 0, "v0"),
                    JsonHelper.getFloat(json, 1, "u1"),
                    JsonHelper.getFloat(json, 1, "v1"));
        }
        int bl = JsonHelper.getInt(json, 0, "bl", "borderLeft", "borderX", "border");
        int br = JsonHelper.getInt(json, 0, "br", "borderRight", "borderY", "border");
        int bt = JsonHelper.getInt(json, 0, "bt", "borderTop", "borderBottom", "border");
        int bb = JsonHelper.getInt(json, 0, "bb", "borderBottom", "borderTop", "border");
        if (bl > 0 || br > 0 || bt > 0 || bb > 0) {
            builder.adaptable(bl, bt, br, bb);
        }
        if (JsonHelper.getBoolean(json, false, "tiled")) {
            builder.tiled();
        }
        String colorTypeName = JsonHelper.getString(json, null, "colorType", "color");
        if (colorTypeName != null) {
            builder.colorType(ColorType.get(colorTypeName));
        } else if (JsonHelper.getBoolean(json, false, "canApplyTheme")) {
            builder.canApplyTheme();
        }
        return builder.build();
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        String name = DrawableSerialization.getTextureId(this);
        if (name != null) {
            json.addProperty("id", name);
            return true;
        }
        json.addProperty("location", this.location.toString());
        json.addProperty("u0", this.u0);
        json.addProperty("v0", this.v0);
        json.addProperty("u1", this.u1);
        json.addProperty("v1", this.v1);
        if (this.colorType != null) json.addProperty("colorType", this.colorType.getName());
        return true;
    }

    private static int defaultImageWidth = 16, defaultImageHeight = 16;

    public static void setDefaultImageSize(int w, int h) {
        defaultImageWidth = w;
        defaultImageHeight = h;
    }

    /**
     * A builder class to help create image textures.
     */
    public static class Builder {

        private ResourceLocation location;
        private int iw = defaultImageWidth, ih = defaultImageHeight;
        private int x, y, w, h;
        private float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
        private Mode mode = Mode.FULL;
        private int bl = 0, bt = 0, br = 0, bb = 0;
        private String name;
        private boolean tiled = false;
        private ColorType colorType = null;
        private boolean nonOpaque = false;

        /**
         * @param loc location of the image to draw
         */
        public Builder location(ResourceLocation loc) {
            this.location = loc;
            return this;
        }

        /**
         * @param mod  mod location of the image to draw
         * @param path path of the image to draw
         */
        public Builder location(String mod, String path) {
            this.location = new ResourceLocation(mod, path);
            return this;
        }

        /**
         * @param path path of the image to draw in minecraft asset folder
         */
        public Builder location(String path) {
            this.location = new ResourceLocation(path);
            return this;
        }

        /**
         * Set the image size. Required for {@link #tiled()}, {@link #adaptable(int, int)} and
         * {@link #subAreaXYWH(int, int, int, int)}
         *
         * @param w image width
         * @param h image height
         */
        public Builder imageSize(int w, int h) {
            this.iw = w;
            this.ih = h;
            return this;
        }

        /**
         * This will make the image be drawn tiled rather than stretched.
         *
         * @param imageWidth  image width
         * @param imageHeight image height
         */
        public Builder tiled(int imageWidth, int imageHeight) {
            return tiled().imageSize(imageWidth, imageHeight);
        }

        /**
         * This will make the image be drawn tiled rather than stretched.
         */
        public Builder tiled() {
            this.tiled = true;
            return this;
        }

        /**
         * Will draw the whole image file.
         */
        public Builder fullImage() {
            this.mode = Mode.FULL;
            return this;
        }

        /**
         * Specify a sub area of the image in pixels, with a position and a size.
         *
         * @param x x in pixels
         * @param y y in pixels
         * @param w width in pixels
         * @param h height in pixels
         */
        public Builder subAreaXYWH(int x, int y, int w, int h) {
            this.mode = Mode.PIXEL;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            return this;
        }

        /**
         * Specify a sub area of the image in pixels, with a start position and an end position.
         *
         * @param left   start position on the x-axis (equivalent to x in above methods)
         * @param top    start position on the y-axis (equivalent to y in above methods)
         * @param right  end position on the x-axis (equivalent to x + w in above methods)
         * @param bottom end position on the y-axis (equivalent to y + h in above methods)
         */
        public Builder subAreaLTRB(int left, int top, int right, int bottom) {
            return subAreaXYWH(left, top, right - left, bottom - top);
        }

        /**
         * Specify a sub area of the image in relative uv values (0 - 1). u0 and v0 are start positions, while u1 and v1
         * are end positions.
         * This means that the relative size is u1 - u0 and v1 - v0.
         *
         * @param u0 x start
         * @param v0 y start
         * @param u1 x end
         * @param v1 y end
         */
        public Builder subAreaUV(float u0, float v0, float u1, float v1) {
            this.mode = Mode.RELATIVE;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            return this;
        }

        /**
         * This will draw the corners, edges and body of the image separately. This will only stretch/tile the
         * body so the border looks right on all sizes. This is also known as a
         * <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice texture</a>.
         *
         * @param bl left border width. Can be 0.
         * @param bt top border width. Can be 0.
         * @param br right border width. Can be 0.
         * @param bb bottom border width. Can be 0.
         */
        public Builder adaptable(int bl, int bt, int br, int bb) {
            this.bl = bl;
            this.bt = bt;
            this.br = br;
            this.bb = bb;
            return this;
        }

        /**
         * This will draw the corners, edges and body of the image separately. This will only stretch/tile the
         * body so the border looks right on all sizes. This is also known as a
         * <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice texture</a>.
         *
         * @param borderX left and right border width. Can be 0.
         * @param borderY top and bottom border width. Can be 0
         */
        public Builder adaptable(int borderX, int borderY) {
            return adaptable(borderX, borderY, borderX, borderY);
        }

        /**
         * This will draw the corners, edges and body of the image separately. This will only stretch/tile the
         * body so the border looks right on all sizes. This is also known as a
         * <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice texture</a>.
         *
         * @param border border width
         */
        public Builder adaptable(int border) {
            return adaptable(border, border);
        }

        /**
         * Specify if theme color should apply to this texture.
         *
         * @see #defaultColorType()
         */
        public Builder canApplyTheme() {
            return defaultColorType();
        }

        /**
         * Sets a function which defines how theme color is applied to this texture. Null means no color will be
         * applied.
         * <il>
         * <li>Background textures should use {@link ColorType#DEFAULT} or {@link #defaultColorType()}</li>
         * <li>White icons (only has a shape and some grey shading) should use {@link ColorType#ICON} or
         * {@link #iconColorType()}</li>
         * <li>Text should use {@link ColorType#TEXT} or {@link #textColorType()}</li>
         * <li>Everything else (f.e. colored icons and overlays) should use null</li>
         * </il>
         *
         * @param colorType function which defines how theme color is applied to this texture
         * @return this
         */
        public Builder colorType(@Nullable ColorType colorType) {
            this.colorType = colorType;
            return this;
        }

        /**
         * Sets this texture to use default theme color.
         * Usually used for background textures (grey shaded).
         *
         * @return this
         * @see #colorType(ColorType)
         */
        public Builder defaultColorType() {
            return colorType(ColorType.DEFAULT);
        }

        /**
         * Sets this texture to use text theme color.
         * Usually used for texts.
         *
         * @return this
         * @see #colorType(ColorType)
         */
        public Builder textColorType() {
            return colorType(ColorType.TEXT);
        }

        /**
         * Sets this texture to use icon theme color.
         * Usually used for grey shaded icons without color.
         *
         * @return this
         * @see #colorType(ColorType)
         */
        public Builder iconColorType() {
            return colorType(ColorType.ICON);
        }

        /**
         * Registers the texture with a name, so it can be used in json without creating the texture again.
         * By default, theme color is applicable.
         *
         * @param name texture name
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets this texture as at least partially transparent, will not disable glBlend when drawing.
         */
        public Builder nonOpaque() {
            this.nonOpaque = true;
            return this;
        }

        /**
         * Creates the texture
         *
         * @return the created texture
         */
        public UITexture build() {
            UITexture texture = create();
            if (this.name == null) {
                String[] p = texture.location.getPath().split("/");
                p = p[p.length - 1].split("\\.");
                this.name = texture.location.getNamespace().equals(GTCEu.MOD_ID) ? p[0] :
                        texture.location.getNamespace() + ":" + p[0];
                if (DrawableSerialization.getTexture(this.name) != null) {
                    return texture;
                }
            }
            DrawableSerialization.registerTexture(this.name, texture);
            return texture;
        }

        private UITexture create() {
            if (this.location == null) {
                throw new NullPointerException("Location must not be null");
            }
            if (this.iw <= 0 || this.ih <= 0) throw new IllegalArgumentException("Image size must be > 0");
            if (this.mode == Mode.FULL) {
                this.u0 = 0;
                this.v0 = 0;
                this.u1 = 1;
                this.v1 = 1;
                this.mode = Mode.RELATIVE;
            } else if (this.mode == Mode.PIXEL) {
                float tw = 1f / this.iw, th = 1f / this.ih;
                this.u0 = this.x * tw;
                this.v0 = this.y * th;
                this.u1 = (this.x + this.w) * tw;
                this.v1 = (this.y + this.h) * th;
                this.mode = Mode.RELATIVE;
            }
            if (this.mode == Mode.RELATIVE) {
                if (this.u0 < 0 || this.v0 < 0 || this.u1 > 1 || this.v1 > 1)
                    throw new IllegalArgumentException("UV values must be 0 - 1");
                if (this.bl > 0 || this.bt > 0 || this.br > 0 || this.bb > 0) {
                    return new AdaptableUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.colorType,
                            this.nonOpaque, this.iw, this.ih, this.bl, this.bt, this.br, this.bb, this.tiled);
                }
                if (this.tiled) {
                    return new TiledUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.iw, this.ih,
                            this.colorType, this.nonOpaque);
                }
                return new UITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.colorType, this.nonOpaque);
            }
            throw new IllegalStateException();
        }
    }

    private enum Mode {
        FULL,
        PIXEL,
        RELATIVE
    }
}
