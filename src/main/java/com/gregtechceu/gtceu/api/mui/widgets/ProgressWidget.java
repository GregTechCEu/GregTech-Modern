package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.value.IDoubleValue;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.DoubleValue;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.util.Mth;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.DoubleSupplier;

@Accessors(fluent = true, chain = true)
public class ProgressWidget extends Widget<ProgressWidget> {

    private final UITexture[] fullTexture = new UITexture[4];
    private UITexture emptyTexture;
    @Setter
    private Direction direction = Direction.RIGHT;
    private int imageSize = -1;

    private IDoubleValue<?> doubleValue;

    @Override
    public void onInit() {
        if (this.doubleValue == null) {
            this.doubleValue = new DoubleValue(0.5);
        }
        if (this.direction == Direction.CIRCULAR_CW && this.fullTexture[0] != null) {
            UITexture base = this.fullTexture[0];
            this.fullTexture[0] = base.getSubArea(0f, 0.5f, 0.5f, 1f);
            this.fullTexture[1] = base.getSubArea(0f, 0f, 0.5f, 0.5f);
            this.fullTexture[2] = base.getSubArea(0.5f, 0f, 1f, 0.5f);
            this.fullTexture[3] = base.getSubArea(0.5f, 0.5f, 1f, 1f);
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.doubleValue = castIfTypeElseNull(syncHandler, IDoubleValue.class);
        return this.doubleValue != null;
    }

    @Override
    public void onResized() {
        if (this.imageSize < 0) {
            this.imageSize = getArea().width;
        }
    }

    public float getCurrentProgress() {
        return (float) this.doubleValue.getDoubleValue();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (this.emptyTexture != null) {
            this.emptyTexture.draw(context, 0, 0, getArea().w(), getArea().h(), widgetTheme);
            Color.setGlColorOpaque(Color.WHITE.main);
        }
        float progress = getCurrentProgress();
        if (this.fullTexture[0] != null && progress > 0) {
            if (this.direction == Direction.CIRCULAR_CW) {
                drawCircular(context, progress, widgetTheme);
                return;
            }
            if (progress >= 1) {
                this.fullTexture[0].draw(context, 0, 0, getArea().w(), getArea().h(), widgetTheme);
            } else {
                progress = getProgressUV(progress);
                float u0 = 0, v0 = 0, u1 = 1, v1 = 1;
                float x = 0, y = 0, width = getArea().width, height = getArea().height;
                switch (this.direction) {
                    case RIGHT:
                        u1 = progress;
                        width *= progress;
                        break;
                    case LEFT:
                        u0 = 1 - progress;
                        width *= progress;
                        x = getArea().width - width;
                        break;
                    case DOWN:
                        v1 = progress;
                        height *= progress;
                        break;
                    case UP:
                        v0 = 1 - progress;
                        height *= progress;
                        y = getArea().height - height;
                        break;
                }
                this.fullTexture[0].drawSubArea(context, x, y, width, height, u0, v0, u1, v1, widgetTheme);
            }
        }
    }

    public float getProgressUV(float uv) {
        if (getScreen().getCurrentTheme().getSmoothProgressBarOverride()) {
            return uv;
        }
        return (float) (Math.floor(uv * this.imageSize) / this.imageSize);
    }

    private void drawCircular(GuiContext context, float progress, WidgetTheme widgetTheme) {
        float[] subAreas = {
                getProgressUV(Mth.clamp(progress / 0.25f, 0, 1)),
                getProgressUV(Mth.clamp((progress - 0.25f) / 0.25f, 0, 1)),
                getProgressUV(Mth.clamp((progress - 0.5f) / 0.25f, 0, 1)),
                getProgressUV(Mth.clamp((progress - 0.75f) / 0.25f, 0, 1))
        };
        float halfWidth = getArea().width / 2f;
        float halfHeight = getArea().height / 2f;

        float progressScaled = subAreas[0] * halfHeight;
        this.fullTexture[0].drawSubArea(context,
                0, getArea().height - progressScaled,
                halfWidth, progressScaled,
                0.0f, 1.0f - progressScaled / halfHeight,
                1.0f, 1.0f, widgetTheme); // BL, draw UP

        progressScaled = subAreas[1] * halfWidth;
        this.fullTexture[1].drawSubArea(context,
                0, 0,
                progressScaled, halfHeight,
                0.0f, 0.0f,
                progressScaled / (halfWidth), 1.0f,
                widgetTheme); // TL, draw RIGHT

        progressScaled = subAreas[2] * halfHeight;
        this.fullTexture[2].drawSubArea(context,
                halfWidth, 0,
                halfWidth, progressScaled,
                0.0f, 0.0f,
                1.0f, progressScaled / halfHeight,
                widgetTheme); // TR, draw DOWN

        progressScaled = subAreas[3] * halfWidth;
        this.fullTexture[3].drawSubArea(context,
                getArea().width - progressScaled, halfHeight,
                progressScaled, halfHeight,
                1.0f - progressScaled / halfWidth, 0.0f,
                1.0f, 1.0f, widgetTheme); // BR, draw LEFT
    }

    public ProgressWidget value(IDoubleValue<?> value) {
        this.doubleValue = value;
        setValue(value);
        return this;
    }

    public ProgressWidget progress(DoubleSupplier progress) {
        return value(new DoubleValue.Dynamic(progress, null));
    }

    public ProgressWidget progress(double progress) {
        return value(new DoubleValue(progress));
    }

    /**
     * Sets the texture to render
     *
     * @param emptyTexture empty bar, always rendered
     * @param fullTexture  full bar, partly rendered, based on progress
     * @param imageSize    image size in direction of progress. used for non-smooth rendering
     */
    public ProgressWidget texture(UITexture emptyTexture, UITexture fullTexture, int imageSize) {
        this.emptyTexture = emptyTexture;
        this.fullTexture[0] = fullTexture;
        this.imageSize = imageSize;
        return this;
    }

    /**
     * @param texture a texture where the empty and full bar are stacked on top of each other
     */
    public ProgressWidget texture(UITexture texture, int imageSize) {
        return texture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1), imageSize);
    }

    public ProgressWidget direction(Direction direction) {
        this.direction = direction;
        return this;
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        CIRCULAR_CW
    }
}
