package net.minecraft.client.gui;

import net.minecraft.client.DeltaTracker;

public final class LayeredDraw {

    private LayeredDraw() {}

    public interface Layer {

        void render(GuiGraphics guiGraphics, DeltaTracker tracker);
    }
}
