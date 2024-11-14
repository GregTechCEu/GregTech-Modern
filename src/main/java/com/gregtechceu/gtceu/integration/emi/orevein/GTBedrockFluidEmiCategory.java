package com.gregtechceu.gtceu.integration.emi.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

public class GTBedrockFluidEmiCategory extends EmiRecipeCategory {

    public static final GTBedrockFluidEmiCategory CATEGORY = new GTBedrockFluidEmiCategory();

    public GTBedrockFluidEmiCategory() {
        super(GTCEu.id("bedrock_fluid_diagram"), EmiStack.of(GTMaterials.Oil.getFluid().getBucket().asItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (BedrockFluidDefinition fluid : ClientProxy.CLIENT_FLUID_VEINS.values()) {
            registry.addRecipe(new GTBedrockFluid(fluid));
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
    }
}
