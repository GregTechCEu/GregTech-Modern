package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

import org.jetbrains.annotations.NotNull;

public class DummyArmorLogic implements IArmorLogic {

    @Override
    public ArmorType getArmorType() {
        return ArmorType.HELMET;
    }

    @Override
    public int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot) {
        return 0;
    }

    @Override
    public boolean isValidArmor(ItemStack itemStack, Entity entity, EquipmentSlot equipmentSlot) {
        return false;
    }

    @Override
    public Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                      EquipmentClientInfo.Layer layer) {
        return Identifier.withDefaultNamespace("textures/armor/diamond_layer_0.png");
    }
}
