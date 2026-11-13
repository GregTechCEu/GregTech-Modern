package com.gregtechceu.gtceu.utils.lang;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FunctionalInterface
public interface CustomTooltipBuilder {

    void addTooltips(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltips);
}
