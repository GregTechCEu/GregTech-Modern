package brachy.modularui.integration.rei.handler;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.integration.rei.REIStackConverter;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.gui.screens.Screen;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.Getter;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackProvider;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class REIScreenHandler<T extends Screen & IMuiScreen> extends RecipeViewerHandler
        implements DraggableStackProvider<T>, ExclusionZonesProvider<T> {

    private static final Map<Class<?>, REIScreenHandler<?>> CACHE = new Reference2ReferenceOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Screen & IMuiScreen> REIScreenHandler<T> of(Class<T> clazz) {
        return (REIScreenHandler<T>) CACHE.computeIfAbsent(clazz, clz -> new REIScreenHandler<>((Class<T>) clz));
    }

    protected final Class<T> clazz;

    @Getter
    private final DraggableStackVisitor<T> draggableVisitor;
    @Getter
    private final OverlayDecider overlayDecider;

    private REIScreenHandler(Class<T> clazz) {
        this.clazz = clazz;

        this.draggableVisitor = new MUIDraggableStackVisitor<>(this.clazz);
        this.overlayDecider = new MUIOverlayDecider(this.clazz);
    }

    public void register(ScreenRegistry registry) {
        registry.registerDraggableStackProvider(this);
        registry.registerDraggableStackVisitor(this.getDraggableVisitor());
        registry.registerDecider(this.getOverlayDecider());
    }

    public static <T extends Screen & IMuiScreen> void register(Class<T> clazz, ScreenRegistry registry) {
        of(clazz).register(registry);
    }

    public void register(ExclusionZones registry) {
        registry.register(this.clazz, this);
    }

    public static <T extends Screen & IMuiScreen> void register(Class<T> clazz, ExclusionZones registry) {
        of(clazz).register(registry);
    }

    @Override
    public @Nullable DraggableStack getHoveredStack(DraggingContext<T> context, double mouseX, double mouseY) {
        IWidget hovered = context.getScreen().screen().getContext().getTopHovered();
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
            @SuppressWarnings({"rawtypes", "unchecked"})
            var converted = ((REIStackConverter.Converter) converter).convertTo(provider);
            return new DraggableStack() {

                @Override
                public EntryStack<?> getStack() {
                    if (converted.isEmpty()) return EntryStack.empty();
                    return converted.getFirst();
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
        return this.clazz.isAssignableFrom(screen.getClass());
    }

    @Override
    public DraggingContext<T> getContext() {
        return DraggableStackProvider.super.getContext();
    }

    @Override
    public double getPriority() {
        return 10;
    }

    @Override
    public Collection<me.shedaniel.math.Rectangle> provide(T screen) {
        return screen.screen().getContext()
                .getRecipeViewerSettings().getAllExclusionAreas().stream()
                .map(REIScreenHandler::asREIRect)
                .toList();
    }

    protected static me.shedaniel.math.Rectangle asREIRect(Rectangle rect) {
        return new me.shedaniel.math.Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    protected static DraggableStack currentIngredient = null;

    @Override
    public void setSearchFocused(boolean focused) {
        TextField searchField = REIRuntime.getInstance().getSearchTextField();
        if (searchField != null) searchField.setFocused(focused);
    }

    @Override
    public boolean isSearchFocused(){
        TextField searchField = REIRuntime.getInstance().getSearchTextField();
        if (searchField != null) return searchField.isFocused();
        return false;
    }

    @Override
    public @Nullable Object getCurrentlyDragged() {
        if (currentIngredient == null) return null;
        return currentIngredient.get().getValue();
    }
}
