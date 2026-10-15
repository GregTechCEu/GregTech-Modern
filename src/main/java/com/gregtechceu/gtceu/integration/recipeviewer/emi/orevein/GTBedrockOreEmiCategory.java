package com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GTBedrockOreEmiCategory extends EmiRecipeCategory {

    public static final GTBedrockOreEmiCategory CATEGORY = new GTBedrockOreEmiCategory();

    public GTBedrockOreEmiCategory() {
        super(GTCEu.id("bedrock_ore_diagram"),
                EmiStack.of(ChemicalHelper.get(TagPrefix.rawOre, GTMaterials.Tungstate)));
    }

    public static void registerDisplays(EmiRegistry registry) {
        var fluids = Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(GTRegistries.Keys.BEDROCK_ORE);
        fluids.holders()
                .filter(ore -> ore.value().canGenerate())
                .forEach(ore -> registry.addRecipe(new GTBedrockOre(ore)));
    }

    public static void registerWorkStations(EmiRegistry registry) {
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.bedrock_ore_diagram");
    }

    public static class GTBedrockOre extends ModularUIEmiRecipe {

        private final Holder<BedrockOreDefinition> bedrockOre;

        public GTBedrockOre(Holder<BedrockOreDefinition> bedrockOre) {
            super(Objects.requireNonNull(bedrockOre.getKey()).location().withPrefix("/bedrock_ore_diagram/"),
                    () -> new OreVeinRecipeWidget(bedrockOre.value()));
            this.bedrockOre = bedrockOre;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return GTBedrockOreEmiCategory.CATEGORY;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(bedrockOre.value().dimensionFilter()))
                    .map(v -> (EmiIngredient) EmiStack.of(v.getIcon())).toList();
        }

        @Override
        public List<EmiStack> getOutputs() {
            return OreVeinRecipeWidget.getRawMaterialList(bedrockOre.value()).stream().map(EmiStack::of).toList();
        }
    }
}
