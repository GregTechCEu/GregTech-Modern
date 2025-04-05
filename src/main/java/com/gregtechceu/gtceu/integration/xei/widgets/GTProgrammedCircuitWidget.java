package com.gregtechceu.gtceu.integration.xei.widgets;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraftforge.items.ItemStackHandler;

public class GTProgrammedCircuitWidget extends WidgetGroup {

    public GTProgrammedCircuitWidget() {
        super(0, 0, 150, 80);
        setClientSideWidget();
        setRecipe();
    }

    public void setRecipe() {
        addWidget(new ImageWidget(39, 0, 36, 36, GuiTextures.SLOT));

        ItemStackHandler handler = new CustomItemStackHandler(32);
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 8; i++) {
                handler.setStackInSlot((i + j * 8), IntCircuitBehaviour.stack(1 + (i + j * 8)));
                addWidget(new SlotWidget(handler, (i + j * 8), 3 + 18 * i, 18 * j, false, false)
                        .setIngredientIO((i + j * 8 == 31 ? IngredientIO.OUTPUT : IngredientIO.BOTH)));
            }
        }
    }
}
