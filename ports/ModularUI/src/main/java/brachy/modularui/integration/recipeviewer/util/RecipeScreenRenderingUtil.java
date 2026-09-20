package brachy.modularui.integration.recipeviewer.util;

import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.drawable.GuiTint;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.Experimental
public class RecipeScreenRenderingUtil {

    public static final IItemHandlerModifiable EMPTY_ITEM_HANDLER = new EmptyItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }
    };

    @ApiStatus.Internal
    public static void drawScreenBackground(GuiGraphicsExtractor guiGraphics, ModularScreen screen,
                                            int mouseX, int mouseY, float partialTick) {
        screen.getContext().setGraphics(guiGraphics);
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        guiGraphics.pose().pushMatrix();
        int previousTint = GuiTint.get();
        try {
            screen.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.nextStratum();
            ClientScreenHandler.drawVanillaElements(guiGraphics, screen.getScreenWrapper().wrappedScreen(),
                    mouseX, mouseY, partialTick);
        } finally {
            GuiTint.set(previousTint);
            guiGraphics.pose().popMatrix();
        }
    }

    @ApiStatus.Internal
    public static void drawScreenForeground(GuiGraphicsExtractor guiGraphics, ModularScreen screen,
                                            int mouseX, int mouseY, float partialTick) {
        screen.getContext().setGraphics(guiGraphics);
        screen.getContext().updateState(mouseX, mouseY, partialTick);
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        int previousTint = GuiTint.get();
        try {
            // Item lighting is selected by the deferred item renderer from its model state.
            screen.drawForeground(guiGraphics);
        } finally {
            GuiTint.set(previousTint);
            guiGraphics.pose().popMatrix();
        }
    }
}
