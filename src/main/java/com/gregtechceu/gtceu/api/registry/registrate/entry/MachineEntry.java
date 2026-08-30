package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.util.entry.RegistryEntry;

public class MachineEntry<T extends MachineDefinition> extends RegistryEntry<MachineDefinition, T> implements ItemLike {

    public MachineEntry(GTRegistrate owner, DeferredHolder<MachineDefinition, T> key) {
        super(owner, key);
    }

    public int getTier() {
        return value().getTier();
    }

    public MetaMachineBlock getBlock() {
        return value().getBlock();
    }

    public MetaMachineItem getItem() {
        return value().getItem();
    }

    public BlockEntityType<? extends MetaMachine> getBlockEntityType() {
        return value().getBlockEntityType();
    }

    @Override
    public Item asItem() {
        return getItem();
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }
}
