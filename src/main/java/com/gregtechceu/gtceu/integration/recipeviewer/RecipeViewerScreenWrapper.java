package com.gregtechceu.gtceu.integration.recipeviewer;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.jetbrains.annotations.NotNull;

public record RecipeViewerScreenWrapper(ModularScreen screen) implements IMuiScreen {

    @Override
    public Screen getWrappedScreen() {
        return Minecraft.getInstance().screen;
    }

    @Override
    public @NotNull ModularScreen getScreen() {
        return screen;
    }

    @Override
    public void updateGuiArea(Rectangle area) {
        // overlay should not modify screen
    }
}
