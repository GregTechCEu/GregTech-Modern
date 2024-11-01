package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/6/30
 * @implNote CircuitFancyConfigurator
 */
public class CircuitFancyConfigurator implements IFancyConfigurator {

    final ItemStackHandler circuitSlot;

    public CircuitFancyConfigurator(ItemStackHandler circuitSlot) {
        this.circuitSlot = circuitSlot;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.circuit.title");
    }

    @Override
    public IGuiTexture getIcon() {
        if (IntCircuitBehaviour.isIntegratedCircuit(circuitSlot.getStackInSlot(0))) {
            return new ItemStackTexture(circuitSlot.getStackInSlot(0));
        }
        return new GuiTextureGroup(new ItemStackTexture(IntCircuitBehaviour.stack(0)),
                new ItemStackTexture(Items.BARRIER));
    }

    @Override
    public Widget createConfigurator() {
        var group = new WidgetGroup(0, 0, 174, 132);
        group.addWidget(new LabelWidget(9, 8, "Programmed Circuit Configuration"));
        group.addWidget(new SlotWidget(circuitSlot, 0, (group.getSize().width - 18) / 2, 20,
                !ConfigHolder.INSTANCE.machines.ghostCircuit, !ConfigHolder.INSTANCE.machines.ghostCircuit)
                .setBackground(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY)));
        if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
            group.addWidget(new ButtonWidget((group.getSize().width - 18) / 2, 20, 18, 18, IGuiTexture.EMPTY,
                    clickData -> {
                        if (!clickData.isRemote) {
                            circuitSlot.setStackInSlot(0, ItemStack.EMPTY);
                        }
                    }));
        }
        int idx = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                group.addWidget(new ButtonWidget(5 + (18 * y), 48 + (18 * x), 18, 18,
                        new GuiTextureGroup(GuiTextures.SLOT,
                                new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                        clickData -> {
                            if (!clickData.isRemote) {
                                ItemStack stack = circuitSlot.getStackInSlot(0).copy();
                                if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                                    IntCircuitBehaviour.setCircuitConfiguration(stack, finalIdx);
                                    circuitSlot.setStackInSlot(0, stack);
                                } else if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                                    circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(finalIdx));
                                }
                            }
                        }));
                idx++;
            }
        }
        for (int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            group.addWidget(new ButtonWidget(5 + (18 * x), 102, 18, 18,
                    new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(finalIdx)).scale(16f / 18)),
                    clickData -> {
                        if (!clickData.isRemote) {
                            ItemStack stack = circuitSlot.getStackInSlot(0).copy();
                            if (IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                                IntCircuitBehaviour.setCircuitConfiguration(stack, finalIdx);
                                circuitSlot.setStackInSlot(0, stack);
                            } else if (ConfigHolder.INSTANCE.machines.ghostCircuit) {
                                circuitSlot.setStackInSlot(0, IntCircuitBehaviour.stack(finalIdx));
                            }
                        }
                    }));
        }
        return group;
    }

    @Override
    public List<Component> getTooltips() {
        var list = new ArrayList<>(IFancyConfigurator.super.getTooltips());
        list.addAll(Arrays.stream(
                LangHandler.getMultiLang("gtceu.gui.configurator_slot.tooltip").toArray(new MutableComponent[0]))
                .toList());
        return list;
    }
}
