package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMaterialPartItem extends IItemComponent, IDurabilityBar, IAddInformation, ICustomDescriptionId {

    int getPartMaxDurability(ItemStack itemStack);

    default Material getPartMaterial(ItemStack itemStack) {
        var material = itemStack.getOrDefault(GTDataComponents.ITEM_MATERIAL, GTMaterials.NULL);
        var defaultMaterial = GTMaterials.Aluminium;
        if (material.isNull() || !material.hasProperty(PropertyKey.INGOT)) {
            return defaultMaterial;
        }
        return material;
    }

    default void setPartMaterial(ItemStack itemStack, @NotNull Material material) {
        if (!material.hasProperty(PropertyKey.INGOT))
            throw new IllegalArgumentException("Part material must have an Ingot!");
        itemStack.set(GTDataComponents.ITEM_MATERIAL, material);
        // update other components after setting part stats
        itemStack.set(DataComponents.MAX_DAMAGE, getPartMaxDurability(itemStack));
        itemStack.setDamageValue(0);
    }

    default int getPartDamage(ItemStack itemStack) {
        return itemStack.getDamageValue();
    }

    default void setPartDamage(ItemStack itemStack, int damage) {
        itemStack.setDamageValue(damage);
    }

    @Override
    @Nullable
    default Component getItemName(ItemStack stack) {
        var material = getPartMaterial(stack);
        return Component.translatable(stack.getDescriptionId(), material.getLocalizedName());
    }

    @Override
    default void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                 TooltipFlag isAdvanced) {
        var material = getPartMaterial(stack);
        var maxDurability = getPartMaxDurability(stack);
        var damage = getPartDamage(stack);
        tooltipComponents
                .add(Component.translatable("metaitem.tool.tooltip.durability", maxDurability - damage, maxDurability));
        tooltipComponents
                .add(Component.translatable("metaitem.tool.tooltip.primary_material", material.getLocalizedName()));
    }

    @OnlyIn(Dist.CLIENT)
    static ItemColor getItemStackColor() {
        return (itemStack, i) -> {
            if (itemStack.getItem() instanceof IComponentItem componentItem) {
                for (IItemComponent component : componentItem.getComponents()) {
                    if (component instanceof IMaterialPartItem materialPartItem) {
                        return materialPartItem.getPartMaterial(itemStack).getMaterialARGB();
                    }
                }
            }
            return -1;
        };
    }

    @Override
    default float getDurabilityForDisplay(ItemStack itemStack) {
        var maxDurability = getPartMaxDurability(itemStack);
        return (maxDurability - getPartDamage(itemStack)) * 1f / maxDurability;
    }
}
