package com.gregtechceu.gtceu.api.item.fabric;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.Item;

/**
 * @author KilaBash
 * @date 2023/8/12
 * @implNote MaterialBlockItemImpl
 */
public class MaterialBlockItemImpl extends MaterialBlockItem {
    protected MaterialBlockItemImpl(MaterialBlock block, Properties properties) {
        super(block, properties);
    }

    public static MaterialBlockItem create(MaterialBlock block, Item.Properties properties) {
        return new MaterialBlockItemImpl(block, properties);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        if (getItemBurnTime() > 0) {
            FuelRegistry.INSTANCE.add(this, getItemBurnTime());
        }
    }
}
