package com.gregtechceu.gtceu.integration.kjs.builders.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.data.model.GTMachineModels;

import com.tterrag.registrate.providers.DataGenContext;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.integration.kjs.GTKubeJSPlugin.RUNTIME_BLOCKSTATE_PROVIDER;

public interface IMachineBuilderKJS {

    void generateMachineModels();

    default void generateMachineModel(@Nullable MachineBuilder<?, ?> builder, @Nullable MachineDefinition definition) {
        if (builder == null || definition == null) return;
        if (builder.model() == null && builder.blockModel() == null) return;

        // Fake a data provider for the GT model builders
        var context = new DataGenContext<>(definition::getBlock, definition.getName(), definition.getId());
        if (builder.blockModel() != null) {
            builder.blockModel().accept(context, RUNTIME_BLOCKSTATE_PROVIDER);
        } else {
            GTMachineModels.createMachineModel(builder.model()).accept(context, RUNTIME_BLOCKSTATE_PROVIDER);
        }
    }
}
