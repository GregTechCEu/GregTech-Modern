package com.gregtechceu.gtceu.integration.jei.handler;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.core.mixins.jei.IngredientListOverlayAccessor;
import com.gregtechceu.gtceu.integration.jei.GTJEIPlugin;
import com.gregtechceu.gtceu.integration.xei.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.xei.handlers.RecipeViewerHandler;

import net.minecraft.client.gui.screens.Screen;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JEIScreenHandler<T extends Screen & IMuiScreen> extends RecipeViewerHandler
                             implements IGhostIngredientHandler<T> {

    private static final Map<Class<?>, JEIScreenHandler<?>> CACHE = new Reference2ReferenceOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Screen & IMuiScreen> JEIScreenHandler<T> of(Class<T> cls) {
        return (JEIScreenHandler<T>) CACHE.computeIfAbsent(cls, c -> new JEIScreenHandler<T>());
    }

    private JEIScreenHandler() {}

    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(T screen,
                                                        @NotNull ITypedIngredient<I> ingredient,
                                                        boolean doStart) {
        currentIngredient = ingredient;

        List<GhostIngredientSlot<?>> ghostSlots = screen.getScreen().getContext()
                .getXeiSettings().getGhostIngredientSlots();
        List<Target<I>> ghostHandlerTargets = new ArrayList<>();
        for (var slot : ghostSlots) {
            if (slot.isEnabled() && slot.castGhostIngredientIfValid(ingredient.getIngredient()) != null) {
                @SuppressWarnings("unchecked")
                GhostIngredientSlot<I> slotWithType = (GhostIngredientSlot<I>) slot;
                ghostHandlerTargets.add(new GhostIngredientTarget<>(slotWithType));
            }
        }
        return ghostHandlerTargets;
    }

    @Override
    public void onComplete() {
        currentIngredient = null;
    }

    static ITypedIngredient<?> currentIngredient = null;

    @Override
    public void setSearchFocused(boolean focused) {
        // only set the search field state if it's JEI's actual search field and not JEMI
        if (GTJEIPlugin.getRuntime().getIngredientListOverlay() instanceof IngredientListOverlayAccessor accessor) {
            accessor.getSearchField().setFocused(focused);
        }
    }

    @Override
    public @Nullable Object getCurrentlyDragged() {
        if (currentIngredient == null) return null;
        return currentIngredient.getIngredient();
    }
}
