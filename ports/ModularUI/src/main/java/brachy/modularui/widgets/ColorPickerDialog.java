package brachy.modularui.widgets;

import brachy.modularui.ModularUI;
import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.HueBar;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;

public class ColorPickerDialog extends Dialog<Integer, ColorPickerDialog> {

    private static final IDrawable handleBackground = new Rectangle().color(Color.WHITE.main);

    @Getter private int color;
    @Getter private int red;
    @Getter private int green;
    @Getter private int blue;
    @Getter private double hue;
    @Getter private double saturation;
    private double value;

    private int alpha;
    @Getter private final boolean controlAlpha;

    private final Rectangle preview = new Rectangle();
    private final Rectangle sliderBackgroundR = new Rectangle();
    private final Rectangle sliderBackgroundG = new Rectangle();
    private final Rectangle sliderBackgroundB = new Rectangle();
    private final Rectangle sliderBackgroundA = new Rectangle();
    private final Rectangle sliderBackgroundS = new Rectangle();
    private final Rectangle sliderBackgroundV = new Rectangle();

    public ColorPickerDialog(String name, int startColor, boolean controlAlpha) {
        super(name);

        this.controlAlpha = controlAlpha;
        this.alpha = Color.getAlpha(startColor);
        updateAll(startColor);
        size(140, controlAlpha ? 106 : 94);

        PagedWidget.Controller controller = new PagedWidget.Controller();
        child(Flow.col()
                .left(5).right(5).top(5).bottom(5)
                .child(Flow.row()
                        .left(5).right(5).height(14)
                        .child(new PageButton(0, controller)
                                .sizeRel(0.5f, 1f)
                                .overlay(Text.str("RGB")))
                        .child(new PageButton(1, controller)
                                .sizeRel(0.5f, 1f)
                                .overlay(Text.str("HSV"))))
                .child(Flow.row().widthRel(1f).height(12).marginTop(4)
                        .child(Text.str("Hex: ").asWidget().heightRel(1f))
                        .child(new TextFieldWidget()
                                .height(12)
                                .expanded()
                                .setValidator(this::validateRawColor)
                                .value(new StringValue.Dynamic(() -> {
                                    if (controlAlpha) {
                                        return "#" + Color.toFullHexString(this.red, this.green, this.blue, this.alpha);
                                    }
                                    return "#" + Color.toFullHexString(this.red, this.green, this.blue);
                                }, val -> {
                                    try {
                                        updateAll((int) (long) Long.decode(val));
                                    } catch (NumberFormatException ignored) {
                                        ModularUI.LOGGER.error("Illegal color string '{}'", val);
                                    }
                                })))
                        .child(this.preview.asWidget().background(GuiTextures.CHECKBOARD).size(10, 10).margin(1)))
                .child(new PagedWidget<>()
                        .left(5).right(5)
                        .expanded()
                        .controller(controller)
                        .addPage(createRGBPage(createAlphaSlider("rgb")))
                        .addPage(createHSVPage(createAlphaSlider("hsv"))))
                .child(Flow.row()
                        .left(10).right(10).height(14)
                        .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                        .child(new ButtonWidget<>()
                                .heightRel(1f).width(50)
                                .overlay(Text.str("Cancel"))
                                .onMousePressed((context, button) -> {
                                    closeIfOpen();
                                    return true;
                                }))
                        .child(new ButtonWidget<>()
                                .heightRel(1f).width(50)
                                .overlay(Text.str("Confirm"))
                                .onMousePressed((context, button) -> {
                                    closeWith(this.color);
                                    return true;
                                }))));
    }

