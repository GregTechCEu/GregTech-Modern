package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
@Accessors(chain = true)
public class MEFancyInvConfigurator implements IFancyConfigurator {
    private final ItemStackTransfer inventory;

    @Getter
    private final Component title;

    @Getter
    @Setter
    private List<Component> tooltips = Collections.emptyList();

    public MEFancyInvConfigurator(ItemStackTransfer inventory, Component title) {
        this.inventory = inventory;
        this.title = title;
    }
    //TODO: Actual Texture - Ask Gate if we can use the ones from GE for the PatternBufferPort (tbh for all slot textures lol)
    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }

    @Override
    public Widget createConfigurator() {
        int rowSize = (int) Math.sqrt(inventory.getSlots());
        int colSize = rowSize;
        if (inventory.getSlots() == 8) {
            rowSize = 4;
            colSize = 2;
        }
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                container.addWidget(new SlotWidget(inventory, index++, 4 + x * 18, 4 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setIngredientIO(IngredientIO.INPUT));
            }
        }

        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);

        return group;
    }
}