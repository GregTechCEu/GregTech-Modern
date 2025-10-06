package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.value.IDoubleValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiAction;
import com.gregtechceu.gtceu.api.mui.base.widget.Interactable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiTextures;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.DoubleValue;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandler;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Unit;
import com.gregtechceu.gtceu.client.mui.screen.viewport.ModularGuiContext;

import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true)
public class SliderWidget extends Widget<SliderWidget> implements Interactable {

    private IDoubleValue<?> doubleValue;
    private IDrawable stopperDrawable = new Rectangle().setColor(Color.withAlpha(Color.WHITE.main, 0.4f));
    private IDrawable handleDrawable = GuiTextures.BUTTON_CLEAN;
    private GuiAxis axis = GuiAxis.X;
    private DoubleList stopper;
    private int stopperWidth = 2, stopperHeight = 4;
    private final Unit sliderWidth = new Unit(), sliderHeight = new Unit();
    private final Area sliderArea = new Area();
    @Getter
    private double min, max;
    private double each = 0;
    @Getter
    private boolean dragging = false;

    private double cache = Double.MIN_VALUE;

    public SliderWidget() {
        sliderHeight(1f).sliderWidth(6);
        listenGuiAction((IGuiAction.MouseReleased) (mouseX, mouseY, button) -> {
            boolean val = this.dragging;
            this.dragging = false;
            return val;
        });
        bounds(0, 100);
    }

    @Override
    public void onInit() {
        if (this.doubleValue == null) {
            this.doubleValue = new DoubleValue((this.max - this.min) * 0.5 + this.min);
        }
        if (this.each > 0 && this.stopper == null) {
            this.stopper = new DoubleArrayList();
            for (double d = this.min; d < this.max; d += this.each) {
                this.stopper.add(d);
            }
            this.stopper.add(this.max);
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        this.doubleValue = castIfTypeElseNull(syncHandler, IDoubleValue.class);
        return this.doubleValue != null;
    }

    @Override
    public void drawBackground(ModularGuiContext context, WidgetTheme widgetTheme) {
        super.drawBackground(context, widgetTheme);
        if (this.stopper != null && this.stopperDrawable != null && this.stopperWidth > 0 && this.stopperHeight > 0) {
            for (double stop : this.stopper) {
                int pos = valueToPos(stop) + this.sliderArea.getSize(this.axis) / 2;
                if (this.axis.isHorizontal()) {
                    pos -= this.stopperWidth / 2;
                    int crossAxisPos = (int) (getArea().height / 2D - this.stopperHeight / 2D);
                    this.stopperDrawable.draw(context, pos, crossAxisPos, this.stopperWidth, this.stopperHeight,
                            WidgetTheme.getDefault());
                } else {
                    pos -= this.stopperHeight / 2;
                    int crossAxisPos = (int) (getArea().width / 2D - this.stopperWidth / 2D);
                    this.stopperDrawable.draw(context, crossAxisPos, pos, this.stopperWidth, this.stopperHeight,
                            WidgetTheme.getDefault());
                }
            }
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (this.handleDrawable != null) {
            this.handleDrawable.draw(context, this.sliderArea, context.getTheme().getButtonTheme());
        }
    }

    @Override
    public void onResized() {
        float sw = this.sliderWidth.getValue();
        if (this.sliderWidth.isRelative()) sw *= getArea().width;
        float sh = this.sliderHeight.getValue();
        if (this.sliderHeight.isRelative()) sh *= getArea().height;
        this.sliderArea.setSize((int) sw, (int) sh);
        GuiAxis other = this.axis.getOther();
        this.sliderArea.setPoint(other, getArea().getSize(other) / 2 - this.sliderArea.getSize(other) / 2);
    }

    @Override
    public void postResize() {
        setValue(getSliderValue(), false);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.dragging) return;
        double val = getSliderValue();
        if (this.cache != val) {
            setValue(val, false);
        }
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        int p = this.axis.isHorizontal() ?
                getContext().unTransformX(getContext().getMouseX(), getContext().getMouseY()) :
                getContext().unTransformY(getContext().getMouseX(), getContext().getMouseY());
        setValue(posToValue(p), true);
        this.dragging = true;
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging) {
            onMousePressed(mouseX, mouseY, button);
        }
    }

