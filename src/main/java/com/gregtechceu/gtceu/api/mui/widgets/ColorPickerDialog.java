package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.GuiTextures;
import com.gregtechceu.gtceu.api.mui.drawable.HueBar;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.value.DoubleValue;
import com.gregtechceu.gtceu.api.mui.value.StringValue;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;

import java.util.function.Consumer;

public class ColorPickerDialog extends Dialog<Integer> {

    private static final IDrawable handleBackground = new Rectangle().setColor(Color.WHITE.main);

    private int color;
    private final int alpha;
    private final boolean controlAlpha;

    private final Rectangle preview = new Rectangle();
    private final Rectangle sliderBackgroundR = new Rectangle();
    private final Rectangle sliderBackgroundG = new Rectangle();
    private final Rectangle sliderBackgroundB = new Rectangle();
    private final Rectangle sliderBackgroundA = new Rectangle();
    private final Rectangle sliderBackgroundS = new Rectangle();
    private final Rectangle sliderBackgroundV = new Rectangle();

    public ColorPickerDialog(Consumer<Integer> resultConsumer, int startColor, boolean controlAlpha) {
        this("color_picker", resultConsumer, startColor, controlAlpha);
    }

    public ColorPickerDialog(String name, Consumer<Integer> resultConsumer, int startColor, boolean controlAlpha) {
        super(name, resultConsumer);
        this.alpha = Color.getAlpha(startColor);
        updateColor(startColor);
        this.controlAlpha = controlAlpha;
        size(140, controlAlpha ? 106 : 94).background(GuiTextures.MC_BACKGROUND);
        IWidget alphaSlider = controlAlpha ? new Row()
                .widthRel(1f).height(12)
                .child(IKey.str("A: ").asWidget().heightRel(1f))
                .child(createSlider(this.sliderBackgroundA)
                        .bounds(0, 255)
                        .value(new DoubleValue.Dynamic(() -> Color.getAlpha(this.color),
                                val -> updateColor(Color.withAlpha(this.color, (int) val))))) :
                null;

        PagedWidget.Controller controller = new PagedWidget.Controller();
        child(new Column()
                .left(5).right(5).top(5).bottom(5)
                .child(new Row()
                        .left(5).right(5).height(14)
                        .child(new PageButton(0, controller)
                                .sizeRel(0.5f, 1f)
                                .invertSelected(true)
                                .overlay(IKey.str("RGB")))
                        .child(new PageButton(1, controller)
                                .sizeRel(0.5f, 1f)
                                .invertSelected(true)
                                .overlay(IKey.str("HSV"))))
                .child(new Row().widthRel(1f).height(12).marginTop(4)
                        .child(IKey.str("Hex: ").asWidget().heightRel(1f))
                        .child(new TextFieldWidget()
                                .height(12)
                                .expanded()
                                .setValidator(this::validateRawColor)
                                .value(new StringValue.Dynamic(() -> {
                                    if (controlAlpha) {
                                        return "#" + Integer.toHexString(this.color);
                                    }
                                    return "#" + Integer.toHexString(Color.withAlpha(this.color, 0));
                                }, val -> {
                                    try {
                                        updateColor(Integer.decode(val));
                                    } catch (NumberFormatException ignored) {}
                                })))
                        .child(this.preview.asWidget().background(GuiTextures.CHECKBOARD).size(10, 10).margin(1)))
                .child(new PagedWidget<>()
                        .left(5).right(5)
                        .expanded()
                        .controller(controller)
                        .addPage(createRGBPage(alphaSlider))
                        .addPage(createHSVPage(alphaSlider)))
                .child(new Row()
                        .left(10).right(10).height(14)
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(new ButtonWidget<>()
                                .heightRel(1f).width(50)
                                .overlay(IKey.str("Cancel"))
                                .onMousePressed((mouseX, mouseY, button) -> {
                                    animateClose();
                                    return true;
                                }))
                        .child(new ButtonWidget<>()
                                .heightRel(1f).width(50)
                                .overlay(IKey.str("Confirm"))
                                .onMousePressed((mouseX, mouseY, button) -> {
                                    closeWith(this.color);
                                    return true;
                                }))));
    }

