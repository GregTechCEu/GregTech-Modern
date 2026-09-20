package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(fluent = true)
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
    public void extractBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleDrawBackground(guiGraphics, mouseX, mouseY, partialTick, super::extractBackground);
    }

    @Override
    public boolean isPauseScreen() {
        return this.screen.isPauseScreen();
    }

    @Override
    public String toString() {
        return "Wrapper(" + screen() + ")";
    }
}
