package brachy.modularui.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ToString
@Accessors(fluent = true, chain = true)
public class UITexture implements IDrawable {

    public static final MapCodec<UITexture> CODEC_FROM_BUILDER = Builder.CODEC.flatXmap(Builder::buildForCodec, t -> DataResult.success(t.toBuilder()));
    public static final Codec<UITexture> CODEC_FROM_NAME = Codec.stringResolver(TextureRegistry::getTextureId, TextureRegistry::getTexture);
    public static final MapCodec<UITexture> CODEC = IDrawable.CODECS.register("texture",
            CodecUtil.chainedMapCodec(CODEC_FROM_NAME.fieldOf("name"), CODEC_FROM_BUILDER));

    public static final UITexture DEFAULT = fullImage(Identifier.withDefaultNamespace("gui/options_background"), ColorType.DEFAULT);
    public static final FileToIdConverter GUI_TEXTURE_ID_CONVERTER = new FileToIdConverter("textures/gui", ".png");

    private static final Identifier ICONS_LOCATION = ModularUI.id("textures/gui/icons.png");

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

    @ToString.Include
    @Getter
    public final Identifier location;
    @Getter public final float u0, v0, u1, v1;
    @Getter
    @Nullable
    public final ColorType colorType;
    @Getter public final boolean nonOpaque;

