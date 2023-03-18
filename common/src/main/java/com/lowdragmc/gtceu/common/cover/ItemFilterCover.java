package com.lowdragmc.gtceu.common.cover;

import com.lowdragmc.gtceu.api.capability.ICoverable;
import com.lowdragmc.gtceu.api.cover.CoverBehavior;
import com.lowdragmc.gtceu.api.cover.CoverDefinition;
import com.lowdragmc.gtceu.api.cover.IUICover;
import com.lowdragmc.gtceu.api.cover.filter.ItemFilter;
import com.lowdragmc.gtceu.api.gui.GuiTextures;
import com.lowdragmc.gtceu.api.gui.UITemplate;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

/**
 * @author KilaBash
 * @date 2023/3/13
 * @implNote ItemFilterCover
 */
public class ItemFilterCover extends CoverBehavior implements IUICover {

    protected ItemFilter itemFilter;

    public ItemFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            itemFilter = ItemFilter.loadFilter(attachItem);
        }
        return itemFilter;
    }

    @Override
    public boolean canAttach() {
        return ItemTransferHelper.getItemTransfer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide) != null;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 157, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 3, attachItem.getDescriptionId()))
                .widget(getItemFilter().openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),  GuiTextures.SLOT, 7, 75, true));
    }
}
