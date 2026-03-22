package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;

import net.minecraft.world.item.*;

public class GTDyeableArmorItem extends GTArmorItem implements DyeableLeatherItem {

    public GTDyeableArmorItem(ArmorMaterial armorMaterial, ArmorItem.Type type, Item.Properties properties,
                              Material material, ArmorProperty armorProperty) {
        super(armorMaterial, type, properties, material, armorProperty);
        if (GTCEu.isClientSide()) {
            ArmorItemRenderer.create(this, type);
        }
    }
}
