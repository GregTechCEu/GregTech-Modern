package com.gregtechceu.gtceu.common.machine.multiblock.part.hpca;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.misc.MonitorComponentIcons;
import com.gregtechceu.gtceu.api.machine.trait.hpca.HPCAComponentTrait;

public class HPCABridgePartMachine extends HPCAComponentPartMachine {

    public HPCABridgePartMachine(BlockEntityCreationInfo info) {
        super(info, (m) -> new HPCAComponentTrait(m, GTValues.VA[GTValues.IV], GTValues.VA[GTValues.IV], false, true));
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public Object getComponentIcon() {
        return MonitorComponentIcons.hpcaBridgeComponent();
    }
}
