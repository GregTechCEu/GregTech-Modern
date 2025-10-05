package com.gregtechceu.gtceu.api.mui.overlay;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import net.minecraft.client.gui.screens.Screen;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps the current gui screen and uses it for overlays.
 */
@ApiStatus.Experimental
public class OverlayScreenWrapper implements IMuiScreen {

    @Getter
    private final Screen wrappedScreen;
    @Getter
    private final ModularScreen screen;

    public OverlayScreenWrapper(Screen wrappedScreen, ModularScreen screen) {
        this.wrappedScreen = wrappedScreen;
        this.screen = screen;
    }

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