    @Getter protected int colorOverride = 0;

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
    public UITexture(Identifier location, float u0, float v0, float u1, float v1, @Nullable ColorType colorType) {
        this(location, u0, v0, u1, v1, colorType, false, 0);
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
    public UITexture(Identifier location, float u0, float v0, float u1, float v1, @Nullable ColorType colorType, boolean nonOpaque) {
        this(location, u0, v0, u1, v1, colorType, nonOpaque, 0);
    }

    /**
     * Creates a drawable texture
     *
     * @param location      location of the texture
     * @param u0            x offset of the image (0-1)
     * @param v0            y offset of the image (0-1)
     * @param u1            x end offset of the image (0-1)
     * @param v1            y end offset of the image (0-1)
     * @param colorType     a function to get which color from a widget theme should be used to color this texture. Can be null.
     * @param nonOpaque     whether the texture should draw with blend (if true) or not (if false).
     * @param colorOverride color override for the texture in ARGB format. 0 means no override
     */
    public UITexture(Identifier location, float u0, float v0, float u1, float v1, @Nullable ColorType colorType, boolean nonOpaque, int colorOverride) {
        this.colorType = colorType;
        boolean png = !location.getPath().endsWith(PNG_SUFFIX);
        boolean textures = !location.getPath().startsWith(TEXTURES_PREFIX);
        if (png || textures) {
            String path = location.getPath();
            path = png ? (textures ? TEXTURES_PREFIX + path + PNG_SUFFIX : path + PNG_SUFFIX) : TEXTURES_PREFIX + path;
            location = location.withPath(path);
        }
        this.location = location;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.nonOpaque = nonOpaque;
        this.colorOverride = colorOverride;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static UITexture fullImage(Identifier location) {
        return new UITexture(location, 0, 0, 1, 1, null);
    }

    public static UITexture fullImage(Identifier location, ColorType colorType) {
        return new UITexture(location, 0, 0, 1, 1, colorType);
    }

    public UITexture register(String name) {
        TextureRegistry.registerTexture(name, this);
        return this;
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
    @Override
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

    @Override
    public boolean canApplyTheme() {
        return this.colorType != null;
    }

    @Override
    public void applyColor(int themeColor) {
        if (this.colorOverride != 0) {
            Color.setGlColor(this.colorOverride);
        } else {
            IDrawable.super.applyColor(themeColor);
        }
    }

    @Override
    public String getTypeName() {
        return "texture";
    }

    public Builder toBuilder() {
        return builder()
                .location(this.location)
                .subAreaUV(this.u0, this.v0, this.u1, this.v1)
                .colorType(this.colorType)
                .nonOpaque(this.nonOpaque);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && isEqual((UITexture) o);
    }

    protected boolean isEqual(UITexture texture) {
        return Objects.equals(location, texture.location) &&
                Float.compare(u0, texture.u0) == 0 && Float.compare(v0, texture.v0) == 0 &&
                Float.compare(u1, texture.u1) == 0 && Float.compare(v1, texture.v1) == 0 &&
                nonOpaque == texture.nonOpaque &&
                colorOverride == texture.colorOverride && Objects.equals(colorType, texture.colorType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, u0, v0, u1, v1, colorType, nonOpaque, colorOverride);
    }

    protected UITexture copy() {
        return new UITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.colorType, this.nonOpaque, this.colorOverride);
    }

    public UITexture withColorOverride(int color) {
        UITexture t = copy();
        t.colorOverride = color;
        return t;
    }

    /**
     * A builder class to help create image textures.
     */
    @Accessors(fluent = false)
    public static class Builder {

        public static final MutableObjectCodec<Builder> CODEC = MutableObjectCodec.builder(UITexture::builder)
                .add("location", Builder::location, Builder::getLocation, Identifier.CODEC)
                .addOpt("imageWidth", Builder::setIw, Builder::getIw, Codec.INT, -1).alias("iw")
                .addOpt("imageHeight", Builder::setIh, Builder::getIh, Codec.INT, -1).alias("ih")
                .addOpt("x", Builder::setX, Builder::getX, Codec.INT, 0)
                .addOpt("y", Builder::setY, Builder::getY, Codec.INT, 0)
                .addOpt("w", Builder::setW, Builder::getW, Codec.INT, 0)
                .addOpt("h", Builder::setH, Builder::getH, Codec.INT, 0)
                .addOpt("u0", Builder::setU0, Builder::getU0, Codec.FLOAT, 0f).alias("uStart")
                .addOpt("v0", Builder::setV0, Builder::getV0, Codec.FLOAT, 0f).alias("vStart")
                .addOpt("u1", Builder::setU1, Builder::getU1, Codec.FLOAT, 1f).alias("uEnd")
                .addOpt("v1", Builder::setV1, Builder::getV1, Codec.FLOAT, 1f).alias("vEnd")
                .addOpt("bl", Builder::setBl, Builder::getBl, Codec.INT, 0).alias("borderLeft", "borderX", "border")
                .addOpt("bt", Builder::setBt, Builder::getBt, Codec.INT, 0).alias("borderTop", "borderY", "border")
                .addOpt("br", Builder::setBr, Builder::getBr, Codec.INT, 0).alias("borderRight", "borderX", "border")
                .addOpt("bb", Builder::setBb, Builder::getBb, Codec.INT, 0).alias("borderBottom", "borderY", "border")
                .addOpt("name", Builder::name, Builder::getName, Codec.STRING, null)
                .addOpt("tiled", Builder::tiled, Builder::isTiled, Codec.BOOL, false)
                .addOpt("colorType", Builder::colorType, Builder::getColorType, ColorType.CODEC, null)
                .addOpt("nonOpaque", Builder::nonOpaque, Builder::isNonOpaque, Codec.BOOL, false)
                .addOpt("colorOverride", Builder::colorOverride, Builder::getColorOverride, Codec.INT, 0)
                .build();

        @Getter private Identifier location;
        @Getter
        @Setter
        private int iw = -1, ih = -1;
        @Getter private int x, y, w, h;
        @Getter private float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
        @Getter private Mode mode = Mode.FULL;
        @Getter private int bl = 0, bt = 0, br = 0, bb = 0;
        @Getter private String name;
        @Getter private boolean tiled = false;
        @Getter private ColorType colorType = null;
        @Getter private boolean nonOpaque = false;
        @Getter private int colorOverride = 0;

        /**
         * @param loc location of the image to draw
         */
        public Builder location(Identifier loc) {
            this.location = loc;
            return this;
        }

        /**
         * @param mod  mod location of the image to draw
         * @param path path of the image to draw
         */
        public Builder location(String mod, String path) {
            this.location = Identifier.fromNamespaceAndPath(mod, path);
            return this;
        }

        /**
         * @param path path of the image to draw in minecraft asset folder
         */
        public Builder location(String path) {
            this.location = Identifier.parse(path);
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
            return tiled(true);
        }

        public Builder tiled(boolean tiled) {
            this.tiled = tiled;
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
         * @param left   start position on the x-axis (equivalent to x in {@link #subAreaXYWH(int, int, int, int)})
         * @param top    start position on the y-axis (equivalent to y in {@link #subAreaXYWH(int, int, int, int)})
         * @param right  end position on the x-axis (equivalent to x + w in {@link #subAreaXYWH(int, int, int, int)})
         * @param bottom end position on the y-axis (equivalent to y + h in {@link #subAreaXYWH(int, int, int, int)})
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
            return nonOpaque(true);
        }

        public Builder nonOpaque(boolean nonOpaque) {
            this.nonOpaque = nonOpaque;
            return this;
        }

        public Builder colorOverride(int colorOverride) {
            this.colorOverride = colorOverride;
            return this;
        }

        /**
         * Creates the texture
         *
         * @return the created texture
         */
        public UITexture build() {
            return create()
                    .resultOrPartial(s -> {
                        throw new IllegalArgumentException(s);
                    }).map(texture -> {
                        TextureRegistry.registerTexture(this.name, texture);
                        return texture;
                    }).map(texture -> this.colorOverride != 0 ? texture.withColorOverride(this.colorOverride) : texture)
                    .orElseThrow();
        }

        private DataResult<UITexture> buildForCodec() {
            // no error throwing and no drawable registration
            return create().map(texture -> this.colorOverride != 0 ? texture.withColorOverride(this.colorOverride) : texture);
        }

        private DataResult<UITexture> create() {
            if (this.location == null) {
                return DataResult.error(() -> "Location must not be null");
            }
            if (this.mode == Mode.FULL) {
                this.u0 = 0;
                this.v0 = 0;
                this.u1 = 1;
                this.v1 = 1;
                this.mode = Mode.RELATIVE;
            } else if (this.mode == Mode.PIXEL) {
                if (this.iw <= 0 || this.ih <= 0) return DataResult.error(() -> "Image size must be > 0 for sub area via xywh or ltrb");
                if (this.x < 0 || this.y < 0 || this.w > this.iw || this.h > this.ih) {
                    return DataResult.error(() -> "X and Y must be > 0 and W and H must by smaller than the specified image size");
                }
                float tw = 1f / this.iw, th = 1f / this.ih;
                this.u0 = this.x * tw;
                this.v0 = this.y * th;
                this.u1 = (this.x + this.w) * tw;
                this.v1 = (this.y + this.h) * th;
                this.mode = Mode.RELATIVE;
            }
            if (this.mode == Mode.RELATIVE) {
                if (this.u0 < 0 || this.v0 < 0 || this.u1 > 1 || this.v1 > 1) {
                    return DataResult.error(() -> "UV values must be 0 - 1");
                }
                if (this.bl > 0 || this.bt > 0 || this.br > 0 || this.bb > 0) {
                    if (this.iw <= 0 || this.ih <= 0) {
                        return DataResult.error(() -> "Image size must be > 0 for adaptable textures (border > 0)");
                    }
                    return DataResult.success(new AdaptableUITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.colorType,
                            this.nonOpaque, 0, this.iw, this.ih, this.bl, this.bt, this.br, this.bb, this.tiled));
                }
                if (this.tiled) {
                    if (this.iw <= 0 || this.ih <= 0) {
                        return DataResult.error(() -> "Image size must be > 0 for tiled textures");
                    }
                    return DataResult.success(new TiledUITexture(this.location, this.u0, this.v0, this.u1, this.v1,
                            this.colorType, this.nonOpaque, 0, this.iw, this.ih));
                }
                return DataResult.success(new UITexture(this.location, this.u0, this.v0, this.u1, this.v1, this.colorType, this.nonOpaque));
            }
            return DataResult.error(() -> "Unknown error");
        }

        // Setters for codec

        private void setX(int x) {
            this.x = x;
            if (x > 0) this.mode = Mode.PIXEL;
        }

        private void setY(int y) {
            this.y = y;
            if (y > 0) this.mode = Mode.PIXEL;
        }

        private void setW(int w) {
            this.w = w;
            if (w > 0) this.mode = Mode.PIXEL;
        }

        private void setH(int h) {
            this.h = h;
            if (h > 0) this.mode = Mode.PIXEL;
        }

        private void setU0(float u0) {
            this.u0 = u0;
            if (u0 > 0) this.mode = Mode.RELATIVE;
        }

        private void setV0(float v0) {
            this.v0 = v0;
            if (v0 > 0) this.mode = Mode.RELATIVE;
        }

        private void setU1(float u1) {
            this.u1 = u1;
            if (u1 < 1) this.mode = Mode.RELATIVE;
        }

        private void setV1(float v1) {
            this.v1 = v1;
            if (v1 < 1) this.mode = Mode.RELATIVE;
        }

        private void setBl(int bl) {
            this.bl = bl;
        }

        private void setBb(int bb) {
            this.bb = bb;
        }

        private void setBr(int br) {
            this.br = br;
        }

        private void setBt(int bt) {
            this.bt = bt;
        }
    }

    private enum Mode {
        FULL,
        PIXEL,
        RELATIVE
    }
}
