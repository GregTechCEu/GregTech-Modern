package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.nugget;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.turbineBlade;
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
        var turbineBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        if (turbineBehaviour != null) {
            float durability = 1f - (float) turbineBehaviour.getPartDamage(stack) /
                    (float) turbineBehaviour.getPartMaxDurability(stack);
            return applyDurabilityRecipe("rotor_decomp", stack, turbineBehaviour.getPartMaterial(stack),
                    (float) (turbineBlade.materialAmount() * 8) / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        }

        if (stack.getItem() instanceof IGTTool tool && !tool.isElectric()) {
            float durability = (float) (tool.getTotalMaxDurability(stack) - stack.getDamageValue() + 1) /
                    (tool.getTotalMaxDurability(stack) + 1);
            return applyDurabilityRecipe("tool_decomp", stack, tool.getMaterial(),
                    (float) (tool.getToolType().materialAmount / GTValues.M), durability,
                    GTValues.VH[GTValues.LV], 2);
        }

        return null;
    }

    public @Nullable GTRecipe applyDurabilityRecipe(String id, ItemStack inputStack, @NotNull Material mat,
                                                    float fullAmount, float durability, long voltage,
                                                    int durationFactor) {
        if (!mat.hasProperty(PropertyKey.INGOT)) return null;

        var material = mat.getProperty(PropertyKey.INGOT);
        var materialArc = material.getArcSmeltingInto();

        float outputAmount = (durability * fullAmount);
        int dustAmount = (int) outputAmount;
        int leftover = (int) ((outputAmount - (float) dustAmount) * 9.f);

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
        ItemStack stack = GTItems.TURBINE_ROTOR.asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.rotor"));
        GTRecipe rotorRecipe;
        GTRecipe pickaxeRecipe;
        float durability = 0.69f;
        var turbineBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        assert turbineBehaviour != null : "Default Turbine Stack doesn't have Turbine Behaviour";
        turbineBehaviour.setPartMaterial(stack, GTMaterials.Iron);
        turbineBehaviour.setPartDamage(stack, 8928);

        rotorRecipe = applyDurabilityRecipe("rotor_decomp", stack, turbineBehaviour.getPartMaterial(stack),
                (float) (turbineBlade.materialAmount() * 8) / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        assert rotorRecipe != null : "Default Turbine Decomp recipe couldn't be generated";
        rotorRecipe.setId(rotorRecipe.getId().withPrefix("/"));

        // noinspection DataFlowIssue
        stack = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron, GTToolType.PICKAXE).asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.tool"));
        stack.setDamageValue(79);
        pickaxeRecipe = applyDurabilityRecipe("tool_decomp", stack, GTMaterials.Iron,
                (float) (GTToolType.PICKAXE.materialAmount / GTValues.M), durability,
                GTValues.VH[GTValues.LV], 2);

        assert pickaxeRecipe != null : "Default Tool Decomp recipe couldn't be generated";
        pickaxeRecipe.setId(pickaxeRecipe.getId().withPrefix("/"));
        ARC_FURNACE_RECYCLING.addRecipe(pickaxeRecipe);
        ARC_FURNACE_RECYCLING.addRecipe(rotorRecipe);
    }
}
