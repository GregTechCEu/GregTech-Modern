package brachy.modularui.utils;

import net.minecraft.client.renderer.Rect2i;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;

public class Rectangle {

    @Getter
    @Setter
    public int x;
    @Getter
    @Setter
    public int y;
    @Getter
    @Setter
    public int width;
    @Getter
    @Setter
    public int height;

    public Rectangle() {
        this(0, 0, 0, 0);
    }

    public Rectangle(Rectangle toCopy) {
        this(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
        this.y = ey - this.height;
    }

    public int mx() {
        return (int) (this.x + this.width * 0.5);
    }

    public int my() {
        return (int) (this.y + this.height * 0.5);
    }

    public Rectangle intersect(Rectangle other) {
        int i = this.x;
        int j = this.y;
        int k = this.x + this.width;
        int l = this.y + this.height;
        int i1 = other.getX();
        int j1 = other.getY();
        int k1 = i1 + other.getWidth();
        int l1 = j1 + other.getHeight();
        this.x = Math.max(i, i1);
        this.y = Math.max(j, j1);
        this.width = Math.max(0, Math.min(k, k1) - this.x);
        this.height = Math.max(0, Math.min(l, l1) - this.y);
        return this;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setBounds(Rectangle toCopy) {
        setBounds(toCopy.x, toCopy.y, toCopy.width, toCopy.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

    public void subArea(float u0, float v0, float u1, float v1) {
        set((int) (this.x + u0 * this.width), (int) (this.y + v0 * this.height), (int) ((u1 - u0) * this.width), (int) ((v1 - v0) * this.height));
    }

    public PointF getCenter() {
        return new PointF(x + width / 2.0f, y + height / 2.0f);
    }

    @OnlyIn(Dist.CLIENT)
    public Rect2i asRect2i() {
        return new Rect2i(this.x, this.y, this.width, this.height);
    }

    public boolean contains(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    public boolean contains(Point point) {
        return contains(point.x, point.y);
    }
}
