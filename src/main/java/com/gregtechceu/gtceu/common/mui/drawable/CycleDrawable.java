package com.gregtechceu.gtceu.common.mui.drawable;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IJsonSerializable;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.json.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record CycleDrawable(IDrawable... drawables) implements IDrawable, IJsonSerializable<CycleDrawable> {

    public @Nullable IDrawable getCurrent() {
        if (this.drawables.length == 0) return null;
        return this.drawables[Math.abs((int) (System.currentTimeMillis() / 1000) % this.drawables.length)];
    }

    @Override
    public boolean canApplyTheme() {
        IDrawable current = getCurrent();
        return current != null && current.canApplyTheme();
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        IDrawable current = getCurrent();
        if (current == null) return;

        current.draw(context, x, y, width, height, widgetTheme);
    }

    public static IDrawable parseJson(JsonObject json) {
        JsonElement drawables = JsonHelper.getJsonElement(json, "drawables", "children");
        if (drawables != null && drawables.isJsonArray()) {
            return parseJson(drawables.getAsJsonArray());
        }
        ModularUI.LOGGER.throwing(
                new JsonParseException("CycleDrawable json should have an array named 'drawables' or 'children'."));
        return IDrawable.EMPTY;
    }

    public static IDrawable parseJson(JsonArray drawables) {
        List<IDrawable> list = new ArrayList<>();
        for (JsonElement child : drawables) {
            IDrawable drawable = JsonHelper.deserialize(child, IDrawable.class);
            if (drawable != null) {
                list.add(drawable);
            }
        }
        if (list.isEmpty()) {
            return IDrawable.EMPTY;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return new CycleDrawable(list.toArray(IDrawable[]::new));
    }

    // this method should never be called, but the special casing code is copied here in case it does.
    @Override
    public boolean saveToJson(JsonObject json) {
        JsonArray jsonArray = new JsonArray();
        for (IDrawable drawable : this.drawables()) {
            jsonArray.add(JsonHelper.serialize(drawable));
        }
        json.add("drawables", jsonArray);
        return true;
    }
}
