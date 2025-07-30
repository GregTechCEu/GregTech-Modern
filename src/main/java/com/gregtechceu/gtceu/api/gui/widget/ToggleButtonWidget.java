package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ToggleButtonWidget extends SwitchWidget {

    private final IGuiTexture texture;
    private String tooltipText;
    private boolean isMultiLang;

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, BooleanSupplier isPressedCondition,
                              BooleanConsumer setPressedExecutor) {
        this(xPosition, yPosition, width, height, GuiTextures.VANILLA_BUTTON, isPressedCondition, setPressedExecutor);
    }

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                              BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        super(xPosition, yPosition, width, height, null);
        texture = buttonTexture;
        if (buttonTexture instanceof ResourceTexture resourceTexture) {
            setTexture(resourceTexture.getSubTexture(0, 0, 1, 0.5), resourceTexture.getSubTexture(0, 0.5, 1, 0.5));
        } else {
            setTexture(buttonTexture, buttonTexture);
        }

        setSupplier(isPressedCondition::getAsBoolean);
        setOnPressCallback((cd, bool) -> {
            setPressedExecutor.accept(bool.booleanValue());
            this.updateHoverTooltips();
        });
    }

    public ToggleButtonWidget setShouldUseBaseBackground() {
        if (texture != null) {
            setTexture(
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5), texture),
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5), texture));
        }
        return this;
    }

    public ToggleButtonWidget setTooltipText(String tooltipText) {
        this.tooltipText = tooltipText;
        updateHoverTooltips();
        return this;
    }

    public ToggleButtonWidget isMultiLang() {
        isMultiLang = true;
        updateHoverTooltips();
        return this;
    }

    protected void updateHoverTooltips() {
        if (tooltipText != null) {
            if (!isMultiLang) {
                setHoverTooltips(tooltipText + (isPressed ? ".enabled" : ".disabled"));
            } else {
                setHoverTooltips(
                        List.copyOf(LangHandler.getMultiLang(tooltipText + (isPressed ? ".enabled" : ".disabled"))));
            }
        }
    }
}
