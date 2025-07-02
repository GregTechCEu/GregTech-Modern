package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

import java.util.function.Consumer;

@LDLRegister(name = "button", group = "widget.basic")
public class ButtonWidget extends com.lowdragmc.lowdraglib.gui.widget.ButtonWidget {

    public ButtonWidget() {
        this(0, 0, 40, 20, GuiTextures.BUTTON, null);
    }

    public ButtonWidget(int xPosition, int yPosition, int width, int height,
                        IGuiTexture buttonTexture, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height, buttonTexture, onPressed);
    }

    public ButtonWidget(int xPosition, int yPosition, int width, int height, Consumer<ClickData> onPressed) {
        super(xPosition, yPosition, width, height, onPressed);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            isClicked = true;
            ClickData clickData = new ClickData();
            writeClientAction(1, clickData::writeToBuf);
            if (onPressCallback != null) {
                onPressCallback.accept(clickData);
            }
            playButtonClickSound();
            return true;
        }
        return false;
    }
}
