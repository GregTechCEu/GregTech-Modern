package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;

import brachy.modularui.api.drawable.IDrawable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAEmptyPartMachine extends HPCAComponentPartMachine {

    public HPCAEmptyPartMachine(BlockEntityCreationInfo info) {
        super(info, new HPCAComponentTrait(0, 0, false, false));
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public IDrawable getComponentIcon() {
        return GTGuiTextures.HPCA_EMPTY_COMPONENT;
    }
}
