package brachy.modularui.screen.event;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.screen.ModularScreen;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenScreenEvent extends Event {

    @Getter private final Screen screen;
    @Getter private final List<ModularScreen> overlays = new ArrayList<>();

    public OpenScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public boolean isModularScreen() {
        return screen instanceof IMuiScreen;
    }

    public @Nullable ModularScreen getModularScreen() {
        return screen instanceof IMuiScreen muiScreen ? muiScreen.screen() : null;
    }

    public void addOverlay(ModularScreen screen) {
        this.overlays.add(screen);
    }
}
