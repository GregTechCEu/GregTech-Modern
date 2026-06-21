package com.gregtechceu.gtceu.integration.rei.multipage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.rei.IGui2Renderer;
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory;

import net.minecraft.network.chat.Component;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

public class MultiblockInfoDisplayCategory extends ModularUIDisplayCategory<MultiblockInfoDisplay> {

    public static final CategoryIdentifier<MultiblockInfoDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("multiblock_info"));
    private final Renderer icon;

    public MultiblockInfoDisplayCategory() {
        this.icon = IGui2Renderer.toDrawable(new ItemStackTexture(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem()));
    }

    public static void registerDisplays(DisplayRegistry registry) {
        GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoDisplay::new)
                .forEach(registry::add);
    }

    @Override
    public int getDisplayHeight() {
        return 160 + 8;
    }

    @Override
    public int getDisplayWidth(MultiblockInfoDisplay display) {
        return 160 + 8;
    }

    @Override
    public CategoryIdentifier<? extends MultiblockInfoDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }
}
