package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.LevelItemPipeNet;
import com.gregtechceu.gtceu.data.blockentity.GTBlockEntities;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

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

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (level.isClientSide) return null;
            if (blockEntity instanceof ItemPipeBlockEntity itemPipeBlockEntity) {
                if (side != null && itemPipeBlockEntity.isConnected(side)) {
                    itemPipeBlockEntity.ensureHandlersInitialized();
                    itemPipeBlockEntity.checkNetwork();
                    return itemPipeBlockEntity.getHandler(side, true);
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PipeBlockEntity<?, ?> fluidPipeBlockEntity) {
                return fluidPipeBlockEntity.getCoverContainer();
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_TOOLABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IToolable toolable) {
                return toolable;
            }
            return null;
        }, this);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
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
        return tile != null && tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos(),
                tile.getBlockState(), tile, side.getOpposite()) != null;
    }
}
