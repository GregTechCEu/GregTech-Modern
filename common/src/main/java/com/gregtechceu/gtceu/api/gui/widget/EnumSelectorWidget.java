package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnumSelectorWidget<T extends Enum<T> & EnumSelectorWidget.SelectableEnum> extends WidgetGroup {
    private final CycleButtonWidget buttonWidget;

    private final List<T> values;
    private final Consumer<T> onChanged;

    private int selected = 0;

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, T[] values, T initialValue, Consumer<T> onChanged) {
        this(xPosition, yPosition, width, height, Arrays.asList(values), initialValue, onChanged);
    }

    public EnumSelectorWidget(int xPosition, int yPosition, int width, int height, List<T> values, T initialValue, Consumer<T> onChanged) {
        this.values = values;
        this.onChanged = onChanged;

        this.buttonWidget = new CycleButtonWidget(xPosition, yPosition, width, height, values.size(), this::getTexture, this::onSelected);
        this.addWidget(buttonWidget);

        setSelected(initialValue);
    }

    private IGuiTexture getTexture(int selected) {
        return new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, values.get(selected).getIcon());
    }

    private void onSelected(int selected) {
        setSelected(values.get(selected));

        onChanged.accept(values.get(selected));
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
        if (!LDLib.isRemote())
            return;

        buttonWidget.setHoverTooltips(List.copyOf(LangHandler.getSingleOrMultiLang(values.get(selected).getTooltip())));
    }

    public interface SelectableEnum {
        String getTooltip();
        IGuiTexture getIcon();
    }
}
