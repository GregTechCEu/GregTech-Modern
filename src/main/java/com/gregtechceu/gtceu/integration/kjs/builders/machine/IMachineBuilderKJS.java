package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;

import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.providers.DataGenContext;
import org.jetbrains.annotations.Nullable;

public interface IMachineBuilderKJS {

    void generateMachineModels();

    default void generateMachineModel(@Nullable MachineBuilder<?, ?, ?> builder,
                                      @Nullable MachineDefinition definition) {
        if (builder == null || definition == null) return;
        if (builder.properties().model() == null && builder.properties().blockModel() == null) return;

        // Fake a data provider for the GT model builders
        DataGenContext<Block, MetaMachineBlock> context = new DataGenContext<>(definition::getBlock,
                definition.getName(), definition.getId());
        if (builder.properties().blockModel() != null) {
            builder.properties().blockModel().accept(context, RuntimeBlockstateProvider.INSTANCE);
        } else {
            GTMachineModels.createMachineModel(builder.properties().model()).accept(context,
                    RuntimeBlockstateProvider.INSTANCE);
        }
    }
}
