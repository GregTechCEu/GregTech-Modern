package com.gregtechceu.gtceu.integration.recipeviewer.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreProcessingRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import brachy.modularui.integration.rei.recipe.ModularUIREIDisplay;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplayCategory;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.ORE;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;

public class GTOreProcessingReiCategory extends
                                        ModularUIREIDisplayCategory<GTOreProcessingReiCategory.GTOreProcessingDisplay> {

    public static final CategoryIdentifier<GTOreProcessingDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("ore_processing_diagram"));
    @Getter
    private final Renderer icon;

    public GTOreProcessingReiCategory() {
        this.icon = EntryStacks.of(Items.RAW_IRON);
    }

    @Override
    public CategoryIdentifier<? extends GTOreProcessingDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_processing_diagram");
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (Material mat : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (mat.hasProperty(ORE) && !mat.hasFlag(MaterialFlags.NO_ORE_PROCESSING_TAB)) {
                registry.add(new GTOreProcessingDisplay(mat));
            }
        }
    }

    public static void registerWorkstations(CategoryRegistry registry) {
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(MACERATOR[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(ORE_WASHER[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(THERMAL_CENTRIFUGE[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(CENTRIFUGE[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(CHEMICAL_BATH[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(ELECTROMAGNETIC_SEPARATOR[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingReiCategory.CATEGORY,
                EntryStacks.of(SIFTER[GTValues.LV].asStack()));
    }

    public static class GTOreProcessingDisplay extends ModularUIREIDisplay {

        public GTOreProcessingDisplay(Material material) {
            super(material.getResourceLocation(), () -> new OreProcessingRecipeWidget(material), CATEGORY);
        }
    }
}
