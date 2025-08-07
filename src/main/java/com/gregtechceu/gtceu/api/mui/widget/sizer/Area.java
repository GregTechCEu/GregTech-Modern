package com.gregtechceu.gtceu.api.mui.widget.sizer;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.layout.IViewportStack;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.api.mui.utils.Point;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.utils.GTMath;
import lombok.Getter;
import lombok.Setter;

/**
 * A rectangular widget area, composed of a position and a size.
 * Also has fields for a relative position, a layer and margin & padding.
 */
public class Area extends Rectangle implements IUnResizeable {

    public static boolean isInside(int x, int y, int w, int h, int px, int py) {
        SHARED.set(x, y, w, h);
        return SHARED.isInside(px, py);
    }

    public static final Area SHARED = new Area();

    public static final Area ZERO = new Area();

    /**
     * relative position (in most cases the direct parent)
     */
    public int rx, ry;
    /**
     * each panel has its own layer
     */
    @Getter
    @Setter
    private byte panelLayer = 0;
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

    public int x() {
        return this.x;
    }

    public void x(int x) {
        this.x = x;
    }

    public int y() {
        return this.y;
    }

    public void y(int y) {
        this.y = y;
    }

    public int w() {
        return this.width;
    }

    public void w(int w) {
        this.width = w;
    }

    public int h() {
        return this.height;
    }

    public void h(int h) {
        this.height = h;
    }

    public int ex() {
        return this.x + this.width;
    }

    public void ex(int ex) {
        this.x = ex - this.width;
    }

    public int ey() {
        return this.y + this.height;
    }

    public void ey(int ey) {
        this.y = ey - this.width;
    }

    public int mx() {
        return (int) (this.x + this.width * 0.5);
    }

