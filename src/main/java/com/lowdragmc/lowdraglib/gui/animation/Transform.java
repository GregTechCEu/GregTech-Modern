package com.lowdragmc.lowdraglib.gui.animation;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.utils.interpolate.IEase;

import it.unimi.dsi.fastutil.floats.FloatConsumer;

public class Transform extends Animation {

    private boolean in = true;

    public Transform offset(int x, int y) {
        return this;
    }

    public Transform setScale(float scale) {
        return this;
    }

    @Override
    public Transform duration(long duration) {
        super.duration(duration);
        return this;
    }

    @Override
    public Transform delay(long delay) {
        super.delay(delay);
        return this;
    }

    @Override
    public Transform ease(IEase ease) {
        super.ease(ease);
        return this;
    }

    @Override
    public Transform onUpdate(FloatConsumer consumer) {
        super.onUpdate(consumer);
        return this;
    }

    public boolean isIn() {
        return in;
    }

    public boolean isOut() {
        return !in;
    }

    public Animation setIn() {
        in = true;
        return this;
    }

    public Animation setOut() {
        in = false;
        return this;
    }

    public void pre(GuiGraphics graphics) {}

    public void post(GuiGraphics graphics) {}

    public Transform scale(float scale) {
        return setScale(scale);
    }
}
