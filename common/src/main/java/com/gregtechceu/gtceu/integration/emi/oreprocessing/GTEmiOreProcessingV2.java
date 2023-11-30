package com.gregtechceu.gtceu.integration.emi.oreprocessing;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.ButtonWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GTEmiOreProcessingV2 implements EmiRecipe {
    public final Material material;
    protected final ResourceLocation id;
    protected final List<EmiIngredient> inputs = new ArrayList<>();
    protected final List<EmiStack> outputs = new ArrayList<>();
    public final List<EmiRecipe> oreSmeltings = new ArrayList<>();
    public GTEmiOreProcessingV2(EmiRegistry registry, Material material) {
        this.material = material;
        id = GTCEu.id("/ore_processing/" + material.getName());

        //inputs.clear();
        //outputs.clear();
        ClientLevel level = Minecraft.getInstance().level;
        if (level==null) return;
        Set<Ingredient> ores = new LinkedHashSet<>();
        Set<ItemStack> ingots = new LinkedHashSet<>();
        for (TagPrefix tagPrefix : TagPrefix.ORES.keySet()) {
            ItemStack oreStack = ChemicalHelper.get(tagPrefix, material);
            Optional<SmeltingRecipe> recipe0 = registry.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(oreStack), level);
            if (recipe0.isPresent()){
                SmeltingRecipe recipe = recipe0.get();
                ores.addAll(recipe.getIngredients());
                ingots.add(recipe.getResultItem(level.registryAccess()));
            }
            //Item oreItem = oreStack.getItem();
            //Optional<EmiRecipe> recipe0 = Optional.empty();
            //EmiRecipeManager recipeManager = EmiApi.getRecipeManager();
            //for (EmiRecipe recipe : recipeManager.getRecipesByInput(EmiStack.of(oreStack))) {
            //    if (recipe.getCategory() == VanillaEmiRecipeCategories.SMELTING) {
            //        recipe0 = Optional.of(recipe);
            //        break;
            //    }
            //}
            //if (recipe0.isPresent()) {
            //    EmiRecipe recipe = recipe0.get();
            //    inputs.addAll(recipe.getInputs());
            //    outputs.addAll(recipe.getOutputs());
            //    oreSmeltings.add(recipe);
            //}
            //ResourceLocation oreId = BuiltInRegistries.ITEM.getKey(oreItem);
            //ResourceLocation recipeId = oreId.withPrefix("smelting/smelt_").withSuffix("_to_ingot");
            //EmiRecipe recipe = recipeManager.getRecipe(recipeId);
            //if (recipe != null) {
            //    inputs.addAll(recipe.getInputs());
            //    outputs.addAll(recipe.getOutputs());
            //    oreSmeltings.add(recipe);
            //}
        }
        for (Ingredient oreIngredient : ores) {
            inputs.add(EmiIngredient.of(oreIngredient));
        }
        for (ItemStack ingot : ingots) {
            outputs.add(EmiStack.of(ingot));
        }
        //inputs.add(EmiIngredient.of(ores.stream().map(EmiIngredient::of).toList()));
        //outputs.addAll(EmiStack.of())

    }
    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreProcessingEmiCategory.CATEGORY;
    }
    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }
    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }
    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }
    @Override
    public int getDisplayWidth() {
        return 160;
    }
    @Override
    public int getDisplayHeight() {
        return 18;
    }
    @Override
    public void addWidgets(WidgetHolder widgets) {
        oreSmeltings.clear();
        for (TagPrefix tagPrefix : TagPrefix.ORES.keySet()) {
            ItemStack oreStack = ChemicalHelper.get(tagPrefix, material);
            Optional<EmiRecipe> recipe0 = Optional.empty();
            EmiRecipeManager recipeManager = EmiApi.getRecipeManager();
            for (EmiRecipe recipe : recipeManager.getRecipesByInput(EmiStack.of(oreStack))) {
                if (recipe.getCategory() == VanillaEmiRecipeCategories.SMELTING) {
                    recipe0 = Optional.of(recipe);
                    break;
                }
            }
            if (recipe0.isPresent()) {
                EmiRecipe recipe = recipe0.get();
                oreSmeltings.add(recipe);
            }
        }
        List<EmiIngredient> list = new ArrayList<>();
        for (EmiRecipe oreSmelting : oreSmeltings) {
            EmiIngredient emiIngredient = oreSmelting.getInputs().get(0);
            list.add(emiIngredient);
        }
        widgets.addButton(0, 0, EmiTexture.SLOT.width * 2, EmiTexture.SLOT.height, 0, 0, () -> true, new ButtonWidget.ClickAction() {
            @Override
            public void click(double mouseX, double mouseY, int button) {

            }
        });
        widgets.addSlot(EmiIngredient.of(list), 0, 0);
        widgets.addSlot(EmiIngredient.of(EmiApi.getRecipeManager().getWorkstations(VanillaEmiRecipeCategories.SMELTING)), EmiTexture.SLOT.width, 0).drawBack(false);
        List<EmiStack> result = new ArrayList<>();
        for (EmiRecipe r : oreSmeltings) {
            EmiStack emiStack = r.getOutputs().get(0);
            result.add(emiStack);
        }
        widgets.addSlot(EmiIngredient.of(result), EmiTexture.SLOT.width * 2, 0).recipeContext(this);
    }
}
