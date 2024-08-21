package com.gregtechceu.gtceu.api.graphnet.pipenet.physical;

import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.Nullable;

public interface IPipeCapabilityObject {

    void setTile(PipeBlockEntity tile);

    Capability<?>[] getCapabilities();

    <T> T getCapabilityForSide(Capability<T> capability, @Nullable Direction facing);
}
