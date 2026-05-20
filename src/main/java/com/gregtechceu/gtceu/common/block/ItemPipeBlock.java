package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.PipeNetworkType;
import com.gregtechceu.gtceu.api.pipenet.property.FloatSegmentProperty;
import com.gregtechceu.gtceu.api.pipenet.property.IntSegmentProperty;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import com.gregtechceu.gtceu.common.pipelike.SegmentPropertyTypes;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeType, ItemPipeProperties> {

    public ItemPipeBlock(Properties properties, ItemPipeType itemPipeType, Material material) {
        super(properties, itemPipeType, material, material.getProperty(PropertyKey.ITEM_PIPE));
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<ItemPipeType>> getBlockEntityType() {
        return GTBlockEntities.ITEM_PIPE.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        FloatSegmentProperty transferRate = defaultSegmentProperties.getProperty(SegmentPropertyTypes.TRANSFER_RATE);
        IntSegmentProperty priority = defaultSegmentProperties.getProperty(SegmentPropertyTypes.PRIORITY);

        if (transferRate.getValue() % 1 != 0) {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate",
                    (int) ((transferRate.getValue() * 64) + 0.5)));
        } else {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks",
                    transferRate.getValue().intValue()));
        }

        tooltip.add(Component.translatable("gtceu.item_pipe.priority", priority.getValue()));
    }

    @Override
    public PipeNetworkType getPipeType() {
        return GTPipeNetworks.ITEM;
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public boolean canPipeConnectToBlock(PipeBlockEntity<ItemPipeType> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null &&
                tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).isPresent();
    }
}
