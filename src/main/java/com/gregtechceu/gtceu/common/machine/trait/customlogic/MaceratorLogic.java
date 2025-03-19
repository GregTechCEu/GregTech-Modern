package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;

public class MaceratorLogic implements GTRecipeType.ICustomRecipeLogic {

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var itemInputs = Objects
                .requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
                        ArrayList::new)
                .stream()
                .filter(IItemHandlerModifiable.class::isInstance)
                .map(IItemHandlerModifiable.class::cast)
                .toArray(IItemHandlerModifiable[]::new);

        var inputs = new CombinedInvWrapper(itemInputs);
        var stack = inputs.getStackInSlot(0);

        var turbineBehaviour = TurbineRotorBehaviour.getBehaviour(stack);
        if (turbineBehaviour != null) {
            float durability = 1.f - (float) turbineBehaviour.getPartDamage(stack) /
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
        float outputAmount = (durability * fullAmount);
        int dustAmount = (int) outputAmount;
        int leftover = (int) ((outputAmount - (float) dustAmount) * 36.f);
        TagPrefix tag = leftover % 4 >= leftover % 9 ? dustSmall : dustTiny;
        int leftAmount = leftover % 4 >= leftover % 9 ? leftover / 9 : leftover / 4;

        if (dustAmount == 0 && leftAmount == 0)
            return null;

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
        ItemStack stack = GTItems.TURBINE_ROTOR.asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.rotor"));
        GTRecipe rotorRecipe;
        GTRecipe pickaxeRecipe;
        float durability = 0.75f;
        // noinspection ConstantConditions
        TurbineRotorBehaviour.getBehaviour(stack).setPartMaterial(stack, GTMaterials.Iron);
        TurbineRotorBehaviour.getBehaviour(stack).setPartDamage(stack, 8928);
        var turbineBehaviour = TurbineRotorBehaviour.getBehaviour(stack);

        rotorRecipe = applyDurabilityRecipe("rotor_decomp", stack, turbineBehaviour.getPartMaterial(stack),
                (float) (turbineBlade.materialAmount() * 8) / GTValues.M, durability, GTValues.VH[GTValues.EV], 1);
        rotorRecipe.setId(rotorRecipe.getId().withPrefix("/"));

        stack = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Iron, GTToolType.PICKAXE).asStack();
        stack.setHoverName(Component.translatable("gtceu.auto_decomp.tool"));
        stack.setDamageValue(79);
        pickaxeRecipe = applyDurabilityRecipe("tool_decomp", stack, GTMaterials.Iron,
                (float) (GTToolType.PICKAXE.materialAmount / GTValues.M), durability,
                GTValues.VH[GTValues.LV], 2);

        pickaxeRecipe.setId(pickaxeRecipe.getId().withPrefix("/"));
        MACERATOR_RECIPES.addToMainCategory(pickaxeRecipe);
        MACERATOR_RECIPES.addToMainCategory(rotorRecipe);
        GTRecipeType.ICustomRecipeLogic.super.buildRepresentativeRecipes();
    }
}
