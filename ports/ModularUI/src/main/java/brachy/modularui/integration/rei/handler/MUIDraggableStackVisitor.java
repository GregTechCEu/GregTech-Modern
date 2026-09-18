package brachy.modularui.integration.rei.handler;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.integration.rei.REIStackConverter;

import net.minecraft.client.gui.screens.Screen;

import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;

import java.util.List;
import java.util.stream.Stream;

import static brachy.modularui.integration.rei.handler.REIScreenHandler.asREIRect;
import static brachy.modularui.integration.rei.handler.REIScreenHandler.currentIngredient;

public class MUIDraggableStackVisitor<T extends Screen & IMuiScreen> implements DraggableStackVisitor<T> {

    private final Class<T> clazz;

    public MUIDraggableStackVisitor(Class<T> clazz) {
        this.clazz = clazz;
    }


    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return clazz.isAssignableFrom(screen.getClass());
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context,
                                                              DraggableStack stack) {
        currentIngredient = stack;
        return context.getScreen().screen().getContext()
                .getRecipeViewerSettings().getGhostIngredientSlots().stream()
                .map(target -> BoundsProvider.ofRectangle(asREIRect(target.getArea())));
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<T> context,
                                                    DraggableStack stack) {
        List<GhostIngredientSlot<?>> ghostSlots = context.getScreen().screen().getContext()
                .getRecipeViewerSettings().getGhostIngredientSlots();
        for (var slot : ghostSlots) {
            if (!slot.isEnabled()) {
                continue;
            }
            var entryStack = stack.getStack();

            if (slot.ingredientHandlingOverride(entryStack)) {
                currentIngredient = null;
                return DraggedAcceptorResult.ACCEPTED;
            }
            REIStackConverter.Converter<?> converter = REIStackConverter.getForNullable(slot.ingredientClass());
            if (converter == null) {
                continue;
            }
            var converted = converter.convertFrom(entryStack);
            if (converted != null) {
                // noinspection unchecked,rawtypes
                ((GhostIngredientSlot) slot).setGhostIngredient(converted);
                currentIngredient = null;
                return DraggedAcceptorResult.ACCEPTED;
            }
        }
        currentIngredient = null;
        return DraggedAcceptorResult.PASS;
    }
}
