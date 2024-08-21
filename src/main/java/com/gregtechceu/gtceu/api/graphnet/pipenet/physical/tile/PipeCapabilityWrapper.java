package com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile;

import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PipeCapabilityWrapper {

    private byte activeMask;
    private final PipeBlockEntity owner;
    private final WorldPipeNetNode node;
    public final Capability<?>[] capabilities;

    public PipeCapabilityWrapper(PipeBlockEntity owner, @NotNull WorldPipeNetNode node) {
        this.owner = owner;
        this.node = node;
        this.capabilities = node.getNet().getTargetCapabilities();
    }

    public boolean supports(Capability<?> capability) {
        for (Capability<?> cap : capabilities) {
            if (Objects.equals(cap, capability)) return true;
        }
        return false;
    }

    public void setActive(@NotNull Direction facing) {
        if (!isActive(facing)) {
            this.activeMask |= (byte) (1 << facing.ordinal());
            this.node.setActive(this.activeMask > 0);
            this.owner.notifyBlockUpdate();
        }
    }

    public void setIdle(@NotNull Direction facing) {
        if (isActive(facing)) {
            this.activeMask &= (byte) ~(1 << facing.ordinal());
            this.node.setActive(this.activeMask > 0);
            this.owner.notifyBlockUpdate();
        }
    }

    public boolean isActive(@NotNull Direction facing) {
        return (this.activeMask & 1 << facing.ordinal()) > 0;
    }
}
