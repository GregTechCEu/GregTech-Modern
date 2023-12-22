package com.gregtechceu.gtceu.api.item.tool.fabric;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/26
 * @implNote GTToolItemImpl
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTToolItemImpl extends GTToolItem implements FabricItem {

    protected GTToolItemImpl(GTToolType toolType, MaterialToolTier tier, Material material, int electricTier, IGTToolDefinition definition, Properties properties) {
        super(toolType, tier, material, electricTier, definition, properties);
    }

    public static GTToolItem create(GTToolType toolType, MaterialToolTier tier, Material material, int electricTier, IGTToolDefinition definition, Properties properties) {
        return new GTToolItemImpl(toolType, tier, material, electricTier, definition, properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack itemStack) {
        if (itemStack.getMaxDamage() >= itemStack.getDamageValue()) {
            itemStack = itemStack.copy();
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            return itemStack;
        }
        return ItemStack.EMPTY;
    }
}
