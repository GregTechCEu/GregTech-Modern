package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/** Common contract for item toolboxes used by GT recipes and maintenance. */
public interface ICustomToolIngredient {

    boolean containsTool(ItemStack toolbox, GTToolType toolType);

    boolean damageTool(ItemStack toolbox, GTToolType toolType, @Nullable LivingEntity user, int damage);

    void markLastUsedTool(ItemStack stack, GTToolType toolType);
}
