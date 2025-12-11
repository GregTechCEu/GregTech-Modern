package com.gregtechceu.gtceu.api.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredPartMachine extends MultiblockPartMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info);
        this.tier = tier;
    }
}
