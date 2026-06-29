package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleMachineBuilder<P> extends MachineBuilder<MachineDefinition, P, SimpleMachineBuilder<P>> {

    public SimpleMachineBuilder(GTRegistrate owner, P parent, String name, BuilderCallback callback,
                                Function<ResourceLocation, MachineDefinition> definition,
                                BiFunction<BlockBehaviour.Properties, MachineDefinition, MetaMachineBlock> blockFactory,
                                BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        super(owner, parent, name, callback, definition, blockFactory, itemFactory, blockEntityFactory);
    }
}
