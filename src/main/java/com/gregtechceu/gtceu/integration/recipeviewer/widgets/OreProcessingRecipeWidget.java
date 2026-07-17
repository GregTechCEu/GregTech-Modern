package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.gui.ContentOverlay;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidEntryList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemEntryList;
import brachy.modularui.widget.ParentWidget;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;

public class OreProcessingRecipeWidget extends ParentWidget<OreProcessingRecipeWidget> {

    // XY positions of every item and fluid, in three enormous lists
    protected final static IntImmutableList ITEM_INPUT_LOCATIONS = IntImmutableList.of(
            3, 3,       // ore
            23, 3,      // furnace (direct smelt)
            3, 24,      // macerator (ore -> crushed)
            23, 71,     // macerator (crushed -> impure)
            50, 80,     // centrifuge (impure -> dust)
            24, 25,     // ore washer
            97, 71,     // thermal centrifuge
            70, 80,     // macerator (centrifuged -> dust)
            114, 48,    // macerator (crushed purified -> purified)
            133, 71,    // centrifuge (purified -> dust)
            3, 123,     // cauldron / simple washer (crushed)
            41, 145,    // cauldron (impure)
            102, 145,   // cauldron (purified)
            24, 48,     // chem bath
            155, 71,    // electro separator
            101, 25     // sifter
    );

    protected final static IntImmutableList ITEM_OUTPUT_LOCATIONS = IntImmutableList.of(
            46, 3,      // smelt result: 0
            3, 47,      // ore -> crushed: 2
            3, 65,      // byproduct: 4
            23, 92,     // crushed -> impure: 6
            23, 110,    // byproduct: 8
            50, 101,    // impure -> dust: 10
            50, 119,    // byproduct: 12
            64, 25,     // crushed -> crushed purified (wash): 14
            82, 25,     // byproduct: 16
            97, 92,     // crushed/crushed purified -> centrifuged: 18
            97, 110,    // byproduct: 20
            70, 101,    // centrifuged -> dust: 22
            70, 119,    // byproduct: 24
            137, 47,    // crushed purified -> purified: 26
            155, 47,    // byproduct: 28
            133, 92,    // purified -> dust: 30
            133, 110,   // byproduct: 32
            3, 105,     // crushed cauldron: 34
            3, 145,     // -> purified crushed: 36
            23, 145,    // impure cauldron: 38
            63, 145,    // -> dust: 40
            84, 145,    // purified cauldron: 42
            124, 145,   // -> dust: 44
            64, 48,     // crushed -> crushed purified (chem bath): 46
            82, 48,     // byproduct: 48
            155, 92,    // purified -> dust (electro separator): 50
            155, 110,   // byproduct 1: 52
            155, 128,   // byproduct 2: 54
            119, 3,     // sifter outputs... : 56
            137, 3,     // 58
            155, 3,     // 60
            119, 21,    // 62
            137, 21,    // 64
            155, 21     // 66
    );

    protected final static IntImmutableList FLUID_LOCATIONS = IntImmutableList.of(
            42, 25, // washer in
            42, 48  // chem bath in
    );

    // Used to set intermediates as both input and output
    protected final static IntSet FINAL_OUTPUT_INDICES = IntSet.of(
            0, 4, 8, 10, 12, 16, 20, 22, 24, 28, 30, 32, 40, 44, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66);

    public OreProcessingRecipeWidget(Material material) {
        size(176, 166);
        setRecipe(new GTOreByProduct(material));
    }

    public void setRecipe(GTOreByProduct recipeWrapper) {
        // only draw slot on inputs if it is the ore
        boolean hasSifter = recipeWrapper.hasSifter();

        child(GTGuiTextures.OREBY_BASE.asWidget().size(176, 166));
        if (recipeWrapper.hasDirectSmelt()) {
            child(GTGuiTextures.OREBY_SMELT.asWidget().size(176, 166));
        }
        if (recipeWrapper.hasChemBath()) {
            child(GTGuiTextures.OREBY_CHEM.asWidget().size(176, 166));
        }
        if (recipeWrapper.hasSeparator()) {
            child(GTGuiTextures.OREBY_SEP.asWidget().size(176, 166));
        }
        if (hasSifter) {
            child(GTGuiTextures.OREBY_SIFT.asWidget().size(176, 166));
        }

        List<ItemEntryList> itemInputs = recipeWrapper.itemInputs;
        ParentWidget<?> itemStackGroup = new ParentWidget<>().sizeRel(1f);
        for (int i = 0; i < ITEM_INPUT_LOCATIONS.size(); i += 2) {
            itemStackGroup.child(RecipeViewerSlotWidget.create()
                    .recipeSlotRole(RecipeSlotRole.INPUT)
                    .pos(ITEM_INPUT_LOCATIONS.getInt(i), ITEM_INPUT_LOCATIONS.getInt(i + 1))
                    .tooltipBuilder(recipeWrapper.getTooltip(i / 2))
                    .value(itemInputs.get(i / 2))
                    .background(i == 0 ? GuiTextures.SLOT_ITEM : IDrawable.NONE));
        }

        NonNullList<ItemStack> itemOutputs = recipeWrapper.itemOutputs;
        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            int slotIndex = i / 2;
            Content chance = recipeWrapper.getChance(i / 2 + itemInputs.size());
            IDrawable overlay = null;
            if (chance != null) {
                overlay = new ContentOverlay(chance, false);
            }
            if (itemOutputs.get(slotIndex).isEmpty()) {
                continue;
            }

            itemStackGroup.child(RecipeViewerSlotWidget.create()
                    .pos(ITEM_OUTPUT_LOCATIONS.getInt(i), ITEM_OUTPUT_LOCATIONS.getInt(i + 1))
                    .recipeSlotRole(RecipeSlotRole.OUTPUT)
                    .tooltip(recipeWrapper.getTooltip(slotIndex + itemInputs.size()))
                    .overlay(overlay)
                    .value(itemOutputs.get(i / 2)));
        }

        List<FluidEntryList> fluidInputs = recipeWrapper.fluidInputs;
        ParentWidget<?> fluidStackGroup = new ParentWidget<>().sizeRel(1f);
        for (int i = 0; i < FLUID_LOCATIONS.size(); i += 2) {
            int slotIndex = i / 2;
            if (!fluidInputs.get(slotIndex).isEmpty()) {
                fluidStackGroup.child(RecipeViewerSlotWidget.create()
                        .recipeSlotRole(RecipeSlotRole.INPUT)
                        .pos(FLUID_LOCATIONS.getInt(i), FLUID_LOCATIONS.getInt(i + 1))
                        .value(fluidInputs.get(slotIndex)));
            }
        }

        child(itemStackGroup);
        child(fluidStackGroup);
    }
}
