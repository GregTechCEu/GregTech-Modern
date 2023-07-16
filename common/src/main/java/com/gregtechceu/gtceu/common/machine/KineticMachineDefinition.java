package com.gregtechceu.gtceu.common.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineDefinition
 */
@Accessors(chain = true)
public class KineticMachineDefinition extends MachineDefinition {
    @Getter
    public final boolean isSource;
    @Getter
    public final float torque;
    /**
     * false (default) - rotation axis = frontFacing clockWise axis
     * <br>
     * true - rotation axis = frontFacing axis
     */
    @Getter
    @Setter
    public boolean frontRotation;

    public KineticMachineDefinition(ResourceLocation id, boolean isSource, float torque) {
        super(id);
        this.isSource = isSource;
        this.torque = torque;
    }

}
