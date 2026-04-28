package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;

public class TabButton extends SwitchWidget {

    private TabContainer container;

    public TabButton() {}

    public TabButton(int x, int y, int width, int height) {
        super(x, y, width, height, (clickData, value) -> {});
    }

    @Override
    public TabButton setTexture(IGuiTexture baseTexture, IGuiTexture pressedTexture) {
        super.setTexture(baseTexture, pressedTexture);
        return this;
    }

    @Override
    public TabButton setBaseTexture(IGuiTexture... textures) {
        super.setBaseTexture(textures);
        return this;
    }

    @Override
    public TabButton setPressedTexture(IGuiTexture... textures) {
        super.setPressedTexture(textures);
        return this;
    }

    @Override
    public TabButton setHoverTexture(IGuiTexture... textures) {
        super.setHoverTexture(textures);
        return this;
    }

    @Override
    public TabButton setHoverBorderTexture(int borderColor, int borderWidth) {
        super.setHoverBorderTexture(borderColor, borderWidth);
        return this;
    }

    public void setContainer(TabContainer container) {
        this.container = container;
    }

    public void onPressed(ClickData clickData, boolean pressed) {
        if (container != null) {
            container.onTabPressed(this);
        }
    }
}
