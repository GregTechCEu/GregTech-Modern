package com.gregtechceu.gtceu.common.item;


import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IMaterialPartItem;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.turbineBlade;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote TurbineRotorBehaviour
 */
public class TurbineRotorBehaviour implements IMaterialPartItem, ISubItemHandler {

    @Override
    public void fillItemCategory(ComponentItem item, CreativeModeTab category, NonNullList<ItemStack> items) {
        turbineBlade.executeHandler(PropertyKey.INGOT, (tagPrefix, material, property) -> {
            var rotorStack = new ItemStack(item);
            var behavior = TurbineRotorBehaviour.getBehaviour(rotorStack);
            if (behavior != null) {
                behavior.setPartMaterial(rotorStack, material);
                items.add(rotorStack);
            }
        });
    }

    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        var property = getPartMaterial(itemStack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : 800 * (int) Math.pow(property.getDurability(), 0.65);
    }

    public int getRotorEfficiency(ItemStack stack) {
        var property = getPartMaterial(stack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : ((int) ((60 + property.getSpeed() * 8)) / 5 * 5);
    }

    public int getRotorDurabilityPercent(ItemStack itemStack) {
        return 100 - 100 * getPartDamage(itemStack) / getPartMaxDurability(itemStack);
    }

    public void applyRotorDamage(ItemStack itemStack, int damageApplied) {
        int rotorDurability = getPartMaxDurability(itemStack);
        int resultDamage = getPartDamage(itemStack) + damageApplied;
        if (resultDamage >= rotorDurability) {
            itemStack.shrink(1);
        } else {
            setPartDamage(itemStack, resultDamage);
        }
    }

    public int getRotorPower(ItemStack stack) {
        var property = getPartMaterial(stack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : (int) (40 + property.getDamage() * 30);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        IMaterialPartItem.super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.rotor.efficiency", getRotorEfficiency(stack)));
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.rotor.power", getRotorPower(stack)));
    }

    @Nullable
    public static TurbineRotorBehaviour getBehaviour(@Nonnull ItemStack itemStack) {
        if (itemStack.getItem() instanceof ComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                if (component instanceof TurbineRotorBehaviour behaviour) {
                    return behaviour;
                }
            }
        }
        return null;
    }
}
