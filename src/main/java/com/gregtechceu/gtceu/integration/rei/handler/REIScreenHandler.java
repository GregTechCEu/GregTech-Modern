package com.gregtechceu.gtceu.integration.rei.handler;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.widget.IGuiElement;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.xei.handlers.IngredientProvider;
import com.gregtechceu.gtceu.integration.xei.handlers.RecipeViewerHandler;

import net.minecraft.client.gui.screens.Screen;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.drag.*;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class REIScreenHandler<T extends Screen & IMuiScreen> extends RecipeViewerHandler
                             implements DraggableStackProvider<T>, ExclusionZonesProvider<T> {

    private static final Map<Class<?>, REIScreenHandler<?>> CACHE = new Reference2ReferenceOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Screen & IMuiScreen> REIScreenHandler<T> of(Class<T> cls) {
        return (REIScreenHandler<T>) CACHE.computeIfAbsent(cls, c -> new REIScreenHandler<T>());
    }

    // I have to do this mess because of conflicting comparable impls.
    @Getter
    private final DraggableStackVisitor<T> draggableVisitor = new DraggableStackVisitor<T>() {

        @Override
        public <R extends Screen> boolean isHandingScreen(R screen) {
            return REIScreenHandler.this.isHandingScreen(screen);
        }

        @Override
        public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<T> context,
                                                                  DraggableStack stack) {
            currentIngredient = stack;
            return context.getScreen().getScreen().getContext()
                    .getXeiSettings().getGhostIngredientSlots().stream()
                    .map(target -> BoundsProvider.ofRectangle(asREIRect(target.getArea())));
        }

        @Override
        public DraggedAcceptorResult acceptDraggedStack(DraggingContext<T> context,
                                                        DraggableStack stack) {
            List<GhostIngredientSlot<?>> ghostSlots = context.getScreen().getScreen().getContext()
                    .getXeiSettings().getGhostIngredientSlots();
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
    };

    private REIScreenHandler() {}

    @Override
    public @Nullable DraggableStack getHoveredStack(DraggingContext<T> context, double mouseX, double mouseY) {
        IGuiElement hovered = context.getScreen().getScreen().getContext().getHovered();
        if (hovered instanceof IngredientProvider<?> provider) {
            var override = provider.ingredientOverride();
            if (override != null) {
                currentIngredient = (DraggableStack) override;
                return currentIngredient;
            }

            REIStackConverter.Converter<?> converter = REIStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                return null;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            var converted = ((REIStackConverter.Converter) converter).convertTo(provider);
            return new DraggableStack() {

                @Override
                public EntryStack<?> getStack() {
                    if (converted.isEmpty()) return EntryStack.empty();
                    return converted.get(0);
                }

                @Override
                public void drag() {
                    currentIngredient = this;
                }

                @Override
                public void release(DraggedAcceptorResult result) {
                    currentIngredient = null;
                }
            };
        }
        return null;
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof IMuiScreen;
    }

    @Override
    public DraggingContext<T> getContext() {
        return DraggableStackProvider.super.getContext();
    }

    @Override
    public double getPriority() {
        return DraggableStackProvider.super.getPriority();
    }

    @Override
    public Collection<me.shedaniel.math.Rectangle> provide(T screen) {
        return screen.getScreen().getContext()
                .getXeiSettings().getAllExclusionAreas().stream()
                .map(REIScreenHandler::asREIRect)
                .toList();
    }

    private static me.shedaniel.math.Rectangle asREIRect(Rectangle rect) {
        return new me.shedaniel.math.Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    static DraggableStack currentIngredient = null;

    @Override
    public void setSearchFocused(boolean focused) {
        TextField searchField = REIRuntime.getInstance().getSearchTextField();
        if (searchField != null) searchField.setFocused(focused);
    }

    @Override
    public @Nullable Object getCurrentlyDragged() {
        if (currentIngredient == null) return null;
        return currentIngredient.get().getValue();
    }
}
