package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComputationProviderTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAComputationPartMachine extends HPCAComponentPartMachine {

    @Getter
    private final boolean advanced;

    public HPCAComputationPartMachine(BlockEntityCreationInfo info, boolean advanced) {
        super(info, createHPCATrait(advanced));
        this.advanced = advanced;
    }

    public static HPCAComponentTrait createHPCATrait(boolean isAdvanced) {
        int upkeepEUt = GTValues.VA[isAdvanced ? GTValues.IV : GTValues.EV];
        int maxEUt = GTValues.VA[isAdvanced ? GTValues.ZPM : GTValues.LuV];
        int cooling = isAdvanced ? 4 : 2;
        int cwu = isAdvanced ? 16 : 4;
        return new HPCAComputationProviderTrait(upkeepEUt, maxEUt, true, false, cwu, cooling);
    }

    @Override
    public IDrawable getComponentIcon() {
        if (hpcaComponentTrait.isDamaged()) {
            return advanced ? GTGuiTextures.HPCA_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GTGuiTextures.HPCA_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GTGuiTextures.HPCA_ADVANCED_COMPUTATION_COMPONENT :
                GTGuiTextures.HPCA_COMPUTATION_COMPONENT;
    }
}
