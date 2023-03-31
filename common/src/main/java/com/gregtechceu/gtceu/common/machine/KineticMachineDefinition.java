package com.gregtechceu.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineDefinition
 */
public class KineticMachineDefinition extends MachineDefinition {
    @Getter
    public final boolean isSource;
    @Getter
    public final float torque;

    public KineticMachineDefinition(ResourceLocation id, boolean isSource, float torque) {
        super(id);
        this.isSource = isSource;
        this.torque = torque;
    }

}
