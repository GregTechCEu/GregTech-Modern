package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class TooltipBehavior implements IAddInformation {

    private final Consumer<List<Component>> tooltipConsumer;

    /**
     * @param tooltipConsumer a consumer adding translated tooltips to the tooltip list
     */
    public TooltipBehavior(Consumer<List<Component>> tooltipConsumer) {
        this.tooltipConsumer = tooltipConsumer;
    }

    public TooltipBehavior(List<Component> components) {
        this(l -> l.addAll(components));
    }

    public TooltipBehavior(Component... components) {
        this(List.of(components));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipConsumer.accept(tooltipComponents);
    }
}
