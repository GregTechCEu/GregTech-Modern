package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
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
import static com.gregtechceu.gtceu.common.data.GTRecipeCategories.ARC_FURNACE_RECYCLING;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ARC_FURNACE_RECIPES;

public enum ArcFurnaceLogic implements GTRecipeType.ICustomRecipeLogic {

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
        if (!mat.hasProperty(PropertyKey.INGOT)) return null;

        IngotProperty material = mat.getPropertyOrThrow(PropertyKey.INGOT);
        Material materialArc = material.getArcSmeltingInto();

        float outputAmount = durability * fullAmount;
        int dustAmount = (int) outputAmount;
        int leftover = (int) ((outputAmount - dustAmount) * 9.f);

        if (dustAmount == 0 && leftover == 0) return null;

        var builder = ARC_FURNACE_RECIPES.recipeBuilder(id + "/" + mat.getName())
                .inputItems(inputStack)
                .inputFluids(GTMaterials.Oxygen.getFluid((int) (materialArc.getMass() * outputAmount) * durationFactor))
                .EUt(voltage)
                .duration((int) (materialArc.getMass() * outputAmount) * durationFactor);

        if (dustAmount > 0) {
            builder.outputItems(ingot, materialArc, dustAmount);
        }
        if (leftover > 0) {
            builder.outputItems(nugget, materialArc, leftover);
        }

        return builder.buildRawRecipe();
    }

    @Override
    public void buildRepresentativeRecipes() {
        float durability = 0.69f;

        // noinspection DataFlowIssue
        ItemStack stack = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron, GTToolType.PICKAXE).asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.tool"));
        stack.setDamageValue(79);

        GTRecipe pickaxeRecipe = applyDurabilityRecipe("tool_decomp", stack, GTMaterials.Iron,
                GTToolType.PICKAXE.materialAmount / GTValues.M, durability,
                GTValues.VH[GTValues.LV], 2);
        assert pickaxeRecipe != null : "Default Tool Decomp recipe couldn't be generated";
        pickaxeRecipe.setId(pickaxeRecipe.getId().withPrefix("/"));
        ARC_FURNACE_RECYCLING.addRecipe(pickaxeRecipe);

        // separator //

        stack = ChemicalHelper.get(turbineRotor, GTMaterials.Iron);
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.rotor"));
        stack.setDamageValue(8928);

        GTRecipe rotorRecipe = applyDurabilityRecipe("rotor_decomp", stack, GTMaterials.Iron,
                turbineRotor.materialAmount() / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        assert rotorRecipe != null : "Default Turbine Decomp recipe couldn't be generated";
        rotorRecipe.setId(rotorRecipe.getId().withPrefix("/"));
        ARC_FURNACE_RECYCLING.addRecipe(rotorRecipe);
    }
}
