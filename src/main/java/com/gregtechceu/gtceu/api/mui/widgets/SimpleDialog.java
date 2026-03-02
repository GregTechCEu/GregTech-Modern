package com.gregtechceu.gtceu.api.mui.widgets;

import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleDialog<T, W extends Widget<W>> extends Dialog<T> {

    public SimpleDialog(String name, Consumer<T> valueConsumer, W widget, Function<W, T> valueGetter, IKey title) {
        super(name, valueConsumer);
        child(new TextWidget<>(title).align(Alignment.TopCenter).marginTop(4));
        child(widget.align(Alignment.CENTER));
        child(new ButtonWidget<>()
                .background(GTGuiTextures.CLOSE)
                .hoverBackground(GTGuiTextures.CLOSE)
                .align(Alignment.TopRight)
                .onMousePressed((mouseX, mouseY, button) -> {
                    closeIfOpen();
                    return true;
                }));
        child(new ButtonWidget<>()
                .background(GTGuiTextures.RIGHTLOAD)
                .hoverBackground(GTGuiTextures.RIGHTLOAD)
                .align(Alignment.BottomCenter)
                .onMousePressed((mouseX, mouseY, button) -> {
                    closeWith(valueGetter.apply(widget));
                    return true;
                }));
    }
}
