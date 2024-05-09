package com.gregtechceu.gtceu.common.machines.multiblock.primitive;

import com.gregtechceu.gtceu.api.machines.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machines.feature.IFancyUIMachine;

public class PrimitiveFancyUIWorkableMachine extends PrimitiveWorkableMachine implements IFancyUIMachine {

    public PrimitiveFancyUIWorkableMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }
}
