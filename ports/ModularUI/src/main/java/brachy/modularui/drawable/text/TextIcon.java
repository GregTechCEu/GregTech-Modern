package brachy.modularui.drawable.text;

import brachy.modularui.api.drawable.IIcon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.sizer.Box;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class TextIcon implements IIcon {

    @Getter
    private final Component text;
    @Getter
    private final int width, height;
    private final float scale;
    private final Alignment alignment;
    private static final Box margin = new Box();

    public TextIcon(Component text, int width, int height, float scale, Alignment alignment) {
        this.text = text;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.alignment = alignment;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        TextRenderer.SHARED.setPos(x, y);
        TextRenderer.SHARED.setAlignment(this.alignment, width);
        TextRenderer.SHARED.setScale(this.scale);
        TextRenderer.SHARED.drawSimple(context.getGraphics(), this.text);
    }

    @Override
    @Nullable
    public IIcon getWrappedDrawable() {
        return null;
    }

    @Override
    public Box getMargin() {
        return margin;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof TextIcon textIcon)) return false;

        return width == textIcon.width && height == textIcon.height && Float.compare(scale, textIcon.scale) == 0 &&
                text.equals(textIcon.text) && alignment.equals(textIcon.alignment);
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + Float.hashCode(scale);
        result = 31 * result + alignment.hashCode();
        return result;
    }
}
