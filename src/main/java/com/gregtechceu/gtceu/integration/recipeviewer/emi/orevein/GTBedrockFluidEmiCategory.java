package com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GTBedrockFluidEmiCategory extends EmiRecipeCategory {

    public static final GTBedrockFluidEmiCategory CATEGORY = new GTBedrockFluidEmiCategory();

    public GTBedrockFluidEmiCategory() {
        super(GTCEu.id("bedrock_fluid_diagram"), EmiStack.of(GTMaterials.Oil.getFluid().getBucket().asItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (BedrockFluidDefinition fluid : Minecraft.getInstance().level.registryAccess().registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID)) {
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

    public static class GTBedrockFluid extends ModularUIEmiRecipe {

        private final BedrockFluidDefinition fluid;

        public GTBedrockFluid(BedrockFluidDefinition fluid) {
            super(Objects.requireNonNull(Minecraft.getInstance().level.registryAccess().registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID).getKey(fluid))
                            .withPrefix("/bedrock_fluid_diagram/"),
                    () -> new OreVeinRecipeWidget(fluid));
            this.fluid = fluid;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return GTBedrockFluidEmiCategory.CATEGORY;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(fluid.dimensionFilter))
                    .map(v -> (EmiIngredient) EmiStack.of(v.getIcon())).toList();
        }

        @Override
        public @NotNull List<EmiStack> getOutputs() {
            return List.of(EmiStack.of(fluid.getStoredFluid()));
        }
    }
}
