package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.BooleanSupplier;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ToggleButtonWidget
 */
@Accessors(chain = true)
public class ToggleButtonWidget extends SwitchWidget {

    private final IGuiTexture texture;
    @Setter
    private String tooltipText;

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        this(xPosition, yPosition, width, height, GuiTextures.VANILLA_BUTTON, isPressedCondition, setPressedExecutor);
    }

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, IGuiTexture buttonTexture,
                              BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        super(xPosition, yPosition, width, height, (clickData, aBoolean) -> setPressedExecutor.accept(aBoolean.booleanValue()));
        texture = buttonTexture;
        if (buttonTexture instanceof ResourceTexture resourceTexture) {
            setTexture(resourceTexture.getSubTexture(0, 0, 1, 0.5), resourceTexture.getSubTexture(0, 0.5, 1, 0.5));
        } else {
            setTexture(buttonTexture, buttonTexture);
        }

        setSupplier(isPressedCondition::getAsBoolean);
    }

    public ToggleButtonWidget setShouldUseBaseBackground() {
        if (texture != null) {
            setTexture(
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5), texture),
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5), texture)
            );
        }
        return this;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (tooltipText != null) {
            setHoverTooltips(tooltipText + (isPressed ? ".enabled" : ".disabled"));
        }
    }
}
