package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * A widget containing an integer input field, as well as adjacent buttons for increasing or decreasing the value.
 *
 * <p>
 *     The buttons' change amount can be altered with Ctrl, Shift, or both.<br>
 *     The input is limited by a minimum and maximum value.
 * </p>
 */
public class LongInputWidget extends WidgetGroup {

    @Getter
    private long value;
    @Getter
    private long min = 0;
    @Getter
    private long max = Long.MAX_VALUE;

    private final Consumer<Long> onChanged;

    private TextFieldWidget textField;

    public LongInputWidget(long initialValue, Consumer<Long> onChanged) {
        this(0, 0, 100, 20, initialValue, onChanged);
    }

    public LongInputWidget(Position position, long initialValue, Consumer<Long> onChanged) {
        this(position, new Size(100, 20), initialValue, onChanged);
    }

    public LongInputWidget(Position position, Size size, long initialValue, Consumer<Long> onChanged) {
        this(position.x, position.y, size.width, size.height, initialValue, onChanged);
    }

    public LongInputWidget(int x, int y, int width, int height, long initialValue, Consumer<Long> onChanged) {
        super(x, y, width, height);

        this.value = initialValue;
        this.onChanged = onChanged;

        buildUI();
    }

    private void buildUI() {
        int buttonWidth = Mth.clamp(this.getSize().width / 5, 15, 40);
        int textFieldWidth = this.getSize().width - (2 * buttonWidth) - 4;

        this.addWidget(new ButtonWidget(0, 0, buttonWidth, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture("-", buttonWidth)),
                this::decrease
        ).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));


        this.textField = new TextFieldWidget(buttonWidth + 2, 0, textFieldWidth, 20,
                () -> String.valueOf(value),
                stringValue -> this.setValue(Mth.clamp(Long.parseLong(stringValue), min, max))
        );
        this.updateTextFieldRange();
        this.addWidget(this.textField);


        this.addWidget(new ButtonWidget(buttonWidth + textFieldWidth + 4, 0, buttonWidth, 20,
                new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, getButtonTexture("+", buttonWidth)),
                this::increase
        ).setHoverTooltips("gui.widget.incrementButton.default_tooltip"));
    }

    private IGuiTexture getButtonTexture(String prefix, long buttonWidth) {
        var texture = new TextTexture(prefix + "1");

        if (!LDLib.isRemote()) {
            return texture;
        }

        // Dynamic text is only necessary on the remote side:

        long maxTextWidth = buttonWidth - 4;

        texture.setSupplier(() -> {
            long amount = GTUtil.isCtrlDown() ? GTUtil.isShiftDown() ? 512 : 64 : GTUtil.isShiftDown() ? 8 : 1;
            String text = prefix + amount;

            texture.scale(maxTextWidth / (float) Math.max(Minecraft.getInstance().font.width(text), maxTextWidth));

            return text;
        });

        return texture;
    }

    private void increase(ClickData cd) {
        this.changeValue(cd, 1);
    }

    private void decrease(ClickData cd) {
        this.changeValue(cd, -1);
    }

    private void changeValue(ClickData cd, long multiplier) {
        if (!cd.isRemote) {
            long amount = cd.isCtrlClick ? cd.isShiftClick ? 512 : 64 : cd.isShiftClick ? 8 : 1;
            this.setValue(Mth.clamp(value + (multiplier * amount), min, max));
        }
    }

    public LongInputWidget setMin(long min) {
        this.min = min;
        updateTextFieldRange();

        return this;
    }

    public LongInputWidget setMax(long max) {
        this.max = max;
        updateTextFieldRange();

        return this;
    }

    public LongInputWidget setValue(long value) {
        this.value = value;
        onChanged.accept(value);

        return this;
    }

    private void updateTextFieldRange() {
        this.textField.setNumbersOnly(min, max);
        this.setValue(Mth.clamp(this.value, min, max));
    }
}
