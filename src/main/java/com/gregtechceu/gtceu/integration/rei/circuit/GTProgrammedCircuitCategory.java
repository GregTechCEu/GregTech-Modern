package com.gregtechceu.gtceu.integration.rei.circuit;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.integration.xei.widgets.GTProgrammedCircuitWidget;

import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.IGui2Renderer;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;
import com.lowdragmc.lowdraglib.rei.ModularUIDisplayCategory;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import java.util.Optional;

public class GTProgrammedCircuitCategory extends
                                         ModularUIDisplayCategory<GTProgrammedCircuitCategory.GTProgrammedCircuitDisplay> {

    private static final ResourceLocation ID = GTCEu.id("programmed_circuit");
    public static final CategoryIdentifier<GTProgrammedCircuitDisplay> CATEGORY = CategoryIdentifier.of(ID);

    @Getter
    private final Renderer icon;
    @Getter
    private final Size size;

    public GTProgrammedCircuitCategory() {
        this.icon = IGui2Renderer.toDrawable(new ItemStackTexture(GTItems.PROGRAMMED_CIRCUIT.asItem()));
        this.size = new Size(158, 80);
    }

    @Override
    public CategoryIdentifier<? extends GTProgrammedCircuitDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.programmed_circuit");
    }

    @Override
    public int getDisplayHeight() {
        return getSize().height;
    }

    @Override
    public int getDisplayWidth(GTProgrammedCircuitDisplay display) {
        return getSize().width;
    }

    public static class GTProgrammedCircuitDisplay extends ModularDisplay<WidgetGroup> {

        public GTProgrammedCircuitDisplay() {
            super(GTProgrammedCircuitWidget::new, GTProgrammedCircuitCategory.CATEGORY);
        }

        @Override
        public Optional<ResourceLocation> getDisplayLocation() {
            return Optional.of(ID);
        }
    }
}
