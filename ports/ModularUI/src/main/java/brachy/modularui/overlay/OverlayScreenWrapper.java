package brachy.modularui.overlay;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps the current gui screen and uses it for overlays.
 */
@ApiStatus.Experimental
public record OverlayScreenWrapper(Screen wrappedScreen, ModularScreen screen) implements IMuiScreen {

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