    public int my() {
        return (int) (this.y + this.height * 0.5);
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

    void applyPos(int parentX, int parentY) {
        this.x = parentX + this.rx;
        this.y = parentY + this.ry;
    }

    public int requestedWidth() {
        return this.width + this.margin.horizontal();
    }

    public int paddedWidth() {
        return this.width - this.padding.horizontal();
    }

    public int requestedHeight() {
        return this.height + this.margin.vertical();
    }

    public int paddedHeight() {
        return this.height - this.padding.vertical();
    }

    public int requestedSize(GuiAxis axis) {
        return axis.isHorizontal() ? requestedWidth() : requestedHeight();
    }

    public int relativeEndX() {
        return this.rx + this.width;
    }

    public int relativeEndY() {
        return this.ry + this.height;
    }

    /**
     * Check whether given position is inside the rect.
     * Use {@link com.gregtechceu.gtceu.api.mui.base.widget.IWidget#isInside(IViewportStack, int, int)} rather than
     * this!
     */
    public boolean isInside(int x, int y) {
        return x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;
    }

    /**
     * Check whether given point is inside the rect.
     * Use {@link com.gregtechceu.gtceu.api.mui.base.widget.IWidget#isInside(IViewportStack, Point)} rather than
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

        x1 = GTMath.clamp(x1, this.x, this.ex());
        y1 = GTMath.clamp(y1, this.y, this.ey());
        x2 = GTMath.clamp(x2, this.x, this.ex());
        y2 = GTMath.clamp(y2, this.y, this.ey());

        area.setPos(x1, y1, x2, y2);
    }

    /**
     * Increases or decreases the size of this area. The position will change so that the center of the new
     * area is in the same place.
     * The size will change with double of the given value. The position will change with the negative of the given
     * value.
     * <br>
     * In short, it will push or pull all four edges by the given amount.
     *
     * @param expand amount to expand area by (no restrictions)
     */
    public void expand(int expand) {
        this.expandX(expand);
        this.expandY(expand);
    }

    /**
     * Increases or decreases the size of this area. The position will change so that the center of the new
     * area is in the same place.
     * The size will change with double of the given value. The position will change with the negative of the given
     * value.
     * <br>
     * In short, it will push or pull all four edges by the given amount.
     *
     * @param expandX amount to expand x-axis by (no restrictions)
     * @param expandY amount to expand y-axis by (no restrictions)
     */
    public void expand(int expandX, int expandY) {
        this.expandX(expandX);
        this.expandY(expandY);
    }

    /**
     * Increases or decreases the width of this area. The x position will change so that the center of the new
     * area is in the same place.
     * The width will change with double of the given value. The x position will change with the negative of the given
     * value.
     * <br>
     * In short, it will push or pull the left and right edges by the given amount.
     *
     * @param expand amount to expand x-axis by (no restrictions)
     */
    public void expandX(int expand) {
        offsetX(-expand);
        growW(expand * 2);
    }

    /**
     * Increases or decreases the height of this area. The y position will change so that the center of the new
     * area is in the same place.
     * The height will change with double of the given value. The y position will change with the negative of the given
     * value.
     * <br>
     * In short, it will push or pull the top and bottom edges by the given amount.
     *
     * @param expand amount to expand y-axis by (no restrictions)
     */
    public void expandY(int expand) {
        offsetY(-expand);
        growH(expand * 2);
    }

    /**
     * Increases or decreases the position of the area by the given amount, but doesn't change its size.
     *
     * @param offset amount to change position by (no restrictions)
     */
    public void offset(int offset) {
        offsetX(offset);
        offsetY(offset);
    }

    /**
     * Increases or decreases the position of the area by the given amount, but doesn't change its size.
     *
     * @param offsetX amount to change x position by (no restrictions)
     * @param offsetY amount to change y position by (no restrictions)
     */
    public void offset(int offsetX, int offsetY) {
        offsetX(offsetX);
        offsetY(offsetY);
    }

    /**
     * Increases or decreases the x position of the area by the given amount, but doesn't change its size.
     *
     * @param offset amount to change x position by (no restrictions)
     */
    public void offsetX(int offset) {
        this.x += offset;
    }

    /**
     * Increases or decreases the y position of the area by the given amount, but doesn't change its size.
     *
     * @param offset amount to change y position by (no restrictions)
     */
    public void offsetY(int offset) {
        this.y += offset;
    }

    /**
     * Increases or decreases the size of the area by the given amount, but doesn't change its position.
     *
     * @param grow amount to change size by (no restrictions)
     */
    public void grow(int grow) {
        growW(grow);
        growH(grow);
    }

    /**
     * Increases or decreases the size of the area by the given amount, but doesn't change its position.
     *
     * @param growW amount to change width by (no restrictions)
     * @param growH amount to change height by (no restrictions)
     */
    public void grow(int growW, int growH) {
        growW(growW);
        growH(growH);
    }

    /**
     * Increases or decreases the width of the area by the given amount, but doesn't change its position.
     *
     * @param grow amount to change width by (no restrictions)
     */
    public void growW(int grow) {
        this.width += grow;
    }

    /**
     * Increases or decreases the height of the area by the given amount, but doesn't change its position.
     *
     * @param grow amount to change height by (no restrictions)
     */
    public void growH(int grow) {
        this.height += grow;
    }

    /**
     * Set all values
     */
    public void set(int x, int y, int w, int h) {
        this.setPos(x, y);
        this.setSize(w, h);
    }

    /**
     * Set the relative position
     */
    public void setRelativePos(int rx, int ry) {
        this.rx = rx;
        this.ry = ry;
    }

    /**
     * Set the position
     */
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the size
     */
    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public void setPos(Rectangle rectangle) {
        setPos(rectangle.x, rectangle.y);
    }

    public void setSize(Rectangle rectangle) {
        setSize(rectangle.width, rectangle.height);
    }

    /**
     * Sets position and size by specifying top left and bottom right corner position.
     *
     * @param sx x position of top left corner
     * @param sy y position of top left corner
     * @param ex x position of bottom right corner
     * @param ey y position of bottom right corner
     */
    public void setPos(int sx, int sy, int ex, int ey) {
        int x0 = Math.min(sx, ex);
        int y0 = Math.min(sy, ey);
        ex = Math.max(sx, ex);
        ey = Math.max(sy, ey);
        setPos(x0, y0);
        setSize(ex - x0, ey - y0);
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public void set(Rectangle area) {
        setBounds(area.x, area.y, area.width, area.height);
    }

    /**
     * Transforms the four corners of this rectangle with the given pose stack. The new rectangle can be rotated.
     * Then a min fit rectangle, which is not rotated and aligned with the screen, is put around the corner.
     *
     * @param stack pose stack
     */
    public void transformAndRectanglerize(IViewportStack stack) {
        int xTL = stack.transformX(this.x, this.y), xTR = stack.transformX(ex(), this.y),
                xBL = stack.transformX(this.x, ey()), xBR = stack.transformX(ex(), ey());
        int yTL = stack.transformY(this.x, this.y), yTR = stack.transformY(ex(), this.y),
                yBL = stack.transformY(this.x, ey()), yBR = stack.transformY(ex(), ey());
        int x0 = GTMath.min(xTL, xTR, xBL, xBR);
        int x1 = GTMath.max(xTL, xTR, xBL, xBR);
        int y0 = GTMath.min(yTL, yTR, yBL, yBR);
        int y1 = GTMath.max(yTL, yTR, yBL, yBR);
        setPos(x0, y0, x1, y1);
    }

    @Override
    public boolean resize(IGuiElement guiElement) {
        guiElement.getArea().set(this);
        return true;
    }

    @Override
    public Area getArea() {
        return this;
    }

    /**
     * This creates a copy, but it only copies position and size.
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
                ", width=" + this.width +
                ", height=" + this.height +
                '}';
    }
}
