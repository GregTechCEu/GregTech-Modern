package brachy.modularui.widget.sizer;

import brachy.modularui.animation.IAnimatable;
import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.Point;
import brachy.modularui.utils.Rectangle;
import brachy.modularui.utils.math.MathUtils;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import com.mojang.serialization.Codec;

import lombok.Getter;

import java.util.Objects;

/**
 * A rectangular widget area, composed of a position and a size.
 * Also has fields for a relative position, a layer and margin & padding.
 */
public class Area extends Rectangle implements IAnimatable<Area> {

    public static final MutableObjectCodec<Area> CODEC = MutableObjectCodec.builder(Area::new)
            .addOpt("x", Area::x, Area::x, Codec.INT, 0)
            .addOpt("y", Area::y, Area::y, Codec.INT, 0)
            .addOpt("w", Area::w, Area::w, Codec.INT, 0).alias("width")
            .addOpt("h", Area::h, Area::h, Codec.INT, 0).alias("height")
            .addOpt("margin", Area::setMargin, Area::getMargin, Box.CODEC, Box.ZERO)
            .addOpt("padding", Area::setPadding, Area::getPadding, Box.CODEC, Box.ZERO)
            .build();

    public static boolean isInside(int x, int y, int w, int h, int px, int py) {
        SHARED.set(x, y, w, h);
        return SHARED.isInside(px, py);
    }

    public static final Area SHARED = new Area();

    public static final Area ZERO = new Area();

    /**
     * relative position (in most cases the direct parent)
     */
    public int rx;
    public int ry;
    /**
     * the widget layer within this panel
     */
    private int z;
    @Getter
    private final Box margin = new Box();
    @Getter
    private final Box padding = new Box();

    public Area() {
        super();
    }

    public Area(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public Area(Rectangle rectangle) {
        super(rectangle);
    }

    public Area(Area area) {
        super(area);
        this.rx = area.rx;
        this.ry = area.ry;
        this.z = area.z;
        getMargin().set(area.getMargin());
        getPadding().set(area.getPadding());
    }

    public int z() {
        return this.z;
    }

    public void z(int z) {
        this.z = z;
    }

    /**
     * Calculate X based on anchor value
     */
    public int x(float anchor) {
        return this.x + (int) (this.width * anchor);
    }

    /**
     * Calculate Y based on anchor value
     */
    public int y(float anchor) {
        return this.y + (int) (this.height * anchor);
    }

    public int getPoint(GuiAxis axis) {
        return axis.isHorizontal() ? this.x : this.y;
    }

    public int getEndPoint(GuiAxis axis) {
        return axis.isHorizontal() ? this.x + this.width : this.y + this.height;
    }

    public int getSize(GuiAxis axis) {
        return axis.isHorizontal() ? this.width : this.height;
    }

    public int getRelativePoint(GuiAxis axis) {
        return axis.isHorizontal() ? this.rx : this.ry;
    }

    public void setPoint(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.x = v;
        } else {
            this.y = v;
        }
    }

