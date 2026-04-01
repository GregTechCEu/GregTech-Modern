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
        child(new TextWidget<>(title).leftRel(0.5f).marginTop(4));
        child(widget.center());
        child(new ButtonWidget<>()
                .background(GTGuiTextures.CLOSE)
                .hoverBackground(GTGuiTextures.CLOSE)
                .posRel(Alignment.TopRight)
                .onMousePressed((mouseX, mouseY, button) -> {
                    closeIfOpen();
                    return true;
                }));
        child(new ButtonWidget<>()
                .background(GTGuiTextures.RIGHTLOAD)
                .hoverBackground(GTGuiTextures.RIGHTLOAD)
                .posRel(Alignment.TopCenter)
                .onMousePressed((mouseX, mouseY, button) -> {
                    closeWith(valueGetter.apply(widget));
                    return true;
                }));
    }
}
