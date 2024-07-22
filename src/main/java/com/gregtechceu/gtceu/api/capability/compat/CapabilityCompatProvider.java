package com.gregtechceu.gtceu.api.capability.compat;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public abstract class CapabilityCompatProvider implements ICapabilityProvider {

    private final ICapabilityProvider upvalue;

    public CapabilityCompatProvider(ICapabilityProvider upvalue) {
        this.upvalue = upvalue;
    }

    protected <T> LazyOptional<T> getUpvalueCapability(Capability<T> capability, Direction facing) {
        return upvalue.getCapability(capability, facing);
    }
}
