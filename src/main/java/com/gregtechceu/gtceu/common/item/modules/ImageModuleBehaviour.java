package com.gregtechceu.gtceu.common.item.modules;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.client.renderer.monitor.MonitorImageRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.item.ItemStack;

public class ImageModuleBehaviour implements IMonitorModuleItem {

    @Override
    public IMonitorRenderer getRenderer(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        return new MonitorImageRenderer(getUrl(stack));
    }

    @Override
    public Widget createUIWidget(ItemStack stack, CentralMonitorMachine machine, MonitorGroup group) {
        WidgetGroup builder = new WidgetGroup();
        TextFieldWidget textField = new TextFieldWidget(0, 0, 100, 10, null, null);
        textField.setCurrentString(getUrl(stack));

        ButtonWidget saveButton = new ButtonWidget(-40, 22, 20, 20, click -> {
            if (!click.isRemote) return;

            setUrl(stack, textField.getCurrentString());
            GTNetwork.sendToServer(new SCPacketMonitorGroupNBTChange(stack, group, machine));
        });
        saveButton.setButtonTexture(GuiTextures.BUTTON_CHECK);
        builder.addWidget(textField);
        builder.addWidget(saveButton);
        return builder;
    }

    @Override
    public String getType() {
        return "image";
    }

    public String getUrl(ItemStack stack) {
        return stack.getOrCreateTag().getString("url");
    }

    public void setUrl(ItemStack stack, String url) {
        stack.getOrCreateTag().putString("url", url);
    }
}
