package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import com.gregtechceu.gtceu.utils.lang.GTCustomTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TooltipBehavior implements IAddInformation {

    private final GTCustomTooltip tooltip;

    public TooltipBehavior(GTCustomTooltip tooltip) {
        this.tooltip = tooltip;
    }

    public TooltipBehavior(Consumer<GTCustomTooltip> tooltipConsumer) {
        this.tooltip = new GTCustomTooltip();
        tooltipConsumer.accept(this.tooltip);
    }

    public TooltipBehavior(Component... components) {
        this(t -> t.tooltips(components));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltip.addTooltips(stack, level, tooltipComponents, isAdvanced);
    }
}
