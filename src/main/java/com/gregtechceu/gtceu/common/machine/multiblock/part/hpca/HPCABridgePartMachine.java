package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.drawable.IDrawable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCABridgePartMachine extends HPCAComponentPartMachine {

    public HPCABridgePartMachine(BlockEntityCreationInfo info) {
        super(info, new HPCAComponentTrait(GTValues.VA[GTValues.IV], GTValues.VA[GTValues.IV], false, true));
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public IDrawable getComponentIcon() {
        return GTGuiTextures.HPCA_BRIDGE_COMPONENT;
    }
}
