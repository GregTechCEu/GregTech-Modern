package com.gregtechceu.gtceu.integration.xei.widgets;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.TagOrCycleFluidHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.TagOrCycleItemStackHandler;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GTOreByProductWidget extends WidgetGroup {

    // XY positions of every item and fluid, in three enormous lists
    protected final static ImmutableList<Integer> ITEM_INPUT_LOCATIONS = ImmutableList.of(
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

    protected final static ImmutableList<Integer> ITEM_OUTPUT_LOCATIONS = ImmutableList.of(
            46, 3,      // smelt result
            3, 47,      // ore -> crushed
            3, 65,      // byproduct
            23, 92,     // crushed -> impure
            23, 110,    // byproduct
            50, 101,    // impure -> dust
            50, 119,    // byproduct
            64, 25,     // crushed -> crushed purified (wash)
            82, 25,     // byproduct
            97, 92,     // crushed/crushed purified -> centrifuged
            97, 110,    // byproduct
            70, 101,    // centrifuged -> dust
            70, 119,    // byproduct
            137, 47,    // crushed purified -> purified
            155, 47,    // byproduct
            133, 92,    // purified -> dust
            133, 110,   // byproduct
            3, 105,     // crushed cauldron
            3, 145,     // -> purified crushed
            23, 145,    // impure cauldron
            63, 145,    // -> dust
            84, 145,    // purified cauldron
            124, 145,   // -> dust
            64, 48,     // crushed -> crushed purified (chem bath)
            82, 48,     // byproduct
            155, 92,    // purified -> dust (electro separator)
            155, 110,   // byproduct 1
            155, 128,   // byproduct 2
            119, 3,     // sifter outputs...
            137, 3,
            155, 3,
            119, 21,
            137, 21,
            155, 21);

    protected final static ImmutableList<Integer> FLUID_LOCATIONS = ImmutableList.of(
            42, 25, // washer in
            42, 48  // chem bath in
    );

    public GTOreByProductWidget(Material material) {
        super(0, 0, 176, 166);
        setClientSideWidget();
        setRecipe(new GTOreByProduct(material));
    }

    public void setRecipe(GTOreByProduct recipeWrapper) {
        List<Boolean> itemOutputExists = new ArrayList<>();

        // only draw slot on inputs if it is the ore
        addWidget(new ImageWidget(ITEM_INPUT_LOCATIONS.get(0), ITEM_INPUT_LOCATIONS.get(1), 18, 18, GuiTextures.SLOT));
        boolean hasSifter = recipeWrapper.hasSifter();

        addWidget(new ImageWidget(0, 0, 176, 166, GuiTextures.OREBY_BASE));
        if (recipeWrapper.hasDirectSmelt()) {
            addWidget(new ImageWidget(0, 0, 176, 166, GuiTextures.OREBY_SMELT));
        }
        if (recipeWrapper.hasChemBath()) {
            addWidget(new ImageWidget(0, 0, 176, 166, GuiTextures.OREBY_CHEM));
        }
        if (recipeWrapper.hasSeparator()) {
            addWidget(new ImageWidget(0, 0, 176, 166, GuiTextures.OREBY_SEP));
        }
        if (hasSifter) {
            addWidget(new ImageWidget(0, 0, 176, 166, GuiTextures.OREBY_SIFT));
        }

        List<Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>>> itemInputs = recipeWrapper.itemInputs;
        TagOrCycleItemStackHandler itemInputsHandler = new TagOrCycleItemStackHandler(itemInputs);
        WidgetGroup itemStackGroup = new WidgetGroup();
        for (int i = 0; i < ITEM_INPUT_LOCATIONS.size(); i += 2) {
            final int finalI = i;
            itemStackGroup.addWidget(new SlotWidget(itemInputsHandler, i / 2, ITEM_INPUT_LOCATIONS.get(i),
                    ITEM_INPUT_LOCATIONS.get(i + 1))
                    .setCanTakeItems(false)
                    .setCanPutItems(false)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setOnAddedTooltips((slot, tooltips) -> recipeWrapper.getTooltip(finalI / 2, tooltips))
                    .setBackground((IGuiTexture) null));
        }

        NonNullList<ItemStack> itemOutputs = recipeWrapper.itemOutputs;
        CustomItemStackHandler itemOutputsHandler = new CustomItemStackHandler(itemOutputs);
        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            int slotIndex = i / 2;
            float xeiChance = 1.0f;
            Content chance = recipeWrapper.getChance(i / 2 + itemInputs.size());
            IGuiTexture overlay = null;
            if (chance != null) {
                xeiChance = (float) chance.chance / chance.maxChance;
                overlay = chance.createOverlay(false, 0, 0, null);
            }
            if (itemOutputs.get(slotIndex).isEmpty()) {
                itemOutputExists.add(false);
                continue;
            }

            itemStackGroup.addWidget(new SlotWidget(itemOutputsHandler, slotIndex, ITEM_OUTPUT_LOCATIONS.get(i),
                    ITEM_OUTPUT_LOCATIONS.get(i + 1))
                    .setCanTakeItems(false)
                    .setCanPutItems(false)
                    .setIngredientIO(IngredientIO.OUTPUT)
                    .setXEIChance(xeiChance)
                    .setOnAddedTooltips(
                            (slot, tooltips) -> recipeWrapper.getTooltip(slotIndex + itemInputs.size(), tooltips))
                    .setBackground((IGuiTexture) null).setOverlay(overlay));
            itemOutputExists.add(true);
        }

        List<Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>>> fluidInputs = recipeWrapper.fluidInputs;
        TagOrCycleFluidHandler fluidInputsHandler = new TagOrCycleFluidHandler(fluidInputs);
        WidgetGroup fluidStackGroup = new WidgetGroup();
        for (int i = 0; i < FLUID_LOCATIONS.size(); i += 2) {
            int slotIndex = i / 2;
            if (!fluidInputs.get(slotIndex).map(Function.identity(), Function.identity()).isEmpty()) {
                var tank = new TankWidget(new CustomFluidTank(fluidInputsHandler.getFluidInTank(slotIndex)),
                        FLUID_LOCATIONS.get(i), FLUID_LOCATIONS.get(i + 1), false, false)
                        .setIngredientIO(IngredientIO.INPUT)
                        .setBackground(GuiTextures.FLUID_SLOT)
                        .setShowAmount(false);
                fluidStackGroup.addWidget(tank);
            }
        }

        this.addWidget(itemStackGroup);
        this.addWidget(fluidStackGroup);

        for (int i = 0; i < ITEM_OUTPUT_LOCATIONS.size(); i += 2) {
            // stupid hack to show all sifter slots if the first one exists
            if (itemOutputExists.get(i / 2) || (i > 28 * 2 && itemOutputExists.get(28) && hasSifter)) {
                addWidget(this.widgets.size() - 3, new ImageWidget(ITEM_OUTPUT_LOCATIONS.get(i),
                        ITEM_OUTPUT_LOCATIONS.get(i + 1), 18, 18, GuiTextures.SLOT));
            }
        }
    }
}
