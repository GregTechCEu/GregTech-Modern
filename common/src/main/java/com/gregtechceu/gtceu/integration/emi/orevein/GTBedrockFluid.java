package com.gregtechceu.gtceu.integration.emi.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GTBedrockFluid extends ModularEmiRecipe<WidgetGroup> {
    private final BedrockFluidDefinition fluid;
    public GTBedrockFluid(BedrockFluidDefinition fluid) {
        super(() -> new GTOreVeinWidget(fluid));
        this.fluid = fluid;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockFluidEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(fluid);
    }
}
