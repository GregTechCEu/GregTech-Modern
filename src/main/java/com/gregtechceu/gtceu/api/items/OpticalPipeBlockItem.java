package com.gregtechceu.gtceu.api.items;

import com.gregtechceu.gtceu.common.block.OpticalPipeBlock;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class OpticalPipeBlockItem extends PipeBlockItem implements IItemRendererProvider {

    public OpticalPipeBlockItem(OpticalPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltip, isAdvanced);
        tooltip.add(Component.translatable("block.gtceu.normal_optical_pipe.tooltip"));

        if (GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("gtceu.tool_action.wire_cutter.connect"));
        } else {
            tooltip.add(Component.translatable("gtceu.tool_action.show_tooltips"));
        }
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }
}