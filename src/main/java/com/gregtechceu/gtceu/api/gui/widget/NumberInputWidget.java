package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget containing an integer input field, as well as adjacent buttons for increasing or decreasing the value.
 *
 * <p>
 * The buttons' change amount can be altered with Ctrl, Shift, or both.<br>
 * The input is limited by a minimum and maximum value.
 * </p>
 */
public abstract class NumberInputWidget<T extends Number> extends WidgetGroup {

    protected abstract T defaultMin();

    protected abstract T defaultMax();

    protected abstract String toText(T value);

    protected abstract T fromText(String value);

    protected record ChangeValues<T extends Number>(T regular, T shift, T ctrl, T ctrlShift) {}

    protected abstract ChangeValues<T> getChangeValues();

    protected abstract T add(T a, T b);

    protected abstract T multiply(T a, T b);

    protected abstract T clamp(T value, T min, T max);

    protected abstract void setTextFieldRange(TextFieldWidget textField, T min, T max);

    protected abstract T getOne(boolean positive);

    /////////////////////////////////////////////////
    // *********** IMPLEMENTATION ***********//
    /////////////////////////////////////////////////

    private final ChangeValues<T> CHANGE_VALUES = getChangeValues();
    private final T ONE_POSITIVE = getOne(true);
    private final T ONE_NEGATIVE = getOne(false);

    @Getter
    private Supplier<T> valueSupplier;
    @Getter
    private T min = defaultMin();
    @Getter
    private T max = defaultMax();

    private final Consumer<T> onChanged;

    private TextFieldWidget textField;

    public NumberInputWidget(Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this(0, 0, 100, 20, valueSupplier, onChanged);
    }

    public NumberInputWidget(Position position, Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this(position, new Size(100, 20), valueSupplier, onChanged);
    }

    public NumberInputWidget(Position position, Size size, Supplier<T> valueSupplier, Consumer<T> onChanged) {
        this(position.x, position.y, size.width, size.height, valueSupplier, onChanged);
    }

    public NumberInputWidget(int x, int y, int width, int height, Supplier<T> valueSupplier, Consumer<T> onChanged) {
        super(x, y, width, height);
        this.valueSupplier = valueSupplier;
        this.onChanged = onChanged;
        buildUI();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        textField.setCurrentString(toText(valueSupplier.get()));
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeUtf(toText(valueSupplier.get()));
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        textField.setCurrentString(buffer.readUtf());
    }

    @Override
    public Widget setHoverTooltips(String... tooltipText) {
        textField.setHoverTooltips(tooltipText);
        return super.setHoverTooltips(tooltipText);
    }

    private void buildUI() {
        int buttonWidth = Mth.clamp(this.getSize().width / 5, 15, 40);
        int textFieldWidth = this.getSize().width - (2 * buttonWidth) - 4;

        this.addWidget(new ButtonWidget(0, 0, buttonWidth, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture("-", buttonWidth)),
                this::decrease).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));

        this.textField = new TextFieldWidget(buttonWidth + 2, 0, textFieldWidth, 20,
                () -> toText(valueSupplier.get()),
                stringValue -> this.setValue(clamp(fromText(stringValue), min, max)));
        this.updateTextFieldRange();
        this.addWidget(this.textField);

        this.addWidget(new ButtonWidget(buttonWidth + textFieldWidth + 4, 0, buttonWidth, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture("+", buttonWidth)),
                this::increase).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));
    }

    private IGuiTexture getButtonTexture(String prefix, int buttonWidth) {
        var texture = new TextTexture(prefix + "1");

        if (!GTCEu.isClientThread()) {
            return texture;
        }

        // Dynamic text is only necessary on the remote side:

        int maxTextWidth = buttonWidth - 4;

        texture.setSupplier(() -> {
            T amount = GTUtil.isCtrlDown() ?
                    GTUtil.isShiftDown() ? CHANGE_VALUES.ctrlShift : CHANGE_VALUES.ctrl :
                    GTUtil.isShiftDown() ? CHANGE_VALUES.shift : CHANGE_VALUES.regular;

            String text = prefix + toText(amount);

            texture.scale(maxTextWidth / (float) Math.max(Minecraft.getInstance().font.width(text), maxTextWidth));

            return text;
        });

        return texture;
    }

    private void increase(ClickData cd) {
        this.changeValue(cd, ONE_POSITIVE);
    }

    private void decrease(ClickData cd) {
        this.changeValue(cd, ONE_NEGATIVE);
    }

    private void changeValue(ClickData cd, T multiplier) {
        if (!cd.isRemote) {
            T amount = cd.isCtrlClick ?
                    cd.isShiftClick ? CHANGE_VALUES.ctrlShift : CHANGE_VALUES.ctrl :
                    cd.isShiftClick ? CHANGE_VALUES.shift : CHANGE_VALUES.regular;

            this.setValue(clamp(add(valueSupplier.get(), multiply(amount, multiplier)), min, max));
        }
    }

    public NumberInputWidget<T> setMin(T min) {
        this.min = min;
        updateTextFieldRange();

        return this;
    }

    public NumberInputWidget<T> setMax(T max) {
        this.max = max;
        updateTextFieldRange();

        return this;
    }

    public NumberInputWidget<T> setValue(T value) {
        onChanged.accept(value);
        return this;
    }

    protected void updateTextFieldRange() {
        setTextFieldRange(textField, min, max);

        this.setValue(clamp(valueSupplier.get(), min, max));
    }
}
