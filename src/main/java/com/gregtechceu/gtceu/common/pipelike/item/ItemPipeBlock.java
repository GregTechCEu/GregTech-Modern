package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeSegmentProperties> {

    public ItemPipeBlock(Properties properties, ItemPipeVariant itemPipeType, Material material) {
        super(properties, itemPipeType, GTPipeNetworks.ITEM, material);
    }
}