    private IWidget createRGBPage(IWidget alphaSlider) {
        return new Column()
                .sizeRel(1f, 1f)
                .child(new Row()
                        .widthRel(1f).height(12)
                        .child(IKey.str("R: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundR)
                                .bounds(0, 255)
                                .value(new DoubleValue.Dynamic(() -> Color.getRed(this.color),
                                        val -> updateColor(Color.withRed(this.color, (int) val))))))
                .child(new Row()
                        .widthRel(1f).height(12)
                        .child(IKey.str("G: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundG)
                                .bounds(0, 255)
                                .value(new DoubleValue.Dynamic(() -> Color.getGreen(this.color),
                                        val -> updateColor(Color.withGreen(this.color, (int) val))))))
                .child(new Row()
                        .widthRel(1f).height(12)
                        .child(IKey.str("B: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundB)
                                .bounds(0, 255)
                                .value(new DoubleValue.Dynamic(() -> Color.getBlue(this.color),
                                        val -> updateColor(Color.withBlue(this.color, (int) val))))))
                .childIf(alphaSlider != null, alphaSlider);
    }

    private IWidget createHSVPage(IWidget alphaSlider) {
        return new Column()
                .sizeRel(1f, 1f)
                .child(new Row()
                        .widthRel(1f).height(12)
                        .child(IKey.str("H: ").asWidget().heightRel(1f))
                        .child(createSlider(new HueBar(GuiAxis.X))
                                .bounds(0, 360)
                                .value(new DoubleValue.Dynamic(() -> Color.getHue(this.color),
                                        val -> updateColor(Color.withHSVHue(this.color, (float) val))))))
                .child(new Row()
                        .widthRel(1f).height(12)
                        .child(IKey.str("S: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundS)
                                .bounds(0, 1)
                                .value(new DoubleValue.Dynamic(() -> Color.getHSVSaturation(this.color),
                                        val -> updateColor(Color.withHSVSaturation(this.color, (float) val))))))
                .child(new Row()
                        .widthRel(1f).height(12)
                        .child(IKey.str("V: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundV)
                                .bounds(0, 1)
                                .value(new DoubleValue.Dynamic(() -> Color.getValue(this.color),
                                        val -> updateColor(Color.withValue(this.color, (float) val))))))
                .childIf(alphaSlider != null, alphaSlider);
    }

    private static SliderWidget createSlider(IDrawable background) {
        return new SliderWidget()
                .expanded()
                .heightRel(1f)
                .background(background.asIcon().size(0, 4))
                .sliderTexture(handleBackground)
                .sliderSize(2, 8);
    }

    private String validateRawColor(String raw) {
        if (!raw.startsWith("#")) {
            if (raw.startsWith("0x") || raw.startsWith("0X")) {
                raw = raw.substring(2);
            }
            return "#" + raw;
        }
        return raw;
    }

    public void updateColor(int color) {
        this.color = color;
        if (!this.controlAlpha) {
            this.color = Color.withAlpha(this.color, this.alpha);
        }
        color = Color.withAlpha(color, 255);
        int rs = Color.withRed(color, 0), re = Color.withRed(color, 255);
        int gs = Color.withGreen(color, 0), ge = Color.withGreen(color, 255);
        int bs = Color.withBlue(color, 0), be = Color.withBlue(color, 255);
        int as = Color.withAlpha(color, 0), ae = Color.withAlpha(color, 255);
        this.sliderBackgroundR.setHorizontalGradient(rs, re);
        this.sliderBackgroundG.setHorizontalGradient(gs, ge);
        this.sliderBackgroundB.setHorizontalGradient(bs, be);
        this.sliderBackgroundA.setHorizontalGradient(as, ae);
        this.sliderBackgroundS.setHorizontalGradient(Color.withHSVSaturation(color, 0f),
                Color.withHSVSaturation(color, 1f));
        this.sliderBackgroundV.setHorizontalGradient(Color.withValue(color, 0f), Color.withValue(color, 1f));
        this.preview.setColor(this.color);
    }
}
