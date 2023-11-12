package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Rundas/Screret
 * @implNote MachineModeFancyConfigurator
 */
public class MachineModeFancyConfigurator implements IFancyConfigurator {
    protected IRecipeLogicMachine machine;

    public MachineModeFancyConfigurator(IRecipeLogicMachine machine) {
        this.machine = machine;
    }

    @Override
    public String getTitle() {
        return "gtceu.gui.machinemode.title";
    }

    @Override
    public IGuiTexture getIcon() {
        return new ResourceTexture("gtceu:textures/item/lv_robot_arm.png");
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
    public void detectAndSendChange(BiConsumer<Integer, Consumer<FriendlyByteBuf>> sender) {
        sender.accept(0, buf -> buf.writeVarInt(machine.getActiveRecipeType()));
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            machine.setActiveRecipeType(buffer.readVarInt());
        }
    }

    @Override
    public Widget createConfigurator() {
        var group = new WidgetGroup(0, 0, 140, 20 * machine.getRecipeTypes().length + 4);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        for (int i = 0; i < machine.getRecipeTypes().length; i++) {
            int finalI = i;
            group.addWidget(new ButtonWidget(2, 2 + i * 20, 136, 20, IGuiTexture.EMPTY, cd -> machine.setActiveRecipeType(finalI)));
            group.addWidget(new ImageWidget(2, 2 + i * 20, 136, 20,
                    () -> new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON.copy().setColor(machine.getActiveRecipeType() == finalI ? ColorPattern.CYAN.color : -1),
                            new TextTexture(machine.getRecipeTypes()[finalI].registryName.toLanguageKey()).setWidth(136).setType(TextTexture.TextType.ROLL))));

        }
        return group;
    }

    @Override
    public List<Component> getTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Change active Machine Mode"));
        return tooltip;
    }
}
