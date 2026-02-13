package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IIcon;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Box;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import lombok.Getter;

/**
 * A {@link IDrawable} wrapper with a fixed size and an alignment.
 */
public class Icon implements IIcon, IJsonSerializable<Icon> {

    private final IDrawable drawable;
    @Getter
    private int width = 0, height = 0;
    private float aspectRatio = 0;
    @Getter
    private Alignment alignment = Alignment.Center;
    @Getter
    private final Box margin = new Box();

    public Icon(IDrawable drawable) {
        this.drawable = drawable;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        x += this.margin.left();
        y += this.margin.top();
        width -= this.margin.horizontal();
        height -= this.margin.vertical();
        int frameWidth = width;
        int frameHeight = height;
        if (this.width > 0) width = this.width;
        if (this.height > 0) height = this.height;
        if (this.aspectRatio > 0) {
            if (this.width <= 0) {
                if (this.height <= 0) {
                    // width and height is unset, so adjust width or height so that one of them takes the full space
                    float w = width, h = height;
                    float properW = this.aspectRatio * h;
                    if (w > properW) {
                        width = (int) properW;
                    } else if (w < properW) {
                        height = (int) (w / this.aspectRatio);
                    }
                } else {
                    // height is set, so adjust width to height
                    float properW = this.aspectRatio * height;
                    width = (int) properW;
                }
            } else if (this.height <= 0) {
                // width is set, so adjust height to width
                height = (int) (width / this.aspectRatio);
            } else if (GTCEu.isDev()) {
                GTCEu.LOGGER.error("Aspect ratio in Icon can't be applied when width and height are specified");
                // remove aspect ratio to avoid log spamming, it does nothing in the current state anyway
                this.aspectRatio = 0;
            }
        }
        // apply alignment
        if (width != frameWidth) {
            x += (int) (frameWidth * this.alignment.x - width * this.alignment.x);
        }
        if (height != frameHeight) {
            y += (int) (frameHeight * this.alignment.y - height * this.alignment.y);
        }
        this.drawable.draw(context, x, y, width, height, widgetTheme);
    }

    @Override
    public IDrawable getWrappedDrawable() {
        return drawable;
    }

    public Icon expandWidth() {
        return width(0);
    }

    public Icon expandHeight() {
        return height(0);
    }

    public Icon width(int width) {
        this.width = Math.max(0, width);
        return this;
    }

    public Icon height(int height) {
        this.height = Math.max(0, height);
        return this;
    }

    public Icon size(int width, int height) {
        return width(width).height(height);
    }

    public Icon size(int size) {
        return width(size).height(size);
    }

    public Icon aspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    public Icon alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Icon center() {
        return alignment(Alignment.Center);
    }

    public Icon margin(int left, int right, int top, int bottom) {
        this.margin.all(left, right, top, bottom);
        return this;
    }

    public Icon margin(int horizontal, int vertical) {
        this.margin.all(horizontal, vertical);
        return this;
    }

    public Icon margin(int all) {
        this.margin.all(all);
        return this;
    }

    public Icon marginLeft(int val) {
        this.margin.left(val);
        return this;
    }

    public Icon marginRight(int val) {
        this.margin.right(val);
        return this;
    }

    public Icon marginTop(int val) {
        this.margin.top(val);
        return this;
    }

    public Icon marginBottom(int val) {
        this.margin.bottom(val);
        return this;
    }

    @Override
    public void loadFromJson(JsonObject json) {
        this.width = (json.has("autoWidth") || json.has("autoSize")) &&
                JsonHelper.getBoolean(json, true, "autoWidth", "autoSize") ? 0 :
                        JsonHelper.getInt(json, 0, "width", "w", "size");
        this.height = (json.has("autoHeight") || json.has("autoSize")) &&
                JsonHelper.getBoolean(json, true, "autoHeight", "autoSize") ? 0 :
                        JsonHelper.getInt(json, 0, "height", "h", "size");
        this.aspectRatio = JsonHelper.getFloat(json, 0, "aspectRatio");
        this.alignment = JsonHelper.deserialize(json, Alignment.class, Alignment.Center, "alignment", "align");
        this.margin.fromJson(json);
    }

    public static Icon ofJson(JsonObject json) {
        return JsonHelper.deserialize(json, IDrawable.class, IDrawable.EMPTY, "drawable", "icon").asIcon();
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.add("drawable", JsonHelper.serialize(this.drawable));
        json.addProperty("width", this.width);
        json.addProperty("height", this.height);
        json.addProperty("aspectRatio", this.aspectRatio);
        json.add("alignment", JsonHelper.serialize(this.alignment));
        this.margin.toJson(json);
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.drawable.getClass().getSimpleName() + ")";
    }
}
