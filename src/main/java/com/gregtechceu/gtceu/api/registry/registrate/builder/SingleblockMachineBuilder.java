package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.builders.BuilderCallback;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SingleblockMachineBuilder<P, M extends MetaMachine>
                                      extends MachineBuilder<MachineDefinition, M, P, SingleblockMachineBuilder<P, M>> {

    public SingleblockMachineBuilder(GTRegistrate owner, P parent, String name, BuilderCallback callback,
                                     Function<ResourceLocation, MachineDefinition> definition,
                                     BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                     BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                     MachineInstanceFactory<M> blockEntityFactory) {
        super(owner, parent, name, callback, definition, blockFactory, itemFactory, blockEntityFactory);
    }

    @Override
    protected MachineEntry.Singleblock createEntryWrapper(DeferredHolder<MachineDefinition, MachineDefinition> delegate) {
        return new MachineEntry.Singleblock(getOwner(), delegate);
    }

    @Override
    public MachineEntry.Singleblock register() {
        return (MachineEntry.Singleblock) super.register();
    }
}
