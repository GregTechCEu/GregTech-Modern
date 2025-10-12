package com.gregtechceu.gtceu.client.mui.screen.event;

import com.gregtechceu.gtceu.api.mui.base.drawable.IRichTextBuilder;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RichTooltipEvent {

    private RichTooltipEvent() {}

    @Cancelable
    public static class Pre extends RenderTooltipEvent.Pre {

        @Getter
        private final IRichTextBuilder<?> tooltip;

        public Pre(@NotNull ItemStack stack, @NotNull GuiGraphics graphics,
                   int x, int y, int screenWidth, int screenHeight, @NotNull Font font,
                   @NotNull List<ClientTooltipComponent> components, @NotNull ClientTooltipPositioner positioner,
                   IRichTextBuilder<?> tooltip) {
            super(stack, graphics, x, y, screenWidth, screenHeight, font, components, positioner);
            this.tooltip = tooltip;
        }
    }

    public static class Color extends RenderTooltipEvent.Color {

        @Getter
        private final IRichTextBuilder<?> tooltip;

        public Color(@NotNull ItemStack stack, @NotNull GuiGraphics graphics,
                     int x, int y, @NotNull Font font, int background, int borderStart, int borderEnd,
                     @NotNull List<ClientTooltipComponent> components, IRichTextBuilder<?> tooltip) {
            super(stack, graphics, x, y, font, background, borderStart, borderEnd, components);
            this.tooltip = tooltip;
        }
    }
}
