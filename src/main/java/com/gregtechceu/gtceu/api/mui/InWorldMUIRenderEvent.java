package com.gregtechceu.gtceu.api.mui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Event;

@AllArgsConstructor
@Getter
public class InWorldMUIRenderEvent extends Event {
    private final GuiGraphics graphics;
    private final float partialTick;
}
