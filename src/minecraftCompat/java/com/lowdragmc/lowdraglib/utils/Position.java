package com.lowdragmc.lowdraglib.utils;

import net.minecraft.world.phys.Vec2;

import org.joml.Vector2f;

import java.util.Objects;

public class Position {

    public static final Position ORIGIN = new Position(0, 0);

    public final int x;
    public final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Position of(int x, int y) {
        return new Position(x, y);
    }

    public Position add(Position position) {
        return add(position.x, position.y);
    }

    public Position add(Size size) {
        return add(size.width, size.height);
    }

    public Position add(int x, int y) {
        return new Position(this.x + x, this.y + y);
    }

    public Position subtract(Position position) {
        return add(-position.x, -position.y);
    }

    public Position addX(int x) {
        return add(x, 0);
    }

    public Position addY(int y) {
        return add(0, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public com.lowdragmc.lowdraglib2.math.Position toLDLib2() {
        return com.lowdragmc.lowdraglib2.math.Position.of(x, y);
    }

    public Vector2f vector2f() {
        return new Vector2f(x, y);
    }

    public Vec2 vec2() {
        return new Vec2(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position position)) return false;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position{x=%d, y=%d}".formatted(x, y);
    }
}
