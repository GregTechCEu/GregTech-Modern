package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;

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
public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeType, ItemPipeProperties> {

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
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public LevelPipeNet<ItemPipeProperties, ItemPipeNet> getWorldPipeNet(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelPipeNet<>(serverLevel, ItemPipeNet::new),
                () -> new LevelPipeNet<>(serverLevel, ItemPipeNet::new), "gtceu_item_pipe_net");
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
}
