package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class QuarkTechArmorItem extends ModularArmorItem {

    public QuarkTechArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public float getHeatResistance() {
        return getType() == Type.CHESTPLATE ? 0.5f : 1.0f;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String texture = super.getArmorTexture(stack, entity, slot, type);
        if (texture != null) return texture;

        return slot != EquipmentSlot.LEGS ?
                GTCEu.id("textures/armor/quark_tech_suite_1.png").toString() :
                GTCEu.id("textures/armor/quark_tech_suite_2.png").toString();
    }
}
