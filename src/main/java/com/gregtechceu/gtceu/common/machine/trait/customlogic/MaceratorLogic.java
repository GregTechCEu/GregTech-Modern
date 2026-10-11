package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.TurbineRotorItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeCategories.MACERATOR_RECYCLING;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;

public enum MaceratorLogic implements GTRecipeType.ICustomRecipeLogic {

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

    public @Nullable GTRecipe search(ItemStack stack) {
        if (stack.getItem() instanceof TurbineRotorItem rotor) {
            float durability = 1f - (float) stack.getDamageValue() / stack.getMaxDamage();
            return applyDurabilityRecipe("rotor_decomp", stack, rotor.material,
                    turbineRotor.materialAmount() / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        }

        if (stack.getItem() instanceof IGTTool tool && !tool.isElectric()) {
            int maxDurability = tool.getTotalMaxDurability(stack) + 1;
            float durability = (float) (maxDurability - stack.getDamageValue()) / maxDurability;
            return applyDurabilityRecipe("tool_decomp", stack, tool.getMaterial(),
                    tool.getToolType().materialAmount / GTValues.M, durability,
                    GTValues.VH[GTValues.LV], 2);
        }

        return null;
    }

    public @Nullable GTRecipe applyDurabilityRecipe(String id, ItemStack inputStack, Material mat,
                                                    long fullAmount, float durability, long voltage,
                                                    int durationFactor) {
        if (!mat.hasProperty(PropertyKey.DUST)) return null;

        float outputAmount = durability * fullAmount;
        int dustAmount = (int) outputAmount;
        int leftover = (int) ((outputAmount - (float) dustAmount) * 36f);
        TagPrefix tag = leftover % 4 >= leftover % 9 ? dustSmall : dustTiny;
        int leftAmount = leftover % 4 >= leftover % 9 ? leftover / 9 : leftover / 4;

        if (dustAmount == 0 && leftAmount == 0) return null;

        var builder = MACERATOR_RECIPES.recipeBuilder(id + "/" + mat.getName())
                .inputItems(inputStack)
                .EUt(voltage)
                .duration((int) (mat.getMass() * outputAmount) * durationFactor);

        if (dustAmount > 0) {
            builder.outputItems(dust, mat, dustAmount);
        }
        if (leftAmount > 0) {
            builder.outputItems(tag, mat, leftAmount);
        }

        return builder.buildRawRecipe();
    }

    @Override
    public void buildRepresentativeRecipes() {
        float durability = 0.75f;

        // noinspection DataFlowIssue
        ItemStack stack = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron, GTToolType.PICKAXE).asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.tool"));
        stack.setDamageValue(64);

        GTRecipe pickaxeRecipe = applyDurabilityRecipe("tool_decomp", stack, GTMaterials.Iron,
                GTToolType.PICKAXE.materialAmount / GTValues.M, durability,
                GTValues.VH[GTValues.LV], 2);
        assert pickaxeRecipe != null : "Default Tool Decomp recipe couldn't be generated";
        pickaxeRecipe.setId(pickaxeRecipe.getId().withPrefix("/"));
        MACERATOR_RECYCLING.addRecipe(pickaxeRecipe);

        // separator //

        stack = ChemicalHelper.get(turbineRotor, GTMaterials.Iron);
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.rotor"));
        stack.setDamageValue(7200);

        GTRecipe rotorRecipe = applyDurabilityRecipe("rotor_decomp", stack, GTMaterials.Iron,
                turbineRotor.materialAmount() / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        assert rotorRecipe != null : "Default Turbine Decomp recipe couldn't be generated";
        rotorRecipe.setId(rotorRecipe.getId().withPrefix("/"));
        MACERATOR_RECYCLING.addRecipe(rotorRecipe);
    }
}
