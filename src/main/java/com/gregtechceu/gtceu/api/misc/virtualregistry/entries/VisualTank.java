package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.capability.templates.FluidTank;

@Accessors(chain = true)
public class VisualTank extends FluidTank implements INBTSerializable<CompoundTag>, ITagSerializable<CompoundTag> {
    @Getter
    private VirtualTank virtualTank;
    @Setter
    private Runnable onChange;

    public VisualTank(VirtualTank tank) {
        super(VirtualTank.DEFAULT_CAPACITY);
        setVirtualTank(tank);
    }

    public VisualTank() {
        super(VirtualTank.DEFAULT_CAPACITY);
        setVirtualTank(new VirtualTank());
    }

    public void setVirtualTank(VirtualTank tank) {
        virtualTank = tank;
        setFluid(tank.getFluid());
        virtualTank.setOnChange(() -> setFluid(virtualTank.getFluid()));
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        virtualTank.setFluid(getFluid());
        if (onChange != null) onChange.run();
    }

    @Override
    public CompoundTag serializeNBT() {
        return virtualTank.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        virtualTank.deserializeNBT(nbt);
        fluid = virtualTank.getFluid();
    }
}
