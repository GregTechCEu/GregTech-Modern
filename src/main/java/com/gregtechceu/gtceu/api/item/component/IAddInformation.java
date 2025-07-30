package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IAddInformation extends IItemComponent {

    void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                         TooltipFlag isAdvanced);
}
