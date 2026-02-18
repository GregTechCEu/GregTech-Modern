package com.gregtechceu.gtceu.integration.jei.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.widget.WidgetTree;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.integration.jei.GTJEIPlugin;
import com.gregtechceu.gtceu.integration.recipeviewer.RecipeSlotRole;
import com.gregtechceu.gtceu.integration.recipeviewer.handlers.IngredientProvider;
import com.gregtechceu.gtceu.integration.recipeviewer.util.RecipeScreenRenderingUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.RecipeTypes;
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
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class GTRecipeJEICategory<T extends Recipe<?>, W extends IWidget> implements IRecipeCategory<T> {

    public static final Function<GTRecipeCategory, RecipeType<GTRecipe>> TYPES = Util
            .memoize(c -> new RecipeType<>(c.registryKey, GTRecipe.class));

    private final LoadingCache<T, ModularScreen> modularScreenCache;

    /*
     * private final GTRecipeCategory category;
     * 
     * @Getter
     * private final IDrawable background;
     * 
     * @Getter
     * private final IDrawable icon;
     */

    protected GTRecipeJEICategory(Function<T, W> wrapperFunction, Function<T, ResourceLocation> recipeIdGetter) {
        this.modularScreenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .maximumSize(10)
                .build(new CacheLoader<>() {

                    @Override
                    public ModularScreen load(T recipe) {
                        W widget = wrapperFunction.apply(recipe);
                        ResourceLocation recipeId = recipeIdGetter.apply(recipe);

                        ModularPanel panel = ModularPanel.defaultPanel(recipeId.toString(),
                                widget.getArea().width, widget.getArea().height);
                        panel.child(widget);
                        return new ModularScreen(recipeId.getNamespace(), panel);
                    }
                });
    }

    /*
     * public GTRecipeJEICategory(IJeiHelpers helpers,
     * 
     * @NotNull GTRecipeCategory category) {
     * super(GTRecipeWrapper::new);
     * this.category = category;
     * var recipeType = category.getRecipeType();
     * IGuiHelper guiHelper = helpers.getGuiHelper();
     * var size = recipeType.getRecipeUI().getJEISize();
     * this.background = guiHelper.createBlankDrawable(size.width, size.height);
     * this.icon = IGui2IDrawable.toDrawable(category.getIcon(), 16, 16);
     * }
     */

    public static void registerRecipes(IRecipeRegistration registration) {
        List<GTRecipeCategory> subCategories = new ArrayList<>();
        // run main categories first
        for (GTRecipeCategory category : GTRegistries.RECIPE_CATEGORIES) {
            if (!category.shouldRegisterDisplays()) continue;
            var type = category.getRecipeType();
            if (category == type.getCategory()) {
                type.buildRepresentativeRecipes();
            } else {
                subCategories.add(category);
                continue;
            }
            var wrapped = List.copyOf(type.getRecipesInCategory(category));
            registration.addRecipes(TYPES.apply(category), wrapped);
        }
        // run subcategories
        for (GTRecipeCategory subCategory : subCategories) {
            if (!subCategory.shouldRegisterDisplays()) continue;
            var type = subCategory.getRecipeType();
            var wrapped = List.copyOf(type.getRecipesInCategory(subCategory));
            registration.addRecipes(TYPES.apply(subCategory), wrapped);
        }
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (MachineDefinition machine : GTRegistries.MACHINES) {
            for (GTRecipeType type : machine.getRecipeTypes()) {
                for (GTRecipeCategory category : type.getCategories()) {
                    if (!category.isXEIVisible() && !GTCEu.isDev()) continue;
                    registration.addRecipeCatalyst(machine.asStack(), machineType(category));
                }
            }
        }
    }

    public static RecipeType<?> machineType(GTRecipeCategory category) {
        if (category == GTRecipeTypes.FURNACE_RECIPES.getCategory()) return RecipeTypes.SMELTING;
        return TYPES.apply(category);
    }

    private ModularScreen getModularScreen(T recipe) {
        return this.modularScreenCache.getUnchecked(recipe);
    }

    private static <T> void addJEISlot(IRecipeLayoutBuilder builder, IngredientProvider<T> widget,
                                       RecipeIngredientRole role, int index) {
        var type = GTJEIPlugin.getRuntime().getIngredientManager()
                .getIngredientTypeChecked(widget.ingredientClass());
        if (type.isEmpty()) {
            return;
        }

        Area widgetArea = widget.getArea();
        IRecipeSlotBuilder slotBuilder = builder.addSlot(role, widgetArea.x, widgetArea.y);

        slotBuilder.addIngredients(type.get(), widget.getIngredients().getStacks());
        slotBuilder.setCustomRenderer(type.get(), new IIngredientRenderer<>() {

            @Override
            public void render(GuiGraphics guiGraphics, T ingredient) {}

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
            RecipeIngredientRole role = mapToRole(provider.recipeRole());
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
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX,
                     double mouseY) {
        ModularScreen screen = getModularScreen(recipe);

        RecipeScreenRenderingUtil.drawScreenBackground(guiGraphics, screen, (int) mouseX, (int) mouseY,
                Minecraft.getInstance().getPartialTick());
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX,
                           double mouseY) {
        // tooltip.clear();
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
            return getModularScreen(this.recipe).getRectangle();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            getModularScreen(this.recipe).mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return getModularScreen(this.recipe).mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
            return getModularScreen(this.recipe).mouseScrolled(mouseX, mouseY, scrollDelta);
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
        public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            ModularScreen screen = getModularScreen(this.recipe);
            RecipeScreenRenderingUtil.drawScreenForeground(guiGraphics, screen, (int) mouseX, (int) mouseY,
                    Minecraft.getInstance().getPartialTick());
        }
    }

    /*
     * @Override
     * 
     * @NotNull
     * public RecipeType<GTRecipe> getRecipeType() {
     * return TYPES.apply(category);
     * }
     * 
     * @Override
     * 
     * @NotNull
     * public Component getTitle() {
     * return Component.translatable(category.getLanguageKey());
     * }
     * 
     * @Override
     * public @Nullable ResourceLocation getRegistryName(@NotNull GTRecipe recipe) {
     * return recipe.id;
     * }
     */
}
