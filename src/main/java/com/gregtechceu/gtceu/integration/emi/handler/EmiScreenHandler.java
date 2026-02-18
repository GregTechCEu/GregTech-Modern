package com.gregtechceu.gtceu.integration.emi.handler;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.integration.emi.EmiStackConverter;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.IngredientProvider;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.RecipeViewerHandler;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.screen.EmiScreenManager;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EmiScreenHandler<T extends Screen & IMuiScreen> extends RecipeViewerHandler
                             implements EmiExclusionArea<T>, EmiDragDropHandler<T>, EmiStackProvider<T> {

    private static final Map<Class<?>, EmiScreenHandler<?>> CACHE = new Reference2ReferenceOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Screen & IMuiScreen> EmiScreenHandler<T> of(Class<T> cls) {
        return (EmiScreenHandler<T>) CACHE.computeIfAbsent(cls, c -> new EmiScreenHandler<T>((Class<T>) c));
    }

    protected final Class<T> clazz;

    private EmiScreenHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void register(EmiRegistry registry) {
        registry.addExclusionArea(this.clazz, this);
        registry.addDragDropHandler(this.clazz, this);
        registry.addStackProvider(this.clazz, this);
    }

    public static <T extends Screen & IMuiScreen> void register(Class<T> clazz, EmiRegistry registry) {
        of(clazz).register(registry);
    }

    @Override
    public boolean dropStack(T screen, EmiIngredient stack, int x, int y) {
        List<GhostIngredientSlot<?>> ghostSlots = screen.getScreen().getContext()
                .getRecipeViewerSettings().getGhostIngredientSlots();

        var stacks = stack.getEmiStacks();
        if (stacks.isEmpty()) return false;
        for (EmiStack emiStack : stacks) {
            for (GhostIngredientSlot<?> slot : ghostSlots) {
                if (!slot.isEnabled() || !slot.getArea().contains(x, y)) {
                    continue;
                }
                if (slot.ingredientHandlingOverride(emiStack)) {
                    return true;
                }
                EmiStackConverter.Converter<?> converter = EmiStackConverter
                        .getForNullable(slot.ingredientClass());
                if (converter == null) {
                    continue;
                }
                var converted = converter.convertFrom(emiStack);
                if (converted != null) {
                    // noinspection unchecked,rawtypes
                    ((GhostIngredientSlot) slot).setGhostIngredient(converted);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addExclusionArea(T screen, Consumer<Bounds> consumer) {
        screen.getScreen().getContext()
                .getRecipeViewerSettings().getAllExclusionAreas()
                .stream()
                .map(rect -> new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()))
                .forEach(consumer);
    }

    @Override
    public EmiStackInteraction getStackAt(T screen, int x, int y) {
        IWidget hovered = screen.getScreen().getContext().getTopHovered();
        if (hovered instanceof IngredientProvider<?> provider) {
            var override = provider.ingredientOverride();
            if (override != null) {
                currentIngredient = (EmiIngredient) override;
                return new EmiStackInteraction(currentIngredient);
            }

            EmiStackConverter.Converter<?> converter = EmiStackConverter.getForNullable(provider.ingredientClass());
            if (converter == null) {
                return EmiStackInteraction.EMPTY;
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            var converted = ((EmiStackConverter.Converter) converter).convertTo(provider);
            return new EmiStackInteraction(converted, null, false);
        }
        return EmiStackInteraction.EMPTY;
    }

    static EmiIngredient currentIngredient = null;

    @Override
    public void setSearchFocused(boolean focused) {
        EmiScreenManager.search.setFocused(focused);
    }

    @Override
    public @Nullable Object getCurrentlyDragged() {
        if (currentIngredient == null || currentIngredient.isEmpty()) return null;

        List<Object> dragged = new ArrayList<>();
        for (EmiStack stack : currentIngredient.getEmiStacks()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getKeyOfType(Item.class) != null) {
                dragged.add(EmiStackConverter.ITEM.convertFrom(stack));
            }
            if (stack.getKeyOfType(Fluid.class) != null) {
                dragged.add(EmiStackConverter.FLUID.convertFrom(stack));
            }
        }
        return dragged;
    }
}
