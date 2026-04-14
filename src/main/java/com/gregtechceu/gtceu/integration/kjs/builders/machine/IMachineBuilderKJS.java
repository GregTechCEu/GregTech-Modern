package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;

import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.providers.DataGenContext;
import org.jetbrains.annotations.Nullable;

public interface IMachineBuilderKJS {

    void generateMachineModels();

    default void generateMachineModel(@Nullable MachineBuilder<?, ?> builder, @Nullable MachineDefinition definition) {
        if (builder == null || definition == null) return;
        if (builder.model() == null && builder.blockModel() == null) return;

        // Fake a data provider for the GT model builders
        DataGenContext<Block, MetaMachineBlock> context = new DataGenContext<>(definition::get,
                definition.getName(), definition.getId());
        if (builder.blockModel() != null) {
            builder.blockModel().accept(context, RuntimeBlockstateProvider.INSTANCE);
        } else {
            GTMachineModels.createMachineModel(builder.model()).accept(context, RuntimeBlockstateProvider.INSTANCE);
        }
    }
}
