package com.gregtechceu.gtceu.integration.recipeviewer.emi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiblockInfoEmiCategory extends EmiRecipeCategory {

    public static final MultiblockInfoEmiCategory CATEGORY = new MultiblockInfoEmiCategory();

    private MultiblockInfoEmiCategory() {
        super(GTCEu.id("multiblock_info"), EmiStack.of(GTMultiMachines.ELECTRIC_BLAST_FURNACE.getItem()));
    }

    public static void registerDisplays(EmiRegistry registry) {
        GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoEmiWrapper::new)
                .forEach(registry::addRecipe);
    }

    @Override
    public Component getName() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    public static class MultiblockInfoEmiWrapper extends ModularEmiRecipe<WidgetGroup> {

        private final MultiblockMachineDefinition definition;
        private SlotWidget slotWidget;

        public MultiblockInfoEmiWrapper(MultiblockMachineDefinition definition) {
            super(() -> PatternPreviewWidget.getPatternWidget(definition));
            this.definition = definition;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            super.addWidgets(widgets);
            // numbers gotten from the size of the widget
            slotWidget = new SlotWidget(EmiStack.of(definition.getItem().asItem()), 138, 12)
                    .recipeContext(this)
                    .drawBack(false);

            widgets.add(slotWidget);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return definition.getId().withPrefix("/multi_info/");
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(EmiStack.of(definition.getItem()));
        }
    }
}
