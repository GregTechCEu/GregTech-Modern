package brachy.modularui.screen.event;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RichTooltipEvent {

    private RichTooltipEvent() {}

    /**
     * A rich tooltip event that is mean ONLY for rich tooltips.
     * {@link Gather.Pre} is invoked before the vanilla event.
     * {@link Gather.Post} is invoked after the vanilla event.
     * Both can be canceled and edited.
     */
    public static class Gather extends Event implements ICancellableEvent {

        @Getter private final IRichTextBuilder<?> tooltip;
        @Getter protected final ItemStack itemStack;
        @Getter protected final GuiContext guiContext;
        @Getter protected int x;
        @Getter protected int y;
        @Getter protected int screenWidth;
        @Getter protected int screenHeight;
        @Getter
        @Setter
        protected int maxWidth;

        protected Gather(IRichTextBuilder<?> tooltip, ItemStack stack, GuiContext guiContext,
                         int x, int y, int screenWidth, int screenHeight, int maxWidth) {
            this.tooltip = tooltip;
            this.itemStack = stack;
            this.guiContext = guiContext;
            this.x = x;
            this.y = y;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.maxWidth = maxWidth;
        }

        public static class Pre extends Gather implements ICancellableEvent {

            public Pre(IRichTextBuilder<?> tooltip, ItemStack stack, GuiContext guiContext,
                       int x, int y, int screenWidth, int screenHeight, int maxWidth) {
                super(tooltip, stack, guiContext, x, y, screenWidth, screenHeight, maxWidth);
            }
        }

        public static class Post extends Gather implements ICancellableEvent {

            public Post(IRichTextBuilder<?> tooltip, ItemStack stack, GuiContext guiContext,
                        int x, int y, int screenWidth, int screenHeight, int maxWidth) {
                super(tooltip, stack, guiContext, x, y, screenWidth, screenHeight, maxWidth);
            }
        }
    }

    public static class Pre extends RenderTooltipEvent.Pre implements ICancellableEvent {

        @Getter
        private final IRichTextBuilder<?> tooltip;

        public Pre(@NotNull ItemStack stack, @NotNull GuiGraphicsExtractor graphics,
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

        public Color(@NotNull ItemStack stack, @NotNull GuiGraphicsExtractor graphics,
                     int x, int y, @NotNull Font font, int background, int borderStart, int borderEnd,
                     @NotNull List<ClientTooltipComponent> components, IRichTextBuilder<?> tooltip) {
            super(stack, graphics, x, y, font, background, borderStart, borderEnd, components);
            this.tooltip = tooltip;
        }
    }
}
