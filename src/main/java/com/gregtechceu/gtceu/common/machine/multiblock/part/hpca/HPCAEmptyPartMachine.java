package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.misc.MonitorComponentIcons;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComponentTrait;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

public class HPCAEmptyPartMachine extends HPCAComponentPartMachine {

    public HPCAEmptyPartMachine(BlockEntityCreationInfo info) {
        super(info, (machine) -> new HPCAComponentTrait(machine, 0, 0, false, false));
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return MonitorComponentIcons.hpcaEmptyComponent();
    }
}
