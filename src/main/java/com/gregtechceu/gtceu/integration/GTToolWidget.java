package com.gregtechceu.gtceu.integration;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class GTToolWidget extends WidgetGroup {

    protected final static ImmutableList<Integer> SLOT_LOCS = ImmutableList.of(
            3, 3, // pickaxe
            21, 3, // shovel
            39, 3, // axe
            57, 3, // sword
            75, 3, // hoe
            57, 39, // butchery knife
            39, 57, // buzz saw
            39, 39, // chainsaw
            147, 21, // crowbar
            3, 75, // drill lv
            21, 75, // drill mv
            39, 75, // drill hv
            57, 75, // drill ev
            75, 75, // drill iv
            129, 21, // file
            129, 3, // hard hammer
            57, 21, // knife
            147, 3, // mining hammer
            3, 21, // mortar
            3, 57, // plunger
            39, 21, // saw
            129, 39, // screwdriver
            147, 39, // screwdriver lv
            21, 57, // scythe
            39, 93, // shears
            3, 93, // soft mallet
            21, 21, // spade
            129, 57, // wire cutter
            147, 57, // wire cutter lv
            129, 75, // wire cutter hv
            147, 75, // wire cutter iv
            129, 93, // wrench
            147, 93, // wrench lv
            129, 111, // wrench hv
            147, 111 // wrench iv
    );

    public GTToolWidget(Material material) {
        super(0, 0, 176, 166);
        setClientSideWidget();
        setRecipe(new GTTool(material));
    }

    public void setRecipe(GTTool recipeWrapper) {
        WidgetGroup itemGroup = new WidgetGroup();

        NonNullList<ItemStack> items = recipeWrapper.items;
        ItemStackTransfer itemHandler = new ItemStackTransfer(items);
        for(int i = 0; i < SLOT_LOCS.size(); i+= 2) {
            final int finalI = i;
            itemGroup.addWidget(new SlotWidget(itemHandler, i / 2, SLOT_LOCS.get(i), SLOT_LOCS.get(i + 1)).setCanTakeItems(false).setCanPutItems(false)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setOnAddedTooltips((slot, tooltip) -> recipeWrapper.getTooltip(finalI / 2, tooltip))
                    .setBackground(GuiTextures.SLOT));
        }

        this.addWidget(itemGroup);


    }
}
