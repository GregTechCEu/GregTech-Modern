package com.gregtechceu.gtceu.forge.core.mixins.mekanism;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.*;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BasicChemicalTank.class)
public abstract class BasicChemicalTankMixin <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IChemicalTank<CHEMICAL, STACK>,
        IChemicalHandler<CHEMICAL, STACK>, ITagSerializable<CompoundTag>, IContentChangeAware {

    @Mutable
    @Shadow @Final
    private IContentsListener listener;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.put(NBTConstants.STORED, getStack().write(new CompoundTag()));
        }
        return nbt;
    }

    @Override
    public void setOnContentsChanged(Runnable runnable) {
        this.listener = runnable::run;
    }

    @Override
    public Runnable getOnContentsChanged() {
        return this.listener::onContentsChanged;
    }
}
