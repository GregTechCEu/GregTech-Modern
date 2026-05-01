package com.lowdragmc.lowdraglib.jei;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ModularUIRecipeCategory<T> implements IRecipeCategory<T> {

    private final Function<T, ModularWrapper<?>> wrapperProvider;
    private final Map<T, ModularWrapper<?>> wrappers = new IdentityHashMap<>();

    protected ModularUIRecipeCategory() {
        this(recipe -> new ModularWrapper<>((Widget) recipe));
    }

    protected ModularUIRecipeCategory(Function<T, ModularWrapper<?>> wrapperProvider) {
        this.wrapperProvider = wrapperProvider;
    }

    private ModularWrapper<?> getModularWrapper(T recipe) {
        return wrappers.computeIfAbsent(recipe, value -> {
            var wrapper = wrapperProvider.apply(value);
            wrapper.setRecipeWidget(0, 0);
            return wrapper;
        });
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        var widgets = getModularWrapper(recipe).modularUI.getFlatWidgetCollection().stream().toList();
        for (int i = 0; i < widgets.size(); i++) {
            if (widgets.get(i) instanceof IRecipeIngredientSlot slot) {
                var role = mapToRole(slot.getIngredientIO());
                if (role != RecipeIngredientRole.RENDER_ONLY) {
                    addJEISlot(builder, slot, role, i);
                }
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        builder.addWidget(new ModularRecipeWidget(getModularWrapper(recipe)));
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        getModularWrapper(recipe).render(graphics, (int) mouseX, (int) mouseY, 0);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltipBuilder, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX,
                           double mouseY) {
        var wrapper = getModularWrapper(recipe);
        if (!wrapper.isShouldRenderTooltips()) {
            return;
        }
        var hover = wrapper.getWidget().getHoverElement(mouseX, mouseY);
        if (hover != null) {
            tooltipBuilder.addAll(hover.getTooltipTexts());
        }
    }

    public static RecipeIngredientRole mapToRole(IngredientIO ingredientIO) {
        if (ingredientIO == null) {
            return RecipeIngredientRole.RENDER_ONLY;
        }
        return switch (ingredientIO) {
            case INPUT, BOTH -> RecipeIngredientRole.INPUT;
            case OUTPUT -> RecipeIngredientRole.OUTPUT;
            case CATALYST -> RecipeIngredientRole.CATALYST;
            case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
        };
    }

    private static void addJEISlot(IRecipeLayoutBuilder builder, IRecipeIngredientSlot slot, RecipeIngredientRole role,
                                   int index) {
        var widget = slot.self();
        var slotBuilder = builder.addSlot(role, widget.getPositionX(), widget.getPositionY())
                .setSlotName("slot_" + index);
        var typedIngredients = new ArrayList<ITypedIngredient<?>>();
        var rawIngredients = new ArrayList<>();
        for (Object ingredient : slot.getXEIIngredients()) {
            if (ingredient == null) {
                continue;
            }
            if (ingredient instanceof ClickableIngredient<?> clickable) {
                typedIngredients.add(clickable.getTypedIngredient());
            } else if (ingredient instanceof ITypedIngredient<?> typed) {
                typedIngredients.add(typed);
            } else if (ingredient instanceof ItemStack itemStack) {
                rawIngredients.add(itemStack);
            } else if (ingredient instanceof FluidStack fluidStack && JEIPlugin.jeiHelpers != null) {
                var helper = JEIPlugin.jeiHelpers.getPlatformFluidHelper();
                var jeiFluid = helper.create(fluidStack.typeHolder(), fluidStack.getAmount(),
                        fluidStack.getComponentsPatch());
                JEIPlugin.jeiHelpers.getIngredientManager()
                        .createTypedIngredient(jeiFluid, false)
                        .ifPresent(typedIngredients::add);
            }
        }
        if (!typedIngredients.isEmpty()) {
            slotBuilder.addTypedIngredients(typedIngredients);
        }
        if (!rawIngredients.isEmpty()) {
            slotBuilder.addIngredientsUnsafe(rawIngredients);
        }
    }

    private record ModularRecipeWidget(ModularWrapper<?> wrapper) implements mezz.jei.api.gui.widgets.IRecipeWidget {

        @Override
        public net.minecraft.client.gui.navigation.ScreenPosition getPosition() {
            return new net.minecraft.client.gui.navigation.ScreenPosition(0, 0);
        }

        @Override
        public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            wrapper.render(guiGraphics, (int) mouseX, (int) mouseY, 0);
        }
    }
}
