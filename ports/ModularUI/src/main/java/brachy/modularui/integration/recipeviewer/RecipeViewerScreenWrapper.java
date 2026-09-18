package brachy.modularui.integration.recipeviewer;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@Accessors(fluent = true)
public record RecipeViewerScreenWrapper(ModularScreen screen) implements IMuiScreen {

    @Override
    public Screen wrappedScreen() {
        return Minecraft.getInstance().screen;
    }

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
