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
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            String type) {
        var texture = super.getArmorTexture(stack, entity, slot, type);
        if (texture != null) return texture;

        String armorTexture = "nano_muscule_suite";
        return slot != EquipmentSlot.LEGS ?
                GTCEu.id(String.format("textures/armor/%s_1.png", armorTexture)).toString() :
                GTCEu.id(String.format("textures/armor/%s_2.png", armorTexture)).toString();
    }
}
