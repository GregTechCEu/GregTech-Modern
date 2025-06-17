package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.client.renderer.machine.DynamicMachineRendererRegistry.getId;
;

@FunctionalInterface
public interface DynamicMachineRendererType extends Comparable<DynamicMachineRendererType> {

    DynamicMachineRenderer makeModel(MachineModel parent);

    @Override
    default int compareTo(@NotNull DynamicMachineRendererType o) {
        return getId(this).compareTo(getId(o));
    }

}
