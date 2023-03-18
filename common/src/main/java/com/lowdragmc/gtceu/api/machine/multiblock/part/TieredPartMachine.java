package com.lowdragmc.gtceu.api.machine.multiblock.part;

import com.lowdragmc.gtceu.api.machine.IMetaMachineBlockEntity;
import com.lowdragmc.gtceu.api.machine.feature.ITieredMachine;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote TieredPartMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TieredPartMachine extends MultiblockPartMachine implements ITieredMachine {

    @Getter
    protected final int tier;

    public TieredPartMachine(IMetaMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

}
