package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.trait.customlogic.ToolDecompLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeCategories.MACERATOR_RECYCLING;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;

public final class MaceratorLogic extends ToolDecompLogic {

    public static final MaceratorLogic INSTANCE = new MaceratorLogic();

    private MaceratorLogic() {}

    @Override
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
    public GTRecipeCategory getDisplayRecipeCategory() {
        return MACERATOR_RECYCLING;
    }
}
