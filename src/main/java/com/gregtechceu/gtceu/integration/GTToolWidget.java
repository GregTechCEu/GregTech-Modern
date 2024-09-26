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
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GTToolWidget extends WidgetGroup {

    protected static Map<Integer, Pair<Integer, Integer>> SLOT_LOCS = new HashMap<>();

    static {
        SLOT_LOCS.put( 1, guiPairs(0,0)); // pickaxe
        SLOT_LOCS.put( 2, guiPairs(1,0)); // shovel
        SLOT_LOCS.put( 3, guiPairs(2,0)); // axe
        SLOT_LOCS.put( 4, guiPairs(3,0)); // sword
        SLOT_LOCS.put( 5, guiPairs(4,0)); // hoe
        SLOT_LOCS.put( 6, guiPairs(3,2)); // butchery knife
        SLOT_LOCS.put( 7, guiPairs(2,3)); // buzz saw
        SLOT_LOCS.put( 8, guiPairs(8,1)); // chainsaw
        SLOT_LOCS.put( 9, guiPairs(7,5)); // crowbar
        SLOT_LOCS.put(10, guiPairs(0,3)); // drill lv
        //SLOT_LOCS.put(11, guiPairs(3,2)); // drill mv
        //SLOT_LOCS.put(12, guiPairs(4,2)); // drill hv
        //SLOT_LOCS.put(13, guiPairs(5,2)); // drill ev
        //SLOT_LOCS.put(14, guiPairs(6,2)); // drill iv
        SLOT_LOCS.put(15, guiPairs(7,2)); // file
        SLOT_LOCS.put(16, guiPairs(7,0)); // hard hammer
        SLOT_LOCS.put(17, guiPairs(3,1)); // knife
        SLOT_LOCS.put(18, guiPairs(0,1)); // mining hammer
        SLOT_LOCS.put(19, guiPairs(2,2)); // mortar
        SLOT_LOCS.put(20, guiPairs(3,2)); // plunger
        SLOT_LOCS.put(21, guiPairs(7,1)); // saw
        SLOT_LOCS.put(22, guiPairs(7,3)); // screwdriver
        SLOT_LOCS.put(23, guiPairs(8,3)); // screwdriver lv
        SLOT_LOCS.put(24, guiPairs(4,1)); // scythe
        SLOT_LOCS.put(25, guiPairs(0,3)); // shears
        SLOT_LOCS.put(26, guiPairs(1,3)); // soft mallet
        SLOT_LOCS.put(27, guiPairs(1,1)); // spade
        SLOT_LOCS.put(28, guiPairs(7,4)); // wire cutter
        SLOT_LOCS.put(29, guiPairs(8,4)); // wire cutter lv
        //SLOT_LOCS.put(30, guiPairs(6,4)); // wire cutter hv
        //SLOT_LOCS.put(31, guiPairs(7,4)); // wire cutter iv
        SLOT_LOCS.put(32, guiPairs(7,6)); // wrench
        SLOT_LOCS.put(33, guiPairs(8,6)); // wrench lv
        //SLOT_LOCS.put(34, guiPairs(2,5)); // wrench hv
        //SLOT_LOCS.put(35, guiPairs(3,5)); // wrench iv
    }

    private static Pair<Integer, Integer> guiPairs(int x, int y) {
        return Pair.of(x * 18 + 2, y * 18 + 2);
    }

    public GTToolWidget(Material material) {
        super(0, 0, 176, 166);
        setClientSideWidget();
        setRecipe(new GTTool(material));
    }

    public void setRecipe(GTTool recipeWrapper) {
        WidgetGroup itemGroup = new WidgetGroup();

        NonNullList<ItemStack> items = recipeWrapper.items;
        ItemStackTransfer itemHandler = new ItemStackTransfer(items);
        for(int i = 1; i < 81; i++) {
            if(!SLOT_LOCS.containsKey(i)) continue;
            if(items.get(i) == ItemStack.EMPTY) continue;
            int finalI = i;
            itemGroup.addWidget(new SlotWidget(itemHandler, i, SLOT_LOCS.get(i).left(), SLOT_LOCS.get(i).right()).setCanTakeItems(false).setCanPutItems(false)
                    .setIngredientIO(IngredientIO.INPUT)
                    .setOnAddedTooltips((slot, tooltip) -> recipeWrapper.getTooltip(finalI, tooltip))
                    .setBackground(GuiTextures.SLOT));
        }

        this.addWidget(itemGroup);


    }
}
