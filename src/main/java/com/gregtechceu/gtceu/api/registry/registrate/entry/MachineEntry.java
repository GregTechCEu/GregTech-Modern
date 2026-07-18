package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class MachineEntry<T extends MachineDefinition> extends ItemProviderEntry<MachineDefinition, T> {

    public MachineEntry(AbstractRegistrate<?> owner, DeferredHolder<MachineDefinition, T> key) {
        super(owner, key);
    }

    public boolean isTiered() {
        return get().isTiered();
    }

    public int getTier() {
        return get().getTier();
    }

    public MetaMachineBlock getBlock() {
        return get().getBlock();
    }

    public MetaMachineItem getItem() {
        return get().getItem();
    }

    @Override
    public MetaMachineItem asItem() {
        return get().asItem();
    }

    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return get().getBlockEntityType();
    }

    public BlockState getDefaultState() {
        return get().defaultBlockState();
    }

    public boolean has(BlockState state) {
        return is(state.getBlock());
    }

    public static <T extends MachineDefinition> MachineEntry<T> cast(RegistryEntry<MachineDefinition, T> entry) {
        return RegistryEntry.cast(MachineEntry.class, entry);
    }

    public static final class Singleblock extends MachineEntry<MachineDefinition> {

        public Singleblock(AbstractRegistrate<?> owner, DeferredHolder<MachineDefinition, MachineDefinition> key) {
            super(owner, key);
        }
    }

    public static final class Multiblock extends MachineEntry<MultiblockMachineDefinition> {

        public Multiblock(AbstractRegistrate<?> owner,
                          DeferredHolder<MachineDefinition, MultiblockMachineDefinition> key) {
            super(owner, key);
        }
    }
}
