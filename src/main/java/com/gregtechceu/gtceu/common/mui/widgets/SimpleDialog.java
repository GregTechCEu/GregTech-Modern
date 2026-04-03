package com.gregtechceu.gtceu.common.mui.widgets;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.TextWidget;

import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleDialog<T, W extends Widget<W>> extends Dialog<T, SimpleDialog<T, W>> {

    public SimpleDialog(String name, Consumer<T> valueConsumer, W widget, Function<W, T> valueGetter, IKey title) {
        super(name);
        child(new TextWidget<>(title).leftRel(0.5f).marginTop(4));
        child(widget.center());
        child(new ButtonWidget<>()
                .background(GuiTextures.CLOSE)
                .hoverBackground(GuiTextures.CLOSE)
                .posRel(Alignment.TopRight)
                .onMousePressed((mouseX, mouseY, button) -> {
                    closeIfOpen();
                    return true;
                }));
        child(new ButtonWidget<>()
                .background(GuiTextures.RIGHTLOAD)
                .hoverBackground(GuiTextures.RIGHTLOAD)
                .posRel(Alignment.TopCenter)
                .onMousePressed((mouseX, mouseY, button) -> {
                    closeWith(valueGetter.apply(widget));
                    return true;
                }));
    }
}
