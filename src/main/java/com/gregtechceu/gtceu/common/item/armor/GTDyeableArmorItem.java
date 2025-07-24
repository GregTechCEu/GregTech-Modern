package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.world.item.*;

public class GTDyeableArmorItem extends GTArmorItem implements DyeableLeatherItem {

    public GTDyeableArmorItem(ArmorMaterial armorMaterial, ArmorItem.Type type, Item.Properties properties,
                              Material material, ArmorProperty armorProperty) {
        super(armorMaterial, type, properties, material, armorProperty);
        if (Platform.isClient()) {
            ArmorItemRenderer.create(this, type);
        }
    }
}
