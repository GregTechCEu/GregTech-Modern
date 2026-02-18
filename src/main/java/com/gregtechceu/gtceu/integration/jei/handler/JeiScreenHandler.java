package com.gregtechceu.gtceu.integration.jei.handler;

import com.gregtechceu.gtceu.api.mui.base.IMuiScreen;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.utils.Rectangle;
import com.gregtechceu.gtceu.core.mixins.jei.IngredientListOverlayAccessor;
import com.gregtechceu.gtceu.integration.jei.GTJEIPlugin;
import com.gregtechceu.gtceu.integration.jei.GTJeiProperties;
import com.gregtechceu.gtceu.integration.jei.GhostIngredientTarget;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.GhostIngredientSlot;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.IngredientProvider;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.RecipeViewerHandler;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JeiScreenHandler<T extends Screen & IMuiScreen> extends RecipeViewerHandler
                             implements IGhostIngredientHandler<T>, IScreenHandler<T> {

    private static final Map<Class<?>, JeiScreenHandler<?>> CACHE = new Reference2ReferenceOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Screen & IMuiScreen> JeiScreenHandler<T> of(Class<T> clazz) {
        return (JeiScreenHandler<T>) CACHE.computeIfAbsent(clazz, clz -> new JeiScreenHandler<>((Class<T>) clz));
    }

    protected final Class<T> clazz;

    private JeiScreenHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <T extends Screen & IMuiScreen, M extends AbstractContainerMenu,
            T2 extends AbstractContainerScreen<M> & IMuiScreen> void register(Class<T> clazz,
                                                                              IGuiHandlerRegistration registration) {
        if (AbstractContainerScreen.class.isAssignableFrom(clazz)) {
            // noinspection unchecked
            ContainerScreen.ofContainer((Class<T2>) clazz).register(registration);
        } else {
            of(clazz).register(registration);
        }
    }

    public void register(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(this.clazz, this);
        registration.addGuiScreenHandler(this.clazz, this);
    }

    @Override
    public <I> List<Target<I>> getTargetsTyped(T screen, ITypedIngredient<I> ingredient, boolean doStart) {
        currentIngredient = ingredient;

        List<GhostIngredientSlot<?>> ghostSlots = screen.getScreen().getContext()
                .getRecipeViewerSettings().getGhostIngredientSlots();
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
    public boolean shouldHighlightTargets() {
        return false;
    }

    @Override
    public @Nullable IGuiProperties apply(T guiScreen) {
        return guiScreen.getScreen().getContext().getRecipeViewerSettings().isEnabled(guiScreen.getScreen()) ?
                new GTJeiProperties(guiScreen) : null;
    }

    @Override
    public void onComplete() {
        currentIngredient = null;
    }

    // this is an actual ItemStack/FluidStack instance, **not** an ITypedIngredient or such
    private static Object currentIngredient = null;

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
        return currentIngredient;
    }

    public static class ContainerScreen<T extends AbstractContainerMenu,
            T1 extends AbstractContainerScreen<T> & IMuiScreen>
                                       extends JeiScreenHandler<T1>
                                       implements IGuiContainerHandler<T1> {

        @SuppressWarnings("unchecked")
        public static <M extends AbstractContainerMenu,
                T extends AbstractContainerScreen<M> & IMuiScreen> ContainerScreen<M, T> ofContainer(Class<T> clazz) {
            return (ContainerScreen<M, T>) CACHE.computeIfAbsent(clazz, clz -> new ContainerScreen<>((Class<T>) clz));
        }

        private ContainerScreen(Class<T1> clazz) {
            super(clazz);
        }

        @Override
        public void register(IGuiHandlerRegistration registry) {
            super.register(registry);
            registry.addGuiContainerHandler(this.clazz, this);
        }

        @Override
        public List<Rect2i> getGuiExtraAreas(T1 screen) {
            return screen.getScreen().getContext()
                    .getRecipeViewerSettings().getAllExclusionAreas()
                    .stream().map(Rectangle::asRect2i)
                    .toList();
        }

        @Override
        public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(T1 screen, double mouseX,
                                                                                  double mouseY) {
            IWidget hovered = screen.getScreen().getContext().getTopHovered();
            if (hovered instanceof IngredientProvider<?> provider) {
                var override = provider.ingredientOverride();
                if (override instanceof IClickableIngredient<?> clickableIngredient) {
                    JeiScreenHandler.currentIngredient = clickableIngredient.getIngredient();
                    return Optional.of(clickableIngredient);
                }
                if (provider.getIngredients().isEmpty()) return Optional.empty();

                Optional<? extends IClickableIngredient<?>> ingredient = this
                        .createClickableIngredient(mapFirstIngredient(provider), hovered.getArea());

                JeiScreenHandler.currentIngredient = ingredient.map(IClickableIngredient::getIngredient).orElse(null);
                // noinspection unchecked
                return (Optional<IClickableIngredient<?>>) ingredient;
            }
            return Optional.empty();
        }

        private <I> I mapFirstIngredient(IngredientProvider<I> provider) {
            return provider.renderMappingFunction().apply(provider.getIngredients().getStacks().get(0));
        }

        private <I> Optional<IClickableIngredient<I>> createClickableIngredient(I ingredient, Rectangle area) {
            return GTJEIPlugin.getRuntime()
                    .getIngredientManager()
                    .createClickableIngredient(ingredient, area.asRect2i(), false);
        }
    }
}
