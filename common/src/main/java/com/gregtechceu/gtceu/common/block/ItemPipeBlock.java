package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeData;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.LevelItemPipeNet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeType, ItemPipeData, LevelItemPipeNet> {
    public ItemPipeBlock(Properties properties, ItemPipeType itemPipeType, Material material) {
        super(properties, itemPipeType, material);
    }

    @Override
    protected ItemPipeData createMaterialData() {
        return new ItemPipeData(material.getProperty(PropertyKey.ITEM_PIPE), (byte) 0);
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
    public BlockEntityType<? extends PipeBlockEntity<ItemPipeType, ItemPipeData>> getBlockEntityType() {
        return GTBlockEntities.ITEM_PIPE.get();
    }
}
