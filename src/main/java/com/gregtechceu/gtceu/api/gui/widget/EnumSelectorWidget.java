package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A widget for selecting a value from an enum or a subset of its values.
 */
public class EnumSelectorWidget<T extends Enum<T>> extends WidgetGroup {

    public interface SelectableEnum {

        String getTooltip();

        IGuiTexture getIcon();
    }

    public final CycleButtonWidget buttonWidget;

    public final List<T> values;
    public final Consumer<T> onChanged;
    private final Function<T, String> tooltipGetter;
    private final Function<T, IGuiTexture> iconGetter;

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
        this(xPosition, yPosition, width, height, values, initialValue, onChanged,
                EnumSelectorWidget::getSelectableTooltip, EnumSelectorWidget::getSelectableIcon);
    }

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, T[] values, T initialValue,
                              Consumer<T> onChanged, Function<T, String> tooltipGetter,
                              Function<T, IGuiTexture> iconGetter) {
        this(xPosition, yPosition, width, height, Arrays.asList(values), initialValue, onChanged, tooltipGetter,
                iconGetter);
    }

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, List<T> values, T initialValue,
                              Consumer<T> onChanged, Function<T, String> tooltipGetter,
                              Function<T, IGuiTexture> iconGetter) {
        super(xPosition, yPosition, width, height);

        this.values = values;
        this.onChanged = onChanged;
        this.tooltipGetter = tooltipGetter;
        this.iconGetter = iconGetter;

        this.buttonWidget = new CycleButtonWidget(0, 0, width, height, values.size(), this::getTexture,
                this::onSelected);
        this.addWidget(buttonWidget);

        setSelected(initialValue);
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeInt(selected);
    }

    @Override
    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        onSelected(buffer.readInt());
    }

    public T getCurrentValue() {
        return values.get(selected);
    }

    public IGuiTexture getTexture(int selected) {
        var selectedValue = values.get(selected);
        return textureSupplier.apply(selectedValue, iconGetter.apply(selectedValue));
    }

    private void onSelected(int selected) {
        T selectedValue = values.get(selected);
        setSelected(selectedValue);
    }

    public EnumSelectorWidget<T> setTextureSupplier(BiFunction<T, IGuiTexture, IGuiTexture> textureSupplier) {
        this.textureSupplier = textureSupplier;

        T selectedValue = getCurrentValue();
        buttonWidget.setBackground(textureSupplier.apply(selectedValue, iconGetter.apply(selectedValue)));

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
        buttonWidget.setHoverTooltips(tooltipSupplier.apply(selectedValue, tooltipGetter.apply(selectedValue)));
    }

    private static String getSelectableTooltip(Enum<?> value) {
        if (value instanceof SelectableEnum selectable) {
            return selectable.getTooltip();
        }
        throw new IllegalArgumentException(value + " does not provide a selector tooltip.");
    }

    private static IGuiTexture getSelectableIcon(Enum<?> value) {
        if (value instanceof SelectableEnum selectable) {
            return selectable.getIcon();
        }
        throw new IllegalArgumentException(value + " does not provide a selector icon.");
    }
}
