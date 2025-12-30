package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A widget for selecting a value from an enum or a subset of its values.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnumSelectorWidget<T extends Enum<T> & EnumSelectorWidget.SelectableEnum> extends WidgetGroup {

    public interface SelectableEnum {

        String getTooltip();

        IGuiTexture getIcon();
    }

    public final CycleButtonWidget buttonWidget;

    public final List<T> values;
    public final Consumer<T> onChanged;

    public int selected = 0;

    private BiFunction<T, IGuiTexture, IGuiTexture> textureSupplier = (value, texture) -> new GuiTextureGroup(
            GuiTextures.VANILLA_BUTTON, texture);

    private BiFunction<T, String, List<Component>> tooltipSupplier = (value, key) -> List
            .copyOf(LangHandler.getSingleOrMultiLang(key));

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, T[] values, T initialValue,
                              Consumer<T> onChanged) {
        this(xPosition, yPosition, width, height, Arrays.asList(values), initialValue, onChanged);
    }

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, List<T> values, T initialValue,
                              Consumer<T> onChanged) {
        super(xPosition, yPosition, width, height);

        this.values = values;
        this.onChanged = onChanged;

        this.buttonWidget = new CycleButtonWidget(0, 0, width, height, values.size(), this::getTexture,
                this::onSelected);
        this.addWidget(buttonWidget);

        setSelected(initialValue);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeInt(selected);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        onSelected(buffer.readInt());
    }

    public T getCurrentValue() {
        return values.get(selected);
    }

    public IGuiTexture getTexture(int selected) {
        var selectedValue = values.get(selected);
        return textureSupplier.apply(selectedValue, selectedValue.getIcon());
    }

    private void onSelected(int selected) {
        T selectedValue = values.get(selected);
        setSelected(selectedValue);
    }

    public EnumSelectorWidget<T> setTextureSupplier(BiFunction<T, IGuiTexture, IGuiTexture> textureSupplier) {
        this.textureSupplier = textureSupplier;

        T selectedValue = getCurrentValue();
        buttonWidget.setBackground(textureSupplier.apply(selectedValue, selectedValue.getIcon()));

        return this;
    }

    public EnumSelectorWidget<T> setTooltipSupplier(BiFunction<T, String, List<Component>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;

        return this;
    }

    public void setSelected(@NotNull T value) {
        var selectedIndex = values.indexOf(value);

        if (selectedIndex == -1)
            throw new NoSuchElementException(value + " is not a possible value for this selector.");

        this.selected = selectedIndex;
        this.buttonWidget.setIndex(selectedIndex);

        updateTooltip();

        onChanged.accept(value);
    }

    private void updateTooltip() {
        if (!GTCEu.isClientThread())
            return;

        T selectedValue = getCurrentValue();
        buttonWidget.setHoverTooltips(tooltipSupplier.apply(selectedValue, selectedValue.getTooltip()));
    }
}
