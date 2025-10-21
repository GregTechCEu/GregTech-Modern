package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ScreenWrapper extends Screen implements IMuiScreen {

    @Getter
    private final @NotNull ModularScreen screen;

    public ScreenWrapper(@NotNull ModularScreen screen) {
        super(Component.empty());
        this.screen = screen;
        this.screen.construct(this);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        handleDrawBackground(guiGraphics, super::renderBackground);
    }

    @Override
    public boolean isPauseScreen() {
        return this.screen.isPauseScreen();
    }

    @Override
    public String toString() {
        return "Wrapper(" + getScreen() + ")";
    }
}
