package com.gregtechceu.gtceu.common.mui.widgets.textfield;

import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true)
public class TextFieldWithScrollableCallback extends TextFieldWidget {

    @Setter
    private @Nullable TriFunction<Double, Double, Double, Boolean> onScrolled;

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if (onScrolled == null) return super.onMouseScrolled(mouseX, mouseY, delta);
        return onScrolled.apply(mouseX, mouseY, delta);
    }
}
