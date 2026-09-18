package brachy.modularui.drawable.schema;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;
import org.joml.Vector3f;

import java.util.Arrays;

public class Viewport {

    @Getter private final int[] viewport = new int[4];

    public void setX(int x) {
        this.viewport[0] = x;
    }

    public void setY(int y) {
        this.viewport[1] = y;
    }

    public void setW(int w) {
        this.viewport[2] = w;
    }

    public void setH(int h) {
        this.viewport[3] = h;
    }

    public int getX() {
        return this.viewport[0];
    }

    public int getY() {
        return this.viewport[1];
    }

    public int getW() {
        return this.viewport[2];
    }

    public int getH() {
        return this.viewport[3];
    }

    public void calculateOpenGLViewportFromRectangle(int x, int y, int width, int height) {
        Window window = Minecraft.getInstance().getWindow();
        double guiScale = window.getGuiScale();
        setX(Mth.ceil(x * guiScale));
        setY(window.getHeight() - Mth.ceil((y + height) * guiScale));
        setW(Mth.ceil(width * guiScale));
        setH(Mth.ceil(height * guiScale));
    }

    public void applyViewport() {
        RenderSystem.viewport(getX(), getY(), getW(), getH());
    }

    public float rescaleXToViewport(float x, float width) {
        return getX() + (int) (x / width * getW());
    }

    public float rescaleYToViewport(float y, float height) {
        return getY() + (int) ((1.0f - y / height) * (getH() - 1));
    }

    public Vector3f rescaleToViewport(float x, float y, float width, float height, Vector3f dest) {
        dest.x = rescaleXToViewport(x, width);
        dest.y = rescaleYToViewport(y, height);
        return dest;
    }

    public float unscaleXFromViewport(float viewportX, float nonViewportWidth) {
        return ((viewportX - getX()) / (float) getW()) * nonViewportWidth;
    }

    public float unscaleYFromViewport(float viewportY, float nonViewportHeight) {
        return (1f - ((viewportY - getY()) / (float) getH())) * nonViewportHeight;
    }

    public Vector3f unscaleFromViewport(float viewportX, float viewportY, float nonViewportWidth, float nonViewportHeight, Vector3f dest) {
        dest.x = unscaleXFromViewport(viewportX, nonViewportWidth);
        dest.y = unscaleYFromViewport(viewportY, nonViewportHeight);
        return dest;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Viewport other = (Viewport) o;
        return Arrays.equals(this.viewport, other.viewport);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.viewport);
    }
}
