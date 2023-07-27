package com.gregtechceu.gtceu.api.gui.compass.component;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.compass.ILayoutComponent;
import com.gregtechceu.gtceu.api.gui.compass.LayoutPageWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/10/21
 * @implNote RecipeComponent
 */
@NoArgsConstructor
public class RecipeComponent extends AbstractComponent {
    @Nullable
    protected Recipe<?> recipe;

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        if (element.hasAttribute("id")) {
            var recipeID = new ResourceLocation(element.getAttribute("id"));
            for (Recipe<?> recipe : Minecraft.getInstance().getConnection().getRecipeManager().getRecipes()) {
                if (recipe.getId().equals(recipeID)) {
                    this.recipe = recipe;
                    return this;
                }
            }
        }
        return this;
    }

    @Override
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        if (recipe == null) return currentPage;
        Int2ObjectMap<Ingredient> inputs = new Int2ObjectArrayMap<>();
        var output = recipe.getResultItem();
        var ingredients = recipe.getIngredients();

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            int w = shapedRecipe.getWidth();
            int h = shapedRecipe.getHeight();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    inputs.put(i + j * w, ingredients.get(i + j * w));
                }
            }
        } else {
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.put(i, ingredients.get(i));
            }
        }

        WidgetGroup recipeGroup;
        ItemStack workstation = ItemStack.EMPTY;
        if (recipe instanceof CraftingRecipe) {
            recipeGroup = createCraftingRecipeWidget(inputs, output);
            workstation = new ItemStack(Items.CRAFTING_TABLE);
        } else if (recipe instanceof AbstractCookingRecipe) {
            recipeGroup = createSmeltingRecipeWidget(inputs, output);
            workstation = new ItemStack(Items.FURNACE);
        } else if (recipe instanceof GTRecipe gtRecipe) {
            var widget = new GTRecipeWidget(gtRecipe);
            recipeGroup = new WidgetGroup(0, 0, widget.getSize().width + 8, widget.getSize().height + 8);
            widget.addSelfPosition(4, 4);
            recipeGroup.setBackground(GuiTextures.BACKGROUND);
            recipeGroup.addWidget(widget);
            if (gtRecipe.recipeType.getIconSupplier() != null) {
                workstation = gtRecipe.recipeType.getIconSupplier().get();
            }
        } else {
            recipeGroup = createCraftingRecipeWidget(inputs, output);
        }
        recipeGroup.addWidget(new ImageWidget(-40, recipeGroup.getSize().height / 2 - 15, 30, 30, new ItemStackTexture(workstation)));
        return currentPage.addStreamWidget(recipeGroup);
    }

    protected WidgetGroup createSmeltingRecipeWidget(Int2ObjectMap<Ingredient> input, ItemStack output) {
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, 150, 30);
        widgetGroup.setBackground(GuiTextures.BACKGROUND);
        CycleItemStackHandler itemStackHandler = new CycleItemStackHandler(List.of(
                Arrays.stream(input.getOrDefault(0, Ingredient.EMPTY).getItems()).toList()));
        widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, 20, 6, false, false)
                .setBackground(GuiTextures.SLOT));

        var handler = new ItemStackTransfer();
        handler.setStackInSlot(0, output);
        widgetGroup.addWidget(new ProgressWidget(ProgressWidget.JEIProgress, 65, 5, 20, 20, new ProgressTexture()));
        widgetGroup.addWidget(new SlotWidget(handler, 0, 120, 6, false, false)
                .setBackground(GuiTextures.SLOT));
        return widgetGroup;
    }

    protected WidgetGroup createCraftingRecipeWidget(Int2ObjectMap<Ingredient> input, ItemStack output) {
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, 150, 12 + 18 * 3);
        widgetGroup.setBackground(GuiTextures.BACKGROUND);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                var itemStackHandler = new CycleItemStackHandler(List.of(
                        Arrays.stream(input.getOrDefault(x + y * 3, Ingredient.EMPTY).getItems()).toList()));
                widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, x * 18 + 20, y * 18 + 6, false, false)
                        .setBackground(GuiTextures.SLOT));
            }
        }

        var handler = new ItemStackTransfer();
        handler.setStackInSlot(0, output);
        widgetGroup.addWidget(new ProgressWidget(ProgressWidget.JEIProgress, (3 * 18 + 20) / 2 + 60 - 10, (12 + 18 * 3) / 2 - 10, 20, 20, GuiTextures.PROGRESS_BAR_ARROW));
        widgetGroup.addWidget(new SlotWidget(handler, 0, 120, (12 + 18 * 3) / 2 - 9, false, false)
                .setBackground(GuiTextures.SLOT));
        return widgetGroup;
    }
}
