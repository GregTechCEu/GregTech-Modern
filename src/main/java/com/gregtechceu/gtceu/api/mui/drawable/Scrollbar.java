package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import com.google.gson.JsonObject;
import lombok.Getter;

public class Scrollbar implements IDrawable, IJsonSerializable {

    public static final Scrollbar DEFAULT = new Scrollbar(false);
    public static final Scrollbar VANILLA = new Scrollbar(true);

    public static Scrollbar ofJson(JsonObject json) {
        if (JsonHelper.getBoolean(json, false, "striped", "vanilla")) {
            return VANILLA;
        }
        return DEFAULT;
    }

    @Getter
    private final boolean striped;

    public Scrollbar(boolean striped) {
        this.striped = striped;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawRect(context.getGraphics(), x, y, width, height, Color.mix(0xFFEEEEEE, widgetTheme.getColor()));
        GuiDraw.drawRect(context.getGraphics(), x + 1, y + 1, width - 1, height - 1,
                Color.mix(0xFF666666, widgetTheme.getColor()));
        GuiDraw.drawRect(context.getGraphics(), x + 1, y + 1, width - 2, height - 2,
                Color.mix(0xFFAAAAAA, widgetTheme.getColor()));

        if (isStriped()) {
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

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("striped", this.striped);
        return true;
    }
}
