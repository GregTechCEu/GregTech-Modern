package com.gregtechceu.gtceu.integration.emi.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockOre extends ModularEmiRecipe<WidgetGroup> {

    private final BedrockOreDefinition fluid;

    public GTBedrockOre(BedrockOreDefinition fluid) {
        super(() -> new GTOreVeinWidget(fluid));
        this.fluid = fluid;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockOreEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return ClientProxy.CLIENT_BEDROCK_ORE_VEINS.inverse().get(fluid);
    }
}
