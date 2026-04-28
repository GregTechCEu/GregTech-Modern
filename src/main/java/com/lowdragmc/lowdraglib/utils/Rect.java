package com.lowdragmc.lowdraglib.utils;

public class Rect {

    public final Position position;
    public final Size size;

    public Rect(Position position, Size size) {
        this.position = position;
        this.size = size;
    }

    public Rect(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= position.x && mouseX < position.x + size.width &&
                mouseY >= position.y && mouseY < position.y + size.height;
    }
}
