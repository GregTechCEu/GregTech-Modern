package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ItemStack;

class DummyArmorLogic implements IArmorLogic {
    @Override
    public ArmorItem.Type getEquipmentSlot() {
        return ArmorItem.Type.HELMET;
    }

    @Override
    public boolean isValidArmor(ItemStack itemStack, Entity entity, Type equipmentSlot) {
        return false;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, Type slot, String type) {
        return "minecraft:textures/models/armor/diamond_layer_0.png";
    }
}