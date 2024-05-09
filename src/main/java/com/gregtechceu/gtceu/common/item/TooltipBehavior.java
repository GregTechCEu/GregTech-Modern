package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.items.component.IAddInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote TooltipBehavior
 */
public class TooltipBehavior implements IAddInformation {
    private final Consumer<List<Component>> tooltips;

    /**
     * @param tooltips a consumer adding translated tooltips to the tooltip list
     */
    public TooltipBehavior(@NotNull Consumer<List<Component>> tooltips) {
        this.tooltips = tooltips;
    }
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltips.accept(tooltipComponents);
    }
}
