package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MachineModeFancyConfigurator implements IFancyUIProvider {

    protected IRecipeLogicMachine machine;

    public MachineModeFancyConfigurator(IRecipeLogicMachine machine) {
        this.machine = machine;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.machinemode.title");
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.ROBOT_ARM_LV.get());
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var group = new MachineModeConfigurator(0, 0, 140, 20 * machine.getRecipeTypes().length + 4);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        for (int i = 0; i < machine.getRecipeTypes().length; i++) {
            int finalI = i;
            group.addWidget(new ButtonWidget(2, 2 + i * 20, 136, 20, IGuiTexture.EMPTY,
                    cd -> setActiveRecipeTypeAndUpdateTickSubs(finalI)));
            group.addWidget(new ImageWidget(2, 2 + i * 20, 136, 20,
                    () -> new GuiTextureGroup(
                            ResourceBorderTexture.BUTTON_COMMON.copy()
                                    .setColor(machine.getActiveRecipeType() == finalI ? ColorPattern.CYAN.color : -1),
                            new TextTexture(machine.getRecipeTypes()[finalI].registryName.toLanguageKey()).setWidth(136)
                                    .setType(TextTexture.TextType.ROLL))));

        }
        return group;
    }

    @Override
    public List<Component> getTabTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gtceu.gui.machinemode.tab_tooltip"));
        return tooltip;
    }

    private void setActiveRecipeTypeAndUpdateTickSubs(int activeRecipeType) {
        boolean needUpdateTickSubs = !machine.keepSubscribing() && activeRecipeType != machine.getActiveRecipeType();
        machine.setActiveRecipeType(activeRecipeType);
        if (needUpdateTickSubs) {
            machine.getRecipeLogic().updateTickSubscription();
        }
    }

    public class MachineModeConfigurator extends WidgetGroup {

        public MachineModeConfigurator(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            buffer.writeVarInt(machine.getActiveRecipeType());
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            machine.setActiveRecipeType(buffer.readVarInt());
        }

        @Override
        public void detectAndSendChanges() {
            this.writeUpdateInfo(0, buf -> buf.writeVarInt(machine.getActiveRecipeType()));
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == 0) {
                machine.setActiveRecipeType(buffer.readVarInt());
            }
        }
    }
}
