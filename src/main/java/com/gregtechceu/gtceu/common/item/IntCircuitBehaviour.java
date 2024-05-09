package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.items.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.items.component.IAddInformation;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTDataComponents;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote IntCircuitBehaviour
 */
public class IntCircuitBehaviour implements IItemUIFactory, IAddInformation {
    public static final int CIRCUIT_MAX = 32;

    public static ItemStack stack(int configuration) {
        var stack = GTItems.INTEGRATED_CIRCUIT.asStack();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(HeldItemUIFactory.HeldItemHolder holder, int configuration) {
        setCircuitConfiguration(holder.getHeld(), configuration);
        holder.markAsDirty();
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        itemStack.set(GTDataComponents.CIRCUIT_CONFIG, configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        return itemStack.getOrDefault(GTDataComponents.CIRCUIT_CONFIG, 0);
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        return GTItems.INTEGRATED_CIRCUIT.isIn(itemStack);
    }

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(HeldItemUIFactory.HeldItemHolder holder, int amount) {
        adjustConfiguration(holder.getHeld(), amount);
        holder.markAsDirty();
    }

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(ItemStack stack, int amount) {
        int configuration = getCircuitConfiguration(stack);
        configuration += amount;
        configuration = Mth.clamp(configuration, 0, CIRCUIT_MAX);
        setCircuitConfiguration(stack, configuration);
    }


    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        int configuration = getCircuitConfiguration(stack);
        tooltipComponents.add(Component.translatable("metaitem.int_circuit.configuration", configuration));
    }

    
    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        LabelWidget label = new LabelWidget(9, 8, "Programmed Circuit Configuration");
        label.setDropShadow(false);
        label.setTextColor(0x404040);
        var modular = new ModularUI(184, 132, holder, entityPlayer)
                .widget(label);
        SlotWidget slotwidget = new SlotWidget(new CustomItemStackHandler(stack(getCircuitConfiguration(holder.getHeld()))), 0, 82, 20, false, false);
        slotwidget.setBackground(GuiTextures.SLOT);
        modular.widget(slotwidget);
        int idx = 0;
        for(int x = 0; x <= 2; x++) {
            for(int y = 0; y <= 8; y++) {
                int finalIdx = idx;
                modular.widget(new ButtonWidget(10 + (18 * y), 48 + (18 * x), 18, 18, new GuiTextureGroup(GuiTextures.SLOT, new ItemStackTexture(stack(finalIdx)).scale(16f / 18)),
                        data -> { setCircuitConfiguration(holder, finalIdx); slotwidget.setHandlerSlot(new CustomItemStackHandler(stack(finalIdx)), 0); }));
                idx++;
            }
        }
        for(int x = 0; x <= 5; x++) {
            int finalIdx = x + 27;
            modular.widget(new ButtonWidget(10 + (18 * x), 102, 18, 18, new GuiTextureGroup(GuiTextures.SLOT, new ItemStackTexture(stack(finalIdx)).scale(16f / 18)),
                    data -> { setCircuitConfiguration(holder, finalIdx); slotwidget.setHandlerSlot(new CustomItemStackHandler(stack(finalIdx)), 0); }));
        }
        modular.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return modular;
    }
}
