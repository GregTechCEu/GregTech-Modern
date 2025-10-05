package com.gregtechceu.gtceu.api.mui.utils;

import net.minecraft.Util;

import lombok.Getter;

public class FpsCounter {

    @Getter
    private int fps = 0;
    private int frameCount = 0;
    private long timer = Util.getMillis();

    public void reset() {
        this.fps = 0;
        this.frameCount = 0;
        this.timer = Util.getMillis();
    }

    public void onDraw() {
        frameCount++;
        long time = Util.getMillis();
        if (time - timer >= 1000) {
            fps = frameCount;
            frameCount = 0;
            timer += 1000;
        }
    }
}
