package brachy.modularui.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;
import brachy.modularui.widget.sizer.Box;

import com.mojang.serialization.Codec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;

/**
 * A {@link IDrawable} wrapper with a fixed size and an alignment.
 */
public class Icon implements IIcon {

    public static final MutableObjectCodec<Icon> CODEC = MutableObjectCodec.drawableBuilder(Icon::new)
            .add("drawable", Icon::drawable, Icon::getDrawable, IDrawable.CODEC)
            .addOpt("width", Icon::width, Icon::getWidth, Codec.INT, 0)
            .addOpt("height", Icon::height, Icon::getHeight, Codec.INT, 0)
            .addOpt("aspectRatio", Icon::aspectRatio, Icon::getAspectRatio, Codec.FLOAT, 0f)
            .addOpt("alignment", Icon::alignment, Icon::getAlignment, Alignment.CODEC, Alignment.Center)
            .addOpt("margin", Icon::margin, Icon::getMargin, Box.CODEC, Box.ZERO)
            .build();

    @Getter private IDrawable drawable;
    @Getter private int width = 0, height = 0;
    @Getter private float aspectRatio = 0;
    @Getter private Alignment alignment = Alignment.Center;
    @Getter private final Box margin = new Box();

    private Icon() {
        this.drawable = IDrawable.EMPTY;
    }

    public Icon(IDrawable drawable) {
        this.drawable = drawable;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        x += this.margin.left();
        y += this.margin.top();
        width -= this.margin.horizontal();
        height -= this.margin.vertical();
        int frameWidth = width;
        int frameHeight = height;
        if (this.width > 0) width = this.width;
        if (this.height > 0) height = this.height;
        if (this.aspectRatio > 0) {
            if (this.width <= 0) {
                if (this.height <= 0) {
                    // width and height is unset, so adjust width or height so that one of them takes the full space
                    float w = width, h = height;
                    float properW = this.aspectRatio * h;
                    if (w > properW) {
                        width = (int) properW;
                    } else if (w < properW) {
                        height = (int) (w / this.aspectRatio);
                    }
                } else {
                    // height is set, so adjust width to height
                    float properW = this.aspectRatio * height;
                    width = (int) properW;
                }
            } else if (this.height <= 0) {
                // width is set, so adjust height to width
                height = (int) (width / this.aspectRatio);
            } else if (ModularUI.isDev()) {
                ModularUI.LOGGER.error("Aspect ratio in Icon can't be applied when width and height are specified");
                // remove aspect ratio to avoid log spamming, it does nothing in the current state anyway
                this.aspectRatio = 0;
            }
        }
        // apply alignment
        if (width != frameWidth) {
            x += (int) (frameWidth * this.alignment.x - width * this.alignment.x);
        }
        if (height != frameHeight) {
            y += (int) (frameHeight * this.alignment.y - height * this.alignment.y);
        }
        this.drawable.draw(context, x, y, width, height, widgetTheme);
    }

    @Override
    public IDrawable getWrappedDrawable() {
        return drawable;
    }

    public Icon drawable(IDrawable drawable) {
        this.drawable = drawable;
        return this;
    }

    public Icon expandWidth() {
        return width(0);
    }

    public Icon expandHeight() {
        return height(0);
    }

    public Icon width(int width) {
        this.width = Math.max(0, width);
        return this;
    }

    public Icon height(int height) {
        this.height = Math.max(0, height);
        return this;
    }

    public Icon size(int width, int height) {
        return width(width).height(height);
    }

    public Icon size(int size) {
        return width(size).height(size);
    }

    public Icon aspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    public Icon alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Icon center() {
        return alignment(Alignment.Center);
    }

    public Icon margin(int left, int right, int top, int bottom) {
        this.margin.all(left, right, top, bottom);
        return this;
    }

    public Icon margin(int horizontal, int vertical) {
        this.margin.all(horizontal, vertical);
        return this;
    }

    public Icon margin(int all) {
        this.margin.all(all);
        return this;
    }

    public Icon marginLeft(int val) {
        this.margin.left(val);
        return this;
    }

    public Icon marginRight(int val) {
        this.margin.right(val);
        return this;
    }

    public Icon marginTop(int val) {
        this.margin.top(val);
        return this;
    }

    public Icon marginBottom(int val) {
        this.margin.bottom(val);
        return this;
    }

    public Icon margin(Box box) {
        if (box != null && box != this.margin) {
            Box.CODEC.copyFields(box, this.margin);
        }
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Icon icon)) return false;

        return width == icon.width && height == icon.height && Float.compare(aspectRatio, icon.aspectRatio) == 0 &&
                drawable.equals(icon.drawable) && alignment.equals(icon.alignment) && margin.equals(icon.margin);
    }

    @Override
    public int hashCode() {
        int result = drawable.hashCode();
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + Float.hashCode(aspectRatio);
        result = 31 * result + alignment.hashCode();
        result = 31 * result + margin.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.drawable.getClass().getSimpleName() + ")";
    }
}
