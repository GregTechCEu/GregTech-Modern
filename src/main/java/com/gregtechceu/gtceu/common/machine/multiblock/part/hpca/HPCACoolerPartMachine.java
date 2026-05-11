package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCACoolantProviderTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCACoolerPartMachine extends HPCAComponentPartMachine {

    @Getter
    private final boolean advanced;

    public HPCACoolerPartMachine(BlockEntityCreationInfo info, boolean advanced) {
        super(info, createHPCATrait(advanced));
        this.advanced = advanced;
    }

    public static HPCAComponentTrait createHPCATrait(boolean isAdvanced) {
        int upkeepEU = isAdvanced ? GTValues.VA[GTValues.IV] : 0;
        int coolingAmount = isAdvanced ? 2 : 1;
        int maxCoolant = isAdvanced ? 8 : 0;
        return new HPCACoolantProviderTrait(upkeepEU, upkeepEU, false, false, coolingAmount, maxCoolant,
                isAdvanced);
    }

    @Override
    public IDrawable getComponentIcon() {
        return advanced ? GTGuiTextures.HPCA_ACTIVE_COOLER_COMPONENT : GTGuiTextures.HPCA_HEAT_SINK_COMPONENT;
    }
}
