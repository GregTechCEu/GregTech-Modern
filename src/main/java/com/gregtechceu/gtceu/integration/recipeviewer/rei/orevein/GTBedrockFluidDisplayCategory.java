package com.gregtechceu.gtceu.integration.recipeviewer.rei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.integration.rei.recipe.ModularUIREIDisplay;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplayCategory;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GTBedrockFluidDisplayCategory extends
                                           ModularUIREIDisplayCategory<GTBedrockFluidDisplayCategory.GTBedrockFluidDisplay> {

    public static final CategoryIdentifier<GTBedrockFluidDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("bedrock_fluid_diagram"));

    private final Renderer icon;

    public GTBedrockFluidDisplayCategory() {
        this.icon = EntryStacks.of(GTMaterials.Oil.getFluid().getBucket().asItem());
    }

    @Override
    public CategoryIdentifier<? extends GTBedrockFluidDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_fluid_diagram");
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (var fluid : ClientProxy.CLIENT_FLUID_VEINS.entrySet()) {
            registry.add(new GTBedrockFluidDisplay(fluid.getKey(), fluid.getValue()));
        }
    }

    public static void registerWorkstations(CategoryRegistry registry) {
        registry.addWorkstations(GTBedrockFluidDisplayCategory.CATEGORY,
                EntryStacks.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstations(GTBedrockFluidDisplayCategory.CATEGORY,
                EntryStacks.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    public static class GTBedrockFluidDisplay extends ModularUIREIDisplay {

        private final BedrockFluidDefinition fluid;

        public GTBedrockFluidDisplay(ResourceLocation id, BedrockFluidDefinition fluid) {
            super(id, () -> new OreVeinRecipeWidget(fluid), GTBedrockFluidDisplayCategory.CATEGORY);
            this.fluid = fluid;
        }

        @Override
        public @NotNull List<EntryIngredient> getOutputEntries() {
            List<EntryIngredient> outputs = new ArrayList<>();
            outputs.add(EntryIngredients.of(fluid.getStoredFluid().get()));
            return outputs;
        }
    }
}
