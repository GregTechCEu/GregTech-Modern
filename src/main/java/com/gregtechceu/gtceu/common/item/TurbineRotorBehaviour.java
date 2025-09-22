package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IMaterialPartItem;
import com.gregtechceu.gtceu.api.item.component.ISubItemHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.turbineBlade;

public class TurbineRotorBehaviour implements IMaterialPartItem, ISubItemHandler {

    @Override
    public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (!material.shouldGenerateRecipesFor(turbineBlade) || !material.hasProperty(PropertyKey.INGOT)) {
                continue;
            }

            var rotorStack = new ItemStack(item);
            var behavior = TurbineRotorBehaviour.getBehaviour(rotorStack);
            if (behavior != null) {
                behavior.setPartMaterial(rotorStack, material);
                items.add(rotorStack);
            }
        }
    }

    public int getRotorPower(ItemStack stack) {
        var property = getPartMaterial(stack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : property.getPower();
    }

    public int getRotorEfficiency(ItemStack stack) {
        var property = getPartMaterial(stack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : property.getEfficiency();
    }

    @Override
    public int getPartMaxDurability(ItemStack itemStack) {
        var property = getPartMaterial(itemStack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : 800 * (int) Math.pow(property.getDurability(), 0.65);
    }

    public float getDamage(ItemStack itemStack) {
        var property = getPartMaterial(itemStack).getProperty(PropertyKey.ROTOR);
        return property == null ? -1 : property.getDamage();
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

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        IMaterialPartItem.super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents
                .add(Component.translatable("metaitem.tool.tooltip.rotor.efficiency", getRotorEfficiency(stack)));
        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.rotor.power", getRotorPower(stack)));
    }

    @Nullable
    public static TurbineRotorBehaviour getBehaviour(@NotNull ItemStack itemStack) {
        if (itemStack.getItem() instanceof IComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                if (component instanceof TurbineRotorBehaviour behaviour) {
                    return behaviour;
                }
            }
        }
        return null;
    }
}
