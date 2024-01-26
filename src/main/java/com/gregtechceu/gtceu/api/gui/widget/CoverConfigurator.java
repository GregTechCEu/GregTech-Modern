package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.gui.animation.Animation;
import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.AllArgsConstructor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@AllArgsConstructor
public class CoverConfigurator implements IFancyConfigurator {
    protected final ICoverable coverable;
    protected final IItemTransfer transfer;
    protected final ConfiguratorPanel panel;
    protected final SceneWidget sceneWidget;
    protected final SlotWidget slotWidget;
    // runtime
    @Nullable
    protected final Direction side;
    @Nullable
    protected final CoverBehavior coverBehavior;
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.cover_setting.title");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(GTItems.ITEM_FILTER.get());
    }

    @Override
    public Widget createConfigurator() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        if (side != null) {
            if (coverable.getCoverAtSide(side) instanceof IUICover iuiCover) {
                Widget coverConfigurator = iuiCover.createUIWidget();
                coverConfigurator.setBackground(GuiTextures.BACKGROUND);
                coverConfigurator.setSelfPosition(new Position(4, -4));
                group.addWidgetAnima(coverConfigurator, new Transform()
                    .offset(-coverConfigurator.getSize().width / 2, -coverConfigurator.getSize().height / 2)
                    .scale(0)
                    .duration(300));
                group.setSize(new Size(Math.max(120, coverConfigurator.getSize().width + 8), Math.max(80, 80 + coverConfigurator.getSize().height)));
            }
        }
        return group;
    }
}
