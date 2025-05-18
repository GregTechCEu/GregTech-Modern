package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TooltipBehavior implements IAddInformation {

    private final Consumer<List<Component>> tooltips;

    /**
     * @param tooltips a consumer adding translated tooltips to the tooltip list
     */
    public TooltipBehavior(@NotNull Consumer<List<Component>> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltips.accept(tooltipComponents);
    }
}
