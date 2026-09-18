package brachy.modularui.integration.jei.recipe;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.jei.ModularUIJeiPlugin;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.recipeviewer.util.RecipeScreenRenderingUtil;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.TooltipFlag;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@ApiStatus.Experimental
public abstract class ModularUIRecipeCategory<T> implements IRecipeCategory<T> {

    private final LoadingCache<T, ModularScreen> modularScreenCache;

    protected ModularUIRecipeCategory(Function<T, IWidget> wrapperFunction, Function<T, Identifier> recipeIdGetter) {
        this.modularScreenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .maximumSize(10)
                .build(new CacheLoader<>() {

                    @Override
                    public ModularScreen load(T recipe) {
                        IWidget widget = wrapperFunction.apply(recipe);
                        Identifier recipeId = recipeIdGetter.apply(recipe);

                        ModularPanel<?> panel = ModularPanel.defaultPanel(recipeId.toString(),
                                widget.getArea().width, widget.getArea().height);
                        panel.child(widget);
                        return new ModularScreen(recipeId.getNamespace(), panel);
                    }
                });
    }

    private ModularScreen getModularScreen(T recipe) {
        return this.modularScreenCache.getUnchecked(recipe);
    }

    private static <T> void addJEISlot(IRecipeLayoutBuilder builder, IngredientProvider<T> widget,
                                       RecipeIngredientRole role, int index) {
        var type = ModularUIJeiPlugin.getRuntime().getIngredientManager()
                .getIngredientTypeChecked(widget.ingredientClass());
        if (type.isEmpty()) {
            return;
        }

        Area widgetArea = widget.getArea();
        IRecipeSlotBuilder slotBuilder = builder.addSlot(role, widgetArea.x, widgetArea.y);

        slotBuilder.addIngredients(type.get(), widget.getIngredients().getStacks());
        slotBuilder.setCustomRenderer(type.get(), new IIngredientRenderer<>() {

            @Override
            public void render(GuiGraphicsExtractor guiGraphics, T ingredient) {}

            @SuppressWarnings("removal")
            @Override
            public List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag) {
                return Collections.emptyList();
            }

            @Override
            public int getWidth() {
                return widgetArea.width;
            }

            @Override
            public int getHeight() {
                return widgetArea.height;
            }
        });
        // set slot name
        slotBuilder.setSlotName("slot_" + index);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        ModularScreen screen = getModularScreen(recipe);

        MutableInt i = new MutableInt(0);
        WidgetTree.foreachChildBFS(screen.getMainPanel(), widget -> {
            if (!(widget instanceof IngredientProvider<?> provider)) {
                return true;
            }
            RecipeIngredientRole role = mapToRole(provider.getRecipeRole());
            addJEISlot(builder, provider, role, i.getAndIncrement());
            return true;
        }, true);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        builder.addGuiEventListener(new ModularUIGuiEventListener(recipe));
        builder.addWidget(new UIForegroundRenderWidget(recipe));
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        ModularScreen screen = getModularScreen(recipe);

        RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, (int) mouseX, (int) mouseY,
                Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.clear();
    }

    public static RecipeIngredientRole mapToRole(RecipeSlotRole slotRole) {
        return switch (slotRole) {
            case INPUT -> RecipeIngredientRole.INPUT;
            case OUTPUT -> RecipeIngredientRole.OUTPUT;
            case CATALYST -> RecipeIngredientRole.CATALYST;
            case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
        };
    }

    public class ModularUIGuiEventListener implements IJeiGuiEventListener {

        private final T recipe;

        public ModularUIGuiEventListener(T recipe) {
            this.recipe = recipe;
        }

        public ScreenRectangle getArea() {
            return getModularScreen(this.recipe).getMainRectangle();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            //getModularScreen(this.recipe).mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mousePressed(button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mouseReleased(button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return getModularScreen(this.recipe).mouseDragged(button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
            return getModularScreen(this.recipe).mouseScrolled(deltaX, deltaY);
        }

        @Override
        public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
            return getModularScreen(this.recipe).keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public class UIForegroundRenderWidget implements IRecipeWidget {

        private final T recipe;

        public UIForegroundRenderWidget(T recipe) {
            this.recipe = recipe;
        }

        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(0, 0);
        }

        @Override
        public void drawWidget(GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
            ModularScreen screen = getModularScreen(this.recipe);
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, (int) mouseX, (int) mouseY,
                    Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        }
    }
}
