package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/8/12
 * @implNote TagPrefixItemImpl
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TagPrefixItemImpl extends TagPrefixItem {
    protected TagPrefixItemImpl(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties, tagPrefix, material);
    }

    public static TagPrefixItem create(Item.Properties properties, TagPrefix tagPrefix, Material material) {
        return new TagPrefixItemImpl(properties, tagPrefix, material);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        if (getItemBurnTime() > 0) {
            FuelRegistry.INSTANCE.add(this, getItemBurnTime());
        }
    }
}
