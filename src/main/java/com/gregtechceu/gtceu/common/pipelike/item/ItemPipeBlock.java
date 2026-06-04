package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeProperties> {

    public ItemPipeBlock(Properties properties, ItemPipeVariant itemPipeType, Material material) {
        super(properties, itemPipeType, GTPipeNetworks.ITEM, material);
    }

    @Override
    public ItemPipeProperties createRawData() {
        return material.getProperty(PropertyKey.ITEM_PIPE);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ItemPipeProperties properties = createProperties();

        if (properties.getTransferRate() % 1 != 0) {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate",
                    (int) ((properties.getTransferRate() * 64) + 0.5)));
        } else {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks",
                    (int) properties.getTransferRate()));
        }

        tooltip.add(Component.translatable("gtceu.item_pipe.priority", properties.getPriority()));
    }
}
