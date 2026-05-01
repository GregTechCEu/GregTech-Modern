package com.lowdragmc.lowdraglib.gui.animation;

import com.gregtechceu.gtceu.core.compat.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.lowdragmc.lowdraglib.utils.interpolate.IEase;

import it.unimi.dsi.fastutil.floats.FloatConsumer;

public class Animation {

    private Widget widget;
    private Runnable onFinish = () -> {};

    public Animation setWidget(Widget widget) {
        this.widget = widget;
        return this;
    }

    public Widget getWidget() {
        return widget;
    }

    public boolean isFinish() {
        return true;
    }

    public Animation appendOnFinish(Runnable runnable) {
        Runnable previous = onFinish;
        onFinish = () -> {
            previous.run();
            runnable.run();
        };
        return this;
    }

    public Runnable getOnFinish() {
        return onFinish;
    }

    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {}

    public Animation duration(long duration) {
        return this;
    }

    public Animation delay(long delay) {
        return this;
    }

    public Animation ease(IEase ease) {
        return this;
    }

    public Animation onUpdate(FloatConsumer consumer) {
        return this;
    }

    public Animation onFinish(Runnable runnable) {
        onFinish = runnable;
        return this;
    }

    public Animation size(Size size) {
        return this;
    }

    public Animation position(Position position) {
        return this;
    }
}
