package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ContainerScreenWrapper extends AbstractContainerScreen<ModularContainerMenu> implements IMuiScreen {

    @Getter
    private final @NotNull ModularScreen screen;

    public ContainerScreenWrapper(ModularContainerMenu container, @NotNull ModularScreen screen) {
        super(container, container.getPlayer().getInventory(), Component.empty());
        this.screen = screen;
        this.screen.construct(this);
    }

    /**
     * This is only used to create the menu type with Registrate. Do not use it (even though it may work).
     *
     * @deprecated Internal use only.
     */
    @SuppressWarnings({"DataFlowIssue", "DeprecatedIsStillUsed"})
    @Deprecated
    @ApiStatus.Internal
    public ContainerScreenWrapper(ModularContainerMenu container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        if (container.isScreenInitialized()) {
            this.screen = container.getScreen();
            this.screen.construct(this);
        } else {
            this.screen = null;
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleDrawBackground(guiGraphics, mouseX, mouseY, partialTick, super::renderBackground);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {}

    @Override
    public @NotNull ModularScreen screen() {
        return screen;
    }

    @Override
    public void updateGuiArea(Rectangle area) {
        this.leftPos = area.x;
        this.topPos = area.y;
        this.imageWidth = area.width;
        this.imageHeight = area.height;
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