    public void setSize(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.width = v;
        } else {
            this.height = v;
        }
    }

    public void setRelativePoint(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.rx = v;
        } else {
            this.ry = v;
        }
    }

    public void addPoint(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.x += v;
        } else {
            this.y += v;
        }
    }

    public void addSize(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.width += v;
        } else {
            this.height += v;
        }
    }

    public void addRelativePoint(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.rx += v;
        } else {
            this.ry += v;
        }
    }

    void applyPos(int parentX, int parentY) {
        this.x = parentX + this.rx;
        this.y = parentY + this.ry;
    }

    public int requestedWidth() {
        return this.width + getMargin().horizontal();
    }

    public int paddedWidth() {
        return this.width - getPadding().horizontal();
    }

    public int requestedHeight() {
        return this.height + getMargin().vertical();
    }

    public int paddedHeight() {
        return this.height - getPadding().vertical();
    }

    public int requestedSize(GuiAxis axis) {
        return axis.isHorizontal() ? requestedWidth() : requestedHeight();
    }

    public int paddedSize(GuiAxis axis) {
        return axis.isHorizontal() ? paddedWidth() : paddedHeight();
    }

    public int relativeEndX() {
        return this.rx + this.width;
    }

    public int relativeEndY() {
        return this.ry + this.height;
    }

    /**
     * Check whether given position is inside the rect.
     * Use {@link IWidget#isInside(IViewportStack, int, int)} rather than
     * this!
     */
    public boolean isInside(int x, int y) {
        return x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;
    }

    /**
     * Check whether given point is inside the rect.
     * Use {@link IWidget#isInside(IViewportStack, int, int)} rather than
     * this!
     */
    public boolean isInside(Point point) {
        return isInside(point.x, point.y);
    }

    /**
     * Check whether given rect intersects this rect
     */
    public boolean intersects(Rectangle area) {
        return this.x < area.getX() + area.getWidth() && this.y < area.getY() + area.getHeight() &&
                area.getX() < this.x + this.width && area.getY() < this.y + this.height;
    }

    /**
     * Clamp given area inside of this one
     */
    public void clamp(Area area) {
        int x1 = area.x();
        int y1 = area.y();
        int x2 = area.ex();
        int y2 = area.ey();

        x1 = Mth.clamp(x1, this.x, this.ex());
        y1 = Mth.clamp(y1, this.y, this.ey());
        x2 = Mth.clamp(x2, this.x, this.ex());
        y2 = Mth.clamp(y2, this.y, this.ey());

        area.setPos(x1, y1, x2, y2);
    }

    /**
     * Set the relative position
     */
    public void setRelativePos(int rx, int ry) {
        this.rx = rx;
        this.ry = ry;
    }

    public void setMargin(Box box) {
        if (this.margin == box) return;
        Box.CODEC.copyFields(box, this.margin);
    }

    public void setPadding(Box box) {
        if (this.padding == box) return;
        Box.CODEC.copyFields(box, this.padding);
    }

    /**
     * Transforms the four corners of this rectangle with the given pose stack. The new rectangle can be rotated.
     * Then a min fit rectangle, which is aligned with the screen axis, is put around the corners.
     *
     * @param stack pose stack
     */
    public void transformAndRectanglerize(IViewportStack stack) {
        int xTL = stack.transformX(this.x, this.y), xTR = stack.transformX(ex(), this.y),
                xBL = stack.transformX(this.x, ey()), xBR = stack.transformX(ex(), ey());
        int yTL = stack.transformY(this.x, this.y), yTR = stack.transformY(ex(), this.y),
                yBL = stack.transformY(this.x, ey()), yBR = stack.transformY(ex(), ey());
        int x0 = MathUtils.min(xTL, xTR, xBL, xBR);
        int x1 = MathUtils.max(xTL, xTR, xBL, xBR);
        int y0 = MathUtils.min(yTL, yTR, yBL, yBR);
        int y1 = MathUtils.max(yTL, yTR, yBL, yBR);
        setPos(x0, y0, x1, y1);
    }

    public ScreenRectangle toScreenRectangle() {
        return new ScreenRectangle(this.x, this.y, this.width, this.height);
    }

    /**
     * This creates a copy with size, pos, margin padding and z layer.
     *
     * @return copy
     */
    public Area createCopy() {
        return new Area(this);
    }

    @Override
    public String toString() {
        return "Area{" +
                "x=" + this.x +
                ", y=" + this.y +
                ", w=" + this.width +
                ", h=" + this.height +
                ", rx=" + this.rx +
                ", ry=" + this.ry +
                '}';
    }

    @Override
    public Area interpolate(Area start, Area end, float t) {
        this.x = Interpolations.lerp(start.x, end.x, t);
        this.y = Interpolations.lerp(start.y, end.y, t);
        this.width = Interpolations.lerp(start.width, end.width, t);
        this.height = Interpolations.lerp(start.height, end.height, t);
        this.rx = Interpolations.lerp(start.rx, end.rx, t);
        this.ry = Interpolations.lerp(start.ry, end.ry, t);
        getMargin().interpolate(start.getMargin(), end.getMargin(), t);
        getPadding().interpolate(start.getPadding(), end.getPadding(), t);
        return this;
    }

    @Override
    public Area copyOrImmutable() {
        return createCopy();
    }

    @Override
    public boolean shouldAnimate(Area target) {
        return x != target.x || y != target.y || width != target.width || height != target.height ||
                rx != target.rx || ry != target.ry || !getMargin().isEqual(target.getMargin()) ||
                !getPadding().isEqual(target.getPadding());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Area area = (Area) o;
        return rx == area.rx && ry == area.ry && z == area.z &&
                Objects.equals(getMargin(), area.getMargin()) &&
                Objects.equals(getPadding(), area.getPadding());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rx, ry, z, margin, padding);
    }
}
