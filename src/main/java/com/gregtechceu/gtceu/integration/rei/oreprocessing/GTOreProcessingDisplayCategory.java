package com.gregtechceu.gtceu.integration.rei.oreprocessing;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.rei.IGui2Renderer;
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.ORE;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;

public class GTOreProcessingDisplayCategory extends ModularUIDisplayCategory<GTOreProcessingDisplay> {
    public static final CategoryIdentifier<GTOreProcessingDisplay> CATEGORY = CategoryIdentifier.of(GTCEu.id("ore_processing_diagram"));
    @Getter
    private final Renderer icon;

    @Getter
    private final Size size;

    public GTOreProcessingDisplayCategory() {
        this.icon = IGui2Renderer.toDrawable(new ItemStackTexture(Blocks.IRON_ORE.asItem()));
        this.size = new Size(186,174);
    }

    @Override
    public CategoryIdentifier<? extends GTOreProcessingDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @Override
    public int getDisplayHeight() {
        return getSize().height;
    }

    @Override
    public int getDisplayWidth(GTOreProcessingDisplay display) {
        return getSize().width;
    }

    @Nonnull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_processing_diagram");
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (Material mat : GTRegistries.MATERIALS) {
            if (mat.hasProperty(ORE)) {
                registry.add(new GTOreProcessingDisplay(mat));
            }
        }
    }

    public static void registerWorkstations(CategoryRegistry registry) {
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(MACERATOR[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(ORE_WASHER[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(THERMAL_CENTRIFUGE[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(CENTRIFUGE[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(CHEMICAL_BATH[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(ELECTROMAGNETIC_SEPARATOR[GTValues.LV].asStack()));
        registry.addWorkstations(GTOreProcessingDisplayCategory.CATEGORY, EntryStacks.of(SIFTER[GTValues.LV].asStack()));
    }
}
