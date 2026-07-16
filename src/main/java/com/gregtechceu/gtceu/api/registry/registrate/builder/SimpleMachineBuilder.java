package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleMachineBuilder<P, M extends MetaMachine> extends MachineBuilder<MachineDefinition, M, P, SimpleMachineBuilder<P, M>> {

    public SimpleMachineBuilder(GTRegistrate owner, P parent, String name, BuilderCallback callback,
                                Function<ResourceLocation, MachineDefinition> definition,
                                BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                MachineInstanceFactory<M> instanceFactory) {
        super(owner, parent, name, callback, definition, blockFactory, itemFactory, instanceFactory);
    }
}
