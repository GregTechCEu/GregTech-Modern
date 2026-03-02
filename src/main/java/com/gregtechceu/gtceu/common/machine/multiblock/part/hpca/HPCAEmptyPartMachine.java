package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAEmptyPartMachine extends HPCAComponentPartMachine {

    public HPCAEmptyPartMachine(BlockEntityCreationInfo info) {
        super(info, (machine) -> new HPCAComponentTrait(machine, 0, 0, false, false));
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