    public double posToValue(int p) {
        double v = (p - this.sliderArea.getSize(this.axis) / 2D) /
                (double) (getArea().getSize(this.axis) - this.sliderArea.getSize(this.axis));
        return v * (this.max - this.min) + this.min;
    }

    public int valueToPos(double value) {
        value -= this.min;
        value /= (this.max - this.min);
        return (int) (value * (getArea().getSize(this.axis) - this.sliderArea.getSize(this.axis)));
    }

    public double getSliderValue() {
        return this.doubleValue == null ? 0.0 : this.doubleValue.getDoubleValue();
    }

    public void setValue(double value, boolean setSource) {
        if (this.stopper != null && !this.stopper.isEmpty()) {
            double lastDistance = Double.MAX_VALUE;
            boolean found = false;
            for (int i = 0; i < this.stopper.size(); i++) {
                double dist = Math.abs(this.stopper.getDouble(i) - value);
                if (dist < lastDistance) {
                    lastDistance = dist;
                } else if (dist > lastDistance) {
                    value = this.stopper.getDouble(i - 1);
                    found = true;
                    break;
                }
            }
            if (!found && lastDistance < Double.MAX_VALUE) {
                value = this.stopper.getDouble(this.stopper.size() - 1);
            }
        }
        value = Mth.clamp(value, this.min, this.max);
        this.cache = value;
        this.sliderArea.setPoint(this.axis, valueToPos(value));
        if (setSource) {
            this.doubleValue.setDoubleValue(value);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " # " + getSliderValue();
    }

    public SliderWidget value(IDoubleValue<?> value) {
        this.doubleValue = value;
        setValue(value);
        return this;
    }

    public SliderWidget bounds(double min, double max) {
        this.max = Math.max(min, max);
        this.min = Math.min(min, max);
        return this;
    }

    public SliderWidget stopper(DoubleList stopper) {
        if (this.stopper == null) this.stopper = new DoubleArrayList();
        this.stopper.addAll(stopper);
        this.stopper.sort(Double::compare);
        return this;
    }

    public SliderWidget stopper(double... stopper) {
        if (this.stopper == null) this.stopper = new DoubleArrayList();
        for (double stop : stopper) {
            this.stopper.add(stop);
        }
        this.stopper.sort(Double::compare);
        return this;
    }

    public SliderWidget stopper(double each) {
        this.each = each;
        return this;
    }

    public SliderWidget setAxis(GuiAxis axis) {
        this.axis = axis;
        return this;
    }

    public SliderWidget sliderWidth(int w) {
        this.sliderWidth.setValue(w);
        this.sliderWidth.setMeasure(Unit.Measure.PIXEL);
        return this;
    }

    public SliderWidget sliderWidth(float w) {
        this.sliderWidth.setValue(w);
        this.sliderWidth.setMeasure(Unit.Measure.RELATIVE);
        return this;
    }

    public SliderWidget sliderHeight(int h) {
        this.sliderHeight.setValue(h);
        this.sliderHeight.setMeasure(Unit.Measure.PIXEL);
        return this;
    }

    public SliderWidget sliderHeight(float h) {
        this.sliderHeight.setValue(h);
        this.sliderHeight.setMeasure(Unit.Measure.RELATIVE);
        return this;
    }

    public SliderWidget sliderSize(int w, int h) {
        return sliderWidth(w).sliderHeight(h);
    }

    public SliderWidget sliderSize(float w, float h) {
        return sliderWidth(w).sliderHeight(h);
    }

    public SliderWidget sliderTexture(IDrawable sliderTexture) {
        this.handleDrawable = sliderTexture;
        return this;
    }

    public SliderWidget stopperTexture(IDrawable sliderTexture) {
        this.stopperDrawable = sliderTexture;
        return this;
    }

    public SliderWidget stopperSize(int w, int h) {
        this.stopperWidth = w;
        this.stopperHeight = h;
        return this;
    }
}
