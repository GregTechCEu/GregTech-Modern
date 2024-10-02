package com.gregtechceu.gtceu.client.shader;

import com.gregtechceu.gtceu.client.util.RenderUtil;
import net.minecraft.client.Minecraft;

public class PingPongBuffer {

    private static final PostTarget BUFFER_A;
    private static final PostTarget BUFFER_B;
    private static boolean flag;

    static {
        BUFFER_A = new PostTarget(10, 10, false);
        BUFFER_B = new PostTarget(10, 10, false);
        BUFFER_A.setClearColor(0, 0, 0, 0);
        BUFFER_B.setClearColor(0, 0, 0, 0);
    }

    public static void updateSize(int width, int height) {
        RenderUtil.updateFBOSize(BUFFER_A, width, height);
        RenderUtil.updateFBOSize(BUFFER_B, width, height);
    }

    public static void cleanAllUp() {
        BUFFER_A.clear(Minecraft.ON_OSX);
        BUFFER_B.clear(Minecraft.ON_OSX);
    }

    public static PostTarget getCurrentBuffer(boolean clean) {
        PostTarget buffer = flag ? BUFFER_A : BUFFER_B;
        if (clean) {
            buffer.clear(Minecraft.ON_OSX);
        }
        return buffer;
    }

    public static PostTarget getNextBuffer(boolean clean) {
        PostTarget buffer = flag ? BUFFER_B : BUFFER_A;
        if (clean) {
            buffer.clear(Minecraft.ON_OSX);
        }
        return buffer;
    }

    public static PostTarget swap() {
        return swap(false);
    }

    public static PostTarget swap(boolean clean) {
        flag = !flag;
        return getCurrentBuffer(clean);
    }

    public static void bindFramebufferTexture() {
        getCurrentBuffer(false).bindRead();
    }
}
