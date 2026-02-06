package com.gregtechceu.gtceu.client.mui.screen;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.eventbus.api.Event;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenScreenEvent extends Event {

    @Getter
    private final Screen screen;
    private final List<ModularScreen> overlays = new ArrayList<>();

    public OpenScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public boolean isModularScreen() {
        return screen instanceof IMuiScreen;
    }

    public @Nullable ModularScreen getModularScreen() {
        return screen instanceof IMuiScreen muiScreen ? muiScreen.getScreen() : null;
    }

    public List<ModularScreen> getOverlays() {
        return overlays;
    }

    public void addOverlay(ModularScreen screen) {
        this.overlays.add(screen);
    }
}
