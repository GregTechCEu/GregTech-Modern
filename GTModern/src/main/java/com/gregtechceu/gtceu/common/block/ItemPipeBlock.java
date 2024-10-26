package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.LevelItemPipeNet;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeType, ItemPipeProperties, LevelItemPipeNet> {

    public ItemPipeBlock(Properties properties, ItemPipeType itemPipeType, Material material) {
        super(properties, itemPipeType, material);
    }

    @Override
    protected ItemPipeProperties createProperties(ItemPipeType itemPipeType, Material material) {
        return itemPipeType.modifyProperties(material.getProperty(PropertyKey.ITEM_PIPE));
    }

    @Override
    protected ItemPipeProperties createMaterialData() {
        return material.getProperty(PropertyKey.ITEM_PIPE);
    }

    @Override
    protected PipeModel createPipeModel() {
        return pipeType.createPipeModel(material);
    }

    @Override
    public LevelItemPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelItemPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<ItemPipeType, ItemPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.ITEM_PIPE.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ItemPipeProperties properties = createProperties(defaultBlockState(), stack);

        if (properties.getTransferRate() % 1 != 0) {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate",
                    (int) ((properties.getTransferRate() * 64) + 0.5)));
        } else {
            tooltip.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks",
                    (int) properties.getTransferRate()));
        }

        tooltip.add(Component.translatable("gtceu.item_pipe.priority", properties.getPriority()));
    }

    @Override
    public boolean canPipesConnect(IPipeNode<ItemPipeType, ItemPipeProperties> selfTile, Direction side,
                                   IPipeNode<ItemPipeType, ItemPipeProperties> sideTile) {
        return selfTile instanceof ItemPipeBlockEntity && sideTile instanceof ItemPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<ItemPipeType, ItemPipeProperties> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null &&
                tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).isPresent();
    }
}
