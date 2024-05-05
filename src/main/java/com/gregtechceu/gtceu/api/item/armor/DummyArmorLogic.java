package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

class DummyArmorLogic implements IArmorLogic {
    @Override
    public ArmorItem.Type getArmorType() {
        return ArmorItem.Type.HELMET;
    }

    @Override
    public boolean isValidArmor(ItemStack itemStack, Entity entity, EquipmentSlot equipmentSlot) {
        return false;
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, ArmorItem.Type slot, String type) {
        return new ResourceLocation("minecraft", "textures/models/armor/diamond_layer_0.png");
    }
}