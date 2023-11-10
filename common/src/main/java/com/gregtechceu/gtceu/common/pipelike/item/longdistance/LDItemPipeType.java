package com.gregtechceu.gtceu.common.pipelike.item.longdistance;

import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistancePipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.world.level.block.state.BlockState;

public class LDItemPipeType extends LongDistancePipeType {

    public static final LDItemPipeType INSTANCE = new LDItemPipeType();

    private LDItemPipeType() {
        super("item");
    }

    @Override
    public boolean isValidBlock(BlockState blockState) {
        return GTBlocks.LD_ITEM_PIPE.is(blockState.getBlock());
    }

    @Override
    public boolean isValidEndpoint(ILDEndpoint endpoint) {
        return endpoint instanceof LDItemEndpointMachine;
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldItemPipeMinDistance;
    }
}