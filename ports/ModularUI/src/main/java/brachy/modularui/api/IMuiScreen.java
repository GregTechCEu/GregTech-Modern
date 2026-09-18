package brachy.modularui.api;

import brachy.modularui.core.mixins.client.AbstractContainerScreenAccessor;
import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.ScreenWrapper;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Implement this interface on a {@link Screen} to be able to use it as a custom wrapper.
 * The Screen should have final {@link ModularScreen} field, which is set from the constructor.
 * Additionally, the Screen MUST call {@link ModularScreen#construct(IMuiScreen)} in its constructor.
 * See {@link ScreenWrapper ScreenWrapper} and {@link ContainerScreenWrapper GuiContainerWrapper}
 * for default implementations.
 */
@OnlyIn(Dist.CLIENT)
public interface IMuiScreen {

    /**
     * Returns the {@link ModularScreen} that is being wrapped. This should return a final instance field.
     *
     * @return the wrapped modular screen
     */
    @NotNull
    ModularScreen screen();

    /**
     * This method decides how the gui background is drawn.
     * The intended usage is to override {@link Screen#renderBackground}
     * and call this method with the super method reference as the second parameter.
     *
     * @param guiGraphics  this screen's {@link GuiGraphicsExtractor} instance
     * @param drawFunction a method reference to draw the world background normally with the
     *                     {@code guiGraphics} as the parameter
     */
    @ApiStatus.NonExtendable
    default void handleDrawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick,
                                      RenderFunction drawFunction) {
        if (ClientScreenHandler.shouldDrawWorldBackground()) {
            drawFunction.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        ClientScreenHandler.drawDarkBackground(wrappedScreen(), guiGraphics);
    }

    /**
     * This method is called every time the {@link ModularScreen} resizes.
     * This usually only affects {@link AbstractContainerScreen AbstractContainerScreens}.
     *
     * @param area area of the main panel
     */
    default void updateGuiArea(Rectangle area) {
        if (wrappedScreen() instanceof AbstractContainerScreenAccessor acc) {
            acc.setLeftPos(area.x);
            acc.setTopPos(area.y);
            acc.setImageWidth(area.width);
            acc.setImageHeight(area.height);
        }
    }

    /**
     * @return if this wrapper is a {@link AbstractContainerScreen}
     */
    @ApiStatus.NonExtendable
    default boolean isContainerScreen() {
        return wrappedScreen() instanceof AbstractContainerScreen<?>;
    }

    /**
     * Hovering widget is handled by {@link ModularGuiContext}.
     * If it detects a slot, this method is called. Only affects {@link AbstractContainerScreen
     * AbstractContainerScreens}.
     *
     * @param slot hovered slot
     */
    @ApiStatus.NonExtendable
    default void setHoveredSlot(Slot slot) {
        if (wrappedScreen() instanceof AbstractContainerScreenAccessor acc) {
            acc.setHoveredSlot(slot);
        }
    }

    /**
     * Returns the {@link Screen} that wraps the {@link ModularScreen}.
     * In most cases this does not need to be overridden as this interfaces should be implemented on {@link Screen
     * Screens}.
     *
     * @return the wrapping gui screen
     */
    default Screen wrappedScreen() {
        return (Screen) this;
    }

    @FunctionalInterface
    interface RenderFunction {

        void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);
    }
}