    private IWidget createRGBPage(@Nullable Supplier<IWidget> alphaSlider) {
        return Flow.col()
                .sizeRel(1f, 1f)
                .child(Flow.row()
                        .widthRel(1f).height(12)
                        .child(Text.str("R: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundR)
                                .name("red")
                                .bounds(0, 255)
                                .value(new DoubleValue.Dynamic(() -> this.red, this::updateRed))))
                .child(Flow.row()
                        .widthRel(1f).height(12)
                        .child(Text.str("G: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundG)
                                .name("green")
                                .bounds(0, 255)
                                .value(new DoubleValue.Dynamic(() -> this.green, this::updateGreen))))
                .child(Flow.row()
                        .widthRel(1f).height(12)
                        .child(Text.str("B: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundB)
                                .name("blue")
                                .bounds(0, 255)
                                .value(new DoubleValue.Dynamic(() -> this.blue, this::updateBlue))))
                .childIf(alphaSlider != null, alphaSlider);
    }

    private IWidget createHSVPage(@Nullable Supplier<IWidget> alphaSlider) {
        return Flow.col()
                .sizeRel(1f, 1f)
                .child(Flow.row()
                        .widthRel(1f).height(12)
                        .child(Text.str("H: ").asWidget().heightRel(1f))
                        .child(createSlider(new HueBar(GuiAxis.X))
                                .name("hue")
                                .bounds(0, 360)
                                .value(new DoubleValue.Dynamic(() -> this.hue, this::updateHue))))
                .child(Flow.row()
                        .widthRel(1f).height(12)
                        .child(Text.str("S: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundS)
                                .name("saturation")
                                .bounds(0, 1)
                                .value(new DoubleValue.Dynamic(() -> this.saturation, this::updateSaturation))))
                .child(Flow.row()
                        .widthRel(1f).height(12)
                        .child(Text.str("V: ").asWidget().heightRel(1f))
                        .child(createSlider(this.sliderBackgroundV)
                                .name("value")
                                .bounds(0, 1)
                                .value(new DoubleValue.Dynamic(() -> this.value, this::updateValue))))
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

    private @Nullable Supplier<IWidget> createAlphaSlider(String s) {
        if (!controlAlpha) {
            return null;
        }
        return () -> Flow.row()
                .widthRel(1f).height(12)
                .child(Text.str("A: ").asWidget().heightRel(1f))
                .child(createSlider(this.sliderBackgroundA)
                        .name("alpha " + s)
                        .bounds(0, 255)
                        .value(new DoubleValue.Dynamic(() -> this.alpha, this::updateAlpha)));
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

    private void updateRed(double v) {
        this.red = (int) v;
        updateFromRGB();
    }

    private void updateGreen(double v) {
        this.green = (int) v;
        updateFromRGB();
    }

    private void updateBlue(double v) {
        this.blue = (int) v;
        updateFromRGB();
    }

    private void updateHue(double v) {
        this.hue = v;
        updateFromHSV();
    }

    private void updateSaturation(double v) {
        this.saturation = v;
        updateFromHSV();
    }

    private void updateValue(double v) {
        this.value = v;
        updateFromHSV();
    }

    private void updateAlpha(double v) {
        if (!this.controlAlpha) return;
        this.alpha = (int) v;
        this.color = Color.withAlpha(this.color, this.alpha);
    }

    private void updateFromRGB() {
        this.color = Color.argb(this.red, this.green, this.blue, this.alpha);
        this.saturation = Color.getHSVSaturation(this.color);
        this.value = Color.getValue(this.color);
        this.hue = Color.getHue(this.color);
        updateColor(this.color);
    }

    private void updateFromHSV() {
        this.color = Color.ofHSV((float) this.hue, (float) this.saturation, (float) this.value, this.alpha);
        this.red = Color.getRed(this.color);
        this.green = Color.getGreen(this.color);
        this.blue = Color.getBlue(this.color);
        updateColor(this.color);
    }

    public void updateAll(int color) {
        if (!this.controlAlpha) {
            color = Color.withAlpha(color, this.alpha);
        }
        this.color = color;
        this.alpha = Color.getAlpha(color);
        this.red = Color.getRed(color);
        this.green = Color.getGreen(color);
        this.blue = Color.getBlue(color);
        this.hue = Color.getHue(color);
        this.saturation = Color.getHSVSaturation(color);
        this.value = Color.getValue(color);
        updateColor(color);
    }

    public void updateColor(int color) {
        color = Color.withAlpha(color, 255);
        int rs = Color.withRed(color, 0), re = Color.withRed(color, 255);
        int gs = Color.withGreen(color, 0), ge = Color.withGreen(color, 255);
        int bs = Color.withBlue(color, 0), be = Color.withBlue(color, 255);
        int as = Color.withAlpha(color, 0), ae = Color.withAlpha(color, 255);
        this.sliderBackgroundR.horizontalGradient(rs, re);
        this.sliderBackgroundG.horizontalGradient(gs, ge);
        this.sliderBackgroundB.horizontalGradient(bs, be);
        this.sliderBackgroundA.horizontalGradient(as, ae);
        this.sliderBackgroundS.horizontalGradient(Color.withHSVSaturation(color, 0f),
                Color.withHSVSaturation(color, 1f));
        this.sliderBackgroundV.horizontalGradient(Color.withValue(color, 0f), Color.withValue(color, 1f));
        this.preview.color(color);
    }

    public double getHSVValue() {
        return value;
    }

    public int getColorAlpha() {
        return alpha;
    }
}
