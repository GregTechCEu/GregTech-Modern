package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;

import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;

public class GTDyeableArmorItem extends GTArmorItem implements DyeableLeatherItem {

    public GTDyeableArmorItem(ArmorMaterial armorMaterial, ArmorType type, Item.Properties properties,
                              Material material, ArmorProperty armorProperty) {
        super(armorMaterial, type, properties, material, armorProperty);
    }
}
