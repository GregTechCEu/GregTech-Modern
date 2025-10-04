package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;

public enum ForgeHammerLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var recipeHandlers = holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        for (var handler : recipeHandlers) {
            for (var content : handler.getContents()) {
                if (!(content instanceof ItemStack stack)) continue;
                if (stack.isEmpty()) continue;
                var recipe = search(stack);
                if (recipe != null) return recipe;
            }
        }
        return null;
    }

    private @Nullable GTRecipe search(ItemStack stack) {
        if (stack.getItem() instanceof MaterialBlockItem blockItem) {
            var oreTag = blockItem.tagPrefix;
            var mat = blockItem.material;

            var oreProperty = mat.getProperty(PropertyKey.ORE);

            return buildOreRecipe("hammer_", stack, mat, oreTag, oreProperty);
        }
        return null;
    }

    public @Nullable GTRecipe buildOreRecipe(String id, ItemStack inputStack, Material material,
                                             TagPrefix stoneTypePrefix, OreProperty property) {

        if (!material.shouldGenerateRecipesFor(stoneTypePrefix)) {
            return null;
        }

        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) {
            byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        }

        Material smeltingMaterial = property.getDirectSmeltResult().isNull() ? material :
                property.getDirectSmeltResult();
        ItemStack ingotStack;
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }

        int oreTypeMultiplier = TagPrefix.ORES.get(stoneTypePrefix).isDoubleDrops() ? 2 : 1;
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * oreTypeMultiplier);

        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        String prefixString = stoneTypePrefix == ore ? "" : stoneTypePrefix.name + "_";

        if(crushedStack.isEmpty()) return null;

        int dustAmount = property.getOreMultiplier() * oreTypeMultiplier;
        GTRecipeBuilder builder = FORGE_HAMMER_RECIPES
                .recipeBuilder(id + prefixString + material.getName() + "_ore_to_crushed_ore")
                .inputItems(inputStack.copyWithCount(1))
                .duration(10)
                .EUt(16)
                .category(GTRecipeCategories.ORE_FORGING);
        if (material.hasProperty(PropertyKey.GEM) && !ChemicalHelper.get(gem, material).isEmpty()) {
            builder.outputItems(ChemicalHelper.get(gem, material).copyWithCount(dustAmount));
        } else {
            builder.outputItems(crushedStack.copyWithCount(dustAmount));
        }
        return builder.buildRawRecipe();
    }

    @Override
    public void buildRepresentativeRecipes() {
        GTRecipeType.ICustomRecipeLogic.super.buildRepresentativeRecipes();
    }
}
