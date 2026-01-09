package com.gregtechceu.gtceu.client.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.widget.ResizeDragArea;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class CursorHandler {

    // the normal pointer cursor
    public static long CURSOR_NORMAL;
    // text input cursor
    // usually I-beam shaped
    public static long CURSOR_TEXT_INPUT;
    // crosshair cursor
    public static long CURSOR_CROSSHAIR;
    // "hovering over a clickable object" cursor
    // usually a pointing finger
    public static long CURSOR_POINT_HOVERED;
    // "operation not allowed" cursor
    // usually a circle with a line through it
    public static long CURSOR_NOT_ALLOWED;

    // left to right resize cursor
    public static long CURSOR_RESIZE_HORIZONTAL;
    // top to down resize cursor
    public static long CURSOR_RESIZE_VERTICAL;
    // top right to bottom left resize cursor
    public static long CURSOR_RESIZE_TR_BL;
    // top-left to bottom right resize cursor
    public static long CURSOR_RESIZE_TL_BR;
    // omnidirectional resize cursor
    // has arrows up-down and left-right
    public static long CURSOR_RESIZE_ALL;

    private static long windowHandle;

    public static void setCursorResizeIcon(@Nullable ResizeDragArea dragArea) {
        if (dragArea == null) {
            resetCursorIcon();
            return;
        }
        long cursor = switch (dragArea) {
            case TOP_LEFT, BOTTOM_RIGHT -> CURSOR_RESIZE_TL_BR;
            case TOP_RIGHT, BOTTOM_LEFT -> CURSOR_RESIZE_TR_BL;
            case TOP, BOTTOM -> CURSOR_RESIZE_VERTICAL;
            case RIGHT, LEFT -> CURSOR_RESIZE_HORIZONTAL;
        };
        GLFW.glfwSetCursor(windowHandle, cursor);
    }

    public static void resetCursorIcon() {
        GLFW.glfwSetCursor(windowHandle, CURSOR_NORMAL);
    }

    public static long createSafeCursor(int shape) {
        try (GLFWErrorCallback ignored = org.lwjgl.glfw.GLFW.glfwSetErrorCallback(null)) {
            long cursor = GLFW.glfwCreateStandardCursor(shape);
            if (cursor == 0L) { // If can't load platform-specific default cursors
                GTCEu.LOGGER.warn("GLFW: Failed to create standard cursor shape {}. Falling back to default pointer.",
                        shape);
                // TODO: Load custom textures
                return 0L;
            }
            return cursor;
        }
    }

    public static void init() {
        windowHandle = Minecraft.getInstance().getWindow().getWindow();

        // load platform-specific default cursors (instead of using custom textures)

        // GLFW will switch to the default cursor when 0 is passed into glfwSetCursor
        CURSOR_NORMAL = createSafeCursor(GLFW.GLFW_ARROW_CURSOR);
        CURSOR_TEXT_INPUT = createSafeCursor(GLFW.GLFW_IBEAM_CURSOR);
        CURSOR_CROSSHAIR = createSafeCursor(GLFW.GLFW_CROSSHAIR_CURSOR);
        CURSOR_POINT_HOVERED = createSafeCursor(GLFW.GLFW_POINTING_HAND_CURSOR);
        CURSOR_NOT_ALLOWED = createSafeCursor(GLFW.GLFW_NOT_ALLOWED_CURSOR);

        CURSOR_RESIZE_HORIZONTAL = createSafeCursor(GLFW.GLFW_RESIZE_EW_CURSOR);
        CURSOR_RESIZE_VERTICAL = createSafeCursor(GLFW.GLFW_RESIZE_NS_CURSOR);
        CURSOR_RESIZE_TR_BL = createSafeCursor(GLFW.GLFW_RESIZE_NESW_CURSOR);
        CURSOR_RESIZE_TL_BR = createSafeCursor(GLFW.GLFW_RESIZE_NWSE_CURSOR);
        CURSOR_RESIZE_ALL = createSafeCursor(GLFW.GLFW_RESIZE_ALL_CURSOR);
    }

    public static GLFWImage readGLImage(BufferedImage img, boolean inverse, boolean transpose) {
        int size = img.getHeight();
        ByteBuffer buffer = ByteBuffer.allocate(4 * size * size);
        int y = inverse ? 0 : size - 1;
        while (inverse ? y < size : y >= 0) {
            for (int x = 0; x < size; x++) {
                int x0, y0;
                if (transpose) {
                    x0 = y;
                    y0 = x;
                } else {
                    x0 = x;
                    y0 = y;
                }
                int argb = img.getRGB(x0, y0);
                buffer.putInt(argb);
            }
            if (inverse) y++;
            else y--;
        }
        buffer.flip();

        GLFWImage image = GLFWImage.malloc();
        image.width(size).height(size).pixels(buffer);
        return image;
    }
}
