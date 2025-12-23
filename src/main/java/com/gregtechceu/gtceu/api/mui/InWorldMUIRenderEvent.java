package com.gregtechceu.gtceu.api.mui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InWorldMUIRenderEvent extends Event {

    private final GuiGraphics graphics;
    private final float partialTick;
}
