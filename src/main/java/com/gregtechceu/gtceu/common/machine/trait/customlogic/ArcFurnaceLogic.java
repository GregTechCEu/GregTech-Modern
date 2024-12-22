package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ARC_FURNACE_RECIPES;

public class ArcFurnaceLogic implements GTRecipeType.ICustomRecipeLogic {

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
        if (!mat.hasProperty(PropertyKey.INGOT))
            return null;

        var material = mat.getProperty(PropertyKey.INGOT);
        var materialArc = material.getArcSmeltingInto();

        float outputAmount = (durability * fullAmount);
        int dustAmount = (int) outputAmount;
        int leftover = (int) ((outputAmount - (float) dustAmount) * 9.f);

        if (dustAmount == 0 && leftover == 0)
            return null;

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
}
