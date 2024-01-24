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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class CoverConfigurator implements IFancyConfigurator {
    protected final ICoverable coverable;
    protected final IItemTransfer transfer;
    protected final ConfiguratorPanel panel;
    protected SceneWidget sceneWidget;
    protected SlotWidget slotWidget;
    protected ImageWidget background;
    // runtime
    @Nullable
    protected Direction side;
    @Nullable
    protected CoverBehavior coverBehavior;

    public CoverConfigurator(ICoverable coverable, IItemTransfer transfer, ConfiguratorPanel panel) {
        this.coverable = coverable;
        this.transfer = transfer;
        this.panel = panel;
    }

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
                coverConfigurator.setSelfPosition(new Position(4, 80 - 4));
                group.addWidgetAnima(coverConfigurator, new Transform()
                    .offset(-coverConfigurator.getSize().width / 2, -coverConfigurator.getSize().height / 2)
                    .scale(0)
                    .duration(300));
                sceneWidget.animation(new Animation()
                    .duration(300)
                    .size(new Size(Math.max(120, coverConfigurator.getSize().width + 8) - 8, 80 - 8)));
                group.setSize(new Size(Math.max(120, coverConfigurator.getSize().width + 8), Math.max(80, 80 + coverConfigurator.getSize().height)));
                background.animation(new Animation()
                    .duration(300)
                    .size(group.getSize()));
            }
        }
        return group;
    }
}
