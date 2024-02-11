package com.gregtechceu.gtceu.api.gui.fancy;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PageSwitcherUIProvider implements IFancyUIProvider {
    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new WidgetGroup();
    }

    @Override
    public IGuiTexture getTabIcon() {
        return IGuiTexture.EMPTY;
    }

    @Override
    public Component getTitle() {
        return Component.empty();
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }
}
