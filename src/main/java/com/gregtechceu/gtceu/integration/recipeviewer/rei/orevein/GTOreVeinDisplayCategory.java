package com.gregtechceu.gtceu.integration.recipeviewer.rei.orevein;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.OreVeinRecipeWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
public class GTOreVeinDisplayCategory extends ModularUIREIDisplayCategory<GTOreVeinDisplayCategory.GTOreVeinDisplay> {

    public static final CategoryIdentifier<GTOreVeinDisplay> CATEGORY = CategoryIdentifier
            .of(GTCEu.id("ore_vein_diagram"));

    private final Renderer icon;

    public GTOreVeinDisplayCategory() {
        this.icon = EntryStacks.of(Items.IRON_INGOT);
    }

    @Override
    public CategoryIdentifier<? extends GTOreVeinDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.ore_vein_diagram");
    }

    public static void registerDisplays(DisplayRegistry registry) {
        for (var oreDefinition : ClientProxy.CLIENT_ORE_VEINS.entrySet()) {
            registry.add(new GTOreVeinDisplay(oreDefinition.getKey(), oreDefinition.getValue()));
        }
    }

    public static void registerWorkstations(CategoryRegistry registry) {
        registry.addWorkstations(GTOreVeinDisplayCategory.CATEGORY, EntryStacks.of(GTItems.PROSPECTOR_LV.asStack()));
        registry.addWorkstations(GTOreVeinDisplayCategory.CATEGORY, EntryStacks.of(GTItems.PROSPECTOR_HV.asStack()));
        registry.addWorkstations(GTOreVeinDisplayCategory.CATEGORY, EntryStacks.of(GTItems.PROSPECTOR_LuV.asStack()));
    }

    public static class GTOreVeinDisplay extends ModularUIREIDisplay {

        private final GTOreDefinition oreDefinition;

        public GTOreVeinDisplay(ResourceLocation id, GTOreDefinition oreDefinition) {
            super(id, () -> new OreVeinRecipeWidget(oreDefinition), CATEGORY);
            this.oreDefinition = oreDefinition;
        }

        @Override
        public @NotNull List<EntryIngredient> getOutputEntries() {
            List<EntryIngredient> ingredients = new ArrayList<>();
            for (ItemStack output : OreVeinRecipeWidget.getContainedOresAndBlocks(oreDefinition)) {
                ingredients.add(EntryIngredients.of(output));
            }
            return ingredients;
        }
    }
}
