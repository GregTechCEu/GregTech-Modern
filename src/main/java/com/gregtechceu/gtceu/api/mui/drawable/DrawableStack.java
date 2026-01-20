package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A stack of {@link IDrawable} backed by an array which are drawn on top of each other.
 */
public class DrawableStack implements IDrawable, IJsonSerializable<DrawableStack> {

    public static final IDrawable[] EMPTY_BACKGROUND = {};
    public static final DrawableStack EMPTY = new DrawableStack(EMPTY_BACKGROUND);

    @Getter
    private final IDrawable[] drawables;

    public DrawableStack(IDrawable... drawables) {
        this.drawables = drawables == null || drawables.length == 0 ? EMPTY_BACKGROUND : drawables;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null) drawable.draw(context, x, y, width, height, widgetTheme);
        }
    }

    @Override
    public boolean canApplyTheme() {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null && drawable.canApplyTheme()) {
                return true;
            }
        }
        return false;
    }

    public static IDrawable parseJson(JsonObject json) {
        JsonElement drawables = JsonHelper.getJsonElement(json, "drawables", "children");
        if (drawables != null && drawables.isJsonArray()) {
            return parseJson(drawables.getAsJsonArray());
        }
        GTCEu.LOGGER.throwing(
                new JsonParseException("DrawableStack json should have an array named 'drawables' or 'children'."));
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
        return new DrawableStack(list.toArray(IDrawable[]::new));
    }

    // this method should never be called, but the special casing code is copied here in case it does.
    @Override
    public boolean saveToJson(JsonObject json) {
        JsonArray jsonArray = new JsonArray();
        for (IDrawable drawable : this.getDrawables()) {
            jsonArray.add(JsonHelper.serialize(drawable));
        }
        json.add("drawables", jsonArray);
        return true;
    }
}
