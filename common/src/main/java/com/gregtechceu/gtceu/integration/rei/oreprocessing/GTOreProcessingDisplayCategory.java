package com.gregtechceu.gtceu.integration.rei.oreprocessing;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
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
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.DUST;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.ORE;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public class GTOreProcessingDisplayCategory extends ModularUIDisplayCategory<GTOreProcessingDisplay> {
    public static final CategoryIdentifier<GTOreProcessingDisplay> CATEGORY = CategoryIdentifier.of(new ResourceLocation(GTCEu.MOD_ID + ":ore_processing_diagram"));
    @Getter
    private final Renderer icon;

    @Getter
    private final Size size;

    public GTOreProcessingDisplayCategory() {
        this.icon = IGui2Renderer.toDrawable(new ItemStackTexture(ChemicalHelper.get(ingot, GTMaterials.Aluminium)));
        this.size = new Size(186,166);
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
        return Component.literal("Ore Processing Diagram");
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (Material mat : GTRegistries.MATERIALS) {
            if (mat.hasProperty(ORE)) {
                registry.add(new GTOreProcessingDisplay(mat));
            }
        }
    }

    public static void registerWorkStations(CategoryRegistry registry) {
        for (Material mat : GTRegistries.MATERIALS) {
            if (mat.hasProperty(ORE)) {
                registry.addWorkstations(CATEGORY, EntryStacks.of(ChemicalHelper.get(ore, mat)));
                registry.addWorkstations(CATEGORY, EntryStacks.of(ChemicalHelper.get(rawOre, mat)));
                registry.addWorkstations(CATEGORY, EntryStacks.of(ChemicalHelper.get(crushed, mat)));
                registry.addWorkstations(CATEGORY, EntryStacks.of(ChemicalHelper.get(crushedPurified, mat)));
                registry.addWorkstations(CATEGORY, EntryStacks.of(ChemicalHelper.get(crushedRefined, mat)));
            }
            if (mat.hasProperty(DUST)) {
                registry.addWorkstations(CATEGORY, EntryStacks.of(ChemicalHelper.get(dust, mat)));
            }
        }
    }

}
