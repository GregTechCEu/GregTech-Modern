package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import net.minecraft.network.chat.Component;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class FancySelectorConfigurator<T extends Enum<T> & EnumSelectorWidget.SelectableEnum>
                                      implements IFancyConfiguratorButton {

    private final EnumSelectorWidget<T> widget;

    @Setter
    @Accessors(chain = true)
    private Function<T, List<Component>> tooltip = t -> Collections.singletonList(Component.empty());

    public FancySelectorConfigurator(T[] values, T initialValue, Consumer<T> onChanged) {
        this.widget = new EnumSelectorWidget<>(0, 0, 20, 20, values, initialValue, onChanged);
    }

    @Override
    public IGuiTexture getIcon() {
        return widget.getTexture(widget.selected);
    }

    @Override
    public List<Component> getTooltips() {
        return this.tooltip.apply(widget.getCurrentValue());
    }

    @Override
    public void onClick(ClickData clickData) {
        ++widget.selected;
        if (widget.selected >= widget.values.size()) {
            widget.selected = 0;
        }

        widget.buttonWidget.setIndex(widget.selected);
        if (widget.onChanged != null) {
            widget.onChanged.accept(widget.getCurrentValue());
        }
    }
}
