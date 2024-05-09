package com.gregtechceu.gtceu.integration.emi.oreprocessing;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.integration.GTOreByProductWidget;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;


public class GTEmiOreProcessing extends ModularEmiRecipe<GTOreByProductWidget> {
    final Material material;

    public GTEmiOreProcessing(Material material) {
        super(() -> new GTOreByProductWidget(material));
        this.material = material;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreProcessingEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return material.getResourceLocation();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
