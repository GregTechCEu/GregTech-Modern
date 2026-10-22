package com.gregtechceu.gtceu.api.mui.modular_item;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.*;

public class ModularArmorUIFactory extends AbstractUIFactory<ModularArmorUIFactory.ArmorGuiData> {

    public static final ModularArmorUIFactory INSTANCE = new ModularArmorUIFactory();
    static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    protected ModularArmorUIFactory() {
        super(GTCEu.id("modular_armor"));
    }

    public void open(Player player) {
        GuiManager.open(this, new ArmorGuiData(player), verifyServerSide(player));
    }

    @Override
    public IUIHolder<ArmorGuiData> getGuiHolder(ArmorGuiData data) {
        return new ModularArmorManagerUI();
    }

    @Override
    public boolean canInteractWith(Player player, ArmorGuiData data) {
        if (player != data.getPlayer() || !player.isAlive()) return false;
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            if (player.getItemBySlot(ARMOR_SLOTS[i]) != data.armor[i]) return false;
        }
        return true;
    }

    @Override
    public void writeGuiData(ArmorGuiData data, RegistryFriendlyByteBuf buffer) {
        for (ItemStack stack : data.armor) ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
    }

    @Override
    public ArmorGuiData readGuiData(Player player, RegistryFriendlyByteBuf buffer) {
        ArmorGuiData data = new ArmorGuiData(player);
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            data.armor[i] = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        }
        return data;
    }

    public static class ArmorGuiData extends GuiData {

        // Server references remain attached to the equipped items, clients receive display copies.
        final ItemStack[] armor = new ItemStack[ARMOR_SLOTS.length];

        ArmorGuiData(Player player) {
            super(player);
            for (int i = 0; i < ARMOR_SLOTS.length; i++) {
                armor[i] = player.getItemBySlot(ARMOR_SLOTS[i]);
            }
        }
    }
}
