package com.gregtechceu.gtceu.api.machines.multiblock.part;

import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.feature.ITieredMachine;
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

    public TieredPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

}
