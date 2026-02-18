package com.gregtechceu.gtceu.integration.jei.handler;

import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.RecipeTransferError;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.RecipeTransferHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class JeiContainerHandler<T extends ModularContainerMenu> implements IUniversalRecipeTransferHandler<T> {

    public static <T extends ModularContainerMenu> void register(Class<T> clz,
                                                                 IRecipeTransferRegistration registration) {
        new JeiContainerHandler<>(clz, registration.getTransferHelper()).register(registration);
    }

    private final Class<T> clazz;
    private final IRecipeTransferHandlerHelper handlerHelper;

    private JeiContainerHandler(Class<T> clazz, IRecipeTransferHandlerHelper handlerHelper) {
        this.clazz = clazz;
        this.handlerHelper = handlerHelper;
    }

    private void register(IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(this);
    }

    @Override
    public Class<T> getContainerClass() {
        return clazz;
    }

    @Override
    public Optional<MenuType<T>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(T container, Object recipe, IRecipeSlotsView recipeSlots,
                                                         Player player, boolean maxTransfer, boolean doTransfer) {
        ModularScreen screen = container.getScreen();
        if (screen instanceof RecipeTransferHandler<?> transferHandler &&
                Objects.equals(recipe.getClass(), transferHandler.getRecipeClass())) {
            RecipeTransferError muiError = transferHandler.transferRecipeSafe(recipe, maxTransfer, !doTransfer);
            if (muiError != null) {
                return new CosmeticJeiTransferError(muiError);
            }
        }
        return null;
    }

    public record CosmeticJeiTransferError(RecipeTransferError muiError) implements IRecipeTransferError {

        @Override
        public Type getType() {
            if (muiError instanceof RecipeTransferError.Internal) {
                return Type.INTERNAL;
            } else if (muiError instanceof RecipeTransferError.UserFacing) {
                return Type.USER_FACING;
            } else if (muiError instanceof RecipeTransferError.Cosmetic) {
                return Type.COSMETIC;
            } else {
                throw new IllegalStateException(
                        "Recipe transfer error %s is not an internal, user facing or cosmetic error"
                                .formatted(muiError));
            }
        }

        @Override
        public int getButtonHighlightColor() {
            return muiError.getButtonHighlightColor();
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip) {
            if (this.muiError instanceof RecipeTransferError.UserFacing) {
                tooltip.add(Component.translatable("jei.tooltip.transfer"));
                for (Component component : muiError.getTooltip()) {
                    tooltip.add(component.copy().withStyle(ChatFormatting.RED));
                }
            } else {
                tooltip.addAll(muiError.getTooltip());
            }
        }

        @Override
        public int getMissingCountHint() {
            return muiError.getMissingCountHint();
        }
    }
}
