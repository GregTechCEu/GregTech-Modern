package com.gregtechceu.gtceu.common.pipelike.item.longdistance;

import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistancePipeType;
import com.gregtechceu.gtceu.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.world.level.block.state.BlockState;

public class LDItemPipeType extends LongDistancePipeType {

    public static final LDItemPipeType INSTANCE = new LDItemPipeType();

    private LDItemPipeType() {
        super("item");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldItemPipeMinDistance;
    }
}