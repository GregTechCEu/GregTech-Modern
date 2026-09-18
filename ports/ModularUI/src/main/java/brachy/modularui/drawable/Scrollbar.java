package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record Scrollbar(boolean striped) implements IDrawable {

    public static final Scrollbar DEFAULT = new Scrollbar(false);
    public static final Scrollbar VANILLA = new Scrollbar(true);

    public static Scrollbar get(boolean striped) {
        return striped ? VANILLA : DEFAULT;
    }

    public static final MapCodec<Scrollbar> CODEC = IDrawable.CODECS.register(
            Codec.BOOL.fieldOf("striped").xmap(Scrollbar::get, Scrollbar::striped),
            "scrollbar", "Scrollbar");

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawRect(context.getGraphics(), x, y, width, height, Color.mix(0xFFEEEEEE, widgetTheme.getColor()));
        GuiDraw.drawRect(context.getGraphics(), x + 1, y + 1, width - 1, height - 1,
                Color.mix(0xFF666666, widgetTheme.getColor()));
        GuiDraw.drawRect(context.getGraphics(), x + 1, y + 1, width - 2, height - 2,
                Color.mix(0xFFAAAAAA, widgetTheme.getColor()));

        if (striped()) {
            if (height <= 5 && width <= 5) return;
            int color = widgetTheme.getTextColor();
            if (height >= width) {
                int start = y + 2;
                int end = height + start - 4;
                for (int cy = start; cy < end; cy += 2) {
                    GuiDraw.drawRect(context.getGraphics(), x + 2, cy, width - 4, 1, color);
                }
            } else {
                int start = x + 2;
                int end = width + start - 4;
                for (int cx = start; cx < end; cx += 2) {
                    GuiDraw.drawRect(context.getGraphics(), cx, y + 2, 1, height - 4, color);
                }
            }
        }
    }

    @Override
    public boolean canApplyTheme() {
        return true;
    }
}
