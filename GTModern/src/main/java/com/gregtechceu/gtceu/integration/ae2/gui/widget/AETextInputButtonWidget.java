package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

@Accessors(chain = true)
public class AETextInputButtonWidget extends WidgetGroup {

    @Setter
    private Consumer<String> onConfirm;

    @Getter
    @Setter
    private String text = "";

    private Component[] hoverTexts = new Component[0];

    @Getter
    private boolean isInputting;

    private Widget textField;

    public AETextInputButtonWidget() {}

    public AETextInputButtonWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public AETextInputButtonWidget(Position position) {
        super(position);
    }

    public AETextInputButtonWidget(Position position, Size size) {
        super(position, size);
    }

    public AETextInputButtonWidget setButtonTooltips(Component... tooltipTexts) {
        this.hoverTexts = tooltipTexts;
        return this;
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.textField = new TextFieldWidget(
                0,
                0,
                getSizeWidth() - getSizeHeight() - 2,
                getSizeHeight(),
                this::getText,
                this::setText).setActive(false).setVisible(false);
        this.addWidget(new ToggleButtonWidget(
                getSizeWidth() - getSizeHeight(),
                0,
                getSizeHeight(),
                getSizeHeight(),
                this::isInputting,
                pressed -> {
                    isInputting = pressed;
                    if (pressed) {
                        textField.setActive(true);
                        textField.setVisible(true);
                    } else {
                        onConfirm.accept(text);
                        textField.setActive(false);
                        textField.setVisible(false);
                    }
                })
                .setTexture(
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✎")),
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, new TextTexture("✔")))
                .setHoverTooltips(hoverTexts));
        this.addWidget(textField);
    }
}
