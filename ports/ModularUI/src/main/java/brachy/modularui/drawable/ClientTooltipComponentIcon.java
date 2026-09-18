package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.sizer.Box;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class ClientTooltipComponentIcon implements IIcon {

    @Getter private final ClientTooltipComponent clientTooltipComponent;
    private Font lastFont;

    public ClientTooltipComponentIcon(ClientTooltipComponent clientTooltipComponent) {
        this.clientTooltipComponent = clientTooltipComponent;
    }

    @Override
    public @Nullable IDrawable getWrappedDrawable() {
        return null;
    }

    @Override
    public int getWidth() {
        return this.clientTooltipComponent.getWidth(this.lastFont != null ? this.lastFont : Minecraft.getInstance().font);
    }

    @Override
    public int getHeight() {
        return this.clientTooltipComponent.getHeight();
    }

    @Override
    public Box getMargin() {
        return Box.ZERO;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        this.lastFont = context.getFont();
        this.clientTooltipComponent.renderText(this.lastFont, x, y, context.getLastGraphicsPose(), context.getGraphics().bufferSource());
        this.clientTooltipComponent.renderImage(this.lastFont, x, y, context.getGraphics());
    }
}
