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
    public boolean protectsAgainstFalls() {
        return getType() == Type.BOOTS;
    }

    @Override
    public float getHeatResistance() {
        return getType() == Type.CHESTPLATE ? 0.5f : 1.0f;
    }

    @Override
    public boolean protectsAgainstFire() {
        return getType() == Type.CHESTPLATE;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        var texture = super.getArmorTexture(stack, entity, slot, type);
        if (texture != null) return texture;

        String armorTexture = "quark_tech_suite";
        return slot != EquipmentSlot.LEGS ?
                GTCEu.id(String.format("textures/armor/%s_1.png", armorTexture)).toString() :
                GTCEu.id(String.format("textures/armor/%s_2.png", armorTexture)).toString();
    }
}
