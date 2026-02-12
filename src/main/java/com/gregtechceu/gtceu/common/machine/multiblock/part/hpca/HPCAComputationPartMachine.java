package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComputationProviderTrait;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAComputationPartMachine extends HPCAComponentPartMachine {

    @Getter
    private final boolean advanced;

    public HPCAComputationPartMachine(BlockEntityCreationInfo info, boolean advanced) {
        super(info, (m) -> createHPCATrait(m, advanced));
        this.advanced = advanced;
    }

    public static HPCAComponentTrait createHPCATrait(HPCAComponentPartMachine machine, boolean isAdvanced) {
        int upkeepEUt = GTValues.VA[isAdvanced ? GTValues.IV : GTValues.EV];
        int maxEUt = GTValues.VA[isAdvanced ? GTValues.ZPM : GTValues.LuV];
        int cooling = isAdvanced ? 4 : 2;
        int cwu = isAdvanced ? 16 : 4;
        return new HPCAComputationProviderTrait(machine, upkeepEUt, maxEUt, true, false, cwu, cooling);
    }

    @Override
    public ResourceTexture getComponentIcon() {
        if (hpcaComponentTrait.isDamaged()) {
            return advanced ? GuiTextures.HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT :
                GuiTextures.HPCA_ICON_COMPUTATION_COMPONENT;
    }
}
