package com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class GTOreVeinEmiCategory extends EmiRecipeCategory {

    public static final GTOreVeinEmiCategory CATEGORY = new GTOreVeinEmiCategory();

    public GTOreVeinEmiCategory() {
        super(GTCEu.id("ore_vein_diagram"), EmiStack.of(Items.RAW_IRON));
    }

    public static void registerDisplays(EmiRegistry registry) {
        for (GTOreDefinition oreDefinition : Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY)) {
            registry.addRecipe(new GTEmiOreVein(oreDefinition));
        }
    }

    public static void registerWorkStations(EmiRegistry registry) {
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_LV.asStack()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstation(CATEGORY, EmiStack.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.ore_vein_diagram");
    }

    public static class GTEmiOreVein extends ModularUIEmiRecipe {

        private final GTOreDefinition oreDefinition;

        public GTEmiOreVein(GTOreDefinition oreDefinition) {
            super(Minecraft.getInstance().level.registryAccess().registryOrThrow(GTRegistries.ORE_VEIN_REGISTRY)
                    .getKey(oreDefinition).withPrefix("/ore_vein_diagram/"),
                    () -> new OreVeinRecipeWidget(oreDefinition));
            this.oreDefinition = oreDefinition;
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return GTOreVeinEmiCategory.CATEGORY;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return Arrays.stream(OreVeinRecipeWidget.getDimensionMarkers(oreDefinition.dimensionFilter()))
                    .map(v -> (EmiIngredient) EmiStack.of(v.getIcon())).toList();
        }

        @Override
        public @NotNull List<EmiStack> getOutputs() {
            return OreVeinRecipeWidget.getContainedOresAndBlocks(oreDefinition)
                    .stream()
                    .map(EmiStack::of)
                    .toList();
        }
    }
}
