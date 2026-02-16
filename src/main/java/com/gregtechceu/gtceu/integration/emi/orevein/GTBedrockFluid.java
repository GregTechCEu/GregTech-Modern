package com.gregtechceu.gtceu.integration.emi.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockFluid extends ModularEmiRecipe<WidgetGroup> {

    private final Holder<BedrockFluidDefinition> fluid;

    public GTBedrockFluid(Holder<BedrockFluidDefinition> fluid) {
        super(() -> new GTOreVeinWidget(fluid, null));
        this.fluid = fluid;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockFluidEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return fluid.getKey().location().withPrefix("/bedrock_fluid_diagram/");
    }
}
