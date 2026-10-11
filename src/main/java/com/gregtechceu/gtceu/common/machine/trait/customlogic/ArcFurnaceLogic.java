package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.machine.trait.customlogic.ToolDecompLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeCategories.ARC_FURNACE_RECYCLING;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ARC_FURNACE_RECIPES;

public final class ArcFurnaceLogic extends ToolDecompLogic {

    public static final ArcFurnaceLogic INSTANCE = new ArcFurnaceLogic();

    public ArcFurnaceLogic() {
        super(ARC_FURNACE_RECYCLING);
    }

    @Override
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
}
