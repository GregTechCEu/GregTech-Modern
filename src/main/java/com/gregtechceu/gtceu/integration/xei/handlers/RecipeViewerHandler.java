package com.gregtechceu.gtceu.integration.xei.handlers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.client.mui.screen.ScreenWrapper;
import com.gregtechceu.gtceu.integration.emi.handler.EmiScreenHandler;
import com.gregtechceu.gtceu.integration.jei.handler.JEIScreenHandler;
import com.gregtechceu.gtceu.integration.rei.handler.REIScreenHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class RecipeViewerHandler {

    private static RecipeViewerHandler current = null;

    @NotNull
    public static RecipeViewerHandler getCurrent() {
        if (current == null) {
            Supplier<Function<Class<ScreenWrapper>, ? extends RecipeViewerHandler>> supplier;
            if (GTCEu.isModLoaded(GTValues.MODID_EMI)) {
                supplier = () -> EmiScreenHandler::of;
            } else if (GTCEu.isModLoaded(GTValues.MODID_REI)) {
                supplier = () -> REIScreenHandler::of;
            } else if (GTCEu.isModLoaded(GTValues.MODID_JEI)) {
                supplier = () -> JEIScreenHandler::of;
            } else {
                supplier = () -> cls -> DUMMY;
            }
            current = supplier.get().apply(ScreenWrapper.class);
        }
        return current;
    }

    public abstract void setSearchFocused(boolean focused);

    public abstract @Nullable Object getCurrentlyDragged();

    public boolean isHoveringOver(GhostIngredientSlot<?> slot) {
        Object currentlyDragged = getCurrentlyDragged();
        if (currentlyDragged == null) {
            return false;
        } else if (currentlyDragged instanceof Iterable<?> iterable) {
            for (Object dragged : iterable) {
                if (slot.castGhostIngredientIfValid(dragged) != null) {
                    return true;
                }
            }
            return false;
        } else {
            return slot.castGhostIngredientIfValid(currentlyDragged) != null;
        }
    }

    private static final RecipeViewerHandler DUMMY = new RecipeViewerHandler() {

        @Override
        public void setSearchFocused(boolean focused) {}

        @Override
        public @Nullable Object getCurrentlyDragged() {
            return null;
        }
    };
}
