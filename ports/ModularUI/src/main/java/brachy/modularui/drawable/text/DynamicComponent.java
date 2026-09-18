package brachy.modularui.drawable.text;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widgets.TextWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DynamicComponent implements Component, IDrawable {

    private long time = -1;
    private final Supplier<Component> supplier;
    private Style style = Style.EMPTY;
    @Getter private float scale = 1f;
    private Component lastComp;

    public DynamicComponent(Supplier<Component> supplier) {
        this.supplier = supplier;
    }

    private Component getComp() {
        if (supplier == null) {
            return Component.empty();
        }
        if (!ModularUI.isClientSide()) {
            this.lastComp = this.supplier.get();
            if (this.lastComp instanceof MutableComponent mutableComponent) {
                mutableComponent.setStyle(mutableComponent.getStyle().applyTo(this.style));
            }
        } else if (this.time != ClientScreenHandler.getTicks()) {
            this.lastComp = supplier.get();
            this.time = ClientScreenHandler.getTicks();
            if (this.lastComp instanceof MutableComponent mutableComponent) {
                mutableComponent.setStyle(mutableComponent.getStyle().applyTo(this.style));
            }
        }
        return this.lastComp;
    }

    @Override
    public @NotNull Style getStyle() {
        return getComp().getStyle();
    }

    @Override
    public @NotNull ComponentContents getContents() {
        return getComp().getContents();
    }

    @Override
    public @NotNull List<Component> getSiblings() {
        return getComp().getSiblings();
    }

    @Override
    public @NotNull FormattedCharSequence getVisualOrderText() {
        return getComp().getVisualOrderText();
    }

    @Override
    public TextWidget<?> asWidget() {
        return new TextWidget<>(this::getComp);
    }

    public DynamicComponent scale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        Component comp = getComp();
        if (comp instanceof MutableComponent mutableComponent) {
            Style currentStyle = mutableComponent.getStyle();
            mutableComponent.setStyle(currentStyle.applyTo(this.style));
            if (mutableComponent instanceof ModularComponent modularComponent) {
                float s = modularComponent.getScale();
                modularComponent.scale(s * this.scale);
                modularComponent.draw(context, x, y, width, height, widgetTheme);
                modularComponent.scale(s);
            } else {
                FontRenderHelper.drawComponent(comp, context, x, y, width, height, widgetTheme, this.scale);
            }
            mutableComponent.setStyle(currentStyle);
        } else {
            FontRenderHelper.drawComponent(comp, context, x, y, width, height, widgetTheme, this.scale);
        }
    }

    public DynamicComponent fallbackStyle(Style style) {
        this.style = style;
        return this;
    }

    public DynamicComponent fallbackStyle(ChatFormatting style) {
        return fallbackStyle(this.style.applyFormat(style));
    }

    public DynamicComponent fallbackStyle(ChatFormatting... style) {
        return fallbackStyle(this.style.applyFormats(style));
    }

    public Style getFallbackStyle() {
        return this.style;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DynamicComponent that)) return false;

        return Objects.equals(supplier, that.supplier) && style.equals(that.style);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(supplier);
        result = 31 * result + style.hashCode();
        return result;
    }
}
