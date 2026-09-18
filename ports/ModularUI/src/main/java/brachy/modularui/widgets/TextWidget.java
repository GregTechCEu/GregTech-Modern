package brachy.modularui.widgets;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.sizer.Box;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class TextWidget<W extends TextWidget<W>> extends Widget<W> {

    @Getter private Component key;
    @Getter private Alignment alignment = Alignment.CenterLeft;
    @Getter private IntSupplier color = null;
    @Getter private Boolean textShadow = null;
    @Getter private float scale = 1f;
    @Getter private int maxWidth = -1;

    private String lastText;
    private @Nullable Supplier<Component> keySupplier;

    public TextWidget(@NotNull Supplier<Component> keySupplier) {
        this.keySupplier = keySupplier;
        this.key = keySupplier.get();
    }

    public TextWidget(Component key) {
        this.key = key;
        this.keySupplier = null;
    }

    public TextWidget(String key) {
        this(Text.str(key));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        TextRenderer renderer = TextRenderer.SHARED;
        Component text = checkComponentUpdated();
        WidgetTheme theme = getActiveWidgetTheme(widgetTheme, isHovering());
        renderer.setColor(this.color != null ? this.color.getAsInt() : theme.getTextColor());
        renderer.setAlignment(this.alignment, getArea().paddedWidth() + this.scale, getArea().paddedHeight());
        renderer.setShadow(this.textShadow != null ? this.textShadow : theme.isTextShadow());
        renderer.setPos(getArea().getPadding().left(), getArea().getPadding().top());
        renderer.setScale(this.scale);
        renderer.setSimulate(false);
        renderer.draw(context.getGraphics(), text);
    }

    public W value(Supplier<Component> textSupplier) {
        this.keySupplier = textSupplier;
        return getThis();
    }

    public W value(Component text) {
        this.key = text;
        this.keySupplier = null;
        return getThis();
    }

    public W value(String str) {
        return value(Text.str(str));
    }

    protected Component checkComponentUpdated() {
        if (this.keySupplier != null) {
            var newKey = this.keySupplier.get();
            if (!Objects.equals(newKey, this.key)) {
                this.key = newKey;
            }
        }
        if (!Objects.equals(lastText, this.key.getString())) {
            onTextChanged(this.key);
            this.lastText = this.key.getString();
        }
        return this.key;
    }

    protected void onTextChanged(Component newText) {
        // scheduling it would resize it on next frame, but we need it now
        WidgetTree.resizeInternal(resizer(), false);
    }

    private TextRenderer simulate(float maxWidth) {
        Box padding = getArea().getPadding();
        TextRenderer renderer = TextRenderer.SHARED;
        renderer.setAlignment(Alignment.TopLeft, maxWidth);
        renderer.setPos(padding.left(), padding.top());
        renderer.setScale(this.scale);
        renderer.setSimulate(true);
        // Don't update the key here, otherwise an infinite loop of checkComponentUpdated -> simulate -> checkComponentUpdated occurs
        renderer.draw(null, this.key);
        renderer.setSimulate(false);
        return renderer;
    }

    @Override
    public int getDefaultHeight() {
        float maxWidth;
        if (resizer().isWidthCalculated()) {
            maxWidth = getArea().width + this.scale;
        } else if (this.maxWidth > 0) {
            maxWidth = Math.max(this.maxWidth, 5);
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width + this.scale;
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetHeight(renderer.getLastHeight());
    }

    @Override
    public int getDefaultWidth() {
        float maxWidth;
        if (this.maxWidth > 0) {
            maxWidth = Math.max(this.maxWidth, 5);
        } else if (getParent().resizer().isWidthCalculated()) {
            maxWidth = getParent().getArea().width;
        } else {
            maxWidth = getScreen().getScreenArea().width;
        }
        TextRenderer renderer = simulate(maxWidth);
        return getWidgetWidth(renderer.getLastWidth());
    }

    protected int getWidgetWidth(float actualTextWidth) {
        Box padding = getArea().getPadding();
        return Math.max(1, (int) Math.ceil(actualTextWidth + padding.horizontal()));
    }

    protected int getWidgetHeight(float actualTextHeight) {
        Box padding = getArea().getPadding();
        return Math.max(1, (int) Math.ceil(actualTextHeight + padding.vertical()));
    }

    @Override
    public boolean canHoverThrough() {
        return true;
    }

    @Deprecated
    public W alignment(Alignment alignment) {
        return textAlign(alignment);
    }

    public W textAlign(Alignment alignment) {
        this.alignment = alignment;
        return getThis();
    }

    public W color(int color) {
        return color(() -> color);
    }

    public W color(@Nullable IntSupplier color) {
        this.color = color;
        return getThis();
    }

    public W scale(float scale) {
        this.scale = scale;
        return getThis();
    }

    public W shadow(@Nullable Boolean shadow) {
        this.textShadow = shadow;
        return getThis();
    }

    public W style(ChatFormatting formatting) {
        // TODO
        //this.key.style(formatting);
        return getThis();
    }

    public W maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return getThis();
    }

    public Boolean isShadow() {
        return this.getTextShadow();
    }
}
