package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class NanoMuscleArmorItem extends ModularArmorItem {

    public NanoMuscleArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public float getHeatResistance() {
        return getType() == Type.CHESTPLATE ? 0.75f : 1.0f;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String texture = super.getArmorTexture(stack, entity, slot, type);
        if (texture != null) return texture;

        return slot != EquipmentSlot.LEGS ?
                GTCEu.id("textures/armor/nano_muscle_suite_1.png").toString() :
                GTCEu.id("textures/armor/nano_muscle_suite_2.png").toString();
    }
}
