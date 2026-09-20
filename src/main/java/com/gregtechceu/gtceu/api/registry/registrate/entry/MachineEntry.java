package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;

public class MachineEntry<T extends MachineDefinition> extends ItemProviderEntry<MachineDefinition, T> {

    public MachineEntry(GTRegistrate owner, DeferredHolder<MachineDefinition, T> key) {
        super(owner, key);
    }

    public int getTier() {
        return value().getTier();
    }

    public MetaMachineBlock getBlock() {
        return value().getBlock();
    }

    public BlockEntityType<? extends MetaMachine> getBlockEntityType() {
        return value().getBlockEntityType();
    }
}
