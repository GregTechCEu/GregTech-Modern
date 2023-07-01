package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote IDisplayUIMachine
 */
public interface IDisplayUIMachine extends IUIMachine, IMultiController {
    default void addDisplayText(List<Component> textList) {
        if (!isFormed()) {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip").withStyle(ChatFormatting.GRAY);
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        } else {
            if (this instanceof IMaintenance maintenance) {
                if (maintenance.hasMaintenanceMechanics() && ConfigHolder.INSTANCE.machines.enableMaintenance) {
                    addMaintenanceText(maintenance, textList);
                }
            }
        }
    }

    default void handleDisplayClick(String componentData, ClickData clickData) {
    }

    default IGuiTexture getScreenTexture() {
        return GuiTextures.DISPLAY;
    }

    @Override
    default ModularUI createUI(Player entityPlayer) {
        var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
        screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(154)
                .clickHandler(this::handleDisplayClick));
        return new ModularUI(176, 216, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(screen)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 134, true));
    }

    default void addMaintenanceText(IMaintenance maintenance, List<Component> textList) {
        if (!maintenance.hasMaintenanceProblems()) {
            textList.add(Component.translatable("gtceu.multiblock.universal.no_problems")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
            );
        } else {

            MutableComponent hoverEventTranslation = Component.translatable("gtceu.multiblock.universal.has_problems_header")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));

            if ((maintenance.getMaintenanceProblems() & 1) == 0)
                hoverEventTranslation.append(Component.translatable("gtceu.multiblock.universal.problem.wrench", "\n"));

            if (((maintenance.getMaintenanceProblems() >> 1) & 1) == 0)
                hoverEventTranslation.append(Component.translatable("gtceu.multiblock.universal.problem.screwdriver", "\n"));

            if (((maintenance.getMaintenanceProblems() >> 2) & 1) == 0)
                hoverEventTranslation.append(Component.translatable("gtceu.multiblock.universal.problem.soft_mallet", "\n"));

            if (((maintenance.getMaintenanceProblems() >> 3) & 1) == 0)
                hoverEventTranslation.append(Component.translatable("gtceu.multiblock.universal.problem.hard_hammer", "\n"));

            if (((maintenance.getMaintenanceProblems() >> 4) & 1) == 0)
                hoverEventTranslation.append(Component.translatable("gtceu.multiblock.universal.problem.wire_cutter", "\n"));

            if (((maintenance.getMaintenanceProblems() >> 5) & 1) == 0)
                hoverEventTranslation.append(Component.translatable("gtceu.multiblock.universal.problem.crowbar", "\n"));

            MutableComponent textTranslation = Component.translatable("gtceu.multiblock.universal.has_problems");

            textList.add(textTranslation.setStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventTranslation))));
        }
    }

}
