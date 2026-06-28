package com.gregtechceu.gtceu.integration.recipeviewer.rei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
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
public class GTBedrockOreDisplayCategory extends
                                         ModularUIREIDisplayCategory<GTBedrockOreDisplayCategory.GTBedrockOreDisplay> {

    public static final CategoryIdentifier<GTBedrockOreDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("bedrock_ore_diagram"));

    private final Renderer icon;

    public GTBedrockOreDisplayCategory() {
        this.icon = EntryStacks.of(GTMaterials.Oil.getFluid().getBucket().asItem());
    }

    @Override
    public CategoryIdentifier<? extends GTBedrockOreDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.bedrock_ore_diagram");
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (var fluid : ClientProxy.CLIENT_BEDROCK_ORE_VEINS.entrySet()) {
            registry.add(new GTBedrockOreDisplay(fluid.getKey(), fluid.getValue()));
        }
    }

    public static void registerWorkstations(CategoryRegistry registry) {
        registry.addWorkstations(GTBedrockOreDisplayCategory.CATEGORY,
                EntryStacks.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstations(GTBedrockOreDisplayCategory.CATEGORY,
                EntryStacks.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    public static class GTBedrockOreDisplay extends ModularUIREIDisplay {

        private final BedrockOreDefinition bedrockOre;

        public GTBedrockOreDisplay(ResourceLocation id, BedrockOreDefinition bedrockOre) {
            super(id, () -> new OreVeinRecipeWidget(bedrockOre), CATEGORY);
            this.bedrockOre = bedrockOre;
        }

        @Override
        public @NotNull List<EntryIngredient> getOutputEntries() {
            List<EntryIngredient> outputs = new ArrayList<>();
            for (Material material : bedrockOre.getAllMaterials()) {
                outputs.add(EntryIngredients.of(ChemicalHelper.get(TagPrefix.rawOre, material)));
            }
            return outputs;
        }
    }
}
